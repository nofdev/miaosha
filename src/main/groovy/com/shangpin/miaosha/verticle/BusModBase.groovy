package com.shangpin.miaosha.verticle

import org.vertx.groovy.core.eventbus.EventBus
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.json.JsonObject

/**
 * Created by Qiang on 3/6/14.
 */
class BusModBase extends Verticle {
    def eventBus
    def config
    def logger

    def start() {
        eventBus = vertx.eventBus
        config = container.config
        logger = container.logger
    }

    def sendStatus(String status, Message message) {
        sendStatus(status, message, null)
    }

    def sendStatus(String status, Message message, Map map) {
        if (map == null) {
            map = [:]
        }
        map.put("status", status)
        message.reply(map);
    }

    def sendOK(Message message) {
        sendOK(message, null)
    }

    def sendOK(Message message, Map map) {
        sendStatus("ok", message, map)
    }

    def sendError(Message message, String error, Boolean isLogger = true) {
        sendError(message, error, null, isLogger)
    }

    def sendError(Message message, String error, Exception e, Boolean isLogger = true) {
        if (isLogger) {
            logger.error(error, e)
        }
        message.reply([status: "error", message: error])
    }
}
