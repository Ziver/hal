// --------------------------------------------------------
//                      Autostart
// --------------------------------------------------------

"use strict";

var alertDivId = "alert-container"
var alertTemplate = {
    ERROR: `
        <div id="" data-alert-id="" class="alert alert-danger alert-dismissible fade in">
            <button type="button" class="close" data-dismiss="alert">
                <span>&times;</span>
            </button>
            <span class="glyphicon glyphicon-minus-sign"></span>
            <strong class="alert-title"></strong> &nbsp;
            <span class="alert-description"></span>
        </div>`,
    WARNING: `
        <div id="" data-alert-id="" class="alert alert-warning alert-dismissible fade in">
            <button type="button" class="close" data-dismiss="alert">
                <span>&times;</span>
            </button>
            <span class="glyphicon glyphicon-warning-sign"></span>
            <strong class="alert-title"></strong> &nbsp;
            <span class="alert-description"></span>
        </div>`,
    SUCCESS: `
        <div id="" data-alert-id="" class="alert alert-success alert-dismissible fade in">
            <button type="button" class="close" data-dismiss="alert">
                <span>&times;</span>
            </button>
            <span class="glyphicon glyphicon-ok-circle"></span>
            <strong class="alert-title"></strong> &nbsp;
            <span class="alert-description"></span>
        </div>`,
    INFO: `
        <div id="" data-alert-id="" class="alert alert-info alert-dismissible fade in">
            <button type="button" class="close" data-dismiss="alert">
                <span>&times;</span>
            </button>
            <span class="glyphicon glyphicon-info-sign"></span>
            <strong class="alert-title"></strong> &nbsp;
            <span class="alert-description"></span>
        </div>`
}

$(function(){
    updateAlerts();

    setInterval(function() {
        updateAlerts();
    }, 3000); // 3 sec
});

function updateAlerts() {
    fetch('/api/alert?action=poll')
        .then(response => response.json())
        .then(data => {
            data.forEach(alert => {
                var alertElement = $("#alert-id-" + alert.id);
                if (alertElement.length <= 0) {
                    alertElement = $(alertTemplate[alert.level]);
                    $("#" + alertDivId).append(alertElement);

                    alertElement.attr("id", "alert-id-" + alert.id);
                    alertElement.data("alert-id", alert.id);
                    alertElement.find(".close").click(dismissEvent);
                }

                alertElement.find(".alert-title").html(alert.title);
                alertElement.find(".alert-description").html(alert.description);
            });
        });
}

function dismissEvent(e) {
    dismissAlert($(e.target).parent().parent().data("alert-id"));
}
function dismissAlert(id) {
    fetch('/api/alert?action=dismiss&id=' + id)
        .then(response => response.json())
        .then(data => {
        });
}