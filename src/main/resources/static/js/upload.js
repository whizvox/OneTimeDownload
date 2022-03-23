let uploadAlert = $('#upload-alert');
let alertCode = $('#upload-alert-code');
let alertMessage = $('#upload-alert-message');
let submitButton = $(':submit');
let dropZone = $('#file-upload-dropzone');
let fileNameText = $('#file-upload-filename');
let fileField = $('#file');
let passwordField = $('#password');

dropZone
    .hover(function() {
      $(this).css('cursor', 'pointer');
    }, function() {
      $(this).css('cursor', 'default');
    })
    .on('dragenter', function() {
      $(this).addClass('file-dropped');
    })
    .on('dragleave', function() {
      $(this).removeClass('file-dropped');
    })
    .on('dragover', function(e) {
      e.stopPropagation();
      e.preventDefault();
      return true;
    })
    .on('drop', function(e) {
      e.stopPropagation();
      e.preventDefault();
      $(this).removeClass('file-dropped');
      let files = e.originalEvent.dataTransfer.files;
      fileNameText
          .removeClass('text-muted')
          .text(files[0].name);
      fileField.prop('files', files);
    });

fileField.change(function(e) {
  fileNameText
      .removeClass('text-muted')
      .text(e.target.files[0].name);
});

$('#password-confirm').change(function() {
  let mismatch = passwordField.val() !== $(this).val();
  submitButton.prop('disabled', mismatch);
  if (mismatch) {
    $(this).removeClass('is-valid');
    $(this).addClass('is-invalid');
  } else {
    $(this).removeClass('is-invalid');
    $(this).addClass('is-valid');
  }
});

submitButton.on('click', function(e) {
  e.preventDefault();
  uploadAlert.attr('hidden', true);
  uploadAlert.removeClass('alert-warning', 'alert-danger');
  $(this).attr('disabled', true);
  $(this).text('Uploading...');
  let formData = new FormData($('#upload-file')[0]);
  formData.set('password', encodeUTF8ToBase64(passwordField[0].value))
  $.ajax({
    url: '/files',
    type: 'post',
    enctype: 'multipart/form-data',
    processData: false,
    contentType: false,
    cache: false,
    data: formData,
    headers: getCSRFHeader(),
    success: function(data) {
      $(location).attr('href', '/view/' + data.data.id);
    },
    error: function(xhr, status, error) {
      uploadAlert.attr('hidden', false);
      alertCode.text(xhr.status + " " + error);
      if (xhr.status === 400) {
        uploadAlert.addClass('alert-warning');
      } else {
        uploadAlert.addClass('alert-danger');
      }
      if (xhr.responseJSON) {
        alertMessage.text(xhr.responseJSON.data.message);
      } else {
        alertMessage.text(xhr.response);
      }
      submitButton.attr('disabled', false);
      submitButton.text('Submit');
    }
  })
});