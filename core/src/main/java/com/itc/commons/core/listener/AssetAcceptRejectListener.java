package com.itc.commons.core.listener;

import com.itc.commons.core.util.CampaignPathParser;
import com.itc.commons.core.services.AssetRejectionService;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component(service = ResourceChangeListener.class,
        property = {
                ResourceChangeListener.PATHS + "=glob:/content/dam/itc/marketing-campaign/*/*/*/*/*/jcr:content",
                ResourceChangeListener.CHANGES + "=CHANGED",
                ResourceChangeListener.PROPERTY_NAMES_HINT + "=approval",
                ResourceChangeListener.PROPERTY_NAMES_HINT + "=review",
                ResourceChangeListener.PROPERTY_NAMES_HINT + "=sendto"
        })
public class AssetAcceptRejectListener implements ResourceChangeListener {

    @Reference
    AssetRejectionService assetRejectionService;

    @Reference

    ResourceResolverFactory resolverFactory;

    private static Logger log = LoggerFactory.getLogger(AssetAcceptRejectListener.class);

    @Override
    public void onChange(List<ResourceChange> changes) {
        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put(ResourceResolverFactory.SUBSERVICE, "asset-approval-service-user");
        try {
            ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(authInfo);
            for (ResourceChange change : changes) {
                String userId = changes.get(0).getUserId();
                log.info("User Id of Reviewer : {}", userId);

                String groupName = null;
                if (userId != null) {
                    UserManager userManager = resourceResolver.adaptTo(UserManager.class);
                    if (userManager != null) {
                        Authorizable user = userManager.getAuthorizable(userId);
                        if (user != null) {
                            Iterator<Group> groups = user.memberOf();
                            String groupNameFromPath = new CampaignPathParser(changes.get(0).getPath()).getReviewerGroupName();
                            while (groups.hasNext()) {
                                if(groupNameFromPath.equals(groups.next().getID())) {
                                    groupName = groupNameFromPath;
                                    log.info("Group Id of Reviewer : {}", groupName);
                                }
                            }
                        }
                    }
                } else {
                    log.error("Can not able to get User ID of reviewer");
                }
                String path = change.getPath();
                log.info("Asset Path : {}", path);
                Resource resource = resourceResolver.getResource(path);
                if (resource != null) {
                    String status = (String) resource.getValueMap().get("approval");
                    if(status.equals("reject")){
                        String review = (String) resource.getValueMap().get("review");
                        String sendTo = (String) resource.getValueMap().get("sendto");
                        if(sendTo.equals("group")){
                            assetRejectionService.handleAssetRejectionToGroup(path,review,resourceResolver, groupName);
                        } else if(sendTo.equals("single")) {
                            String userId_value = resource.getParent().getValueMap().get("jcr:createdBy").toString();
                            assetRejectionService.handleAssetRejectionToUser(path,review,resourceResolver, userId_value);
                        }

                    } else if(status.equals("accept")){
                        log.info("Asset Approved");
                    } else {
                        log.error("Invalid field value for approval property");
                    }
                }
            }
        } catch (LoginException e) {
            log.error("Service User Error : {}", e.getMessage());
        } catch (RepositoryException e){
            log.error("Error while handling Rejection service : {}", e.getMessage());
        }catch (RuntimeException e) {
            log.error("Unknown Error Occurred : {}", e.getMessage());
        }

    }
}
