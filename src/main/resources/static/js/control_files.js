$(document).ready(function() {
  // view
  let form = $('#form-view-files');
  let submitButton = form.children(':submit');
  let results = $('#results-files');
  let noResultsAlert = $('#no-results');
  let editButton = $('#btn-edit-file');
  let deleteButton = $('#btn-delete-file');
  let table = $('#table-files');
  let errorMessage = $('#error-message');
  let selectAllFiles = $('#select-all-files');

  // edit
  let editAlert = $('#alert-edit-file');
  let editForm = $('#form-edit-file');
  let editIdField = editForm.find('input[name=id]');
  let editFileNameField = editForm.find('input[name=fileName]');
  let editExpiresField = editForm.find('input[name=expires]');
  let editDownloadedField = editForm.find('input[name=downloaded]');
  let editSubmitButton = $('#btn-file-edit-save');
  let editModal = $('#modal-edit-file');

  // delete
  let deleteModal = $('#modal-delete-file');
  let deleteAlert = $('#alert-delete-file');
  let deleteForm = $('#form-delete-file');
  let deleteSubmitButton = deleteForm.find(':submit');
  let deleteList = deleteForm.find('ul');

  let selectedFiles = [];
  let fetchedFiles = new Map();

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
    disableElement(editButton);
    disableElement(deleteButton);
    fetchedFiles.clear();
    selectedFiles = [];
    submitButton.text('Searching...');
    disableElement(submitButton);
    $.ajax({
      url: '/files/search',
      type: 'get',
      data: $(this).serialize(),
      contentType: false,
      cache: false,
      headers: getCSRFHeader(),
      success: function(data) {
        let files = data.data.items;
        if (files.length === 0) {
          showElement(noResultsAlert);
        } else {
          showElement(results);
          let tableBody = table.children('tbody');
          tableBody.remove();
          tableBody = table.append('<tbody>');
          fetchedFiles.clear();
          files.forEach(file => {
            fetchedFiles.set(file.id, file);
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
              row.append($('<td>').text(file.user.id));
            } else {
              row.append($('<td>').text('Anonymous'));
            }
            tableBody.append(row);
          });
        }
      },
      error: function(xhr, status, error) {
        errorMessage.text(xhr.responseText);
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
      selectedFiles = [];
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

  editButton.click(function() {
    if (selectedFiles.length === 1) {
      let file = fetchedFiles.get(selectedFiles[0]);
      editIdField.val(file.id);
      editFileNameField.val(file.fileName);
      const d = new Date(file.expires);
      editExpiresField.val((new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString()).slice(0, -1));
      editDownloadedField.val(file.downloaded + "");
    }
  });

  editSubmitButton.click(function(event) {
    event.preventDefault();
    hideElement(editAlert);
    editAlert.removeClass('alert-success', 'alert-danger');
    disableElement($(this));
    $.ajax({
      url: "/files/" + editIdField.val(),
      type: 'put',
      processData: false,
      contentType: false,
      cache: false,
      data: new FormData(editForm[0]),
      headers: getCSRFHeader(),
      success: function(data) {
        showElement(editAlert);
        editAlert.addClass('alert-success');
        editAlert.text('Changes successfully applied');
        submitButton.click();
      },
      error: function(xhr, status, error) {
        showElement(editAlert);
        editAlert.addClass('alert-danger');
        editAlert.text(xhr.response);
      },
      complete: function() {
        enableElement(editSubmitButton);
      }
    });
  });

  deleteButton.click(function() {
    if (selectedFiles.length > 0) {
      hideElement(deleteAlert);
      deleteList.find('li').remove();
      for (let i in selectedFiles) {
        deleteList.append($('<li>').text(selectedFiles[i]).addClass('font-monospace'));
      }
    }
  });

  deleteSubmitButton.click(function(event) {
    event.preventDefault();
    hideElement(deleteAlert);
    deleteAlert.removeClass('alert-success', 'alert-danger');
    disableElement($(this));
    let formData = new FormData();
    formData.append('ids', selectedFiles);
    $.ajax({
      url: '/files/delete',
      type: 'post',
      processData: false,
      contentType: false,
      cache: false,
      headers: getCSRFHeader(),
      data: formData,
      success: function(data) {
        deleteAlert.addClass('alert-success');
        deleteAlert.text(`File(s) deleted`);
        showElement(deleteAlert);
        submitButton.click();
        deleteModal.find('.btn-close').click();
      },
      error: function(xhr) {
        deleteAlert.addClass('alert-danger');
        deleteAlert.text(xhr.responseText);
        showElement(deleteAlert);
      },
      complete: function() {
        enableElement(deleteSubmitButton);
      }
    });
  });

});