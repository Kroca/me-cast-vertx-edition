angular
    .module('mediaList')
    .component('mediaList', {
        templateUrl: 'media-list/media-list.template.html',
        controller: ['$http', function MediaListController($http) {
            var self = this;
            $http.get('/list').then(function (response) {
                self.media = response.data;
            });
            var audioPlayer = document.getElementById('audioPlayer');
            self.loadData = function loadData(fileId) {
                audioPlayer.src = 'http://localhost:8080/download/' + fileId;
            }
        }]
    });