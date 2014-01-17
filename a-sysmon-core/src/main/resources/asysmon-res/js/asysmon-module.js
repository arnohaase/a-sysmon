
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

    asysmon.service('Rest', ['$log', '$http', '$location', 'Modal', function($log, $http, $location, Modal) {
        this.call = function(service, onSuccess, onError) {
            $http
                .get('_$_asysmon_$_/rest/' + curPage($location) + '/' + service)
                .success(onSuccess)
                .error(function(data, status, headers, config) {
                    Modal.error('Server or Network Error', htmlForErrorDialog(data, status, headers, config));

                    if(onError) {
                        onError(data, status, headers, config);
                    }
                });
        };
    }]);

    function htmlForErrorDialog(data, status, headers, config) {
        var result = '<table width="100%" class="table table-condensed table-striped">' +
            '<tr><td>URL</td><td>' + config.url + '</td></tr>';

        if(status !== 599) {
            result += '<tr><td>Status</td><td>' + status + '</td></tr>';
        }
        else {
            if(data && data.msg) {
                result += '<tr><td>Error</td><td>' + data.msg + '</td></tr>';
            }
            if(data && data.details && data.details.length) {
                result += '<tr><td>Details</td><td>';
                angular.forEach(data.details, function(d) {
                    result += escapeHtml(d) + '<br>';
                });
                result += '</td></tr>';
            }
        }

        result += '</table>';

        return result;
    }

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
        if(!number && number !== 0) {
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
    function escapeHtml(string) {
        return String(string || '').replace(/[&<>"'\/]/g, function (s) {
            return entityMap[s];
        });
    }
    asysmon.constant('escapeHtml', escapeHtml);


    asysmon.constant('startsWith', function (s, prefix) {
        s = s || '';
        prefix = prefix || '';

        return s.indexOf(prefix) === 0;
    });

    asysmon.service('Modal', function() {
        function doShow(title, body) {
            $('#modal-title').text(title);
            $('#modal-body').html(body);
            $('#the-modal').modal('show');

            var height = $(window).height() - 200;
            $("#modal-body").css("max-height", height);
        }

        this.error = function(title, body) {
            $('#modal-icon').html('<span class="glyphicon glyphicon-warning-sign" style="color: red; padding-right: 25px;"></span>');
            doShow(title, body);
        };
        this.warning = function(title, body) {
            $('#modal-icon').html('<span class="glyphicon glyphicon-warning-sign" style="color: orange; padding-right: 25px;"></span>');
            doShow(title, body);
        };
        this.info = function(title, body) {
            $('#modal-icon').html('<span class="glyphicon glyphicon-ok" style="color: green; padding-right: 25px;"></span>');
            doShow(title, body);
        };
        this.hide = function() {
            $('#the-modal').modal('hide');
        }
    });
}());





