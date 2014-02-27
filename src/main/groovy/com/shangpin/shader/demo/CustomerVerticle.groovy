package com.shangpin.shader.demo

import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.eventbus.Message
import org.vertx.java.core.json.JsonObject

/**
 * Created by Qiang on 2/26/14.
 */
class CustomerVerticle extends Verticle{

    def start(){
        def appConfig = container.config


        container.deployModule("io.vertex~mod-mongo-persistor~2.1.0",appConfig.get("mongo-persistor"))

        def routeMatcher = new RouteMatcher()

        routeMatcher.get("/"){HttpServerRequest req->
            req.response.sendFile("index.html")
        }

        routeMatcher.get("/customers"){HttpServerRequest req->
            def matcher = new JsonObject(["name":"mengqiang"])
            def json = new JsonObject(["collection":"customers","action":"find","matcher":matcher])
            vertx.eventBus.send("mongodb-persistor",json){Message<JsonObject> message->
                req.response().end(message.body().encodePrettily())
            }
        }

        def server = vertx.createHttpServer()
        server.requestHandler(routeMatcher.asClosure())

        server.listen(8888)

        vertx.createHttpServer().requestHandler(){HttpServerRequest req->
                req.response.end("Hello word")
            }.listen(8888)
        container.logger.info("Webserver started,listening on port 8888")
    }
}
