package com.shangpin.miaosha.server;

import com.shangpin.miaosha.BusinessException;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.mods.web.WebServerBase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Qiang on 3/2/14.
 */
public class MyWebServer extends WebServerBase {

    private void responseError(HttpServerRequest req, Exception e) {
        responseError(req, "System exception", e);
    }

    private void responseError(HttpServerRequest req, String error, Exception e) {
        logger.error(error, e);
        JsonObject json = new JsonObject().putString("status", "error").putString("message", error);
        req.response().setStatusCode(500);
        req.response().end(json.encodePrettily());
    }


    @Override
    protected RouteMatcher routeMatcher() {
        RouteMatcher matcher = new RouteMatcher();
        matcher.get("/product", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                try {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", req.params().get("id"));
                    vertx.eventBus().send("product/get", new JsonObject(map), new Handler<Message>() {
                        @Override
                        public void handle(Message reply) {
                            req.response().end(reply.body().toString());
                        }
                    });
                } catch (BusinessException e) {
                    responseError(req, e.getMessage(), e);
                } catch (Exception e) {
                    responseError(req, e);
                }
            }
        });

//        matcher.get("/price"){
//
//        }
        matcher.get("/inventories", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                try {
                    Map<String, Object> map = new HashMap<>();
                    String ids = req.params().get("ids");
                    if(ids!=null){
                        map.put("ids", ids.split(","));
                    }
                    vertx.eventBus().send("inventories/get", new JsonObject(map), new Handler<Message>() {
                        @Override
                        public void handle(Message reply) {
                            req.response().end(reply.body().toString());
                        }
                    });
                } catch (BusinessException e) {
                    responseError(req, e.getMessage(), e);
                } catch (Exception e) {
                    responseError(req, e);
                }
            }
        });

        matcher.get("/time",new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest req) {
                Date now = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                req.response().end(new JsonObject().putString("status","ok").putString("currentTime",dateFormat.format(now)).encodePrettily());
            }
        });
        matcher.post("/order",new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {

                req.endHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {
                        Map<String,Object> map = new HashMap<>();
                        MultiMap multiMap = req.formAttributes();

                        for(Map.Entry<String,String> entry:multiMap){
                            if(map.get(entry.getKey())==null){
                                map.put(entry.getKey(),entry.getValue());
                            }else {
                                map.get(entry.getKey()+","+entry.getValue());
                            }
                        }

                        vertx.eventBus().send("order/post",new JsonObject(map),new Handler<Message>() {
                            @Override
                            public void handle(Message reply) {

                            }
                        });
                    }
                });
            }
        });

        matcher.noMatch(staticHandler());
        return matcher;
    }
}
