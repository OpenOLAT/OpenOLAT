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
import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.imsglobal.basiclti.BasicLTIUtil;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.WebappHelper;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * Description:<br>
 * is the controller for displaying contents in an iframe served by Basic LTI
 * @author guido
 * @author Charles Severance
 * 
 */
public class LTIRunController extends BasicController {

	private VelocityContainer run;
	private BasicLTICourseNode courseNode;
	private Panel main;
	private ModuleConfiguration config;
	private CourseEnvironment courseEnv;
	private String postData;
	private Mapper contentMapper;
	private Mapper talkbackMapper;

	/**
	 * Constructor for tunneling run controller
	 * 
	 * @param wControl
	 * @param config The module configuration
	 * @param ureq The user request
	 * @param ltCourseNode The current course node
	 * @param cenv the course environment
	 */
	public LTIRunController(WindowControl wControl, ModuleConfiguration config, UserRequest ureq, BasicLTICourseNode ltCourseNode,
			CourseEnvironment cenv) {
		super(ureq, wControl);
		this.courseNode = ltCourseNode;
		this.config = config;
		this.courseEnv = cenv;

		main = new Panel("ltrunmain");
		doBasicLTI(ureq);
		this.putInitialPanel(main);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		//nothing to do
	}

	protected void event(UserRequest ureq, Controller source, Event event) {
	// nothing to do
	}

	private void doBasicLTI(UserRequest ureq) {
		run = createVelocityContainer("run");

		// push title and learning objectives, only visible on intro page
		run.contextPut("menuTitle", courseNode.getShortTitle());
		run.contextPut("displayTitle", courseNode.getLongTitle());

		// put url in template to show content on extern page
		URL url = null;
		try {
			url = new URL((String) config.get(LTIConfigForm.CONFIGKEY_PROTO), (String) config.get(LTIConfigForm.CONFIGKEY_HOST), ((Integer) config
					.get(LTIConfigForm.CONFIGKEY_PORT)).intValue(), (String) config.get(LTIConfigForm.CONFIGKEY_URI));
		} catch (MalformedURLException e) {
			// this should not happen since the url was already validated in edit mode
			run.contextPut("url", "");
		}
		if (url != null) {
			StringBuilder sb = new StringBuilder(128);
			sb.append(url.toString());
			// since the url only includes the path, but not the query (?...), append
			// it here, if any
			String query = (String) config.get(LTIConfigForm.CONFIGKEY_QUERY);
			if (query != null) {
				sb.append("?");
				sb.append(query);
			}
			run.contextPut("url", sb.toString());

			String key = (String) config.get(LTIConfigForm.CONFIGKEY_KEY);
			String pass = (String) config.get(LTIConfigForm.CONFIGKEY_PASS);
			String debug = (String) config.get(LTIConfigForm.CONFIG_KEY_DEBUG);

			talkbackMapper = new Mapper() {

				@Override
				public MediaResource handle(String relPath, HttpServletRequest request) {
					/**
					 * this is the place for error handling coming from the LTI tool, depending on error state
					 * may present some information for the user or just add some information to the olat.log file
					 */
					StringMediaResource mediares = new StringMediaResource();
					StringBuilder sb = new StringBuilder();
					sb.append("lti_msg: ").append(request.getParameter("lti_msg")).append("<br/>");
					sb.append("lti_errormsg: ").append(request.getParameter("lti_errormsg")).append("<br/>");
					sb.append("lti_log: ").append(request.getParameter("lti_log")).append("<br/>");
					sb.append("lti_errorlog: ").append(request.getParameter("lti_errorlog")).append("<br/>");
					mediares.setData("<html><body>" + sb.toString() + "</body></html>");
					mediares.setContentType("text/html");
					mediares.setEncoding("UTF-8");
					return mediares;
				}
			};
			String backMapperUrl = registerMapper(talkbackMapper);

			String serverUri = ureq.getHttpReq().getScheme()+"://"+ureq.getHttpReq().getServerName()+":"+ureq.getHttpReq().getServerPort();

			Properties props = LTIProperties(ureq);
			setProperty(props, "launch_presentation_return_url", serverUri + backMapperUrl + "/");
			props = BasicLTIUtil.signProperties(props, sb.toString(), "POST", key, pass, null, null, null);

			postData = BasicLTIUtil.postLaunchHTML(props, sb.toString(), "true".equals(debug));

			contentMapper = new Mapper() {
				@Override
				public MediaResource handle(String relPath, HttpServletRequest request) {
					StringMediaResource mediares = new StringMediaResource();
					mediares.setData(postData);
					mediares.setContentType("text/html");
					mediares.setEncoding("UTF-8");
					return mediares;
				}

			};
			logDebug("Basic LTI Post data: "+postData, null);

		}
		String mapperUri = registerMapper(contentMapper);
		run.contextPut("mapperUri", mapperUri + "/");

		main.setContent(run);
	}

	

	private Properties LTIProperties(UserRequest ureq) {
		final Identity ident = ureq.getIdentity();
		final Locale loc = ureq.getLocale();
		User u = ident.getUser();
		final String lastName = u.getProperty(UserConstants.LASTNAME, loc);
		final String firstName = u.getProperty(UserConstants.FIRSTNAME, loc);
		final String email = u.getProperty(UserConstants.EMAIL, loc);

		String custom = (String) config.get(LTIConfigForm.CONFIG_KEY_CUSTOM);
		boolean sendname = Boolean.valueOf((String)config.get(LTIConfigForm.CONFIG_KEY_SENDNAME));
		boolean sendemail = Boolean.valueOf((String) config.get(LTIConfigForm.CONFIG_KEY_SENDEMAIL));

		Properties props = new Properties();
		setProperty(props, "resource_link_id", courseNode.getIdent());
		setProperty(props, "resource_link_title", courseNode.getShortTitle());
		setProperty(props, "resource_link_description", courseNode.getLongTitle());
		setProperty(props, "user_id", u.getKey() + "");
		setProperty(props, "launch_presentation_locale", loc.toString());
		setProperty(props, "launch_presentation_document_target", "iframe");

		if (sendname) {
			setProperty(props, "lis_person_name_given", firstName);
			setProperty(props, "lis_person_name_family", lastName);
			setProperty(props, "lis_person_name_full", firstName+" "+lastName);
		}
		if (sendemail) {
			setProperty(props, "lis_person_contact_email_primary", email);
		}

		setProperty(props, "roles", setRoles(ureq.getUserSession().getRoles()));
		setProperty(props, "context_id", courseEnv.getCourseResourceableId().toString());
		setProperty(props, "context_label", courseEnv.getCourseTitle());
		setProperty(props, "context_title", courseEnv.getCourseTitle());
		setProperty(props, "context_type", "CourseSection");

		// Pull in and parse the custom parameters
		// Note to Chuck - move this into BasicLTI Util
		if (custom != null) {
			String[] params = custom.split("[\n;]");
			for (int i = 0; i < params.length; i++) {
				String param = params[i];
				if (param == null) continue;
				if (param.length() < 1) continue;
				int pos = param.indexOf("=");
				if (pos < 1) continue;
				if (pos + 1 > param.length()) continue;
				String key = BasicLTIUtil.mapKeyName(param.substring(0, pos));
				if (key == null) continue;
				String value = param.substring(pos + 1);
				value = value.trim();
				if (value.length() < 1) continue;
				if (value == null) continue;
				setProperty(props, "custom_" + key, value);
			}
		}

		setProperty(props, "tool_consumer_instance_guid", Settings.getServerconfig("server_fqdn"));
		setProperty(props, "tool_consumer_instance_name", WebappHelper.getInstanceId());
		setProperty(props, "tool_consumer_instance_contact_email", WebappHelper.getMailConfig("mailSupport"));
		

		return props;
	}

	public static void setProperty(Properties props, String key, String value) {
		if (value == null) return;
		if (value.trim().length() < 1) return;
		props.setProperty(key, value);
	}

	/**
	 * A comma-separated list of URN values for roles. If this list is non-empty,
	 * it should contain at least one role from the LIS System Role, LIS
	 * Institution Role, or LIS Context Role vocabularies (See Appendix A of
	 * LTI_BasicLTI_Implementation_Guide_rev1.pdf).
	 * 
	 * @param roles
	 * @return
	 */
	private String setRoles(Roles roles) {
		StringBuilder rolesStr;
		if (roles.isGuestOnly()) {
			rolesStr = new StringBuilder("Guest");
		} else {
			rolesStr = new StringBuilder("Learner");
			boolean coach = courseEnv.getCourseGroupManager().isIdentityCourseCoach(getIdentity());
			if (coach) {
				rolesStr.append(",").append("Instructor");
			}
			boolean admin = courseEnv.getCourseGroupManager().isIdentityCourseAdministrator(getIdentity());
			if (roles.isOLATAdmin() || admin) {
				rolesStr.append(",").append("Administrator");
			}
		}
		
		return rolesStr.toString();
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}

}
