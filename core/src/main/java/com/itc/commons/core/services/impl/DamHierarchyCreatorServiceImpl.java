package com.itc.commons.core.services.impl;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component(service = DamHierarchyCreatorServiceImpl.class, immediate = true)
public class DamHierarchyCreatorServiceImpl {

    @Reference
    ResourceResolverFactory resolverFactory;


    private static final Logger log = LoggerFactory.getLogger(DamHierarchyCreatorServiceImpl.class);


    String[] nodesInStructure = {"itc", "marketing-campaign"};
    String PARENT_NODE_PATH = "/content/dam";

    @Activate
    public void activate(){
        try {
            Session session =initSession();
            if(session!=null) {
                createNodeStructure(nodesInStructure);
                PARENT_NODE_PATH= PARENT_NODE_PATH+"/"+nodesInStructure[0]+"/"+nodesInStructure[1];
                log.info("Initial Dam Structure Created");
                session.save();
            }
        } catch (LoginException | RepositoryException e) {
            throw new RuntimeException("Error while Initial DAM Structure creation : "+e.getMessage());
        }
    }

    private Session initSession() throws LoginException {
        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put(ResourceResolverFactory.SUBSERVICE, "asset-approval-service-user");
        ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(authInfo);
        return resourceResolver.adaptTo(Session.class);
    }


    /**
     * Creates DAM node Structure for given array of node names
     * @param nodes
     * @throws RepositoryException
     * @throws LoginException
     */
    public void createNodeStructure(String[] nodes) throws RepositoryException, LoginException {
        try {
            Session session = initSession();
            Node parentNode = session.getNode(PARENT_NODE_PATH);
            for (String node : nodes) {
                parentNode = createDamNode(parentNode, node, "sling:Folder");
            }
            session.save();
            log.info("DAM nodes structure created");
        } catch (PathNotFoundException e){
            throw new PathNotFoundException("Parent path does not exist");
        } catch (RepositoryException e) {
            throw new RepositoryException("Error during node structure creation of '"+ Arrays.toString(nodes)+"' : "+e.getMessage());
        } catch (LoginException e) {
            throw new LoginException("Error while session initiation : "+e.getMessage() );
        }
    }

    private Node createDamNode(Node parentNode, String newNode, String nodeType) throws RepositoryException {
        if(parentNode!=null){
            if (!parentNode.hasNode(newNode)){
                parentNode.addNode(newNode, nodeType);
                log.info("DAM node {} created in {}", newNode, parentNode.getName());
            }
            parentNode = parentNode.getNode(newNode);
        }
        return parentNode;
    }

    /**
     * Sets jcr property with given value of a node
     * @param nodePath
     * @param propertyName
     * @param propertyValue
     */
    public void setNodeProperty(String nodePath, String propertyName, String propertyValue){
        try {
            Session session = initSession();
            Node node = session.getNode(nodePath);
            node.setProperty(propertyName,propertyValue);
            log.info("property '{}' added successfully", propertyName);
            session.save();
        } catch (LoginException e) {
            log.error("Error while getting session : {}",e.getMessage());
        } catch (RepositoryException e) {
            log.error("Error while setting property : {}",e.getMessage());
        }
    }
}