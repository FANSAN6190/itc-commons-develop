package com.itc.commons.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;

public interface AssetRejectionService {

    /**
     * Handles asset rejection and triggers email notification to user agency
     *
     * @param assetPath         Path of the rejected DAM asset
     * @param campaignDescription  Reason for rejection
     * @param resolver          ResourceResolver from caller context
     * @param reviewerGroup     Reviewer ID
     */
    void handleAssetRejectionToGroup(String assetPath, String campaignDescription, ResourceResolver resolver, String reviewerGroup) throws RepositoryException;

    /**
     * Handles asset rejection and triggers email notification to agency user
     *
     * @param assetPath         Path of the rejected DAM asset
     * @param campaignDescription  Reason for rejection
     * @param resolver          ResourceResolver from caller context
     * @param agencyUser        The user who uploaded the asset
     */
    void handleAssetRejectionToUser(String assetPath, String campaignDescription, ResourceResolver resolver, String agencyUser) throws RepositoryException;
}
