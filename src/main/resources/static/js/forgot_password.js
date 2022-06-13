$(document).ready(function() {

  const alert = $('#alert');
  const form = $('#form-forgot-password');
  const submitButton = form.find(':submit');

  form.submit(function(event) {
    event.preventDefault();
    hideElement(alert);
    disableElement(submitButton);
    submitButton.text('Submitting request...');
    $.ajax({
      url: '/users/reset',
      type: 'post',
      data: new FormData($(this)[0]),
      headers: getCSRFHeader(),
      processData: false,
      contentType: false,
      cache: false,
      success: function(data) {
        if (data.data) {
          showAlert(alert, 'info', "Password reset link sent. Check your email's inbox!");
        } else {
          showAlert(alert, 'warning', "That email address is not registered with this site.");
        }
      },
      error: function(xhr) {
        showErrorAlert(alert, false, xhr);
      },
      complete: function() {
        enableElement(submitButton);
        submitButton.text("Request password reset");
      }
    });
  });

});