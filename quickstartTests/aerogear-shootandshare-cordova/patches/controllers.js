angular.module('starter.controllers', [])

.controller('Controller', function ($scope, $ionicPlatform, file) {
  $scope.takePicture = function () {
      getPicture();
  };

  $ionicPlatform.ready(function () {
    $scope.takePicture();
  });

  var url = {
    gplus: 'https://www.googleapis.com/upload/drive/v2/files',
    keycloak: '<location of keycloak server e.g. http://192.168.0.12:8080/shoot/rest/photos>',
    facebook: 'https://graph.facebook.com/me/photos'
  }

  $scope.upload = function (type) {
    oauth2[type].requestAccess()
      .then(function (token) {
        file.put(url[type], $scope.image, token)
          .then(function () {
            alert('Upload complete');
          });
      }, function (err) {
        console.log(err);
        alert(err.error);
      });
  };

  function getPicture() {

	  navigator.camera = (function() {

		  var fileSystemName;
		  
		  function success(fileEntry) {
			  fileSystemName = fileEntry;
		  }

		  function failure(e) {
			  console.log("FileSystem Error, unable to get picture from application directory.")
		  }

		  function getPicture(onSuccess, onFail) {

            console.log("downloading to " + cordova.file.applicationStorageDirectory + "/pic.png")

			var ft = new FileTransfer();
			ft.download(
					"file:///android_asset/www/img/pic.png",
					cordova.file.applicationStorageDirectory + "/pic.png",
					function(entry) {
						console.log(entry.fullPath)
						onSuccess(entry.fullPath)
					},
					function(error) {
						onFail(error)
					},
					true,
					{});
		  }
		  
		  return {
			  getPicture: getPicture
		  }
		})(); 

    navigator.camera.getPicture(onSuccess, onFail);
  }

  function onSuccess(image) {
    console.log('success');
    $scope.image = image;
    $scope.$apply();
  }

  function onFail(error) {
    alert(error);
  }

});
