package com.itc.commons.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;


public interface GroupService {
  List<Map<String, String>> fetchGroups(ResourceResolver resolver) throws RepositoryException;
}