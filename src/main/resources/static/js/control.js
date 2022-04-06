$(document).ready(function() {

  let alert = $('#alert');
  let alertText = alert.find('span');
  let filesCount = $('#files-count');
  let filesStorage = $('#files-storage');
  let usersCount = $('#users-count');
  let usersUnverified = $('#users-unverified');
  let serverUptime = $('#server-uptime');
  let recalculateButton = $('#btn-recalculate');
  let restartButton = $('#btn-restart');
  let restartAlert = $('#alert-restart');
  let restartAlertText = restartAlert.find('span');
  let shutdownButton = $('#btn-shutdown');
  let shutdownAlert = $('#alert-shutdown');
  let shutdownAlertText = shutdownAlert.find('span');

  recalculateButton.click(function() {
    hideElement(alert);
    disableElement(recalculateButton);
    $.ajax({
      url: '/server/stats',
      type: 'get',
      headers: getCSRFHeader(),
      contentType: false,
      cache: false,
      success: function(data) {
        let stats = data.data;
        filesCount.text(stats.filesCount);
        filesStorage.text(formatDigitalLength(stats.filesStorage));
        usersCount.text(stats.usersCount);
        usersUnverified.text(stats.usersUnverified);
        serverUptime.text(formatDuration(Math.floor(stats.serverUptime / 1000)));
      },
      error: function(xhr, status, error) {
        if (xhr.hasOwnProperty('responseJSON')) {
          let res = xhr.responseJSON;
          if (res.error) {
            alertText.text(res.data);
          } else {
            alertText.text(res);
          }
        } else {
          alertText.text(xhr.responseText);
        }
        showElement(alert);
      },
      complete: function() {
        enableElement(recalculateButton);
      }
    });
  });

  restartButton.click(function() {
    disableElement($(this));
    hideElement(restartAlert);
    restartAlert.removeClass('alert-danger', 'alert-success');
    $.ajax({
      url: '/server/restart',
      type: 'post',
      headers: getCSRFHeader(),
      contentType: false,
      cache: false,
      success: function(data) {
        restartAlert.addClass('alert-success');
        restartAlertText.text('Server will restart in 5 seconds');
        showElement(restartAlert);
      },
      error: function(xhr, status, error) {
        restartAlert.addClass('alert-danger');
        if (xhr.hasOwnProperty('responseJSON')) {
          let res = xhr.responseJSON;
          if (res.error) {
            restartAlertText.text(res.msg);
          } else {
            restartAlertText.text(res);
          }
        } else {
          restartAlertText.text(xhr.responseText);
        }
        enableElement(restartButton);
      }
    });
  });

  shutdownButton.click(function() {
    disableElement($(this));
    hideElement(shutdownAlert);
    shutdownAlert.removeClass('alert-danger', 'alert-success');
    $.ajax({
      url: '/server/shutdown',
      type: 'post',
      headers: getCSRFHeader(),
      contentType: false,
      cache: false,
      success: function(data) {
        shutdownAlert.addClass('alert-success');
        shutdownAlertText.text('Server will shut down in 5 seconds');
        showElement(shutdownAlert);
      },
      error: function(xhr, status, error) {
        shutdownAlert.addClass('alert-danger');
        if (xhr.hasOwnProperty('responseJSON')) {
          let res = xhr.responseJSON;
          if (res.error) {
            shutdownAlertText.text(res.msg);
          } else {
            shutdownAlertText.text(res);
          }
        } else {
          shutdownAlertText.text(xhr.responseText);
        }
        enableElement(shutdownButton);
      }
    });
  });

  recalculateButton.click();

});