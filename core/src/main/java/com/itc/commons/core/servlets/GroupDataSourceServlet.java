package com.itc.commons.core.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.itc.commons.core.services.GroupService;
import com.itc.commons.core.services.MailService;
import com.itc.commons.core.services.impl.DamHierarchyCreatorServiceImpl;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.mail.MessagingException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static com.adobe.granite.rest.Constants.CT_JSON;
import static com.itc.commons.core.utils.GsonUtil.GSON;

/**
 * Return fetched groups as a JSON response
 */

@Component(
        service = Servlet.class,
        immediate = true,
        property = {
                Constants.SERVICE_DESCRIPTION + "=All AEM Groups DataSource",
                "sling.servlet.paths=/bin/groupdatasource",
                "sling.servlet.methods=GET, POST"
        }
)
public class GroupDataSourceServlet extends SlingAllMethodsServlet {

    private static final Logger logger = LoggerFactory.getLogger(GroupDataSourceServlet.class);

    @Reference
    private GroupService groupService;
    @Reference
    private MailService mailService;
    @Reference
    private DamHierarchyCreatorServiceImpl damHierarchyCreatorService;

    /**
     * @param request HTTP request to fetch groups
     * @param response HTTP response with available groups
     * @throws ServletException in case of servlet-level issues
     * @throws IOException if an input/output error occurs
     */

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        logger.info("Received POST request Form");

        try (ResourceResolver resourceResolver = request.getResourceResolver()) {

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }

            String jsonString = sb.toString();

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
            String groupName = jsonObject.get("group").getAsString().toLowerCase();
            logger.info("Group name received: {}", groupName);

            if (!groupService.isValidAgencyGroup(groupName, resourceResolver)) {
                logger.warn("Group '{}' is not valid or does not exist.", groupName);
                writeJsonResponse(response, Map.of("error", "This group "+groupName+ " doesn't exist, kindly create this group "), HttpServletResponse.SC_BAD_REQUEST);
                return;
            }



            String category = jsonObject.get("category").getAsString();
            String categoryDisplay = jsonObject.get("categoryDisplay").getAsString();
            String brand = jsonObject.get("brand").getAsString();
            String brandDisplay = jsonObject.get("brandDisplay").getAsString();
            String subBrand = jsonObject.get("subBrand").getAsString();
            String subBrandDisplay = jsonObject.get("subBrandDisplay").getAsString();
            String campaignName = jsonObject.get("campaignName").getAsString();
            String campaignDescription = jsonObject.get("campaignDescription").getAsString();
            String group = jsonObject.get("group").getAsString();


            String[] damNodes = {category, brand, subBrand, campaignName.toLowerCase().replace(" ", "-")};
            damHierarchyCreatorService.createNodeStructure(damNodes);

            String scheme = request.getScheme();
            String serverName = request.getServerName();
            String port = String.valueOf((request.getServerPort()));

            String path = "/assets.html/content/dam/itc/marketing-campaign/" + damNodes[0] + "/" + damNodes[1] + "/" + damNodes[2] + "/" + damNodes[3];
            damHierarchyCreatorService.setNodeProperty(path,"campaignDescription", "Campaign for specific need");

            String finalPath = scheme + "://" + serverName + ":" + port + path;

            String subject = brandDisplay + " | " + campaignName + " Creative Request";

            String message = "<p>Dear " + group + ",</p>"
                    .concat("<p>Campaign request for " + campaignName + " has been created with below description:<br>")
                    .concat(campaignDescription + "</p>")
                    .concat("<p>Please upload the asset (once available) to following path:<br>")
                    .concat("<strong>Asset Path:</strong> ").concat(finalPath).concat("</p>")
                    .concat("<p>Regards,<br>Digital Asset Management System</p>");

            mailService.sendEmail(group, resourceResolver, message, subject, true);
            writeJsonResponse(response, "Email sent to all users successfully", 200);
            logger.info("Email sent to all users successfully");
        } catch (LoginException | RepositoryException e) {
            logger.error("Failure in DAM Hierarchy creation service : {}", e.getMessage());
            writeJsonResponse(response, e.getMessage(), 500);
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Email service not working : {}", e.getMessage());
            writeJsonResponse(response, e.getMessage(), 500);
        }
    }


    /**
     * @param response HTTP response
     * @param result json payload on response
     * @param status Returns status code of response
     * @throws IOException if in
     */
    private void writeJsonResponse(SlingHttpServletResponse response, Object result, int status) throws IOException {
        response.setContentType(CT_JSON);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        try (PrintWriter out = response.getWriter()) {
            out.write(GSON.toJson(result));
        }
    }
}