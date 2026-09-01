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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.nodes.MediaSiteCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.lti13.LTI13ContentItem;
import org.olat.ims.lti13.LTI13Context;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.ui.LTI13ChooseResourceController;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.mediasite.LtiVersion;
import org.olat.modules.mediasite.MediaSiteManager;
import org.olat.modules.mediasite.MediaSiteModule;
import org.olat.modules.mediasite.ui.MediaSiteAdminController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 14.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class MediaSiteConfigController extends FormBasicController {

	private static final String globalConfig = "config.global";
	private static final String localConfig = "config.local";
	
	private final ModuleConfiguration config;
	private final CourseEnvironment editCourseEnv;
	private final UserCourseEnvironment userCourseEnv;

	private MediaSiteCourseNode courseNode;
	
	private SingleSelection serverSelection;
	private FormLayoutContainer localConfigurationContainer;
	private TextElement presentationUrlElement;
	private FormLink previewLink;
	private FormLink openMyMediaSiteLink;
	private FormLink chooseContentLink;
	
	private MediaSiteAdminController localConfigurationCtrl;
	private LayoutMain3ColsPreviewController previewLayoutCtr;
	private LTI13ChooseResourceController chooseResourceCtrl;
	private CloseableModalController cmc;
	private LTI13Context activeLtiContext;
	
	@Autowired
	private MediaSiteModule mediaSiteModule;
	@Autowired
	private MediaSiteManager mediaSiteManager;
	@Autowired
	private LTI13Service lti13Service;
	
	// Main 'MediaSite configuration' tab controller for MediaSite course editor
	public MediaSiteConfigController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, MediaSiteCourseNode courseNode, ICourse course, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		
		this.config = config;
		this.courseNode = courseNode;
		this.editCourseEnv = course.getCourseEnvironment();
		this.userCourseEnv = userCourseEnv;
		
		initForm(ureq);
		loadConfig(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		setFormTitle("config.form.title");
		setFormContextHelp("manual_user/learningresources/Course_Element_Mediasite/");
		
		FormLayoutContainer configLayout = FormLayoutContainer.createDefaultFormLayout("configuration", getTranslator());
		configLayout.setRootForm(mainForm);
		formLayout.add(configLayout);
		
		SelectionValue global = new SelectionValue(globalConfig, translate("config.global"), 
				mediaSiteModule.getServerName() + " (" + ltiVersionDisplayString() + ")");
		SelectionValue local = new SelectionValue(localConfig, translate("config.local"), translate("config.local.description"));
		SelectionValues configValues;
		
		if (mediaSiteModule.isGlobalLoginEnabled()) {
			configValues = new SelectionValues(global, local);
		} else {
			configValues = new SelectionValues(local);
		}
		
		serverSelection = uifactory.addCardSingleSelectHorizontal("server.selection", configLayout, configValues.keys(), configValues.values(), configValues.descriptions(), null);
		serverSelection.addActionListener(FormEvent.ONCHANGE);
		
		localConfigurationContainer = FormLayoutContainer.createBareBoneFormLayout("localConfig", getTranslator());
		localConfigurationContainer.setRootForm(mainForm);
		
		formLayout.add(localConfigurationContainer);
		
		FormLayoutContainer presentationLayout = FormLayoutContainer.createDefaultFormLayout("presentationConfig", getTranslator());
		presentationLayout.setRootForm(mainForm);
		formLayout.add(presentationLayout);

		presentationUrlElement = uifactory.addTextElement("presentation.url", -1, null, presentationLayout);
		presentationUrlElement.setMandatory(true);
		
		FormLayoutContainer mediaButtonLayout = FormLayoutContainer.createButtonLayout("mediaButtons", getTranslator());
		mediaButtonLayout.setRootForm(mainForm);
		presentationLayout.add(mediaButtonLayout);
		
		chooseContentLink = uifactory.addFormLink("choose.content", mediaButtonLayout, Link.BUTTON);
		chooseContentLink.setVisible(false);
		previewLink = uifactory.addFormLink("preview.content", mediaButtonLayout, Link.BUTTON);
		openMyMediaSiteLink = uifactory.addFormLink("open.my.media.site", mediaButtonLayout, Link.BUTTON);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonLayout.setRootForm(mainForm);
		presentationLayout.add(buttonLayout);
		
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
	}

	private String ltiVersionDisplayString() {
		return mediaSiteModule.getLtiVersion().displayName();
	}

	private void loadConfig(UserRequest ureq) {
		if (mediaSiteModule.isGlobalLoginEnabled() && !config.getBooleanSafe(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN, false)) {
			localConfigurationContainer.setVisible(false);
			serverSelection.select(globalConfig, true);
		} else {
			localConfigurationCtrl = new MediaSiteAdminController(ureq, getWindowControl(), mainForm);
			listenTo(localConfigurationCtrl);
			localConfigurationCtrl.loadFromCourseNodeConfig(config);
			localConfigurationContainer.add("serverForm", localConfigurationCtrl.getInitialFormItem());
			
			localConfigurationContainer.setVisible(true);
			serverSelection.select(localConfig, true);
		}  
		presentationUrlElement.setValue(config.getStringValue(MediaSiteCourseNode.CONFIG_ELEMENT_ID));
		updateContentSelectionUi();
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		presentationUrlElement.setValue(mediaSiteManager.parseAlias(presentationUrlElement.getValue()));
		
		presentationUrlElement.clearError();
		if (!StringHelper.containsNonWhitespace(presentationUrlElement.getValue())) {
			allOk &= false;
			
			presentationUrlElement.setErrorKey("form.legende.mandatory");
		}
		
		return allOk;
	}
	
	

	@Override
	protected void formOK(UserRequest ureq) {
		if (localConfigurationCtrl != null && serverSelection.isKeySelected(localConfig)) {
			localConfigurationCtrl.saveToModuleConfiguration(ureq, config);
		} else {
			// Remove all local configuration, if global configuration is selected 
			config.setBooleanEntry(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN, false);
			config.remove(MediaSiteCourseNode.CONFIG_PRIVATE_KEY);
			config.remove(MediaSiteCourseNode.CONFIG_PRIVATE_SECRET);
			config.remove(MediaSiteCourseNode.CONFIG_USER_NAME_KEY);
			config.remove(MediaSiteCourseNode.CONFIG_SERVER_URL);
			config.remove(MediaSiteCourseNode.CONFIG_SUPRESS_AGREEMENT);
			config.remove(MediaSiteCourseNode.CONFIG_ADMINISTRATION_URL);
			config.remove(MediaSiteCourseNode.CONFIG_LTI13_TOOL_KEY);
			config.remove(MediaSiteCourseNode.CONFIG_LTI13_BASE_URL);
			config.remove(MediaSiteCourseNode.CONFIG_LTI13_ADMIN_URL);
			config.remove(MediaSiteCourseNode.CONFIG_LTI_VERSION);
		}
		
		config.setStringValue(MediaSiteCourseNode.CONFIG_ELEMENT_ID, mediaSiteManager.parseAlias(presentationUrlElement.getValue()));
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == previewLink) { // (UserRequest ureq, WindowControl wControl, MediaSiteCourseNode courseNode, ModuleConfiguration config, UserCourseEnvironment userCourseEnv, CourseEnvironment courseEnv, boolean showAdministration) {
			Controller mediaSiteRunCtr = new MediaSiteRunController(ureq, getWindowControl(), courseNode, config, editCourseEnv, false, userCourseEnv);
			previewLayoutCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null,
					mediaSiteRunCtr.getInitialComponent(), null);
			previewLayoutCtr.addDisposableChildController(mediaSiteRunCtr);
			previewLayoutCtr.activate();
			listenTo(previewLayoutCtr);
		} else if (source == openMyMediaSiteLink) {
			Controller mediaSiteRunCtr = new MediaSiteRunController(ureq, getWindowControl(), courseNode, config, editCourseEnv, true, userCourseEnv);
			previewLayoutCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null,
					mediaSiteRunCtr.getInitialComponent(), null);
			previewLayoutCtr.addDisposableChildController(mediaSiteRunCtr);
			previewLayoutCtr.activate();
			listenTo(previewLayoutCtr);
		} else if (source == serverSelection) {
			if (serverSelection.getSelectedKey().equals(localConfig)) {
				localConfigurationCtrl = new MediaSiteAdminController(ureq, getWindowControl(), mainForm);
				listenTo(localConfigurationCtrl);
				localConfigurationContainer.add("serverForm", localConfigurationCtrl.getInitialFormItem());			
				localConfigurationContainer.setVisible(true);
				
				localConfigurationCtrl.loadFromCourseNodeConfig(config);
			} else {
				removeAsListenerAndDispose(localConfigurationCtrl);
				localConfigurationCtrl = null;
				localConfigurationContainer.remove("serverForm");
				localConfigurationContainer.setVisible(false);
			}
			updateContentSelectionUi();
		} else if (source == presentationUrlElement) {
			presentationUrlElement.setValue(mediaSiteManager.parseAlias(presentationUrlElement.getValue()));
		} else if (source == chooseContentLink) {
			doChooseContent(ureq);
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == previewLayoutCtr) {
			removeAsListenerAndDispose(previewLayoutCtr);
		} else if (source == chooseResourceCtrl) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doApplySelectedContentItem();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		} else if (source == localConfigurationCtrl) {
			if (event == Event.CHANGED_EVENT) {
				updateContentSelectionUi();
			}
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(chooseResourceCtrl);
		removeAsListenerAndDispose(cmc);
		chooseResourceCtrl = null;
		cmc = null;
		activeLtiContext = null;
	}
	
	protected ModuleConfiguration getUpdatedConfig() {
		return config;
	}

	private void updateContentSelectionUi() {
		chooseContentLink.setVisible(getEffectiveLti13Deployment() != null);
	}

	/**
	 * @return the LTI 1.3 tool deployment currently applicable (global or local server,
	 *         whichever the form is presently set to), or null if none is available yet
	 *         (e.g. LTI 1.1, or a local LTI 1.3 setup that has not been saved once).
	 */
	private LTI13ToolDeployment getEffectiveLti13Deployment() {
		if (serverSelection.getSelectedKey().equals(globalConfig)) {
			if (mediaSiteModule.getLtiVersion() != LtiVersion.lti_1_3) {
				return null;
			}
			Long deploymentKey = mediaSiteModule.getLti13DeploymentKey();
			return deploymentKey == null ? null : lti13Service.getToolDeploymentByKey(deploymentKey);
		}

		if (localConfigurationCtrl == null || localConfigurationCtrl.getSelectedLtiVersion() != LtiVersion.lti_1_3) {
			return null;
		}

		String courseToolKeyStr = config.getStringValue(MediaSiteCourseNode.CONFIG_LTI13_TOOL_KEY);
		if (!StringHelper.containsNonWhitespace(courseToolKeyStr)) {
			return null;
		}

		LTI13Tool tool = lti13Service.getToolByKey(Long.valueOf(courseToolKeyStr));
		return tool == null ? null : mediaSiteManager.resolveCourseDeployment(config, tool);
	}

	private void doChooseContent(UserRequest ureq) {
		LTI13ToolDeployment deployment = getEffectiveLti13Deployment();
		if (deployment == null) {
			return;
		}

		RepositoryEntry courseEntry = editCourseEnv.getCourseGroupManager().getCourseEntry();
		String subIdent = courseNode.getIdent();
		activeLtiContext = lti13Service.getContext(courseEntry, subIdent);
		if (activeLtiContext == null) {
			activeLtiContext = mediaSiteManager.createLti13Context("", deployment, courseEntry, subIdent, null);
		}

		chooseResourceCtrl = new LTI13ChooseResourceController(ureq, getWindowControl(), activeLtiContext, -1);
		listenTo(chooseResourceCtrl);

		String title = translate("choose.content");
		cmc = new CloseableModalController(getWindowControl(), "close", chooseResourceCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doApplySelectedContentItem() {
		List<LTI13ContentItem> items = new ArrayList<>(lti13Service.getContentItems(activeLtiContext));
		if (items.isEmpty()) {
			return;
		}
		items.sort(Comparator.comparing(LTI13ContentItem::getCreationDate));
		LTI13ContentItem selected = items.remove(items.size() - 1);
		items.forEach(lti13Service::deleteContentItem);

		presentationUrlElement.setValue(mediaSiteManager.parseAlias(selected.getUrl()));
		presentationUrlElement.getComponent().setDirty(true);
	}

}
