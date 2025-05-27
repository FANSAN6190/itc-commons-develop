/*
This Js checks whether the link is internal(ITC domain) i.e "/content/candyman/*" or external
returns 'true' if internal else 'false'
*/

"use strict";
use(function () {
    var link = this.url;
    var isInternalLink = false;
    if (link != null) {
        var linkSplit = link.split('/');
        if (link.startsWith("#") || (link.substr(0, 1) == "/" && linkSplit[1] == "content")) {
            isInternalLink = true;
        }
    }
    return isInternalLink;
});
