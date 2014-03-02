package com.shangpin.miaosha.server

import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle
import org.vertx.java.busmods.BusModBase
import org.vertx.java.core.eventbus.Message
import org.vertx.java.core.json.JsonObject

/**
 * Created by Qiang on 2/27/14.
 */
class App extends Verticle {

    def start() {
        def appConfig = container.config
        def logger = container.logger

        logger.info("appConfig is" + appConfig)
//
        container.with {
            deployModule("io.vertx~mod-mongo-persistor~2.1.0", appConfig.get("mongo-persistor")) {
                deployVerticle("groovy:com.shangpin.miaosha.persistor.MockDataInitializer")
            }
//            deployModule("io.vertx~mod-web-server~2.0.0-final",appConfig.get("web-server"))
            deployVerticle("com.shangpin.miaosha.server.MyWebServer", appConfig.get("web-server"))
//            deployModule("io.vertx~mod-auth-mgr~2.0.0-final",appConfig.get("auth-mgr"))
            deployVerticle("groovy:com.shangpin.miaosha.verticle.ProductVerticle")
        }

//        def routeMatcher = new RouteMatcher()
//
//        routeMatcher.get("/") { HttpServerRequest req ->
//            req.response.sendFile("static/index.html")
//        }
//
//        routeMatcher.get("/test"){ HttpServerRequest req ->
//            def json = ["action":"save"]
//        }
//
//        routeMatcher.get("/customers") { HttpServerRequest req ->
//            def matcher = new JsonObject(["name": "mengqiang"])
//            def json = new JsonObject(["collection": "customers", "action": "find", "matcher": matcher])
//            vertx.eventBus.send("mongodb-persistor", json) { Message<JsonObject> message ->
//                req.response().end(message.body().encodePrettily())
//            }
//        }
//
//
//        server.requestHandler(routeMatcher.asClosure())
//
//        server.listen(8888)

    }
}
