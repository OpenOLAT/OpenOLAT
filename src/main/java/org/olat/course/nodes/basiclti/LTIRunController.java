/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.basiclti;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

import org.imsglobal.basiclti.BasicLTIUtil;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Encoder;
import org.olat.core.util.SortedProperties;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.highscore.ui.HighScoreRunController;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.lti.LTIContext;
import org.olat.ims.lti.LTIDisplayOptions;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti.LTIModule;
import org.olat.ims.lti.ui.PostDataMapper;
import org.olat.ims.lti.ui.TalkBackMapper;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.properties.Property;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * is the controller for displaying contents in an iframe served by Basic LTI
 * @author guido
 * @author Charles Severance
 * 
 */
public class LTIRunController extends BasicController {
	private static final String PROP_NAME_DATA_EXCHANGE_ACCEPTED = "LtiDataExchageAccepted";
	
	private Link startButton;
	private final StackedPanel mainPanel;
	private VelocityContainer run;
	private VelocityContainer startPage;
	private BasicLTICourseNode courseNode;
	private ModuleConfiguration config;
	private final CourseEnvironment courseEnv;
	private final UserCourseEnvironment userCourseEnv;
	private SortedProperties userData = new SortedProperties(); 
	private SortedProperties customUserData = new SortedProperties(); 
	private Link acceptLink;
	private Link back;

	private boolean fullScreen;
	private ChiefController thebaseChief;
	
	private final Roles roles;
	private final LTIDisplayOptions display;
	
	@Autowired
	private LTIModule ltiModule;
	@Autowired
	private LTIManager ltiManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	/**
	 * Constructor for the preview in the editor of the course element.
	 *  
	 * @param wControl The window control
	 * @param config The course element configuration
	 * @param ureq The user request
	 * @param ltCourseNode The course element
	 * @param userCourseEnv The user course environment of the author
	 * @param courseEnv The course environment
	 */
	public LTIRunController(WindowControl wControl, ModuleConfiguration config, UserRequest ureq, BasicLTICourseNode ltCourseNode,
			UserCourseEnvironment userCourseEnv, CourseEnvironment courseEnv) {
		super(ureq, wControl, Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));
		this.courseNode = ltCourseNode;
		this.config = config;
		this.roles = ureq.getUserSession().getRoles();
		this.courseEnv = courseEnv;
		this.userCourseEnv = userCourseEnv;
		display = LTIDisplayOptions.iframe;

		run = createVelocityContainer("run");
		// push title and learning objectives, only visible on intro page
		run.contextPut("menuTitle", courseNode.getShortTitle());
		run.contextPut("displayTitle", courseNode.getLongTitle());
		
		if (courseNode.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD,false)){
			HighScoreRunController highScoreCtr = new HighScoreRunController(ureq, wControl, userCourseEnv, courseNode);
			if (highScoreCtr.isViewHighscore()) {
				Component highScoreComponent = highScoreCtr.getInitialComponent();
				run.put("highScore", highScoreComponent);							
			}
		}

		doBasicLTI(ureq, run);
		mainPanel = putInitialPanel(run);
	}

	/**
	 * Constructor for LTI run controller
	 * 
	 * @param wControl The window conntrol
	 * @param config The module configuration
	 * @param ureq The user request
	 * @param ltCourseNode The current course node
	 * @param userCourseEnv The course environment
	 */
	public LTIRunController(WindowControl wControl, ModuleConfiguration config, UserRequest ureq, BasicLTICourseNode ltCourseNode,
			UserCourseEnvironment userCourseEnv) {
 		super(ureq, wControl, Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));
		this.courseNode = ltCourseNode;
		this.config = config;
		this.userCourseEnv = userCourseEnv;
		this.roles = ureq.getUserSession().getRoles();
		this.courseEnv = userCourseEnv.getCourseEnvironment();
		String displayStr = config.getStringValue(BasicLTICourseNode.CONFIG_DISPLAY, "iframe");
		display = LTIDisplayOptions.valueOfOrDefault(displayStr); 

		mainPanel = new SimpleStackedPanel("ltiContainer");
		putInitialPanel(mainPanel);
		doRun(ureq);
	}

	/**
	 * Helper method to check if user has already accepted. this info is stored
	 * in a user property, the accepted values are stored as an MD5 hash (save
	 * space, privacy)
	 * 
	 * @param hash
	 *            MD5 hash with all user data
	 * @return true: user has already accepted for this hash; false: user has
	 *         not yet accepted or for other values
	 */
	private boolean checkHasDataExchangeAccepted(String hash) {
		boolean dataAccepted = false;
		CoursePropertyManager propMgr = courseEnv.getCoursePropertyManager();
		Property prop = propMgr.findCourseNodeProperty(this.courseNode, getIdentity(), null, PROP_NAME_DATA_EXCHANGE_ACCEPTED);
		if (prop != null) {
			// compare if value in property is the same as calculated today. If not, user as to accept again
			String storedHash = prop.getStringValue();
			if (storedHash != null && hash != null && storedHash.equals(hash)) {
				dataAccepted = true;
			} else {
				// remove property, not valid anymore
				propMgr.deleteProperty(prop);
			}
		}
		return dataAccepted;
	}

	/**
	 * Helper to initialize the ask-for-data-exchange screen
	 */
	private void doAskDataExchange() {
		VelocityContainer acceptPage = createVelocityContainer("accept");
		acceptPage.contextPut("userData", userData);
		acceptPage.contextPut("customUserData", customUserData);
		acceptLink = LinkFactory.createButton("accept", acceptPage, this);
		acceptLink.setPrimary(true);
		mainPanel.setContent(acceptPage);
	}
	
	/**
	 * Helper to save the user accepted data exchange
	 */
	private void storeDataExchangeAcceptance() {
		CoursePropertyManager propMgr = courseEnv.getCoursePropertyManager();
		String hash = createHashFromExchangeDataProperties();
		Property prop = propMgr.createCourseNodePropertyInstance(this.courseNode, getIdentity(), null, PROP_NAME_DATA_EXCHANGE_ACCEPTED, null, null, hash, null);
		propMgr.saveProperty(prop);
	}
	
	/**
	 * Helper to read all user data that is exchanged with LTI tool and saves it
	 * to the userData and customUserData properties fields
	 */
	private void createExchangeDataProperties() {
		final User user = getIdentity().getUser();
		//user data
		if (config.getBooleanSafe(LTIConfigForm.CONFIG_KEY_SENDNAME, false)) {
			String lastName = user.getProperty(UserConstants.LASTNAME, getLocale());
			if(StringHelper.containsNonWhitespace(lastName)) {
				userData.put("lastName", lastName);
			}
			String firstName = user.getProperty(UserConstants.FIRSTNAME, getLocale());
			if(StringHelper.containsNonWhitespace(firstName)) {
				userData.put("firstName", firstName);
			}
		}
		if (config.getBooleanSafe(LTIConfigForm.CONFIG_KEY_SENDEMAIL, false)) {
			String email = user.getProperty(UserConstants.EMAIL, getLocale());
			if(StringHelper.containsNonWhitespace(email)) {
				userData.put("email", email);
			}
		}
		// customUserData
		String custom = (String)config.get(LTIConfigForm.CONFIG_KEY_CUSTOM);
		if (StringHelper.containsNonWhitespace(custom)) {
			String[] params = custom.split("[\n;]");
			for (int i = 0; i < params.length; i++) {
				String param = params[i];
				if (!StringHelper.containsNonWhitespace(param)) {
					continue;
				}
				
				int pos = param.indexOf("=");
				if (pos < 1 || pos + 1 > param.length()) {
					continue;
				}
				
				String key = BasicLTIUtil.mapKeyName(param.substring(0, pos));
				if(!StringHelper.containsNonWhitespace(key)) {
					continue;
				}
				
				String value = param.substring(pos + 1).trim();
				if(value.length() < 1) {
					continue;
				}
				
				if(value.startsWith(LTIManager.USER_PROPS_PREFIX)) {
					String userProp = value.substring(LTIManager.USER_PROPS_PREFIX.length(), value.length());
					if(LTIManager.USER_NAME_PROP.equals(userProp)) {
						value = ltiManager.getUsername(getIdentity());
					} else {
						value = user.getProperty(userProp, null);
					}
					if (value!= null) {
						customUserData.put(userProp, value);
					}
				}
			}
		}
	}
	
	/**
	 * Helper to create an MD5 hash from the exchanged user properties. 
	 * @return
	 */
	private String createHashFromExchangeDataProperties() {
		String data = "";
		String hash = null;
		if (userData != null && userData.size() > 0) {
			Enumeration<Object> keys = userData.keys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				data += userData.getProperty(key);				
			}
		}
		if (customUserData != null && customUserData.size() > 0) {
			Enumeration<Object> keys = customUserData.keys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				data += customUserData.getProperty(key);				
			}
		}
		if (data.length() > 0) {
			hash = Encoder.md5hash(data);
		}
		if (isLogDebugEnabled()) {
			logDebug("Create accept hash::" + hash + " for data::" + data);
		}
		return hash;
	}
	
	/**
	 * Helper to initialize the LTI run view after user has accepted data exchange.
	 * @param ureq
	 */
	private void doRun(UserRequest ureq) {
		if (display == LTIDisplayOptions.window) {
			// Use other container for popup opening. Rest of code is the same
			run = createVelocityContainer("runPopup");			
		} else if (display == LTIDisplayOptions.fullscreen) {
			run = createVelocityContainer("run");
			back = LinkFactory.createLinkBack(run, this);
			run.put("back", back);
		} else {			
			run = createVelocityContainer("run");
		}
		// push title and learning objectives, only visible on intro page
		run.contextPut("menuTitle", courseNode.getShortTitle());
		run.contextPut("displayTitle", courseNode.getLongTitle());
		

		startPage = createVelocityContainer("overview");
		startPage.contextPut("menuTitle", courseNode.getShortTitle());
		startPage.contextPut("displayTitle", courseNode.getLongTitle());
		
		if (courseNode.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, false)){
			HighScoreRunController highScoreCtr = new HighScoreRunController(ureq, getWindowControl(), userCourseEnv, courseNode);
			if (highScoreCtr.isViewHighscore()) {
				Component highScoreComponent = highScoreCtr.getInitialComponent();
				startPage.put("highScore", highScoreComponent);							
			}
		}
		
		startButton = LinkFactory.createButton("start", startPage, this);
		startButton.setPrimary(true);

		boolean assessable = config.getBooleanSafe(BasicLTICourseNode.CONFIG_KEY_HAS_SCORE_FIELD, false)
				&& userCourseEnv.isParticipant();
		if(assessable) {
			startPage.contextPut("isassessable", assessable);
	    
			Integer attempts = courseAssessmentService.getAttempts(courseNode, userCourseEnv);
			startPage.contextPut("attempts", attempts);
	    
			ScoreEvaluation eval = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);
			Float cutValue = config.getFloatEntry(BasicLTICourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
			if(cutValue != null) {
				startPage.contextPut("hasPassedValue", Boolean.TRUE);
				startPage.contextPut("passed", eval.getPassed());
			}
			startPage.contextPut("score", eval.getScore());
			startPage.contextPut("hasScore", Boolean.TRUE);
			boolean resultsVisible = eval.getUserVisible() == null || eval.getUserVisible().booleanValue();
			startPage.contextPut("resultsVisible", Boolean.valueOf(resultsVisible));
			mainPanel.setContent(startPage);
		}
		
		// only run when user as already accepted to data exchange or no data 
		// has to be exchanged or when it is configured to not show the accept
		// dialog,
		createExchangeDataProperties();
		String dataExchangeHash = createHashFromExchangeDataProperties();
		Boolean skipAcceptLaunchPage = config.getBooleanEntry(BasicLTICourseNode.CONFIG_SKIP_ACCEPT_LAUNCH_PAGE);
		if (dataExchangeHash == null || checkHasDataExchangeAccepted(dataExchangeHash)
				|| (!ltiModule.isForceLaunchPage() && skipAcceptLaunchPage != null && skipAcceptLaunchPage.booleanValue()) ) {
			Boolean skipLaunchPage = config.getBooleanEntry(BasicLTICourseNode.CONFIG_SKIP_LAUNCH_PAGE);
			if(!ltiModule.isForceLaunchPage() && skipLaunchPage != null && skipLaunchPage.booleanValue()) {
				// start the content immediately
				courseAssessmentService.incrementAttempts(courseNode, userCourseEnv, Role.user);
				openBasicLTIContent(ureq);
			} else {
				// or show the start button
				mainPanel.setContent(startPage);
			}					
		} else {
			doAskDataExchange();
		}
	}
	
	private void openBasicLTIContent(UserRequest ureq) {
		// container is "run", "runFullscreen" or "runPopup" depending in configuration
		doBasicLTI(ureq, run);
		if (display == LTIDisplayOptions.fullscreen) {
			ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
			if (cc != null) {
				thebaseChief = cc;
				String businessPath = getWindowControl().getBusinessControl().getAsString();
				thebaseChief.getScreenMode().setMode(Mode.full, businessPath);
			}
			fullScreen = true;
			getWindowControl().pushToMainArea(run);
		} else {
			mainPanel.setContent(run);
		}
	}
	
	private void closeBasicLTI() {
		if (fullScreen && thebaseChief != null) {
			getWindowControl().pop();
			String businessPath = getWindowControl().getBusinessControl().getAsString();
			thebaseChief.getScreenMode().setMode(Mode.standard, businessPath);
		}
		mainPanel.setContent(startPage);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == startButton) {
			courseAssessmentService.incrementAttempts(courseNode, userCourseEnv, Role.user);
			openBasicLTIContent(ureq);
		} else if (source == acceptLink) {
			storeDataExchangeAcceptance();
			doRun(ureq);
		} else if(source == back) {
			closeBasicLTI();
		}
	}
	
	private String getUrl() {
		// put url in template to show content on extern page
		URL url = null;
		try {
			url = new URL((String)config.get(LTIConfigForm.CONFIGKEY_PROTO), (String) config.get(LTIConfigForm.CONFIGKEY_HOST), ((Integer) config
					.get(LTIConfigForm.CONFIGKEY_PORT)).intValue(), (String) config.get(LTIConfigForm.CONFIGKEY_URI));
		} catch (MalformedURLException e) {
			// this should not happen since the url was already validated in edit mode
			return null;
		}

		StringBuilder querySb = new StringBuilder(128);
		querySb.append(url.toString());
		// since the url only includes the path, but not the query (?...), append
		// it here, if any
		String query = (String) config.get(LTIConfigForm.CONFIGKEY_QUERY);
		if (query != null) {
			querySb.append("?");
			querySb.append(query);
		}
		return querySb.toString();
	}

	private void doBasicLTI(UserRequest ureq, VelocityContainer container) {
		String url = getUrl();
		container.contextPut("url", url == null ? "" : url);

		String oauth_consumer_key = (String) config.get(LTIConfigForm.CONFIGKEY_KEY);
		String oauth_secret = (String) config.get(LTIConfigForm.CONFIGKEY_PASS);
		String debug = (String) config.get(LTIConfigForm.CONFIG_KEY_DEBUG);
		String serverUri = Settings.createServerURI();
		String sourcedId = courseEnv.getCourseResourceableId() + "_" + courseNode.getIdent() + "_" + getIdentity().getKey();
		container.contextPut("sourcedId", sourcedId);
		OLATResource courseResource = courseEnv.getCourseGroupManager().getCourseResource();
		
		Mapper talkbackMapper = new TalkBackMapper(getLocale(), getWindowControl().getWindowBackOffice().getWindow().getGuiTheme().getBaseURI());
		String backMapperUrl = registerCacheableMapper(ureq, sourcedId + "_talkback", talkbackMapper);
		String backMapperUri = serverUri + backMapperUrl + "/";

		String outcomeMapperUri = null;
		if (userCourseEnv.isParticipant()) {
			Mapper outcomeMapper = new CourseNodeOutcomeMapper(getIdentity(), courseResource, courseNode.getIdent(),
					oauth_consumer_key, oauth_secret, sourcedId);
			String outcomeMapperUrl = registerCacheableMapper(ureq, sourcedId, outcomeMapper, LTIManager.EXPIRATION_TIME);
			outcomeMapperUri = serverUri + outcomeMapperUrl + "/";
		}

		boolean sendname = config.getBooleanSafe(LTIConfigForm.CONFIG_KEY_SENDNAME, false);
		boolean sendmail = config.getBooleanSafe(LTIConfigForm.CONFIG_KEY_SENDEMAIL, false);
		String ltiRoles = getLTIRoles();
		String target = config.getStringValue(BasicLTICourseNode.CONFIG_DISPLAY);
		String width = config.getStringValue(BasicLTICourseNode.CONFIG_WIDTH);
		String height = config.getStringValue(BasicLTICourseNode.CONFIG_HEIGHT);
		String custom = (String)config.get(LTIConfigForm.CONFIG_KEY_CUSTOM);
		container.contextPut("height", height);
		container.contextPut("width", width);
		LTIContext context = new LTICourseNodeContext(courseEnv, courseNode, ltiRoles,
				sourcedId, backMapperUri, outcomeMapperUri, custom, target, width, height);
		Map<String,String> unsignedProps = ltiManager.forgeLTIProperties(getIdentity(), getLocale(), context, sendname, sendmail, true);
		Mapper contentMapper = new PostDataMapper(unsignedProps, url, oauth_consumer_key, oauth_secret, "true".equals(debug));

		String mapperUri = registerMapper(ureq, contentMapper);
		container.contextPut("mapperUri", mapperUri + "/");
	}
	
	private String getLTIRoles() {
		if (roles.isGuestOnly()) {
			return "Guest";
		}
		boolean admin = userCourseEnv.isAdmin();
		if(admin) {
			String authorRole = config.getStringValue(BasicLTICourseNode.CONFIG_KEY_AUTHORROLE);
			if(StringHelper.containsNonWhitespace(authorRole)) {
				return authorRole;
			}
			return "Instructor,Administrator";
		}
		boolean coach = userCourseEnv.isCoach();
		if(coach) {
			String coachRole = config.getStringValue(BasicLTICourseNode.CONFIG_KEY_COACHROLE);
			if(StringHelper.containsNonWhitespace(coachRole)) {
				return coachRole;
			}
			return "Instructor";
		}
		
		String participantRole = config.getStringValue(BasicLTICourseNode.CONFIG_KEY_PARTICIPANTROLE);
		if(StringHelper.containsNonWhitespace(participantRole)) {
			return participantRole;
		}
		return "Learner";
	}
	
	@Override
	protected void doDispose() {
		//
	}
}