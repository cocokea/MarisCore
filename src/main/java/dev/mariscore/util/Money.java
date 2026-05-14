package dev.mariscore.util;
import java.math.*;import java.text.DecimalFormat;
public final class Money { private static final DecimalFormat DF=new DecimalFormat("#,##0.##");
 public static BigDecimal parse(String raw){String s=raw.trim().toLowerCase().replace(",",""); BigDecimal mul=BigDecimal.ONE; if(s.endsWith("k")){mul=BigDecimal.valueOf(1_000);s=s.substring(0,s.length()-1);} else if(s.endsWith("m")){mul=BigDecimal.valueOf(1_000_000);s=s.substring(0,s.length()-1);} else if(s.endsWith("b")){mul=BigDecimal.valueOf(1_000_000_000);s=s.substring(0,s.length()-1);} return new BigDecimal(s).multiply(mul).setScale(2,RoundingMode.DOWN); }
 public static String fmt(BigDecimal v){BigDecimal a=v.abs(); if(a.compareTo(BigDecimal.valueOf(1_000_000_000))>=0)return shortFmt(v,1_000_000_000,"B"); if(a.compareTo(BigDecimal.valueOf(1_000_000))>=0)return shortFmt(v,1_000_000,"M"); if(a.compareTo(BigDecimal.valueOf(1_000))>=0)return shortFmt(v,1_000,"K"); return DF.format(v);}
 private static String shortFmt(BigDecimal v,long d,String suf){return DF.format(v.divide(BigDecimal.valueOf(d),2,RoundingMode.DOWN))+suf;}
}
