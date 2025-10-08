package com.snzh.ai.tools;

import com.snzh.domain.dto.OrderCreateDTO;
import com.snzh.domain.dto.OrderItemDTO;
import com.snzh.domain.vo.CastVO;
import com.snzh.domain.vo.ForecastWeatherVO;
import com.snzh.domain.vo.LiveWeatherVO;
import com.snzh.domain.vo.ScenicSpotVO;
import com.snzh.domain.vo.ScenicTicketVO;
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
         "参数说明：scenicId=景点ID（必填，Long类型，需要先通过listAllScenics获取景点列表及其ID）。" +
         "返回内容包括：景点名称、详细描述、具体地址、关联的门票价格及销售情况等。" +
         "适用场景：用户询问某个具体景点的详细信息、想了解景点特色、查询门票价格等。" +
         "注意：如果用户未指定景点，应先调用listAllScenics展示景点列表让用户选择。")
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
         "无需参数，直接调用即可获取完整景点列表。" +
         "返回内容包括：每个景点的名称、简介（前100字）、地址等基本信息。" +
         "适用场景：用户询问景区有哪些景点、想要景点推荐、计划游玩路线、或需要景点概览等。" +
         "提示：如需查看某个景点的详细信息，可使用返回的景点信息配合getScenicDetail工具进一步查询。")
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
     * 推荐游玩路线
     * 注：这是简化版本，实际可以集成更复杂的路径规划算法
     */
    @Tool("根据用户的游玩时长智能推荐合适的游玩路线和行程安排。" +
         "参数说明：duration=游玩时长描述（必填，String类型，例如：'一天'、'半天'、'4小时'、'上午'、'下午'等）。" +
         "返回内容包括：详细的分时段行程安排、推荐景点、预计游览时间、温馨提示等。" +
         "适用场景：用户询问如何安排行程、想要路线推荐、不知道怎么玩、时间有限需要精简路线等。" +
         "路线类型：支持半日游（4小时）、一日游（全天）和多日游，会根据时长自动匹配最优路线。" +
         "注意：如果用户未明确说明游玩时长，应主动询问以便提供更准确的路线建议。")
    public String recommendRoute(String duration) {
        if (duration.contains("一天") || duration.contains("1天") || duration.contains("全天")) {
            return """
                    为您推荐一日游经典路线：
                    
                    ⏰ 上午 (08:00-12:00)
                    📍 忘忧谷 (2小时) - 核心竹林景观，负氧离子含量极高
                    📍 天宝寨 (1.5小时) - 登高望远，俯瞰竹海
                    
                    🍽️ 中午 (12:00-13:30)
                    📍 竹海人家 - 品尝地道竹笋宴
                    
                    ⏰ 下午 (13:30-17:00)
                    📍 七彩飞瀑 (1小时) - 拍照打卡圣地
                    📍 翡翠长廊 (1.5小时) - 竹海精华路段
                    
                    💡 温馨提示：
                    - 建议穿着舒适的运动鞋
                    - 携带防晒用品和水
                    - 景区较大，建议购买观光车票
                    """;
        } else if (duration.contains("半天") || duration.contains("4小时") || duration.contains("上午") || duration.contains("下午")) {
            return """
                    为您推荐半日游精华路线：
                    
                    📍 忘忧谷 (2小时) - 必游景点
                    📍 七彩飞瀑 (1小时) - 拍照打卡
                    📍 翡翠长廊 (1小时) - 漫步竹林
                    
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
                    
                    请告诉我您计划的游玩时长，我将为您制定详细路线！
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
}

