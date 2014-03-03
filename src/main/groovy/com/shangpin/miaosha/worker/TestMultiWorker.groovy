package com.shangpin.miaosha.worker

import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.eventbus.Message
import org.vertx.java.core.json.JsonObject

/**
 * Created by Qiang on 3/3/14.
 */
class TestMultiWorker extends Verticle {
    def start(){
        vertx.eventBus.registerHandler("testMultiWorder"){Message message->
            def time = Math.abs(new Random().nextInt()%1000)
            sleep(time)
            container.logger.info(this.hashCode())
            message.reply(this.hashCode()+" "+time)
        }
    }
}
