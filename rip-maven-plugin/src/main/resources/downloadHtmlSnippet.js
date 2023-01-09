function delay() {
  setTimeout(function() {
    var div = document.getElementsByClassName('information-container wrapper')[0];
    var newElement = document.createElement("div");
    newElement.innerHTML = '<div id=download-div>';

    div.innerHTML += '<div class="swagger-ui" style="font-family: sans-serif;font-size: 14px;">' +
      ' Download <a class="link" href="./swagger/swagger.html">HTML</a> | <a class="link" href="./swagger/swagger.pdf">PDF</a>' +
      '</div>';

    //Below is to fix problem with added div element not always being shown. Taken from: https://stackoverflow.com/q/3485365/6095334
    div.style.display='none';
    div.offsetHeight; // no need to store this anywhere, the reference is enough
    div.style.display='block';
  }, 600);
}

if (document.readyState == 'complete') {
  delay();
} else {
  document.onreadystatechange = function () {
    if (document.readyState === "complete") {
      delay();
    }
  }
}
