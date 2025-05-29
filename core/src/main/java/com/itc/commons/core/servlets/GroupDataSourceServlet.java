package com.itc.commons.core.servlets;

import static com.adobe.granite.rest.Constants.CT_JSON;
import static com.itc.commons.core.utils.GsonUtil.GSON;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.itc.commons.core.services.GroupService;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;

import com.itc.commons.core.services.MailService;
import com.itc.commons.core.services.impl.DamHierarchyCreatorServiceImpl;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

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

    private static final Logger LOG = LoggerFactory.getLogger(GroupDataSourceServlet.class);

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
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        try {
            List<Map<String, String>> groups = groupService.fetchGroups(request.getResourceResolver());
            writeJsonResponse(response, groups, HttpServletResponse.SC_OK);

        } catch (RepositoryException e) {
            LOG.error("Error while fetching group data: {}", e.getMessage(), e);
            writeJsonResponse(response,
                    Map.of("error", "Internal Server Error while fetching groups"),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     *
     * @param request HTTP request to post form data
     * @param response HTTP response after performing operation
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        LOG.info("Received POST request Form");

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

            String[] damNodes = {jsonObject.get("category").getAsString(), jsonObject.get("brand").getAsString(), jsonObject.get("subBrand").getAsString(), jsonObject.get("campaignName").getAsString().toLowerCase().replace(" ", "-")};
            damHierarchyCreatorService.createNodeStructure(damNodes);

            String scheme = request.getScheme();
            String serverName = request.getServerName();
            String port = String.valueOf((request.getServerPort()));
            String path = "/content/dam/itc/marketing-campaign/" + damNodes[0] + "/" + damNodes[1] + "/" + damNodes[2] + "/" + damNodes[3];
            String finalPath = scheme + "://" + serverName + ":" + port + path;

            String subject = "Add assets";

            String message = "<p>Dear USER,</p>"
                    .concat("<p>You are requested to add assets on the path given below:</p>")
                    .concat("<p> ").concat(finalPath).concat("</p>")
                    .concat("<p>Thank you<br/>AEM Asset Services</p>");


            try {
                mailService.sendEmail(jsonObject.get("group").getAsString(), resourceResolver, message, subject);
                writeJsonResponse(response, "Email sent to all users successfully", 200);
            }
            catch (MessagingException e) {
                writeJsonResponse(response, e.getMessage(), 500);
            }
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