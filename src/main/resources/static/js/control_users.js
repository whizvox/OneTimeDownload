$(document).ready(function() {

  let selfUserId = $('#self-user-id');
  let errorAlert = $('#alert-error');
  let form = $('#form-users');
  let searchButton = form.find(':submit');
  let resultsArea = $('#results');
  let noResultsAlert = $('#no-results');
  let editButton = $('#btn-edit');
  let deleteButton = $('#btn-delete');
  let resultsTable = $('#table-users');
  let selectAllCheck = $('#select-all');

  let editModal = $('#modal-edit');
  let editAlert = $('#alert-edit');
  let editForm = $('#form-edit');
  let editEmailField = editForm.find('input[name=email]');
  let editRankField = editForm.find('select[name=rank]');
  let editGroupField = editForm.find('select[name=group]');
  let editVerifiedField = editForm.find('input[name=enabled]');
  let editPasswordField = editForm.find('input[name=password]');
  let editApplyButton = $('#btn-edit-apply');

  let deleteModal = $('#modal-delete');
  let deleteAlert = $('#alert-delete');
  let deleteSelfAlert = $('#alert-delete-self');
  let deleteUsersList = deleteModal.find('ul');
  let deleteApplyButton = $('#btn-delete-apply');

  let fetchedUsers = new Map();
  let selectedUsers = [];

  function updateEditDeleteButtons() {
    if (selectedUsers.length === 0) {
      disableElement(editButton);
      disableElement(deleteButton);
    } else {
      if (selectedUsers.length > 1) {
        disableElement(editButton);
      } else {
        enableElement(editButton);
      }
      enableElement(deleteButton);
    }
  }

  form.submit(function(event) {
    event.preventDefault();
    selectedUsers = [];
    fetchedUsers.clear();
    hideElement(resultsArea);
    hideElement(noResultsAlert);
    hideElement(errorAlert);
    disableElement(searchButton);
    searchButton.text('Searching...');
    $.ajax({
      url: '/users/search',
      type: 'get',
      data: $(this).serialize(),
      contentType: false,
      cache: false,
      headers: getCSRFHeader(),
      success: function(data) {
        let users = data.data.items;
        if (users.length === 0) {
          showElement(noResultsAlert);
        } else {
          let tableBody = resultsTable.find('tbody');
          tableBody.remove();
          tableBody = resultsTable.append($('<tbody>'));
          fetchedUsers.clear();
          users.forEach(user => {
            fetchedUsers.set(user.id, user);
            let row = $('<tr>');
            let check = $('<input>').attr('type', 'checkbox').attr('id', `userentry-${user.id}`);
            check.change(function() {
              let userId = $(this).attr('id').substr(10);
              if (this.checked) {
                selectedUsers.push(userId);
              } else {
                selectedUsers = selectedUsers.filter(id => id !== userId);
              }
              updateEditDeleteButtons();
            });
            row.append($('<td>').append(check));
            row.append($('<td>').text(user.id));
            row.append($('<td>').text(user.email));
            row.append($('<td>').text(user.rank));
            row.append($('<td>').text(user.group));
            row.append($('<td>').text(user.enabled ? 'Yes' : 'No'));
            tableBody.append(row);
          });
          showElement(resultsArea);
        }
      },
      error: function(xhr, status, error) {
        errorAlert.text(xhr.responseText);
        showElement(errorAlert);
      },
      complete: function() {
        searchButton.text('Search');
        enableElement(searchButton);
      }
    });
  });

  selectAllCheck.change(function() {
    const shouldCheck = this.checked;
    if (!shouldCheck) {
      selectedUsers = [];
    }
    resultsTable.find('input[type=checkbox]').each(function() {
      let userId = $(this).attr('id').substr(10);
      if (shouldCheck) {
        selectedUsers.push(userId);
      }
      $(this).prop('checked', shouldCheck);
    });
  });

  editButton.click(function() {
    hideElement(editAlert);
    if (selectedUsers.length > 0) {
      // have to explicitly cast the ID to a number. selectedUsers stores strings, but the keys for fetchedUsers gets
      // auto-type-casted to numbers.
      const user = fetchedUsers.get(Number(selectedUsers[0]));
      editEmailField.val(user.email);
      editRankField.val(user.rank);
      editGroupField.val(user.group);
      editVerifiedField.prop('checked', user.enabled);
    }
  });

  editForm.submit(function(event) {
    event.preventDefault();
    if (selectedUsers.length > 0) {
      const userId = selectedUsers[0];
      disableElement(editApplyButton);
      editApplyButton.text('Saving...');
      hideElement(editAlert);
      editAlert.removeClass('alert-danger', 'alert-success');
      $.ajax({
        url: `/users/${userId}`,
        type: 'put',
        data: $(this).serialize(),
        processData: false,
        contentType: false,
        cache: false,
        headers: getCSRFHeader(),
        success: function(data) {
          editAlert.addClass('alert-success');
          editAlert.text('User successfully updated');
          searchButton.click();
        },
        error: function(xhr, status, error) {
          editAlert.addClass('alert-danger');
          editAlert.text(xhr.responseText);
        },
        complete: function() {
          showElement(editAlert);
          editApplyButton.text('Save changes');
          enableElement(editApplyButton);
        }
      });
    }
  });

  deleteButton.click(function() {
    hideElement(deleteAlert);
    hideElement(deleteSelfAlert);
    if (selectedUsers.length > 0) {
      deleteUsersList.children().remove();
      selectedUsers.forEach(userId => {
        if (userId === selfUserId.val()) {
          showElement(deleteSelfAlert);
        }
        deleteUsersList.append($('<li>').text(`${userId} (${fetchedUsers.get(Number(userId)).email})`));
      });
    }
  });

  deleteApplyButton.click(function() {
    if (selectedUsers.length > 0) {
      hideElement(deleteAlert);
      disableElement(deleteApplyButton);
      deleteApplyButton.text('Deleting...');
      let formData = new FormData();
      formData.append('ids', selectedUsers);
      $.ajax({
        url: '/users/delete',
        type: 'post',
        data: formData,
        headers: getCSRFHeader(),
        processData: false,
        contentType: false,
        cache: false,
        success: function(data) {
          deleteModal.find('.btn-close').click();
        },
        error: function(xhr, status, error) {
          deleteAlert.text(xhr.responseText);
          showElement(deleteAlert);
        },
        complete: function() {
          deleteApplyButton.text('Delete');
          enableElement(deleteApplyButton);
          searchButton.click();
        }
      });
    }
  });

});