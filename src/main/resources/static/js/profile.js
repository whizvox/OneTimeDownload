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

});