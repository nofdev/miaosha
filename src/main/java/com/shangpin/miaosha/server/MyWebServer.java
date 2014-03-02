package com.shangpin.miaosha.server;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.mods.web.WebServerBase;

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
                String id = req.params().get("id");
                if(id!=null&&!"".equals(id)){
                    vertx.eventBus().send("product", id, new Handler<Message>() {
                        @Override
                        public void handle(Message reply) {
                            req.response().end(reply.body().toString());
                        }
                    });
                }else {
                    req.response().setStatusCode(403);
                    req.response().end();
                }
            }
        });

//        matcher.get("/price"){
//
//        }
//        matcher.get("/inventory"){
//
//        }
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
