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

import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
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
import org.olat.course.nodes.MediaSiteCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.mediasite.MediaSiteModule;
import org.olat.modules.mediasite.ui.MediaSiteAdminController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 14.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class MediaSiteConfigController extends FormBasicController {

	private static final String globalConfig = "config.global";
	private static final String localConfig = "config.local";
	
	private final ModuleConfiguration config;
	
	private SingleSelection serverSelection;
	private FormLayoutContainer localConfigurationContainer;
	private TextElement presentationUrlElement;
	private FormLink previewLink;
	private FormLink openMyMediaSiteLink;
	
	private MediaSiteAdminController localConfigurationCtrl;
	private LayoutMain3ColsPreviewController previewLayoutCtr;
	
	@Autowired
	private MediaSiteModule mediaSiteModule;
	
	public MediaSiteConfigController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		
		this.config = config;
		
		initForm(ureq);
		loadConfig();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FormLayoutContainer configLayout = FormLayoutContainer.createDefaultFormLayout("configuration", getTranslator());
		configLayout.setRootForm(mainForm);
		formLayout.add(configLayout);
		
		SelectionValue global = new SelectionValue(globalConfig, translate("config.global"), mediaSiteModule.getServerName());
		SelectionValue local = new SelectionValue(localConfig, translate("config.local"), translate("config.local.description"));
		SelectionValues configValues = new SelectionValues(global, local);
		
		serverSelection = uifactory.addCardSingleSelectHorizontal("server.selection", configLayout, configValues.keys(), configValues.values(), configValues.descriptions(), null);
		serverSelection.addActionListener(FormEvent.ONCHANGE);
		
		localConfigurationCtrl = new MediaSiteAdminController(ureq, getWindowControl(), mainForm);
		
		localConfigurationContainer = FormLayoutContainer.createBareBoneFormLayout("localConfig", getTranslator());
		localConfigurationContainer.setRootForm(mainForm);
		localConfigurationContainer.add(localConfigurationCtrl.getInitialFormItem());
		formLayout.add(localConfigurationContainer);
		
		FormLayoutContainer presentationLayout = FormLayoutContainer.createDefaultFormLayout("presentationConfig", getTranslator());
		presentationLayout.setRootForm(mainForm);
		formLayout.add(presentationLayout);

		presentationUrlElement = uifactory.addTextElement("presentation.url", -1, null, presentationLayout);
		
		FormLayoutContainer mediaButtonLayout = FormLayoutContainer.createButtonLayout("mediaButtons", getTranslator());
		mediaButtonLayout.setRootForm(mainForm);
		presentationLayout.add(mediaButtonLayout);
		
		previewLink = uifactory.addFormLink("preview.content", mediaButtonLayout, Link.BUTTON);
		openMyMediaSiteLink = uifactory.addFormLink("open.my.media.site", mediaButtonLayout, Link.BUTTON);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonLayout.setRootForm(mainForm);
		presentationLayout.add(buttonLayout);
		
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
	}
	
	private void loadConfig() {
		localConfigurationCtrl.loadFromCourseNodeConfig(config);
		
		if (config.getBooleanSafe(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN)) {
			localConfigurationContainer.setVisible(true);
			serverSelection.select(localConfig, true);
		} else {
			localConfigurationContainer.setVisible(false);
			serverSelection.select(globalConfig, true);
		}
		
		presentationUrlElement.setValue(config.getStringValue(MediaSiteCourseNode.CONFIG_ELEMENT_ID));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		localConfigurationCtrl.safeToModulConfiguration(ureq, config);
		config.setStringValue(MediaSiteCourseNode.CONFIG_ELEMENT_ID, presentationUrlElement.getValue());
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == previewLink) {
			Controller mediaSiteRunCtr = new MediaSiteRunController(ureq, getWindowControl(), config);
			previewLayoutCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null,
					mediaSiteRunCtr.getInitialComponent(), null);
			previewLayoutCtr.addDisposableChildController(mediaSiteRunCtr);
			previewLayoutCtr.activate();
			listenTo(previewLayoutCtr);
		} else if (source == openMyMediaSiteLink) {
			Controller mediaSiteRunCtr = new MediaSiteRunController(ureq, getWindowControl(), config, true);
			previewLayoutCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null,
					mediaSiteRunCtr.getInitialComponent(), null);
			previewLayoutCtr.addDisposableChildController(mediaSiteRunCtr);
			previewLayoutCtr.activate();
			listenTo(previewLayoutCtr);
		} else if (source == serverSelection) {
			localConfigurationContainer.setVisible(serverSelection.getSelectedKey().equals(localConfig));
			localConfigurationCtrl.loadFromCourseNodeConfig(config);
		} 	
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == previewLayoutCtr) {
			removeAsListenerAndDispose(previewLayoutCtr);
		}
		super.event(ureq, source, event);
	}
	
	protected ModuleConfiguration getUpdatedConfig() {
		return config;
	}

	@Override
	protected void doDispose() {
		// Nothing to do here
		
	}

}
