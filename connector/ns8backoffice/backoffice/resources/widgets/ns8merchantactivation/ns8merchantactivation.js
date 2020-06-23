function ns8ActivateValidityCheck() {
    var errors = false;
    $('.yw-com_ns8_hybris_backoffice_cmssite_widgets_ns8merchantactivation input').each(function(){
        var $this = $(this);
        var val = $this.val();
        var id = $this.attr('ytestid');

        if(val === "") errors = true;
        if(id === "storeUrl" && !isValidHttpsUrl(val)) errors = true;
        if(id === "email" && !isValidEmail(val)) errors = true;
    });

    $('.yw-com_ns8_hybris_backoffice_cmssite_widgets_ns8merchantactivation .merchant-activation-button').prop('disabled', errors);
}

function isValidHttpsUrl(string) {
    var url;

    try {
        url = new URL(string);
    } catch (ex) {
        return false;
    }

    return url.protocol === "https:";
}

function isValidEmail(string) {
    var EMAIL_REGEX = /^(([^<>()\[\]\.,;:\s@\"]+(\.[^<>()\[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()[\]\.,;:\s@\"]+\.)+[^<>()[\]\.,;:\s@\"]{2,})$/i;
    return EMAIL_REGEX.test(string);
}

zk.afterMount(function () {
    ns8ActivateValidityCheck();

    $('.yw-com_ns8_hybris_backoffice_cmssite_widgets_ns8merchantactivation input').on('change keyup', function(){
        ns8ActivateValidityCheck();
    });
});
