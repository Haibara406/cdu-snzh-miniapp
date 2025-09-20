package com.snzh.constants;

/**
 * @author haibara
 * @description 错误信息常量
 * @since 2025/9/20 10:37
 */
public class ErrorConst {

    /** 地址不能为空 */
    public static final String ADDRESS_NOT_NULL = "地址不能为空";

    /** 地理编码请求失败 */
    public static final String GEOCODE_REQUEST_FAILED = "地理编码请求失败: %s";

    /** 逆地理编码请求失败 */
    public static final String REVERSE_GEOCODE_REQUEST_FAILED = "逆地理编码请求失败: %s";

    /** 路径规划请求失败 */
    public static final String ROUTE_PLANNING_REQUEST_FAILED = "路径规划请求失败: %s";

    /** 路径规划出行方式类型不支持 */
    public static final String ROUTE_PLANNING_TYPE_UNSUPPORTED = "不支持的出行方式类型";

    /** 周边搜索请求失败 */
    public static final String SEARCH_AROUND_REQUEST_FAILED = "周边搜索请求失败: %s";

    /** 关键字搜索请求失败 */
    public static final String SEARCH_BY_KEYWORD_REQUEST_FAILED = "关键字搜索请求失败: %s";

    /** 搜索关键词不能为空 */
    public static final String KEYWORD_NOT_NULL = "搜索关键词不能为空";

    /** 距离计算请求失败 */
    public static final String CALCULATE_DISTANCE_REQUEST_FAILED = "距离计算请求失败: %s";

    /** 执行HTTP请求异常 */
    public static final String HTTP_REQUEST_EXCEPTION = "执行HTTP请求异常: %s";

    /** 高德地图API请求失败，返回为空 */
    public static final String API_RESPONSE_EMPTY = "高德地图API请求失败，返回为空";

    /** 高德地图API请求失败，状态码 */
    public static final String API_RESPONSE_STATUS_ERROR = "高德地图API请求失败，状态码: %s";

    /** 生成静态地图URL异常 */
    public static final String STATIC_MAP_URL_EXCEPTION = "生成静态地图URL异常: %s";

    /** 生成导航URL异常 */
    public static final String NAVIGATION_URL_EXCEPTION = "生成导航URL异常: %s";

    /** 地理编码异常 */
    public static final String GEOCODE_EXCEPTION = "地理编码异常: %s";

    /** 逆地理编码异常 */
    public static final String REVERSE_GEOCODE_EXCEPTION = "逆地理编码异常: %s";


    /** 路径规划异常 */
    public static final String ROUTE_PLANNING_EXCEPTION = "路径规划异常: %s";

    /** 周边搜索异常 */
    public static final String SEARCH_AROUND_EXCEPTION = "周边搜索异常: %s";

    /** 关键字搜索异常 */
    public static final String SEARCH_BY_KEYWORD_EXCEPTION = "关键字搜索异常: %s";

    /** 距离计算异常 */
    public static final String CALCULATE_DISTANCE_EXCEPTION = "距离计算异常: %s";
}
