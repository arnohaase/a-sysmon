
function CtrlThreadDump($scope, $http, $log) {
    $('.btn').tooltip({
        container: 'body',
        html: true
    });

    $scope.expansionModel = {}; // bound to the DOM, used for initial rendering
    $scope.shadowExpansionModel = {}; // continually updated, kept separate to allow for jQuery animations

    $scope.hideReflection = true;

    function initFromResponse(data) {
        $scope.title = data.title;
        $scope.threads = data.threads;

        var appPkg = new RegExp(data.appPkg || '.');

        for(var i=0; i<$scope.threads.length; i++) {
            var t = $scope.threads[i];
            var hasApplicationFrame = false;
            for(var j=0; j<t.stacktrace.length; j++) {
                var ste = t.stacktrace[j];
                ste.isReflection = isReflectionSte(ste);
                ste.isAppPkg = appPkg.test(ste.repr);
                hasApplicationFrame = hasApplicationFrame || ste.isAppPkg;
            }
            t.hasApplicationFrame = hasApplicationFrame;
        }

        $scope.expansionModel = angular.copy($scope.shadowExpansionModel);
    }

    function sendCommand(cmd) {
        $http.get('_$_asysmon_$_/rest/' + cmd + '/threaddump').success(function(data) {
            initFromResponse(data);
        });
    }

    $scope.activeThreads = function() {
        var result = [];
        if($scope.threads) {
            for(var i=0; i<$scope.threads.length; i++) {
                if($scope.threads[i].hasApplicationFrame) {
                    result.push($scope.threads[i]);
                }
            }
        }
        return result;
    };
    $scope.nonActiveThreads = function() {
        var result = [];
        if($scope.threads) {
            for(var i=0; i<$scope.threads.length; i++) {
                if(! $scope.threads[i].hasApplicationFrame) {
                    result.push($scope.threads[i]);
                }
            }
        }
        return result;
    };

    $scope.refresh = function() {
        sendCommand('getData');
    };

    $scope.refresh();

    $scope.nodeIconClass = function(thread) {
        if(thread.stacktrace && thread.stacktrace.length) {
            return $scope.shadowExpansionModel[thread.id] ? 'node-icon-expanded' : 'node-icon-collapsed';
        }
        return 'node-icon-empty';
    };

    $scope.expansionStyle = function(thread) {
        return $scope.expansionModel[thread.id] ? 'block' : 'none';
    };

    $scope.toggleTreeNode = function(event, thread) {
        var clicked = $(event.target);

        var dataRow = clicked.filter('.data-row');
        if(dataRow.length === 0) {
            dataRow = clicked.parents('.data-row');
        }
        var childrenDiv = dataRow.next();
        childrenDiv.slideToggle(50, function() {
            $scope.$apply(function() {
                $scope.shadowExpansionModel[thread.id] = !$scope.shadowExpansionModel[thread.id];
            });
        });
    };

    $scope.stacktraceClass = function(ste) {
        if(ste.repr.indexOf('java.') === 0 || ste.repr.indexOf('javax.') === 0) {
            return 'stacktrace-java';
        }
        if(ste.repr.indexOf('sun.') === 0 || ste.repr.indexOf('com.sun') === 0) {
            return 'stacktrace-sun';
        }
        if(ste.isNative) {
            return 'stacktrace-native';
        }
        if(! ste.hasSource) {
            return 'stacktrace-no-source';
        }
        if(ste.isAppPkg) {
            return 'stacktrace-app-pkg';
        }
        return "stacktrace";
    }

    function isReflectionSte(ste) {
        if(ste.repr.indexOf('java.lang.reflect.') === 0) {
            return true;
        }
        if(ste.repr.indexOf('sun.reflect.') === 0) {
            return true;
        }
        if(ste.repr.indexOf('com.sun.proxy') === 0) {
            return true;
        }
        return false;
    }

    $scope.filteredStackTrace = function(thread) {
        var result = [];
        for(var i=0; i<thread.stacktrace.length; i++) {
            var ste = thread.stacktrace[i];
            if($scope.hideReflection && ste.isReflection) {
                continue;
            }
            if($scope.stacktraceWithSourceOnly && !(ste.hasSource || ste.isNative)) {
                continue;
            }
            result.push(ste);
        }
        return result;
    };
}


