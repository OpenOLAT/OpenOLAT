/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
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
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti13.LTI13Constants;
import org.olat.ims.lti13.LTI13Constants.MessageTypes;
import org.olat.ims.lti13.LTI13Constants.OpenOlatClaims;
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
 * Initial date: 5 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LTI13ChooseResourceController extends BasicController {

	private final VelocityContainer mainVC;
	private final Link closeButton;
	
	private LTI13ToolDeployment toolDeployment;

	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private LTI13Service lti13Service;
	
	public LTI13ChooseResourceController(UserRequest ureq, WindowControl wControl, LTI13ToolDeployment toolDeployment) {
		super(ureq, wControl);
		this.toolDeployment = toolDeployment;
		
		mainVC = createVelocityContainer("select_content");
		closeButton = LinkFactory.createButton("close", mainVC, this);
		putInitialPanel(mainVC);
		init();
	}
	
	private void init() {
		LTI13Key platformKey = lti13Service.getLastPlatformKey();
		// launch data
		LTI13Tool tool = toolDeployment.getTool();
		String targetLinkUri = tool.getToolUrl();
		if(StringHelper.containsNonWhitespace(toolDeployment.getTargetUrl())) {
			targetLinkUri = toolDeployment.getTargetUrl();
		}
		mainVC.contextPut("initiateLoginUrl", tool.getInitiateLoginUrl());
		mainVC.contextPut("iss", lti13Module.getPlatformIss());
		mainVC.contextPut("target_link_uri", targetLinkUri);
		mainVC.contextPut("login_hint", loginHint(platformKey));
		mainVC.contextPut("lti_message_hint", messageHint(platformKey));
		mainVC.contextPut("client_id", tool.getClientId());
		mainVC.contextPut("lti_deployment_id", toolDeployment.getDeploymentId());
	}
	
	private String loginHint(LTI13Key platformKey) {
		JwtBuilder builder = Jwts.builder()
			//headers
			.setHeaderParam(LTI13Constants.Keys.TYPE, LTI13Constants.Keys.JWT)
			.setHeaderParam(LTI13Constants.Keys.ALGORITHM, platformKey.getAlgorithm())
			.setHeaderParam(LTI13Constants.Keys.KEY_IDENTIFIER, platformKey.getKeyId())
			//
			.claim("deploymentKey", toolDeployment.getKey())
			.claim("deploymentId", toolDeployment.getDeploymentId());
		
		return builder
				.signWith(platformKey.getPrivateKey())
				.compact();
	}
	
	private String messageHint(LTI13Key platformKey) {
		JwtBuilder builder = Jwts.builder()
			//headers
			.setHeaderParam(LTI13Constants.Keys.TYPE, LTI13Constants.Keys.JWT)
			.setHeaderParam(LTI13Constants.Keys.ALGORITHM, platformKey.getAlgorithm())
			.setHeaderParam(LTI13Constants.Keys.KEY_IDENTIFIER, platformKey.getKeyId())
			//
			.claim(OpenOlatClaims.IDENTITY_KEY, getIdentity().getKey())
			.claim(OpenOlatClaims.MESSAGE_TYPE, MessageTypes.LTI_DEEP_LINKING_REQUEST);
		if(toolDeployment.getEntry() != null) {
			builder = builder.claim(OpenOlatClaims.REPOSITORY_ENTRY_KEY, toolDeployment.getEntry().getKey());
			if(StringHelper.containsNonWhitespace(toolDeployment.getSubIdent())) {
				builder = builder.claim(OpenOlatClaims.SUB_IDENT, toolDeployment.getSubIdent());
			}
		} else if(toolDeployment.getBusinessGroup() != null) {
			builder = builder.claim(OpenOlatClaims.BUSINESS_GROUP_KEY, toolDeployment.getBusinessGroup().getKey());
		}
		
		String closeUrl = getCloseUrl();
		builder = builder.claim(OpenOlatClaims.RETURN_URL, closeUrl);
		
		return builder
				.signWith(platformKey.getPrivateKey())
				.compact();
	}
	
	private String getCloseUrl() {
		URLBuilder ubu = getWindow().getURLBuilder().createCopyFor(closeButton);
		StringOutput sb = new StringOutput();
		ubu.buildURI(sb, AJAXFlags.MODE_NORMAL);
		return Settings.createServerURI() + "" + sb.toString();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(closeButton == source) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}
