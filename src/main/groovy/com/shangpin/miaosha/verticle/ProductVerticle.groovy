package com.shangpin.miaosha.verticle

import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle

/**
 * Created by Qiang on 3/2/14.
 */
class ProductVerticle extends Verticle {

    def start() {
        vertx.eventBus.registerHandler("product"){Message message->
            container.logger.info("Product id is "+message.body())
            def matcher = ["id": message.body()]
            def findOps = ["collection": "products", "action": "findone", "matcher": matcher]
            vertx.eventBus.send("mongodb-persistor", findOps) { Message result ->
                message.reply(result.body)
            }
        }
    }
}
