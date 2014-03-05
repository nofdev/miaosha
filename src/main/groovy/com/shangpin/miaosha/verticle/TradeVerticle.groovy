package com.shangpin.miaosha.verticle

import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.json.JsonObject

/**
 * Created by Qiang on 3/4/14.
 */
class TradeVerticle extends Verticle {

    def start() {
        vertx.eventBus.registerHandler("order/post") { Message message ->
            container.logger.info("Order post request message is " + [cart: 'fuck'].cart)


            def matcher = [_id: message.body().get("skuId")]
            def findOps = [collection: "inventories", action: "findone", matcher: matcher]

            vertx.eventBus.send("mongodb-persistor", findOps) { Message result ->
                message.reply(result.body)
            }
        }
    }
}
