package org.redpill.alfresco.repo.statistics.jobs;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.alfresco.repo.statistics.service.ReportSiteUsage;
import org.redpill.alfresco.repo.statistics.service.UserProfileStats;
import org.springframework.util.Assert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ExportStatsForUserProfiles extends ClusteredExecuter{

  private static final Log LOG = LogFactory.getLog(ExportStatsForUserProfiles .class);
  final static String URI = "http://www.scania.com/model/teamroom-userprofile/1.0";

  protected ReportSiteUsage reportSiteUsage;
  protected Repository repository;
  protected FileFolderService fileFolderService;
  protected NodeService nodeService;
  protected SearchService searchService;
  protected NamespaceService namespaceService;
  protected ContentService contentService;
  protected SiteService siteService;
  protected AuthorityService authorityService;
  
  protected String userGroups;

  public String getUserGroups() {
	return userGroups;
}

public void setUserGroups(String userGroups) {
	this.userGroups = userGroups;
}

public final String XPATH_DATA_DICTIONARY = "/app:company_home/app:dictionary";
  public final String FOLDER_NAME_REDPILL_LINPRO = "Redpill-Linpro";
  public final String FOLDER_NAME_STATISTICS = "Statistics";
  public final String FOLDER_NAME_USER_PROFILE_STATISTICS = "UserProfileStatistics";

  public int N_OF_USERS = 0;
  public int N_OF_UPDATED_PROFILES = 0;
  public int N_OF_UPLOADED_PICTURES = 0;
  public int N_OF_ADDED_NICKNAMES = 0;
  public int N_OF_ADDED_JOB_TITLES = 0;
  public int N_OF_ADDED_OFFICE_PHONES = 0;
  public int N_OF_ADDED_MOBILE_PHONES = 0;
  public int N_OF_FILLED_LOCATIONS = 0;
  public int N_OF_ADDED_COMPANIES = 0;
  public int N_OF_FILLED_ABOUT = 0;
  public int N_OF_SELECTED_LOCAL_INTRANET = 0;
  public int N_OF_FILLED_SKILLS = 0;



  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  public void setFileFolderService(FileFolderService fileFolderService) {
    this.fileFolderService = fileFolderService;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  public void setNamespaceService(NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }
  
  public void setAuthorityService(AuthorityService authorityService) {
	this.authorityService = authorityService;
}

  @Override
  protected String getJobName() {
    return "Export Statistics For User Profiles";
  }

  @Override
  protected void executeInternal() {
    LOG.info("Starting generation of user profile statistics");
    AuthenticationUtil.runAs(new RunAsWork<Void>() {
      @Override
      public Void doWork() throws Exception {
        RetryingTransactionHelper transactionHelper = _transactionService.getRetryingTransactionHelper();
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
          @Override
          public Void execute() throws Throwable {
            
            Map<String, List<UserProfileStats>> userProfileStats = new HashMap<>();

            List<UserProfileStats> UPsummary = new ArrayList<>();

            List<UserProfileStats> userStats = new ArrayList<>();
         // create csv
            StringBuilder sb = new StringBuilder();

            // get all users
            List<NodeRef> users = getUsersStartingFrom();

    		String allGroups = getUserGroups();
    		List<String> groupList =null;
				
    		if (allGroups != null && StringUtils.isNotEmpty(allGroups)) {
						groupList = new ArrayList<String>(Arrays.asList(allGroups.split(",")));
					}
    		
            // query user
            for (NodeRef user:users) {
              // write user info in string
              String userInfo = getUserStats(user, userStats,groupList);

              // write user info in a line in csv, end with next line
              sb.append(userInfo);
            }
            
           
            String stats = ("Total Number Of Users" + "\r\n"+
            + N_OF_USERS + "\r\n\r\n"+ "Summary" + "\r\n" +
                ";"+N_OF_UPDATED_PROFILES + ";"
                + N_OF_UPLOADED_PICTURES + ";"+
                N_OF_ADDED_NICKNAMES + ";" +
                N_OF_ADDED_JOB_TITLES + ";"+
                N_OF_ADDED_OFFICE_PHONES + ";" +
                N_OF_ADDED_MOBILE_PHONES + ";"+
                N_OF_FILLED_LOCATIONS + ";"+
                N_OF_ADDED_COMPANIES + ";"+
                N_OF_FILLED_ABOUT + ";"+
                N_OF_SELECTED_LOCAL_INTRANET + ";"+
                N_OF_FILLED_SKILLS+ "\r\n\r\n"+
                "Username;Updated;Pictures;Nicknames;Job Titles;Office Phones;Mobile Phones;Locations;Company;Descriptions;Local Intranet;Skills;Groups\r\n");
            sb.insert(0, stats);
            //print csv
            long time = System.currentTimeMillis();
            storeResult(sb.toString(),"csv", time);
            List<Integer> summaryList= new ArrayList<>();
            summaryList.add(N_OF_USERS);
            summaryList.add(N_OF_UPDATED_PROFILES);
            summaryList.add(N_OF_UPLOADED_PICTURES);
            summaryList.add(N_OF_ADDED_NICKNAMES);
            summaryList.add(N_OF_ADDED_JOB_TITLES);
            summaryList.add(N_OF_ADDED_OFFICE_PHONES);
            summaryList.add(N_OF_ADDED_MOBILE_PHONES);
            summaryList.add(N_OF_FILLED_LOCATIONS);
            summaryList.add(N_OF_ADDED_COMPANIES);
            summaryList.add(N_OF_FILLED_ABOUT);
            summaryList.add(N_OF_SELECTED_LOCAL_INTRANET);
            summaryList.add(N_OF_FILLED_SKILLS);

            UserProfileStats summary = new UserProfileStats();
            summary.setSummary(summaryList);
            UPsummary.add(summary);
            userProfileStats.put("Summary",UPsummary);
            userProfileStats.put("UserStats",userStats);
            String json = createJson(userProfileStats);
            storeResult(json,"json", time);

            return null;
          }
        }, false, false);
        return null;
      }
    }, AuthenticationUtil.SYSTEM_USER_NAME);
    LOG.info("Finished generating user profile statistics");
  }

  public String createJson(Map<String, List<UserProfileStats>> userProfileStats) {
    Gson gson = new GsonBuilder().create();
    String json = gson.toJson(userProfileStats);
    return json;
  }

  protected List<NodeRef> getUsersStartingFrom(){//int skip){
    SearchParameters sp = new SearchParameters();
    StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    sp.addStore(storeRef);
    sp.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
    sp.setQuery("SELECT * FROM cm:person");
    //sp.setMaxItems(500);
    //sp.setSkipCount(skip);
    ResultSet rs = searchService.query(sp);
    List<NodeRef> nodes = null;
    try
    {
      if (rs.length() == 0)
      {
          return nodes;
      }
      nodes = rs.getNodeRefs();
    }
    finally
    {
      rs.close();
    }
    return nodes;
  }

  protected void storeResult(String csv, String ext, long time) {
    List<NodeRef> selectResult = searchService.selectNodes(repository.getRootHome(), XPATH_DATA_DICTIONARY, null, namespaceService, false);
    NodeRef dataDictionaryNodeRef = selectResult.get(0);
    if (LOG.isTraceEnabled()) {
      LOG.trace("Looked up data dictionary: " + dataDictionaryNodeRef);
    }
    NodeRef rlNodeRef = nodeService.getChildByName(dataDictionaryNodeRef, ContentModel.ASSOC_CONTAINS, FOLDER_NAME_REDPILL_LINPRO);
    if (rlNodeRef == null) {
      rlNodeRef = fileFolderService.create(dataDictionaryNodeRef, FOLDER_NAME_REDPILL_LINPRO, ContentModel.TYPE_FOLDER).getNodeRef();
    }

    NodeRef statisticsNodeRef = nodeService.getChildByName(rlNodeRef, ContentModel.ASSOC_CONTAINS, FOLDER_NAME_STATISTICS);
    if (statisticsNodeRef == null) {
      statisticsNodeRef = fileFolderService.create(rlNodeRef, FOLDER_NAME_STATISTICS, ContentModel.TYPE_FOLDER).getNodeRef();
    }

    NodeRef userStatisticsNodeRef = nodeService.getChildByName(statisticsNodeRef, ContentModel.ASSOC_CONTAINS, FOLDER_NAME_USER_PROFILE_STATISTICS);
    if (userStatisticsNodeRef == null) {
      userStatisticsNodeRef = fileFolderService.create(statisticsNodeRef, FOLDER_NAME_USER_PROFILE_STATISTICS, ContentModel.TYPE_FOLDER).getNodeRef();
    }

    NodeRef extFolder = nodeService.getChildByName(userStatisticsNodeRef, ContentModel.ASSOC_CONTAINS, ext);
    if (extFolder == null) {
      extFolder = fileFolderService.create(userStatisticsNodeRef, ext, ContentModel.TYPE_FOLDER).getNodeRef();
    }

    int year = Calendar.getInstance().get(Calendar.YEAR);

    NodeRef yearNodeRef = nodeService.getChildByName(extFolder, ContentModel.ASSOC_CONTAINS, Integer.toString(year));
    if (yearNodeRef == null) {
      yearNodeRef = fileFolderService.create(extFolder, Integer.toString(year), ContentModel.TYPE_FOLDER).getNodeRef();
    }

    String name = "user-profile-statistics-" + time + "."+ext;
    FileInfo create = fileFolderService.create(yearNodeRef, name, ContentModel.TYPE_CONTENT);
    ContentWriter writer = contentService.getWriter(create.getNodeRef(), ContentModel.PROP_CONTENT, true);
    if ("csv".equals(ext)){
      writer.setMimetype("text/"+ext);
      writer.putContent(csv);
      writer.guessEncoding();
    } else {
      writer.setMimetype("application/"+ext);
      writer.putContent(csv);
      writer.guessEncoding();
    }

  }

  private String getAttributeStringValue(Map<QName, Serializable> properties, QName attributeName) {
    Serializable attributeValue = properties.get(attributeName);

    String value = "";

    if (attributeValue != null) {
      value = org.json.simple.JSONObject.escape((String) attributeValue);
    }

    return value;
  }

	private String getAttributeArrayValue(Map<QName, Serializable> properties, QName attributeName) {

		List<String> attributeValues = (ArrayList<String>) properties.get(attributeName);
		StringBuffer tagsBuffer = new StringBuffer();

		if (attributeValues != null && attributeValues.size() > 0) {
			for (String attributeValue : attributeValues) {

				if (StringUtils.isNotEmpty(tagsBuffer)) {
					tagsBuffer.append(",");
				}
				tagsBuffer.append(attributeValue);
			}

			return tagsBuffer.toString();
		}

		return "";
	}

  private String getAttributeDateValue(Map<QName, Serializable> properties, QName attributeName, String dateFormat) {
    Date attributeValue = (Date) properties.get(attributeName);

    SimpleDateFormat df = new SimpleDateFormat(dateFormat);

    String value = "";

    if (attributeValue != null) {
      value = org.json.simple.JSONObject.escape((String) df.format(attributeValue));
    }

    return value;
  }

  private String getPersonDescriptionContentAsString(Map<QName, Serializable> properties) {
    final ContentData personDescription = (ContentData) properties.get(ContentModel.PROP_PERSONDESC);

    if (personDescription != null) {

      return AuthenticationUtil.runAsSystem(new RunAsWork<String>() {
        @Override
        public String doWork() throws Exception {
          ContentReader reader = contentService.getRawReader(personDescription.getContentUrl());
          if (reader != null && reader.exists()) {
            return reader.getContentString();
          }

          return null;
        }
      });
    }

    return null;
  }

  public String getUserStats(NodeRef user, List<UserProfileStats> userStats,List<String> groupList){

    UserProfileStats ups = new UserProfileStats();

    N_OF_USERS++;

    StringBuilder sb = new StringBuilder();

    Map<QName, Serializable> properties = nodeService.getProperties(user);

    String username = getAttributeStringValue(properties, ContentModel.PROP_USERNAME);
    if (username != null && username.length()>0){
      sb.append(username);
      ups.setUserName(username);
    }
    sb.append(";");
    String updated = getAttributeDateValue(properties, QName.createQName(URI, "lastModified"), "yyyy-MM-dd HH:mm:ss");
    if (updated != null && updated.length()>0){
      sb.append(updated);
      ups.setUpdated(updated);
      N_OF_UPDATED_PROFILES++;
    }
    sb.append(";");
    List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(user, ContentModel.ASSOC_AVATAR);
    if (targetAssocs != null && !targetAssocs.isEmpty()){
      String pictureFilename = getAttributeStringValue(nodeService.getProperties(targetAssocs.get(0).getTargetRef()),ContentModel.PROP_NAME);
      if(pictureFilename != null){
        ups.setPictureFilename(pictureFilename);
      }
      sb.append("true");
      ups.setUploadedPicture(true);
      N_OF_UPLOADED_PICTURES++;
    } else {
      sb.append("false");
      ups.setUploadedPicture(false);
    }
    sb.append(";");
    String nickname = getAttributeStringValue(properties, QName.createQName(URI, "nickname"));
    if (nickname != null && nickname.length()>0){
      sb.append(nickname);
      ups.setNickName(nickname);
    N_OF_ADDED_NICKNAMES++;
    }
    sb.append(";");
    String jobtitle = getAttributeStringValue(properties, ContentModel.PROP_JOBTITLE);
    if (jobtitle != null && jobtitle.length()>0){
      sb.append(jobtitle);
      ups.setJobTitle(jobtitle);
      N_OF_ADDED_JOB_TITLES++;
    }
    sb.append(";");
    String telephone = getAttributeStringValue(properties, ContentModel.PROP_TELEPHONE);
    if (telephone != null && telephone.length()>0){
      sb.append(telephone);
      ups.setOfficePhone(telephone);
      N_OF_ADDED_OFFICE_PHONES++;
    }
    sb.append(";");
    String mobile = getAttributeStringValue(properties, ContentModel.PROP_MOBILE);
    if (mobile != null && mobile.length()>0){
      sb.append(mobile);
      ups.setMobilePhone(mobile);
      N_OF_ADDED_MOBILE_PHONES++;
    }
    sb.append(";");
    String location = getAttributeStringValue(properties, ContentModel.PROP_LOCATION);
    if (location != null && location.length()>0){
      sb.append(location);
      ups.setLocation(location);
      N_OF_FILLED_LOCATIONS++;
    }
    sb.append(";");
    String company = getAttributeStringValue(properties, QName.createQName(URI, "company"));
    if (company != null && company.length()>0){
      sb.append(company);
      ups.setCompany(company);
      N_OF_ADDED_COMPANIES++;
    }
    sb.append(";");
    String description = getPersonDescriptionContentAsString(properties);
    if (description != null && description.length()>0){
      sb.append(description);
      ups.setAbout(description);
      N_OF_FILLED_ABOUT++;
    }
    sb.append(";");
    String localintranet = getAttributeStringValue(properties, QName.createQName(URI, "preferredLocalIntranetURL"));
    if (localintranet != null && localintranet.length()>0){
      sb.append(localintranet);
      ups.setLocalIntranet(localintranet);
    N_OF_SELECTED_LOCAL_INTRANET++;
    }
//    sb.append(";");
//    String geolocation = getAttributeStringValue(properties, ContentModel.PROP_LOCALE);
//    if (geolocation != null && geolocation.length()>0){
//      sb.append(geolocation);
//    N_OF_SELECTED_GEOLOCATION++;
//    }
    sb.append(";");
    String tags = getAttributeArrayValue(properties, QName.createQName(URI, "tags"));
    if (StringUtils.isNotEmpty(tags)){
      sb.append(tags);
      ups.setSkills(tags);
    N_OF_FILLED_SKILLS++;
    }
    
    sb.append(";");

    // getting user's groups
    sb.append(getActualGroups(username,groupList));
	
	
    /* 
     * 
     * How many profiles exist
     * 
     * How many have updated/added/filled:
     * # users who have updated profile ?
     * association cm:preferanceImage
     * cm:nickname
     * cm:jobtitle
     * cm:telephone
     * cm:mobile
     * cm:location
     * cm:persondescription
     * cm:preferredLocalIntranetURL
     * geolocation ???????
     * cm:tags     skill_1;skill_2 ..... etc
     * username;lastUpdated;pictureFilename;bickName;jobTitle;officePhone;mobilePhone;geoLocation;company;about;localIntranet;skill_1;2;3etc...
     * */
    sb.append("\r\n");

    userStats.add(ups);
    return sb.toString();
  }
  
  /****
   * 
   * Return user's groups based on group list defined in alfresco global properties file
   * 
   * @param username
   * @param groupList
   * @return
   */

	private String getActualGroups(String username, List<String> groupList) {
		Set<String> authorities = authorityService.getContainingAuthoritiesInZone(AuthorityType.GROUP, username,
				AuthorityService.ZONE_APP_DEFAULT, null, 1000);

		StringBuffer groupBuffer = new StringBuffer();
		for (String authority : authorities) {
			if (StringUtils.isNotBlank(authority)) {

				String groupStr = authority.replaceAll("GROUP_", "");

				if (groupList.contains(groupStr)) {

					if (StringUtils.isNotEmpty(groupBuffer)) {
						groupBuffer.append(",");
					}
					groupBuffer.append(groupStr);
				}
			}
		}

		return groupBuffer.toString();
	}

@Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    Assert.notNull(repository);
    Assert.notNull(contentService);
    Assert.notNull(fileFolderService);
    Assert.notNull(nodeService);
    Assert.notNull(namespaceService);
    Assert.notNull(searchService);
    Assert.notNull(siteService);
    Assert.notNull(authorityService);
    LOG.info("Initialized " + GenerateSiteStatistics.class.getName());
  }
}
