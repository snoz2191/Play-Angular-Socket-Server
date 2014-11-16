/*
 * Author: Domingo
 */

var app = angular.module("app", ["ngResource","ngRoute","ui.bootstrap"])
	.constant("apiUrl", "http://localhost:9000\\:9000/api") // to tell AngularJS that 9000 is not a dynamic parameter
	.config(["$routeProvider", function($routeProvider) {
	"use strict";
		return $routeProvider
                    .when("/", {
                        templateUrl: "/assets/templates/main.html",
                        controller: "MainCtrl"
                    })
                    .otherwise({
                        redirectTo: "/"});

	}]);

// the global controller
app.controller("AppCtrl", ["$scope", "$location", function($scope, $location) {
	// the very sweet go function is inherited to all other controllers
	"use strict";
	$scope.go = function (path) {
		$location.path(path);
	};

	$scope.isActive = function (viewLocation) {
        return viewLocation === $location.path();
    };
}]);

app.filter('capitalize', function() {
    return function(input, all) {
        return (!!input) ? input.replace(/([^\W_]+[^\s-]*) */g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();}) : '';
    }
});

// the list controller
app.controller("MainCtrl", ["$scope", "$resource", "apiUrl", "$http", "$timeout", "$rootScope",  function($scope, $resource, apiUrl, $http, $timeout, $rootScope) {
    "use strict";

    $rootScope.voted = true;
    var onTimeout = function() {
        $rootScope.voted = true;
        $scope.getTweet = false;
        console.log("Timeout!")
        $rootScope.tweet.user = "";
        $http.post('/tweets' , $rootScope.tweet).
            success(function(){
                console.log("Update!")
            }).
            error(function(status){
            });
    };

    $scope.openFrom = function($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.fromOpened = true;
    };

    $scope.openTo = function($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.toOpened = true;
    };

    $scope.generate = function(){

        $http.post('/stats', {initial:initDate,end:endDate}).
            success(function(data){
               /*D3 Grafica*/
            }).
            error(function(status){
                alert("OH OH!");
            });
    }
}]);