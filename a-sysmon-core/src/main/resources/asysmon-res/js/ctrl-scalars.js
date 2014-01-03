
angular.module('ASysMonApp').controller('CtrlScalars', function($scope, $log, Rest) {

    function initFromResponse(data) {
        $scope.scalars = data.scalars;
    }

    function sendCommand(cmd) {
        Rest.call(cmd, initFromResponse);
    }

    $scope.refresh = function() {
        sendCommand('getData');
    };

    $scope.refresh();

    var thousandsSeparator = 1234.5.toLocaleString().charAt(1);
    var decimalSeparator   = 1234.5.toLocaleString().charAt(5);

    $scope.formatNumber = function(number, numFracDigits) {
        var parts = number.toFixed(numFracDigits).toString().split('.');
        parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, thousandsSeparator);
        return parts.join(decimalSeparator);
    };

    $scope.formattedScalar = function(name, factor) {
        factor = factor || 1;
        var s = $scope.scalars && $scope.scalars[name];
        return s ? $scope.formatNumber(s.value*factor, s.numFracDigits) : '';
    };
    $scope.hasLoad = function() {
        return $scope.scalars && $scope.scalars['load-1-minute'];
    };
    $scope.load1 = function() {
        return $scope.formattedScalar('load-1-minute');
    };
    $scope.load5 = function() {
        return $scope.formattedScalar('load-5-minutes');
    };
    $scope.load15 = function() {
        return $scope.formattedScalar('load-15-minutes');
    };

    function startsWith(s, prefix) {
        s = s || '';
        prefix = prefix || '';

        return s.indexOf(prefix) === 0;
    }

    $scope.genericScalarNames = function() {
        var result = [];
        if($scope.scalars) {
            $.each($scope.scalars, function(n) {
                if(startsWith(n, 'load-')) {
                    return;
                }
                result.push(n);
            });
        }
        return result;
    };
});

