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

        /**
         * 下单
         */
        eventBus.registerHandler("createTrade") { Message message ->
            logger.info("Trade post request message is " + message.body())
            def params = Json.decodeValue(message.body().params,Map.class)

            logger.info(params)

            def skuIds = params.cartItems.collect {
                it.skuId
            }

            eventBus.send("findAllQuantitiesBySkuIds", skuIds) { Message result ->
                if("error".equals(result.body().status)){
                    sendError(result,result.body().message)
                    return
                }
            }

//            def matcher = [_id: message.body().get("skuId")]
//            def findOps = [collection: "inventories", action: "findone", matcher: matcher]

            eventBus.send("mongodb-persistor", findOps) { Message result ->
                message.reply(result.body)
            }
        }
    }
}
