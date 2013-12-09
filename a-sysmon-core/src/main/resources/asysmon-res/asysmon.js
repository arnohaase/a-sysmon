var aSysMonApp = angular.module('ASysMonApp', []);

aSysMonApp.controller('ASysMonCtrl', function($scope, $http, $log) {
    function initFromResponse(data) {
        $scope.title = data.title;
        $scope.isStarted = data.isStarted;
        $scope.scalars = data.scalars;
        $scope.columnDefs = data.columnDefs;
        $scope.traces = data.traces;

        initTraceNodes($scope.traces, 0);

        $scope.totalDataWidth = 0;
        for(var i=0; i<data.columnDefs.length; i++) {
            var cw = data.columnDefs[i].width.toLowerCase();
            if(cw === 'short') {
                $scope.totalDataWidth += 40;
            }
            if(cw === 'medium') {
                $scope.totalDataWidth += 60;
            }
            if(cw === 'long') {
                $scope.totalDataWidth += 80;
            }
        }
    }

    function initTraceNodes(nodes, level) {
        if(nodes) {
            for(var i=0; i<nodes.length; i++) {
                nodes[i].level = level;
                initTraceNodes(nodes[i].children, level+1);
            }
        }
    }

    function sendCommand(cmd) {
        $http.get(cmd).success(function(data) {
            initFromResponse(data);
        });
    }

    $scope.refresh = function() {
        sendCommand('getData');
    };
    $scope.clear = function() {
        sendCommand('doClear');
    };
    $scope.start = function() {
        sendCommand('doStart');
    };
    $scope.stop = function() {
        sendCommand('doStop');
    };

    $scope.refresh();


    $scope.formattedScalar = function(name, factor) {
        factor = factor || 1;

        var s = $scope.scalars && $scope.scalars[name];
        return s ? (s.value*factor).toFixed(s.numFracDigits) : '';
    };
    $scope.hasLoad = function() {
        return $scope.scalars && $scope.scalars['load-1-minute'];
    };
    $scope.load1 = function() {
        return $scope.formattedScalar('load-1-minute');
    };
    $scope.load5 = function() {
        return $scope.formattedScalar('load-5-minutes');
    };
    $scope.load15 = function() {
        return $scope.formattedScalar('load-15-minutes');
    };

    $scope.hasMem = function() {
        return $scope.scalars && $scope.scalars['mem-used'];
    };
    $scope.memUsed = function() {
        return $scope.formattedScalar('mem-used', 1/1024/1024) + 'M';
    };
    $scope.memTotal = function() {
        return $scope.formattedScalar('mem-total', 1/1024/1024) + 'M';
    };
    $scope.memMax = function() {
        return $scope.formattedScalar('mem-max', 1/1024/1024) + 'M';
    };

    $scope.genericScalarNames = function() {
        var result = [];
        if($scope.scalars) {
            $.each($scope.scalars, function(n) {
                if(n.startsWith('load-')) {
                    return;
                }
                if(n.startsWith('mem-')) {
                    return;
                }
                result.push(n);
            });
        }
        return result;
    }


    $scope.colClass = function(idx) {
        return 'column-' + $scope.columnDefs[idx].width.toLowerCase();
    };
    $scope.formatNumber = function(num, numFracDigits) { //TODO make this a filter?
        return num.toFixed(numFracDigits);
    };

    $scope.toggleTreeNode = function(event, node) {
        var clicked = $(event.target);
        var dataRow = clicked.parents('.data-row');
        var childrenDiv = dataRow.next();
        childrenDiv.slideToggle(50);
    };
});

