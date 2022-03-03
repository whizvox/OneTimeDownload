let badPasswordBox = $('#incorrect-password');
let form = $('#form-download-file');
let idField = $('#file-id');
let passwordField = $('#password');
let downloadButton = $('#btn-download');

downloadButton.on('click', function() {
  let fileId = idField[0].value;
  let reqData = {'password': encodeUTF8ToBase64(passwordField[0].value)};
  $.ajax({
    url: '/files/info/' + fileId,
    type: 'get',
    contentType: false,
    cache: false,
    data: reqData,
    success: function(data) {
      $(location).attr('href', `/files/dl/${fileId}?password=${reqData['password']}`);
    },
    error: function(xhr, status, error) {
      console.log(xhr);
      console.log(status);
      console.log(error);
    }
  });
});