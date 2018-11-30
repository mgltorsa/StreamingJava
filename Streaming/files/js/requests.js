var myIp = 'localhost';
var datas = [];
var d = "";
var port = 1234;
var current = null;

function sendMessage() {
    var form = $("form");
    var datas = form.serialize();
    var url = 'http://' + myIp + ':' + port;
    alert(url);
    var xhttpRequest = new XMLHttpRequest();
    xhttpRequest.open('POST', url, true);
    xhttpRequest.onreadystatechange = function() {
        if (xhttpRequest.readyState === 4 && xhttpRequest.status === 200) {
            var re = xhttpRequest.responseText;
            window.location.replace("http://" + myIp + ":" + port + re);
        } else if (xhttpRequest.readyState === 4 && xhttpRequest.status === 500) {
            var re = xhttpRequest.responseText;
            alert(re);
        }
    };
    xhttpRequest.send(datas);
}
document.querySelector('#identifier').addEventListener('focus', function(e) {
    if (current) current.pause();
    current = anime({
        targets: 'path',
        strokeDashoffset: {
            value: 0,
            duration: 700,
            easing: 'easeOutQuart'
        },
        strokeDasharray: {
            value: '240 1386',
            duration: 700,
            easing: 'easeOutQuart'
        }
    });
});
document.querySelector('#password').addEventListener('focus', function(e) {
    if (current) current.pause();
    current = anime({
        targets: 'path',
        strokeDashoffset: {
            value: -336,
            duration: 700,
            easing: 'easeOutQuart'
        },
        strokeDasharray: {
            value: '240 1386',
            duration: 700,
            easing: 'easeOutQuart'
        }
    });
});
document.querySelector('#submit').addEventListener('focus', function(e) {
    if (current) current.pause();
    current = anime({
        targets: 'path',
        strokeDashoffset: {
            value: -730,
            duration: 700,
            easing: 'easeOutQuart'
        },
        strokeDasharray: {
            value: '530 1386',
            duration: 700,
            easing: 'easeOutQuart'
        }
    });
});