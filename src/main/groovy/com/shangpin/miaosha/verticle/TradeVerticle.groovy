package com.shangpin.miaosha.verticle

import com.darylteo.vertx.promises.groovy.Promise
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

            def params = message.body()
            def cartItems = params.cartItems
            def skuIds = cartItems.collect {
                it.skuId
            }

//            def promise = Promise.defer()
//            promise.fulfill(message)
//            promise.then{
//                eventBus.send("findAllQuantitiesBySkuIds", [ids: skuIds]) { Message result ->
//                    if ("error".equals(result.body().status)) {
//                        promise.reject(result.body().message)
//                    }else{
//                        def results = result.body().results
//                        if (skuIds.size() != results.size()) {
//                            promise.reject("Invalid skuIds")
//                        }else{
//                            def cartItemMap = cartItems.collectEntries {
//                                [(it.skuId): it.quantity]
//                            }
//                            logger.debug(cartItemMap)
//                            def resultMap = results.collectEntries {
//                                [(it._id): it.quantity]
//                            }
//                            logger.debug(resultMap)
//                            def soldOutSkuIds = []//TODO 可以用collectMany{it.value > resultMap[it.key] ? [it.key] : []}解决，但是有运行时错误，可能是groovy-all的bug
//                            cartItemMap.each {
//                                if (it.value > resultMap["1001"]) soldOutSkuIds.add(it.key)
//                            }
//                            logger.debug(soldOutSkuIds)
//                            if (soldOutSkuIds.size > 0) {
//                                promise.reject("${soldOutSkuIds} have sold out")//售罄
//                            }else {
//                                //TODO promises
//                                cartItemMap.each {
//                                    def criteria = [_id: it.key, $atomic: true]
//                                    def objNew = [$inc: [quantity: (0 - it.value)]]
//                                    def updateOps = [action: "update", collection: "inventories", criteria: criteria, objNew: objNew, upsert: false, multi: false]
//                                    eventBus.send("mongodb-persistor", updateOps)
//                                }
//                                //TODO 连接下单服务（同步）
//                                eventBus.send("confirmOrder",[:]){Message reply
//
//                                }
////                            eventBus.send("syncCreateOrder",[:]){Message result2 ->
////                                if(result2.body().status=="ok"){
////                                    sendOK(message)
////                                }
////                            }
//                                //TODO 连接更新库存服务（异步）
////                            sendOK(message)
//                            }
//                        }
//                    }
//                }
//            }.then({sendOK(message)},{sendError(message,promise.reason.message)})

            //先查流量库存，没有返回售罄
            eventBus.send("findAllQuantitiesBySkuIds", [ids: skuIds]) { Message result ->
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
                        logger.debug(cartItemMap)
                        def resultMap = results.collectEntries {
                            [(it._id): it.quantity]
                        }
                        logger.debug(resultMap)
                        def soldOutSkuIds = []//TODO 可以用collectMany{it.value > resultMap[it.key] ? [it.key] : []}解决，但是有运行时错误，可能是groovy-all的bug
                        cartItemMap.each {
                            if (it.value > resultMap["1001"]) soldOutSkuIds.add(it.key)
                        }
                        logger.debug(soldOutSkuIds)
                        if (soldOutSkuIds.size > 0) {
                            sendError(message, "${soldOutSkuIds} have sold out")//售罄
                            return
                        } else {
                            //TODO promises
                            cartItemMap.each {
                                def criteria = [_id: it.key, $atomic: true]
                                def objNew = [$inc: [quantity: (0 - it.value)]]
                                def updateOps = [action: "update", collection: "inventories", criteria: criteria, objNew: objNew, upsert: false, multi: false]
                                eventBus.send("mongodb-persistor", updateOps)
                            }
                            //TODO 连接下单服务（同步）
                            eventBus.send("confirmOrder",[:]){Message reply

                            }
//                            eventBus.send("syncCreateOrder",[:]){Message result2 ->
//                                if(result2.body().status=="ok"){
//                                    sendOK(message)
//                                }
//                            }
                            //TODO 连接更新库存服务（异步）
//                            sendOK(message)
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
