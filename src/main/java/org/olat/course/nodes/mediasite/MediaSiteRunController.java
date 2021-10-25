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
package org.olat.course.nodes.mediasite;

import java.util.Map;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.MediaSiteCourseNode;
import org.olat.ims.lti.LTIContext;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti.ui.PostDataMapper;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.mediasite.MediaSiteModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 14.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class MediaSiteRunController extends BasicController {
	
	private final ModuleConfiguration config;
	private final boolean showAdministration;
	
	private Panel mainPanel;
	
	@Autowired
	private MediaSiteModule mediaSiteModule;
	@Autowired
	private LTIManager ltiManager;

	public MediaSiteRunController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		this(ureq, wControl, config, false);
	}
	
	public MediaSiteRunController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, boolean showAdministration) {
		super(ureq, wControl);
		
		this.config = config;
		this.showAdministration = showAdministration;
		
		mainPanel = new Panel("mediaSitePanel");
		runMediaSite(ureq);
		putInitialPanel(mainPanel);
	}
	
	private void runMediaSite(UserRequest ureq) {
		VelocityContainer container = createVelocityContainer("run");		
		
		String url;
		String oauth_key;
		String oauth_secret;
		
		if (config.getBooleanSafe(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN)) {
			if (showAdministration) {
				url = config.getStringValue(MediaSiteCourseNode.CONFIG_ADMINISTRATION_URL);
			} else {
				url = String.format(config.getStringValue(MediaSiteCourseNode.CONFIG_SERVER_URL), config.getStringValue(MediaSiteCourseNode.CONFIG_ELEMENT_ID));
			}
			
			oauth_key = config.getStringValue(MediaSiteCourseNode.CONFIG_PRIVATE_KEY);
			oauth_secret = config.getStringValue(MediaSiteCourseNode.CONFIG_PRIVATE_SECRET);
		} else {
			if (showAdministration) {
				url = mediaSiteModule.getAdministrationURL();
			} else {
				url = String.format(mediaSiteModule.getBaseURL(), config.getStringValue(MediaSiteCourseNode.CONFIG_ELEMENT_ID));
			}

			oauth_key = mediaSiteModule.getEnterpriseKey();
			oauth_secret = mediaSiteModule.getEnterpriseSecret();
		}
		
		LTIContext context = new MediaSiteContext();
		Map<String, String> unsignedProps = ltiManager.forgeLTIProperties(getIdentity(), getLocale(), context, true, true, false);
		
		String usernameKey = config.getStringValue(MediaSiteCourseNode.CONFIG_USER_NAME_KEY, mediaSiteModule.getUsernameProperty());
		unsignedProps.put(usernameKey, ureq.getUserSession().getIdentity().getUser().getNickName());
		
		boolean isDebug = config.getBooleanSafe(MediaSiteCourseNode.CONFIG_IS_DEBUG, false);
		Mapper contentMapper = new PostDataMapper(unsignedProps, url, oauth_key, oauth_secret, isDebug);
		
		String mapperUri = registerMapper(ureq, contentMapper);
		container.contextPut("mapperUri", mapperUri + "/");
		container.contextPut("height", "auto");
		container.contextPut("width", "auto");
		
		JSAndCSSComponent js = new JSAndCSSComponent("js", new String[] { "js/openolat/iFrameResizerHelper.js" }, null);
		container.put("js", js);
		
		mainPanel.setContent(container);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		
	}

	@Override
	protected void doDispose() {
		
	}

}
