package com.itc.commons.core.services.impl;

import com.itc.commons.core.services.GroupService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of GroupService Interface
 * Fetches all the valid groups
 */
@Component(service = GroupService.class)
public class GroupServiceImpl implements GroupService {

  private static final Logger LOG = LoggerFactory.getLogger(GroupServiceImpl.class);

  /**
   *
   * @param resolver
   * @return
   * @throws RepositoryException
   */
  @Override
  public List<Map<String, String>> fetchGroups(ResourceResolver resolver) throws RepositoryException {
    List<Map<String, String>> groups = new ArrayList<>();
    UserManager userManager = resolver.adaptTo(UserManager.class);

    if (userManager == null) {
      LOG.info("UserManager is null. Cannot fetch groups.");
      throw new RepositoryException("UserManager is null.");
    }

    Iterator<Authorizable> allAuthorizables = userManager.findAuthorizables("rep:authorizableId", null);

    while (allAuthorizables.hasNext()) {
      Authorizable authorizable = allAuthorizables.next();
      if (authorizable.isGroup()) {
        String groupId = authorizable.getID();
        if (isValidGroup(groupId)) {
          Map<String, String> groupMap = new HashMap<>();
          groupMap.put("value", groupId.toUpperCase());
          groupMap.put("text", groupId.toUpperCase());
          groups.add(groupMap);
        }
      }
    }

    return groups;
  }

  /**
   *
   * @param groupId
   * @return
   */
  private boolean isValidGroup(String groupId) {
    return groupId != null && groupId.toLowerCase().startsWith("itc") && groupId.toLowerCase().contains("agency");
  }
}