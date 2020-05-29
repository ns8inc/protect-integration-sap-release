package com.ns8.hybris.addon.controllers;

import com.ns8.hybris.addon.facades.Ns8MerchantFacade;
import com.ns8.hybris.addon.facades.Ns8TrueStatsFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Controller to surface to the FE the NS8 TrueStats javascript content
 */
@RestController
@RequestMapping("/ns8")
public class Ns8TrueStatsController {

    @Resource
    private Ns8TrueStatsFacade ns8TrueStatsFacade;
    @Resource
    private Ns8MerchantFacade ns8MerchantFacade;

    /**
     * Returns the content of the NS8 TrueStats javascript if the merchant is active,
     * Bad request if merchant is not active
     *
     * @return TrueStats javascript content if merchant is active
     */
    @GetMapping(value = "/truestats")
    public ResponseEntity fetchTrueStatsScript() {
        if (ns8MerchantFacade.isMerchantActive()) {
            return ResponseEntity.ok(ns8TrueStatsFacade.fetchTrueStatsContent());
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
}
