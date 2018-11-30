var oldHtml = $("#events").html();

function showRes(res) {
    var str = res;
    var split = str.split("|");
    var items = "";
    for (var i = 0; i < split.length; i++) {
        var info = split[i];
        var item_res = addEvent(info);
        if (item_res !== null) {
            items += addEvent(info);
        }
    }
    $("#events").html(oldHtml + items);
}

function addEvent(info) {
    if (info !== "") {
        var split = info.split(":");
        var horse = split[0];
        var bet = split[1];
        var date = split[2];
        var item = "<div class='event_item'>" + "<div class='dot_active ei_Dot'></div>" + "<div class='ei_Title'>" + horse + "</div>" + "<div class='ei_Copy'> Apuesta: $" + bet + "</div>" + "<div class='ei_Copy'> Fecha: " + date + "</div>" + "</div>";
        return item;
    } else {
        return null;
    }
}

function changeStyle() {
    if ($("#style_container").hasClass('light')) {
        $("#style_container").removeClass('light');
        $("#style_container").toggleClass('dark');
    } else {
        $("#style_container").removeClass('dark');
        $("#style_container").toggleClass('light');
    }
}