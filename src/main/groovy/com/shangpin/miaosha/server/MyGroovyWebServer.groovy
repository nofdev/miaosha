package com.shangpin.miaosha.server

import org.vertx.groovy.core.Vertx
import org.vertx.mods.web.WebServerBase
import org.vertx.java.core.http.RouteMatcher as JRM

import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher

public class MyGroovyWebServer extends WebServerBase {
    @Override
    JRM routeMatcher() {
        RouteMatcher matcher = new RouteMatcher();
        def vertxx = new Vertx(vertx)
        matcher.get("/product") { HttpServerRequest req ->
            if (!req.params.id)
                req.response.end('<html><p>Please specify product id!!</p></html>')
            else
                vertxx.eventBus.send('product', ['id': req.params.id]) { Message reply ->
                    req.response.end(reply.body().toString())
                }
        }

//        matcher.get("/price"){
//
//        }
//        matcher.get("/inventories",new Handler<HttpServerRequest>() {
//            @Override
//            public void handle(final HttpServerRequest req) {
//                Map<String,Object> map = new HashMap<>();
//                map.put("ids",req.params().get("ids").split(","));
//                vertx.eventBus().send("inventories", new JsonObject(map), new Handler<Message>() {
//                    @Override
//                    public void handle(Message reply) {
//                        req.response().end(reply.body().toString());
//                    }
//                });
//            }
//        });
//        matcher.get("/time"){
//
//        }
//        matcher.post("/order"){
//
//        }
        matcher.jRM.noMatch(staticHandler());
        return matcher.jRM;
    }
}
