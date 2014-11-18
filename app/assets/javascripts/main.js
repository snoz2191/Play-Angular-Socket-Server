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
    $scope.showGraphic = false;
    $scope.searched = false;

    $scope.openFrom = function($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.toOpened = false;
        $scope.fromOpened = true;
    };

    $scope.openTo = function($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.toOpened = true;
        $scope.fromOpened = false;
    };

    $scope.search = function(){
        if ( !$scope.from ) {
            $scope.initDate = new Date(2006,6,15)
        } else {
            $scope.initDate = $scope.from;
        }

        if ( !$scope.to ) {
            $scope.endDate = new Date()
        } else {
            $scope.endDate = $scope.to;
        }
        console.log("Fecha Inicial:" + $scope.initDate);
        console.log("Fecha Final:" + $scope.endDate);
        $scope.datos = [];
        $http.post('/stats', {initial:$scope.initDate,end:$scope.endDate}).
            success(function(data){
               $scope.summary = data;
               translate($scope.summary,$scope.datos);
               if ( $scope.datos.length > 0 ) {
                   crearGrafica($scope.datos);
                   $scope.showGraphic = true;
                   $scope.searched = true;
               } else {
                   $scope.showGraphic = false;
               }

            }).
            error(function(status){
                alert("OH OH!");
            });

        /*Parse from Json to summary data for plotting*/
        function translate (o, a) {
            for (var i in o) {
                if (o[i] !== null && typeof(o[i])=="object") {
                        a.push({"cat":i,
                            "Positivo": o[i]["positive"],
                            "Neutro":o[i]["neutral"],
                            "Negativo":o[i]["negative"]
                        });
                }
            }
        }

        /*Plotting the chart*/
        function crearGrafica (data){
            d3.select("svg").remove();
            var margin = {top: 50, right: 150, bottom: 50, left: 150},
                width = 800 - margin.left - margin.right,
                height = 400 - margin.top - margin.bottom;

            var x0 = d3.scale.ordinal().rangeRoundBands([0, width], .1);
            var x1 = d3.scale.ordinal();
            var y = d3.scale.linear()
                .range([height, 0]);

            var color = d3.scale.ordinal()
                .range(["#3366cc", "#345f76", "#123546"]);

            var xAxis = d3.svg.axis()
                .scale(x0)
                .orient("bottom");
            var yAxis = d3.svg.axis()
                .scale(y)
                .orient("left")
                .tickFormat(d3.format("d"));

            var svg = d3.select("grafica").append("svg")
                .attr("width", width + margin.left + margin.right)
                .attr("height", height + margin.top + margin.bottom)
                .style("font", "11px sans-serif")
                .style("background-color", "#fff")
                .append("g")
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

            var tip = d3.tip()
                .attr('class', 'd3-tip')
                .offset([-10, 0])
                .html(function(d) {
                    return "<strong>Cantidad:</strong> <span style='color:black'>" + d.value + "</span>";
                });

            svg.call(tip);

            var pols = d3.keys(data[0]).filter(function(key) { return key !== "cat"; });

            data.forEach(function(d) {
                d.polaridad = pols.map(function(name) { return {name: name, value: +d[name]}; });
            });

            x0.domain(data.map(function(d) { return d.cat; }));
            x1.domain(pols).rangeRoundBands([0, x0.rangeBand()]);
            y.domain([0, d3.max(data, function(d) { return d3.max(d.polaridad, function(d) { return d.value; }); })]);

            var ejeX = svg.append("g")
                .attr("class", "x axis")
                .style({'stroke': '#000', 'fill': 'none', 'shape-rendering': 'crispEdges'})
                .attr("transform", "translate(0," + height + ")")
                .call(xAxis);
            ejeX.selectAll('text').style({ 'stroke-width': '-0.5'})
                .style("stroke", "none")
                .style("fill", "#000");

            var ejeY = svg.append("g")
                .attr("class", "y axis")
                .style({'stroke': '#000', 'fill': 'none','shape-rendering': 'crispEdges'})
                .call(yAxis);
            ejeY.selectAll('text').style({ 'stroke-width': '-0.5'})
                .style("stroke", "none")
                .style("fill", "#000")
                .append("text")
                .attr("transform", "rotate(-90)")
                .attr("y", 6)
                .attr("dy", ".71em")
                .style("text-anchor", "end")
                .text("tweets");

            var state = svg.selectAll(".cat")
                .data(data)
                .enter().append("g")
                .attr("class", "g")
                .attr("transform", function(d) { return "translate(" + x0(d.cat) + ",0)"; });

            state.selectAll("rect")
                .data(function(d) { return d.polaridad; })
                .enter().append("rect")
                .attr("width", x1.rangeBand())
                .attr("x", function(d) { return x1(d.name); })
                .attr("y", function(d) { return y(d.value); })
                .attr("height", function(d) { return height - y(d.value); })
                .on('mouseover', tip.show)
                .on('mouseout', tip.hide)
                .style("fill", function(d) { return color(d.name); });

            //Leyenda
            var legend = svg.selectAll(".legend")
                .data(pols.slice().reverse())
                .enter().append("g")
                .attr("class", "legend")
                .attr("transform", function(d, i) { return "translate(50," + i * 20 + ")"; });
            legend.append("rect")
                .attr("x", width - 18)
                .attr("width", 18)
                .attr("height", 18)
                .style("fill", color);
            legend.append("text")
                .attr("x", width - 24)
                .attr("y", 9)
                .attr("dy", ".35em")
                .style("text-anchor", "end")
                .text(function(d) { return d; });

            //Titulo
            var titulo = d3.select("svg").append("g")
                .attr("id", "titulo")
                .attr("class", "titulo")
                .attr("height", 100)
                .attr("width", 100)
                .attr('transform', 'translate(155,20)')
                .append("text")
                .attr("x", 0)
                .attr("y", -1)
                .style("font-family","Arial")
                .style("font-size", "13px")
                .style("fill", "#000")
                .style("font-weight", "bold")
                .text("Sentimiento por categoría.");
        }

        $scope.exportar = function() {

            var html = d3.select("svg")
                .attr("version", 1.1)
                .attr("xmlns", "http://www.w3.org/2000/svg")
                .node().parentNode.innerHTML;
            var imgsrc = 'data:image/svg+xml;base64,'+ btoa(unescape(encodeURIComponent(html)));
            var canvas = document.querySelector("canvas");
            var context = canvas.getContext("2d");
            var image = new Image;
            image.src = imgsrc;
            image.onload = function() {
                context.drawImage(image, 0, 0);
                var canvasdata = canvas.toDataURL("image/png");
                var a = document.createElement("a");
                a.download = "Grafica.png";
                a.href = canvasdata;
                a.click();
            };

        }
    }
}]);