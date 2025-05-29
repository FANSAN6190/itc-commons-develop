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

    /**
     * Getter to return brand name
     * @return brand name as String
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Getter to return Sub brand name
     * @return Sub brand name as String
     */
    public String getSubBrand() {
        return subBrand;
    }

    /**
     * Getter to return campaign name
     * @return Campaign name as String
     */
    public String getCampaign() {
        return campaign;
    }

    /**
     * Getter to return agency group name
     * @return group name as String in format "brand-subBrand-agency-group"
     */
    public String getAgencyGroupName()
    {
        return brand.concat("-").concat(subBrand).concat("-agency-group");
    }

    /**
     * Getter to return reviewer group name
     * @return group name as String in format "brand-subBrand-reviewer-group"
     */
    public String getReviewerGroupName()
    {
        return brand.concat("-").concat(subBrand).concat("-reviewer-group");
    }
}
