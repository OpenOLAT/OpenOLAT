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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.basiclti;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.lti.LTIContext;
import org.olat.ims.lti.LTIDisplayOptions;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti.ui.LTIDisplayContentController;
import org.olat.ims.lti.ui.PostDataMapper;
import org.olat.ims.lti.ui.TalkBackMapper;
import org.olat.modules.ModuleConfiguration;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI10DisplayController extends BasicController implements LTIDisplayContentController {
	
	private Link back;
	private final VelocityContainer run;

	private final Roles roles;
	private final BasicLTICourseNode courseNode;
	private final ModuleConfiguration config;
	private final UserCourseEnvironment userCourseEnv;
	private final CourseEnvironment courseEnv;
	
	@Autowired
	private LTIManager ltiManager;
	
	public LTI10DisplayController(UserRequest ureq, WindowControl wControl, BasicLTICourseNode courseNode,
			UserCourseEnvironment userCourseEnv, CourseEnvironment courseEnv, LTIDisplayOptions display) {
		super(ureq, wControl);
		
		this.courseEnv = courseEnv;
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		roles = ureq.getUserSession().getRoles();
		this.config = courseNode.getModuleConfiguration();
	
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
		putInitialPanel(run);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == back) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
	}

	@Override
	public void openLtiContent(UserRequest ureq) {
		doBasicLTI(ureq, run);
	}

	private void doBasicLTI(UserRequest ureq, VelocityContainer container) {
		String url = getUrl();
		container.contextPut("url", url == null ? "" : url);

		String oauthConsumerKey = (String) config.get(LTIConfigForm.CONFIGKEY_KEY);
		String oauthSecret = (String) config.get(LTIConfigForm.CONFIGKEY_PASS);
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
					oauthConsumerKey, oauthSecret, sourcedId);
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
		Mapper contentMapper = new PostDataMapper(unsignedProps, url, oauthConsumerKey, oauthSecret, "true".equals(debug));

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
	

}
