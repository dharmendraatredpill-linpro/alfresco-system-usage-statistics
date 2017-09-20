package org.redpill.alfresco.repo.statistics.scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


public class CsvStatContent extends DeclarativeWebScript{
  
  protected NodeService _nodeService;
  
  public void setNodeService(NodeService nodeService) {
    this._nodeService = nodeService;
  }
  protected SearchService searchService;
  
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }
  
  protected ContentService _contentService;
  
  public void setcontentService(ContentService contentService) {
    this._contentService = contentService;
  }
  final SecurityContext securityContext = SecurityContextHolder.getContext();
  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest request, Status status, Cache cache) {
    Map<String, Object> result = new HashMap<>();

    String fileName = request.getParameter("content");
    String file = fileName.substring(0, fileName.length()-5);
    String y = fileName.substring(fileName.length() -4, fileName.length());
    
    NodeRef folderNR = getNodeRef();
    List<ChildAssociationRef> csvFolder = _nodeService.getChildAssocs(folderNR);
    NodeRef csv = getNR(csvFolder,"csv");
    List<ChildAssociationRef> yearFolder = _nodeService.getChildAssocs(csv);
    NodeRef year = getNR(yearFolder,y);

    List<ChildAssociationRef> fileFolder = _nodeService.getChildAssocs(year);
    NodeRef nr = getNR(fileFolder, file);

    Map<QName, Serializable> properties;
    properties = _nodeService.getProperties(nr);
    String content = getPersonDescriptionContentAsString(properties);

    List<String> stringList = new ArrayList<>();
    stringList.add(content);
    result.put("content", stringList);
    return result;
  }

  private String getPersonDescriptionContentAsString(Map<QName, Serializable> properties) {
    final ContentData personDescription = (ContentData) properties.get(ContentModel.PROP_CONTENT);

    if (personDescription != null) {

      return AuthenticationUtil.runAsSystem(new RunAsWork<String>() {
        @Override
        public String doWork() throws Exception {
          ContentReader reader = _contentService.getRawReader(personDescription.getContentUrl());
          if (reader != null && reader.exists()) {
            return reader.getContentString();
          }

          return null;
        }
      });
    }

    return null;
  }

  public NodeRef getNR(List<ChildAssociationRef> csvFolder, String type){
    for (ChildAssociationRef child : csvFolder){
      NodeRef childNR = child.getChildRef();
      String name = _nodeService.getProperty(childNR, ContentModel.PROP_NAME).toString();
      if(type.equals(name)){
        return childNR;
      }
    }
    return null;
  }
  protected NodeRef getNodeRef(){
    StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    AuthenticationUtil.setFullyAuthenticatedUser("admin");
    ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_CMIS_ALFRESCO, "SELECT * FROM cmis:folder WHERE cmis:name = 'UserProfileStatistics'");
    NodeRef node = null;
    try
    {
      if (rs.length() == 0)
      {
          throw new AlfrescoRuntimeException("Folder with user profile statistics not found");
      }
      node = rs.getNodeRef(0);
    }
    finally
    {
      rs.close();
    }
    return node;
  }
}
