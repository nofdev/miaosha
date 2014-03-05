package com.shangpin.miaosha.server

import com.shangpin.miaosha.BusinessException
import io.netty.handler.codec.http.QueryStringDecoder
import org.vertx.groovy.core.MultiMap
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.buffer.Buffer
import org.vertx.java.core.json.impl.Json
import org.vertx.mods.web.WebServerBase
import org.vertx.java.core.http.RouteMatcher as JRM

import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher

import java.text.DateFormat
import java.text.SimpleDateFormat

public class MyGroovyWebServer extends WebServerBase {

    private void responseError(HttpServerRequest req, Exception e) {
        responseError(req, "System exception", e);
    }

    private void responseError(HttpServerRequest req, String error, Exception e) {
        logger.error(error, e);
        def json = ["status": "error", "message": error];
        req.response.setStatusCode(500);
        req.response.end(json);
    }


    @Override
    JRM routeMatcher() {
        RouteMatcher matcher = new RouteMatcher();
        def vertx = new Vertx(vertx)

        matcher.get("/product") { HttpServerRequest req ->
            try {
                vertx.eventBus.send('product/get', [id: req.params.id]) { Message reply ->
                    req.response.end(Json.encode(reply.body()))
                }
            } catch (BusinessException e) {
                responseError(req, e.getMessage(), e);
            } catch (Exception e) {
                responseError(req, e);
            }
        }

//        matcher.get("/price"){
//
//        }
        matcher.get("/inventories") { HttpServerRequest req ->
            try {
                vertx.eventBus.send("inventories/get", [ids: req.params.get("ids")?.split(",")]) { Message reply ->
                    req.response.end(Json.encode(reply.body()));
                }
            } catch (BusinessException e) {
                responseError(req, e.getMessage(), e);
            } catch (Exception e) {
                responseError(req, e);
            }
        }

        matcher.get("/time") { HttpServerRequest req ->
            Date now = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            req.response.end(Json.encode([status: "ok", currentTime: dateFormat.format(now)]));
        }

        matcher.post("/order") { HttpServerRequest req ->

            req.bodyHandler { Buffer buffer ->
                String contentType = req.headers.get("Content-Type")
                logger.info(buffer.toString())
                def params = [:]
//                if ("application/x-www-form-urlencoded".equals(contentType)) {
                    def qsd = new QueryStringDecoder(buffer.toString(), false)
                    params = qsd.parameters().collectEntries {
                        if (it.value.size() <= 1) {
                            ["${it.key}": it.value.get(0)]
                        }else{
                            ["${it.key}": it.value]
                        }
                    }
//                }
                vertx.eventBus.send("order/post", params) { Message reply ->
                    req.response.end(Json.encode(reply.body()))
                }
            }
//            req.expectMultiPart=true
//            req.endHandler() {
//                MultiMap multiMap = req.formAttributes;
//
//                def map = multiMap.entries.groupBy {
//                    it.key
//                }
//
//                logger.info(map)
//
//                vertx.eventBus.send("order/post", map) { Message reply ->
//                    req.response.end(Json.encode(reply.body()))
//                }
//            }
        }


        matcher.jRM.noMatch(staticHandler());
        return matcher.jRM;
    }
}
