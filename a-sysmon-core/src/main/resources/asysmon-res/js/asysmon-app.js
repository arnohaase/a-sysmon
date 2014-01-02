var aSysMonApp = angular.module('ASysMonApp', ['ngRoute']);

aSysMonApp.config(['$routeProvider',
    function($routeProvider) {
        for(var i=0; i<asysmon.config.menuEntries.length; i++) {
            var menuEntry = asysmon.config.menuEntries[i];
            for(var j=0; j<menuEntry.entries.length; j++) {
                var pageDef = menuEntry.entries[j];
                $routeProvider.when('/' + pageDef.id, {templateUrl: '_$_asysmon_$_/static/partials/' + pageDef.htmlFileName, controller: pageDef.controller});
            }
        }

        $routeProvider.otherwise({ redirectTo: '/threaddump' }); //TODO get default page into config
    }]);

aSysMonApp.controller('ASysMonCtrl', function($scope) {
    $scope.config = asysmon.config;
});