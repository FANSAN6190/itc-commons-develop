package com.itc.commons.core.sling.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

@Model(
        adaptables = {Resource.class, SlingHttpServletRequest.class},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class BrandModel {

    private static final Logger log = LoggerFactory.getLogger(BrandModel.class);


    @ValueMapValue
    private String brandName;


    private static final String DEFAULT_BRAND_NAME = "default_brand";



    @PostConstruct
    protected void init() {
        if (brandName == null) {
            log.info("brand name is not authored");
        }
        log.info("Brand name initialized: {}", brandName != null ? brandName : DEFAULT_BRAND_NAME);
    }

    public String getBrandName() {
        log.info("brand name is: {}", brandName != null ? brandName : DEFAULT_BRAND_NAME);
        return brandName != null ? brandName : DEFAULT_BRAND_NAME;

    }
}
