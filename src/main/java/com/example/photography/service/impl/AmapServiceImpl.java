package com.example.photography.service.impl;

import com.example.photography.config.AmapConfig;
import com.example.photography.dto.response.AmapLocationResponse;
import com.example.photography.service.AmapService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

/**
 * 高德地图服务实现类
 */
@Service
public class AmapServiceImpl implements AmapService {
    
    @Autowired
    private AmapConfig amapConfig;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public AmapLocationResponse geocoding(String address) {
        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(amapConfig.getFullUrl(amapConfig.getGeocodingUrl()))
                    .queryParam("key", amapConfig.getWebApiKey())
                    .queryParam("address", address)
                    .queryParam("output", "JSON")
                    .build()
                    .toUri();
            
            System.out.println("📍 高德地理编码请求: " + uri.toString());
            
            ResponseEntity<AmapLocationResponse> response = restTemplate.getForEntity(uri, AmapLocationResponse.class);
            AmapLocationResponse result = response.getBody();
            
            if (result != null && "1".equals(result.getStatus())) {
                System.out.println("✅ 地理编码成功: " + result.getGeocodes().size() + " 个结果");
                return result;
            } else {
                System.err.println("❌ 地理编码失败: " + (result != null ? result.getInfo() : "响应为空"));
                throw new RuntimeException("地理编码失败: " + (result != null ? result.getInfo() : "响应为空"));
            }
        } catch (Exception e) {
            System.err.println("❌ 地理编码异常: " + e.getMessage());
            throw new RuntimeException("地理编码异常: " + e.getMessage(), e);
        }
    }
    
    @Override
    public AmapLocationResponse reverseGeocoding(double longitude, double latitude) {
        return reverseGeocodingExtended(longitude, latitude, "base", 0);
    }
    
    @Override
    public AmapLocationResponse reverseGeocodingExtended(double longitude, double latitude, String extensions, Integer roadlevel) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(amapConfig.getFullUrl(amapConfig.getReverseGeocodingUrl()))
                    .queryParam("key", amapConfig.getWebApiKey())
                    .queryParam("location", longitude + "," + latitude)
                    .queryParam("output", "JSON")
                    .queryParam("extensions", extensions != null ? extensions : "base");
            
            if (roadlevel != null) {
                builder.queryParam("roadlevel", roadlevel);
            }
            
            URI uri = builder.build().toUri();
            
            System.out.println("📍 高德逆地理编码请求: " + uri.toString());
            
            // 先获取原始JSON响应以便调试
            ResponseEntity<String> rawResponse = restTemplate.getForEntity(uri, String.class);
            String rawJson = rawResponse.getBody();
            System.out.println("🔍 高德API原始响应: " + rawJson);
            
            // 尝试解析为Map以便灵活处理
            ResponseEntity<Map> mapResponse = restTemplate.getForEntity(uri, Map.class);
            Map<String, Object> responseMap = mapResponse.getBody();
            
            if (responseMap != null && "1".equals(responseMap.get("status"))) {
                // 手动构建AmapLocationResponse
                AmapLocationResponse result = new AmapLocationResponse();
                result.setStatus((String) responseMap.get("status"));
                result.setInfo((String) responseMap.get("info"));
                result.setInfocode((String) responseMap.get("infocode"));
                
                // 处理regeocode部分
                Map<String, Object> regeocodeMap = (Map<String, Object>) responseMap.get("regeocode");
                if (regeocodeMap != null) {
                    AmapLocationResponse.ReGeocode regeocode = new AmapLocationResponse.ReGeocode();
                    regeocode.setFormattedAddress((String) regeocodeMap.get("formatted_address"));
                    
                    // 处理addressComponent
                    Map<String, Object> addressComponent = (Map<String, Object>) regeocodeMap.get("addressComponent");
                    if (addressComponent != null) {
                        AmapLocationResponse.ReGeocode.AddressComponent component = new AmapLocationResponse.ReGeocode.AddressComponent();
                        component.setCountry((String) addressComponent.get("country"));
                        component.setProvince((String) addressComponent.get("province"));
                        component.setCity((String) addressComponent.get("city"));
                        component.setDistrict((String) addressComponent.get("district"));
                        component.setTownship((String) addressComponent.get("township"));
                        regeocode.setAddressComponent(component);
                    }
                    
                    result.setRegeocode(regeocode);
                }
                
                System.out.println("✅ 逆地理编码成功: " + 
                    (result.getRegeocode() != null ? result.getRegeocode().getFormattedAddress() : "无地址信息"));
                return result;
            } else {
                System.err.println("❌ 逆地理编码失败: " + (responseMap != null ? responseMap.get("info") : "响应为空"));
                throw new RuntimeException("逆地理编码失败: " + (responseMap != null ? responseMap.get("info") : "响应为空"));
            }
        } catch (Exception e) {
            System.err.println("❌ 逆地理编码异常: " + e.getMessage());
            throw new RuntimeException("逆地理编码异常: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> placeSearch(String keywords, String city, String types) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(amapConfig.getFullUrl(amapConfig.getPlaceSearchUrl()))
                    .queryParam("key", amapConfig.getWebApiKey())
                    .queryParam("keywords", keywords)
                    .queryParam("output", "JSON");
            
            if (city != null && !city.isEmpty()) {
                builder.queryParam("city", city);
            }
            if (types != null && !types.isEmpty()) {
                builder.queryParam("types", types);
            }
            
            URI uri = builder.build().toUri();
            
            System.out.println("🔍 高德地点搜索请求: " + uri.toString());
            
            ResponseEntity<Map> response = restTemplate.getForEntity(uri, Map.class);
            Map<String, Object> result = response.getBody();
            
            if (result != null && "1".equals(result.get("status"))) {
                System.out.println("✅ 地点搜索成功: " + result.get("count") + " 个结果");
                return result;
            } else {
                System.err.println("❌ 地点搜索失败: " + (result != null ? result.get("info") : "响应为空"));
                throw new RuntimeException("地点搜索失败: " + (result != null ? result.get("info") : "响应为空"));
            }
        } catch (Exception e) {
            System.err.println("❌ 地点搜索异常: " + e.getMessage());
            throw new RuntimeException("地点搜索异常: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> placeSearchAround(String keywords, double longitude, double latitude, int radius) {
        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(amapConfig.getFullUrl(amapConfig.getPlaceSearchUrl()))
                    .queryParam("key", amapConfig.getWebApiKey())
                    .queryParam("keywords", keywords)
                    .queryParam("location", longitude + "," + latitude)
                    .queryParam("radius", radius)
                    .queryParam("output", "JSON")
                    .build()
                    .toUri();
            
            System.out.println("🔍 高德周边搜索请求: " + uri.toString());
            
            ResponseEntity<Map> response = restTemplate.getForEntity(uri, Map.class);
            Map<String, Object> result = response.getBody();
            
            if (result != null && "1".equals(result.get("status"))) {
                System.out.println("✅ 周边搜索成功: " + result.get("count") + " 个结果");
                return result;
            } else {
                System.err.println("❌ 周边搜索失败: " + (result != null ? result.get("info") : "响应为空"));
                throw new RuntimeException("周边搜索失败: " + (result != null ? result.get("info") : "响应为空"));
            }
        } catch (Exception e) {
            System.err.println("❌ 周边搜索异常: " + e.getMessage());
            throw new RuntimeException("周边搜索异常: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> driving(String origin, String destination) {
        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(amapConfig.getFullUrl(amapConfig.getDirectionUrl()))
                    .queryParam("key", amapConfig.getWebApiKey())
                    .queryParam("origin", origin)
                    .queryParam("destination", destination)
                    .queryParam("output", "JSON")
                    .build()
                    .toUri();
            
            System.out.println("🚗 高德路径规划请求: " + uri.toString());
            
            ResponseEntity<Map> response = restTemplate.getForEntity(uri, Map.class);
            Map<String, Object> result = response.getBody();
            
            if (result != null && "1".equals(result.get("status"))) {
                System.out.println("✅ 路径规划成功");
                return result;
            } else {
                System.err.println("❌ 路径规划失败: " + (result != null ? result.get("info") : "响应为空"));
                throw new RuntimeException("路径规划失败: " + (result != null ? result.get("info") : "响应为空"));
            }
        } catch (Exception e) {
            System.err.println("❌ 路径规划异常: " + e.getMessage());
            throw new RuntimeException("路径规划异常: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> ipLocation(String ip) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(amapConfig.getFullUrl(amapConfig.getIpLocationUrl()))
                    .queryParam("key", amapConfig.getWebApiKey())
                    .queryParam("output", "JSON");
            
            if (ip != null && !ip.isEmpty()) {
                builder.queryParam("ip", ip);
            }
            
            URI uri = builder.build().toUri();
            
            System.out.println("📍 高德IP定位请求: " + uri.toString());
            
            ResponseEntity<Map> response = restTemplate.getForEntity(uri, Map.class);
            Map<String, Object> result = response.getBody();
            
            if (result != null && "1".equals(result.get("status"))) {
                System.out.println("✅ IP定位成功: " + result.get("city"));
                return result;
            } else {
                System.err.println("❌ IP定位失败: " + (result != null ? result.get("info") : "响应为空"));
                throw new RuntimeException("IP定位失败: " + (result != null ? result.get("info") : "响应为空"));
            }
        } catch (Exception e) {
            System.err.println("❌ IP定位异常: " + e.getMessage());
            throw new RuntimeException("IP定位异常: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getStaticMapUrl(double longitude, double latitude, int zoom, String size, String markers) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(amapConfig.getFullUrl(amapConfig.getStaticMapUrl()))
                .queryParam("key", amapConfig.getWebApiKey())
                .queryParam("location", longitude + "," + latitude)
                .queryParam("zoom", zoom)
                .queryParam("size", size != null ? size : "400*300")
                .queryParam("scale", 1)
                .queryParam("format", "PNG");
        
        if (markers != null && !markers.isEmpty()) {
            builder.queryParam("markers", markers);
        }
        
        String url = builder.build().toString();
        System.out.println("🗺️ 静态地图URL: " + url);
        return url;
    }
    
    @Override
    public double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        // 使用Haversine公式计算两点间距离
        final double R = 6371000; // 地球半径（米）
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        double distance = R * c;
        
        System.out.println("📏 计算距离: " + distance + " 米");
        return distance;
    }
    
    @Override
    public boolean isLocationInRange(double userLon, double userLat, double centerLon, double centerLat, double radius) {
        double distance = calculateDistance(userLon, userLat, centerLon, centerLat);
        boolean inRange = distance <= radius;
        
        System.out.println("📍 位置验证: 距离=" + distance + "米, 允许范围=" + radius + "米, 结果=" + (inRange ? "✅通过" : "❌超出范围"));
        return inRange;
    }
    
    @Override
    public String formatAddress(AmapLocationResponse response) {
        if (response == null) return "";
        
        if (response.getRegeocode() != null && response.getRegeocode().getFormattedAddress() != null) {
            return response.getRegeocode().getFormattedAddress();
        }
        
        if (response.getGeocodes() != null && !response.getGeocodes().isEmpty()) {
            return response.getGeocodes().get(0).getFormattedAddress();
        }
        
        return "";
    }
    
    @Override
    public List<Map<String, Object>> getNearbyPois(double longitude, double latitude) {
        try {
            AmapLocationResponse response = reverseGeocodingExtended(longitude, latitude, "all", 1);
            List<Map<String, Object>> pois = new ArrayList<>();
            
            if (response.getRegeocode() != null && response.getRegeocode().getPois() != null) {
                for (AmapLocationResponse.ReGeocode.Poi poi : response.getRegeocode().getPois()) {
                    Map<String, Object> poiMap = new HashMap<>();
                    poiMap.put("id", poi.getId());
                    poiMap.put("name", poi.getName());
                    poiMap.put("type", poi.getType());
                    poiMap.put("address", poi.getAddress());
                    poiMap.put("location", poi.getLocation());
                    poiMap.put("distance", poi.getDistance());
                    poiMap.put("direction", poi.getDirection());
                    pois.add(poiMap);
                }
            }
            
            System.out.println("🏢 附近POI: " + pois.size() + " 个");
            return pois;
        } catch (Exception e) {
            System.err.println("❌ 获取附近POI异常: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
