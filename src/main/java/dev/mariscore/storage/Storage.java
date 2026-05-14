package dev.mariscore.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.mariscore.MarisCorePlugin;
import dev.mariscore.config.Settings;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Storage {
    private final MarisCorePlugin plugin;
    private final Settings s;
    private final ConcurrentMap<UUID, Account> cache = new ConcurrentHashMap<>();
    private volatile HikariDataSource ds;
    private volatile boolean closed;

    public Storage(MarisCorePlugin plugin, Settings s) { this.plugin = plugin; this.s = s; }

    public void init() {
        HikariConfig c = new HikariConfig();
        if (s.type.equals("mysql")) {
            c.setJdbcUrl("jdbc:mysql://" + s.host + ":" + s.port + "/" + s.database + "?useSSL=" + s.ssl + "&serverTimezone=UTC");
            c.setUsername(s.username); c.setPassword(s.password); c.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            c.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder() + "/mariscore.db"); c.setDriverClassName("org.sqlite.JDBC"); c.setMaximumPoolSize(1);
        }
        if (!s.type.equals("sqlite")) { c.setMaximumPoolSize(s.maxPool); c.setMinimumIdle(s.minIdle); }
        ds = new HikariDataSource(c); closed = false;
        runSync("CREATE TABLE IF NOT EXISTS maris_accounts (uuid VARCHAR(36) PRIMARY KEY,name VARCHAR(16),money DECIMAL(32,2) NOT NULL DEFAULT 0,shards DECIMAL(32,2) NOT NULL DEFAULT 0,updated_at BIGINT NOT NULL)");
    }

    private void runSync(String sql) { try (var cn = ds.getConnection(); var st = cn.createStatement()) { st.execute(sql); } catch (SQLException e) { throw new RuntimeException(e); } }
    public boolean isClosed() { HikariDataSource current = ds; return closed || current == null || current.isClosed(); }

    public Optional<Account> cached(UUID id) { return Optional.ofNullable(cache.get(id)); }
    public BigDecimal balanceCached(UUID id, String col) {
        Account a = cache.get(id); if (a == null) return BigDecimal.ZERO;
        return col.equalsIgnoreCase("shards") ? a.shards() : a.money();
    }

    public CompletableFuture<Void> ensure(UUID id, String name, BigDecimal money, BigDecimal shards) {
        return task(() -> {
            if (isClosed()) return null;
            try (var cn = ds.getConnection()) {
                try (PreparedStatement ps = cn.prepareStatement("INSERT INTO maris_accounts(uuid,name,money,shards,updated_at) VALUES(?,?,?,?,?)")) {
                    ps.setString(1, id.toString()); ps.setString(2, name); ps.setBigDecimal(3, money); ps.setBigDecimal(4, shards); ps.setLong(5, System.currentTimeMillis()); ps.executeUpdate();
                    cache.put(id, new Account(id, name, money, shards));
                } catch (SQLException ex) {
                    try (PreparedStatement up = cn.prepareStatement("UPDATE maris_accounts SET name=?,updated_at=? WHERE uuid=?")) {
                        up.setString(1, name); up.setLong(2, System.currentTimeMillis()); up.setString(3, id.toString()); up.executeUpdate();
                    }
                    try (PreparedStatement select = cn.prepareStatement("SELECT * FROM maris_accounts WHERE uuid=?")) {
                        select.setString(1, id.toString());
                        try (ResultSet rs = select.executeQuery()) {
                            if (rs.next()) {
                                Account account = map(rs);
                                cache.put(account.uuid(), account);
                            }
                        }
                    }
                }
            }
            return null;
        });
    }

    public CompletableFuture<Optional<Account>> byName(String name) { return task(() -> {
        if (isClosed()) return Optional.empty();
        try (var cn = ds.getConnection(); var ps = cn.prepareStatement("SELECT * FROM maris_accounts WHERE LOWER(name)=LOWER(?)")) {
            ps.setString(1, name); try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) return Optional.empty(); Account a = map(rs); cache.put(a.uuid(), a); return Optional.of(a); }
        }
    }); }

    public CompletableFuture<Optional<Account>> byId(UUID id) { return task(() -> {
        if (isClosed()) return Optional.ofNullable(cache.get(id));
        Optional<Account> a = loadByIdSync(id); a.ifPresent(acc -> cache.put(acc.uuid(), acc)); return a;
    }); }

    private Optional<Account> loadByIdSync(UUID id) throws SQLException {
        try (var cn = ds.getConnection(); var ps = cn.prepareStatement("SELECT * FROM maris_accounts WHERE uuid=?")) {
            ps.setString(1, id.toString()); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        }
    }

    public CompletableFuture<BigDecimal> balance(UUID id, String col) { return task(() -> {
        if (isClosed()) return balanceCached(id, col);
        try (var cn = ds.getConnection(); var ps = cn.prepareStatement("SELECT " + col + " FROM maris_accounts WHERE uuid=?")) {
            ps.setString(1, id.toString()); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO; }
        }
    }); }

    public CompletableFuture<Void> add(UUID id, String col, BigDecimal delta) { return task(() -> {
        if (isClosed()) return null;
        try (var cn = ds.getConnection(); var ps = cn.prepareStatement("UPDATE maris_accounts SET " + col + "=" + col + "+?,updated_at=? WHERE uuid=?")) {
            ps.setBigDecimal(1, delta); ps.setLong(2, System.currentTimeMillis()); ps.setString(3, id.toString()); ps.executeUpdate();
        }
        cache.compute(id, (k, a) -> {
            if (a == null) return null;
            return col.equalsIgnoreCase("shards") ? new Account(a.uuid(), a.name(), a.money(), a.shards().add(delta)) : new Account(a.uuid(), a.name(), a.money().add(delta), a.shards());
        });
        return null;
    }); }

    public CompletableFuture<Void> set(UUID id, String col, BigDecimal val) { return task(() -> {
        if (isClosed()) return null;
        try (var cn = ds.getConnection(); var ps = cn.prepareStatement("UPDATE maris_accounts SET " + col + "=?,updated_at=? WHERE uuid=?")) {
            ps.setBigDecimal(1, val); ps.setLong(2, System.currentTimeMillis()); ps.setString(3, id.toString()); ps.executeUpdate();
        }
        cache.compute(id, (k, a) -> {
            if (a == null) return null;
            return col.equalsIgnoreCase("shards") ? new Account(a.uuid(), a.name(), a.money(), val) : new Account(a.uuid(), a.name(), val, a.shards());
        });
        return null;
    }); }

    private Account map(ResultSet r) throws SQLException { return new Account(UUID.fromString(r.getString("uuid")), r.getString("name"), r.getBigDecimal("money"), r.getBigDecimal("shards")); }
    private <T> CompletableFuture<T> task(Callable<T> c) { CompletableFuture<T> f = new CompletableFuture<>(); if (isClosed() && closed) { try { f.complete(c.call()); } catch (Throwable e) { f.completeExceptionally(e); } return f; } plugin.scheduler().async(() -> { try { f.complete(c.call()); } catch (Throwable e) { plugin.getLogger().severe(String.valueOf(e.getMessage())); f.completeExceptionally(e); } }); return f; }
    public void close() { closed = true; HikariDataSource current = ds; if (current != null && !current.isClosed()) current.close(); }
    public record Account(UUID uuid, String name, BigDecimal money, BigDecimal shards) {}
}
