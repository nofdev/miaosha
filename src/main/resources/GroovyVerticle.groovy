vertx.eventBus.registerHandler("ping-address") { message ->
    message.reply("pong!")
    container.logger.info("Sent back pong groovy!")
}
//container.with {
//    deployModule("io.vertx~mod-mongo-persistor~2.1.0", container.config.get("mongo-persistor")) {
//        deployVerticle("groovy:com.shangpin.miaosha.persistor.MockDataInitializer")
//    }
////            deployModule("io.vertx~mod-web-server~2.0.0-final",appConfig.get("web-server"))
//    deployVerticle("com.shangpin.miaosha.server.MyWebServer", container.config.get("web-server"))
////            deployModule("io.vertx~mod-auth-mgr~2.0.0-final",appConfig.get("auth-mgr"))
//    deployVerticle("groovy:com.shangpin.miaosha.verticle.ItemVerticle")
//}