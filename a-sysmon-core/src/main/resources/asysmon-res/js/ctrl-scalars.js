
angular.module('ASysMonApp').controller('CtrlScalars', function($scope, $log, Rest, formatNumber, startsWith) {
    var effectiveNumCpus = 1;

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
        effectiveNumCpus = data.scalars['cpu:available'].value / 100;
        render();
    }

    function isMisc(s) {
        if(startsWith(s.name, 'load-')) return false;
        if(startsWith(s.name, 'mem:')) return false;
        if(startsWith(s.name, 'cpu:')) return false;
        if(startsWith(s.name, 'net:')) return false;
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
        $('#cpu').html(htmlForCpu());
        $('#network').html(htmlForNetwork());
        $('#load1').html(htmlForLoad($scope.scalars['load-1-minute']));
        $('#load5').html(htmlForLoad($scope.scalars['load-5-minutes']));
        $('#load15').html(htmlForLoad($scope.scalars['load-15-minutes']));
        $('#jvm-mem').html(htmlForJvmMemory());
    }

    $scope.otherCpuPercent = function() {
        if(!$scope.scalars) {
            return '';
        }
        var raw = $scope.scalars['cpu:all-used'].value - $scope.scalars['cpu:self-user'].value - $scope.scalars['cpu:self-kernel'].value;
        return formatNumber(raw, 1);
    }

    function htmlForCpu() {
        function segment(color, value) {
            var max = $scope.scalars['cpu:available'].value;
            var width = value * 100 / max;
            return '<div class="progress-bar ' + color + '" role="progressbar" aria-valuenow="' + value + '" aria-valuemin="0" aria-valuemax="' + max + '" style="width: ' + width + '%"></div>';
        }

        return '<div class="progress scalar-load-progress">' +
            segment('progress-bar-info', $scope.scalars['cpu:self-kernel'].value) +
            segment('progress-bar-success', $scope.scalars['cpu:self-user'].value) +
            segment('progress-bar-warning', $scope.scalars['cpu:all-used'].value - $scope.scalars['cpu:self-user'].value - $scope.scalars['cpu:self-kernel'].value) +
            '</div>';
    }

    function networkInterfaces() {
        var result = [];

        angular.forEach($scope.scalars, function(s) {
            if(!startsWith(s.name, 'net:')) {
                return;
            }

            var iface = s.name.substr(4);
            iface = iface.substr(0, iface.indexOf(':'));

            for(var i=0; i<result.length; i++) {
                if(result[i] === iface) {
                    return;
                }
            }
            result.push(iface);
        });

        result.sort();
        return result;
    }

    function htmlForNetwork() {
        var result = '<table class="table table-condensed table-striped">' +
            '<tr><th class="scalar-name">Interface</th><th class="scalar-value-centered">received pkt/s</th><th class="scalar-value-centered">sent pkt/s</th><th class="scalar-value-centered">collisions/s</th></tr>';

        angular.forEach(networkInterfaces(), function(iface) {
            function effValue(raw) {
                if(raw === 0) return 0;
                if(raw < .2) raw = .2;
                return Math.log(raw) / Math.LN10 + 1; // range from 0 to 7
            }

            var scalarReceived   = $scope.scalars['net:' + iface + ':received-pkt'];
            var scalarSent       = $scope.scalars['net:' + iface + ':sent-pkt'];
            var scalarCollisions = $scope.scalars['net:' + iface + ':collisions'];

            result += '<tr><td class="scalar-name">' + iface +
                '</td><td class="scalar-value-centered">' + htmlForPercentageBar(100, 0, 7, effValue(scalarReceived.value),    8,  8,  8, scalarReceived.formattedValue) +
                '</td><td class="scalar-value-centered">' + htmlForPercentageBar(100, 0, 7, effValue(scalarSent.value),       -1,  8,  8, scalarSent.formattedValue) +
                '</td><td class="scalar-value-centered">' + htmlForPercentageBar(100, 0, 3, effValue(scalarCollisions.value), -1, -1, -1, scalarCollisions.formattedValue) +
                '</td></tr>';
        });

        return result + '</table>';
    }

    function htmlForLoad(loadScalar) {
        var numCpus = effectiveNumCpus;
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

        if(percent > 40) {
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

    function htmlForJvmMemory() {
        var result = '<table class="table table-condensed table-striped">';
        result += '<tr><th class="scalar-name">Memory Kind</th><th></th><th class="scalar-value">Used</th><th class="scalar-value">Comm.</th><th class="scalar-value">Max</th></tr>';

        angular.forEach(memKinds(), function(memKind) {
            var used      = $scope.scalars['mem:' + memKind + ':used'];
            var committed = $scope.scalars['mem:' + memKind + ':committed'];
            var max       = $scope.scalars['mem:' + memKind + ':max'];

            var percent = used.value / max.value * 100;

            result += '<tr>' +
                '<td class="scalar-name">' + memKind + '</td>' +
                '<td class="scalar-value">' + htmlForPercentageBar('100px', 0, 100, percent, 50, 80, 90, formatNumber(percent,1) + '%') + '</td>' +
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

