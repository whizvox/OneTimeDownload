function clickConfirmLink(elem, href, method = 'get', onSuccess = undefined, onError = undefined) {
  if (onSuccess === undefined) {
    onSuccess = function() {
      alert('Success');
    };
  }
  if (onError === undefined) {
    onError = function(xhr, status, error) {
      alert('Fail');
      console.log(xhr);
      console.log(status);
      console.log(error);
    }
  }
  elem.on('click', function() {
    let good = confirm("Are you sure you want to do this?");
    if (good) {
      $.ajax({
        url: '/debug/' + href,
        type: method,
        processData: false,
        contentType: false,
        cache: false,
        headers: getCSRFHeader(),
        success: onSuccess,
        error: onError
      });
    }
  });
}

clickConfirmLink($('#btn-files-clear'), 'files/clear', 'delete', function(data) {
  let count = data.data;
  if (count === 0) {
    alert('No files deleted');
  } else if (count === 1) {
    alert('1 file deleted');
  } else {
    alert(`${data.data} file(s) deleted`);
  }
});

clickConfirmLink($('#btn-server-shutdown'), 'server/shutdown', 'post', function() {
  alert('Server will shutdown in 5 seconds');
});

clickConfirmLink($('#btn-add-admin'), 'users/admin', 'post', function(data) {
  alert(`Admin account created. Username: admin, Password: ${data.data}`);
});

clickConfirmLink($('#btn-delete-admin'), 'users/admin', 'delete', function() {
  alert('Admin account deleted');
});

clickConfirmLink($('#btn-clear-tokens'), 'tokens/clear', 'delete', function(data) {
  alert(`${data.data} token(s) deleted`);
});

clickConfirmLink($('#btn-clear-users'), 'users/clear', 'delete', function(data) {
  alert(`${data.data} user(s) deleted`);
});