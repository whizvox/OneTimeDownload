$(document).ready(function() {
  let alertError = $('#alert-error');
  let alertErrorMsg = $('#alert-error-msg');

  let form = $('#form-register');
  let emailField = form.find('input[name=email]');
  let confirmEmailField = $('#confirm-email');
  let passwordField = form.find('input[name=password]');
  let confirmPasswordField = $('#confirm-password');
  let submitButton = form.find(':submit');

  let invalidEmailAlert = $('#invalid-email');
  let emailTakenAlert = $('#email-taken');
  let emailMismatchAlert = $('#email-mismatch');
  let invalidPasswordAlert = $('#invalid-password');
  let passwordMismatchAlert = $('#password-mismatch');

  function validateField(field, invalid, validator) {
    if (validator()) {
      field.removeClass('invalid');
      hideElement(invalid);
    } else {
      field.addClass('invalid');
      showElement(invalid);
    }
    updateSubmitButton();
  }

  function isEmailValid() {
    return emailField.val().match(/.+@.+\..+/);
  }

  function isEmailConfirmValid() {
    return confirmEmailField.val() === emailField.val();
  }

  function isPasswordValid() {
    return passwordField.val().match(new RegExp(passwordField.attr('pattern')));
  }

  function isPasswordConfirmValid() {
    return confirmPasswordField.val() === passwordField.val();
  }

  function updateSubmitButton() {
    let valid =
        isEmailValid() &&
        isEmailConfirmValid() &&
        isPasswordValid() &&
        isPasswordConfirmValid();
    if (valid) {
      enableElement(submitButton);
    } else {
      disableElement(submitButton);
    }
  }

  emailField.on('input', function() {
    validateField($(this), invalidEmailAlert, isEmailValid);
  });

  confirmEmailField.on('input', function() {
    if ($(this).val() === emailField.val()) {
      $(this).removeClass('invalid');
      hideElement(emailMismatchAlert);
    } else {
      $(this).addClass('invalid');
      showElement(emailMismatchAlert);
    }
    updateSubmitButton();
  });

  confirmPasswordField.on('input', function() {
    if ($(this).val() === passwordField.val()) {
      $(this).removeClass('invalid');
      hideElement(passwordMismatchAlert);
    } else {
      $(this).addClass('invalid');
      showElement(passwordMismatchAlert);
    }
    updateSubmitButton();
  });

  passwordField.on('input', function() {
    const pattern = new RegExp($(this).attr('pattern'));
    if ($(this).val().match(pattern)) {
      $(this).removeClass('invalid');
      hideElement(invalidPasswordAlert);
    } else {
      $(this).addClass('invalid');
      showElement(invalidPasswordAlert);
    }
    updateSubmitButton();
  });

  form.submit(function(event) {
    event.preventDefault();
    hideElement(alertError);
    alertError.removeClass('alert-warn', 'alert-danger');
    $.ajax({
      url: $(this).attr('action'),
      type: $(this).attr('method'),
      data: new FormData(form[0]),
      headers: getCSRFHeader(),
      processData: false,
      contentType: false,
      cache: false,
      success: function(data) {
        $(location).attr('href', '/need-confirm');
      },
      error: function(xhr) {
        alertError.attr('hidden', false);
        let response = xhr.responseJSON;
        if (xhr.status === 400 && response !== null) {
          alertError.addClass('alert-warn');
          alertErrorMsg.text(response.data.message);
        } else {
          alertError.addClass('alert-danger');
          alertErrorMsg.text(xhr.response);
        }
      }
    })
  });
});
