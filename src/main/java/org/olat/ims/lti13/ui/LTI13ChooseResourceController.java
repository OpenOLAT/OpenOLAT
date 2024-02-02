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
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.ims.lti13.LTI13Constants;
import org.olat.ims.lti13.LTI13Constants.MessageTypes;
import org.olat.ims.lti13.LTI13Constants.OpenOlatClaims;
import org.olat.ims.lti13.LTI13Context;
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
public class LTI13ChooseResourceController extends BasicController implements GenericEventListener {

	private final VelocityContainer mainVC;
	private final Link closeButton;
	private JSAndCSSComponent jsc;
	
	private final int addPosition;
	private LTI13Context ltiContext;
	private LTI13ToolDeployment toolDeployment;
	private final OLATResourceable ltiResourceOres;

	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private LTI13Service lti13Service;
	@Autowired
	private CoordinatorManager coordinatorManager;
	
	public LTI13ChooseResourceController(UserRequest ureq, WindowControl wControl, LTI13Context ltiContext, int addPosition) {
		super(ureq, wControl);
		this.addPosition = addPosition;
		this.ltiContext = ltiContext;
		this.toolDeployment = ltiContext.getDeployment();
		
		mainVC = createVelocityContainer("select_content");
		closeButton = LinkFactory.createButton("close", mainVC, this);
		putInitialPanel(mainVC);
		init();
		
		ltiResourceOres = OresHelper.createOLATResourceableInstance(MessageTypes.LTI_DEEP_LINKING_RESPONSE, ltiContext.getKey());
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, getIdentity(), ltiResourceOres);
	}
	
	@Override
	protected void doDispose() {
		coordinatorManager.getCoordinator().getEventBus().deregisterFor(this, ltiResourceOres);
		super.doDispose();
	}
	
	public int getAddPosition() {
		return addPosition;
	}

	private void init() {
		LTI13Key platformKey = lti13Service.getLastPlatformKey();
		// launch data
		LTI13Tool tool = toolDeployment.getTool();
		String targetLinkUri = tool.getToolUrl();
		if(StringHelper.containsNonWhitespace(ltiContext.getTargetUrl())) {
			targetLinkUri = ltiContext.getTargetUrl();
		}
		mainVC.contextPut("initiateLoginUrl", tool.getInitiateLoginUrl());
		mainVC.contextPut("iss", lti13Module.getPlatformIss());
		mainVC.contextPut("target_link_uri", targetLinkUri);
		mainVC.contextPut("login_hint", loginHint(platformKey));
		mainVC.contextPut("lti_message_hint", messageHint(platformKey));
		mainVC.contextPut("client_id", tool.getClientId());
		mainVC.contextPut("lti_deployment_id", toolDeployment.getDeploymentId());
		
		jsc = new JSAndCSSComponent("intervall", this.getClass(), 1500);
		mainVC.put("updatecontrol", jsc);
	}
	
	private String loginHint(LTI13Key platformKey) {
		JwtBuilder builder = Jwts.builder()
			.header()
				.type(LTI13Constants.Keys.JWT)
				.keyId(platformKey.getKeyId())
				.add(LTI13Constants.Keys.ALGORITHM, platformKey.getAlgorithm())
			//
			.and()
			.claim("contextKey", ltiContext.getKey())
			.claim("deploymentKey", toolDeployment.getKey())
			.claim("deploymentId", toolDeployment.getDeploymentId());
		
		return builder
				.signWith(platformKey.getPrivateKey())
				.compact();
	}
	
	private String messageHint(LTI13Key platformKey) {
		JwtBuilder builder = Jwts.builder()
			.header()
				.type(LTI13Constants.Keys.JWT)
				.add(LTI13Constants.Keys.ALGORITHM, platformKey.getAlgorithm())
				.keyId(platformKey.getKeyId())
			.and()
			.claim(OpenOlatClaims.IDENTITY_KEY, getIdentity().getKey())
			.claim(OpenOlatClaims.MESSAGE_TYPE, MessageTypes.LTI_DEEP_LINKING_REQUEST);
		if(ltiContext.getEntry() != null) {
			builder = builder.claim(OpenOlatClaims.REPOSITORY_ENTRY_KEY, ltiContext.getEntry().getKey());
			if(StringHelper.containsNonWhitespace(ltiContext.getSubIdent())) {
				builder = builder.claim(OpenOlatClaims.SUB_IDENT, ltiContext.getSubIdent());
			}
		} else if(ltiContext.getBusinessGroup() != null) {
			builder = builder.claim(OpenOlatClaims.BUSINESS_GROUP_KEY, ltiContext.getBusinessGroup().getKey());
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
	public void event(Event event) {
		if(event instanceof LTI13ChooseResourceEvent cre
				&& toolDeployment.getDeploymentId().equals(cre.getDeploymentId())
				&& ltiContext.getKey().equals(cre.getLtiContextKey())) {
			UserRequest ureq = new SyntheticUserRequest(getIdentity(), getLocale());
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(closeButton == source) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}
