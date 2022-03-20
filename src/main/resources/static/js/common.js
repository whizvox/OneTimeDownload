function encodeUTF8ToBase64(str) {
  return btoa(unescape(encodeURIComponent(str)))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=+$/, '');
}

function getCSRFHeader() {
  let res = {};
  res[$("meta[name='_csrf_header']").attr("content")] = $("meta[name='_csrf']").attr("content");
  return res;
}

$(document).ready(function() {
  $('#btn-logout').on('click', function() {
    $.ajax({
      url: '/logout',
      type: 'post',
      headers: getCSRFHeader(),
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