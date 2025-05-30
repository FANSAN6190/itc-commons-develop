package com.itc.commons.core.servlets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itc.commons.core.services.S3FileUploadService;
import com.itc.commons.core.sling.models.GenericFormFieldsModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
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
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;

import static com.itc.commons.core.utils.GsonUtil.GSON;
import static org.apache.sling.api.servlets.ServletResolverConstants.*;

@Component(service = Servlet.class, property = {
  Constants.SERVICE_DESCRIPTION + "= Campaign Data Upload Servlet",
  SLING_SERVLET_SELECTORS + "=uploadCampaignData",
  SLING_SERVLET_RESOURCE_TYPES + "=sling/servlet/default",
  SLING_SERVLET_EXTENSIONS + "=json",
  SLING_SERVLET_METHODS + "=POST"
})
public class GenericCampaignFormServlet extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericCampaignFormServlet.class);

    @Reference
    private transient S3FileUploadService s3FileUploadService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        LOGGER.info("Received POST request for campaign data upload.");

        try {
            Resource resource = request.getResource();
            GenericFormFieldsModel formModel = resource.adaptTo(GenericFormFieldsModel.class);

            if (formModel == null) {
                LOGGER.error("Failed to adapt resource to GenericFormFieldsModel.");
                writeJsonResponse(response, Map.of("success", false, "message", "Could not initialize form model"), HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            JsonObject jsonObject = JsonParser.parseString(stringBuilder.toString()).getAsJsonObject();
            if (Objects.isNull(jsonObject)) {
                LOGGER.error("Invalid request: Missing JSON data.");
                writeJsonResponse(response, Map.of("success", false, "message", "Invalid request: Missing JSON data."), HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String brandName = formModel.getBrandName();
            String campaignName = formModel.getCampaignName();
            LOGGER.info("Extracted brandName: {}, campaignName: {}", brandName, campaignName);

            if (StringUtils.isEmpty(brandName) || StringUtils.isEmpty(campaignName)) {
                LOGGER.error("Invalid request: Missing brandName or campaignName.");
                writeJsonResponse(response, Map.of("success", false, "message", "Missing brandName or campaignName"), HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String signedUrl = s3FileUploadService.updateFileInS3(
              jsonObject, brandName, campaignName, formModel.getFieldLabels()
            );

            if (StringUtils.isNotEmpty(signedUrl)) {
                LOGGER.info("File uploaded successfully. Signed URL: {}", signedUrl);
                writeJsonResponse(response, Map.of("success", true, "url", signedUrl), HttpServletResponse.SC_OK);
            } else {
                LOGGER.error("File upload failed.");
                writeJsonResponse(response, Map.of("success", false, "message", "Failed to upload the file."), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            LOGGER.error("Error processing the request: {}", e.getMessage(), e);
            writeJsonResponse(response, Map.of("success", false, "message", "Internal server error."), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void writeJsonResponse(SlingHttpServletResponse response, Object result, int status) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        try (PrintWriter out = response.getWriter()) {
            out.write(GSON.toJson(result));
        }
    }
}
