function encodeUTF8ToBase64(str) {
  return btoa(unescape(encodeURIComponent(str)))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=+$/, '');
}

function formatDigitalLength(n) {
  if (n < 1000) {
    return n + " B";
  }
  const suffixes = ["KB", "MB", "GB", "TB", "EB", "ZB", "YB"];
  let i = 0;
  while (n > 1000000 && i < suffixes.length - 1) {
    n = Math.floor(n / 1000);
    i++;
  }
  return Math.floor(n / 1000) + "." + Math.floor((n % 1000) / 100) + " " + suffixes[i];
}

function disableElement(elem) {
  elem.attr('disabled', true);
}

function enableElement(elem) {
  elem.attr('disabled', false);
}

function isDisabled(elem) {
  return elem.attr('disabled');
}

function hideElement(elem) {
  elem.attr('hidden', true);
}

function showElement(elem) {
  elem.attr('hidden', false);
}

function isHidden(elem) {
  return elem.attr('hidden');
}

function formatRelativeTime(date) {
  let delta = Math.floor((date.valueOf() - new Date().valueOf()) / 1000);
  let after = delta > 0;
  delta = Math.abs(delta);
  let seconds = delta % 60;
  let minutes = Math.floor(delta / 60) % 60;
  let hours = Math.floor(delta / 3600) % 24;
  let days = Math.floor(delta / 86400);
  let res = "";
  if (days > 0) {
    res += days + " day";
    if (days > 1) {
      res += "s";
    }
  } else if (hours > 0) {
    res += hours + " hour";
    if (hours !== 1) {
      res += "s";
    }
  } else if (minutes > 0) {
    res += minutes + " minute";
    if (minutes !== 1) {
      res += "s";
    }
  } else {
    res += seconds + " second";
    if (seconds !== 1) {
      res += "s";
    }
  }
  if (after) {
    res += " from now";
  } else {
    res += " ago";
  }
  return res;
}

function formatDuration(seconds) {
  let sec = seconds % 60;
  let min = Math.floor(seconds / 60) % 60;
  let hrs = Math.floor(seconds / 3600) % 24;
  let days = Math.floor(seconds / 86400);
  let res = "";
  if (days > 0) {
    res += days + "d";
  }
  if (hrs > 0) {
    if (res.length > 0) {
      res += " ";
    }
    res += hrs + "h";
  }
  if (min > 0) {
    if (res.length > 0) {
      res += " ";
    }
    res += min + "m";
  }
  if (sec > 0) {
    if (res.length > 0) {
      res += " ";
    }
    res += sec + "s";
  }
  return res;
}

function getCSRFHeader() {
  let res = {};
  res[$("meta[name='_csrf_header']").attr("content")] = $("meta[name='_csrf']").attr("content");
  return res;
}

/**
 * Given an XMLHttpRequest, attempt to derive a 1TDL API response object
 * @param xhr XMLHttpRequest
 * @return The response object, or null if one could not be found
 */
function parseApiResponse(xhr) {
  if (xhr.hasOwnProperty('responseJSON')) {
    let res = xhr.responseJSON;
    if (res.hasOwnProperty('error') && res.hasOwnProperty('status') && res.hasOwnProperty('data')) {
      return res;
    }
  }
  return null;
}

function showErrorAlert(alertElem, warning, xhr) {
  let errorHeader = "Unexpected response";
  switch (xhr.status) {
    case 400: {
      errorHeader = "Bad request";
      break;
    }
    case 401: {
      errorHeader = "Unauthorized";
      break;
    }
    case 403: {
      errorHeader = "Forbidden";
      break;
    }
    case 404: {
      errorHeader = "Not found";
      break;
    }
    case 409: {
      errorHeader = "Conflict";
      break;
    }
    case 413: {
      errorHeader = "Payload too large";
      break;
    }
    case 500: {
      errorHeader = "Internal server error";
      break;
    }
  }
  let errorBody = xhr.responseText;
  if (xhr.hasOwnProperty('responseJSON')) {
    let res = xhr.responseJSON;
    if (res.hasOwnProperty('data')) {
      if (typeof(res.data) !== 'object') {
        errorBody = res.data;
      } else {
        // pretty print
        errorBody = JSON.stringify(res.data, null, 2);
      }
    } else if (res.hasOwnProperty('timestamp') && res.hasOwnProperty('trace')) {
      // FIXME Change default Spring error response to something less revealing (no stacktrace)
      errorBody = "TIMESTAMP: " + res.timestamp;
    } else {
      errorBody = JSON.stringify(res);
    }
  }
  showAlert(alertElem, warning ? 'warning' : 'danger', errorBody, xhr.status + ': ' + errorHeader);
}

function showAlert(alertElem, alertType, text, header = undefined) {
  alertElem.removeClass('alert-danger alert-info alert-warning alert-primary alert-secondary alert-success alert-light alert-dark');
  alertElem.addClass('alert-' + alertType);
  alertElem.contents().remove();
  if (header !== undefined) {
    let headerElem = $('<h3>');
    headerElem.text(header);
    let bodyElem = $('<p>');
    bodyElem.text(text);
    alertElem.append(headerElem);
    alertElem.append(bodyElem);
  } else {
    alertElem.text(text);
  }
  showElement(alertElem);
}

$(document).ready(function() {
  $('#btn-logout').on('click', function() {
    $.ajax({
      url: '/logout',
      type: 'post',
      headers: getCSRFHeader(),
      success: function() {
        location.reload();
      },
      error: function(xhr) {
        console.log(xhr);
      }
    });
  })
});