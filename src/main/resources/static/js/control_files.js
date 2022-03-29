$(document).ready(function() {
  let form = $('#form-view-files');
  let submitButton = form.children(':submit');
  let results = $('#results-files');
  let noResultsAlert = $('#no-results');
  let editButton = $('#btn-edit-file');
  let deleteButton = $('#btn-delete-file');
  let table = $('#table-files');
  let errorMessage = $('#error-message');
  let selectAllFiles = $('#select-all-files');

  let selectedFiles = [];

  function updateEditDeleteButtons() {
    if (selectedFiles.length === 0) {
      disableElement(editButton);
      disableElement(deleteButton);
    } else {
      if (selectedFiles.length > 1) {
        disableElement(editButton);
      } else {
        enableElement(editButton);
      }
      enableElement(deleteButton);
    }
  }

  form.submit(function(event) {
    event.preventDefault();
    hideElement(results);
    hideElement(noResultsAlert);
    hideElement(errorMessage);
    submitButton.text('Searching...');
    disableElement(submitButton);
    disableElement(editButton);
    disableElement(deleteButton);
    $.ajax({
      url: '/files/search',
      type: 'get',
      data: $(this).serialize(),
      contentType: false,
      cache: false,
      headers: getCSRFHeader(),
      success: function(data) {
        let files = data.data.items;
        console.log(files);
        console.log(data);
        if (files.length === 0) {
          showElement(noResultsAlert);
        } else {
          showElement(results);
          let tableBody = table.children('tbody');
          tableBody.remove();
          tableBody = table.append('<tbody>');
          files.forEach(file => {
            let row = $('<tr>');
            let check = $('<input>').attr('type', 'checkbox').attr('id', `fileentry-${file.id}`);
            check.change(function() {
              let fileId = $(this).attr('id').substr(10);
              if (this.checked) {
                selectedFiles.push(fileId);
              } else {
                selectedFiles = selectedFiles.filter(id => id !== fileId);
              }
              updateEditDeleteButtons();
            });
            row.append($('<td>').append(check));
            row.append($('<td>').text(file.id));
            row.append($('<td>').text(file.fileName));
            let uploadDate = new Date(file.uploaded);
            row.append($('<td>').text(formatRelativeTime(uploadDate)).attr('title', uploadDate));
            row.append($('<td>').text(formatDigitalLength(file.originalSize)).attr('title', file.originalSize.toLocaleString() + " B"));
            let expireDate = new Date(file.expires);
            row.append($('<td>').text(formatRelativeTime(expireDate)).attr('title', expireDate));
            row.append($('<td>').text(file.downloaded ? 'Yes' : 'No'));
            if (file.user) {
              //row.append();
            } else {
              row.append($('<td>').text('Anonymous'));
            }
            tableBody.append(row);
          });
        }
      },
      error: function(xhr, status, error) {
        /*if (xhr.responseJSON) {
          let data = xhr.responseJSON;
          if (data.timestamp) {

          }
        }*/
        errorMessage.text(xhr.response);
        showElement(errorMessage);
      },
      complete: function() {
        submitButton.text('Search');
        enableElement(submitButton);
      }
    });
  });

  selectAllFiles.change(function() {
    let shouldCheck = this.checked;
    if (!shouldCheck) {
      selectedFiles = selectedFiles.filter(_ => false);
    }
    table.find('tbody input[type=checkbox]').each(function() {
      let fileId = $(this).attr('id').substr(10);
      if (shouldCheck) {
        selectedFiles.push(fileId);
      }
      $(this).prop('checked', shouldCheck);
    });
    updateEditDeleteButtons();
  });

});