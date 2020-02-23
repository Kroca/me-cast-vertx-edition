angular.module('ucastApp').config(['$routeProvider',
    function config($routeProvider) {
        $routeProvider
            .when('/list', {
                template: '<media-list></media-list>'
            }).when('/upload', {
            template: 'There is going to be upload page soon.'
        }).otherwise('/');
    }
]);