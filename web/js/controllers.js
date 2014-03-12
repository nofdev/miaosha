'use strict';

/* Controllers */
var tradeApp = angular.module('tradeApp', []);

tradeApp.controller('tradeCtrl', function ($scope) {
    $scope.params = {"cartItems": [
        {"skuId": "1001", "quantity": 1},
        {"skuId": "1002", "quantity": 1}
    ]};
});