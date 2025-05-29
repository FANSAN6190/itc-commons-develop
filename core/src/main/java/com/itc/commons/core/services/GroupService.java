package com.itc.commons.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;


public interface GroupService {
  public boolean isValidAgencyGroup(String groupName, ResourceResolver resolver) throws RepositoryException;
}