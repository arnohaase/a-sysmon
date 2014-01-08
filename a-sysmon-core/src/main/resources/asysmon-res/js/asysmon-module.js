
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

    asysmon.service('config', ['$location', 'configRaw', function($location, configRaw) {
        var byPageId = {};
        angular.forEach(configRaw.menuEntries, function(menuEntry) {
            angular.forEach(menuEntry.entries, function(pageDef) {
                byPageId[pageDef.id] = pageDef;
            });
        });

        this.forPage = function(pageId) {
            return byPageId[pageId];
        };
        this.forCurrentPage = function() {
            return this.forPage(curPage($location));
        };
        this.raw = function() {
            return configRaw;
        };
    }]);

    var thousandsSeparator = 1234.5.toLocaleString().charAt(1);
    var decimalSeparator   = 1234.5.toLocaleString().charAt(5);

    asysmon.constant('formatNumber', function(number, numFracDigits) {
        if(!number) {
            return '';
        }
        numFracDigits = numFracDigits || 0;

        var parts = number.toFixed(numFracDigits).toString().split('.');
        parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, thousandsSeparator);
        return parts.join(decimalSeparator);
    });

    var entityMap = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': '&quot;',
        "'": '&#39;',
        "/": '&#x2F;'
    };
    asysmon.constant('escapeHtml', function (string) {
        return String(string || '').replace(/[&<>"'\/]/g, function (s) {
            return entityMap[s];
        });
    });
}());





