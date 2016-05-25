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
