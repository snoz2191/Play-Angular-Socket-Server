/*
 * Author: Domingo
 */

var app = angular.module("app", ["ngResource","ngRoute"])
	.constant("apiUrl", "http://localhost:9000\\:9000/api") // to tell AngularJS that 9000 is not a dynamic parameter
	.config(["$routeProvider", function($routeProvider) {
	"use strict";
		return $routeProvider
                    .when("/", {
                        templateUrl: "/assets/templates/main.html",
                        controller: "MainCtrl"
                    })
                    .when("/rank", {
                        templateUrl: "/assets/templates/rank.html",
                        controller: "RankCtrl"
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

    $scope.generate = function(){
        $http.get('/tweets/'+ $scope.User).
            success(function(data){
                $rootScope.tweet = data;
                $scope.emptied = false;
                $scope.getTweet = true;
            }).
            error(function(status){
                $scope.emptied = true;
                $scope.getTweet = false;
            });
        $rootScope.voted = false;
        $scope.timer = $timeout(onTimeout, 60000);
    }

	$scope.vote = function( vote ){
        $rootScope.voted = true;
        $scope.polarity = vote;
        if ( vote == "negative") {
            $rootScope.tweet.negvote += 1;
        } else if ( vote == "neutral") {
            $rootScope.tweet.neuvote += 1;
        } else if ( vote == "positive") {
            $rootScope.tweet.posivote += 1;
        }
        $http.post('/tweets' , $rootScope.tweet).
                    success(function(){
                    }).
                    error(function(status){
                    });
        if ($scope.timer) $timeout.cancel($scope.timer);
	}

    $scope.$on("$destroy", function() {
//        if ($scope.voted) {
//
//        } else {
//            console.log("No voto!")
//            $scope.tweet.user = "";
//            $http.post('/tweets' , $scope.tweet).
//                success(function(){
//                    console.log("Update!")
//                }).
//                error(function(status){
//                });
//        }
        if ($scope.timer) $timeout.cancel($scope.timer);
    });
}]);


// the create controller
app.controller("RankCtrl", ["$scope", "$resource", "$timeout", "apiUrl", "$http", "$rootScope", "$window", function($scope, $resource, $timeout, apiUrl, $http, $rootScope, $window) {
    "use strict";
    if ( $rootScope.voted == false && $rootScope.tweet != null ) {
        console.log("No voto!")
        $rootScope.tweet.user = "";
        $http.post('/tweets' ,$rootScope.tweet).
            success(function(){
                //$window.location.reload();
                console.log("Update!")
            }).
            error(function(status){
            }).
            then(function(){
                $http.get('/rank').
                    success(function(data){
                        $scope.users = data
                    }).
                    error(function(status){
                    });
            });
    } else {
        $http.get('/rank').
            success(function(data){
                $scope.users = data
            }).
            error(function(status){
            });
    }


	// to save a celebrity
	$scope.save = function() {
		var CreateCelebrity = $resource(apiUrl + "/celebrities/new"); // a RESTful-capable resource object
		CreateCelebrity.save($scope.celebrity); // $scope.celebrity comes from the detailForm in public/html/detail.html
		$timeout(function() { $scope.go('/'); }); // go back to public/html/main.html
	};

    $scope.orderProp = 'score';
}]);

// the edit controller
app.controller("EditCtrl", ["$scope", "$resource", "$routeParams", "$timeout", "apiUrl", function($scope, $resource, $routeParams, $timeout, apiUrl) {
	"use strict";
	var ShowCelebrity = $resource(apiUrl + "/celebrities/:id", {id:"@id"}); // a RESTful-capable resource object
	if ($routeParams.id) {
		// retrieve the corresponding celebrity from the database
		// $scope.celebrity.id.$oid is now populated so the Delete button will appear in the detailForm in public/html/detail.html
		$scope.celebrity = ShowCelebrity.get({id: $routeParams.id});
		$scope.dbContent = ShowCelebrity.get({id: $routeParams.id}); // this is used in the noChange function
	}
	
	// decide whether to enable or not the button Save in the detailForm in public/html/detail.html 
	$scope.noChange = function() {
		return angular.equals($scope.celebrity, $scope.dbContent);
	};

	// to update a celebrity
	$scope.save = function() {
		var UpdateCelebrity = $resource(apiUrl + "/celebrities/" + $routeParams.id); // a RESTful-capable resource object
		UpdateCelebrity.save($scope.celebrity); // $scope.celebrity comes from the detailForm in public/html/detail.html
		$timeout(function() { $scope.go('/'); }); // go back to public/html/main.html
	};
	
	// to delete a celebrity
	$scope.delete = function() {
		var DeleteCelebrity = $resource(apiUrl + "/celebrities/" + $routeParams.id); // a RESTful-capable resource object
		DeleteCelebrity.delete(); // $scope.celebrity comes from the detailForm in public/html/detail.html
		$timeout(function() { $scope.go('/'); }); // go back to public/html/main.html
	};
}]);