package com.shangpin.miaosha.persistor

import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.eventbus.Message

/**
 * Created by Qiang on 3/1/14.
 */
class MockDataInitializer extends Verticle {
    def start() {
        itemInit()
        inventoryInit()
    }

    private void itemInit() {
        def skus = [
                ["_id": "1001", "attributes": ["color": "red", "size": "37"]],
                ["_id": "1002", "attributes": ["color": "red", "size": "38"]],
                ["_id": "1003", "attributes": ["color": "red", "size": "39"]],
        ]
        def item = ["_id": "1000", "name": "春哥鞋", "price": 2000, "skus": skus]

        def saveOps = ["action": "save", "collection": "items", "document": item]

        vertx.eventBus.send("mongodb-persistor", saveOps)
    }

    private void inventoryInit(){
        def inventories = [
                ["_id":"1001","quantity": 200],
                ["_id":"1002","quantity": 200],
                ["_id":"1003","quantity": 300],
        ]
        inventories.each {
            def saveOps = ["action":"save","collection": "inventories","document":it]
            vertx.eventBus.send("mongodb-persistor", saveOps)
        }
    }
}
