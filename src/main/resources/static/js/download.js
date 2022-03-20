let accessDeniedAlert = $('#access-denied');
let form = $('#form-download-file');
let idField = $('#file-id');
let passwordField = $('#password');
let downloadButton = $('#btn-download');

function onDownloadStage(before) {
  if (before) {
    downloadButton.attr('disabled', true);
    downloadButton.text('Downloading...');
    accessDeniedAlert.attr('hidden', true);
  } else {
    downloadButton.attr('disabled', false);
    downloadButton.text('Download');
  }
}

downloadButton.on('click', function() {
  onDownloadStage(true);
  let fileId = idField[0].value;
  let reqData = {'password': encodeUTF8ToBase64(passwordField[0].value)};
  $.ajax({
    url: `/files/available/${fileId}`,
    type: 'get',
    contentType: false,
    cache: false,
    data: reqData,
    headers: getCSRFHeader(),
    success: function(data) {
      if (data.data) {
        $(location).attr('href', `/files/dl/${fileId}?password=${reqData['password']}`);
      } else {
        accessDeniedAlert.attr('hidden', false);
      }
    },
    error: function(xhr, status, error) {
      accessDeniedAlert.attr('hidden', false);
    },
    complete: function(xhr, status) {
      onDownloadStage(false);
    }
  });
});