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

    private def responseError(HttpServerRequest req, Throwable e) {
        logger.error(e.getMessage(), e)
        def json = ["status": "error", "message": "System exception"]
        req.response.setStatusCode(500)
        req.response.putHeader("Content-Type", "application/json;charset=utf-8")
        req.response.end(Json.encode(json))
    }

    private def responseOK(HttpServerRequest req, Map map) {
        map<<["status": "ok"]
        req.response.setStatusCode(200)
        req.response.putHeader("Content-Type", "application/json;charset=utf-8")
        req.response.end(Json.encode(map))
    }

    private static final long DEFAULT_TIMEOUT_TIME = 1000l

    @Override
    JRM routeMatcher() {
        RouteMatcher matcher = new RouteMatcher();
        def vertx = new Vertx(vertx)

        /**
         *  获取商品详情
         *  可以被缓存
         */
        matcher.get("/item") { HttpServerRequest req ->
            vertx.eventBus.sendWithTimeout("getItem", [id: req.params.id], DEFAULT_TIMEOUT_TIME) { AsyncResult<Message> reply ->
                if (reply.succeeded) {
                    responseOK(req,reply.result.body())
                } else {
                    responseError(req, reply.cause)
                }
            }
        }

//        matcher.get("/price"){}

        /**
         * 根据skuId列表获取sku可售库存
         * 实时
         */
        matcher.get("/skus/quantity") { HttpServerRequest req ->
            vertx.eventBus.sendWithTimeout("findAllQuantitiesBySkuIds", [ids: req.params.get("ids")?.split(",")], DEFAULT_TIMEOUT_TIME) { AsyncResult<Message> reply ->
                if (reply.succeeded) {
                    responseOK(reply.result.body());
                } else {
                    responseError(req, reply.cause)
                }
            }
        }

        /**
         * 获取当前系统时间
         */
        matcher.get("/time") { HttpServerRequest req ->
            Date now = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            responseOK([status: "ok", currentTime: dateFormat.format(now)]);
        }

        /**
         * 发起一笔交易
         * 创建订单
         */
        matcher.post("/trade") { HttpServerRequest req ->
            req.bodyHandler { Buffer buffer ->
                String contentType = req.headers.get("Content-Type")
                logger.debug("Content type is ${contentType}")
                logger.debug("Body is ${buffer.toString()}")
                def params = [:]
                if (contentType?.contains("application/x-www-form-urlencoded")) {
                    def qsd = new QueryStringDecoder(buffer.toString(), false)
                    params = qsd.parameters().collectEntries {
                        if (it.value.size() <= 1) {
                            [(it.key), it.value.get(0)]
                        } else {
                            [(it.key), it.value]
                        }
                    }
                } else if (contentType?.contains("application/json")) {
                    params = Json.decodeValue(buffer.toString(), Map.class)
                } else {
                    //TODO still not support content type "multipart/form-data"
                }
                logger.debug("Params is ${params}")
                vertx.eventBus.sendWithTimeout("createTrade", params, DEFAULT_TIMEOUT_TIME) { AsyncResult<Message> reply ->
                    if (reply.succeeded) {
                        responseOK(reply.result.body());
                    } else {
                        responseError(req, reply.cause)
                    }
                }
            }
        }

        matcher.get("/testClient") {

            (0..50).each {
                vertx.eventBus.send("createOrder", [:]) { Message message ->
                    logger.info(message.body())
                }
            }
        }

        matcher.jRM.noMatch(staticHandler());
        return matcher.jRM;
    }
}
