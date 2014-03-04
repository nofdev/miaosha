package com.shangpin.miaosha.server;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.mods.web.WebServerBase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Qiang on 3/2/14.
 */
public class MyWebServer extends WebServerBase {
    @Override
    protected RouteMatcher routeMatcher() {
        RouteMatcher matcher = new RouteMatcher();
        matcher.get("/product", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                Map<String,Object> map = new HashMap<>();
                map.put("id",req.params().get("id"));
                vertx.eventBus().send("product", new JsonObject(map), new Handler<Message>() {
                    @Override
                    public void handle(Message reply) {
                        req.response().end(reply.body().toString());
                    }
                });
            }
        });

//        matcher.get("/price"){
//
//        }
        matcher.get("/inventories",new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                Map<String,Object> map = new HashMap<>();
                map.put("ids",req.params().get("ids").split(","));
                vertx.eventBus().send("inventories", new JsonObject(map), new Handler<Message>() {
                    @Override
                    public void handle(Message reply) {
                        req.response().end(reply.body().toString());
                    }
                });
            }
        });
//        matcher.get("/time"){
//
//        }
//        matcher.post("/order"){
//
//        }
        matcher.noMatch(staticHandler());
        return matcher;
    }
}
