package com.example.photography.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 高德地图定位响应DTO
 */
public class AmapLocationResponse {
    
    /**
     * 返回状态码
     */
    private String status;
    
    /**
     * 返回的结果数目
     */
    private String count;
    
    /**
     * 返回信息
     */
    private String info;
    
    /**
     * 信息码
     */
    private String infocode;
    
    /**
     * 地理编码信息列表
     */
    private List<Geocode> geocodes;
    
    /**
     * 逆地理编码信息
     */
    private ReGeocode regeocode;
    
    /**
     * 地理编码信息
     */
    public static class Geocode {
        /**
         * 格式化地址
         */
        @JsonProperty("formatted_address")
        private String formattedAddress;
        
        /**
         * 国家
         */
        private String country;
        
        /**
         * 省份
         */
        private String province;
        
        /**
         * 城市
         */
        private String city;
        
        /**
         * 区域
         */
        private String district;
        
        /**
         * 街道
         */
        private String township;
        
        /**
         * 门牌号
         */
        private String neighborhood;
        
        /**
         * 建筑物
         */
        private String building;
        
        /**
         * 行政区编码
         */
        private String adcode;
        
        /**
         * 街道编码
         */
        private String towncode;
        
        /**
         * 坐标点 "经度,纬度"
         */
        private String location;
        
        /**
         * 匹配级别
         */
        private String level;
        
        // Getters and Setters
        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        
        public String getTownship() { return township; }
        public void setTownship(String township) { this.township = township; }
        
        public String getNeighborhood() { return neighborhood; }
        public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }
        
        public String getBuilding() { return building; }
        public void setBuilding(String building) { this.building = building; }
        
        public String getAdcode() { return adcode; }
        public void setAdcode(String adcode) { this.adcode = adcode; }
        
        public String getTowncode() { return towncode; }
        public void setTowncode(String towncode) { this.towncode = towncode; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
    }
    
    /**
     * 逆地理编码信息
     */
    public static class ReGeocode {
        /**
         * 格式化地址
         */
        @JsonProperty("formatted_address")
        private String formattedAddress;
        
        /**
         * 地址组件
         */
        private AddressComponent addressComponent;
        
        /**
         * 兴趣点信息
         */
        private List<Poi> pois;
        
        /**
         * 道路信息
         */
        private List<Road> roads;
        
        /**
         * 路口信息
         */
        private List<RoadInter> roadinters;
        
        /**
         * 地址组件
         */
        public static class AddressComponent {
            private String country;
            private String province;
            private String city;
            private String citycode;
            private String district;
            private String adcode;
            private String township;
            private String towncode;
            private String neighborhood;
            private String building;
            
            // Getters and Setters
            public String getCountry() { return country; }
            public void setCountry(String country) { this.country = country; }
            
            public String getProvince() { return province; }
            public void setProvince(String province) { this.province = province; }
            
            public String getCity() { return city; }
            public void setCity(String city) { this.city = city; }
            
            public String getCitycode() { return citycode; }
            public void setCitycode(String citycode) { this.citycode = citycode; }
            
            public String getDistrict() { return district; }
            public void setDistrict(String district) { this.district = district; }
            
            public String getAdcode() { return adcode; }
            public void setAdcode(String adcode) { this.adcode = adcode; }
            
            public String getTownship() { return township; }
            public void setTownship(String township) { this.township = township; }
            
            public String getTowncode() { return towncode; }
            public void setTowncode(String towncode) { this.towncode = towncode; }
            
            public String getNeighborhood() { return neighborhood; }
            public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }
            
            public String getBuilding() { return building; }
            public void setBuilding(String building) { this.building = building; }
        }
        
        /**
         * 兴趣点信息
         */
        public static class Poi {
            private String id;
            private String name;
            private String type;
            private String typecode;
            private String address;
            private String location;
            private String distance;
            private String direction;
            
            // Getters and Setters
            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            
            public String getTypecode() { return typecode; }
            public void setTypecode(String typecode) { this.typecode = typecode; }
            
            public String getAddress() { return address; }
            public void setAddress(String address) { this.address = address; }
            
            public String getLocation() { return location; }
            public void setLocation(String location) { this.location = location; }
            
            public String getDistance() { return distance; }
            public void setDistance(String distance) { this.distance = distance; }
            
            public String getDirection() { return direction; }
            public void setDirection(String direction) { this.direction = direction; }
        }
        
        /**
         * 道路信息
         */
        public static class Road {
            private String id;
            private String name;
            private String distance;
            private String direction;
            private String location;
            
            // Getters and Setters
            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            
            public String getDistance() { return distance; }
            public void setDistance(String distance) { this.distance = distance; }
            
            public String getDirection() { return direction; }
            public void setDirection(String direction) { this.direction = direction; }
            
            public String getLocation() { return location; }
            public void setLocation(String location) { this.location = location; }
        }
        
        /**
         * 路口信息
         */
        public static class RoadInter {
            private String distance;
            private String direction;
            private String location;
            @JsonProperty("first_name")
            private String firstName;
            @JsonProperty("second_name")
            private String secondName;
            
            // Getters and Setters
            public String getDistance() { return distance; }
            public void setDistance(String distance) { this.distance = distance; }
            
            public String getDirection() { return direction; }
            public void setDirection(String direction) { this.direction = direction; }
            
            public String getLocation() { return location; }
            public void setLocation(String location) { this.location = location; }
            
            public String getFirstName() { return firstName; }
            public void setFirstName(String firstName) { this.firstName = firstName; }
            
            public String getSecondName() { return secondName; }
            public void setSecondName(String secondName) { this.secondName = secondName; }
        }
        
        // Getters and Setters for ReGeocode
        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
        
        public AddressComponent getAddressComponent() { return addressComponent; }
        public void setAddressComponent(AddressComponent addressComponent) { this.addressComponent = addressComponent; }
        
        public List<Poi> getPois() { return pois; }
        public void setPois(List<Poi> pois) { this.pois = pois; }
        
        public List<Road> getRoads() { return roads; }
        public void setRoads(List<Road> roads) { this.roads = roads; }
        
        public List<RoadInter> getRoadinters() { return roadinters; }
        public void setRoadinters(List<RoadInter> roadinters) { this.roadinters = roadinters; }
    }
    
    // Main class getters and setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getCount() { return count; }
    public void setCount(String count) { this.count = count; }
    
    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }
    
    public String getInfocode() { return infocode; }
    public void setInfocode(String infocode) { this.infocode = infocode; }
    
    public List<Geocode> getGeocodes() { return geocodes; }
    public void setGeocodes(List<Geocode> geocodes) { this.geocodes = geocodes; }
    
    public ReGeocode getRegeocode() { return regeocode; }
    public void setRegeocode(ReGeocode regeocode) { this.regeocode = regeocode; }
}
