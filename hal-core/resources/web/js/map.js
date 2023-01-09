var svg;
var editModeEnabled = false;

$(function(){
    if (!SVG.supported) {
        alert("Image format(SVG) not supported by your browser.");
        return;
    }

    // ------------------------------------------
    // Setup map
    // ------------------------------------------

    svg = SVG("map");

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
                $("#bg-file-progress").css("width", t+"%");
            },
        }
    });

    // ------------------------------------------
    // Start draw loop
    // ------------------------------------------

    setInterval(function() {
        if (editModeEnabled == false)
            drawMap();
    }, 3000); // 3 sec

});


function editMode(enable){
    if (editModeEnabled == enable)
        return;
    editModeEnabled = enable;
    if (editModeEnabled){
        $(".edit-mode").show();
        $(".view-mode").hide();
        $("#map").css("border-color", "#6eb16e");
        svg.select(".draggable").draggable(true);
    }
    else {
        $(".edit-mode").hide();
        $(".view-mode").show();
        $("#map").css("border-color", "");
        svg.select(".draggable").draggable(false);
    }
}

function drawMap(){
    // Get map data
    $.getJSON("/api/map?action=getdata", function(json){
        // Reset map
        svg.clear();

        // --------------------------------------
        // Draw
        // --------------------------------------

        // Background

        if (SVG.find("svg .bg-image").length > 0) {
            var bg_image = svg.image("?bgimage").addClass("bg-image")
                .x(0)
                .y(0)
                .width("100%")
                .height("100%");
        }

        // Rooms

        if (json.rooms != null) {
            $.each(json.rooms, function(i, room) {
                var group = svg.group();

                group.rect(room.width, room.height);
                group.text(room.name).move(10, 10).fill('#999');

                group.addClass("draggable").addClass("room")
                    .x(room.x)
                    .y(room.y)
                    .attr("room-id", room.id);
            });
        }

        // Sensors

        if (json.sensors != null) {
            $.each(json.sensors, function(i, sensor) {
                var group = svg.group();

                group.image("/img/temperature.svg");
                group.text(sensor.data).move(45, 15).fill('#999');

                group.addClass("draggable").addClass("sensor")
                    .x(sensor.x)
                    .y(sensor.y)
                    .attr("sensor-id", sensor.id);
                group.title(sensor.name);
            });
        }

        // Events

        if (json.event != null) {
            $.each(json.events, function(i, event) {
                var group = svg.group();

                var img = "/img/lightbulb_off.svg";
                if (event.data == "ON")
                    img = "/img/lightbulb_on.svg";
                group.image(img);

                group.addClass("draggable").addClass("event")
                    .x(event.x)
                    .y(event.y)
                    .attr("event-id", event.id);
                group.title(event.name);
            });
        }
    });
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