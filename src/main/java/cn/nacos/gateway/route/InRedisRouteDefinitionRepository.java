package cn.nacos.gateway.route;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;


@Component
public class InRedisRouteDefinitionRepository implements RouteDefinitionRepository {

    private Logger logger = LoggerFactory.getLogger(this.toString());

    private static final String GATEWAY_ROUTES = "nacos_gateway_routes";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        stringRedisTemplate.opsForHash().values(GATEWAY_ROUTES).stream().forEach(routeDefinition -> {
            routeDefinitions.add(JSONObject.parseObject(routeDefinition.toString(), RouteDefinition.class));
        });
        return Flux.fromIterable(routeDefinitions);
    }


    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(routeDefinition -> {

            stringRedisTemplate.opsForHash().put(GATEWAY_ROUTES, routeDefinition.getId(),
                    JSONObject.toJSONString(routeDefinition));
            return Mono.empty();
        });
    }


    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            if (stringRedisTemplate.opsForHash().hasKey(GATEWAY_ROUTES, id)) {
                stringRedisTemplate.opsForHash().delete(GATEWAY_ROUTES, id);
                return Mono.empty();
            } else {
                return Mono.defer(() -> Mono.error(new NotFoundException("RouteDefinition Not Found: Id=" + routeId)));
            }
        });
    }


}
