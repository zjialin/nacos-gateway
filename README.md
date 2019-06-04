基于 nacos  spring-cloud-starter-gateway 移动网关




{
    "id": "jd_route",
    "order": 0,
    "filters": [],
    "predicates": [{
        "name": "Path"
        "args": {
            "pattern": "/jd"
        },
    }],
    "lb": "/test-app"
}