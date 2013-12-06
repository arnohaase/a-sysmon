var asysmonApp = angular.module('asysmon-app', [
    'ngRoute',
    'asysmonControllers'
]);

asysmonApp.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/start', {
                templateUrl: 'partials/start.html',
                controller: 'StartCtrl'
            }).
//            when('/phones/:phoneId', {
//                templateUrl: 'partials/phone-detail.html',
//                controller: 'PhoneDetailCtrl'
//            }).
            otherwise({
                redirectTo: '/start'
            });
    }]);
