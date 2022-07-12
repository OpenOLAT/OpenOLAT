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
package org.olat.ims.lti13.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.lti.LTIDisplayOptions;
import org.olat.ims.lti.ui.LTIDisplayContentController;
import org.olat.ims.lti13.LTI13Constants;
import org.olat.ims.lti13.LTI13Key;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.springframework.beans.factory.annotation.Autowired;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

/**
 * 
 * Initial date: 17 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13DisplayController extends BasicController implements LTIDisplayContentController {

	private Link back;
	private VelocityContainer mainVC;
	
	private LTI13ToolDeployment toolDeployment;
	
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private LTI13Service lti13Service;
	
	public LTI13DisplayController(UserRequest ureq, WindowControl wControl,
			LTI13ToolDeployment toolDeployment, UserCourseEnvironment userCourseEnv) {
		this(ureq, wControl, toolDeployment, userCourseEnv.isAdmin(), userCourseEnv.isCoach(), userCourseEnv.isParticipant());
	}

	public LTI13DisplayController(UserRequest ureq, WindowControl wControl, LTI13ToolDeployment toolDeployment, boolean admin, boolean coach, boolean participant) {
		super(ureq, wControl);
		this.toolDeployment = toolDeployment;
		String loginHint = loginHint(admin, coach, participant);
		initLaunch(loginHint);
	}
	
	private void initLaunch(String loginHint) {
		mainVC = createVelocityContainer("launch");

		// display settings
		LTIDisplayOptions displayOption = toolDeployment.getDisplayOptions();
		mainVC.contextPut("newWindow", LTIDisplayOptions.window == displayOption);
		if(displayOption == LTIDisplayOptions.fullscreen) {
			back = LinkFactory.createLinkBack(mainVC, this);
		}
		if(toolDeployment.getDisplayHeight() != null && "auto".equals(toolDeployment.getDisplayHeight())) {
			mainVC.contextPut("height", toolDeployment.getDisplayHeight());
		}
		if(toolDeployment.getDisplayWidth() != null && "auto".equals(toolDeployment.getDisplayWidth())) {
			mainVC.contextPut("width", toolDeployment.getDisplayWidth());
		}

		// launch data
		LTI13Tool tool = toolDeployment.getTool();
		String targetLinkUri = tool.getToolUrl();
		if(StringHelper.containsNonWhitespace(toolDeployment.getTargetUrl())) {
			targetLinkUri = toolDeployment.getTargetUrl();
		}
		mainVC.contextPut("initiateLoginUrl", tool.getInitiateLoginUrl());
		mainVC.contextPut("iss", lti13Module.getPlatformIss());
		mainVC.contextPut("target_link_uri", targetLinkUri);
		mainVC.contextPut("login_hint", loginHint);
		mainVC.contextPut("lti_message_hint", getIdentity().getKey().toString());
		mainVC.contextPut("client_id", tool.getClientId());
		mainVC.contextPut("lti_deployment_id", toolDeployment.getDeploymentId());
		
		putInitialPanel(mainVC);
	}
	
	private String loginHint(boolean admin, boolean coach, boolean participant) {
		LTI13Key platformKey = lti13Service.getLastPlatformKey();
		
		JwtBuilder builder = Jwts.builder()
			//headers
			.setHeaderParam(LTI13Constants.Keys.TYPE, LTI13Constants.Keys.JWT)
			.setHeaderParam(LTI13Constants.Keys.ALGORITHM, platformKey.getAlgorithm())
			.setHeaderParam(LTI13Constants.Keys.KEY_IDENTIFIER, platformKey.getKeyId())
			//
			.claim("deploymentKey", toolDeployment.getKey())
			.claim("deploymentId", toolDeployment.getDeploymentId())
			.claim("courseadmin", Boolean.valueOf(admin))
			.claim("coach", Boolean.valueOf(coach))
			.claim("participant", Boolean.valueOf(participant));
		
		return builder
				.signWith(platformKey.getPrivateKey())
				.compact();
	}

	@Override
	public void openLtiContent(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == back) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
	}
}
