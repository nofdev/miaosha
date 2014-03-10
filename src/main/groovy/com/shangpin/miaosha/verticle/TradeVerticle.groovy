package com.shangpin.miaosha.verticle

import com.fasterxml.jackson.core.type.TypeReference
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.json.JsonObject
import org.vertx.java.core.json.impl.Json

/**
 * Created by Qiang on 3/4/14.
 */
class TradeVerticle extends BusModBase {

    def start() {
        super.start()

        vertx.eventBus.registerHandler("createTrade") { Message message ->
            container.logger.info("Trade post request message is " + message.body())

            def cartItems = Json.decodeValue(message.body().cartItems, List.class)

            def skuIds = cartItems.collect {
                it.skuId
            }

            vertx.eventBus.send("findAllQuantitiesBySkuIds", skuIds) { Message result ->
                if("error".equals(result.body().status)){
                    sendError(result,result.body().message)
                    return
                }
            }

            def matcher = [_id: message.body().get("skuId")]
            def findOps = [collection: "inventories", action: "findone", matcher: matcher]

            vertx.eventBus.send("mongodb-persistor", findOps) { Message result ->
                message.reply(result.body)
            }
        }
    }
}
