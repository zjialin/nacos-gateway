package cn.nacos.gateway.route;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
@RefreshScope
public class DynamicRouteServiceImplByNacos {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private DynamicRouteServiceImpl dynamicRouteService;


    @Value("${gateway-data-id}")
    private String dataId;

    @Value("${gateway-group}")
    private String group;

    @Value("${spring.cloud.nacos.config.server-addr}")
    private String address;


    @Bean
    public String routeServiceInit() {
        logger.info("=======================================");
        logger.info("dataId:{}", dataId);
        logger.info("group:{}", group);
        logger.info("address:{}", address);
        dynamicRouteByNacosListener(dataId, group, address);
        return "success";
    }


    /**
     * 监听Nacos Server下发的动态路由配置
     *
     * @param dataId
     * @param group
     */
    public void dynamicRouteByNacosListener(String dataId, String group, String address) {
        try {
            ConfigService configService = NacosFactory.createConfigService(address);
            String content = configService.getConfig(dataId, group, 5000);
            logger.info("Nacos初始化监听：{}", content);
            configService.addListener(dataId, group, new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    RouteDefinition definition = JSONObject.parseObject(configInfo, RouteDefinition.class);
                    dynamicRouteService.update(definition);
                }
                @Override
                public Executor getExecutor() {
                    return null;
                }
            });
        } catch (NacosException e) {
            //todo 提醒:异常自行处理此处省略
        }
    }

}