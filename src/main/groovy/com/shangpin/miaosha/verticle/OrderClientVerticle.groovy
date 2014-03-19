package com.shangpin.miaosha.verticle

import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.core.http.HttpClientResponse
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.json.impl.Json

/**
 * Created by Qiang on 3/12/14.
 */
class OrderClientVerticle extends BusModBase {
    def start() {
        super.start()

        def httpClient = vertx.createHttpClient([host: config.host, port: config.port])
        httpClient.setKeepAlive(config["keep_alive"])
        httpClient.setMaxPoolSize(config["max_pool_size"])

        vertx.eventBus.registerHandler("confirmOrder") { Message message ->
//            def request = httpClient.post(config.uri) { HttpClientResponse response ->
//
//            }
//            request.putHeader("Content-Type","application/x-www-form-urlencoded;charset=utf-8")
////            request.putHeader("Content-Type","application/json;charset=utf-8")
//            request.write(message.body())
//            request.end()

            httpClient.getNow(config.uri) { HttpClientResponse response ->
                if (response.statusCode == 200) {
                    response.bodyHandler { Buffer buffer ->
                        container.logger.debug(buffer.toString())
                        def result = Json.decodeValue(buffer.toString(), Map.class)
                        if (result.code == 0) {
                            sendOK(message)
                        } else {
                            sendError(message, result.msg, false)
                        }
                    }
                } else {
                    sendError(message, "Request exception, the status code is ${response.statusCode}")
                }
                response.exceptionHandler { Throwable cause ->
                    sendError(message, cause.message, cause)
                }
            }
        }
    }

//    def aa = 10
//    def checkFinished(Message message){
//        if(true){
//            message.reply([status:"ok"])
//        }
//    }
}
//var resp1Complete = false;
//var resp2Complete = false;
//
//var bodyToReturn = null;
//
//resp1.bodyHandler(function(body) {
//    resp1Complete = true;
//    bodyToReturn = body;
//    checkFinished();
//});
//
//resp2.bodyHandler(function(body) {
//    resp2Complete = true;
//    checkFinished();
//});
//
//function checkFinished() {
//    if (resp1Complete && resp2Complete) {
//        {
//            req.response.end(bodyToReturn);
//        }
//    }