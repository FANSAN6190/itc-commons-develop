package com.itc.commons.core.utils;

public class CampaignPathParser {

    private final String brand;
    private final String subBrand;
    private final String campaign;

    public CampaignPathParser(String assetPath) {
        String[] parts = assetPath.split("/");
        if (parts.length >= 9) {
            this.brand = parts[6];
            this.subBrand = parts[7];
            this.campaign = parts[8];
        } else {
            this.brand = "";
            this.subBrand = "";
            this.campaign = "";
        }
    }

    public String getBrand() {
        return brand;
    }

    public String getSubBrand() {
        return subBrand;
    }

    public String getCampaign() {
        return campaign;
    }

    public String getAgencyGroupName()
    {
        return brand.concat("-").concat(subBrand).concat("-agency-group");
    }

    public String getReviewerGroupName()
    {
        return brand.concat("-").concat(subBrand).concat("-reviewer-group");
    }
}
