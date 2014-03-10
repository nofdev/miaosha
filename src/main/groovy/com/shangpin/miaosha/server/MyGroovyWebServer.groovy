package com.shangpin.miaosha.server

import io.netty.handler.codec.http.QueryStringDecoder
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.core.AsyncResult
import org.vertx.java.core.http.RouteMatcher as JRM
import org.vertx.java.core.json.impl.Json
import org.vertx.mods.web.WebServerBase

import java.text.DateFormat
import java.text.SimpleDateFormat

public class MyGroovyWebServer extends WebServerBase {

//    private void responseError(HttpServerRequest req, Exception e) {
//        responseError(req, "System exception", e);
//    }
//
//    private void responseError(HttpServerRequest req, String error, Exception e) {
//        logger.error(error, e);
//        def json = ["status": "error", "message": error];
//        req.response.setStatusCode(500);
//        req.response.end(json);
//    }


    @Override
    JRM routeMatcher() {
        RouteMatcher matcher = new RouteMatcher();
        def vertx = new Vertx(vertx)

        /**
         *  获取商品详情
         *  可以被缓存
         */
        matcher.get("/item") { HttpServerRequest req ->
            vertx.eventBus.sendWithTimeout("getItem",[id:req.params.id],1000L){AsyncResult<Message> reply ->
                if(reply.succeeded){
                    reply.result.reply("aaaaa")
//                    req.response.end(Json.encode(reply.result.body()))
                }else{
//                    req.response.end(reply.result.reply())
//                    reply.result.reply("aaaaa")
                }
            }
        }

//        matcher.get("/price"){}

        /**
         * 根据skuId列表获取sku可售库存
         * 实时
         */
        matcher.get("/skus/quantity") { HttpServerRequest req ->
            vertx.eventBus.send("findAllQuantitiesBySkuIds", [ids: req.params.get("ids")?.split(",")]) { Message reply ->
                req.response.end(Json.encode(reply.body()));
            }
        }

        /**
         * 获取当前系统时间
         */
        matcher.get("/time") { HttpServerRequest req ->
            Date now = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            req.response.end(Json.encode([status: "ok", currentTime: dateFormat.format(now)]));
        }

        /**
         * 发起一笔交易
         * 创建订单
         */
        matcher.post("/trade") { HttpServerRequest req ->
            req.bodyHandler { Buffer buffer ->
                String contentType = req.headers.get("Content-Type")
                logger.debug(buffer.toString())
                def params = [:]
                if (contentType.contains("application/x-www-form-urlencoded")) {
                    def qsd = new QueryStringDecoder(buffer.toString(), false)
                    params = qsd.parameters().collectEntries {
                        if (it.value.size() <= 1) {
                            [(it.key), it.value.get(0)]
                        } else {
                            [(it.key), it.value]
                        }
                    }
                } else {
                    //TODO still not support content type "multipart/form-data"
                }
                vertx.eventBus.send("createTrade", params) { Message reply ->
                    req.response.end(Json.encode(reply.body()))
                }
            }
        }


        matcher.jRM.noMatch(staticHandler());
        return matcher.jRM;
    }
}
