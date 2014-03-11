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
            logger.debug("Trade post request message is " + message.body())

            def params = Json.decodeValue(message.body().params, Map.class)
            def cartItems = params.cartItems
            def skuIds = cartItems.collect {
                it.skuId
            }

            eventBus.send("findAllQuantitiesBySkuIds", skuIds) { Message result ->
                if ("error".equals(result.body().status)) {
                    sendError(message, result.body().message)
                    return
                } else {
                    def results = result.body().results
                    if (skuIds.size() != results.size()) {
                        sendError(message, "Invalid skuIds", false)
                        return
                    } else {
                        def cartItemMap = cartItems.collectEntries {
                            [(it.skuId): it.quantity]
                        }
                        def resultMap = results.collectEntries {
                            [(it._id): it.quantity]
                        }
                        def soldOutSkuIds = cartItemMap.collectMany {
                            it.value > resultMap[it.key] ? [it.key] : [it.key]
                        }
                        if (soldOutSkuIds.size > 0) {
                            sendError(message, "${soldOutSkuIds} have sold out")
                            return
                        } else {
                            cartItemMap.each {
                                def criteria = [_id: it.skuId,$atomic: true]
                                def objNew = [$inc: [quantity: (0-it.quantity)]]
                                def updateOps = [action: "update", collection: "inventories", criteria: criteria, objNew: objNew, upsert: false, multi: false]
                                eventBus.send("mongodb-persistor", updateOps)
                            }
                            //TODO 连接下单服务（同步）
                            //TODO 连接更新库存服务（异步）
                            sendOK(message)
                        }
                    }
                }
            }

//            def matcher = [_id: message.body().get("skuId")]
//            def findOps = [collection: "inventories", action: "findone", matcher: matcher]

//            eventBus.send("mongodb-persistor", findOps) { Message result ->
//                message.reply(result.body)
//            }
        }
    }
}
