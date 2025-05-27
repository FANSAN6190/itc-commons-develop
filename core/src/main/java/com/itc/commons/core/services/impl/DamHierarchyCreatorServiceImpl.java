package com.itc.commons.core.services.impl;

import com.itc.commons.core.listener.AssetAcceptRejectListener;
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


    private static final Logger log = LoggerFactory.getLogger(AssetAcceptRejectListener.class);


    String[] nodesInStructure = {"itc", "marketing-campaign"};
    String PARENT_NODE_PATH = "/content/dam";

    @Activate
    public void activate(){
        try {
            Session session =initSession();
            if(session!=null) {
                Node parentNode = session.getNode(PARENT_NODE_PATH);
                parentNode = createDamNode(parentNode, nodesInStructure[0],"sling:Folder");
                PARENT_NODE_PATH=PARENT_NODE_PATH.concat("/"+nodesInStructure[0]);
                parentNode = createDamNode(parentNode, nodesInStructure[1],"sling:Folder");
                PARENT_NODE_PATH = PARENT_NODE_PATH.concat("/"+nodesInStructure[1]);
                log.info("Initial Dam Structure Created");
                session.save();
            }
        } catch (LoginException | RepositoryException e) {
            throw new RuntimeException("Error during DAM hierarchy initiation : "+ e.getMessage());
        }
    }

    public Session initSession() throws LoginException {
        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put(ResourceResolverFactory.SUBSERVICE, "asset-approval-service-user");
        ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(authInfo);
        return resourceResolver.adaptTo(Session.class);
    }

    public void createNodeStructure(String[] nodes) {
        try {
            Session session = initSession();
            Node parentNode = session.getNode(PARENT_NODE_PATH);
            for (String node : nodes) {
                parentNode = createDamNode(parentNode, node, "sling:Folder");
            }
            session.save();
            log.info("DAM nodes structure created");
        } catch (PathNotFoundException e){
            log.error("Parent path does not exist");
            throw new RuntimeException("Parent path does not exist");
        }
        catch (RepositoryException e) {
            log.error("Error during node structure creation of : {}", Arrays.toString(nodes));
            throw new RuntimeException("Error during node structure creation of '"+ Arrays.toString(nodes)+"' : "+e.getMessage());
        } catch (LoginException e) {
            throw new RuntimeException("Error while session initiation : "+e.getMessage() );
        }
    }

    public Node createDamNode(Node parentNode, String newNode, String nodeType) throws RepositoryException {
        if(parentNode!=null){
            if (!parentNode.hasNode(newNode)){
                parentNode.addNode(newNode, nodeType);
                log.info("DAM node {} created in {}", newNode, parentNode.getName());
            }
            parentNode = parentNode.getNode(newNode);
        }
        return parentNode;
    }
}