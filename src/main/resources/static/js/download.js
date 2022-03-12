let accessDeniedDiv = $('#access-denied');
let form = $('#form-download-file');
let idField = $('#file-id');
let passwordField = $('#password');
let downloadButton = $('#btn-download');

downloadButton.on('click', function() {
  accessDeniedDiv.attr('hidden', true);
  let fileId = idField[0].value;
  let reqData = {'password': encodeUTF8ToBase64(passwordField[0].value)};
  $.ajax({
    url: `/files/available/${fileId}`,
    type: 'get',
    contentType: false,
    cache: false,
    data: reqData,
    success: function(data) {
      if (data.data) {
        $(location).attr('href', `/files/dl/${fileId}?password=${reqData['password']}`);
      } else {
        accessDeniedDiv.attr('hidden', false);
      }
    },
    error: function(xhr, status, error) {
      accessDeniedDiv.attr('hidden', false);
    }
  });
});