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

    /** 用户不存在或已禁用 */
    public static final String USER_NOT_EXIST_OR_BANNED = "用户不存在或已禁用";

    /** Token无效或已过期 */
    public static final String INVALID_TOKEN = "Token无效或已过期";

    /** 缺少Token */
    public static final String NEED_TOKEN = "缺少Token";

    /** token已过期，请重新登录 */
    public static final String TOKEN_EXPIRED = "token已过期，请重新登录";

    /** 账户已被锁定 */
    public static final String ACCOUNT_LOCKED = "账户已被锁定";

    /** 登陆失败 */
    public static final String LOGIN_FAILED = "登陆失败";

    /** 数据不存在 */
    public static final String DATA_NOT_FOUND = "数据不存在";

    /** 账户不存在 */
    public static final String ACCOUNT_NOT_FOUND = "账户不存在";

    /** 获取手机号失败 */
    public static final String GET_PHONE_NUMBER_ERROR = "获取手机号失败";

    /** 基础设施类型不存在 */
    public static final String FACILITY_TYPE_NOT_FOUND = "基础设施类型不存在";

    /** 基础设施类型已存在 */
    public static final String FACILITY_TYPE_HAS_EXIST = "基础设施类型已存在";

    public static final String FACILITY_NOT_FOUND = "基础设施不存在";

    public static final String FACILITY_ID_NOT_NULL = "基础设施id不能为空";

    public static final String NAVIGATION_FAILED = "导航路径获取失败";

    public static final String ROUTE_PLAN_ERROR = "路径规划异常";

    public static final String CALCULATE_DISTANCE_ERROR = "距离计算异常";

    public static final String FACILITY_HAS_EXIST = "基础设施已存在";

}
