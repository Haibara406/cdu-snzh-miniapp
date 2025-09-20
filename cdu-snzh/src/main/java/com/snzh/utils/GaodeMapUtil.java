package com.snzh.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.snzh.constants.ErrorConst;
import com.snzh.constants.MapConstant;

import com.snzh.domain.properties.GaodeMapProperties;
import com.snzh.exceptions.MapServerException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


/**
 * @author haibara
 * @description 高德地图工具类
 * @since 2025/9/20 14:01
 */
@Slf4j
@Component
@EnableConfigurationProperties(GaodeMapProperties.class)
public class GaodeMapUtil {

    // TODO 写在配置文件
    @Value("${miniapp.gaode.key:99014055e07f33fbc1d67e49a650640f}")
    private String key;

    @Value("${miniapp.gaode.address:https://restapi.amap.com/v3}")
    private String address;

    @Value("${miniapp.gaode.timeout:5000}")
    private Integer timeout;

    @Value("${miniapp.gaode.connect-timeout:3000}")
    private Integer connectTimeout;

    @Value("${miniapp.gaode.static-map-zoom:14}")
    private Integer staticMapZoom;

    @Value("${miniapp.gaode.static-map-width:400}")
    private Integer staticMapWidth;

    @Value("${miniapp.gaode.static-map-height:300}")
    private Integer staticMapHeight;

    public GaodeMapUtil() {
    }

    /**
     * 地理编码 - 地址转经纬度
     */
    public Map<String, Double> geocode(String address) {
        if (StrUtil.isBlank(address)) {
            throw new MapServerException(ErrorConst.ADDRESS_NOT_NULL);
        }
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = this.address + "/geocode/geo";

            Map<String, Object> params = new HashMap<>();
            params.put("key", this.key);
            params.put("address", encodedAddress);

            String response = executeRequest(url, params);
            JSONObject result = JSONUtil.parseObj(response);

            if (!"1".equals(result.getStr("status"))) {
                log.error(ErrorConst.GEOCODE_REQUEST_FAILED, result.getStr("info"));
                throw new MapServerException(String.format(ErrorConst.GEOCODE_REQUEST_FAILED, result.getStr("info")));
            }

            JSONObject geocode = result.getJSONArray("geocodes").getJSONObject(0);
            String location = geocode.getStr("location");
            String[] coordinates = location.split(",");

            Map<String, Double> locationMap = new HashMap<>(2);
            locationMap.put("longitude", Double.parseDouble(coordinates[0]));
            locationMap.put("latitude", Double.parseDouble(coordinates[1]));

            return locationMap;
        } catch (Exception e) {
            log.error("地理编码异常", e);
            throw new MapServerException("地理编码异常: " + e.getMessage());
        }
    }

    /**
     * 逆地理编码 - 经纬度转地址
     */
    public String reverseGeocode(String longitude, String latitude) {
        try {
            String location = longitude + "," + latitude;
            String url = this.address + "/geocode/regeo";

            Map<String, Object> params = new HashMap<>();
            params.put("key", this.key);
            params.put("location", location);

            String response = executeRequest(url, params);
            JSONObject result = JSONUtil.parseObj(response);

            if (!"1".equals(result.getStr("status"))) {
                log.error(ErrorConst.REVERSE_GEOCODE_REQUEST_FAILED, result.getStr("info"));
                throw new MapServerException(String.format(ErrorConst.REVERSE_GEOCODE_REQUEST_FAILED, result.getStr("info")));
            }

            JSONObject regeocode = result.getJSONObject("regeocode");
            return regeocode.getStr("formatted_address");
        } catch (Exception e) {
            log.error("", e);
            throw new MapServerException("逆地理编码异常: " + e.getMessage());
        }
    }

    /**
     * 路径规划
     *
     * @param startLongitude 起点经度
     * @param startLatitude  起点纬度
     * @param endLongitude   终点经度
     * @param endLatitude    终点纬度
     * @param type           出行方式：0-驾车，1-步行，2-公交，3-骑行
     * @return 路径规划结果，包含距离、时间等信息
     */
    public Map<String, Object> routePlanning(String startLongitude, String startLatitude,
                                             String endLongitude, String endLatitude, int type) {
        try {
            String origin = startLongitude + "," + startLatitude;
            String destination = endLongitude + "," + endLatitude;

            String url;
            Map<String, Object> params = new HashMap<>();
            params.put("key", this.key);
            params.put("origin", origin);
            params.put("destination", destination);

            url = switch (type) {
                case 0 -> this.address + "/direction/driving";
                case 1 -> this.address + "/direction/walking";
                case 2 -> this.address + "/direction/transit/integrated";
                case 3 -> this.address + "/direction/bicycling";
                default -> throw new MapServerException("不支持的出行方式类型");
            };

            String response = executeRequest(url, params);
            JSONObject result = JSONUtil.parseObj(response);

            if (!"1".equals(result.getStr("status"))) {
                log.error(ErrorConst.ROUTE_PLANNING_REQUEST_FAILED, result.getStr("info"));
                throw new MapServerException(String.format(ErrorConst.ROUTE_PLANNING_REQUEST_FAILED, result.getStr("info")));
            }

            Map<String, Object> routeInfo = new HashMap<>();
            JSONObject route = result.getJSONObject("route");

            // 驾车/步行/骑行
            if (type == 0 || type == 1 || type == 3) {
                if (!route.getJSONArray("paths").isEmpty()) {
                    JSONObject path = route.getJSONArray("paths").getJSONObject(0);
                    routeInfo.put("distance", path.getInt("distance"));
                    routeInfo.put("duration", path.getInt("duration"));
                }
                // 公交
            } else if (type == 2) {
                if (!route.getJSONArray("transits").isEmpty()) {
                    JSONObject transit = route.getJSONArray("transits").getJSONObject(0);
                    routeInfo.put("distance", transit.getInt("distance"));
                    routeInfo.put("duration", transit.getInt("duration"));
                }
            }

            return routeInfo;
        } catch (Exception e) {
            log.error("路径规划异常", e);
            throw new MapServerException("路径规划异常: " + e.getMessage());
        }
    }

    /**
     * 周边搜索
     *
     * @param longitude 中心点经度
     * @param latitude  中心点纬度
     * @param keyword   搜索关键词
     * @param radius    搜索半径，单位：米
     * @param type      POI类型
     * @param page      页码
     * @param limit     每页记录数
     * @return 周边POI信息
     */
    public JSONObject searchAround(String longitude, String latitude, String keyword,
                                   Integer radius, String type, Integer page, Integer limit) {
        try {
            String location = longitude + "," + latitude;
            String url = this.address + "/place/around";

            Map<String, Object> params = new HashMap<>();
            params.put("key", this.key);
            params.put("location", location);
            params.put("keywords", keyword);
            params.put("radius", radius != null ? radius : MapConstant.Default.SEARCH_RADIUS);
            if (StrUtil.isNotBlank(type)) {
                params.put("types", type);
            }
            params.put("offset", limit != null ? limit : MapConstant.Default.PAGE_SIZE);
            params.put("page", page != null ? page : MapConstant.Default.PAGE);

            String response = executeRequest(url, params);
            JSONObject result = JSONUtil.parseObj(response);

            if (!"1".equals(result.getStr("status"))) {
                log.error(ErrorConst.SEARCH_AROUND_REQUEST_FAILED, result.getStr("info"));
                throw new MapServerException(String.format(ErrorConst.SEARCH_AROUND_REQUEST_FAILED, result.getStr("info")));
            }

            return result;
        } catch (Exception e) {
            log.error("周边搜索异常", e);
            throw new MapServerException("周边搜索异常: " + e.getMessage());
        }
    }

    // ===== POI 专用搜索 =====

    public JSONObject searchNearbyToilets(String longitude, String latitude, Integer radius, Integer page, Integer limit) {
        return searchAround(longitude, latitude, "厕所", radius, MapConstant.PoiType.TOILET, page, limit);
    }

    public JSONObject searchNearbyParkings(String longitude, String latitude, Integer radius, Integer page, Integer limit) {
        return searchAround(longitude, latitude, "停车场", radius, MapConstant.PoiType.PARKING, page, limit);
    }

    public JSONObject searchNearbyRestaurants(String longitude, String latitude, Integer radius, Integer page, Integer limit) {
        return searchAround(longitude, latitude, "", radius, MapConstant.PoiType.RESTAURANT, page, limit);
    }

    public JSONObject searchNearbyHotels(String longitude, String latitude, Integer radius, Integer page, Integer limit) {
        return searchAround(longitude, latitude, "", radius, MapConstant.PoiType.HOTEL, page, limit);
    }

    /**
     * 关键字搜索
     *
     * @param keyword 搜索关键词
     * @param city    城市名称或编码
     * @param page    页码
     * @param limit   每页记录数
     * @return 搜索结果
     */
    public JSONObject searchByKeyword(String keyword, String city, Integer page, Integer limit) {
        if (StrUtil.isBlank(keyword)) {
            log.error(ErrorConst.KEYWORD_NOT_NULL);
        }
        try {
            String url = this.address + "/place/text";
            Map<String, Object> params = new HashMap<>();
            params.put("key", this.key);
            params.put("keywords", keyword);
            if (StrUtil.isNotBlank(city)) {
                params.put("city", city);
            }
            params.put("offset", limit != null ? limit : 20);
            params.put("page", page != null ? page : 1);

            String response = executeRequest(url, params);
            JSONObject result = JSONUtil.parseObj(response);

            if (!"1".equals(result.getStr("status"))) {
                log.error(ErrorConst.SEARCH_BY_KEYWORD_REQUEST_FAILED, result.getStr("info"));
                throw new MapServerException(String.format(ErrorConst.SEARCH_BY_KEYWORD_REQUEST_FAILED, result.getStr("info")));
            }

            return result;
        } catch (Exception e) {
            log.error("关键字搜索异常", e);
            throw new MapServerException("关键字搜索异常: " + e.getMessage());
        }
    }

    /**
     * 计算两点之间的距离
     *
     * @param startLongitude 起点经度
     * @param startLatitude  起点纬度
     * @param endLongitude   终点经度
     * @param endLatitude    终点纬度
     * @param type           计算方式
     * @return 距离，单位米
     */
    public int calculateDistance(String startLongitude, String startLatitude,
                                 String endLongitude, String endLatitude, int type) {
        try {
            String origins = startLongitude + "," + startLatitude;
            String destination = endLongitude + "," + endLatitude;
            String url = this.address + "/distance";

            Map<String, Object> params = new HashMap<>();
            params.put("key", this.key);
            params.put("origins", origins);
            params.put("destination", destination);

            String calculationType = switch (type) {
                case MapConstant.DistanceType.DRIVING -> "1";
                case MapConstant.DistanceType.WALKING -> "2";
                default -> "0";
            };
            params.put("type", calculationType);

            String response = executeRequest(url, params);
            JSONObject result = JSONUtil.parseObj(response);

            if (!"1".equals(result.getStr("status"))) {
                log.error(ErrorConst.CALCULATE_DISTANCE_REQUEST_FAILED, result.getStr("info"));
                throw new MapServerException(String.format(ErrorConst.CALCULATE_DISTANCE_REQUEST_FAILED, result.getStr("info")));
            }

            if (!result.getJSONArray("results").isEmpty()) {
                JSONObject distanceResult = result.getJSONArray("results").getJSONObject(0);
                return distanceResult.getInt("distance");
            }
            return 0;
        } catch (Exception e) {
            log.error("距离计算异常", e);
            throw new MapServerException("距离计算异常: " + e.getMessage());
        }
    }

    /**
     * 生成静态地图URL
     *
     * @param longitude 中心点经度
     * @param latitude  中心点纬度
     * @param width     图片宽度
     * @param height    图片高度
     * @param zoom      缩放级别
     * @param markers   标记点
     * @return 静态地图URL
     */
    public String generateStaticMapUrl(String longitude, String latitude, Integer width, Integer height,
                                       Integer zoom, String markers) {
        try {
            String location = longitude + "," + latitude;
            StringBuilder urlBuilder = new StringBuilder(this.address + "/staticmap?");

            urlBuilder.append("key=").append(this.key);
            urlBuilder.append("&center=").append(location);
            urlBuilder.append("&zoom=").append(zoom != null ? zoom : this.staticMapZoom);
            urlBuilder.append("&size=").append(width != null ? width : this.staticMapWidth)
                    .append("*").append(height != null ? height : this.staticMapHeight);

            if (StrUtil.isNotBlank(markers)) {
                urlBuilder.append("&markers=mid,0xFF0000,A:").append(URLEncoder.encode(markers, StandardCharsets.UTF_8));
            }

            return urlBuilder.toString();
        } catch (Exception e) {
            log.error(ErrorConst.STATIC_MAP_URL_EXCEPTION, e.getMessage(), e);
            throw new MapServerException(String.format(ErrorConst.STATIC_MAP_URL_EXCEPTION, e.getMessage()));
        }
    }

    /**
     * 生成导航URL
     *
     * @param endLongitude 终点经度
     * @param endLatitude  终点纬度
     * @param endName      终点名称
     * @param navType      导航类型
     * @return 导航URL
     */
    public String generateNavigationUrl(String endLongitude, String endLatitude, String endName, int navType) {
        try {
            String encodedName = URLEncoder.encode(endName, StandardCharsets.UTF_8);
            StringBuilder urlBuilder = new StringBuilder("https://uri.amap.com/navigation?");
            urlBuilder.append("to=").append(endLongitude).append(",").append(endLatitude).append(",").append(encodedName);

            String mode;
            switch (navType) {
                case MapConstant.NavigationType.WALKING: mode = "walk"; break;
                case MapConstant.NavigationType.TRANSIT: mode = "bus"; break;
                case MapConstant.NavigationType.RIDING: mode = "ride"; break;
                case MapConstant.NavigationType.AR_WALKING:
                    mode = "walk";
                    urlBuilder.append("&walkType=5");
                    break;
                case MapConstant.NavigationType.DRIVING:
                default: mode = "car";
            }
            urlBuilder.append("&mode=").append(mode);
            urlBuilder.append("&src=myapp");
            return urlBuilder.toString();
        } catch (Exception e) {
            log.error(ErrorConst.NAVIGATION_URL_EXCEPTION, e.getMessage(), e);
            throw new MapServerException(String.format(ErrorConst.NAVIGATION_URL_EXCEPTION, e.getMessage()));
        }
    }

    /**
     * 执行HTTP请求（GET方式）
     *
     * @param url    请求URL
     * @param params 请求参数
     * @return 响应内容
     */
    private String executeRequest(String url, Map<String, Object> params) {
        try {
            HttpRequest request = HttpUtil.createGet(url);
            if (params != null && !params.isEmpty()) {
                request.form(params);
            }
            request.timeout(this.timeout);
            request.setConnectionTimeout(this.connectTimeout);

            HttpResponse response = request.execute();
            if (!response.isOk()) {
                log.error(ErrorConst.API_RESPONSE_STATUS_ERROR, response.getStatus());
                throw new MapServerException(String.format(ErrorConst.API_RESPONSE_STATUS_ERROR, response.getStatus()));
            }

            String body = response.body();
            if (StrUtil.isBlank(body)) {
                log.error(ErrorConst.API_RESPONSE_EMPTY);
                throw new MapServerException(ErrorConst.API_RESPONSE_EMPTY);
            }

            return body;
        } catch (Exception e) {
            log.error(ErrorConst.HTTP_REQUEST_EXCEPTION, e.getMessage(), e);
            throw new MapServerException(String.format(ErrorConst.HTTP_REQUEST_EXCEPTION, e.getMessage()));
        }
    }

}