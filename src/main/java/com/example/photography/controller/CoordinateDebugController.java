package com.example.photography.controller;

import com.example.photography.dto.response.ApiResponse;
import com.example.photography.util.CoordinateConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 坐标调试控制器
 * 用于调试和验证坐标转换是否正确
 */
@Slf4j
@RestController
@RequestMapping("/api/debug/coordinate")
@Tag(name = "坐标调试", description = "坐标系转换调试接口（仅用于开发调试）")
public class CoordinateDebugController {
    
    @PostMapping("/convert")
    @Operation(summary = "坐标转换", description = "将WGS84坐标转换为GCJ-02，并计算与目标点的距离")
    public ApiResponse<Map<String, Object>> convertAndCalculate(@RequestBody CoordinateRequest request) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 原始坐标（WGS84）
            result.put("original", Map.of(
                "type", "WGS84",
                "latitude", request.getWgs84Latitude(),
                "longitude", request.getWgs84Longitude()
            ));
            
            // 转换为GCJ-02
            double[] gcj02 = CoordinateConverter.wgs84ToGcj02(
                request.getWgs84Latitude(), 
                request.getWgs84Longitude()
            );
            
            result.put("converted", Map.of(
                "type", "GCJ-02",
                "latitude", gcj02[0],
                "longitude", gcj02[1],
                "offsetLat", gcj02[0] - request.getWgs84Latitude(),
                "offsetLng", gcj02[1] - request.getWgs84Longitude()
            ));
            
            // 如果提供了目标坐标，计算距离
            if (request.getTargetLatitude() != null && request.getTargetLongitude() != null) {
                // 转换前的距离（错误的）
                double distanceBefore = calculateDistance(
                    request.getWgs84Latitude(), request.getWgs84Longitude(),
                    request.getTargetLatitude(), request.getTargetLongitude()
                );
                
                // 转换后的距离（正确的）
                double distanceAfter = calculateDistance(
                    gcj02[0], gcj02[1],
                    request.getTargetLatitude(), request.getTargetLongitude()
                );
                
                result.put("target", Map.of(
                    "type", "GCJ-02 (配置坐标)",
                    "latitude", request.getTargetLatitude(),
                    "longitude", request.getTargetLongitude()
                ));
                
                result.put("distance", Map.of(
                    "beforeConversion", Map.of(
                        "meters", Math.round(distanceBefore),
                        "description", "WGS84与GCJ-02直接计算（错误）"
                    ),
                    "afterConversion", Map.of(
                        "meters", Math.round(distanceAfter),
                        "description", "都转为GCJ-02后计算（正确）"
                    ),
                    "difference", Math.round(Math.abs(distanceBefore - distanceAfter))
                ));
                
                result.put("withinRange", Map.of(
                    "100m", distanceAfter <= 100,
                    "200m", distanceAfter <= 200,
                    "distance", Math.round(distanceAfter)
                ));
            }
            
            log.info("坐标转换调试: WGS84({}, {}) -> GCJ-02({}, {})", 
                request.getWgs84Latitude(), request.getWgs84Longitude(), 
                gcj02[0], gcj02[1]);
            
            return ApiResponse.success("坐标转换成功", result);
            
        } catch (Exception e) {
            log.error("坐标转换失败", e);
            return ApiResponse.error("坐标转换失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/reverse-convert")
    @Operation(summary = "反向转换", description = "将GCJ-02坐标转换为WGS84")
    public ApiResponse<Map<String, Object>> reverseConvert(@RequestBody ReverseCoordinateRequest request) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 原始坐标（GCJ-02）
            result.put("original", Map.of(
                "type", "GCJ-02",
                "latitude", request.getGcj02Latitude(),
                "longitude", request.getGcj02Longitude()
            ));
            
            // 转换为WGS84
            double[] wgs84 = CoordinateConverter.gcj02ToWgs84(
                request.getGcj02Latitude(), 
                request.getGcj02Longitude()
            );
            
            result.put("converted", Map.of(
                "type", "WGS84",
                "latitude", wgs84[0],
                "longitude", wgs84[1],
                "offsetLat", wgs84[0] - request.getGcj02Latitude(),
                "offsetLng", wgs84[1] - request.getGcj02Longitude()
            ));
            
            return ApiResponse.success("反向转换成功", result);
            
        } catch (Exception e) {
            log.error("反向转换失败", e);
            return ApiResponse.error("反向转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算两点之间的距离（米）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径，单位：米
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    @Data
    public static class CoordinateRequest {
        private Double wgs84Latitude;    // WGS84纬度（用户当前位置）
        private Double wgs84Longitude;   // WGS84经度（用户当前位置）
        private Double targetLatitude;   // 目标纬度（GCJ-02，配置的签到点）
        private Double targetLongitude;  // 目标经度（GCJ-02，配置的签到点）
    }
    
    @Data
    public static class ReverseCoordinateRequest {
        private Double gcj02Latitude;
        private Double gcj02Longitude;
    }
}

