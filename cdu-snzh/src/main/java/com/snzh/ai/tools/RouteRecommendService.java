package com.snzh.ai.tools;

import com.snzh.domain.vo.ScenicSpotVO;
import com.snzh.service.IScenicSpotService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author haibara
 * @description 智能路线推荐服务
 * @since 2025/10/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteRecommendService {

    private final IScenicSpotService scenicSpotService;

    /**
     * 景点信息封装类
     */
    @Data
    private static class ScenicInfo {
        private Long id;
        private String name;
        private String description;
        private String longitude;
        private String latitude;
        private int difficulty; // 难度等级：1-简单，2-中等，3-困难
        private int recommendTime; // 建议游览时间（分钟）
        private boolean suitableForChildren; // 是否适合儿童
        private boolean suitableForElderly; // 是否适合老人
        private boolean suitableForPhotography; // 是否适合摄影
        private boolean rainyDayFriendly; // 是否适合雨天
        private String[] tags; // 标签
        private int priority; // 优先级（基于综合因素计算）
    }

    /**
     * 用户偏好信息
     */
    @Data
    public static class UserPreference {
        private int duration; // 游玩时长（小时）
        private boolean hasChildren; // 是否有小孩
        private boolean hasElderly; // 是否有老人
        private boolean hiking; // 是否徒步
        private boolean photography; // 是否摄影
        private boolean leisure; // 是否休闲游
        private LocalDate visitDate; // 游玩日期
        private String weatherCondition; // 天气状况
        private int temperature; // 温度
        private String weatherDesc; // 天气描述
    }

    /**
     * 路线推荐结果
     */
    @Data
    public static class RouteRecommendation {
        private String title;
        private String weatherInfo;
        private List<RouteSegment> segments;
        private String tips;
        private String summary;
    }

    @Data
    public static class RouteSegment {
        private String timeRange;
        private String period;
        private List<ScenicItem> scenics;
        private String description;
    }

    @Data
    public static class ScenicItem {
        private String name;
        private int duration;
        private String reason;
        private String tips;
    }

    /**
     * 智能推荐路线（增强版）
     */
    public RouteRecommendation recommendRoute(UserPreference preference) {
        try {
            // 1. 获取所有景点信息
            List<ScenicSpotVO> allScenics = scenicSpotService.getScenicSpotList();
            
            // 2. 构建景点信息映射
            List<ScenicInfo> scenicInfos = buildScenicInfoList(allScenics);
            
            // 3. 根据用户偏好和天气筛选合适的景点
            List<ScenicInfo> suitableScenics = filterScenicsByPreference(scenicInfos, preference);
            
            // 4. 计算景点间的距离和最优路线
            List<ScenicInfo> optimizedRoute = optimizeRoute(suitableScenics, preference);
            
            // 5. 生成详细的游览计划
            RouteRecommendation recommendation = generateRouteRecommendation(optimizedRoute, preference);
            
            return recommendation;
        } catch (Exception e) {
            log.error("路线推荐失败", e);
            return getDefaultRoute(preference.getDuration());
        }
    }

    /**
     * 构建景点信息列表
     */
    private List<ScenicInfo> buildScenicInfoList(List<ScenicSpotVO> scenics) {
        List<ScenicInfo> result = new ArrayList<>();
        
        for (ScenicSpotVO scenic : scenics) {
            ScenicInfo info = new ScenicInfo();
            info.setId(scenic.getId());
            info.setName(scenic.getName());
            info.setDescription(scenic.getDescription());
            info.setLongitude(scenic.getLongitude());
            info.setLatitude(scenic.getLatitude());
            
            // 根据景点名称设置属性
            configScenicAttributes(info, scenic.getName());
            
            result.add(info);
        }
        
        return result;
    }

    /**
     * 配置景点属性（基于景点名称和特点）
     */
    private void configScenicAttributes(ScenicInfo info, String name) {
        switch (name) {
            case "忘忧谷":
                info.setDifficulty(2);
                info.setRecommendTime(120);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(false); // 有台阶
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(false);
                info.setTags(new String[]{"竹林", "幽静", "瀑布", "溪流"});
                break;
            case "天宝寨":
                info.setDifficulty(3);
                info.setRecommendTime(90);
                info.setSuitableForChildren(false);
                info.setSuitableForElderly(false); // 需要登高
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(false);
                info.setTags(new String[]{"登高", "古建筑", "全景"});
                break;
            case "七彩飞瀑":
                info.setDifficulty(2);
                info.setRecommendTime(60);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(true);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(false);
                info.setTags(new String[]{"瀑布", "彩虹", "拍照"});
                break;
            case "翡翠长廊":
                info.setDifficulty(1);
                info.setRecommendTime(90);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(true);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(true);
                info.setTags(new String[]{"竹林", "步道", "电影取景地"});
                break;
            case "天皇寺":
                info.setDifficulty(2);
                info.setRecommendTime(60);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(true);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(true);
                info.setTags(new String[]{"寺庙", "禅意", "竹海"});
                break;
            case "龙吟寺":
                info.setDifficulty(2);
                info.setRecommendTime(45);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(false);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(true);
                info.setTags(new String[]{"寺庙", "观景台", "云海"});
                break;
            case "仙寓洞":
                info.setDifficulty(2);
                info.setRecommendTime(60);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(false);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(false);
                info.setTags(new String[]{"洞穴", "道观", "历史"});
                break;
            case "仙女湖":
                info.setDifficulty(1);
                info.setRecommendTime(60);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(true);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(true);
                info.setTags(new String[]{"湖泊", "休闲", "浪漫"});
                break;
            case "青龙湖":
                info.setDifficulty(1);
                info.setRecommendTime(90);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(true);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(true);
                info.setTags(new String[]{"湖泊", "游船", "休闲"});
                break;
            case "海中海":
                info.setDifficulty(1);
                info.setRecommendTime(60);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(true);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(true);
                info.setTags(new String[]{"湖泊", "《卧虎藏龙》", "电影取景地"});
                break;
            case "花溪十三桥":
                info.setDifficulty(1);
                info.setRecommendTime(90);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(true);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(false);
                info.setTags(new String[]{"古桥", "溪流", "田园"});
                break;
            case "竹尖漫步":
                info.setDifficulty(2);
                info.setRecommendTime(60);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(false);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(false);
                info.setTags(new String[]{"栈道", "竹林", "沉浸式"});
                break;
            case "蜀南竹海博物馆":
                info.setDifficulty(1);
                info.setRecommendTime(90);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(true);
                info.setSuitableForPhotography(false);
                info.setRainyDayFriendly(true);
                info.setTags(new String[]{"博物馆", "文化", "室内"});
                break;
            case "望龙坪":
                info.setDifficulty(2);
                info.setRecommendTime(60);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(false);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(false);
                info.setTags(new String[]{"观景台", "远眺", "日出日落"});
                break;
            case "青云长廊":
                info.setDifficulty(2);
                info.setRecommendTime(60);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(false);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(false);
                info.setTags(new String[]{"长廊", "田园", "观景"});
                break;
            case "观海楼":
                info.setDifficulty(2);
                info.setRecommendTime(60);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(false);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(false);
                info.setTags(new String[]{"古楼", "观景", "历史"});
                break;
            case "拜寿台":
                info.setDifficulty(2);
                info.setRecommendTime(60);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(false);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(false);
                info.setTags(new String[]{"观景台", "云海", "日出"});
                break;
            default:
                // 默认配置
                info.setDifficulty(2);
                info.setRecommendTime(60);
                info.setSuitableForChildren(true);
                info.setSuitableForElderly(true);
                info.setSuitableForPhotography(true);
                info.setRainyDayFriendly(true);
                info.setTags(new String[]{"景点"});
        }
    }

    /**
     * 根据用户偏好筛选景点
     */
    private List<ScenicInfo> filterScenicsByPreference(List<ScenicInfo> scenics, UserPreference preference) {
        return scenics.stream()
                .filter(scenic -> {
                    // 如果有小孩，过滤不适合的景点
                    if (preference.isHasChildren() && !scenic.isSuitableForChildren()) {
                        return false;
                    }
                    // 如果有老人，过滤不适合的景点
                    if (preference.isHasElderly() && !scenic.isSuitableForElderly()) {
                        return false;
                    }
                    // 如果是雨天，优先选择雨天友好的景点
                    if (preference.getWeatherCondition() != null && 
                        preference.getWeatherCondition().contains("雨") && 
                        !scenic.isRainyDayFriendly()) {
                        scenic.setPriority(scenic.getPriority() - 2);
                    }
                    // 如果是摄影爱好者，优先选择适合摄影的景点
                    if (preference.isPhotography() && scenic.isSuitableForPhotography()) {
                        scenic.setPriority(scenic.getPriority() + 2);
                    }
                    // 如果是徒步，适当增加难度
                    if (preference.isHiking()) {
                        scenic.setPriority(scenic.getPriority() + (scenic.getDifficulty() - 1));
                    }
                    // 如果是休闲游，优先选择简单的景点
                    if (preference.isLeisure()) {
                        scenic.setPriority(scenic.getPriority() + (3 - scenic.getDifficulty()));
                    }
                    
                    return true;
                })
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .collect(Collectors.toList());
    }

    /**
     * 优化路线（基于距离和时间）
     */
    private List<ScenicInfo> optimizeRoute(List<ScenicInfo> scenics, UserPreference preference) {
        if (scenics.isEmpty()) {
            return scenics;
        }
        
        int totalMinutes = preference.getDuration() * 60;
        int lunchTime = 90; // 午餐时间
        int restTime = 30; // 休息时间
        int availableTime = totalMinutes - lunchTime - restTime;
        
        List<ScenicInfo> result = new ArrayList<>();
        int currentTime = 0;
        
        // 使用贪心算法选择景点
        for (ScenicInfo scenic : scenics) {
            if (currentTime + scenic.getRecommendTime() <= availableTime) {
                result.add(scenic);
                currentTime += scenic.getRecommendTime();
                // 添加移动时间（假设景点间平均15分钟）
                currentTime += 15;
            }
            
            if (currentTime >= availableTime) {
                break;
            }
        }
        
        // 尝试计算实际距离优化路线（如果有经纬度信息）
        if (result.size() > 1) {
            result = reorderByDistance(result);
        }
        
        return result;
    }

    /**
     * 根据距离重新排序景点（最短路径）
     */
    private List<ScenicInfo> reorderByDistance(List<ScenicInfo> scenics) {
        // 简化版的最近邻算法
        List<ScenicInfo> result = new ArrayList<>();
        List<ScenicInfo> remaining = new ArrayList<>(scenics);
        
        // 从第一个景点开始
        ScenicInfo current = remaining.remove(0);
        result.add(current);
        
        while (!remaining.isEmpty()) {
            ScenicInfo nearest = findNearestScenic(current, remaining);
            result.add(nearest);
            remaining.remove(nearest);
            current = nearest;
        }
        
        return result;
    }

    /**
     * 查找最近的景点
     */
    private ScenicInfo findNearestScenic(ScenicInfo from, List<ScenicInfo> candidates) {
        ScenicInfo nearest = candidates.get(0);
        double minDistance = calculateDistance(from, nearest);
        
        for (int i = 1; i < candidates.size(); i++) {
            ScenicInfo candidate = candidates.get(i);
            double distance = calculateDistance(from, candidate);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = candidate;
            }
        }
        
        return nearest;
    }

    /**
     * 计算两个景点之间的直线距离（简化版）
     */
    private double calculateDistance(ScenicInfo from, ScenicInfo to) {
        if (from.getLongitude() == null || from.getLatitude() == null ||
            to.getLongitude() == null || to.getLatitude() == null) {
            return Double.MAX_VALUE;
        }
        
        try {
            double lon1 = Double.parseDouble(from.getLongitude());
            double lat1 = Double.parseDouble(from.getLatitude());
            double lon2 = Double.parseDouble(to.getLongitude());
            double lat2 = Double.parseDouble(to.getLatitude());
            
            // 使用Haversine公式计算距离
            double R = 6371; // 地球半径（千米）
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                      Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return R * c;
        } catch (Exception e) {
            log.error("计算距离失败", e);
            return Double.MAX_VALUE;
        }
    }

    /**
     * 生成路线推荐结果
     */
    private RouteRecommendation generateRouteRecommendation(List<ScenicInfo> route, UserPreference preference) {
        RouteRecommendation recommendation = new RouteRecommendation();
        
        // 设置标题
        String title = generateTitle(preference);
        recommendation.setTitle(title);
        
        // 设置天气信息
        String weatherInfo = generateWeatherInfo(preference);
        recommendation.setWeatherInfo(weatherInfo);
        
        // 生成路线分段
        List<RouteSegment> segments = generateRouteSegments(route, preference);
        recommendation.setSegments(segments);
        
        // 生成温馨提示
        String tips = generateTips(preference, route);
        recommendation.setTips(tips);
        
        // 生成总结
        String summary = generateSummary(route, preference);
        recommendation.setSummary(summary);
        
        return recommendation;
    }

    private String generateTitle(UserPreference preference) {
        if (preference.getDuration() <= 4) {
            return "为您推荐半日游精华路线";
        } else if (preference.getDuration() <= 8) {
            return "为您推荐一日游经典路线";
        } else {
            return "为您推荐两日休闲游路线";
        }
    }

    private String generateWeatherInfo(UserPreference preference) {
        if (preference.getWeatherDesc() == null) {
            return "";
        }
        return String.format("🌤 %s天气：%s，温度%d℃", 
            formatDate(preference.getVisitDate()),
            preference.getWeatherDesc(),
            preference.getTemperature());
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "当天";
        }
        LocalDate now = LocalDate.now();
        long days = java.time.temporal.ChronoUnit.DAYS.between(now, date);
        if (days == 0) {
            return "今天";
        } else if (days == 1) {
            return "明天";
        } else if (days == 2) {
            return "后天";
        } else {
            return date.format(DateTimeFormatter.ofPattern("MM月dd日"));
        }
    }

    private List<RouteSegment> generateRouteSegments(List<ScenicInfo> route, UserPreference preference) {
        List<RouteSegment> segments = new ArrayList<>();
        
        if (preference.getDuration() <= 4) {
            // 半日游：只有一个时段
            RouteSegment segment = new RouteSegment();
            segment.setPeriod("半日游");
            segment.setTimeRange("约4小时");
            segment.setScenics(convertToScenicItems(route, preference));
            segments.add(segment);
        } else if (preference.getDuration() <= 8) {
            // 一日游：分上午、中午、下午
            int midPoint = route.size() / 2;
            
            // 上午
            RouteSegment morning = new RouteSegment();
            morning.setPeriod("上午");
            morning.setTimeRange("08:00-12:00");
            morning.setScenics(convertToScenicItems(route.subList(0, midPoint), preference));
            segments.add(morning);
            
            // 午餐
            RouteSegment lunch = new RouteSegment();
            lunch.setPeriod("中午");
            lunch.setTimeRange("12:00-13:30");
            lunch.setDescription("🍽️ 建议在景区餐厅品尝地道竹笋宴和当地特色美食");
            segments.add(lunch);
            
            // 下午
            RouteSegment afternoon = new RouteSegment();
            afternoon.setPeriod("下午");
            afternoon.setTimeRange("13:30-17:00");
            afternoon.setScenics(convertToScenicItems(route.subList(midPoint, route.size()), preference));
            segments.add(afternoon);
        } else {
            // 两日游
            int dayOneEnd = route.size() / 2;
            
            // 第一天
            RouteSegment day1 = new RouteSegment();
            day1.setPeriod("第一天");
            day1.setTimeRange("全天");
            day1.setScenics(convertToScenicItems(route.subList(0, dayOneEnd), preference));
            segments.add(day1);
            
            // 第二天
            RouteSegment day2 = new RouteSegment();
            day2.setPeriod("第二天");
            day2.setTimeRange("全天");
            day2.setScenics(convertToScenicItems(route.subList(dayOneEnd, route.size()), preference));
            segments.add(day2);
        }
        
        return segments;
    }

    private List<ScenicItem> convertToScenicItems(List<ScenicInfo> scenics, UserPreference preference) {
        return scenics.stream().map(scenic -> {
            ScenicItem item = new ScenicItem();
            item.setName(scenic.getName());
            item.setDuration(scenic.getRecommendTime());
            item.setReason(generateReason(scenic, preference));
            item.setTips(generateScenicTips(scenic, preference));
            return item;
        }).collect(Collectors.toList());
    }

    private String generateReason(ScenicInfo scenic, UserPreference preference) {
        List<String> reasons = new ArrayList<>();
        
        if (preference.isPhotography() && scenic.isSuitableForPhotography()) {
            reasons.add("绝佳拍照地");
        }
        if (preference.isHasChildren() && scenic.isSuitableForChildren()) {
            reasons.add("适合亲子游");
        }
        if (preference.isHasElderly() && scenic.isSuitableForElderly()) {
            reasons.add("老人友好");
        }
        if (preference.isLeisure() && scenic.getDifficulty() == 1) {
            reasons.add("轻松休闲");
        }
        
        if (reasons.isEmpty()) {
            return String.join("、", scenic.getTags());
        }
        
        return String.join("、", reasons);
    }

    private String generateScenicTips(ScenicInfo scenic, UserPreference preference) {
        List<String> tips = new ArrayList<>();
        
        if (scenic.getDifficulty() >= 2) {
            tips.add("建议穿舒适鞋子");
        }
        
        if (!scenic.isRainyDayFriendly() && preference.getWeatherCondition() != null && 
            preference.getWeatherCondition().contains("雨")) {
            tips.add("雨天路滑请注意安全");
        }
        
        if (scenic.isSuitableForPhotography()) {
            tips.add("最佳拍照时间：上午10:00-11:00或下午14:00-15:00");
        }
        
        return tips.isEmpty() ? null : String.join("；", tips);
    }

    private String generateTips(UserPreference preference, List<ScenicInfo> route) {
        List<String> tips = new ArrayList<>();
        
        tips.add("💡 建议提前购买门票和观光车票，节省排队时间");
        tips.add("💡 携带足够的饮用水和适量零食");
        
        if (preference.getWeatherCondition() != null) {
            if (preference.getWeatherCondition().contains("雨")) {
                tips.add("💡 携带雨具，穿防滑鞋");
            } else if (preference.getWeatherCondition().contains("晴")) {
                tips.add("💡 注意防晒，带好遮阳帽和防晒霜");
            }
        }
        
        if (preference.isHasElderly()) {
            tips.add("💡 老人游览请适当休息，量力而行");
        }
        
        if (preference.isHasChildren()) {
            tips.add("💡 看管好孩子，注意安全");
        }
        
        tips.add("💡 景区较大，建议购买观光车票");
        tips.add("💡 尊重自然，不要破坏竹林");
        
        return String.join("\n", tips);
    }

    private String generateSummary(List<ScenicInfo> route, UserPreference preference) {
        return String.format("本次行程共游览%d个景点，预计总用时约%d小时，涵盖了蜀南竹海最精华的景观。" +
            "路线已根据您的需求进行优化，确保游览体验最佳。祝您旅途愉快！🎋",
            route.size(), preference.getDuration());
    }

    /**
     * 获取默认路线（当智能推荐失败时使用）
     */
    private RouteRecommendation getDefaultRoute(int duration) {
        RouteRecommendation recommendation = new RouteRecommendation();
        
        if (duration <= 4) {
            recommendation.setTitle("为您推荐半日游精华路线");
            recommendation.setSummary("游览时间约4小时，包含必游景点");
        } else if (duration <= 8) {
            recommendation.setTitle("为您推荐一日游经典路线");
            recommendation.setSummary("游览时间约8小时，深度游览核心景区");
        } else {
            recommendation.setTitle("为您推荐两日休闲游路线");
            recommendation.setSummary("游览时间约2天，慢节奏体验竹海之美");
        }
        
        return recommendation;
    }

    /**
     * 解析游玩时长
     */
    public static int parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) {
            return 8; // 默认一天
        }
        
        duration = duration.toLowerCase();
        
        if (duration.contains("半天") || duration.contains("4小时") || 
            duration.contains("上午") || duration.contains("下午")) {
            return 4;
        } else if (duration.contains("一天") || duration.contains("1天") || 
                  duration.contains("全天") || duration.contains("8小时")) {
            return 8;
        } else if (duration.contains("两天") || duration.contains("2天") || 
                  duration.contains("二天")) {
            return 16;
        } else {
            // 尝试提取数字
            try {
                String[] parts = duration.split("[^0-9]");
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        return Integer.parseInt(part);
                    }
                }
            } catch (Exception e) {
                // 忽略
            }
        }
        
        return 8; // 默认返回8小时
    }
}

