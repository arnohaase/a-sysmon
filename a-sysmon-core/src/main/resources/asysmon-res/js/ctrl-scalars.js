
angular.module('ASysMonApp').controller('CtrlScalars', function($scope, $log, Rest, formatNumber, startsWith) {

    function initFromResponse(data) {
        $scope.scalars = data.scalars;
        $scope.miscScalars = [];
        angular.forEach($scope.scalars, function(s, name) {
            s.name = name;

            var factor = 1, suffix='';
            if(startsWith(s.name, 'mem:')) {
                factor = 1024*1024;
                suffix = ' MB';
            }
            s.formattedValue = formatNumber(s.value / factor, s.numFracDigits) + suffix;

            if(isMisc(s)) {
                $scope.miscScalars.push(s)
            }
        });
        render();
    }

    function isMisc(s) {
        if(startsWith(s.name, 'load-')) return false;
        if(startsWith(s.name, 'mem:')) return false;
        return true;
    }

    function sendCommand(cmd) {
        Rest.call(cmd, initFromResponse);
    }

    $scope.refresh = function() {
        sendCommand('getData');
    };

    $scope.refresh();

    function render() {
        $('#load1').html(htmlForLoad($scope.scalars['load-1-minute']));
        $('#load5').html(htmlForLoad($scope.scalars['load-5-minutes']));
        $('#load15').html(htmlForLoad($scope.scalars['load-15-minutes']));
        $('#mem').html(htmlForMemory());
    }

    function htmlForLoad(loadScalar) {
        var numCpus = 8; //TODO
        return htmlForPercentageBar('100px', 0, numCpus, loadScalar.value, 1, numCpus/2, numCpus, loadScalar.formattedValue);
    }

    function htmlForPercentageBar(width, min, max, value, thresholdInfo, thresholdWarning, thresholdDanger, formattedValue) {
        var progressColor = '';
        if(value < thresholdInfo) {
            progressColor = 'progress-bar-success';
        }
        else if(value < thresholdWarning) {
            progressColor = 'progress-bar-info';
        }
        else if(value < thresholdDanger) {
            progressColor = 'progress-bar-warning';
        }
        else {
            progressColor = 'progress-bar-danger';
        }

        var percent = (value-min) / (max-min) * 100;
        if(percent > 100) {
            percent = 100;
        }

        var numberInside='', numberOutside='';

        if(percent > 33) {
            numberInside = '<span>' + formattedValue + '</span>';
        }
        else {
            numberOutside = '<div style="position: absolute; width: ' + width + '; text-align: center">&nbsp;' + formattedValue + '</div>';
        }

        return '<div class="progress scalar-load-progress">' +
            numberOutside +
            '<div class="progress-bar ' + progressColor + '" role="progressbar" aria-valuenow="' + percent + '" aria-valuemin="0" aria-valuemax="100" style="width: ' + percent + '%">' +
            numberInside +
            '</div>' +
            '</div>';
    }

    function htmlForMemory() {
        var result = '<table class="table table-condensed table-striped">';
        result += '<tr><th class="scalar-name">Memory Kind</th><th></th><th class="scalar-value">Used</th><th class="scalar-value">Comm.</th><th class="scalar-value">Max</th></tr>';

        angular.forEach(memKinds(), function(memKind) {
            var used      = $scope.scalars['mem:' + memKind + ':used'];
            var committed = $scope.scalars['mem:' + memKind + ':committed'];
            var max       = $scope.scalars['mem:' + memKind + ':max'];

            var percent = used.value / max.value * 100;

            result += '<tr>' +
                '<td class="scalar-name">' + memKind + '</td>' +
                '<td class="scalar-value">' + htmlForPercentageBar('100px', 0, 100, percent, 50, 70, 90, formatNumber(percent,1) + '%') + '</td>' +
                '<td class="scalar-value">' + used.     formattedValue + '</td>' +
                '<td class="scalar-value">' + committed.formattedValue + '</td>' +
                '<td class="scalar-value">' + max.      formattedValue + '</td>' +
                '</tr>';
        });

        result += '</table>';
        return result;
    }

    function memKinds() {
        var map = {};
        angular.forEach($scope.scalars, function(s) {
            if(startsWith(s.name, 'mem:')) {
                var memKind = s.name.substr(4);
                memKind = memKind.substr(0, memKind.indexOf(':'));
                map[memKind] = memKind;
            }
        });

        var result = [];
        angular.forEach(map, function(memKind) {
            result.push(memKind);
        });
        result.sort();
        return result;
    }
});

