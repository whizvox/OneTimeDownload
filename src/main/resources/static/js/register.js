$(document).ready(function() {
  let alertError = $('#alert-error');
  let alertErrorMsg = $('#alert-error-msg');

  $('#form-register').submit(function(event) {
    event.preventDefault();
    alertError.attr('hidden', true);
    alertError.removeClass('alert-warn', 'alert-danger');
    let formData = new FormData($(this)[0]);
    $.ajax({
      url: $(this).attr('action'),
      type: $(this).attr('method'),
      data: formData,
      headers: getCSRFHeader(),
      processData: false,
      contentType: false,
      cache: false,
      success: function(response) {
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
