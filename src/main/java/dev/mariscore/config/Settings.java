package dev.mariscore.config;
import org.bukkit.plugin.java.JavaPlugin;
import java.math.BigDecimal;
public final class Settings {
  public final String type, host, database, username, password; public final int port, maxPool, minIdle; public final boolean ssl; public final BigDecimal startingMoney, startingShards;
  public Settings(JavaPlugin p){ var c=p.getConfig(); type=c.getString("storage.type","sqlite").toLowerCase(); host=c.getString("storage.mysql.host","localhost"); port=c.getInt("storage.mysql.port",3306); database=c.getString("storage.mysql.database","mariscore"); username=c.getString("storage.mysql.username","root"); password=c.getString("storage.mysql.password",""); ssl=c.getBoolean("storage.mysql.ssl",false); maxPool=c.getInt("storage.pool.maximumPoolSize",10); minIdle=c.getInt("storage.pool.minimumIdle",2); startingMoney=BigDecimal.valueOf(c.getDouble("settings.starting-money",0)); startingShards=BigDecimal.valueOf(c.getDouble("settings.starting-shards",0)); }
}
