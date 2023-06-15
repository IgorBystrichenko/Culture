// Модальное окно логина
var loginModal = document.getElementById("loginModal");
var loginButton = document.getElementById("loginButton");
var loginClose = document.getElementById("loginClose");

loginButton.onclick = function() {
  loginModal.style.display = "block";
}

loginClose.onclick = function() {
  loginModal.style.display = "none";
}

window.onmousedown = function(event) {
  if (event.target === loginModal) {
    loginModal.style.display = "none";
  }
}