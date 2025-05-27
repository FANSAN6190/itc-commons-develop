/*
This Js will set min and max limit in Multifield
usage:
    add granite:data node in Multifield, parellel to field node.
    add property min or max as string type in granite:data.
*/

function limitMultifield(){

$("button[coral-multifield-add]").click(function() {
    var field = $(this).parent();
    var size_max = field.attr("data-max");
    if (size_max) {
        var ui = $(window).adaptTo("foundation-ui");
		var totalLinkCount =  $(this).siblings("coral-multifield-item").length;
        if (totalLinkCount >= size_max) {
            ui.alert("Warning", "Maximum " + size_max + " items are supported!", "notice");
            return false;
        }
    }
});

$(".cq-dialog-submit").click(function() {
    var field = $("button[coral-multifield-add]").parent();
    var size_min = field.attr("data-min");
    if (size_min) {
        var ui = $(window).adaptTo("foundation-ui");
		    var totalLinkCount = $("coral-multifield-item").length;
        if (totalLinkCount < size_min) {
            ui.alert("Warning", "Minimum " + size_min + " items are required!", "notice");
            return false;
        }
    }
});
}

$(document).on("dialog-ready", limitMultifield);
