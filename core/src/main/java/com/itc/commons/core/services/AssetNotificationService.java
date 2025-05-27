package com.itc.commons.core.services;

import org.apache.sling.api.resource.ResourceResolver;

public interface AssetNotificationService {
  void notifyNewAsset(String reviewerGroupName, String assetPath, ResourceResolver resourceResolver);
}
