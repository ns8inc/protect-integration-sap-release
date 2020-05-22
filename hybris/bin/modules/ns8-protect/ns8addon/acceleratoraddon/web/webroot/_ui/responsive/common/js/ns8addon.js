ACC.ns8addon = {
    _autoload: [
        "ns8Load"
    ],

    ns8Load: function () {
        var ns8ScriptUrl = ACC.config.encodedContextPath + "/ns8/truestats";
        $.getScript(ns8ScriptUrl);
    }
};
