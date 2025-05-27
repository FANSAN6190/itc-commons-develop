/*
This Js checks whether the link is internal(ITC domain) i.e "/content/candyman/*" or external
returns if true: valid Link with .html extension
        if false: the input url as it is.
*/

"use strict";
use(function () {
    var link = this.url;
    var validLink = link;
    if (link != null) {
        var linkSplit = link.split('/');
        //no need to check if valid link starts with #. return link as it is.
        if (link.substr(0, 1) == "/" && linkSplit[1] == "content") {
            validLink = validLink + ".html";
        }
    }
    return validLink;
});
