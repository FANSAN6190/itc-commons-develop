package com.itc.commons.core.services.impl;
import com.day.cq.commons.Externalizer;
import com.itc.commons.core.classVariable.CampaignPathParser;
import com.itc.commons.core.services.AssetRejectionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.itc.commons.core.services.MailService;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.mail.MessagingException;
import java.io.Externalizable;

/**
 * Service implementation to handle DAM asset rejection.
 * This service validates the asset's approval status, resolves the correct group,
 * and sends a rejection email using the MailService.
 */
@Component(service = AssetRejectionService.class, immediate = true)
public class AssetRejectionServiceImpl implements AssetRejectionService {

    private static final Logger log = LoggerFactory.getLogger(AssetRejectionServiceImpl.class);

    private static final String PROPERTY_APPROVAL_STATUS = "approval";
    private static final String STATUS_REJECT = "reject";
    private static final String DEFAULT_REVIEWER_GROUP = "reviewer-0";

    private static final String STATUS_APPROVED_MSG = "no_action, the status is approved";
    private static final String ERROR_MISSING_INPUT = "error: Missing assetPath or reviewer";
    private static final String ERROR_ASSET_NOT_FOUND = "error: Asset not found at path: ";

    @Reference
    private MailService emailService;

    @Reference
    private Externalizer externalizer;

    /**
     * Method to make message draft for email
     * @param agencyName name of agency user or agency group
     * @param campaignName name of campaign
     * @param campaignDescription reason for removal of rejection
     * @param finalAssetPath the path of dam with localhost and everything
     */
    String getMessageForEmail(String agencyName,String campaignName,String campaignDescription,String finalAssetPath)
    {
        String message="<p>Dear ".concat(agencyName).concat("</p>")
                .concat("<p>You are requested to review you asset as it has been rejected on path:<br>")
                .concat("Campaign request for ").concat(campaignName).concat(" has been Rejected:</p>")
                .concat("<p>").concat(campaignDescription).concat("</p>")
                .concat("<p>").concat("<p>Please upload the asset (once available) with required changes to following path: <br>")
                .concat(finalAssetPath).concat("</p>")
                .concat("<p>Regards<br>Digital Asset Management System</p>");
        log.info("Message for email is {}:",message);
        return message;
    }

    /**
     * Property checks for status approval and null checks
     *
     * @param assetPath     path of the DAM asset
     * @param agency  ID of the reviewerGroup
     * @param resolver      resource resolver for accessing JCR content
     */
    void propertyCheck(String assetPath, ResourceResolver resolver,String agency) throws RepositoryException {
        if (StringUtils.isBlank(assetPath) || StringUtils.isBlank(agency)) {
            log.error(ERROR_MISSING_INPUT+" from user call");
            throw new ItemNotFoundException(ERROR_MISSING_INPUT);
        }

        Resource assetResource = resolver.getResource(assetPath);
        if (assetResource == null || assetResource.adaptTo(Node.class) == null) {
            log.error("Asset not found at provided path: {}", assetPath);
            throw new ItemNotFoundException(ERROR_ASSET_NOT_FOUND + assetPath);
        }

        Node assetNode = assetResource.adaptTo(Node.class);
        if (assetNode == null || !assetNode.hasProperty(PROPERTY_APPROVAL_STATUS)) {
            log.warn("Approval status property not found on asset: {}", assetPath);
            throw new NullPointerException(STATUS_APPROVED_MSG);
        }

        String approvalStatus = assetNode.getProperty(PROPERTY_APPROVAL_STATUS).getString();
        if (!STATUS_REJECT.equalsIgnoreCase(approvalStatus)) {
            log.error("Asset approval status is not rejected: {}", approvalStatus);
        }
    }

    /**
     * Handles the rejection logic for a DAM asset.
     *
     * @param assetPath     path of the DAM asset
     * @param campaignDescription description for why asset got rejected
     * @param resolver      resource resolver for accessing JCR content
     * @param reviewerGroup ID of the reviewerGroup
     */
    @Override
    public void handleAssetRejectionToGroup(String assetPath, String campaignDescription, ResourceResolver resolver, String reviewerGroup) throws RepositoryException {
        propertyCheck(assetPath,resolver,reviewerGroup);
        String AssetCleanedPath = assetPath.replace("/jcr:content", "");
        CampaignPathParser campaignPathParser=new CampaignPathParser(AssetCleanedPath);
        String brandName=campaignPathParser.getBrand();
        String campaignName=campaignPathParser.getCampaign();

        boolean isGroup=true;
        String EMAIL_SUBJECT_REJECTION = brandName.concat(" | ").concat(campaignName).concat(" creative Request Rejected");

        String groupAgency = resolveGroupAgency(reviewerGroup);

        String finalAssetPath = externalizer.authorLink(resolver, AssetCleanedPath);

        String message = getMessageForEmail(groupAgency,campaignName,campaignDescription,finalAssetPath);
        try {
            emailService.sendEmail(groupAgency, resolver, message, EMAIL_SUBJECT_REJECTION,isGroup);
            log.info("Email sent to group agency: {}", groupAgency);
        } catch (MessagingException e) {
            log.error("Email service not working for handleAssetRejectionGroup: {}", String.valueOf(e));
        }
    }

    /**
     * Handles the rejection logic for a DAM asset and send to user only.
     *
     * @param assetPath     path of the DAM asset
     * @param campaignDescription description for why asset got rejected
     * @param resolver      resource resolver for accessing JCR content
     * @param agencyUser ID of the agencyUser
     */
    @Override
    public void handleAssetRejectionToUser(String assetPath, String campaignDescription, ResourceResolver resolver,String agencyUser) throws RepositoryException {

        String CleanedPath = assetPath.replace("/jcr:content",
                "");
        String AssetCleanedPath = "/assets.html".concat(CleanedPath);

        CampaignPathParser campaignPathParser=new CampaignPathParser(AssetCleanedPath);
        String brandName=campaignPathParser.getBrand();
        String campaignName=campaignPathParser.getCampaign();

        String EMAIL_SUBJECT_REJECTION = brandName.concat(" | ").concat(campaignName).concat(" creative Request Rejected");

        propertyCheck(assetPath,resolver,agencyUser);
        String finalAssetPath = externalizer.authorLink(resolver, AssetCleanedPath);
        boolean isGroup=false;
        String message = getMessageForEmail(agencyUser,campaignName,campaignDescription,finalAssetPath);
        try {
            emailService.sendEmail(agencyUser, resolver, message, EMAIL_SUBJECT_REJECTION,isGroup);
            log.info("Email sent to user agency: {}", agencyUser);
        } catch (MessagingException e) {
            log.error("Email service not working for handleAssetRejectionUser : {}", String.valueOf(e));
        }
    }

    /**
     * Resolves the agency group based on reviewer.
     *
     * @param reviewerGroup the ID of the reviewer
     * @return the mapped agency group ID
     */
    private String resolveGroupAgency(String reviewerGroup) {
        if (reviewerGroup.contains("_reviewer_")) {
            return reviewerGroup.replace("_reviewer_", "_agency_");
        } else {
            log.error("reviewer name convention is invalid, {}",reviewerGroup);
        }
        return DEFAULT_REVIEWER_GROUP;
    }
}

