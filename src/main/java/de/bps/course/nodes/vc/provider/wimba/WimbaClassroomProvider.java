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
package de.bps.course.nodes.vc.provider.wimba;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

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

import de.bps.course.nodes.vc.VCConfiguration;
import de.bps.course.nodes.vc.provider.VCProvider;

/**
 * 
 * Description:<br>
 * Virtual classroom provider implementation for Wimba Classroom
 * 
 * <P>
 * Initial Date:  06.01.2011 <br>
 * @author skoeber
 */
public class WimbaClassroomProvider extends LogDelegator implements VCProvider {
	
	/** Return type of responses, see Wimba Classroom 6.0 API Guide, page 5 */
	private static final String CONTENT_TYPE = "text/html";
	/** Session cookie, see Wimba Classroom 6.0 API Guide, page 6 */
	private static final String COOKIE = "AuthCookieHandler_Horizon=";
	/** Session token for remote login, see Wimba Classroom 6.0 API Guide, page 56 */
	private static final String TOKEN = "authToken";
	/** Delimiter for data in record format, see Wimba Classroom 6.0 API Guide, page 5 */
	private static final String DELIM = "=END RECORD";
	protected static final String PREFIX = "olat_";
	
	// targets for service URLs
	protected static String TARGET_OPEN_MANAGEROOM = "cmd.open.manageroom";
	protected static String TARGET_OPEN_POLLRESULTS = "cmd.open.pollresults";
	protected static String TARGET_OPEN_TRACKING = "cmd.open.tracking";
	protected static String TARGET_OPEN_ROOMSETTINGS = "cmd.open.roomsettings";
	protected static String TARGET_OPEN_MEDIASETTINGS = "cmd.open.mediasettings";
	protected static String TARGET_OPEN_WIZARD = "cmd.open.wizard";
	
	private static String ENDPOINT_WIZARD = "/wizard/wizard.html.pl?wizardconf=wizard.conf";
	
	// configuration
	private static WimbaClassroomConfiguration defaultConfig;
	private boolean enabled;
	private String providerId;
	private String displayName;
	private String protocol;
	private int port;
	private String baseUrl;
	private String adminLogin;
	private String adminPassword;
	
	// runtime data
	private String cookie;
	private String token;
	
	private WimbaClassroomProvider(String providerId, String displayName, String protocol, int port, String baseUrl, String adminLogin, String adminPassword) {
		setProviderId(providerId);
		setDisplayName(displayName);
		setProtocol(protocol);
		setPort(port);
		setBaseUrl(baseUrl);
		setAdminLogin(adminLogin);
		setAdminPassword(adminPassword);
	}
	
	/**
	 * Public constructor, mostly used by spring<br/>
	 * <b>Important</b> when using: set configuration manually!
	 */
	public WimbaClassroomProvider() {
		//
	}

	@Override
	public VCProvider newInstance() {
		return new WimbaClassroomProvider(providerId, displayName, protocol, port, baseUrl, adminLogin, adminPassword);
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
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isProviderAvailable() {
		if(!loginAdmin()) return false;
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", "statusServer");
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		boolean success = evaluateOk(response);
		
		return success;
	}

	@Override
	public boolean createClassroom(String roomId, String name, String description, Date begin, Date end, VCConfiguration config) {
		if(existsClassroom(roomId, config)) return true;
		boolean success = handleClassroomRequest("createClass", roomId, name, description, begin, end, config);
		// set preview mode because this setting has no effect on creation
		if(success) {
			success = setPreviewMode(roomId, false, false);
			updateRights(null, roomId, "Student", true, false);
			updateGroupRights(null, roomId, "Student", "RegisteredUser", true);
		}
		return success;
	}

	@Override
	public boolean updateClassroom(String roomId, String name, String description, Date begin, Date end, VCConfiguration config) {
		if(!existsClassroom(roomId, config)) {
			logWarn("Tried to update Wimba Classroom meeting, that not exists!", null);
			return false;
		}
		return handleClassroomRequest("modifyClass", roomId, name, description, begin, end, config);
	}
	
	private boolean handleClassroomRequest(String function, String roomId, String name, String description, Date begin, Date end, VCConfiguration config) {
		WimbaClassroomConfiguration wc = (WimbaClassroomConfiguration) config;
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", function);
		parameters.put("target", PREFIX + roomId);
		parameters.put("userlimit", "-1");
		/** name is limited to maximum of 50 characters, see Wimba Classroom 6.0 API Guide, page 15 */
		if(name != null && name.length() > 50) name = name.substring(0, 49);
		parameters.put("longname", name);
		/** name is limited to maximum of 50 characters, see Wimba Classroom 6.0 API Guide, page 15 */
		if(description != null && description.length() > 50) description = description.substring(0, 49);
		parameters.put("can_liveshare", param(wc.isAppshareEnabled()));
		parameters.put("can_archive", param(wc.isArchivingEnabled()));
		parameters.put("auto_open_new__archives", param(wc.isAutoOpenNewArchives()));
		parameters.put("bor_enabled", param(wc.isBreakoutRoomsEnabled()));
		parameters.put("can_ppt_import", param(wc.isPowerPointImportEnabled()));
		parameters.put("student_wb_enabled", param(wc.isStudentEBoardEnabled()));
		parameters.put("hms_two_way_enabled", param(wc.isStudentsSpeakAllowed()));
		parameters.put("media_format", "hms");
		parameters.put("media_type", wc.isStudentsVideoAllowed() ? "two-way-video" : "simulcast-only");
		parameters.put("hms_simulcast_restricted", param(!wc.isToolsToStudentsEnabled()));
		parameters.put("userstatus_enabled", param(wc.isUserStatusIndicatorsEnabled()));
		parameters.put("send_userstatus_updates", param(wc.isUserStatusUpdateInChatEnabled()));
		parameters.put("chatenable", param(wc.isStudentsChatAllowed()));
		parameters.put("privatechatenable", param(wc.isStudentsPrivateChatAllowed()));
		parameters.put("archive", param(false));
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		
		boolean success = evaluateOk(response);
		if(!success) handleError(response.getStatus(), null);
		
		if(wc.isGuestAccessAllowed()) {
			updateRights(null, roomId, "Student", true, false);
		} else if(function.equals("modifyClass")) {
			// only delete guest access if this is an already existing meeting
			updateRights(null, roomId, "Student", true, true);
		}
		
		return success;
	}

	@Override
	public boolean removeClassroom(String roomId, VCConfiguration config) {
		if(!existsClassroom(roomId, config)) return true;
		
		if(!loginAdmin()) throw new AssertException("Cannot login to Wimba Classroom. Please check module configuration and Wimba Classroom connectivity");
		
		// first delete recordings
		Map<String, String> mapRecordings = listRecordings(roomId);
		for(String key : mapRecordings.keySet()) {
			removeClassroomRecording(key);
		}
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", "deleteClass");
		parameters.put("target", PREFIX + roomId);
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		
		boolean success = evaluateOk(response);
		if(!success) handleError(response.getStatus(), null);
		
		return success;
	}

	@Override
	public URL createClassroomUrl(String roomId, Identity identity, VCConfiguration config) {
		URL url = null;
		URI uri = UriBuilder.fromUri(protocol + "://" + baseUrl).port(port)
			.path("check_wizard.pl").queryParam("channel", PREFIX + roomId).queryParam("hzA", token).build();
		try {
			url = uri.toURL();
		} catch (MalformedURLException e) {
			logWarn("Cannot create access URL to Wimba Classroom meeting for id \"" + PREFIX + roomId + "\" and user \"" + identity.getKey() + "\" ("+identity.getKey()+")", e);
		}
		
		return url;
	}

	@Override
	public URL createClassroomGuestUrl(String roomId, Identity identity, VCConfiguration config) {
		URL url = null;
		URI uri = UriBuilder.fromUri(protocol + "://" + baseUrl).port(port)
			.path("launcher.cgi").queryParam("room",PREFIX + roomId).build();
		try {
			url = uri.toURL();
		} catch (MalformedURLException e) {
			logWarn("Cannot create guest access URL to Wimba Classroom meeting for id \"" + PREFIX + roomId, e);
		}
		
		return url;
	}

	@Override
	public boolean existsClassroom(String roomId, VCConfiguration config) {
		if(!loginAdmin()) return false;
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", "listClass");
		parameters.put("filter00", "class_id");
		parameters.put("filter00value", PREFIX + roomId);
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		
		boolean success = evaluateOk(response);
		if(success) success = response.hasRecords() && response.numRecords() == 1;
		
		return success;
	}

	@Override
	public boolean login(Identity identity, String password) {
		if(!loginAdmin()) return false;
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", "getAuthToken");
		parameters.put("target", PREFIX + identity.getKey());
		parameters.put("nickname", identity.getUser().getProperty(UserConstants.FIRSTNAME, null).replaceAll("\\W", "_")+"_"+identity.getUser().getProperty(UserConstants.LASTNAME, null).replaceAll("\\W", "_"));
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		boolean success = evaluateOk(response);
		
		if(success & response.hasRecords()) {
			String responseToken = response.findRecord(TOKEN);
			if(responseToken != null) {
				token = responseToken;
			} else {
				success = false;
			}
		}
		
		return success;
	}
	
	private boolean loginAdmin() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", "NoOp");
		parameters.put("AuthType", "AuthCookieHandler");
		parameters.put("AuthName", "Horizon");
		parameters.put("credential_0", adminLogin);
		parameters.put("credential_1", adminPassword);
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		boolean success = evaluateOk(response);
		
		if(!success) {
			logError("Cannot login to Wimba Classroom. Please check module configuration and Wimba Classroom connectivity", null);
		}
		
		return success;
	}

	@Override
	public boolean createModerator(Identity identity, String roomId) {
		if(!loginAdmin()) throw new AssertException("Cannot login to Wimba Classroom. Please check module configuration and Wimba Classroom connectivity");
		
		boolean success = false;
		boolean exists = existsLogin(identity);
		
		// create login if necessary
		if(!exists) success = createLogin(identity);
		// update access rights
		if(exists | success) success = updateRights(identity, roomId, "Instructor", false, false);
		
		return success;
	}

	@Override
	public boolean createUser(Identity identity, String roomId) {
		if(!loginAdmin()) throw new AssertException("Cannot login to Wimba Classroom. Please check module configuration and Wimba Classroom connectivity");
		
		boolean success = false;
		boolean exists = existsLogin(identity);
		
		// create login if necessary
		if(!exists) success = createLogin(identity);
		// update access rights
		if(exists | success) success = updateRights(identity, roomId, "Student", false, false);
		
		return success;
	}
	
		/**
		 * 
		 * @param identity
		 * @param roomId
		 * @param role
		 * @param isGuest
		 * @param delete
		 * @return
		 */
	private boolean updateRights(Identity identity, String roomId, String role, boolean isGuest, boolean delete) {
		if (identity==null && !isGuest) return false;
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", delete ? "deleteRole" : "createRole");
		parameters.put("target", PREFIX + roomId);
		parameters.put("user_id", isGuest ? "Guest" : PREFIX + identity.getKey());
		parameters.put("role_id", role);
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		boolean success = evaluateOk(response);
		
		return success;
	}
	
	/**
	 * 
	 * @param identity
	 * @param roomId
	 * @param role
	 * @param group_id
	 * @param delete
	 * @return
	 */
	private boolean updateGroupRights(Identity identity, String roomId, String role, String group_id, boolean delete) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", delete ? "deleteRole" : "createRole");
		parameters.put("target", PREFIX + roomId);
		parameters.put("group_id", group_id);
		parameters.put("role_id", role);
		if (identity != null)
			parameters.put("user_id", PREFIX + identity.getKey());
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		boolean success = evaluateOk(response);
		
		return success;
	}
	
	private boolean createLogin(Identity identity) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", "createUser");
		parameters.put("target", PREFIX + identity.getKey());
		parameters.put("password_type", "P");// specified password, see Wimba Classroom 6.0 API Guide, page 8
		parameters.put("password", Encoder.md5hash(identity.getName() + "@" + Settings.getApplicationName()));
		parameters.put("first_name", identity.getUser().getProperty(UserConstants.FIRSTNAME, null));
		parameters.put("last_name", identity.getUser().getProperty(UserConstants.LASTNAME, null));
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		boolean success = evaluateOk(response);
		
		return success;
	}
	
	private boolean existsLogin(Identity identity) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", "listUser");
		parameters.put("attribute", "user_id");
		parameters.put("filter01", "user_id");
		parameters.put("filter01value", PREFIX + identity.getKey());
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		boolean success = evaluateOk(response);
		boolean exists = false;
		if(success) exists = response.numRecords() == 1;
		
		return exists;
	}

	@Override
	public boolean createGuest(Identity identity, String roomId) {
		throw new NotImplementedException("method createGuest not yet implemented");
	}

	@Override
	public Controller createDisplayController(UserRequest ureq, WindowControl wControl, String roomId, String name, String description,
			boolean isModerator, boolean readOnly, VCConfiguration config) {
		WimbaDisplayController displayCtr = new WimbaDisplayController(ureq, wControl, roomId, name, description, isModerator, (WimbaClassroomConfiguration) config, this);
		return displayCtr;
	}

	@Override
	public Controller createConfigController(UserRequest ureq, WindowControl wControl, String roomId, VCConfiguration config) {
		WimbaConfigController configCtr = new WimbaConfigController(ureq, wControl, roomId, this, (WimbaClassroomConfiguration) config);
		return configCtr;
	}
	
	public boolean isPreviewMode(String roomId, boolean isRecording) {
		if(!loginAdmin()) throw new AssertException("Cannot login to Wimba Classroom. Please check module configuration and Wimba Classroom connectivity");
		
		boolean mode = false;
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", "listClass");
		parameters.put("attribute", "preview");
		parameters.put("filter00", "class_id");
		parameters.put("filter00value", isRecording ? roomId : PREFIX + roomId);
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		boolean success = evaluateOk(response);
		
		if(success & response.hasRecords()) {
			String modeStr = response.getRecords().get(0).get("preview");
			if(modeStr == null || modeStr.equals("0")) mode = false;
			else if(modeStr.equals("1")) mode = true;
		}
		
		return mode;
	}
	
	public boolean setPreviewMode(String roomId, boolean enabled, boolean isRecording) {
		if(!loginAdmin()) throw new AssertException("Cannot login to Wimba Classroom. Please check module configuration and Wimba Classroom connectivity");
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", "modifyClass");
		parameters.put("target", isRecording ? roomId : PREFIX + roomId);
		parameters.put("preview", param(enabled));
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		
		boolean success = evaluateOk(response);
		if(!success) handleError(response.getStatus(), null);
		
		return success;
	}
	
	/**
	 * 
	 * @param roomId
	 * @return map with recordingId and name
	 */
	public Map<String, String> listRecordings(String roomId) {
		Map<String, String> mapKeyName = new HashMap<String, String>();
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", "listClass");
		parameters.put("attribute", "longname");
		parameters.put("filter00", "archive_of");
		parameters.put("filter00value", PREFIX + roomId);
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		boolean success = evaluateOk(response);
		
		if(success & response.hasRecords()) {
			List<Map<String, String>> records = response.getRecords();
			for(Map<String, String> record : records) {
				mapKeyName.put(record.get("class_id"), record.get("longname"));
			}
		}
		
		return mapKeyName;
	}
	
	public URL createClassroomRecordingUrl(String recordingId, Identity identity) {
		URL url = null;
		/* Notice: This is a very special and wimba specific case, the recordingId must not prefixed! */
		URI uri = UriBuilder.fromUri(protocol + "://" + baseUrl).port(port)
			.path("check_wizard.pl").queryParam("channel", recordingId).queryParam("hzA", token).build();
		try {
			url = uri.toURL();
		} catch (MalformedURLException e) {
			logWarn("Cannot create access URL to Wimba Classroom meeting for id \"" + recordingId + "\" and user \"" + identity.getKey() + "\" ("+identity.getKey()+")", e);
		}
		
		return url;
	}
	
	public boolean removeClassroomRecording(String recordingId) {
		if(!loginAdmin()) throw new AssertException("Cannot login to Wimba Classroom. Please check module configuration and Wimba Classroom connectivity");
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("function", "deleteClass");
		parameters.put("target", recordingId);
		String raw = sendRequest(parameters);
		WimbaResponse response = getResponseDocument(raw);
		
		boolean success = evaluateOk(response);
		if(!success) handleError(response.getStatus(), null);
		
		return success;
	}

	@Override
	public VCConfiguration createNewConfiguration() {
		// do a deep copy
		Object deepCopy = null;
		try {
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput;
			objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(defaultConfig);
			ByteArrayInputStream byteInput = new ByteArrayInputStream(byteOutput.toByteArray());
			ObjectInputStream objectInput = new ObjectInputStream(byteInput);
			deepCopy = objectInput.readObject();
		} catch (Exception e) {
			logError("Creation of default Wimba Classroom configuration failed.", e);
		}
		WimbaClassroomConfiguration newConfig = (WimbaClassroomConfiguration) deepCopy;
		
		return newConfig;
	}
	
	/**
	 * Creates Wimba Classroom specific direct access URLs for several services
	 * @param target
	 * @param roomId
	 * @return
	 */
	protected String createServiceUrl(String target, String roomId) {
		StringBuilder sb = new StringBuilder();
		sb.append(getProtocol()).append("://").append(getBaseUrl()).append(":").append(getPort());
		String classId = PREFIX + roomId;
		String hzA = "&hzA=" + token;
		if(target.equals(TARGET_OPEN_WIZARD)) {
			sb.append(ENDPOINT_WIZARD);
		} else if(target.equals(TARGET_OPEN_MANAGEROOM)) {
			sb.append("/admin/api/class/carousels?class_id=");
			sb.append(classId);
			sb.append(hzA);
		} else if(target.equals(TARGET_OPEN_POLLRESULTS)) {
			sb.append("/admin/api/class/results?class_id=");
			sb.append(classId);
			sb.append(hzA);
		} else if(target.equals(TARGET_OPEN_TRACKING)) {
			sb.append("/admin/api/server/tracking?mode=detailed&popup=1&channel=");
			sb.append(classId);
			sb.append(hzA);
		} else if(target.equals(TARGET_OPEN_ROOMSETTINGS)) {
			sb.append("/admin/api/class/properties.pl?class_id=");
			sb.append(classId);
			sb.append(hzA);
		} else if(target.equals(TARGET_OPEN_MEDIASETTINGS)) {
			sb.append("/admin/api/class/media.pl?class_id=");
			sb.append(classId);
			sb.append(hzA);
		}
		sb.append("&no_sidebar=1");
		
		return sb.toString();
	}
	
	/////////////////////////////
	// internal helper methods //
	/////////////////////////////
	
	private boolean evaluateOk(WimbaResponse response) {
		return response.getStatus().equals(StatusCode.OK);
	}
	
	private WimbaResponse getResponseDocument(String rawResponse) {
		StringReader input = new StringReader(rawResponse);
		LineNumberReader reader = new LineNumberReader(input);
		
		WimbaResponse response = new WimbaResponse();
		String line;
		Map<String, String> record = new HashMap<String, String>();
		try {
			// start with status code in first line
			line = reader.readLine();
			response.setStatus(getStatusCode(line));
			// read the records following
			while( (line=reader.readLine()) != null ) {
				if(line.equals(DELIM)) {
					// end of record
					response.addRecord(record);
					record = new HashMap<String, String>();
				} else {
					// regular part of a record
					String[] elem = line.split("=", 2);
					record.put(elem[0], elem[1]);
				}
			}
		} catch (IOException e) {
			logError("The Wimba Classroom response could not parsed. Raw response: " + rawResponse, e);
		}
		
		return response;
	}

	/**
	 * @param line
	 */
	private int getStatusCode(String line) {
		int code = StatusCode.UNDEFINED.getCode();
		if(line != null) {
			String extracted = line.split(" ", 2)[0];
			if(extracted != null && !extracted.isEmpty()) {
				try {
					code = Integer.parseInt(extracted);
				} catch(NumberFormatException e) {
					// nothing to do since code is pre-set
				}
			}
		}
		return code;
	}
	
	private String sendRequest(Map<String, String> parameters) {
    URL url = createRequestUrl(parameters);
    HttpURLConnection urlConn;

    try {
      urlConn = (HttpURLConnection) url.openConnection();
      // setup url connection
      urlConn.setDoOutput(true);
      urlConn.setUseCaches(false);
      urlConn.setInstanceFollowRedirects(false);
      // add content type
      urlConn.setRequestProperty("Content-Type", CONTENT_TYPE);
      // add cookie information
      if(cookie != null) urlConn.setRequestProperty("Cookie", cookie);

      // send request
      urlConn.connect();
      
      // detect redirect
      int code = urlConn.getResponseCode();
      boolean moved = code == HttpURLConnection.HTTP_MOVED_PERM | code == HttpURLConnection.HTTP_MOVED_TEMP;
      if(moved) {
      	String location = urlConn.getHeaderField("Location");
      	List<String> headerVals = urlConn.getHeaderFields().get("Set-Cookie");
      	for(String val : headerVals) {
      		if(val.startsWith(COOKIE)) cookie = val;
      	}
      	url = createRedirectUrl(location);
      	urlConn = (HttpURLConnection) url.openConnection();
      	urlConn.setRequestProperty("Cookie", cookie);
      }

      // read response
      BufferedReader input = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
      StringBuilder response= new StringBuilder();
      String line;
      while( (line = input.readLine()) != null ) response.append(line).append("\n");
      input.close();
    
      if(isLogDebugEnabled()) logDebug("Response: " + response);

      return response.toString();
    } catch (IOException e) {
      logError("Sending request to Wimba Classroom failed. Request: " + url.toString(), e);
      return "";
    }
  }
	
	private URL createRedirectUrl(String location) {
		UriBuilder ubu = UriBuilder.fromUri(protocol + "://" + baseUrl).port(port).path(location);

    URL url = null;
    try {
      url = ubu.build().toURL();
    } catch (Exception e) {
    	logWarn("Error while creating redirect URL for Wimba Classroom request.", e);
    	// try to build the URL in a naiv way below
    }
    if(url == null) {
    	// error case, try the naiv way
    	try {
    		String urlString = new String(protocol + "://" + baseUrl + ":" + port + location);
				url = new URL(urlString);
			} catch (MalformedURLException e) {
				logError("Error while creating URL for Wimba Classroom request. Please check the configuration!", e);
			}
    }
    
    if(isLogDebugEnabled()) logDebug("Redirect: " + url);

    return url;
	}
	
	private URL createRequestUrl(Map<String, String> parameters) {
		UriBuilder ubu = UriBuilder.fromUri(protocol + "://" + baseUrl).port(port).path("admin").path("api").path("api.pl");

    for(String key : parameters.keySet()) {
      ubu.queryParam(key, parameters.get(key));
    }

    URL url = null;
    try {
      url = ubu.build().toURL();
    } catch (Exception e) {
    	logWarn("Error while creating URL for Wimba Classroom request.", e);
    	// try to build the URL in a naiv way below
    }
    if(url == null) {
    	// error case, try the naiv way
    	try {
    		StringBuilder sb = new StringBuilder(protocol + "://" + baseUrl + ":" + port + "/admin/api/api.pl");
    		if(!parameters.isEmpty()) sb.append("?");
    		for(String key : parameters.keySet()) {
    			sb.append(key + "=" + parameters.get(key) + "&");
    		}
    		sb.replace(sb.length(), sb.length(), "");
				url = new URL(sb.toString());
			} catch (MalformedURLException e) {
				logError("Error while creating URL for Wimba Classroom request. Please check the configuration!", e);
			}
    }
    
    if(isLogDebugEnabled()) logDebug("Request: " + url);

    return url;
	}
	
	private void handleError(StatusCode status, Throwable cause) {
		logError("Request to Wimba Classroom returned error: " + status.getCode() + " (" + status.name() + ")", cause);
	}
	
	private String param(boolean bool) {
		return bool ? "1" : "0";
	}
	
	@Override
	public Map<String, String> getTemplates() {
		// no support for templating meetings
		return Collections.emptyMap();
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
	
	public void setDefaultConfig(WimbaClassroomConfiguration config) {
		defaultConfig = config;
		defaultConfig.setProviderId(providerId);
	}

  /////////////////////////////
  // getters used internally //
  /////////////////////////////
	
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

	public WimbaClassroomConfiguration getDefaultConfig() {
		return defaultConfig;
	}

}
//</OLATCE-103>