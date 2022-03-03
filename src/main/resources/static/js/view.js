$('#file-uploaded').text(new Date($('#file-uploaded').html()));
$('#download-link-noclick').text(location.origin + '/download/' + $('#file-id').html());
$('#btn-copy-link').on('click', function() {
  navigator.clipboard.writeText($('#download-link-noclick').html());
});