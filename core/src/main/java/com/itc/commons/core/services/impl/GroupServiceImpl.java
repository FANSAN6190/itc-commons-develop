package com.itc.commons.core.services.impl;

import com.itc.commons.core.services.GroupService;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

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
  public boolean isValidAgencyGroup(String groupName, ResourceResolver resolver) throws RepositoryException {
    if (groupName == null || groupName.isBlank()) {
      LOG.warn("Group name is null or blank.");
      return false;
    }

    LOG.info("Checking if group exists with name: {}", groupName);

    UserManager userManager = resolver.adaptTo(UserManager.class);
    if (userManager == null) {
      LOG.error("UserManager is null. Cannot validate group.");
      throw new RepositoryException("UserManager is null.");
    }

    try {
      Authorizable authorizable = userManager.getAuthorizable(groupName);
      if (authorizable != null && authorizable.isGroup()) {
        LOG.info("Group found: {}", groupName);
        return true;
      } else {
        LOG.warn("Group not found or is not a group: {}", groupName);
        return false;
      }
    } catch (RepositoryException e) {
      LOG.error("Error while checking group existence for '{}'", groupName, e);
      throw e;
    }
  }
}