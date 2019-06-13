package cn.nacos.gateway.controller;

import cn.nacos.gateway.model.GatewayPredicateDefinition;
import cn.nacos.gateway.model.GatewayRouteDefinition;
import cn.nacos.gateway.route.DynamicRouteServiceImpl;
import cn.nacos.gateway.route.InRedisRouteDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("route")
public class RouteController {

    @Autowired
    private DynamicRouteServiceImpl dynamicRouteService;

    @Autowired
    private InRedisRouteDefinitionRepository inRedisRouteDefinitionRepository;

    @ResponseBody
    @GetMapping("/getAll")
    public Flux<RouteDefinition> getAll() {
        return inRedisRouteDefinitionRepository.getRouteDefinitions();
    }


    @ResponseBody
    @GetMapping("/add")
    public String add(@RequestBody GatewayRouteDefinition gatewayRouteDefinition) {
        RouteDefinition routeDefinition = assembleRouteDefinition(gatewayRouteDefinition);
        return dynamicRouteService.add(routeDefinition);
    }

    @ResponseBody
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable String id) {
        return dynamicRouteService.delete(id);
    }


    @ResponseBody
    @GetMapping("/update")
    public String update(@RequestBody GatewayRouteDefinition gatewayRouteDefinition) {
        RouteDefinition routeDefinition = assembleRouteDefinition(gatewayRouteDefinition);
        return dynamicRouteService.update(routeDefinition);
    }



    private RouteDefinition assembleRouteDefinition(GatewayRouteDefinition gatewayRouteDefinition) {
        RouteDefinition definition = new RouteDefinition();
        List<PredicateDefinition> pdList = new ArrayList<>();
        definition.setId(gatewayRouteDefinition.getId());
        List<GatewayPredicateDefinition> predicates = gatewayRouteDefinition.getPredicates();
        for (GatewayPredicateDefinition gatewayPredicateDefinition : predicates) {
            PredicateDefinition predicateDefinition = new PredicateDefinition();
            predicateDefinition.setArgs(gatewayPredicateDefinition.getArgs());
            predicateDefinition.setName(gatewayPredicateDefinition.getName());
            pdList.add(predicateDefinition);
        }
        definition.setPredicates(pdList);
        URI uri = UriComponentsBuilder.fromHttpUrl(gatewayRouteDefinition.getUri()).build().toUri();
        definition.setUri(uri);
        return definition;
    }
}
