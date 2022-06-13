$(document).ready(function() {

  const alert = $('#alert');
  const form = $('#form-reset-password');
  const passwordField = $('#password');
  const confirmPasswordField = $('#confirm-password');
  const invalidPasswordFeedback = $('#feedback-invalid');
  const mismatchedPasswordsFeedback = $('#feedback-mismatch');
  const submitButton = form.find(':submit');

  function doPasswordsMatch() {
    return passwordField.val() === confirmPasswordField.val();
  }

  function isPasswordValid() {
    return passwordField.is(':valid');
  }

  function updateSubmitButton() {
    let shouldEnable = doPasswordsMatch() && isPasswordValid();
    if (shouldEnable) {
      enableElement(submitButton);
    } else {
      disableElement(submitButton);
    }
  }

  function updateMismatchFeedback() {
    let showFeedback = !doPasswordsMatch();
    if (showFeedback) {
      showElement(mismatchedPasswordsFeedback);
    } else {
      hideElement(mismatchedPasswordsFeedback);
    }
  }

  passwordField.on('input', function() {
    if (isPasswordValid()) {
      $(this).removeClass('is-invalid');
      hideElement(invalidPasswordFeedback);
    } else {
      $(this).addClass('is-invalid');
      showElement(invalidPasswordFeedback);
    }
    updateMismatchFeedback();
    updateSubmitButton();
  });

  confirmPasswordField.on('input', function() {
    updateMismatchFeedback();
    updateSubmitButton();
  });

  form.submit(function(event) {
    event.preventDefault();
    submitButton.text("Resetting...");
    disableElement(submitButton);
    $('input').removeClass('is-invalid');
    hideElement($('.invalid-feedback'));
    hideElement(alert);
    $.ajax({
      url: '/users/reset',
      type: 'put',
      data: new FormData($(this)[0]),
      headers: getCSRFHeader(),
      processData: false,
      contentType: false,
      cache: false,
      success: function(data) {
        $(location).attr('href', '/login?reset');
      },
      error: function(xhr, status, error) {
        if (xhr.status === 404) {
          showAlert(alert, 'warning', "Token either does not exist or has expired");
        } else if (xhr.status === 400) {
          showAlert(alert, 'warning', "Invalid password");
          showElement(invalidPasswordFeedback);
        } else {
          showErrorAlert(alert, false, xhr);
        }
      },
      complete: function() {
        submitButton.text('Reset password');
        enableElement(submitButton);
      }
    });
  });

});