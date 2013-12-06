var asysmonControllers = angular.module('asysmonControllers', []);

asysmonControllers.controller('StartCtrl', ['$scope', '$http',
    function ($scope) {
        $scope.names = ['Ada', 'Berta', 'Caesar'];
    }]);

