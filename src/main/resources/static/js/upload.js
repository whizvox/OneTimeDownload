$(document).ready(function() {

  let uploadAlert = $('#upload-alert');
  let submitButton = $(':submit');
  let dropZone = $('#file-upload-dropzone');
  let fileNameText = $('#file-upload-filename');
  let fileField = $('#file');
  let fileInvalid = $('#file-invalid');
  let passwordField = $('#password');
  let passwordConfirmField = $('#password-confirm');
  let passwordConfirmInvalid = $('#password-confirm-invalid');
  let lifespanField = $('#lifespan');
  let lifespanValue = $('#lifespan-value');
  let lifespanAfterAccessField = $('#lifespan-after-access');
  let lifespanAfterAccessValue = $('#lifespan-after-access-value');

  function updateSubmitButton() {
    let valid =
        fileField[0].files.length > 0 &&
        passwordField.val() === passwordConfirmField.val() &&
        (lifespanField.val() === undefined || lifespanField.val() > 0);
    if (valid) {
      enableElement(submitButton);
    } else {
      disableElement(submitButton);
    }
  }

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
        updateSubmitButton();
      });

  fileField.change(function(e) {
    let fileList = e.target.files;
    if (fileList.length > 0) {
      fileNameText
          .removeClass('text-muted')
          .text(fileList[0].name);
      hideElement(fileInvalid);
    } else {
      fileNameText
          .addClass('text-muted')
          .text('Click or drag and drop');
      showElement(fileInvalid);
    }
    updateSubmitButton();
  });

  function validatePasswordConfirmField() {
    let mismatch = passwordField.val() !== passwordConfirmField.val();
    submitButton.prop('disabled', mismatch);
    if (mismatch) {
      passwordConfirmField.removeClass('is-valid');
      passwordConfirmField.addClass('is-invalid');
      showElement(passwordConfirmInvalid);
    } else {
      passwordConfirmField.removeClass('is-invalid');
      passwordConfirmField.addClass('is-valid');
      hideElement(passwordConfirmInvalid);
    }
    updateSubmitButton();
  }

  passwordField.on('input', function() {
    validatePasswordConfirmField();
  });

  passwordConfirmField.on('input', function() {
    validatePasswordConfirmField();
  });

  function updateLifespanValueFormat() {
    lifespanValue.text(formatDuration(lifespanField.val() * 60));
  }

  lifespanField.on('input', function() {
    updateLifespanValueFormat();
  });

  function updateLifespanAfterAccessFormat() {
    lifespanAfterAccessValue.text(formatDuration(lifespanAfterAccessField.val() * 60));
  }

  lifespanAfterAccessField.on('input', function() {
    updateLifespanAfterAccessFormat();
  });

  submitButton.on('click', function(e) {
    e.preventDefault();
    hideElement(uploadAlert);
    disableElement($(this));
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
      error: function(xhr) {
        showErrorAlert(uploadAlert, false, xhr);
        enableElement(submitButton);
        submitButton.text('Submit');
      }
    })
  });

  updateLifespanValueFormat();
  updateLifespanAfterAccessFormat();
  updateSubmitButton();

});