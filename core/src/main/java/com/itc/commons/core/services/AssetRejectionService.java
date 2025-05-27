package com.itc.commons.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

public interface AssetRejectionService {
    /**
     * Handles asset rejection and triggers email notification.
     *
     * @param assetPath        Path of the rejected DAM asset
     * @param reviewer         Reviewer ID
     * @param resolver         ResourceResolver from caller context
     * @return status message ("success", "no_action", or "error: message")
     */
    void handleAssetRejection(String assetPath, String reviewer, ResourceResolver resolver) throws RepositoryException;
}
