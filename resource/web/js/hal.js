///////////////////////////////// Autostart
$(function(){
    $(".toggle-switch").bootstrapSwitch();

    $(".timestamp").relTimestamp();
});

////////////////////////////////////// JQuery functions

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

// converts all timestamps to human readable time and date
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


////////////////////////////////////// Hal functions

////////////// Chart functions
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

    updateChart(chart, url, updateTime);;
}
function updateChart(chart, url, updateTime){
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
    };
}