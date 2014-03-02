package com.shangpin.miaosha.persistor

import org.vertx.groovy.platform.Verticle

/**
 * Created by Qiang on 3/1/14.
 */
class MockDataInitializer extends Verticle {
    def start() {
        vertx.eventBus.send("mongodb-persistor", buildUser())
    }

    private buildUser() {
        def skus = [
                ["id": "1001", "attributes": ["color": "red", "size": "37"], "inventory": 200],
                ["id": "1002", "attributes": ["color": "red", "size": "38"], "inventory": 200],
                ["id": "1003", "attributes": ["color": "red", "size": "39"], "inventory": 200],
        ]
        def product = ["id": "1000", "name": "春哥鞋", "price": 2000, "skus": skus]

        def saveOps = ["action": "save", "collection": "products", "document": product]

    }
}
