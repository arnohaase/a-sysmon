
angular.module('ASysMonApp', ['ngRoute', 'asysmon'], function($routeProvider, configRaw) {
    angular.forEach(configRaw.menuEntries, function(menuEntry) {
        angular.forEach(menuEntry.entries, function(pageDef) {
            $routeProvider.when('/' + pageDef.id, {templateUrl: '_$_asysmon_$_/static/partials/' + pageDef.htmlFileName, controller: pageDef.controller});
        });
    });

    $routeProvider.otherwise({ redirectTo: '/threaddump' }); //TODO get default page into config
});

angular.module('ASysMonApp').controller('ASysMonCtrl', function($scope, $route, $location, config) {
    $scope.configRaw = config.raw();
});
