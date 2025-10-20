package com.snzh.ai.tools;

import com.snzh.ai.tools.RouteRecommendService.RouteRecommendation;
import com.snzh.ai.tools.RouteRecommendService.RouteSegment;
import com.snzh.ai.tools.RouteRecommendService.ScenicItem;
import com.snzh.ai.tools.RouteRecommendService.UserPreference;
import com.snzh.domain.dto.OrderCreateDTO;
import com.snzh.domain.dto.OrderItemDTO;
import com.snzh.domain.vo.CastVO;
import com.snzh.domain.vo.ForecastWeatherVO;
import com.snzh.domain.vo.LiveWeatherVO;
import com.snzh.domain.vo.ScenicSpotVO;
import com.snzh.domain.vo.ScenicTicketVO;
import com.snzh.domain.vo.FacilityVO;
import com.snzh.service.IFacilityService;
import com.snzh.service.IOrderService;
import com.snzh.service.IScenicSpotService;
import com.snzh.service.IScenicTicketService;
import com.snzh.service.impl.WeatherService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author haibara
 * @description AI工具服务（供AI调用的业务工具集）
 * @since 2025/10/5
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiToolService {

    private final WeatherService weatherService;
    private final IScenicSpotService scenicSpotService;
    private final IScenicTicketService scenicTicketService;
    private final IOrderService orderService;
    private final RouteRecommendService routeRecommendService;
    private final IFacilityService facilityService;

    /**
     * 查询当前实时天气
     */
    @Tool("查询蜀南竹海景区所在地（长宁县）的当前实时天气信息。" +
         "无需参数，直接调用即可获取当前时刻的天气状况。" +
         "返回内容包括：实时天气状况、当前温度、风向、风力、湿度等信息。" +
         "适用场景：用户询问'现在天气如何'、'今天天气怎么样'、'当前温度多少'等实时天气查询。" +
         "注意：此工具仅返回实时天气，如需查询未来天气预报，请使用queryForecastWeather工具。")
    public String queryWeather() {
        try {
            LiveWeatherVO weather = weatherService.getLiveWeather();
            if (weather == null) {
                return "天气信息暂时无法获取";
            }

            return String.format("当前%s天气：%s，温度%s℃，风向%s，风力%s级，湿度%s%%。数据更新时间：%s",
                    weather.getCity(),
                    weather.getWeather(),
                    weather.getTemperature(),
                    weather.getWinddirection(),
                    weather.getWindpower(),
                    weather.getHumidity(),
                    weather.getReporttime());
        } catch (Exception e) {
            log.error("查询天气失败", e);
            return "天气查询失败，请稍后重试";
        }
    }

    /**
     * 查询未来天气预报
     */
    @Tool("查询蜀南竹海景区所在地（长宁县）未来一周的天气预报。" +
         "无需参数，直接调用即可获取未来3-7天的天气预报信息。" +
         "返回内容包括：未来每一天的日期、星期、白天/夜间天气、温度范围、风向、风力等详细信息。" +
         "适用场景：用户询问'明天天气如何'、'后天会下雨吗'、'这周末天气怎么样'、'未来几天天气'、" +
         "'什么时候适合去玩'等未来天气查询，特别适合用户提前规划行程。" +
         "注意：此工具返回未来天气预报，如需查询当前实时天气，请使用queryWeather工具。")
    public String queryForecastWeather() {
        try {
            ForecastWeatherVO forecast = weatherService.getForecastWeather();
            if (forecast == null || forecast.getCasts() == null || forecast.getCasts().isEmpty()) {
                return "天气预报信息暂时无法获取";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📅 ").append(forecast.getCity()).append("未来天气预报：\n");
            sb.append("（数据更新时间：").append(forecast.getReporttime()).append("）\n\n");

            for (CastVO cast : forecast.getCasts()) {
                sb.append("📍 ").append(cast.getDate()).append(" ").append(cast.getWeek()).append("\n");
                sb.append("  白天：").append(cast.getDayweather())
                        .append("，").append(cast.getDaytemp()).append("℃")
                        .append("，").append(cast.getDaywind())
                        .append(cast.getDaypower()).append("级\n");
                sb.append("  夜间：").append(cast.getNightweather())
                        .append("，").append(cast.getNighttemp()).append("℃")
                        .append("，").append(cast.getNightwind())
                        .append(cast.getNightpower()).append("级\n");
                sb.append("\n");
            }

            sb.append("💡 温馨提示：根据天气情况合理安排出行时间，雨天注意防滑和携带雨具。");

            return sb.toString();
        } catch (Exception e) {
            log.error("查询天气预报失败", e);
            return "天气预报查询失败，请稍后重试";
        }
    }

    /**
     * 查询景点详情
     */
    @Tool("根据景点ID查询指定景点的详细信息。" +
         "\n⚠️ 【重要】此工具仅在以下情况使用：" +
         "\n1. 用户明确询问某个具体景点的详细信息（如：'翡翠长廊怎么样？'）" +
         "\n2. 用户想了解某个景点的门票价格" +
         "\n3. 用户已经知道景点名称，想要更多细节" +
         "\n\n❌ 【禁止】以下情况不要使用此工具：" +
         "\n- 路线规划时：应使用recommendRoute工具，它会自动包含景点信息" +
         "\n- 想了解所有景点：应使用listAllScenics工具" +
         "\n- 逐个查询多个景点：效率低且浪费资源，使用listAllScenics或recommendRoute" +
         "\n\n参数说明：scenicId=景点ID（必填，Long类型）" +
         "\n返回内容：景点名称、描述、地址、门票价格等")
    public String getScenicDetail(Long scenicId) {
        try {
            ScenicSpotVO scenic = scenicSpotService.getScenicSpotDetail(scenicId);
            if (scenic == null) {
                return "景点不存在";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("景点名称：").append(scenic.getName()).append("\n");
            sb.append("景点描述：").append(scenic.getDescription()).append("\n");
            sb.append("地址：").append(scenic.getAddress()).append("\n");

            // 查询该景点的门票
            try {
                List<ScenicTicketVO> tickets = scenicTicketService.findTicketsByScenicId(scenicId);
                if (tickets != null && !tickets.isEmpty()) {
                    sb.append("\n在售门票：\n");
                    for (ScenicTicketVO ticket : tickets) {
                        sb.append("- 价格：¥").append(ticket.getPrice())
                                .append("，已售：").append(ticket.getSoldCount()).append("张\n");
                    }
                }
            } catch (Exception e) {
                // 该景点可能没有门票，忽略异常
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("查询景点失败", e);
            return "景点查询失败";
        }
    }

    /**
     * 查询所有景点列表
     */
    @Tool("查询蜀南竹海景区内所有景点的基本信息列表。" +
         "\n无需参数，直接调用即可获取完整景点列表。" +
         "\n返回内容：每个景点的名称、简介、地址等基本信息。" +
         "\n\n✅ 【适用场景】：" +
         "\n1. 用户询问'景区有哪些景点？'" +
         "\n2. 用户想要大致了解景点概况" +
         "\n3. 用户还没决定去哪里玩，想先看看有什么" +
         "\n\n⚠️ 【注意】：" +
         "\n- 如果用户想要路线规划，应该直接使用recommendRoute工具，不要先调用此工具" +
         "\n- 如果用户已经表达了具体需求（如：时间、偏好），直接用recommendRoute更高效")
    public String listAllScenics() {
        try {
            List<ScenicSpotVO> scenics = scenicSpotService.getScenicSpotList();
            if (scenics == null || scenics.isEmpty()) {
                return "暂无景点信息";
            }

            StringBuilder sb = new StringBuilder("景区共有").append(scenics.size()).append("个景点：\n\n");
            for (ScenicSpotVO scenic : scenics) {
                sb.append("【").append(scenic.getName()).append("】\n");
                if (scenic.getDescription() != null && scenic.getDescription().length() > 100) {
                    sb.append(scenic.getDescription().substring(0, 100)).append("...\n");
                } else {
                    sb.append(scenic.getDescription()).append("\n");
                }
                sb.append("位置：").append(scenic.getAddress()).append("\n\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("查询景点列表失败", e);
            return "景点列表查询失败";
        }
    }

    /**
     * 查询所有在售门票
     */
    @Tool("查询景区内所有景点当前在售的门票类型、价格及销售情况。" +
         "无需参数，直接调用即可获取完整门票列表。" +
         "返回内容包括：按景点分组的门票信息，包含价格、已售数量等。" +
         "适用场景：用户询问门票价格、想了解各景点票价、购票前查询、或需要价格对比等。" +
         "注意：返回的是实时在售门票，价格可能根据季节或活动有所变化。建议在用户明确购买意向后再次确认价格。")
    public String listAvailableTickets() {
        try {
            // 先获取所有景点，然后查询每个景点的门票
            List<ScenicSpotVO> scenics = scenicSpotService.getScenicSpotList();
            if (scenics == null || scenics.isEmpty()) {
                return "暂无景点信息";
            }

            StringBuilder sb = new StringBuilder("当前在售门票：\n\n");
            for (ScenicSpotVO scenic : scenics) {
                try {
                    List<ScenicTicketVO> tickets = scenicTicketService.findTicketsByScenicId(scenic.getId());
                    if (tickets != null && !tickets.isEmpty()) {
                        sb.append("【").append(scenic.getName()).append("】\n");
                        for (ScenicTicketVO ticket : tickets) {
                            sb.append("  价格：¥").append(ticket.getPrice())
                                    .append("，已售：").append(ticket.getSoldCount()).append("张\n");
                        }
                        sb.append("\n");
                    }
                } catch (Exception e) {
                    // 该景点可能没有门票，继续下一个
                }
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("查询门票列表失败", e);
            return "门票列表查询失败";
        }
    }

    /**
     * 推荐游玩路线（智能版）- 路线规划的首选工具
     * 综合考虑天气、用户偏好、景点距离等因素
     */
    @Tool("🌟【路线规划首选工具】根据用户的游玩时长、游玩场景和天气情况智能推荐最合适的游玩路线和行程安排。" +
         "\n\n✅【优先使用此工具的场景】：" +
         "\n- 用户询问'怎么玩'、'路线推荐'、'行程安排'" +
         "\n- 用户提到游玩时间（半天、一天、两天等）" +
         "\n- 用户描述了偏好（摄影、休闲、有老人小孩等）" +
         "\n- 用户是自驾游客" +
         "\n\n💡【此工具的强大之处】：" +
         "\n- 一次性返回完整方案：景点+设施+天气+建议" +
         "\n- 自动按距离推荐餐厅、住宿、停车场、充电桩" +
         "\n- 无需再逐个查询景点详情" +
         "\n\n【重要提示】此工具返回的是基础推荐数据，你需要基于用户的具体描述进行深度分析和个性化解读，不要机械地复述工具输出！" +
         "\n\n参数说明：" +
         "- duration: 游玩时长（必填，String类型，例如：'一天'、'半天'、'4小时'、'两天'等）" +
         "- visitDate: 游玩日期（可选，String类型，格式：yyyy-MM-dd，用于查询天气）" +
         "- hasChildren: 是否有小孩（可选，Boolean类型，默认false）- 根据用户话语判断" +
         "- hasElderly: 是否有老人（可选，Boolean类型，默认false）- 根据用户话语判断" +
         "- hiking: 是否徒步（可选，Boolean类型，默认false）- 用户提到徒步、登山、运动等" +
         "- photography: 是否摄影（可选，Boolean类型，默认false）- 用户提到摄影、拍照等" +
         "- leisure: 是否休闲游（可选，Boolean类型，默认false）- 用户强调轻松、休闲" +
         "- selfDriving: 是否自驾游（可选，Boolean类型，默认false）- 用户提到自驾、开车" +
         "- hasElectricVehicle: 是否电动车（可选，Boolean类型，默认false）- 用户提到电动车、新能源车" +
         "\n返回内容：工具会返回结构化的路线数据，包括天气、景点、设施等信息。" +
         "\n\n【你应该怎么做】：" +
         "1. 在调用工具前，先从用户的话语中提取关键信息（老人/小孩/自驾/兴趣爱好等）" +
         "2. 调用工具后，不要直接输出工具返回的内容" +
         "3. 要基于用户的具体描述，用自己的语言解释推荐理由" +
         "4. 给出针对性的建议，展现你对用户需求的理解" +
         "5. 如用户提到老人，要特别说明为何选择这些轻松的景点" +
         "6. 如用户提到摄影，要说明拍照的最佳时间和技巧" +
         "7. 如用户自驾，要提醒停车和充电事项" +
         "\n\n适用场景：用户询问如何安排行程、想要路线推荐、不知道怎么玩、时间有限需要精简路线、" +
         "带老人/小孩出游、摄影爱好者、自驾游等各种场景。")
    public String recommendRoute(
            String duration,
            String visitDate,
            Boolean hasChildren,
            Boolean hasElderly,
            Boolean hiking,
            Boolean photography,
            Boolean leisure,
            Boolean selfDriving,
            Boolean hasElectricVehicle) {
        try {
            // 1. 解析游玩时长
            int durationHours = RouteRecommendService.parseDuration(duration);
            
            // 2. 构建用户偏好
            UserPreference preference = new UserPreference();
            preference.setDuration(durationHours);
            preference.setHasChildren(hasChildren != null && hasChildren);
            preference.setHasElderly(hasElderly != null && hasElderly);
            preference.setHiking(hiking != null && hiking);
            preference.setPhotography(photography != null && photography);
            preference.setLeisure(leisure != null && leisure);
            preference.setSelfDriving(selfDriving != null && selfDriving);
            preference.setHasElectricVehicle(hasElectricVehicle != null && hasElectricVehicle);
            
            // 3. 获取天气信息
            try {
                if (visitDate != null && !visitDate.isEmpty()) {
                    LocalDate date = LocalDate.parse(visitDate);
                    preference.setVisitDate(date);
                    
                    // 获取天气预报
                    ForecastWeatherVO forecast = weatherService.getForecastWeather();
                    if (forecast != null && forecast.getCasts() != null) {
                        for (CastVO cast : forecast.getCasts()) {
                            if (cast.getDate().equals(date.toString())) {
                                preference.setWeatherCondition(cast.getDayweather());
                                preference.setWeatherDesc(cast.getDayweather());
                                preference.setTemperature(Integer.parseInt(cast.getDaytemp()));
                                break;
                            }
                        }
                    }
                } else {
                    // 使用当前天气
                    LiveWeatherVO weather = weatherService.getLiveWeather();
                    if (weather != null) {
                        preference.setWeatherCondition(weather.getWeather());
                        preference.setWeatherDesc(weather.getWeather());
                        preference.setTemperature(Integer.parseInt(weather.getTemperature()));
                    }
                }
            } catch (Exception e) {
                log.warn("获取天气信息失败，使用默认配置", e);
            }
            
            // 4. 调用智能推荐服务
            RouteRecommendation recommendation = routeRecommendService.recommendRoute(preference);
            
            // 5. 格式化输出
            return formatRecommendation(recommendation);
            
        } catch (Exception e) {
            log.error("路线推荐失败", e);
            // 降级到简单推荐
            return getSimpleRecommendation(duration);
        }
    }
    
    /**
     * 格式化推荐结果（简化版 - 返回结构化数据供AI分析）
     * 重要：返回的是数据要点，不是完整的话术，让AI有空间进行个性化解读
     */
    private String formatRecommendation(RouteRecommendation recommendation) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("【路线推荐数据】\n");
        sb.append("路线类型：").append(recommendation.getTitle()).append("\n");
        
        // 天气信息
        if (recommendation.getWeatherInfo() != null && !recommendation.getWeatherInfo().isEmpty()) {
            sb.append("天气：").append(recommendation.getWeatherInfo()).append("\n");
        }
        
        sb.append("\n【行程安排】\n");
        
        // 路线分段
        if (recommendation.getSegments() != null) {
            for (RouteSegment segment : recommendation.getSegments()) {
                sb.append("\n时段：").append(segment.getPeriod());
                if (segment.getTimeRange() != null) {
                    sb.append(" (").append(segment.getTimeRange()).append(")");
                }
                sb.append("\n");
                
                // 描述（如午餐时间）
                if (segment.getDescription() != null) {
                    sb.append(segment.getDescription()).append("\n");
                }
                
                // 景点列表 - 简化格式
                if (segment.getScenics() != null && !segment.getScenics().isEmpty()) {
                    sb.append("景点：\n");
                    for (ScenicItem scenic : segment.getScenics()) {
                        sb.append("- ").append(scenic.getName());
                        sb.append(" | ").append(scenic.getDuration()).append("分钟");
                        if (scenic.getReason() != null) {
                            sb.append(" | 特点：").append(scenic.getReason());
                        }
                        sb.append("\n");
                        if (scenic.getTips() != null) {
                            sb.append("  提示：").append(scenic.getTips()).append("\n");
                        }
                    }
                }
                
                // 基础设施推荐 - 重要！要在显眼位置
                if (segment.getFacilityRecommendation() != null) {
                    String facilityInfo = formatFacilityRecommendationSimple(segment.getFacilityRecommendation());
                    if (!facilityInfo.trim().isEmpty()) {
                        sb.append("\n【配套设施】\n");
                        sb.append(facilityInfo);
                    }
                }
            }
        }
        
        sb.append("\n【通用建议】\n");
        // 温馨提示 - 只列出要点
        if (recommendation.getTips() != null) {
            sb.append(recommendation.getTips()).append("\n");
        }
        
        sb.append("\n【数据统计】\n");
        if (recommendation.getSummary() != null) {
            sb.append(recommendation.getSummary()).append("\n");
        }
        
        sb.append("\n【AI请注意】");
        sb.append("\n1. 以上是基础推荐数据，请你基于用户的具体情况（年龄、兴趣、出行方式等）进行个性化解读和建议");
        sb.append("\n2. 【配套设施】中的餐厅、住宿、停车场、充电桩等都已按距离排序，最近的排在最前面");
        sb.append("\n3. 请务必向用户介绍这些配套设施，并说明距离信息（如：'距离景点约0.8公里'）");
        sb.append("\n4. 强调这些推荐是基于距离优化的，让用户知道这些是离景点最近、最方便的选择");
        sb.append("\n5. 如果用户是自驾，特别要提及停车场（如有电动车，也要提充电桩）");
        sb.append("\n6. 如果是两日游，一定要推荐住宿并说明距离");
        
        return sb.toString();
    }
    
    /**
     * 格式化基础设施推荐（简化版 - 包含距离信息）
     */
    private String formatFacilityRecommendationSimple(RouteRecommendService.FacilityRecommendation facility) {
        StringBuilder sb = new StringBuilder();
        
        // 餐厅推荐（优先显示，必须显示）
        if (facility.getRestaurants() != null && !facility.getRestaurants().isEmpty()) {
            sb.append("🍽️ 餐厅推荐（按距离排序，离景点最近）：\n");
            int count = 1;
            for (RouteRecommendService.FacilityItem restaurant : facility.getRestaurants()) {
                sb.append("  ").append(count++).append(". ").append(restaurant.getName());
                if (restaurant.getDistance() != null && !restaurant.getDistance().isEmpty()) {
                    sb.append(" - ").append(restaurant.getDistance());
                }
                if (restaurant.getReason() != null && !restaurant.getReason().isEmpty()) {
                    sb.append(" | ").append(restaurant.getReason());
                }
                sb.append("\n");
            }
            sb.append("  💡 人均消费50-80元\n");
        } else {
            // 没有具体餐厅数据时，也要明确提示
            sb.append("🍽️ 用餐建议：景区内有多家餐厅可供选择，建议提前规划用餐时间（人均50-80元）\n");
        }
        
        // 住宿推荐（显示距离，强调"离景点近"）
        if (facility.getAccommodations() != null && !facility.getAccommodations().isEmpty()) {
            sb.append("🏨 住宿推荐（按距离排序，离景点最近）：\n");
            int count = 1;
            for (RouteRecommendService.FacilityItem accommodation : facility.getAccommodations()) {
                sb.append("  ").append(count++).append(". ").append(accommodation.getName());
                if (accommodation.getDistance() != null && !accommodation.getDistance().isEmpty()) {
                    sb.append(" - 距离").append(accommodation.getDistance());
                } else {
                    sb.append(" - 景区内住宿");
                }
                if (accommodation.getAddress() != null && !accommodation.getAddress().isEmpty()) {
                    sb.append(" | 地址：").append(accommodation.getAddress());
                }
                sb.append("\n");
            }
        }
        
        // 停车场推荐（显示距离，强调"最近"）
        if (facility.getParkings() != null && !facility.getParkings().isEmpty()) {
            sb.append("🅿️ 停车场推荐（按距离排序，离景点最近）：\n");
            int count = 1;
            for (RouteRecommendService.FacilityItem parking : facility.getParkings()) {
                sb.append("  ").append(count++).append(". ").append(parking.getName());
                if (parking.getDistance() != null && !parking.getDistance().isEmpty()) {
                    sb.append(" - ").append(parking.getDistance());
                } else {
                    sb.append(" - 景区内停车场");
                }
                sb.append("\n");
            }
        }
        
        // 充电桩推荐（显示距离，强调"最近"）
        if (facility.getChargingStations() != null && !facility.getChargingStations().isEmpty()) {
            sb.append("🔌 充电桩推荐（按距离排序，离景点最近）：\n");
            int count = 1;
            for (RouteRecommendService.FacilityItem charging : facility.getChargingStations()) {
                sb.append("  ").append(count++).append(". ").append(charging.getName());
                if (charging.getDistance() != null && !charging.getDistance().isEmpty()) {
                    sb.append(" - ").append(charging.getDistance());
                } else {
                    sb.append(" - 景区内充电桩");
                }
                sb.append("\n");
            }
        }
        
        // 卫生间位置（简化显示）
        if (facility.getToilets() != null && !facility.getToilets().isEmpty()) {
            sb.append("🚻 附近卫生间：");
            sb.append(facility.getToilets().stream()
                    .map(RouteRecommendService.FacilityItem::getName)
                    .limit(3)
                    .reduce((a, b) -> a + "、" + b)
                    .orElse(""));
            sb.append("\n");
        }
        
        // 其他服务设施（简化显示）
        if (facility.getServices() != null && !facility.getServices().isEmpty()) {
            sb.append("ℹ️ 服务设施：");
            sb.append(facility.getServices().stream()
                    .map(RouteRecommendService.FacilityItem::getName)
                    .limit(3)
                    .reduce((a, b) -> a + "、" + b)
                    .orElse(""));
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 格式化基础设施推荐（详细版 - 已弃用，保留用于兼容）
     */
    @Deprecated
    private String formatFacilityRecommendation(RouteRecommendService.FacilityRecommendation facility) {
        StringBuilder sb = new StringBuilder();
        
        // 餐厅推荐
        if (facility.getRestaurants() != null && !facility.getRestaurants().isEmpty()) {
            sb.append("\n🍽️ 推荐餐厅：\n");
            int count = 1;
            for (RouteRecommendService.FacilityItem restaurant : facility.getRestaurants()) {
                sb.append("  ").append(count++).append(". ").append(restaurant.getName());
                if (restaurant.getReason() != null) {
                    sb.append(" - ").append(restaurant.getReason());
                }
                sb.append("\n");
                if (restaurant.getAddress() != null) {
                    sb.append("     地址：").append(restaurant.getAddress()).append("\n");
                }
                if (restaurant.getContactPhone() != null) {
                    sb.append("     电话：").append(restaurant.getContactPhone()).append("\n");
                }
            }
        }
        
        // 住宿推荐
        if (facility.getAccommodations() != null && !facility.getAccommodations().isEmpty()) {
            sb.append("\n🏨 推荐住宿：\n");
            int count = 1;
            for (RouteRecommendService.FacilityItem accommodation : facility.getAccommodations()) {
                sb.append("  ").append(count++).append(". ").append(accommodation.getName());
                sb.append("\n");
                if (accommodation.getAddress() != null) {
                    sb.append("     地址：").append(accommodation.getAddress()).append("\n");
                }
                if (accommodation.getContactPhone() != null) {
                    sb.append("     电话：").append(accommodation.getContactPhone()).append("\n");
                }
            }
        }
        
        // 停车场推荐
        if (facility.getParkings() != null && !facility.getParkings().isEmpty()) {
            sb.append("\n🅿️ 停车场：\n");
            int count = 1;
            for (RouteRecommendService.FacilityItem parking : facility.getParkings()) {
                sb.append("  ").append(count++).append(". ").append(parking.getName());
                if (parking.getReason() != null) {
                    sb.append(" - ").append(parking.getReason());
                }
                sb.append("\n");
                if (parking.getAddress() != null) {
                    sb.append("     地址：").append(parking.getAddress()).append("\n");
                }
                if (parking.getContactPhone() != null) {
                    sb.append("     电话：").append(parking.getContactPhone()).append("\n");
                }
            }
        }
        
        // 充电桩推荐
        if (facility.getChargingStations() != null && !facility.getChargingStations().isEmpty()) {
            sb.append("\n🔌 充电桩：\n");
            int count = 1;
            for (RouteRecommendService.FacilityItem charging : facility.getChargingStations()) {
                sb.append("  ").append(count++).append(". ").append(charging.getName());
                if (charging.getReason() != null) {
                    sb.append(" - ").append(charging.getReason());
                }
                sb.append("\n");
                if (charging.getAddress() != null) {
                    sb.append("     地址：").append(charging.getAddress()).append("\n");
                }
                if (charging.getContactPhone() != null) {
                    sb.append("     电话：").append(charging.getContactPhone()).append("\n");
                }
            }
        }
        
        // 卫生间位置
        if (facility.getToilets() != null && !facility.getToilets().isEmpty()) {
            sb.append("\n🚻 附近卫生间：\n");
            for (RouteRecommendService.FacilityItem toilet : facility.getToilets()) {
                sb.append("  · ").append(toilet.getName());
                if (toilet.getAddress() != null) {
                    sb.append(" (").append(toilet.getAddress()).append(")");
                }
                sb.append("\n");
            }
        }
        
        // 其他服务设施
        if (facility.getServices() != null && !facility.getServices().isEmpty()) {
            sb.append("\n🏥 服务设施：\n");
            for (RouteRecommendService.FacilityItem service : facility.getServices()) {
                sb.append("  · ").append(service.getName());
                if (service.getReason() != null) {
                    sb.append(" - ").append(service.getReason());
                }
                sb.append("\n");
                if (service.getAddress() != null) {
                    sb.append("    地址：").append(service.getAddress()).append("\n");
                }
            }
        }
        
        // 设施提示
        if (facility.getTips() != null && !facility.getTips().isEmpty()) {
            sb.append("\n").append(facility.getTips()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 简单推荐（降级方案）
     */
    private String getSimpleRecommendation(String duration) {
        if (duration != null && (duration.contains("一天") || duration.contains("1天") || duration.contains("全天"))) {
            return """
                    为您推荐一日游经典路线：
                    
                    ⏰ 上午 (08:00-12:00)
                    📍 忘忧谷 (120分钟) - 核心竹林景观，负氧离子含量极高
                    📍 天宝寨 (90分钟) - 登高望远，俯瞰竹海
                    
                    🍽️ 中午 (12:00-13:30)
                    📍 景区餐厅 - 品尝地道竹笋宴
                    
                    ⏰ 下午 (13:30-17:00)
                    📍 七彩飞瀑 (60分钟) - 拍照打卡圣地
                    📍 翡翠长廊 (90分钟) - 竹海精华路段
                    
                    💡 温馨提示：
                    - 建议穿着舒适的运动鞋
                    - 携带防晒用品和水
                    - 景区较大，建议购买观光车票
                    """;
        } else if (duration != null && (duration.contains("半天") || duration.contains("4小时") || 
                  duration.contains("上午") || duration.contains("下午"))) {
            return """
                    为您推荐半日游精华路线：
                    
                    📍 忘忧谷 (120分钟) - 必游景点，竹林幽静
                    📍 七彩飞瀑 (60分钟) - 拍照打卡
                    📍 翡翠长廊 (60分钟) - 漫步竹林
                    
                    💡 温馨提示：
                    - 提前购买门票可节省排队时间
                    - 建议游览时间：上午9:00-12:00或下午14:00-17:00
                    """;
        } else {
            return """
                    蜀南竹海景区建议游玩时长：
                    
                    🔸 精华半日游（4小时）：忘忧谷 → 七彩飞瀑 → 翡翠长廊
                    🔸 经典一日游（8小时）：全部核心景点深度游
                    🔸 休闲两日游：慢节奏体验竹海生活，品尝特色美食
                    
                    请告诉我您计划的游玩时长，以及是否有老人、小孩同行，我将为您制定更详细的路线！
                    """;
        }
    }

    /**
     * 创建订单（AI引导用户提供信息后调用）
     * 注意：
     * 1. 此方法仅创建订单，不进行实际支付。支付需要用户在小程序中完成。
     * 2. userId由系统自动从当前登录用户上下文获取，无需AI提供。
     * 
     * @param userId 用户ID（系统自动注入，非AI参数）
     * @param phone 手机号
     * @param orderType 订单类型（1=景点门票, 2=住宿, 3=餐饮, 4=文创商品）
     * @param visitDate 游玩日期（格式：yyyy-MM-dd）
     * @param ticketId 门票ID
     * @param ticketName 门票名称
     * @param quantity 数量
     * @param price 单价
     * @return 订单创建结果信息（包含订单号）
     */
    @Tool("创建订单。在引导用户确认所有必要信息（游玩日期、门票类型、数量、手机号）后调用此工具创建订单。" +
         "注意：userId由系统自动识别当前用户，无需在参数中提供。" +
         "参数说明：phone=手机号, orderType=订单类型（1=景点门票，可选，默认为1）, " +
         "visitDate=游玩日期（格式：yyyy-MM-dd，必须是未来日期）, ticketId=门票ID, ticketName=门票名称, " +
         "quantity=购买数量, price=门票单价")
    public String createOrder(
            Long userId,
            String phone, 
            Integer orderType,
            String visitDate,
            Long ticketId,
            String ticketName,
            Integer quantity,
            Double price) {
        try {
            log.info("AI工具调用：创建订单 - userId={}, phone={}, visitDate={}, ticketId={}, quantity={}", 
                    userId, phone, visitDate, ticketId, quantity);
            
            // 参数校验
            if (userId == null || phone == null || visitDate == null || 
                ticketId == null || ticketName == null || quantity == null || price == null) {
                return "创建订单失败：参数不完整，请确保已收集用户的手机号、游玩日期、门票信息和数量";
            }
            
            // 解析日期
            LocalDate visit = LocalDate.parse(visitDate);
            
            // 构建订单明细
            List<OrderItemDTO> orderItems = new ArrayList<>();
            OrderItemDTO item = new OrderItemDTO();
            item.setItemType(1); // 门票类型
            item.setItemId(ticketId);
            item.setItemName(ticketName);
            item.setQuantity(quantity);
            item.setPrice(new BigDecimal(price.toString()));
            orderItems.add(item);
            
            // 构建订单DTO
            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setUserId(userId);
            dto.setPhone(phone);
            dto.setOrderType(orderType != null ? orderType : 1); // 默认门票类型
            dto.setVisitDate(visit);
            dto.setOrderItems(orderItems);
            
            // 调用订单服务创建订单
            String orderNo = orderService.createOrder(dto);
            
            // 计算总金额
            BigDecimal totalAmount = new BigDecimal(price.toString()).multiply(new BigDecimal(quantity));
            
            return String.format("""
                    ✅ 订单创建成功！
                    
                    📋 订单信息：
                    订单号：%s
                    门票：%s × %d张
                    游玩日期：%s
                    总金额：¥%.2f
                    
                    ⚠️ 重要提示：
                    1. 订单已创建，请在30分钟内完成支付
                    2. 支付后门票立即生效
                    3. 游玩当天请出示订单供工作人员确认
                    4. 若含学生票，请在游玩当前持学生证或提供学信网学籍信息
                    
                    💡 接下来请：
                    - 前往"我的订单"完成支付
                    - 支付方式：微信支付
                    """, 
                    orderNo, ticketName, quantity, visitDate, totalAmount.doubleValue());
            
        } catch (Exception e) {
            log.error("AI工具调用失败：创建订单", e);
            return "订单创建失败：" + e.getMessage() + "。请稍后重试或联系人工客服。";
        }
    }
    
    /**
     * 查询景区设施信息 - 新增工具
     */
    @Tool("🏗️ 查询景区内各种设施的位置和信息，如卫生间、停车场、餐厅、充电桩等。" +
         "\n\n✅【适用场景】：" +
         "\n- 用户询问'卫生间在哪里'、'哪里有停车场'、'充电桩位置'等单独设施查询" +
         "\n- 用户需要特定类型设施的详细信息" +
         "\n- 补充路线规划中未涵盖的设施查询" +
         "\n\n📍【设施类型说明】：" +
         "\n- 卫生间/厕所：toilet" +
         "\n- 停车场：parking" +  
         "\n- 餐厅/餐饮：restaurant" +
         "\n- 住宿/酒店：accommodation" +
         "\n- 充电桩：charging" +
         "\n- 游客中心：visitor_center" +
         "\n- 医务室：medical" +
         "\n- 售票处：ticket_office" +
         "\n- 出入口：entrance" +
         "\n- 乘车点：bus_stop" +
         "\n- 服务点：service" +
         "\n\n💡【使用建议】：" +
         "\n- 如果用户是在询问路线规划，优先使用recommendRoute工具（包含完整的设施推荐）" +
         "\n- 本工具适合单独的设施查询需求" +
         "\n- 查询结果会按距离排序，最近的在前面")
    public String queryFacilities(String facilityType) {
        try {
            // 设施类型映射
            Long typeId = mapFacilityType(facilityType);
            if (typeId == null) {
                return "抱歉，不支持查询此类设施。支持的设施类型包括：卫生间、停车场、餐厅、住宿、充电桩、游客中心、医务室、售票处、出入口、乘车点、服务点等。";
            }
            
            // 查询设施信息
            List<FacilityVO> facilities = facilityService.getFacilitiesByType(typeId);
            
            if (facilities == null || facilities.isEmpty()) {
                return String.format("抱歉，暂未找到%s的相关信息。您可以咨询现场工作人员或拨打景区客服电话获取帮助。", getFacilityTypeName(typeId));
            }
            
            // 格式化输出
            StringBuilder sb = new StringBuilder();
            sb.append("📍 ").append(getFacilityTypeName(typeId)).append("信息：\n\n");
            
            int count = 1;
            for (FacilityVO facility : facilities) {
                sb.append(count++).append(". **").append(facility.getName()).append("**\n");
                
                if (facility.getAddress() != null && !facility.getAddress().trim().isEmpty()) {
                    sb.append("   📍 位置：").append(facility.getAddress()).append("\n");
                }
                
                if (facility.getContactPhone() != null && !facility.getContactPhone().trim().isEmpty()) {
                    sb.append("   📞 电话：").append(facility.getContactPhone()).append("\n");
                }
                
                if (facility.getOpenTime() != null && !facility.getOpenTime().trim().isEmpty()) {
                    sb.append("   🕐 开放时间：").append(facility.getOpenTime()).append("\n");
                }
                
                sb.append("\n");
            }
            
            // 添加贴心提示
            sb.append("💡 **贴心提示**：\n");
            if (typeId.equals(2L)) { // 停车场
                sb.append("- 建议您记住停车位置，方便离开时找车\n");
                sb.append("- 如需充电，请尝试询问是否有充电桩设施\n");
            } else if (typeId.equals(1L)) { // 卫生间
                sb.append("- 景区面积较大，建议提前规划，及时使用\n");
                sb.append("- 部分卫生间可能因维护暂时关闭，可寻找就近替代\n");
            } else if (typeId.equals(14L)) { // 充电桩
                sb.append("- 请提前准备好充电卡或确认支付方式\n");
                sb.append("- 充电时间建议不少于30分钟以确保充足电量\n");
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            log.error("设施查询失败", e);
            return "设施查询失败，请稍后重试或联系人工客服。";
        }
    }
    
    /**
     * 设施类型映射
     */
    private Long mapFacilityType(String facilityType) {
        if (facilityType == null) {
            return null;
        }
        
        String type = facilityType.toLowerCase().trim();
        return switch (type) {
            case "toilet", "卫生间", "厕所", "洗手间" -> 1L;
            case "parking", "停车场", "停车位", "车位" -> 2L;
            case "restaurant", "餐厅", "餐饮", "吃饭", "用餐" -> 3L;
            case "shopping", "商场", "购物" -> 4L;
            case "visitor_center", "游客中心", "咨询中心" -> 5L;
            case "medical", "医务室", "医疗", "急救" -> 6L;
            case "accommodation", "住宿", "酒店", "宾馆" -> 7L;
            case "cable_car", "缆车", "乘缆点" -> 8L;
            case "ticket_office", "售票处", "买票" -> 9L;
            case "entrance", "出入口", "入口", "出口" -> 10L;
            case "bus_stop", "乘车点", "班车", "巴士" -> 12L;
            case "service", "服务点", "服务" -> 13L;
            case "charging", "充电桩", "充电", "电桩" -> 14L;
            default -> null;
        };
    }
    
    /**
     * 获取设施类型名称
     */
    private String getFacilityTypeName(Long typeId) {
        return switch (typeId.intValue()) {
            case 1 -> "卫生间";
            case 2 -> "停车场";
            case 3 -> "餐厅";
            case 4 -> "商场";
            case 5 -> "游客中心";
            case 6 -> "医务室";
            case 7 -> "住宿";
            case 8 -> "乘缆点";
            case 9 -> "售票处";
            case 10 -> "出入口";
            case 12 -> "乘车点";
            case 13 -> "服务点";
            case 14 -> "充电桩";
            default -> "设施";
        };
    }
}

