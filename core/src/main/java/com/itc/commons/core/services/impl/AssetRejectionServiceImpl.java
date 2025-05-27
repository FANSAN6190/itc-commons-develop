package com.itc.commons.core.services.impl;
import com.day.cq.commons.Externalizer;
import com.itc.commons.core.listener.AssetAcceptRejectListener;
import com.itc.commons.core.services.AssetRejectionService;
import com.itc.commons.core.services.MailService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(AssetAcceptRejectListener.class);

    private static final String PROPERTY_APPROVAL_STATUS="approval";
    private static final String STATUS_REJECT="reject";
    private static final String DEFAULT_REVIEWER_GROUP="reviewer-0";

    private static final String STATUS_APPROVED_MSG = "no_action, the status is approved";
    private static final String ERROR_MISSING_INPUT = "error: Missing assetPath or reviewer";
    private static final String ERROR_ASSET_NOT_FOUND = "error: Asset not found at path: ";
    private static final String ERROR_PREFIX = "error: ";
    private static final String REVIEWER_GROUP_A = "itc_asset_reviewer_group_1";
    private static final String AGENCY_GROUP_A = "itc_asset_agency_group_1";
    private static final String NULL_ERROR_PREFIX="null pointer exception seen";

    @Reference
    private MailService emailService;

    @Reference
    private Externalizer externalizer;
    /**
     * Handles the rejection logic for a DAM asset.
     *
     * @param assetPath path of the DAM asset
     * @param reviewerGroup  ID of the reviewerGroup
     * @param resolver  resource resolver for accessing JCR content
     */
    @Override
    public void handleAssetRejection(String assetPath, String reviewerGroup, ResourceResolver resolver) throws RepositoryException {
        if (StringUtils.isBlank(assetPath) || StringUtils.isBlank(reviewerGroup)) {
            log.error(ERROR_MISSING_INPUT);
            throw new ItemNotFoundException(ERROR_MISSING_INPUT);
        }


        String EMAIL_SUBJECT_REJECTION = "Regarding rejection of asset by reviewer";
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

        String groupAgency = resolveGroupAgency(reviewerGroup);
        String AssetPath=assetPath.replace("/jcr:content","");
        String finalAssetPath=externalizer.authorLink(resolver,AssetPath);
        String message = "<p>Dear USER,</p>"
                .concat("<p>You are requested to review you asset as it has been rejected on path:</p>")
                .concat("<p> ").concat(finalAssetPath).concat("</p>")
                .concat("<p>Thank you<br/>AEM Reviewer Services</p>");
        try{
            emailService.sendEmail(groupAgency, resolver, message, EMAIL_SUBJECT_REJECTION);
            log.info("Email sent to group agency: {}", groupAgency);
        } catch (MessagingException e) {
            log.error("Email service not working : {}", String.valueOf(e));
        }
    }

    /**
     * Resolves the agency group based on reviewer.
     *
     * @param reviewerGroup the ID of the reviewer
     * @return the mapped agency group ID
     */
    private String resolveGroupAgency(String reviewerGroup) {
        if (REVIEWER_GROUP_A.equals(reviewerGroup)) {
            return AGENCY_GROUP_A;
        }
        return DEFAULT_REVIEWER_GROUP;
    }
}