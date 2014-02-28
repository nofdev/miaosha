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

//    def appConfig = container.config
//    def logger = container.logger
//    def server = vertx.createHttpServer()

    def start() {
        def appConfig = container.config
        def logger = container.logger
        def server = vertx.createHttpServer()
//
        container.with {
            deployModule("io.vertx~mod-mongo-persistor~2.1.0", appConfig.get("mongo-persistor"))
//            deployModule("io.vertx~mod-web-server~2.0.0-final")
        }

        def routeMatcher = new RouteMatcher()

        routeMatcher.get("/") { HttpServerRequest req ->
            req.response.sendFile("static/index.html")
        }

        routeMatcher.get("/test"){ HttpServerRequest req ->
            def userDocument = ["username":"mengqiang","password":"123456"]
            def json = ["action":"save","collection": "users","document":userDocument]
            vertx.eventBus.send("mongodb-persistor", json){Message<JsonObject> message ->
                req.response().end(message.body().encodePrettily())
            }
        }

        routeMatcher.get("/users") { HttpServerRequest req ->
            def matcher = new JsonObject(["name": "mengqiang"])
            def json = new JsonObject(["collection": "users", "action": "find", "matcher": matcher])
            vertx.eventBus.send("mongodb-persistor", json) { Message<JsonObject> message ->
                req.response().end(message.body().encodePrettily())
            }
        }


        server.requestHandler(routeMatcher.asClosure())

        server.listen(8888)

    }
}
