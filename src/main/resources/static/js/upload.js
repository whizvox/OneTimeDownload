$('#file-upload-dropzone')
    .hover(function() {
      $(this).css('cursor', 'pointer');
    }, function() {
      $(this).css('cursor', 'default');
    })
    .on('dragenter', function() {
      $(this).addClass('file-dropped');
    })
    .on('dragleave', function() {
      $(this).removeClass('file-dropped');
    })
    .on('dragover', function(e) {
      e.stopPropagation();
      e.preventDefault();
      return true;
    })
    .on('drop', function(e) {
      e.stopPropagation();
      e.preventDefault();
      $(this).removeClass('file-dropped');
      let files = e.originalEvent.dataTransfer.files;
      $('#file-upload-filename')
          .removeClass('text-muted')
          .text(files[0].name);
      $('#file').prop('files', files);
    });

$('#file').change(function(e) {
  $('#file-upload-filename')
      .removeClass('text-muted')
      .text(e.target.files[0].name);
});

$('#password-confirm').change(function() {
  let mismatch = $('#password').val() !== $(this).val();
  $(':submit').prop('disabled', mismatch);
  if (mismatch) {
    $(this).removeClass('is-valid');
    $(this).addClass('is-invalid');
  } else {
    $(this).removeClass('is-invalid');
    $(this).addClass('is-valid');
  }
});

$(':submit').on('click', function(e) {
  e.preventDefault();
  let formData = new FormData($('#upload-file')[0]);
  formData.set('password', encodeUTF8ToBase64($('#password')[0].value))
  $.ajax({
    url: '/files',
    type: 'post',
    enctype: 'multipart/form-data',
    processData: false,
    contentType: false,
    cache: false,
    data: formData,
    success: function(data) {
      $(location).attr('href', '/view/' + data.data.id);
      console.log(data);
    },
    error: function(e) {
      console.log(e);
    }
  })
});