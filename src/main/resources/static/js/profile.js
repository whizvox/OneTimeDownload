$(document).ready(function() {

  const generalAlert = $('#general-alert');
  const accountCreationTimestamp = $('#account-creation-timestamp');
  const btnChangeEmail = $('#btn-change-email');
  const modalChangeEmail = $('#modal-change-email');
  const formChangeEmail = $('#form-change-email');
  const alertChangeEmail = modalChangeEmail.find('.alert');
  const inputChangeEmail_email = $('#email-change-email');
  const feedback_changeEmail_invalidEmail = $('#feedback-change-email-invalid');
  const feedback_changeEmail_emailTaken = $('#feedback-change-email-taken');
  const inputChangeEmail_confirmEmail = $('#confirm-email-change-email');
  const feedback_changeEmail_emailMismatch = $('#feedback-change-email-confirm');
  const inputChangeEmail_password = $('#password-change-email');
  const feedback_changeEmail_wrongPassword = $('#feedback-change-email-wrong-password');
  const btnApplyChangeEmail = $('#btn-apply-change-email');
  const btnSendVerificationEmail = $('#btn-send-verification-email');
  const filesAlert = $('#files-alert');
  const btnViewFiles = $('#btn-view-files');
  const btnRefreshFiles = $('#btn-refresh-files');
  const filesTable = $('#table-files');

  const btnDeactivate = $('#btn-deactivate-account');
  const alertDeactivate = $('#alert-deactivate');
  const passwordFieldDeactivate = $('#deactivate-password');
  const wrongPasswordDeactivate = $('#feedback-deactivate-wrong-password');
  const formDeactivate = $('#form-deactivate');
  const btnDeactivateApply = $('#btn-deactivate-apply');

  // update account creation timestamp to JS's default date format
  accountCreationTimestamp.text(new Date(accountCreationTimestamp.text()));

  function checkConfirmEmailMatches() {
    if (inputChangeEmail_email.val() !== inputChangeEmail_confirmEmail.val()) {
      inputChangeEmail_confirmEmail.addClass('is-invalid');
      showElement(feedback_changeEmail_emailMismatch);
    } else {
      inputChangeEmail_confirmEmail.removeClass('is-invalid');
      hideElement(feedback_changeEmail_emailMismatch);
    }
  }

  function updateChangeEmailSubmitButton() {
    let email = inputChangeEmail_email.val();
    let confirmEmail = inputChangeEmail_confirmEmail.val();
    let isValid = inputChangeEmail_email.is(':valid') && email === confirmEmail;
    if (isValid) {
      enableElement(btnApplyChangeEmail);
    } else {
      disableElement(btnApplyChangeEmail);
    }
  }

  function refreshFilesTable(onCompleteFunc = undefined) {
    filesTable.find('tbody').remove();
    let tableBody = filesTable.append('<tbody>');
    $.ajax({
      url: '/files/all',
      type: 'get',
      headers: getCSRFHeader(),
      contentType: false,
      cache: false,
      success: function(res) {
        let files = res.data.items;
        if (files.length === 0) {
          showAlert(filesAlert, 'secondary', "No files found. Try uploading some!");
        } else {
          files.forEach(file => {
            let row = $('<tr>');
            row.append($('<td>').text(file.id));
            row.append($('<td>').text(file.fileName));
            let uploadDate = new Date(file.uploaded);
            row.append($('<td>').text(formatRelativeTime(uploadDate)).attr('title', uploadDate));
            row.append($('<td>').text(formatDigitalLength(file.originalSize)).attr('title', file.originalSize.toLocaleString() + " B"));
            let expireDate = new Date(file.expires);
            row.append($('<td>').text(formatRelativeTime(expireDate)).attr('title', expireDate));
            row.append($('<td>').text(file.downloaded ? 'Yes' : 'No'));
            tableBody.append(row);
          });
          showElement(filesTable);
        }
      },
      error: function(xhr) {
        showErrorAlert(filesAlert, false, xhr);
      },
      complete: function() {
        if (onCompleteFunc !== undefined) {
          onCompleteFunc();
        }
      }
    });
  }

  inputChangeEmail_email.on('input', function() {
    if (!inputChangeEmail_email.is(':valid')) {
      $(this).addClass('is-invalid');
      showElement(feedback_changeEmail_invalidEmail);
    } else {
      $(this).removeClass('is-invalid');
      hideElement(feedback_changeEmail_invalidEmail);
    }
    checkConfirmEmailMatches();
    updateChangeEmailSubmitButton();
  });

  inputChangeEmail_confirmEmail.on('input', function() {
    checkConfirmEmailMatches();
    updateChangeEmailSubmitButton();
  });

  btnChangeEmail.click(function() {
    hideElement(alertChangeEmail);
    formChangeEmail[0].reset();
    formChangeEmail.find('input').removeClass('is-invalid');
    hideElement(formChangeEmail.find('.invalid-feedback'));
  });

  btnApplyChangeEmail.click(function(event) {
    event.preventDefault();
    disableElement(btnApplyChangeEmail);
    btnApplyChangeEmail.text('Applying...');
    formChangeEmail.find('input').removeClass('is-invalid');
    hideElement(formChangeEmail.find('.invalid-feedback'));
    $.ajax({
      url: '/users/available',
      data: `email=${inputChangeEmail_email.val()}`,
      headers: getCSRFHeader(),
      contentType: false,
      cache: false,
      success: function(data) {
        if (data.data) {
          $.ajax({
            url: 'users/self/email',
            type: 'put',
            // FIXME shouldn't need a FormData object to do this?
            data: new FormData(formChangeEmail[0]),
            headers: getCSRFHeader(),
            processData: false,
            contentType: false,
            cache: false,
            success: function(data) {
              if (data.data) {
                location.reload();
              } else {
                enableElement(btnApplyChangeEmail);
                btnApplyChangeEmail.text('Apply changes');
              }
            },
            error: function(xhr, status, error) {
              if (xhr.status === 403) {
                inputChangeEmail_password.addClass('is-invalid');
                showElement(feedback_changeEmail_wrongPassword);
              }
              enableElement(btnApplyChangeEmail);
              btnApplyChangeEmail.text('Apply changes');
            }
          });
        } else {
          inputChangeEmail_email.addClass('is-invalid');
          showElement(feedback_changeEmail_emailTaken);
          enableElement(btnApplyChangeEmail);
          btnApplyChangeEmail.text('Apply changes');
        }
      },
      error: function(xhr, status, error) {
        enableElement(btnApplyChangeEmail);
        btnApplyChangeEmail.text('Apply changes');
      },
      complete: function() {

      }
    });
  });

  btnSendVerificationEmail.click(function() {
    disableElement($(this));
    $(this).text("Sending...");
    $.ajax({
      url: '/users/send-verification-email',
      type: 'post',
      headers: getCSRFHeader(),
      contentType: false,
      cache: false,
      success: function() {
        showAlert(generalAlert, 'info', "Verification link sent. Check your email's inbox!");
      },
      error: function(xhr) {
        showErrorAlert(generalAlert, false, xhr);
      },
      complete: function() {
        enableElement(btnSendVerificationEmail);
        btnSendVerificationEmail.text("Resend verification email");
      }
    });
  });

  btnRefreshFiles.click(function() {
    disableElement($(this));
    disableElement(btnViewFiles);
    hideElement(filesAlert);
    hideElement(filesTable);
    $(this).text("Refreshing...");
    refreshFilesTable(function() {
      enableElement(btnRefreshFiles);
      enableElement(btnViewFiles);
      btnRefreshFiles.text("Refresh");
    });
  });

  btnViewFiles.click(function() {
    if ($(this).hasClass('btn-secondary')) {
      // hide files
      btnViewFiles.removeClass('btn-secondary');
      btnViewFiles.addClass('btn-info');
      btnViewFiles.text("Show files");
      hideElement(btnRefreshFiles);
      hideElement(filesTable);
    } else {
      // show and refresh files list
      btnViewFiles.removeClass('btn-info');
      btnViewFiles.addClass('btn-secondary');
      btnViewFiles.text("Hide files");
      showElement(btnRefreshFiles);
      btnRefreshFiles.click();
    }
  });

  btnDeactivate.click(function() {
    hideElement(alertDeactivate);
    formDeactivate[0].reset();
    hideElement(formDeactivate.find('.invalid-feedback'));
    formDeactivate.find('input').removeClass('is-invalid');
  });

  btnDeactivateApply.click(function(event) {
    event.preventDefault();
    disableElement(btnDeactivateApply);
    btnDeactivateApply.text("Deactivating...");
    $.ajax({
      url: '/users/self/deactivate',
      type: 'post',
      data: new FormData(formDeactivate[0]),
      headers: getCSRFHeader(),
      processData: false,
      contentType: false,
      cache: false,
      success: function(data) {
        if (data.data) {
          $.ajax({
            url: '/logout',
            type: 'post',
            headers: getCSRFHeader(),
            success: function (xhr) {
              if (xhr.status === 200) {
                $(location).attr('href', "/");
              } else {
                showErrorAlert(alertDeactivate, false, xhr);
              }
            }
          });
        }
      },
      error: function(xhr) {
        if (xhr.status === 400) {
          passwordFieldDeactivate.addClass('is-invalid');
          showElement(wrongPasswordDeactivate);
        } else {
          showErrorAlert(alertDeactivate, false, xhr);
        }
      },
      complete: function() {
        btnDeactivateApply.text("DEACTIVATE");
        enableElement(btnDeactivateApply);
      }
    });
  });

});