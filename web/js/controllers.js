'use strict';

/* Controllers */
var tradeApp = angular.module('tradeApp', []);

tradeApp.controller('tradeCtrl', function ($scope, $http) {
    $scope.params = {"cartItems": [
        {"skuId": "1001", "quantity": 1},
        {"skuId": "1002", "quantity": 1}
    ]};
    $scope.submitOrderForm = function () {
        var data = $scope.params;
        $http.post("/trade", data).success(function (data, status, headers, config) {
            $scope.result = data;
        }).error(function (data, status, headers, config) {
            $scope.result = data;
        });
    };
});