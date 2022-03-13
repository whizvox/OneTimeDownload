let fileUploaded = $('#file-uploaded');
let downloadLinkNoClick = $('#download-link-noclick');

fileUploaded.text(new Date(fileUploaded.html()));
downloadLinkNoClick.text(location.origin + '/download/' + $('#file-id').html());
$('#btn-copy-link').on('click', function() {
  navigator.clipboard.writeText(downloadLinkNoClick.html());
});