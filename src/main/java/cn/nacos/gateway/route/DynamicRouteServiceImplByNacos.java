package cn.nacos.gateway.route;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
@RefreshScope
@Slf4j
public class DynamicRouteServiceImplByNacos {


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
        log.info("=======================================");
        log.info("dataId:{}", dataId);
        log.info("group:{}", group);
        log.info("address:{}", address);
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
            log.info("Nacos初始化监听：{}", content);
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