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
  if (sec > 0 || res.length === 0) {
    res += sec + "s";
  }
  return res;
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