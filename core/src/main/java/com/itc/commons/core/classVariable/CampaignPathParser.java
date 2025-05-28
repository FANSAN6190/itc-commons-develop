package com.itc.commons.core.classVariable;

public class CampaignPathParser {

    private final String brand;
    private final String subBrand;
    private final String campaign;

    public CampaignPathParser(String assetPath) {
        String[] parts = assetPath.split("/");
        if (parts.length >= 8) {
            this.brand = parts[5];
            this.subBrand = parts[6];
            this.campaign = parts[7];
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
        return brand.concat("_").concat(subBrand).concat("_agency_group");
    }

    public String getReviewerGroupName()
    {
        return brand.concat("_").concat(subBrand).concat("_reviewer_group");
    }
}
