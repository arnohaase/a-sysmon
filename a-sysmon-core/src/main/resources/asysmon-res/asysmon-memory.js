var aSysMonApp = angular.module('ASysMonApp', []);

aSysMonApp.controller('ASysMonCtrl', function($scope, $http, $log) {
    //TODO zoom in / out on the time axis
    //TODO display committed and maximum memory

    $scope.showFullGcMarkers = true;
    $scope.$watch('showFullGcMarkers', function() {
        if($scope.gcs) {
            extractGcMarkings($scope.gcs);
            plot();
        }
    });

    $scope.showOtherGcMarkers = false;
    $scope.$watch('showOtherGcMarkers', function() {
        if($scope.gcs) {
            extractGcMarkings($scope.gcs);
            plot();
        }
    });

    function endMillis(gc) {
        return gc.durationNanos < 1000*1000 ? gc.startMillis : (gc.startMillis + gc.durationNanos / 1000 / 1000);
    }

    function isFullGc(gc) {
        return gc.type.indexOf(' major ') !== -1;
    }

    function extractGcMarkings(gcs) {
        $scope.gcMarkings = [];

        for(var i=0; i<gcs.length; i++) {
            var gc = gcs[i];
            if(isFullGc(gc) && $scope.showFullGcMarkers) {
                $scope.gcMarkings.push({color: 'rgba(0,0,100,.8)', lineWidth: 1, xaxis: {from: gc.startMillis, to: endMillis(gc)}});
            }
            if(! isFullGc(gc) && $scope.showOtherGcMarkers) {
                $scope.gcMarkings.push({color: 'rgba(100,200,100,.7)', lineWidth: 1, xaxis: {from: gc.startMillis, to: endMillis(gc)}});
            }
        }
    }

    function initFromResponse(data) {
        $scope.gcs = data.gcs;

        $scope.startTime = undefined;
        if(data.gcs && data.gcs.length) {
            $scope.startTime = new Date(data.gcs[0].startMillis);
        }
        $scope.endTime = new Date();
        $scope.dataSets = [];
        $scope.gcMarkings = [];

        function extractDataAsMap () {
            var result = {};

            for(var i=0; i<data.gcs.length; i++) {
                var gc = data.gcs[i];

                for(var memKind in gc.mem) {
                    if(!gc.mem.hasOwnProperty(memKind)) continue;
                    if(! result[memKind]) {
                        result[memKind] = {
                            label: memKind,
                            data: []
                        };
                    }
                    result[memKind].data.push([gc.startMillis, gc.mem[memKind].usedBefore / 1024 / 1024]);
                    result[memKind].data.push([gc.startMillis + (gc.durationNanos / 1000000), gc.mem[memKind].usedAfter / 1024 / 1024]);
                }
            }

            return result;
        }

        function sortedMemKinds(dataAsMap) {
            var result = [];
            for(var memKind in dataAsMap) {
                if(!dataAsMap.hasOwnProperty(memKind)) continue;
                result.push(memKind);
            }
            result.sort(function(a,b) {
                var aOrder = memKindSortOrder[a];
                var bOrder = memKindSortOrder[b];

                if(aOrder && bOrder) return aOrder - bOrder;
                if(!aOrder) return 1;
                if(!bOrder) return -1;
                return a < b ? -1 : 1;
            });

            return result;
        }

        if(data.gcs && data.gcs.length) {
            var dataAsMap = extractDataAsMap();
            var memKinds = sortedMemKinds(dataAsMap);

            $scope.dataSets = [];
            for(var i=0; i<memKinds.length; i++) {
                $scope.dataSets.push(dataAsMap[memKinds[i]]);
            }

            extractGcMarkings(data.gcs);
        }
    }

    var memKindSortOrder = {
        'PS Perm Gen': 1,
        'PS Old Gen': 2,
        'PS Survivor Space': 3,
        'PS Eden Space': 4
    };

    function sendCommand(cmd) {
        $http.get(cmd).success(function(data) {
            initFromResponse(data);
            plot();
        });
    }

    $scope.refresh = function() {
        sendCommand('getData');
    };

    $scope.refresh();

//    $scope.plot = $scope.plotDummy;
    function plot() {
        $.plot(
            '#mem-gc-placeholder',
            $scope.dataSets,
            {
                legend: {position: 'ne', backgroundOpacity:.7},
                xaxis: {mode: 'time'},
                yaxis: {
                    axisLabel: 'Memory Size (MB)',
                    transform: function(v) {return v;}
                },
                series: {stack: true},
                lines: {fill: true},
                grid: {markings: $scope.gcMarkings}
//                grid: {markings: [{color: 'red', lineWidth: 1, xaxis: {from: 1387556623973, to: 1387556623973 }}]}
            }
        );
    }

    function plotDummy() {
        var d1 = [];
        for (var i = 0; i < 14; i += 0.5) {
            d1.push([i*10000000, Math.sin(i)]);
        }

        var d2 = [[0, 3], [40000000, 8], [80000000, 5], [90000000, 13], [13.5*10000000, 14]];

        // A null signifies separate line segments

        var d3 = [[0, 12], [70000000, 12], null, [70000000, 2.5], [120000000, 2.5]];

        $.plot("#mem-gc-placeholder",
            [
                {label: "my sin()", data: d1, points: {show: true}, lines: {show: true}},
                {label: "asd ajsdflkajs dflköadsj flösdaj f", data: d2},
                d3
            ],
            {
                legend: {position: "ne", backgroundOpacity:.7},
                xaxis: {mode: "time"},
                yaxis: {
                    transform: function(v) {return Math.log(v+3);}
                },
                series: {stack: true},
                lines: {fill: true},
                grid: {markings: [{color: "rgba(0,0,100,.8)", lineWidth: 1, xaxis: {from: 40000000, to: 40000000}}]}
            }
        );
    }

//    plotDummy();
});

