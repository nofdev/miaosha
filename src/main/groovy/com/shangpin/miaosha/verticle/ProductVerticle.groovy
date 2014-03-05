package com.shangpin.miaosha.verticle

import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.json.JsonObject

/**
 * Created by Qiang on 3/2/14.
 */
class ProductVerticle extends Verticle {

    def start() {
        vertx.eventBus.registerHandler("product/get"){Message message->
            container.logger.info("Product get request message is "+message.body())

            def matcher = [_id: message.body().get("id")]
            def findOps = [collection: "products", action: "findone", matcher: matcher]
            vertx.eventBus.send("mongodb-persistor", findOps) { Message result ->
                message.reply(result.body)
            }
        }
        vertx.eventBus.registerHandler("inventories/get"){Message message->
            container.logger.info("Inventories get request message is "+message.body())

            def matcher = [_id:[$in:message.body().get("ids")]]
            def findOps = [collection: "inventories", action: "find", matcher: matcher]
            vertx.eventBus.send("mongodb-persistor", findOps) { Message result ->
                message.reply(result.body)
            }
        }
    }
}
