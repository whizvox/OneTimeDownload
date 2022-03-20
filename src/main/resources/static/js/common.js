function encodeUTF8ToBase64(str) {
  return btoa(unescape(encodeURIComponent(str)))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=+$/, '');
}

$(document).ready(function() {
  $('#btn-logout').on('click', function() {
    $.ajax({
      url: '/logout',
      type: 'post',
      success: function(data, status, xhr) {
        if (xhr.status === 200) {
          location.reload();
        } else {
          console.log(data);
        }
      }
    });
  })
});