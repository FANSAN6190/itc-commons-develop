package com.itc.commons.core.listener;

import com.day.cq.commons.Externalizer;
import com.itc.commons.core.services.AssetNotificationService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.itc.commons.core.util.CampaignPathParser;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener to detect new asset uploads under /content/dam.
 * Uses a system user 'assetListener' to obtain ResourceResolver if needed.
 */
@Component(
        service = ResourceChangeListener.class,
        immediate = true,
        property = {
                ResourceChangeListener.PATHS+"=/content/dam/itc/marketing-campaign",
                ResourceChangeListener.CHANGES+"=ADDED"
        }
)
@ServiceDescription("Asset upload listener for /content/dam/itc/marketing-campaign")
public class CustomEventListener implements ResourceChangeListener {

    Map<String, String> reviewerMap = new HashMap<>();

    @Reference
    private AssetNotificationService assetNotificationService;

    private static final Logger logger = LoggerFactory.getLogger(CustomEventListener.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private Externalizer externalizer;

    private static final String SUBSERVICE_NAME = "asset-approval-service-user";

    @Override
    public void onChange(List<ResourceChange> changes) {
        reviewerMap.put("Biscuits-Sunfeast-agency-group","Biscuits-Sunfeast-reviewer-group");
        Map<String, Object> params = new HashMap<>();
        params.put(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);

        try (ResourceResolver resolver = resourceResolverFactory.getServiceResourceResolver(params)) {
            for (ResourceChange change : changes) {
                String path = change.getPath();


                if (change.getType() == ResourceChange.ChangeType.ADDED && path.endsWith("/jcr:content/metadata")) {
                    String assetPath = path.replace("/jcr:content/metadata", "");
                    logger.info("New asset uploaded at: {}", assetPath);
                    String fullAssetUrl = externalizer.authorLink(resolver, assetPath);
                    logger.info("Full asset URL: {}", fullAssetUrl);
                    String userId = change.getUserId();
                    logger.info("User Id of Uploader : {}", userId);
                    if (userId != null) {
                        UserManager userManager = resolver.adaptTo(UserManager.class);
                        if (userManager != null) {
                            Authorizable user = userManager.getAuthorizable(userId);
                            if (user != null) {
                                Iterator<Group> groups = user.memberOf();

                                String groupNameFromPath = new CampaignPathParser(assetPath).getAgencyGroupName();
                                while (groups.hasNext()) {
                                    String groupName = groups.next().getID();
                                    if(groupNameFromPath.equals(groupName)) {
                                        assetNotificationService.notifyNewAsset(groupNameFromPath.replace("-agency-","-reviewer-"), fullAssetUrl, resolver);
                                        logger.info("Group Id of Agency Asset Uploader : {}", groupNameFromPath);
                                    }
                                }
                            }
                        }
                    } else {
                        throw new RuntimeException("Can not able to get User ID of user.");
                    }

                    Resource assetResource = resolver.getResource(assetPath);
                    if (assetResource != null) {
                        logger.debug("Asset resource found at {}", assetResource.getPath());
                    } else {
                        logger.warn("Asset resource not found at {}", assetPath);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error obtaining ResourceResolver or processing resource changes", e);
        }
    }
}