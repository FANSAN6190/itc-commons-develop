package com.itc.commons.core.services.impl;

import com.itc.commons.core.services.AssetNotificationService;
import com.itc.commons.core.services.MailService;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;

@Component(service = AssetNotificationService.class, immediate = true)
public class AssetNotificationServiceImpl implements AssetNotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AssetNotificationServiceImpl.class);

  @Reference
  private MailService mailService;

  @Override
  public void notifyNewAsset(String reviewerGroupName, String assetPath, ResourceResolver resourceResolver) {
    if (assetPath == null || resourceResolver == null) {
      LOGGER.warn("Asset path or resource resolver is null, cannot send notification.");
      return;
    }

    String message = "<p>Dear " + reviewerGroupName + ",</p>"
            + "<p>A new asset has been uploaded and is pending for review:<br>"
            + "<strong>Asset Path:</strong> " + assetPath + "</p>"
            + "<p>Regards,<br>Digital Asset Management System</p>";

    String subject = "New Asset Uploaded Notification";

    try {
      mailService.sendEmail(reviewerGroupName, resourceResolver, message, subject, true);
      LOGGER.info("Notification email sent successfully for asset path: {}", assetPath);
    }catch (MessagingException e) {
      LOGGER.error("Failed to send notification : {}", e.getMessage());
    }
  }
}