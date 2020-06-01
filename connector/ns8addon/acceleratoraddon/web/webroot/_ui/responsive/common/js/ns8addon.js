ACC.ns8addon = {
    _autoload: [
        "ns8Load"
    ],

    ns8Load: function () {
        var ns8ScriptUrl = ACC.config.encodedContextPath + "/ns8/truestats";
        $.ajax({
            url: ns8ScriptUrl,
            dataType: "script",
            headers: {
                "NS8-Screen-Height": window.innerHeight,
                "NS8-Screen-Width": window.innerWidth
            },
            error: function () {
                console.warn("Merchant is not active");
            }
        });
    }
};
