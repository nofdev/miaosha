package com.shangpin.miaosha.persistor

import org.vertx.groovy.platform.Verticle

/**
 * Created by Qiang on 3/1/14.
 */
class MockDataInitializer extends Verticle {
    def start() {
        productInit()
        skuInventoryInit()
    }

    private void productInit() {
        def skus = [
                ["_id": "1001", "attributes": ["color": "red", "size": "37"]],
                ["_id": "1002", "attributes": ["color": "red", "size": "38"]],
                ["_id": "1003", "attributes": ["color": "red", "size": "39"]],
        ]
        def product = ["_id": "1000", "name": "春哥鞋", "price": 2000, "skus": skus]

        def saveOps = ["action": "save", "collection": "products", "document": product]

        vertx.eventBus.send("mongodb-persistor", saveOps)
    }

    private void skuInventoryInit(){
        def skuInventories = [
                ["_id":"1001","inventory": 200],
                ["_id":"1002","inventory": 200],
                ["_id":"1003","inventory": 300],
        ]
        skuInventories.each {
            def saveOps = ["action":"save","collection": "inventories","document":it]
            vertx.eventBus.send("mongodb-persistor", saveOps)
        }
    }
}
