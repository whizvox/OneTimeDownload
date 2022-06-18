$(document).ready(function() {

  let alert = $('#alert');

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

  function validateField(field, feedback, isValidFunc) {
    if (isValidFunc()) {
      field.removeClass('is-invalid');
      hideElement(feedback);
    } else {
      field.addClass('is-invalid');
      showElement(feedback);
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
    return passwordField.is(':valid');
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
    validateField(confirmEmailField, emailMismatchAlert, isEmailConfirmValid);
    updateSubmitButton();
  });

  confirmEmailField.on('input', function() {
    validateField($(this), emailMismatchAlert, isEmailConfirmValid);
    updateSubmitButton();
  });

  passwordField.on('input', function() {
    validateField($(this), invalidPasswordAlert, isPasswordValid);
    validateField(confirmPasswordField, passwordMismatchAlert, isPasswordConfirmValid);
    updateSubmitButton();
  });

  confirmPasswordField.on('input', function() {
    validateField($(this), passwordMismatchAlert, isPasswordConfirmValid);
    updateSubmitButton();
  });

  form.submit(function(event) {
    event.preventDefault();
    $('input').removeClass('is-invalid');
    hideElement($('.invalid-feedback'));
    disableElement(submitButton);
    submitButton.text("Registering...");
    hideElement(alert);
    $.ajax({
      url: '/users/available',
      type: 'get',
      data: `email=${emailField.val()}`,
      headers: getCSRFHeader(),
      processData: false,
      contentType: false,
      cache: false,
      success: function(data) {
        if (data.data) {
          $.ajax({
            url: '/users/register',
            type: 'post',
            data: new FormData(form[0]),
            headers: getCSRFHeader(),
            processData: false,
            contentType: false,
            cache: false,
            success: function(data) {
              if (data.data.verified) {
                $(location).attr('href', '/login?created');
              } else {
                $(location).attr('href', '/need-verify');
              }
            },
            error: function(xhr) {
              showErrorAlert(alert, false, xhr);
            },
            complete: function() {
              enableElement(submitButton);
              submitButton.text("Register");
            }
          });
        } else {
          emailField.addClass('is-invalid');
          showElement(emailTakenAlert);
          enableElement(submitButton);
          submitButton.text("Register");
        }
      },
      error: function(xhr) {
        showErrorAlert(alert, false, xhr);
        enableElement(submitButton);
        submitButton.text("Register");
      }
    })
  });
});
