$(document).ready(function() {

  let alert = $('#alert');
  let alertText = alert.find('span');
  let form = $('#form-create-user');
  let emailField = form.find('input[name=email]');
  let emailInvalid = $('#email-invalid');
  let emailTaken = $('#email-taken');
  let passwordField = form.find('input[name=password]');
  let passwordInvalid = $('#password-invalid');
  let submitButton = form.find(':submit');

  function isEmailValid() {
    let email = emailField.val();
    return email.length > 0;
  }

  function isPasswordValid() {
    let password = passwordField.val();
    return password.length > 0;
  }

  function updateSubmitButton() {
    let invalid =
        emailField.hasClass('is-invalid') ||
        passwordField.hasClass('is-invalid');
    if (invalid) {
      disableElement(submitButton);
    } else {
      enableElement(submitButton);
    }
  }

  emailField.on('input', function() {
    if (isEmailValid()) {
      emailField.removeClass('is-invalid');
      hideElement(emailInvalid);
    } else {
      emailField.addClass('is-invalid');
      showElement(emailInvalid);
    }
    updateSubmitButton();
  });

  passwordField.on('input', function() {
    if (isPasswordValid()) {
      passwordField.removeClass('is-invalid');
      hideElement(passwordInvalid);
    } else {
      passwordField.addClass('is-invalid');
      showElement(passwordInvalid);
    }
    updateSubmitButton();
  });

  form.submit(function(event) {
    event.preventDefault();
    hideElement($('.invalid-feedback'));
    hideElement(alert);
    alert.removeClass('alert-danger', 'alert-success');
    disableElement(submitButton);

    $.ajax({
      url: '/users/available',
      type: 'get',
      data: `email=${emailField.val()}`,
      headers: getCSRFHeader(),
      contentType: false,
      cache: false,
      success: function(data) {
        if (data.data) {
          $.ajax({
            url: '/users',
            type: 'post',
            data: new FormData(form[0]),
            headers: getCSRFHeader(),
            processData: false,
            contentType: false,
            cache: false,
            success: function(data) {
              let user = data.data;

              alert.addClass('alert-success');
              alertText.text(`New user created with ID: ${user.id}`);
              showElement(alert);
              form.reset();
            },
            error: function(xhr, status, error) {
              alert.addClass('alert-danger');
              if (xhr.hasOwnProperty('responseJSON')) {
                if (xhr.status === 400)
                  alertText.text(xhr.responseJSON.data);
              } else {
                alertText.text(xhr.responseText);
              }
              showElement(alert);
            },
            complete: function() {
              enableElement(submitButton);
            }
          });
        } else {
          alert.addClass('alert-warning');
          alertText.text('That email address is already taken');
          showElement(alert);
        }
      },
      error: function(xhr, status, error) {
        alert.addClass('alert-danger');
        if (xhr.hasOwnProperty('responseJSON')) {
          alertText.text(`${xhr.responseJSON.status}: ${xhr.responseJSON.data}`);
        } else {
          alertText.text(xhr.responseText);
        }
        showElement(alert);
      }
    });
  });

});