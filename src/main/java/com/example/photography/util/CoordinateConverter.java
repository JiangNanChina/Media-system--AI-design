package com.example.photography.util;

/**
 * 坐标转换工具类
 * 用于在不同坐标系之间进行转换
 * 
 * 坐标系说明：
 * - WGS84: GPS原始坐标，国际标准（navigator.geolocation返回的坐标）
 * - GCJ-02: 火星坐标系，中国国家测绘局标准（高德地图、腾讯地图使用）
 * - BD-09: 百度坐标系（百度地图使用）
 * 
 * 转换关系：
 * WGS84 -> GCJ-02 -> BD-09
 */
public class CoordinateConverter {
    
    // 常量定义
    private static final double PI = Math.PI;
    private static final double A = 6378245.0; // 长半轴
    private static final double EE = 0.00669342162296594323; // 偏心率平方
    
    /**
     * WGS84 转 GCJ-02（火星坐标系）
     * 用于将GPS原始坐标转换为高德地图坐标
     * 
     * @param wgsLat WGS84纬度
     * @param wgsLng WGS84经度
     * @return [GCJ-02纬度, GCJ-02经度]
     */
    public static double[] wgs84ToGcj02(double wgsLat, double wgsLng) {
        if (outOfChina(wgsLat, wgsLng)) {
            return new double[]{wgsLat, wgsLng};
        }
        
        double dLat = transformLat(wgsLng - 105.0, wgsLat - 35.0);
        double dLng = transformLng(wgsLng - 105.0, wgsLat - 35.0);
        
        double radLat = wgsLat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI);
        
        double gcjLat = wgsLat + dLat;
        double gcjLng = wgsLng + dLng;
        
        return new double[]{gcjLat, gcjLng};
    }
    
    /**
     * GCJ-02（火星坐标系）转 WGS84
     * 用于将高德地图坐标转换为GPS原始坐标
     * 
     * @param gcjLat GCJ-02纬度
     * @param gcjLng GCJ-02经度
     * @return [WGS84纬度, WGS84经度]
     */
    public static double[] gcj02ToWgs84(double gcjLat, double gcjLng) {
        if (outOfChina(gcjLat, gcjLng)) {
            return new double[]{gcjLat, gcjLng};
        }
        
        double dLat = transformLat(gcjLng - 105.0, gcjLat - 35.0);
        double dLng = transformLng(gcjLng - 105.0, gcjLat - 35.0);
        
        double radLat = gcjLat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI);
        
        double wgsLat = gcjLat - dLat;
        double wgsLng = gcjLng - dLng;
        
        return new double[]{wgsLat, wgsLng};
    }
    
    /**
     * GCJ-02（火星坐标系）转 BD-09（百度坐标系）
     * 
     * @param gcjLat GCJ-02纬度
     * @param gcjLng GCJ-02经度
     * @return [BD-09纬度, BD-09经度]
     */
    public static double[] gcj02ToBd09(double gcjLat, double gcjLng) {
        double z = Math.sqrt(gcjLng * gcjLng + gcjLat * gcjLat) + 0.00002 * Math.sin(gcjLat * PI * 3000.0 / 180.0);
        double theta = Math.atan2(gcjLat, gcjLng) + 0.000003 * Math.cos(gcjLng * PI * 3000.0 / 180.0);
        
        double bdLng = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;
        
        return new double[]{bdLat, bdLng};
    }
    
    /**
     * BD-09（百度坐标系）转 GCJ-02（火星坐标系）
     * 
     * @param bdLat BD-09纬度
     * @param bdLng BD-09经度
     * @return [GCJ-02纬度, GCJ-02经度]
     */
    public static double[] bd09ToGcj02(double bdLat, double bdLng) {
        double x = bdLng - 0.0065;
        double y = bdLat - 0.006;
        
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * PI * 3000.0 / 180.0);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * PI * 3000.0 / 180.0);
        
        double gcjLng = z * Math.cos(theta);
        double gcjLat = z * Math.sin(theta);
        
        return new double[]{gcjLat, gcjLng};
    }
    
    /**
     * WGS84 转 BD-09（百度坐标系）
     * 
     * @param wgsLat WGS84纬度
     * @param wgsLng WGS84经度
     * @return [BD-09纬度, BD-09经度]
     */
    public static double[] wgs84ToBd09(double wgsLat, double wgsLng) {
        double[] gcj = wgs84ToGcj02(wgsLat, wgsLng);
        return gcj02ToBd09(gcj[0], gcj[1]);
    }
    
    /**
     * BD-09（百度坐标系）转 WGS84
     * 
     * @param bdLat BD-09纬度
     * @param bdLng BD-09经度
     * @return [WGS84纬度, WGS84经度]
     */
    public static double[] bd09ToWgs84(double bdLat, double bdLng) {
        double[] gcj = bd09ToGcj02(bdLat, bdLng);
        return gcj02ToWgs84(gcj[0], gcj[1]);
    }
    
    /**
     * 判断坐标是否在中国境外
     * 在中国境外不进行坐标转换
     */
    private static boolean outOfChina(double lat, double lng) {
        if (lng < 72.004 || lng > 137.8347) {
            return true;
        }
        if (lat < 0.8293 || lat > 55.8271) {
            return true;
        }
        return false;
    }
    
    /**
     * 纬度转换
     */
    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }
    
    /**
     * 经度转换
     */
    private static double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
    
    /**
     * 计算两点之间的距离（米）
     * 使用 Haversine 公式
     * 
     * @param lat1 第一个点的纬度
     * @param lng1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lng2 第二个点的经度
     * @return 距离（米）
     */
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371000; // 地球半径，单位：米
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}

