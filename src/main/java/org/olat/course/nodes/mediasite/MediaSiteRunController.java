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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.editor.EditorMainController;
import org.olat.course.nodes.MediaSiteCourseNode;
import org.olat.course.nodes.basiclti.LTI10DisplayController;
import org.olat.course.nodes.basiclti.LTIDataExchangeDisclaimerController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.lti.LTIContext;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti.ui.PostDataMapper;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.mediasite.MediaSiteModule;
import org.olat.properties.Property;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 14.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class MediaSiteRunController extends BasicController {
	
	private static final String PROP_NAME_DATA_EXCHANGE_ACCEPTED = "MediaSiteDataTransmissionAccepted";
	
	private final MediaSiteCourseNode courseNode;
	private final ModuleConfiguration config;
	private final boolean showAdministration;
	private final CourseEnvironment courseEnv;
	
	private Panel mainPanel;
	
	private LTIDataExchangeDisclaimerController disclaimerCtrl;
	
	@Autowired
	private MediaSiteModule mediaSiteModule;
	@Autowired
	private LTIManager ltiManager;

	/**
	 * Constructor for Run Controller
	 * 
	 * @param ureq
	 * @param wControl
	 * @param config
	 * @param userCourseEnv
	 */
	public MediaSiteRunController(UserRequest ureq, WindowControl wControl, MediaSiteCourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		this(ureq, wControl, courseNode, courseNode.getModuleConfiguration(), userCourseEnv.getCourseEnvironment(), false);
	}
	
	/**
	 * Constructor to show MediaSite Administration or preview in Course editor
	 * 
	 * @param ureq
	 * @param wControl
	 * @param config
	 * @param userCourseEnv
	 * @param courseEnv
	 * @param showAdministration
	 */
	public MediaSiteRunController(UserRequest ureq, WindowControl wControl, MediaSiteCourseNode courseNode, ModuleConfiguration config, CourseEnvironment courseEnv, boolean showAdministration) {
		super(ureq, wControl);
		
		this.courseNode = courseNode;
		this.config = config;
		this.showAdministration = showAdministration;
		this.courseEnv = courseEnv;
		
		setTranslator(Util.createPackageTranslator(LTI10DisplayController.class, getLocale(), getTranslator()));
		
		mainPanel = new Panel("mediaSitePanel");
		runMediaSite(ureq);
		putInitialPanel(mainPanel);
	}
	
	private void runMediaSite(UserRequest ureq) {
		boolean usesPrivateLogin = config.getBooleanSafe(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN);
		
		if (!usesPrivateLogin && !mediaSiteModule.isGlobalLoginEnabled()) {
			Translator pT = Util.createPackageTranslator(EditorMainController.class, ureq.getLocale());
			MessageController messageController = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, pT.translate("course.building.block.disabled.user"));
			//NodeRunConstructionResult ncr = new NodeRunConstructionResult(controller, null, null, null);
			//nclr = new NodeClickedRef(treeModel, true, newSelectedNodeId, null, courseNode, ncr, false);
			
			mainPanel.setContent(messageController.getInitialComponent());
			
			return;
		}
		
		showDataTranmissionScreenOrContent(ureq);
	}
	
	private void showContent(UserRequest ureq) {
		VelocityContainer container;
		
		if (showAdministration) {
			container = createVelocityContainer("runPopup");
		} else {
			container = createVelocityContainer("run");
		}
		
		
		boolean usesPrivateLogin = config.getBooleanSafe(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN);
		
		String url;
		String oauth_key;
		String oauth_secret;
		
		if (usesPrivateLogin) {
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
	
	private boolean checkHasDataExchangeAccepted(String hash) {
		boolean dataAccepted = false;
		CoursePropertyManager propMgr = courseEnv.getCoursePropertyManager();
		Property prop = propMgr.findCourseNodeProperty(courseNode, getIdentity(), null, PROP_NAME_DATA_EXCHANGE_ACCEPTED);
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
	 * Helper to save the user accepted data exchange
	 */
	private void storeDataExchangeAcceptance() {
		String hash = disclaimerCtrl.getHashData();
		CoursePropertyManager propMgr = courseEnv.getCoursePropertyManager();
		Property prop = propMgr.createCourseNodePropertyInstance(this.courseNode, getIdentity(), null, PROP_NAME_DATA_EXCHANGE_ACCEPTED, null, null, hash, null);
		propMgr.saveProperty(prop);
	}
	
	private void showDataTranmissionScreenOrContent(UserRequest ureq) {
		// only run when user as already accepted to data exchange or no data 
		// has to be exchanged or when it is configured to not show the accept
		// dialog
		boolean usesPrivateLogin = config.getBooleanSafe(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN);
		boolean skipAcceptLaunchPage = false;
		String customAttributes = "";
		
		if (usesPrivateLogin) {
			customAttributes = config.getStringValue(MediaSiteCourseNode.CONFIG_USER_NAME_KEY);
			skipAcceptLaunchPage = config.getBooleanSafe(MediaSiteCourseNode.CONFIG_SUPRESS_AGREEMENT);
		} else {
			customAttributes = mediaSiteModule.getUsernameProperty();
			skipAcceptLaunchPage = mediaSiteModule.isSupressDataTransmissionAgreement();
		}
		
		if (skipAcceptLaunchPage) {
			showContent(ureq);
			return;
		}
		
		disclaimerCtrl = new LTIDataExchangeDisclaimerController(ureq, getWindowControl(), true, true, customAttributes);
		listenTo(disclaimerCtrl);
		
		String dataExchangeHash = disclaimerCtrl.getHashData();
		
		if (dataExchangeHash != null && checkHasDataExchangeAccepted(dataExchangeHash)) {
			showContent(ureq);					
		} else {
			mainPanel.setContent(disclaimerCtrl.getInitialComponent());
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (disclaimerCtrl == source && event == Event.DONE_EVENT) {
			storeDataExchangeAcceptance();
			showContent(ureq);
		} 
		
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		
	}

}
