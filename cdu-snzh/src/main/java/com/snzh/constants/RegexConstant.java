package com.snzh.constants;

/**
 * @author haibara
 * @description 正则表达式常量
 * @since 2025/7/27 21:53
 */
public class RegexConstant {
    /**
     * 手机号正则表达式（中国大陆手机号）
     */
    public static final String REGEX_MOBILE = "^1[3-9]\\d{9}$";

    /**
     * 邮箱正则表达式
     */
    public static final String REGEX_EMAIL = "^(?!\\.)[a-zA-Z0-9_.+-]+(?<!\\.)@[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+$";

    /**
     * 身份证号正则表达式（支持18位和15位）
     */
    public static final String REGEX_ID_CARD = "(^\\d{15}$)|(^\\d{17}[\\dXx]$)";

    /**
     * 用户名正则表达式(只能包含英文大小写，数字，以及中文字符)最少2字符最多32字符
     */
    public static final String REGEX_USERNAME = "^[a-zA-Z0-9\\u4e00-\\u9fa5]{2,32}$";

    /**
     * 强密码正则表达式（必须包含大小写字母、数字和特殊字符，长度为8-24位）
     */
    public static final String REGEX_PASSWORD_STRONG = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[~!@#$%^&*()_+=\\-.])[A-Za-z\\d~!@#$%^&*()_+=\\-.]{8,24}$";


    /**
     * URL正则表达式
     */
    public static final String REGEX_URL = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    /**
     * 汉字正则表达式
     */
    public static final String REGEX_CHINESE = "^[\\u4e00-\\u9fa5]+$";

    /**
     * 邮政编码正则表达式
     */
    public static final String REGEX_POSTCODE = "^[1-9]\\d{5}$";

    /**
     * 日期正则表达式（yyyy-MM-dd格式）
     */
    public static final String REGEX_DATE = "^\\d{4}-\\d{1,2}-\\d{1,2}$";

    /**
     * 时间正则表达式（HH:mm:ss格式）
     */
    public static final String REGEX_TIME = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$";

    /**
     * 日期时间正则表达式（yyyy-MM-dd HH:mm:ss格式）
     */
    public static final String REGEX_DATETIME = "^\\d{4}-\\d{1,2}-\\d{1,2} ([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$";

    /**
     * IPV4地址正则表达式
     */
    public static final String REGEX_IPV4 = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";

    /**
     * 金额正则表达式（精确到2位小数）
     */
    public static final String REGEX_MONEY = "^(?!0\\d)(\\d{1,6})(\\.\\d{1,2})?$";

    /**
     * 车牌号正则表达式（新能源+非新能源）
     */
    public static final String REGEX_CAR_NO = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳]$";

    /**
     * 图片文件名校验
     */
    public static final String REGEX_IMAGE_SUFFIX = "([^\\s]+(\\.(?i)(jpg|jpeg|png|gif|bmp|webp))$)";

    /**
     * 文档文件名校验
     */
    public static final String REGEX_DOC_SUFFIX = "([^\\s]+(\\.(?i)(doc|docx|xls|xlsx|ppt|pptx|pdf|txt))$)";

    /**
     * Base64 校验
     */
    public static final String REGEX_BASE64 = "^data:([\\w]+\\/[\\w\\-\\+\\.]+)?(;[\\w\\-]+=[\\w\\-]+)*(;base64)?,[a-zA-Z0-9+/=]+$";

    /**
     *去掉标题
     */
    public static final String HEADER_PATTERN = "(?m)^\\s*#.*$";

    /**
     * 去掉加粗
     */
    public static final String BOLD_PATTERN = "\\*\\*(.*?)\\*\\*";

    /**
     * 去掉斜体
     */
    public static final String ITALIC_PATTERN = "\\*(.*?)\\*";

    /**
     * 去掉行内代码
     */
    public static final String INLINE_CODE_PATTERN = "`([^`]*)`";

    /**
     * 去掉删除线
     */
    public static final String STRIKETHROUGH_PATTERN = "~~(.*?)~~";

    /**
     * 去掉链接
     */
    public static final String LINK_PATTERN = "\\[(.*?)\\]\\(.*?\\)";

    /**
     * 去掉图片
     */
    public static final String IMAGE_PATTERN = "!\\[.*?\\]\\(.*?\\)";

    /**
     * 去掉引用
     */
    public static final String BLOCKQUOTE_PATTERN = ">\\s?";

    /**
     * 去掉无序列表
     */
    public static final String UNORDERED_LIST_PATTERN = "(?m)^\\s*[-*+]\\s+";

    /**
     *去掉有序列表
     */
    public static final String ORDERED_LIST_PATTERN = "(?m)^\\s*\\d+\\.\\s+";

    /**
     *去掉换行符
     */
    public static final String NEWLINE_PATTERN = "\\n";


}
