angular
    .module('mediaList')
    .component('mediaList', {
        templateUrl: 'media-list/media-list.template.html',
        controller: ['$http', function MediaListController($http) {
            var self = this;
            $http.get('/list').then(function (response) {
                self.media = response.data;
                console.log(self.media);
            })
        }]
    });