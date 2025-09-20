package com.snzh.constants;

/**
 * 地图相关常量
 */
public class MapConstant {

    /**
     * 高德地图POI类型
     */
    public static class PoiType {
        /**
         * 餐饮服务
         */
        public static final String RESTAURANT = "050000";
        
        /**
         * 住宿服务
         */
        public static final String HOTEL = "100000";
        
        /**
         * 风景名胜
         */
        public static final String SCENIC_SPOT = "110000";
        
        /**
         * 公共厕所
         */
        public static final String TOILET = "200300";
        
        /**
         * 停车场
         */
        public static final String PARKING = "150900";
        
        /**
         * 商场
         */
        public static final String SHOPPING = "060100";
        
        /**
         * 医院
         */
        public static final String HOSPITAL = "090100";
        
        /**
         * 药店
         */
        public static final String PHARMACY = "090500";
    }
    
    /**
     * 导航类型
     */
    public static class NavigationType {
        /**
         * 驾车导航
         */
        public static final int DRIVING = 0;
        
        /**
         * 步行导航
         */
        public static final int WALKING = 1;
        
        /**
         * 公交导航
         */
        public static final int TRANSIT = 2;
        
        /**
         * 骑行导航
         */
        public static final int RIDING = 3;
        
        /**
         * AR步行导航
         */
        public static final int AR_WALKING = 4;
    }
    
    /**
     * 距离计算类型
     */
    public static class DistanceType {
        /**
         * 直线距离
         */
        public static final int STRAIGHT_LINE = 0;
        
        /**
         * 驾车距离
         */
        public static final int DRIVING = 1;
        
        /**
         * 步行距离
         */
        public static final int WALKING = 2;
    }
    
    /**
     * 默认值
     */
    public static class Default {
        /**
         * 默认搜索半径（米）
         */
        public static final int SEARCH_RADIUS = 3000;
        
        /**
         * 默认页码
         */
        public static final int PAGE = 1;
        
        /**
         * 默认每页记录数
         */
        public static final int PAGE_SIZE = 20;
        
        /**
         * 默认静态地图缩放级别
         */
        public static final int STATIC_MAP_ZOOM = 14;
        
        /**
         * 默认静态地图宽度
         */
        public static final int STATIC_MAP_WIDTH = 400;
        
        /**
         * 默认静态地图高度
         */
        public static final int STATIC_MAP_HEIGHT = 300;
    }
} 