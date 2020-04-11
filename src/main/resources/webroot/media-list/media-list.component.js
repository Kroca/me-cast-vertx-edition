angular
    .module('mediaList')
    .component('mediaList', {
        templateUrl: 'media-list/media-list.template.html',
        controller: ['$http', function MediaListController($http) {
            var self = this;
            $http.get('/list').then(function (response) {
                self.media = response.data;
            });

            self.loadData = function loadData(fileId) {
                // $http.get('/download/' + fileId).then(function (response) {
                //     console.log(response.data);
                //     var sound = new Howl({
                //         src: [response.data],
                //         format: ['mp3']
                //     });
                //     sound.play();
                // });
                var sound = new Howl({
                    src: ['http://localhost:8080/download/' + fileId],
                    format: ['mp3'],
                    autoplay: true,
                    loop: true,
                    volume: 0.5,
                    html5:true
                });
                // sound.play();
            }
        }]
    });