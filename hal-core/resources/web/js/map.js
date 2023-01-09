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

    svg = SVG("#map").size("100%", "700").viewbox(0, 0, 1000, 700);

    // Initialize events

    $("#button-edit").click(function() {
        editMode(true);
    });
    $("#button-save").click(function() {
        saveMap();
        editMode(false);
        drawMap();
    });
    $("#button-cancel").click(function() {
        editMode(false);
        drawMap();
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
        $(".edit-mode").show();
        $(".view-mode").hide();
        $("#map").css("border-color", "#6eb16e");
    } else {
        $(".room").selectize(false)
        $(".edit-mode").hide();
        $(".view-mode").show();
        $("#map").css("border-color", "");
    }
}

function beforeDragEvent(e) {
    if (editModeEnabled == false) {
        e.preventDefault(); // Prevent drag
    }
}

function selectEntity(e) {
    if (editModeEnabled == false) {
        e.target.selectize();
    }
}

// --------------------------------------
// Draw
// --------------------------------------

function drawMap() {
    // Reset map
    //svg.clear();

    // Background

    if (svg.find(".bg-image").length <= 0) {
        var bgImage = svg.image("?bgimage").addClass("bg-image")
            .x(0)
            .y(0)
            .width("100%")
            .height("100%");
    }

    // Rooms

    if (data.rooms != null) {
        $.each(data.rooms, function(i, room) {
            svg.find("#room-" + room.id).remove();

            var group = svg.group();

            group.text(room.name).move(5, 5).fill('#999');
            group.rect(room.map.width, room.map.height).selectize();

            group.addClass("room")
                .attr("id", "room-" + room.id)
                .attr("room-id", room.id)
                .x(room.map.x)
                .y(room.map.y);

            group.draggable().on('beforedrag', beforeDragEvent);
            //group.on('mousedown', selectEntity, false);
        });
    }

    // Sensors

    if (data.sensors != null) {
        $.each(data.sensors, function(i, sensor) {
            svg.find("#sensor-" + sensor.id).remove();

            var group = svg.group();
            group.element('title').words(sensor.name);

            group.text(sensor.data.valueStr).move(45, 15).fill('#999');
            group.image("/img/temperature.svg").size(50, 50);

            group.addClass("sensor")
                .attr("id", "sensor-" + sensor.id)
                .attr("sensor-id", sensor.id)
                .x(sensor.map.x)
                .y(sensor.map.y);
            group.draggable().on('beforedrag', beforeDragEvent);
        });
    }

    // Events

    if (data.events != null) {
        $.each(data.events, function(i, event) {
            svg.find("#event-" + event.id).remove();

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
                .y(event.map.y);
            group.draggable().on('beforedrag', beforeDragEvent);
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
    svg.find(".room").each(function(){
        saveDevice(this, "room", "room-id");
    });
    svg.find(".sensor").each(function(){
        saveDevice(this, "sensor", "sensor-id");
    });
    svg.find(".event").each(function(){
        saveDevice(this, "event", "event-id");
    });
}
function saveDevice(element, type, id){
    $.ajax({
        async: false,
        dataType: "json",
        url: "/api/map?",
        data: {
            action: "save",
            id: element.attr(id),
            type: type,
            x: element.x(),
            y: element.y()
        },
    });
}
