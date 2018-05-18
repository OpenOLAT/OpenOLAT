//<OLATCE-103>
/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.vc.provider.adobe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.NotImplementedException;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.bps.course.nodes.vc.VCConfiguration;
import de.bps.course.nodes.vc.provider.VCProvider;

/**
 * 
 * Description:<br>
 * Virtual classroom provider for Adobe Connect.
 * 
 * <P>
 * Initial Date:  09.12.2010 <br>
 * @author skoeber
 */
public class AdobeConnectProvider extends LogDelegator implements VCProvider {

  private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
  private static final String COOKIE = "BREEZESESSION=";
  protected static final String PREFIX = "olat-";
  private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm";

  protected static String DEFAULT_TEMPLATE = "default";
  
  // configuration
  private static AdobeConnectConfiguration defaultConfig;
  private boolean enabled;
  private String providerId;
  private String displayName;
  private String protocol;
  private int port;
  private String baseUrl;
  private String adminLogin;
  private String adminPassword;
  private String accountId;
  private Map<String, String> templates;
  private boolean guestAccessAllowedDefault;
  private boolean guestStartMeetingAllowedDefault;
  private boolean useMeetingDatesDefault;
  private boolean showOptions;
  private String userType;

  // runtime data
  private String cookie;
  
  /**
   * Constructor for internal use to create new instance
   * @param providerId
   * @param protocol
   * @param port
   * @param baseUrl
   * @param adminLogin
   * @param adminPassword
   */
	private AdobeConnectProvider(String providerId, String displayName, String protocol, int port, String baseUrl, String adminLogin, String adminPassword,
			String accountId, Map<String, String> templates, boolean guestAccessAllowedDefault, boolean guestStartMeetingAllowedDefault,
			boolean showOptions, String userType) {
		setProviderId(providerId);
		setDisplayName(displayName);
		setProtocol(protocol);
		setPort(port);
		setBaseUrl(baseUrl);
		setAdminLogin(adminLogin);
		setAdminPassword(adminPassword);
		setAccountId(accountId);
		setTemplates(templates);
		setGuestAccessAllowedDefault(guestAccessAllowedDefault);
		setGuestStartMeetingAllowedDefault(guestStartMeetingAllowedDefault);
		setShowOptions(showOptions);
		setUserType(userType);
	}
  
  /**
   * Public constructor, mostly used by spring<br/>
   * <b>Important</b> when using: set configuration manually!
   */
  public AdobeConnectProvider() {
  	//
  }

  @Override
  public boolean createClassroom(String roomId, String name, String description, Date begin, Date end, VCConfiguration config) {
    if(existsClassroom(roomId, config)) return true;
    
    if(!loginAdmin()) {
    	logError("Cannot login to Adobe Connect. Please check module configuration and Adobe Connect connectivity.", null); 
    	return false;
    }

    // begin and end can be NULL, see interface description
    if(begin == null) begin = new Date();
    if(end == null) end = new Date(begin.getTime() + 365*24*60*60*1000); // preset one year
    
    // formatter for begin and end
    SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT);

    // find my-meetings
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("action", "sco-shortcuts");
    Document responseDoc = getResponseDocument(sendRequest(parameters));
    if(!evaluateOk(responseDoc)) return false;

    String folderScoId = null;
    // use my-meetings folder
    String result = evaluate(responseDoc, "//sco[@type=\"my-meetings\"]");
    if(result != null && !result.isEmpty()) {
    	folderScoId = evaluate(responseDoc, "//sco[@type=\"my-meetings\"]/attribute::sco-id");
    }
    // my-meetings folder not found, fallback to meetings
    if(folderScoId == null) {
    	result = evaluate(responseDoc, "//sco[@type=\"meetings\"]");
    	if(result != null && !result.isEmpty()) {
    		folderScoId = evaluate(responseDoc, "//sco[@type=\"meetings\"]/attribute::sco-id");
    	}
    }
    // meetings folder not found, error case
    if(folderScoId == null) return false;
    // folder found where to insert the new meeting
    // create new meeting
    parameters = new HashMap<String, String>();
    parameters.put("action", "sco-update");
    parameters.put("type", "meeting");
    parameters.put("name", PREFIX + roomId);
    parameters.put("folder-id", folderScoId);
    parameters.put("date-begin", sd.format(begin));
    parameters.put("date-end", sd.format(end));
    parameters.put("url-path", PREFIX + roomId);
    String templateId = ((AdobeConnectConfiguration)config).getTemplateKey();
    if(templateId != null && !templateId.equals(DEFAULT_TEMPLATE))
    	parameters.put("source-sco-id", templateId);
    responseDoc = getResponseDocument(sendRequest(parameters));
    if(!evaluateOk(responseDoc)) return false;
    
    // adjust permissions
    String meetingScoId = evaluate(responseDoc, "//sco/attribute::sco-id");
    parameters.clear();
    parameters.put("action", "permissions-update");
    parameters.put("acl-id", meetingScoId);
    parameters.put("principal-id", "public-access");
    if(((AdobeConnectConfiguration)config).isGuestAccessAllowed())
    	parameters.put("permission-id", "view-hidden");
    else
    	parameters.put("permission-id", "remove");
    responseDoc = getResponseDocument(sendRequest(parameters));
    if(!evaluateOk(responseDoc)) return false;

    logout();
    return true;
  }
  
  @Override
  public boolean updateClassroom(String roomId, String name, String description, Date begin, Date end, VCConfiguration config) {
  	if(!existsClassroom(roomId, config)) return false;
  	if(!loginAdmin()) throw new AssertException("Cannot login to Adobe Connect. Please check module configuration and that Adobe Connect is available.");
  	
  	String scoId = getScoIdFor(roomId);
  	if(scoId == null) return false;
  	
  	// formatter for begin and end
    SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT);
  	
  	Map<String, String> parameters = new HashMap<String, String>();
  	// update meeting configuration
  	parameters.put("action", "sco-update");
  	parameters.put("sco-id", scoId);
  	if(begin != null)
  		parameters.put("date-begin", sd.format(begin));
  	if(end != null)
  		parameters.put("date-end", sd.format(end));
  	String templateId = ((AdobeConnectConfiguration)config).getTemplateKey();
    if(templateId != null && !templateId.equals(DEFAULT_TEMPLATE))
    	parameters.put("source-sco-id", templateId);
    Document responseDoc = getResponseDocument(sendRequest(parameters));
    if(!evaluateOk(responseDoc)) return false;
    
    // adjust permissions
    parameters.clear();
    parameters.put("action", "permissions-update");
    parameters.put("acl-id", scoId);
    parameters.put("principal-id", "public-access");
    if(((AdobeConnectConfiguration)config).isGuestAccessAllowed())
    	parameters.put("permission-id", "view-hidden");
    else
    	parameters.put("permission-id", "remove");
    responseDoc = getResponseDocument(sendRequest(parameters));
    if(!evaluateOk(responseDoc)) return false;
  	
    logout();
  	return true;
  }
  
  @Override
  public boolean removeClassroom(String roomId, VCConfiguration config) {
  	if(!existsClassroom(roomId, config)) return true;
  	if(!loginAdmin()) throw new AssertException("Cannot login to Adobe Connect. Please check module configuration and that Adobe Connect is available.");
  	
  	String scoId = getScoIdFor(roomId);
  	Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("action", "sco-delete");
    parameters.put("sco-id", scoId);
    Document responseDoc = getResponseDocument(sendRequest(parameters));
    if(!evaluateOk(responseDoc)) return false;
  	
    logout();
  	return true;
  }
  
  private String getScoIdFor(String roomId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("action", "sco-search-by-field");
    parameters.put("query", PREFIX + roomId);
    parameters.put("filter-type", "meeting");
    Document responseDoc = getResponseDocument(sendRequest(parameters));
    if(!evaluateOk(responseDoc)) return null;
    
    Object result = evaluate(responseDoc, "//sco/url-path[text()='/" + PREFIX + roomId + "/']", XPathConstants.NODESET);
    if(result == null) return null;
    NodeList nodes = (NodeList) result;
    if(nodes.getLength() == 1) {
    	String scoId = evaluate(responseDoc, "//sco[1]/attribute::sco-id");
    	return scoId;
    }
    else if(nodes.getLength() > 1)
    	throw new AssertException("More than one Adobe Connect room found for one course node!");
    else
    	return null;
  }
  
  @Override
  public URL createClassroomUrl(String roomId, Identity identity, VCConfiguration config) {
  	URL url = null;
  	URI uri = UriBuilder.fromUri(protocol + "://" + baseUrl).port(port)
  	.path(PREFIX + roomId).queryParam("session", cookie).build();
  	try {
  		url = uri.toURL();
  	} catch (MalformedURLException e) {
  		logWarn("Cannot create access URL to Adobe Connect meeting for id \"" + PREFIX + roomId + "\" and user \"" + identity.getKey() + "\"", e);
  	}
  	return url;
  }

  @Override
  public URL createClassroomGuestUrl(String roomId, Identity identity, VCConfiguration config) {
  	URL url = null;
  	URI uri = UriBuilder.fromUri(protocol + "://" + baseUrl).port(port)
  	.path(PREFIX + roomId).queryParam("guestName", identity.getName()).build();
  	try {
  		url = uri.toURL();
  	} catch (MalformedURLException e) {
  		logWarn("Cannot create access URL to Adobe Connect meeting for id \"" + PREFIX + roomId + "\" and user \"" + identity.getKey() + "\"", e);
  	}
  	return url;
  }

  @Override
  public boolean existsClassroom(String roomId, VCConfiguration config) {
    if(!loginAdmin()) return false;
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("action", "sco-search-by-field");
    parameters.put("query", PREFIX + roomId);
    parameters.put("filter-type", "meeting");
    Document responseDoc = getResponseDocument(sendRequest(parameters));
    if(!evaluateOk(responseDoc)) return false;
    
    Object result = evaluate(responseDoc, "//sco/url-path[text()='/" + PREFIX + roomId + "/']", XPathConstants.NODESET);
    logout();
    if(result == null) return false;
    NodeList nodes = (NodeList) result;
    if(nodes.getLength() == 1)
    	return true;
    else if(nodes.getLength() > 1)
    	throw new AssertException("More than one Adobe Connect room found for one course node!");
    else
    	return false;
  }
  
  protected List<String> findClassrooms(String name) {
  	List<String> results = new ArrayList<String>();
  	if(!loginAdmin()) return results;
  	Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("action", "sco-search-by-field");
    parameters.put("field", "name");
    parameters.put("query", name);
    parameters.put("filter-type", "meeting");
    Document responseDoc = getResponseDocument(sendRequest(parameters));
    if(!evaluateOk(responseDoc)) {
    	logError("Invalid response when searching for classrooms with the name \"" + name + "\"", null);
    	return results;
    }
    
    Object result = evaluate(responseDoc, "descendant-or-self::sco-search-by-field-info/child::sco/child::name", XPathConstants.NODESET);
    logout();
    if(result == null) if(isLogDebugEnabled()) logDebug("Search for Adobe Connect classrooms with name \"" + name + "\" with no results");
    NodeList nodes = (NodeList) result;
    for(int i=0; i<nodes.getLength(); i++) {
    	Node node = nodes.item(i);
    	String roomId = node.getFirstChild().getNodeValue();
    	results.add(roomId);
    }
  	
  	return results;
  }
  
  @Override
  public boolean createModerator(Identity identity, String roomId) {
  	if(!loginAdmin()) return false;
  	Map<String, String> parameters = new HashMap<>();
  	// create user
  	parameters.put("action", "principal-update");
  	parameters.put("first-name", identity.getUser().getProperty(UserConstants.FIRSTNAME, null));
  	parameters.put("last-name", identity.getUser().getProperty(UserConstants.LASTNAME, null));
  	parameters.put("login", PREFIX + identity.getName());
  	parameters.put("password", Encoder.md5hash(identity.getName() + "@" + Settings.getApplicationName()));
  	parameters.put("type", userType);
  	parameters.put("has-children", "false");
  	Document responseDoc = getResponseDocument(sendRequest(parameters));
  	
  	if(!evaluateOk(responseDoc)) {
  		boolean exists = false;
  		String error = evaluate(responseDoc, "/results/status[1]/attribute::code");
  		if(error.equals("invalid")) {
  			error = evaluate(responseDoc, "/results[1]/status[1]/invalid/attribute::subcode");
  			exists = error.equals("duplicate");
  		}
  		if(!exists) return false;
  	}
  	
  	// search the user
  	String principalId = getPrincipalIdFor(identity);
  	if(principalId == null) return false; // error case
  	
  	// create permissions for the meeting
  	String scoId = getScoIdFor(roomId);
  	if(scoId == null) return false;
  	parameters.clear();
  	parameters.put("action", "permissions-update");
  	parameters.put("acl-id", scoId);
  	parameters.put("principal-id", principalId);
  	parameters.put("permission-id", "host");
  	String response = sendRequest(parameters);
  	responseDoc = getResponseDocument(response);
  	logout();
  	
  	return evaluateOk(responseDoc);
  }

  @Override
  public boolean createUser(Identity identity, String roomId) {
    throw new NotImplementedException("method createUser not yet implemented");
  }

  @Override
  public boolean createGuest(Identity identity, String roomId) {
  	throw new NotImplementedException("method createGuest not yet implemented");
  }

  @Override
  public String getProviderId() {
    return providerId;
  }
  
  @Override
  public String getDisplayName() {
  	return displayName;
  }

  @Override
  public VCProvider newInstance() {
    AdobeConnectProvider newInstance = new AdobeConnectProvider(providerId, displayName, protocol, port, baseUrl, adminLogin, adminPassword, accountId, templates,
    		guestAccessAllowedDefault, guestStartMeetingAllowedDefault, showOptions, userType);
    return newInstance;
  }

  @Override
  public Controller createDisplayController(UserRequest ureq, WindowControl wControl, String roomId, String name, String description,
		  boolean isModerator, boolean readOnly, VCConfiguration config) {
  	AdobeDisplayController displayCtr = new AdobeDisplayController(ureq, wControl, roomId, name, description, isModerator, readOnly, (AdobeConnectConfiguration) config, this);
    return displayCtr;
  }

  @Override
  public Controller createConfigController(UserRequest ureq, WindowControl wControl, String roomId, VCConfiguration config) {
  	AdobeConfigController configCtr = new AdobeConfigController(ureq, wControl, roomId, this, (AdobeConnectConfiguration) config);
    return configCtr;
  }
  
  @Override
  public boolean login(Identity identity, String password) {
  	if(cookie == null) createCookie();
    Map<String,String> parameters = new HashMap<>();
    parameters.put("action", "login");
    if(accountId != null) parameters.put("account-id", accountId);
    parameters.put("login", PREFIX + identity.getName());
    parameters.put("password", Encoder.md5hash(identity.getName() + "@" + Settings.getApplicationName()));
    Document responseDoc = getResponseDocument(sendRequest(parameters));

    return evaluateOk(responseDoc);
  }

  private boolean loginAdmin() {
    if(cookie == null) createCookie();
    Map<String,String> parameters = new HashMap<>();
    parameters.put("action", "login");
    if(accountId != null) parameters.put("account-id", accountId);
    parameters.put("login", adminLogin);
    parameters.put("password", adminPassword);
    Document responseDoc = getResponseDocument(sendRequest(parameters));
    boolean success = evaluateOk(responseDoc);
    if(!success) {
    	logWarn("Admin login to Adobe Connect failed", null);
    }
    return success;
  }

  private boolean logout() {
    if(cookie == null) return true;
    Map<String,String> parameters = new HashMap<String, String>();
    parameters.put("action", "logout");
    Document responseDoc = getResponseDocument(sendRequest(parameters));
    cookie = null;

    return evaluateOk(responseDoc);
  }

  private boolean createCookie() {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("action", "common-info");
    String response = sendRequest(parameters);

    Document responseDoc = getResponseDocument(response);
    boolean success = evaluateOk(responseDoc);
    if (success) {
      // get cookie
      String result = evaluate(responseDoc, "/results/common[1]/cookie[1]/text()");
      cookie = result;
    }
    return success;
  }
  
  private String getPrincipalIdFor(Identity identity) {
  	Map<String, String> parameters = new HashMap<>();
    parameters.put("action", "principal-list");
    parameters.put("filter-type", userType);
    parameters.put("filter-type", "user");
    parameters.put("filter-login", PREFIX + identity.getName());
    String response = sendRequest(parameters);

    Document responseDoc = getResponseDocument(response);
    boolean success = evaluateOk(responseDoc);
    if(!success) return null;
    // get principalId
    NodeList nodes = (NodeList) evaluate(responseDoc, "//principal", XPathConstants.NODESET);
    if(nodes == null) return null; // error case
    if(nodes.getLength() == 1) {
    	String principalId = evaluate(responseDoc, "//principal[1]/attribute::principal-id");
    	return principalId;
    } else if(nodes.getLength() > 1) {
    	throw new AssertException("Multiple Adobe Connect users with the same login name found: " + identity.getName());
    } else return null;
  }

  private Document getResponseDocument(String response) {
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    domFactory.setNamespaceAware(true); // never forget this!
    DocumentBuilder builder;
    Document doc = null;
    try {
      builder = domFactory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(response));
      doc = builder.parse(is);
    } catch (ParserConfigurationException e) {
      if(isLogDebugEnabled()) logDebug("Error while creating DOM parser.");
    } catch (SAXException e) {
    	if(isLogDebugEnabled()) logDebug("Error while parsing result XML document.");
    } catch (IOException e) {
    	if(isLogDebugEnabled()) logDebug("Error while reading response.");
    }
    return doc;
  }

  private boolean evaluateOk(Document responseDoc) {
    String result = evaluate(responseDoc, "/results/status[1]/attribute::code");
    if(result == null || result.isEmpty())
    	return false;
    return result.equals("ok");
  }

  private String evaluate(Document responseDoc, String expression) {
  	Object result = evaluate(responseDoc, expression, XPathConstants.STRING);
  	if(result == null || !(result instanceof String))
  		return new String();
    return (String)result;
  }

  private Object evaluate(Document responseDoc, String expression, QName type) {
    if(responseDoc == null) return null;
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
    XPathExpression expr;
    Object result;
    try {
      expr = xpath.compile(expression);
      result = expr.evaluate(responseDoc, type);
    } catch (XPathExpressionException e) {
      result = null;
    }

    return result;
  }

  private String sendRequest(Map<String, String> parameters) {
    URL url = createRequestUrl(parameters);
    URLConnection urlConn;

    try {
      urlConn = url.openConnection();
      // setup url connection
      urlConn.setDoOutput(true);
      urlConn.setUseCaches(false);
      // add content type
      urlConn.setRequestProperty("Content-Type", CONTENT_TYPE);
      // add cookie information
      if(cookie != null) urlConn.setRequestProperty("Cookie", COOKIE + cookie);

      // send request
      urlConn.connect();

      // read response
      BufferedReader input = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
      StringBuilder response= new StringBuilder();
      String line;
      while( (line = input.readLine()) != null ) response.append(line);
      input.close();
      
      if(isLogDebugEnabled()) {
      	logDebug("Requested URL: " + url);
      	logDebug("Response: " + response);
      }

      return response.toString();
    } catch (IOException e) {
      logError("Sending request to Adobe Connect failed. Request: " + url.toString(), e);
      return "";
    }
  }

  private URL createRequestUrl(Map<String, String> parameters) {
    UriBuilder ubu = UriBuilder.fromUri(protocol + "://" + baseUrl).port(port).path("api").path("xml");

    for(String key : parameters.keySet()) {
      ubu.queryParam(key, parameters.get(key));
    }

    URL url = null;
    try {
      url = ubu.build().toURL();
    } catch (Exception e) {
    	logWarn("Error while creating URL for Adobe Connect request.", e);
    	// try to build the URL in a naiv way below
    }
    if(url == null) {
    	// error case, try the naiv way
    	try {
    		StringBuilder sb = new StringBuilder(protocol + "://" + baseUrl + ":" + port + "/api/xml");
    		if(!parameters.isEmpty()) sb.append("?");
    		for(String key : parameters.keySet()) {
    			sb.append(key + "=" + parameters.get(key) + "&");
    		}
    		sb.replace(sb.length(), sb.length(), "");
				url = new URL(sb.toString());
			} catch (MalformedURLException e) {
				logError("Error while creating URL for Adobe Connect request. Please check the configuration!", e);
			}
    }

    return url;
  }
  
  @Override
  public boolean isProviderAvailable() {
  	Map<String, String> parameters = new HashMap<String, String>();
  	parameters.put("action", "common-info");
  	Document responseDoc = getResponseDocument(sendRequest(parameters));
  	
  	return evaluateOk(responseDoc);
  }

	@Override
	public VCConfiguration createNewConfiguration() {
		AdobeConnectConfiguration config = new AdobeConnectConfiguration();
		config.setProviderId(providerId);
		config.setGuestAccessAllowed(defaultConfig.isGuestAccessAllowed());
		config.setGuestStartMeetingAllowed(defaultConfig.isGuestStartMeetingAllowed());
		config.setUseMeetingDates(defaultConfig.isUseMeetingDates());
		return config;
	}
	
	@Override
	public Map<String, String> getTemplates() {
		if(templates == null) templates = Collections.emptyMap();
		return templates;
	}
	
  ////////////////////////////
  // setters used by spring //
  ////////////////////////////
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }
  
  public void setDisplayName(String displayName) {
  	this.displayName = displayName;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public void setAdminLogin(String adminLogin) {
    this.adminLogin = adminLogin;
  }

  public void setAdminPassword(String adminPassword) {
    this.adminPassword = adminPassword;
  }
  
  public void setAccountId(String accountId) {
  	if(StringHelper.containsNonWhitespace(accountId)) {
  		this.accountId = accountId;
  	} else {
  		this.accountId = null;
  	}
  }
  
  public void setTemplates(Map<String,String> templates) {
		this.templates = templates;

		List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>(templates.entrySet());
		for(Map.Entry<String, String> entry:entries) {
			if(!StringHelper.containsNonWhitespace(entry.getKey()) || !StringHelper.containsNonWhitespace(entry.getValue())) {
				templates.remove(entry.getKey());
			}
		}
  }

	public void setGuestAccessAllowedDefault(boolean guestAccessAllowedDefault) {
		this.guestAccessAllowedDefault = guestAccessAllowedDefault;
	}

	public void setGuestStartMeetingAllowedDefault(boolean guestStartMeetingAllowedDefault) {
		this.guestStartMeetingAllowedDefault = guestStartMeetingAllowedDefault;
	}
	
	public void setUseMeetingDatesDefault(boolean useMeetingDatesDefault) {
		this.useMeetingDatesDefault = useMeetingDatesDefault;
	}

	public void setShowOptions(boolean showOptions) {
		this.showOptions = showOptions;
	}
	
	public void setUserType(String userType) {
		this.userType = userType;
	}
	
	public void setDefaultConfig(AdobeConnectConfiguration config) {
		defaultConfig = config;
		defaultConfig.setProviderId(providerId);
	}

  /////////////////////////////
  // getters used internally //
  /////////////////////////////
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	protected String getProtocol() {
		return protocol;
	}

	protected int getPort() {
		return port;
	}

	protected String getBaseUrl() {
		return baseUrl;
	}

	protected String getAdminLogin() {
		return adminLogin;
	}

	protected String getAdminPassword() {
		return adminPassword;
	}

	protected String getAccountId() {
		return accountId;
	}

	protected boolean isGuestAccessAllowedDefault() {
		return guestAccessAllowedDefault;
	}

	protected boolean isGuestStartMeetingAllowedDefault() {
		return guestStartMeetingAllowedDefault;
	}
	
	protected boolean isUseMeetingDatesDefault() {
		return useMeetingDatesDefault;
	}

	protected boolean isShowOptions() {
		return showOptions;
	}
	
	protected String getUserType() {
		return userType;
	}

}
//</OLATCE-103>