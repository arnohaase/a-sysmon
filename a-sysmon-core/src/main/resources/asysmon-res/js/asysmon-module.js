
(function () {
    var asysmon = angular.module('asysmon', ['ngRoute'], function() {
    });


    function curPage($location) {
        var raw = $location.path().substr(1);
        var idxSlash = raw.indexOf('/');
        if(idxSlash > 0) {
            return raw.substr(0, idxSlash);
        }
        return raw;
    }

    asysmon.service('Rest', ['$log', '$http', '$location', function($log, $http, $location) {
        this.call = function(service, success) {
            $http
                .get('_$_asysmon_$_/rest/' + curPage($location) + '/' + service)
                .success(success); //TODO error handling
        };
    }]);
}());





