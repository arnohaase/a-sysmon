var aSysMonApp = angular.module('ASysMonApp', ['ngRoute']);

aSysMonApp.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/ThreadDump', {templateUrl: '_$_asysmon_$_/static/partials/threaddump.html', controller: CtrlThreadDump }).
            otherwise({
                redirectTo: '/ThreadDump'
            });
    }]);

