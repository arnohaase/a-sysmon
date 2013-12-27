var aSysMonApp = angular.module('ASysMonApp', []);

aSysMonApp.controller('ASysMonCtrl', function($scope, $http, $log) {
    //TODO display committed and maximum memory
    //TODO get 'current' memory consumption in addition to GC requests
    //TODO tool tip: left or right, depending on x coordinate

    $scope.safeApply = function(fn) {
        var phase = this.$root.$$phase;
        if(phase == '$apply' || phase == '$digest') {
            if(fn && (typeof(fn) === 'function')) {
                fn();
            }
        } else {
            this.$apply(fn);
        }
    };

    $scope.showFullGcMarkers = true;
    $scope.$watch('showFullGcMarkers', function() {
        if($scope.gcs) {
            $scope.dataAsMap['_full_'].points.show = $scope.showFullGcMarkers;
            extractGcMarkings($scope.gcs);
            doPlot();
        }
    });

    $scope.showOtherGcMarkers = false;
    $scope.$watch('showOtherGcMarkers', function() {
        if($scope.gcs) {
            extractGcMarkings($scope.gcs);
            doPlot();
        }
    });

    $scope.showLegend = true;
    $scope.$watch('showLegend', function() {
        if($scope.gcs) {
            doPlot();
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
                $scope.gcMarkings.push({color: 'rgba(50,50,150,.7)', lineWidth: 1, xaxis: {from: gc.startMillis, to: endMillis(gc)}});
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

            if(data.gcs && data.gcs.length) {
                $scope.xMinData = data.gcs[0].startMillis;
            }
            for(var i=0; i<data.gcs.length; i++) {
                var gc = data.gcs[i];

                $log.log(angular.toJson(gc.mem));

                for(var memKind in gc.mem) {
                    if(!gc.mem.hasOwnProperty(memKind)) continue;
                    if(! result[memKind]) {
                        result[memKind] = {
                            label: memKind,
                            hoverable: false,
                            data: []
                        };
                    }

                    var end = endMillis(gc);
                    $scope.xMaxData = end;
                    result[memKind].data.push([gc.startMillis, gc.mem[memKind].usedBefore / 1024 / 1024]);
                    result[memKind].data.push([end, gc.mem[memKind].usedAfter / 1024 / 1024]);
                }
                if(!result['_full_']) {
                    result['_full_'] = {
                        label: '_full_',
                        data: [],
                        lines: {show: false},
                        color: 'rgb(0,0,100)',
                        points: {show: true, radius: 7, fill: true, fillColor: 'rgb(100, 100, 200)'}
                    }
                }
                if(isFullGc(gc)) {
                    result['_full_'].data.push([gc.startMillis, 0]);
                }
            }
            $scope.xMinDisplay = $scope.xMinData;
            $scope.xMaxDisplay = $scope.xMaxData;

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
            $scope.dataAsMap = extractDataAsMap();
            var memKinds = sortedMemKinds($scope.dataAsMap);

            $scope.dataSets = [];
            for(var i=0; i<memKinds.length; i++) {
                $scope.dataSets.push($scope.dataAsMap[memKinds[i]]);
            }

            extractGcMarkings(data.gcs);
        }
    }

    var memKindSortOrder = {
        'PS Perm Gen': 1,
        'PS Old Gen': 2,
        'PS Survivor Space': 3,
        'PS Eden Space': 4,
        '_full_': 10
    };

    function sendCommand(cmd) {
        $http.get('_$_asysmon_$_/rest/' + cmd).success(function(data) {
            initFromResponse(data);
            doPlot();
        });
    }

    $scope.refresh = function() {
        sendCommand('getData');
    };

    $scope.refresh();

    var plot;
    function doPlot() {
        plot = $.plot(
            '#mem-gc-placeholder',
            $scope.dataSets,
            {
                legend: {
                    show: $scope.showLegend,
                    position: 'ne',
                    backgroundOpacity: .7,
                    labelFormatter: function(label) {return label.charAt(0) === '_' ? null : label;}
                },
                xaxis: {
                    mode: 'time',
                    timezone: 'browser'
                },
                yaxis: {
                    axisLabel: 'Memory Size (MB)',
                    zoomRange: false, // flot.navigate
                    panRange: false, // flot.navigate
                    transform: function(v) {return v;}
                },
                series: {stack: true},
//                colors: [0, 1, 2, 3, 4, 5, 6],
                lines: {
                    lineWidth: 1,
                    fill: true
                },
                grid: {
                    hoverable: true,
                    mouseActiveRadius: 15,
//                    backgroundColor: { colors: ["#D1D1D1", "#7A7A7A"] },
                    markings: $scope.gcMarkings
                },
                zoom: { interactive: true }, // flot.navigate
                pan: { interactive: true } // flot.navigate
            }
        );
    }


    function presentationZoomPercents() {
        var overPan = 0;
        if($scope.xMinDisplay < $scope.xMinData) {
            // positive value for left overpan
            overPan = $scope.xMinData - $scope.xMinDisplay;
        }
        if($scope.xMaxDisplay > $scope.xMaxData) {
            // negative value for right overpan
            overPan = $scope.xMaxDisplay - $scope.xMaxData;
        }

        var offsetAbs = ($scope.xMinDisplay - $scope.xMinData);
        if(overPan < 0) {
            offsetAbs -= overPan;
        }
        var visibleAbs = $scope.xMaxDisplay - $scope.xMinDisplay - Math.abs(overPan);

        var dataRange = $scope.xMaxData - $scope.xMinData;
        return {
            percentOffset: offsetAbs  / dataRange * 100,
            percentVisible: visibleAbs  / dataRange * 100
        };
    }

    $scope.getZoomMinPercent = function() {
        return presentationZoomPercents().percentOffset;
    };
    $scope.getZoomVisiblePercent = function() {
        return presentationZoomPercents().percentVisible;
    };

    $scope.zoomIn = function() {
        plot.zoom();
    };
    $scope.zoomOut = function() {
        plot.zoomOut();
    };
    $scope.zoomReset = function() {
        plot.getAxes().xaxis.min = $scope.xMinData;
        plot.getAxes().xaxis.max = $scope.xMaxData;
        $scope.xMinDisplay = $scope.xMinData;
        $scope.xMaxDisplay = $scope.xMaxData;
        doPlot();
    };

    function refreshZoom(evt, plot) {
        $scope.safeApply(function() {
            var xaxis = plot.getAxes().xaxis;
            $scope.xMinDisplay = xaxis.min;
            $scope.xMaxDisplay = xaxis.max;
        });
    }

    //TODO limit zoom, limit pan
    $('#mem-gc-placeholder')
        .bind('plotzoom', refreshZoom)
        .bind('plotpan', refreshZoom);

    (function() {
        var previousPoint = null;

        function tooltipFor(item) {
            for(var i=0; i<$scope.gcs.length; i++) {
                var gc = $scope.gcs[i];
                if(gc.startMillis !== item.datapoint[0]) { //TODO is there a better way to do this?
                    continue;
                }
                //TODO layout of tool tip
                //TODO relative change of used memory per memgc kind
                //TODO *committed* memory (+ info if changed)
                return 'cause: ' + gc.cause + '<br>' +
                    'type: ' + gc.type + '<br>' +
                    'algorithm: ' + gc.algorithm + '<br>' +
                    'duration: ' + gc.durationNanos + 'ns';
            }
            return '';
        }

        $('#mem-gc-placeholder').bind("plothover", function (event, pos, item) {
            if (item) {
                if (previousPoint != item.dataIndex) {
                    previousPoint = item.dataIndex;

                    $("#tooltip").remove();

                    var x = item.datapoint[0];
                    var y = item.datapoint[1];

//                    var text = 'moin moin: ' + item.series.label;

                    showTooltip(item.pageX, item.pageY, tooltipFor(item));
//                        months[x-  1] + "<br/>" + "<strong>" + y + "</strong> (" + item.series.label + ")");
                }
            }
            else {
                $("#tooltip").remove();
                previousPoint = null;
            }
        });

        //TODO nicer styling, move styling to CSS file
        function showTooltip(x, y, contents) {
            $('<div id="tooltip">' + contents + '</div>').css({
                position: 'absolute',
                display: 'none',
                top: y + 5,
                left: x + 20,
                border: '2px solid #4572A7',
                padding: '2px',
                size: '10',
                'background-color': '#fff',
                opacity: 0.90
            }).appendTo("body").fadeIn(200);
        }
    }());
});

