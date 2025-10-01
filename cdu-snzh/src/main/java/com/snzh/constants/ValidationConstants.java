package com.snzh.constants;

/**
 * @author haibara
 * @description 参数验证信息常量
 * @since 2025/9/20 10:37
 */
public class ValidationConstants {

    public static final String CODE_NOT_NULL = "微信临时登录凭证不能为空";

    public static final String INCORRECT_PHONE_FORMAT = "手机号格式错误";

    public static final String INCORRECT_ID_NUMBER_FORMAT = "身份证格式错误";

    public static final String CHINESE_ONLY_FOR_REAL_NAME = "真实姓名只能包含中文字符";

    public static final String INCORRECT_GENDER_VALUE = "性别值不正确";

    public static final String NICKNAME_NOT_NULL = "昵称不能为空";

    public static final String AVATAR_NOT_NULL = "头像不能为空";

    public static final String INCORRECT_NICKNAME_FORMAT = "昵称格式错误，只能包含英文大小写，数字，以及中文字符，且最少2字符最多32字符";

    public static final String INCORRECT_AVATAR_URL_FORMAT = "头像URL格式不正确";

    public static final String ENCRYPTED_DATA_NOT_NULL = "加密数据不能为空";

    public static final String IV_NOT_NULL = "加密算法初始向量不能为空";

    public static final String INCORRECT_STATUS_VALUE = "状态值不正确";

    public static final String USER_ID_NOT_NULL = "用户id不能为空";

    public static final String FACILITY_TYPE_NAME_TOO_LONG = "基础设施类型名称不能超过50个字符";

    public static final String FACILITY_TYPE_NAME_NOT_NULL = "基础设施类型名称不能为空";

    public static final String FACILITY_ID_NOT_NULL = "基础设施id不能为空";

    public static final String INCORRECT_NAV_TYPE = "导航类型不正确";

    public static final String INCORRECT_DISTANCE_TYPE = "距离计算类型不正确";

    public static final String FACILITY_TYPE_ID_NOT_NULL = "基础设施类型id不能为空";

    public static final String FACILITY_NAME_TOO_LONG = "设施名称长度应在2-100字符之间";

    public static final String FACILITY_NAME_NOT_NULL = "基础设施名称不能为空";

    public static final String FACILITY_ADDRESS_NOT_NULL = "基础设施详细地址不能为空";

    public static final String FACILITY_ADDRESS_TOO_LONG = "地址长度不能超过255字符";

    public static final String LONGITUDE_NOT_NULL = "经度不能为空";

    public static final String LATITUDE_NOT_NULL = "纬度不能为空";

    public static final String LONGITUDE_EXCEEDS_CHINA = "经度超过中国范围";

    public static final String LATITUDE_EXCEEDS_CHINA = "经度超过中国范围";

    public static final String FACILITY_OPEN_TIME_DESCRIBE_TOO_LONG = "开放时间描述不能超过100字符";

    public static final String ID_NOT_NULL = "id不能为空";

    public static final String ID_TYPE_NOT_NULL = "idType不能为空";

    public static final String SCENIC_ID_NOT_NULL = "景区id不能为空";

    public static final String SCENIC_NAME_NOT_NULL = "景区名字不能为空";

    public static final String SCENIC_NAME_TOO_LONG = "景区名字长度应在2-100字符之间";

    public static final String SCENIC_DES_TOO_LONG = "景区描述长度应在2-2000字符之间";

    public static final String SCENIC_ADDRESS_NOT_NULL = "景区地址不能为空";

    public static final String SCENIC_ADDRESS_TOO_LONG = "景区地址长度不能超过255字符";

    public static final String INCORRECT_SCENIC_MODEL_URL = "景点3d模型url链接不正确";

    public static final String SCENIC_DISPLAY_ORDER_NOT_NULL = "景点展示顺序不能为空";

    public static final String TICKET_TYPE_ID_NOT_NULL = "门票类型id不能为空";

    public static final String SOLD_COUNT_MUST_BE_POSITIVE = "销售数量不能为复数";

    public static final String INCORRECT_PRICE = "价格不正常";

    public static final String ADDRESS_KEYWORD_TOO_LONG = "地址关键词不能超过50个字符";

    public static final String SCENIC_KEYWORD_TOO_LONG = "景区关键字不能超过50字符";

    public static final String VALID_DAYS_MUST_BE_POSITIVE = "有效天数不能为负数";

    public static final String TICKET_TYPE_NAME_TOO_LONG = "票种名称不能超过100个字符";

    public static final String TICKET_TYPE_NAME_NOT_NULL = "门票类型名不能为空";

    public static final String TICKET_TYPE_DES_TOO_LONG = "门票类型描述长度应在2-2000字符之间";

    public static final String SCENIC_TICKET_SOLD_COUNT_MUST_BE_POSITIVE = "景点门票售卖数量不能为负数";

}
