package com.example.photography.controller;

import com.example.photography.config.AmapConfig;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.AmapLocationResponse;
import com.example.photography.service.AmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高德地图API控制器
 */
@RestController
@RequestMapping("/amap")
@Tag(name = "高德地图服务", description = "提供地理编码、逆地理编码、地点搜索、路径规划等地图服务")
public class AmapController {
    
    @Autowired
    private AmapService amapService;
    
    @Autowired
    private AmapConfig amapConfig;
    
    @GetMapping("/config")
    @Operation(summary = "获取前端地图配置", description = "获取前端使用的高德地图JS API Key等配置信息")
    public ApiResponse<Map<String, Object>> getMapConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("jsApiKey", amapConfig.getJsApiKey());
            config.put("mapStyle", "normal"); // 地图样式
            config.put("zoom", 15); // 默认缩放级别
            config.put("features", new String[]{"bg", "point", "road", "building"}); // 地图特性
            
            return ApiResponse.success("地图配置获取成功", config);
        } catch (Exception e) {
            return ApiResponse.error("获取地图配置失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/geocoding")
    @Operation(summary = "地理编码", description = "将地址转换为经纬度坐标")
    public ApiResponse<AmapLocationResponse> geocoding(@RequestParam String address) {
        try {
            AmapLocationResponse result = amapService.geocoding(address);
            return ApiResponse.success("地理编码成功", result);
        } catch (Exception e) {
            return ApiResponse.error("地理编码失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/reverse-geocoding")
    @Operation(summary = "逆地理编码", description = "将经纬度坐标转换为地址信息")
    public ApiResponse<AmapLocationResponse> reverseGeocoding(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "base") String extensions,
            @RequestParam(defaultValue = "0") Integer roadlevel) {
        try {
            AmapLocationResponse result = amapService.reverseGeocodingExtended(longitude, latitude, extensions, roadlevel);
            return ApiResponse.success("逆地理编码成功", result);
        } catch (Exception e) {
            return ApiResponse.error("逆地理编码失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/place/search")
    @Operation(summary = "地点搜索", description = "根据关键字搜索地点")
    public ApiResponse<Map<String, Object>> placeSearch(
            @RequestParam String keywords,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String types) {
        try {
            Map<String, Object> result = amapService.placeSearch(keywords, city, types);
            return ApiResponse.success("地点搜索成功", result);
        } catch (Exception e) {
            return ApiResponse.error("地点搜索失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/place/around")
    @Operation(summary = "周边搜索", description = "搜索指定位置周边的地点")
    public ApiResponse<Map<String, Object>> placeSearchAround(
            @RequestParam String keywords,
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "1000") int radius) {
        try {
            Map<String, Object> result = amapService.placeSearchAround(keywords, longitude, latitude, radius);
            return ApiResponse.success("周边搜索成功", result);
        } catch (Exception e) {
            return ApiResponse.error("周边搜索失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/direction/driving")
    @Operation(summary = "驾车路径规划", description = "规划两点之间的驾车路径")
    public ApiResponse<Map<String, Object>> driving(
            @RequestParam String origin,
            @RequestParam String destination) {
        try {
            Map<String, Object> result = amapService.driving(origin, destination);
            return ApiResponse.success("路径规划成功", result);
        } catch (Exception e) {
            return ApiResponse.error("路径规划失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/ip-location")
    @Operation(summary = "IP定位", description = "根据IP地址获取大致位置")
    public ApiResponse<Map<String, Object>> ipLocation(@RequestParam(required = false) String ip) {
        try {
            Map<String, Object> result = amapService.ipLocation(ip);
            return ApiResponse.success("IP定位成功", result);
        } catch (Exception e) {
            return ApiResponse.error("IP定位失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/static-map")
    @Operation(summary = "静态地图", description = "获取静态地图图片URL")
    public ApiResponse<String> getStaticMap(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "10") int zoom,
            @RequestParam(defaultValue = "400*300") String size,
            @RequestParam(required = false) String markers) {
        try {
            String mapUrl = amapService.getStaticMapUrl(longitude, latitude, zoom, size, markers);
            return ApiResponse.success("静态地图URL生成成功", mapUrl);
        } catch (Exception e) {
            return ApiResponse.error("静态地图URL生成失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/distance")
    @Operation(summary = "计算距离", description = "计算两点之间的直线距离")
    public ApiResponse<Map<String, Object>> calculateDistance(@RequestBody Map<String, Double> coordinates) {
        try {
            double lon1 = coordinates.get("lon1");
            double lat1 = coordinates.get("lat1");
            double lon2 = coordinates.get("lon2");
            double lat2 = coordinates.get("lat2");
            
            double distance = amapService.calculateDistance(lon1, lat1, lon2, lat2);
            
            Map<String, Object> result = new HashMap<>();
            result.put("distance", distance);
            result.put("distanceKm", distance / 1000);
            result.put("unit", "meters");
            
            return ApiResponse.success("距离计算成功", result);
        } catch (Exception e) {
            return ApiResponse.error("距离计算失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/location/validate")
    @Operation(summary = "位置验证", description = "验证用户位置是否在指定范围内")
    public ApiResponse<Map<String, Object>> validateLocation(@RequestBody Map<String, Object> request) {
        try {
            double userLon = ((Number) request.get("userLon")).doubleValue();
            double userLat = ((Number) request.get("userLat")).doubleValue();
            double centerLon = ((Number) request.get("centerLon")).doubleValue();
            double centerLat = ((Number) request.get("centerLat")).doubleValue();
            double radius = ((Number) request.get("radius")).doubleValue();
            
            boolean inRange = amapService.isLocationInRange(userLon, userLat, centerLon, centerLat, radius);
            double distance = amapService.calculateDistance(userLon, userLat, centerLon, centerLat);
            
            Map<String, Object> result = new HashMap<>();
            result.put("inRange", inRange);
            result.put("distance", distance);
            result.put("allowedRadius", radius);
            result.put("message", inRange ? "位置验证通过" : "超出允许范围");
            
            return ApiResponse.success("位置验证完成", result);
        } catch (Exception e) {
            return ApiResponse.error("位置验证失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/pois/nearby")
    @Operation(summary = "附近POI", description = "获取指定位置附近的兴趣点信息")
    public ApiResponse<List<Map<String, Object>>> getNearbyPois(
            @RequestParam double longitude,
            @RequestParam double latitude) {
        try {
            List<Map<String, Object>> pois = amapService.getNearbyPois(longitude, latitude);
            return ApiResponse.success("附近POI获取成功", pois);
        } catch (Exception e) {
            return ApiResponse.error("附近POI获取失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/format-address")
    @Operation(summary = "格式化地址", description = "格式化逆地理编码返回的地址信息")
    public ApiResponse<String> formatAddress(
            @RequestParam double longitude,
            @RequestParam double latitude) {
        try {
            AmapLocationResponse response = amapService.reverseGeocoding(longitude, latitude);
            String formattedAddress = amapService.formatAddress(response);
            return ApiResponse.success("地址格式化成功", formattedAddress);
        } catch (Exception e) {
            return ApiResponse.error("地址格式化失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/checkin/location-info")
    @Operation(summary = "打卡位置信息", description = "获取打卡地点的详细位置信息，包括格式化地址和附近POI")
    public ApiResponse<Map<String, Object>> getCheckinLocationInfo(
            @RequestParam double longitude,
            @RequestParam double latitude) {
        try {
            // 获取详细地址信息
            AmapLocationResponse locationResponse = amapService.reverseGeocodingExtended(longitude, latitude, "all", 1);
            String formattedAddress = amapService.formatAddress(locationResponse);
            
            // 获取附近POI
            List<Map<String, Object>> nearbyPois = amapService.getNearbyPois(longitude, latitude);
            
            // 生成静态地图URL
            String markers = longitude + "," + latitude + ",A";
            String staticMapUrl = amapService.getStaticMapUrl(longitude, latitude, 15, "300*200", markers);
            
            Map<String, Object> result = new HashMap<>();
            result.put("coordinates", longitude + "," + latitude);
            result.put("formattedAddress", formattedAddress);
            result.put("locationResponse", locationResponse);
            result.put("nearbyPois", nearbyPois);
            result.put("staticMapUrl", staticMapUrl);
            
            return ApiResponse.success("打卡位置信息获取成功", result);
        } catch (Exception e) {
            return ApiResponse.error("打卡位置信息获取失败: " + e.getMessage());
        }
    }
}
