// --------------------------------------------------------
//                      Autostart
// --------------------------------------------------------

$(function(){
    $(".toggle-switch").bootstrapSwitch({inverse: true, size: "mini"});

    $(".timestamp").relTimestamp();
});

// --------------------------------------------------------
// JQuery helper functions
// --------------------------------------------------------

// $.attr() # returns all attributes of an element
(function(old) {
    $.fn.attr = function() {
        if(arguments.length === 0) {
            if(this.length === 0) {
                return null;
            }

            var obj = {};
            $.each(this[0].attributes, function() {
                if(this.specified) {
                    obj[this.name] = this.value;
                }
            });
            return obj;
        }

        return old.apply(this, arguments);
    };
})($.fn.attr);

// --------------------------------------------------------
// Timestamps
// --------------------------------------------------------

// Converts all timestamps to human readable time and date
$.fn.relTimestamp = function() {
    return this.each(function() {
        var timestamp = parseInt($(this).text());
        var timestampNow = Date.now();
        var timeDiff = timestampNow - timestamp;

        if(timeDiff < 10 * 60 * 1000) // less than 10 min
            $(this).text(moment(timestamp).fromNow());
        else if(timeDiff < 24 * 60 * 60 * 1000) // less than 24 hours
            $(this).text(moment(timestamp).fromNow() + " ("+moment(timestamp).format("HH:mm")+")");
        else
            $(this).text(moment(timestamp).format("YYYY-MM-DD HH:mm"));
        return this;
    });
};

// --------------------------------------------------------
// Chart functions
// --------------------------------------------------------

function createChart(elementId, url, updateTime=-1){
    var tickConf = {count: 20};
    if (updateTime < 60*60*1000)
        tickConf['format'] = '%H:%M';
    else if (updateTime < 24*60*60*1000)
        tickConf['format'] = '%Y-%m-%d %H:%M';
    else
        tickConf['format'] = '%Y-%m-%d';


    var chart = c3.generate({
        bindto: elementId,
        data:  {json: []}, // set empty data, data will be loaded later
        axis : {
            x : {
                type : 'timeseries',
                label: 'Timestamp',
                tick: tickConf,
            },
            y: {
                label: 'Power (kWh)',
                min: 0,
            },
            y2: {
                show: true,
                label: 'Temperature (C)',
                min: 0,
            }
        },
        grid: {
            y: {show: true}
        },
        point: {
            show: false
        }
    });

    updateChart(chart, url, updateTime);
    $(window).focus(function(e) {
        updateChart(chart, url);
    });
}
function updateChart(chart, url, updateTime=-1){
    console.log('Updating chart: '+chart.element.id);
    $.getJSON(url, function(json){
        chart.load(getChartData(json));
    });
    if (updateTime > 0)
        setTimeout(function(){ updateChart(chart, url, updateTime); }, updateTime);
}
function getChartData(json){
    var dataXaxis = {};
    var dataYaxis = {};
    var data = [];
    var labels = [];
    json.forEach(function(sensor, i) {
        var index = 'data'+i;
        labels[index] = sensor.user +": "+ sensor.name;
        dataXaxis[index] = 'data'+i+'x';
        data.push([index+'x'].concat(sensor.timestamps));
        data.push([index].concat(sensor.data));

        if (sensor.type == "PowerConsumptionSensorData")
            dataYaxis[index] = 'y';
        else //if (sensor.type == "TemperatureSensorData")
            dataYaxis[index] = 'y2';
    });

    return {
        xs: dataXaxis,
        columns: data,
        names: labels,
        type: 'spline',
        axes: dataYaxis,
        unload: true,
    };
}

// --------------------------------------------------------
// Dynamic forms
// --------------------------------------------------------

var dynamicConf = {};

function initDynamicModalForm(modalId, formTemplateId = null, templateID = null){
    // read in all configurations into global variable (to skip naming issues)
    if (formTemplateId != null) {
        dynamicConf[formTemplateId] = [];
        $("#" + templateID + " div").each(function(){
            dynamicConf[formTemplateId][$(this).attr("id")] = $(this).html();
        });
        // Update dynamic inputs
        $("#" + modalId + " select[name=type]").change(function(){
            $("#" + modalId + " #" + formTemplateId).html(dynamicConf[formTemplateId][$(this).val()]);
        });
    }

    // click event
    $("#" + modalId).on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget);
        var modal = $(this);

        // Reset all inputs
        if (formTemplateId != null)
            modal.find("#" + formTemplateId).empty(); // clear form div

        // select dynamic form
        var selector = modal.find("select[name=type]");
        selector.val(button.data("type"));
        selector.change(); // Update dynamic inputs
        // set dynamic form data
        $.each(button.attr(), function(fieldName, value) {
            if(fieldName.startsWith("data-")) {
                fieldName = fieldName.substring(5);
                // case insensitive search
                input = modal.find("input").filter(function() {
                       return this.name.toLowerCase() == fieldName;
                });
                if (input.attr("type") == "checkbox") { // special handling for checkboxes
                    input.attr("value", "true");
                    if (value=="true") input.attr("checked", "true");
                    else               input.removeAttr("checked");
                    // Add default false value as a unchecked checkbox is not included in the post
                    input.parent().prepend("<input type='hidden' name='" + input.attr("name") + "' value='false' />");
                } else {
                    input.val(value);
                }
            }
        });
    });
}