package com.itc.commons.core.servlets;

import com.google.gson.JsonObject;
import com.itc.commons.core.services.DropdownMappingService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.io.IOException;
import java.io.PrintWriter;

import static com.adobe.granite.rest.Constants.CT_JSON;
import static com.itc.commons.core.utils.GsonUtil.GSON;

@Component(
  service = Servlet.class,
  immediate = true,
  property = {
    Constants.SERVICE_DESCRIPTION + "=Populate dropdown mapping from jcr.",
    "sling.servlet.paths=/bin/populateDropdownMapping",
    "sling.servlet.methods=GET"
  }
)
public class PopulateDropdownMappingServlet extends SlingSafeMethodsServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger(PopulateDropdownMappingServlet.class);

  @Reference
  private DropdownMappingService dropdownMappingService;

  @Override
  protected void doGet(SlingHttpServletRequest req, SlingHttpServletResponse resp) throws IOException {

    try {
      JsonObject result = dropdownMappingService.getDropdownMapping(req.getResourceResolver());
      LOGGER.info("Dropdown Mapping fetched.");

      writeJsonResponse(resp, result, 200);

    } catch(RepositoryException e)
    {
      LOGGER.error("Unable to fetch DropdownMapping from jcr: {}", e.getMessage());
      writeJsonResponse(resp, e.getMessage(), 500);
    }

  }

  /**
   * @param response HTTP response
   * @param result json object on response
   * @param status Returns status code of response
   * @throws IOException if in
   */
  private void writeJsonResponse(SlingHttpServletResponse response, Object result, int status) throws IOException {
    response.setContentType(CT_JSON);
    response.setCharacterEncoding("UTF-8");
    response.setStatus(status);
    try (PrintWriter out = response.getWriter()) {
      out.write(result != null ? GSON.toJson(result) : "{}");
    }
  }
}
