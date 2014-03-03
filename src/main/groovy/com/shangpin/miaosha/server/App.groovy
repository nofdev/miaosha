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
        def config = container.config
        def logger = container.logger

        logger.info("config is" + config)
        container.with {
            deployModule("io.vertx~mod-mongo-persistor~2.1.0", config.get("mongo-persistor")) {
                deployVerticle("groovy:com.shangpin.miaosha.persistor.MockDataInitializer")
            }
//            deployModule("io.vertx~mod-auth-mgr~2.0.0-final",appConfig.get("auth-mgr"))
//            deployModule("io.vertx~mod-web-server~2.0.0-final",appConfig.get("web-server"))
            deployVerticle("com.shangpin.miaosha.server.MyWebServer", config.get("web-server"))
            deployVerticle("groovy:com.shangpin.miaosha.verticle.ProductVerticle")
            deployWorkerVerticle("groovy:com.shangpin.miaosha.worker.TestMultiWorker",null,5)
        }
        (0..1000).each {
            vertx.eventBus.send("testMultiWorder",new JsonObject(["aa":"ping"])){Message message ->
                logger.info(message.body())
            }
        }
    }
}
