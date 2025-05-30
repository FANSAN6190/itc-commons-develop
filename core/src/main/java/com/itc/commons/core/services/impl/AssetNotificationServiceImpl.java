package com.itc.commons.core.services.impl;

import com.day.cq.commons.Externalizer;
import com.itc.commons.core.services.AssetNotificationService;

import javax.mail.MessagingException;

import com.itc.commons.core.services.MailService;
import com.itc.commons.core.utils.CampaignPathParser;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(service = AssetNotificationService.class, immediate = true)
public class AssetNotificationServiceImpl implements AssetNotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AssetNotificationServiceImpl.class);

  @Reference
  private MailService mailService;

  @Reference
  private DamHierarchyCreatorServiceImpl damHierarchyCreatorService;

  @Reference
  private Externalizer externalizer;

  @Override
  public void notifyNewAsset(String reviewerGroupName, String assetPath, ResourceResolver resourceResolver) {
    if (assetPath == null || resourceResolver == null) {
      LOGGER.warn("Asset path or resource resolver is null, cannot send notification.");
      return;
    }

    String fullAssetUrl = externalizer.authorLink(resourceResolver, "/assets.html"+assetPath); // add assets.html to path
    LOGGER.info("Full asset URL: {}", fullAssetUrl);

    CampaignPathParser campaignPathParser = new CampaignPathParser(assetPath);
    String message = "<p>Dear Reviewer,</p>"
            + "<p>A new asset has been uploaded and is pending for review with below campaign details</p>"
            +"<p>Campaign request for "+campaignPathParser.getCampaign()+ " has been created with below description:<br>"
            + damHierarchyCreatorService.getNodeProperty(assetPath,"campaignDescription")
            +"</p><p>Please upload the asset (once available) to following path:"
            + "<strong>Asset Path:</strong> " + fullAssetUrl + "</p>"
            + "<p>Regards,<br/>Digital Asset Management System</p>";

    String subject = "New Asset Uploaded Notification";

    try {
      mailService.sendEmail(reviewerGroupName, resourceResolver, message, subject, true);
      LOGGER.info("Notification email sent successfully for asset path: {}", assetPath);
    }catch (MessagingException e) {
      LOGGER.error("Failed to send notification : {}", e.getMessage());
    }
  }
}