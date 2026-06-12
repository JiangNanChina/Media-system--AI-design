package com.example.photography.service;

import com.example.photography.dto.response.AmapLocationResponse;
import java.util.List;
import java.util.Map;

/**
 * 高德地图服务接口
 */
public interface AmapService {
    
    /**
     * 地理编码：地址转坐标
     * @param address 地址
     * @return 坐标信息
     */
    AmapLocationResponse geocoding(String address);
    
    /**
     * 逆地理编码：坐标转地址
     * @param longitude 经度
     * @param latitude 纬度
     * @return 地址信息
     */
    AmapLocationResponse reverseGeocoding(double longitude, double latitude);
    
    /**
     * 逆地理编码：坐标转地址（带扩展信息）
     * @param longitude 经度
     * @param latitude 纬度
     * @param extensions 扩展信息类型：base|all
     * @param roadlevel 道路等级：0|1
     * @return 地址信息
     */
    AmapLocationResponse reverseGeocodingExtended(double longitude, double latitude, String extensions, Integer roadlevel);
    
    /**
     * 地点搜索
     * @param keywords 搜索关键字
     * @param city 城市编码或名称
     * @param types 类型编码
     * @return 搜索结果
     */
    Map<String, Object> placeSearch(String keywords, String city, String types);
    
    /**
     * 周边搜索
     * @param keywords 搜索关键字
     * @param longitude 经度
     * @param latitude 纬度
     * @param radius 搜索半径(米)
     * @return 搜索结果
     */
    Map<String, Object> placeSearchAround(String keywords, double longitude, double latitude, int radius);
    
    /**
     * 路径规划
     * @param origin 起点坐标 "经度,纬度"
     * @param destination 终点坐标 "经度,纬度"
     * @return 路径规划结果
     */
    Map<String, Object> driving(String origin, String destination);
    
    /**
     * IP定位
     * @param ip IP地址，不传则为当前请求IP
     * @return 定位结果
     */
    Map<String, Object> ipLocation(String ip);
    
    /**
     * 获取静态地图图片URL
     * @param longitude 经度
     * @param latitude 纬度
     * @param zoom 缩放级别 1-17
     * @param size 地图大小 "宽*高" 最大1024*1024
     * @param markers 标记点信息
     * @return 静态地图URL
     */
    String getStaticMapUrl(double longitude, double latitude, int zoom, String size, String markers);
    
    /**
     * 计算两点之间的距离
     * @param lon1 起点经度
     * @param lat1 起点纬度
     * @param lon2 终点经度
     * @param lat2 终点纬度
     * @return 距离（米）
     */
    double calculateDistance(double lon1, double lat1, double lon2, double lat2);
    
    /**
     * 验证坐标是否在指定范围内
     * @param userLon 用户经度
     * @param userLat 用户纬度
     * @param centerLon 中心点经度
     * @param centerLat 中心点纬度
     * @param radius 允许范围（米）
     * @return 是否在范围内
     */
    boolean isLocationInRange(double userLon, double userLat, double centerLon, double centerLat, double radius);
    
    /**
     * 格式化地址信息
     * @param response 高德API响应
     * @return 格式化后的地址
     */
    String formatAddress(AmapLocationResponse response);
    
    /**
     * 获取附近的POI信息
     * @param longitude 经度
     * @param latitude 纬度
     * @return POI信息列表
     */
    List<Map<String, Object>> getNearbyPois(double longitude, double latitude);
}
