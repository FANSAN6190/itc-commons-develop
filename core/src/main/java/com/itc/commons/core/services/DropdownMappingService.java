package com.itc.commons.core.services;

import com.google.gson.JsonObject;
import javax.jcr.RepositoryException;
import org.apache.sling.api.resource.ResourceResolver;

public interface DropdownMappingService {
  JsonObject getDropdownMapping(ResourceResolver resolver) throws RepositoryException;
}
