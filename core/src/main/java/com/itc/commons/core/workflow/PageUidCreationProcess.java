package com.itc.commons.core.workflow;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.itc.commons.core.constants.ApplicationConstants.PAGE_UID;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.wcm.api.Page;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a workflow process for adding the Page UID to pages.
 */
@Component(service = WorkflowProcess.class, property = {
  "process.label=ITC Commons - Addition of Page Uid Process"})
public class PageUidCreationProcess implements WorkflowProcess {

  private static final Logger LOGGER = LoggerFactory.getLogger(PageUidCreationProcess.class);
  @Reference
  private ResourceResolverFactory resolverFactory;

  /**
   * Executes the workflow process to add the Page UID.
   *
   * @param workItem        The current work item in the workflow.
   * @param workflowSession The current workflow session.
   * @param metaDataMap     Additional metadata map.
   * @throws WorkflowException If an error occurs during workflow execution.
   */
  @Override
  public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
    throws WorkflowException {
    WorkflowData workflowData = workItem.getWorkflowData();
    String path = workflowData.getPayload().toString();
    if (StringUtils.isNotEmpty(path)) {
      try (ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class)) {
        Resource pageResource = resolver.getResource(path);
        Resource contentResource = pageResource.adaptTo(Page.class).getContentResource();
        if (contentResource != null) {
          Node node = pageResource.getChild(JCR_CONTENT).adaptTo(Node.class);
          if (node != null) {
            node.setProperty(PAGE_UID, String.valueOf(pageResource.hashCode()));
            resolver.commit();
            LOGGER.debug("pageUid generated for: {}", path);
          }
        }
      } catch (RepositoryException | PersistenceException e) {
        LOGGER.error("Exception occurred while handling the resource change for path: {}", path, e);
      }
    }
  }
}