package com.example.photography.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 高德地图API配置
 */
@Configuration
@ConfigurationProperties(prefix = "amap")
public class AmapConfig {
    
    /**
     * 高德地图Web服务API Key
     */
    private String webApiKey = "";
    
    /**
     * 高德地图JS API Key (前端使用)
     */
    private String jsApiKey = "";
    
    /**
     * 高德地图Web服务API基础URL
     */
    private String baseUrl = "https://restapi.amap.com";
    
    /**
     * 地理编码API URL
     */
    private String geocodingUrl = "/v3/geocode/geo";
    
    /**
     * 逆地理编码API URL
     */
    private String reverseGeocodingUrl = "/v3/geocode/regeo";
    
    /**
     * 地点搜索API URL
     */
    private String placeSearchUrl = "/v3/place/text";
    
    /**
     * 路径规划API URL
     */
    private String directionUrl = "/v3/direction/driving";
    
    /**
     * IP定位API URL
     */
    private String ipLocationUrl = "/v3/ip";
    
    /**
     * 静态地图API URL
     */
    private String staticMapUrl = "/v3/staticmap";
    
    // Getters and Setters
    public String getWebApiKey() {
        return webApiKey;
    }
    
    public void setWebApiKey(String webApiKey) {
        this.webApiKey = webApiKey;
    }
    
    public String getJsApiKey() {
        return jsApiKey;
    }
    
    public void setJsApiKey(String jsApiKey) {
        this.jsApiKey = jsApiKey;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getGeocodingUrl() {
        return geocodingUrl;
    }
    
    public void setGeocodingUrl(String geocodingUrl) {
        this.geocodingUrl = geocodingUrl;
    }
    
    public String getReverseGeocodingUrl() {
        return reverseGeocodingUrl;
    }
    
    public void setReverseGeocodingUrl(String reverseGeocodingUrl) {
        this.reverseGeocodingUrl = reverseGeocodingUrl;
    }
    
    public String getPlaceSearchUrl() {
        return placeSearchUrl;
    }
    
    public void setPlaceSearchUrl(String placeSearchUrl) {
        this.placeSearchUrl = placeSearchUrl;
    }
    
    public String getDirectionUrl() {
        return directionUrl;
    }
    
    public void setDirectionUrl(String directionUrl) {
        this.directionUrl = directionUrl;
    }
    
    public String getIpLocationUrl() {
        return ipLocationUrl;
    }
    
    public void setIpLocationUrl(String ipLocationUrl) {
        this.ipLocationUrl = ipLocationUrl;
    }
    
    public String getStaticMapUrl() {
        return staticMapUrl;
    }
    
    public void setStaticMapUrl(String staticMapUrl) {
        this.staticMapUrl = staticMapUrl;
    }
    
    /**
     * 获取完整的API URL
     */
    public String getFullUrl(String endpoint) {
        return baseUrl + endpoint;
    }
}
