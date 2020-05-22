package com.ns8.hybris.addon.controllers;

import com.ns8.hybris.addon.facades.NS8TrueStatsFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Controller to surface to the FE the NS8 TrueStats javascript content
 */
@RestController
@RequestMapping("/ns8")
public class NS8TrueStatsController {

    @Resource
    private NS8TrueStatsFacade ns8TrueStatsFacade;

    /**
     * Returns the content of the NS8 TrueStats javascript
     *
     * @return TrueStats javascript content
     */
    @GetMapping(value = "/truestats")
    public String fetchTrueStatsScript() {
        return ns8TrueStatsFacade.fetchTrueStatsContent();
    }
}
