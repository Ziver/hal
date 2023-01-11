"use strict";

var svg;
var data = {
    rooms: [],
    sensors: [],
    events: []
};
var editModeEnabled = false;

$(function(){
    // ------------------------------------------
    // Setup map
    // ------------------------------------------

    svg = SVG('map');

    // Initialize events

    $("#button-edit").click(function() {
        editMode(true);
    });
    $("#button-save").click(function() {
        saveMap();
        editMode(false);
        fetchData(drawMap);
    });
    $("#button-cancel").click(function() {
        editMode(false);
        fetchData(drawMap);
    });

    // Initialize background image uploader

    $("#button-bg-edit").click(function() {
        // Reset modal
        $('#bg-file-input').parent().show();
        if ($("#file_input").prop("jFiler") != null)
            $("#file_input").prop("jFiler").reset();
        $('#bg-file-progress').parent().hide();
        $('#bgUploadModal').modal('show');
    });
    $('#bg-file-input').filer({
        limit: 1,
        extensions: ['jpg','png','svg','gif'],
        maxSize: 3, // in MB
        uploadFile: {
            url: "",
            type: 'POST',
            enctype: 'multipart/form-data',
            beforeSend: function(){
                $('#bg-file-input').parent().hide();
                $('#bg-file-progress').parent().show();
            },
            success: function(data, el){
                $('#bgUploadModal').modal('hide');
                drawMap();
            },
            error: function(el){
                $("#bg-file-progress").addClass("progress-bar-danger");
            },
            onProgress: function(t){
                $("#bg-file-progress").css("width", t + "%");
            },
        }
    });

    // ------------------------------------------
    // Start draw loop
    // ------------------------------------------

    fetchData(drawMap);

    setInterval(function() {
        if (editModeEnabled == false) {
            fetchData(drawMap);
        }
    }, 3000); // 3 sec
    //}, 10000); // 10 sec

});

// ----------------------------------------------
// Events
// ----------------------------------------------

function editMode(enable){
    if (editModeEnabled == enable) {
        return;
    }

    editModeEnabled = enable;

    if (editModeEnabled) {
        $('.edit-mode').show();
        $('.view-mode').hide();
        $('#map').css('border-color', '#6eb16e');

        svg.select('.draggable').draggable(true);
        //svg.select('.resizable').on('click', selectEvent, false);
        svg.select('.resizable').selectize({
            points: ['rt', 'lb', 'rb'], // Add selection points on the corners
            rotationPoint: false
        }).resize()
    } else {
        $('.edit-mode').hide();
        $('.view-mode').show();
        $('#map').css('border-color', '');

        svg.select('.draggable').draggable(false);
        svg.select('resizable').selectize(false);
    }
}

function beforeDragEvent(e) {
    if (editModeEnabled == false) {
        e.preventDefault(); // Prevent drag
    }
}

function selectEvent(e) {
    if (editModeEnabled == true) {
        e.target.selectize({
            points: ['rt', 'lb', 'rb'], // Add selection points on the corners
            rotationPoint: false
        }).resize();
    }
}

// --------------------------------------
// Draw
// --------------------------------------

function drawMap() {
    // Reset map
    svg.clear();

    // Background

    if (svg.select(".bg-image").length() <= 0) {
        var bgImage = svg.image("?bgimage").addClass("bg-image")
            .x(0)
            .y(0)
            .width("100%")
            .height("100%");
    }

    // Rooms

    if (data.rooms != null) {
        $.each(data.rooms, function(i, room) {
            svg.select("#room-" + room.id).remove();

            var group = svg.group();

            group.text(room.name).move(5, 5).fill('#999');
            var rect = group.rect(room.map.width, room.map.height);
            setAlertStyle(rect, (room.alert == null ? null : room.alert.level));
            rect.addClass("resizable");

            group.addClass("room")
                .attr("id", "room-" + room.id)
                .attr("room-id", room.id)
                .x(room.map.x)
                .y(room.map.y)
                .addClass("draggable");
        });
    }

    // Sensors

    if (data.sensors != null) {
        $.each(data.sensors, function(i, sensor) {
            svg.select("#sensor-" + sensor.id).remove();

            var group = svg.group();
            group.element('title').words(sensor.name);

            group.text(sensor.data.valueStr).move(45, 15).fill('#999');
            group.image("/img/temperature.svg").size(50, 50);

            group.addClass("sensor")
                .attr("id", "sensor-" + sensor.id)
                .attr("sensor-id", sensor.id)
                .x(sensor.map.x)
                .y(sensor.map.y)
                .addClass("draggable");
        });
    }

    // Events

    if (data.events != null) {
        $.each(data.events, function(i, event) {
            svg.select("#event-" + event.id).remove();

            var group = svg.group();
            group.element('title').words(event.name);

            var img = "/img/lightbulb_off.svg";
            if (event.data.valueStr == "ON")
                img = "/img/lightbulb_on.svg";
            group.image(img).size(50, 50);

            group.addClass("event")
                .attr("id", "event-" + event.id)
                .attr("event-id", event.id)
                .x(event.map.x)
                .y(event.map.y)
                .addClass("draggable");
        });
    }
}

// ----------------------------------------------
// Load and Store data
// ----------------------------------------------

async function fetchData(callback) {
    await fetch('/api/room')
        .then(response => response.json())
        .then(json => {
            data.rooms = json;
        })

    await fetch('/api/sensor')
        .then(response => response.json())
        .then(json => {
            data.sensors = json;
        })

    await fetch('/api/event')
        .then(response => response.json())
        .then(json => {
            data.events = json;
        })

    callback();
}

function saveMap(){
    svg.select(".room").each(function(){
        saveDevice(this, "room", "room-id");
    });
    svg.select(".sensor").each(function(){
        saveDevice(this, "sensor", "sensor-id");
    });
    svg.select(".event").each(function(){
        saveDevice(this, "event", "event-id");
    });
}
function saveDevice(element, type, id) {
    var data = {
        action: "save",
        id: element.attr(id),
        type: type,
        x: element.x(),
        y: element.y()
    };

    var resizable = element.select(".resizable");
    if (resizable.length() > 0) {
        data.width = resizable.get(0).width();
        data.height = resizable.get(0).height();
    }

    $.ajax({
        async: false,
        dataType: "json",
        url: "/api/map?",
        data: data
    });
}

// ----------------------------------------------
// Colors
// ----------------------------------------------

function setAlertStyle(target, level=null) {
    target.addClass("pulse-border");
    target.fill('none');

    switch(level) {
        case "ERROR":
            target.stroke({opacity: 1, color: '#f00'});
            break;
        case "WARNING":
            target.stroke({opacity: 1, color: '#ffa500'});
            break;
        case "SUCCESS":
            target.stroke({opacity: 1, color: '#90EE90'});
            break;
        case "INFO":
            target.stroke({opacity: 1, color: '#87CEFA'});
            break;

        default:
            target.removeClass("pulse-border");
            target.stroke({
                color: '#000',
                opacity: 0.6,
                width: 3
            });;
            break;
    }
}