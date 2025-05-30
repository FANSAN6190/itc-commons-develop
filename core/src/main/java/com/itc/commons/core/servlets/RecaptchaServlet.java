package com.itc.commons.core.servlets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.itc.commons.core.services.RecaptchaService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

import static com.itc.commons.core.constants.ApplicationConstants.CHARACTER_ENCODING_UTF_8;
import static com.itc.commons.core.constants.ApplicationConstants.CONTENT_TYPE_APPLICATION_JSON;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_POST;
import static org.apache.sling.api.servlets.ServletResolverConstants.*;

@Component(service = Servlet.class, property = {
  Constants.SERVICE_DESCRIPTION + "= Recaptcha Verification Servlet",
  SLING_SERVLET_METHODS + "=" + METHOD_POST,
  SLING_SERVLET_RESOURCE_TYPES + "=" + "sling/servlet/default",
  SLING_SERVLET_SELECTORS + "=" + "verifyRecaptcha",
  SLING_SERVLET_EXTENSIONS + "=" + "json"
})
public class RecaptchaServlet extends SlingAllMethodsServlet {

  @Reference
  private transient RecaptchaService recaptchaService;

  private static final Logger LOGGER = LoggerFactory.getLogger(RecaptchaServlet.class);

  @Override
  protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
    response.setCharacterEncoding(CHARACTER_ENCODING_UTF_8);
    response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
    StringBuilder stringBuilder = new StringBuilder();
    try {
      String line;
      BufferedReader reader = request.getReader();
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
      }
      JsonObject jsonObject = JsonParser.parseString(stringBuilder.toString()).getAsJsonObject();
      String token = jsonObject.get("token").getAsString();
      if (StringUtils.isEmpty(token)) {
        response.sendError(400, "Request Parameter is empty");
        return;
      }
      boolean status = recaptchaService.verifyRecaptcha(token);
      if (status) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Recaptcha verified");
      } else {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write("An error occurred while verifying recaptcha");
      }
    }catch (IOException | JsonSyntaxException e){
      LOGGER.error(e.getMessage(), e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write("An error occurred while verifying recaptcha");
    }
  }
}
