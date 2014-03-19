package com.shangpin.miaosha.verticle

import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.json.JsonObject

/**
 * Created by Qiang on 3/2/14.
 */
class ItemVerticle extends BusModBase {

    def start() {
        super.start()

        /**
         * 获取商品信息
         * 不包括库存
         */
        eventBus.registerHandler("getItem"){Message message->
            logger.debug("getItem request message is "+message.body())

            def id = message.body().id
            if(!id){
                sendError(message,"id must be specified")
                return
            }

            def matcher = [_id: message.body().get("id")]
            def findOps = [collection: "items", action: "findone", matcher: matcher]

            eventBus.send("mongodb-persistor", findOps) { Message result ->
                message.reply(result.body)
            }
        }
        /**
         * 获取库存
         * {"results":[{"_id":"1001","quantity":200},{"_id":"1002","quantity":200}],"status":"ok","number":2}
         */
        eventBus.registerHandler("findAllQuantitiesBySkuIds"){Message message->
            logger.debug("findAllQuantitiesBySkuIds request message is "+message.body())

            def ids = message.body().get("ids")
            if(!ids){
                sendError(message,"ids must be specified")
                return
            }

            def matcher = [_id:[$in:ids]]
            def findOps = [collection: "inventories", action: "find", matcher: matcher]
            eventBus.send("mongodb-persistor", findOps) { Message result ->
                message.reply(result.body)
            }
        }
    }
}
