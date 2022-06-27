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
package org.olat.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 01.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HomePageSettingsController extends FormBasicController {
	
	private static final String usageIdentifier = ProfileFormController.class.getCanonicalName();
	private static final String[] checkKeys = new String[]{ "on" }; 
	private static final String[] checkValues = new String[]{ "" }; 
	
	private Identity identityToModify;
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;

	private HomePageFormItem previewEl;
	private FormLayoutContainer previewContainer;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private HomePageConfigManager hpcm;
	
	public HomePageSettingsController(UserRequest ureq, WindowControl wControl,
			Identity identityToModify, boolean isAdministrativeUser) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.identityToModify = identityToModify;
		this.isAdministrativeUser = isAdministrativeUser;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifier, isAdministrativeUser);
		
		initForm(ureq);
	}
	
	public void updateIdentityToModify(UserRequest ureq, Identity identity) {
		this.identityToModify = identity;
		updatePreview(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean firstGroup = true;
		
		List<UserPropertyHandler> homepagePropertyHanders = userManager.getUserPropertyHandlersFor(HomePageConfig.class.getCanonicalName(), isAdministrativeUser);

		Map<String, FormLayoutContainer> groupContainerMap = new HashMap<>();
		HomePageConfig conf = hpcm.loadConfigFor(identityToModify);
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) {
				continue;
			}
			
			// add spacer if necessary (i.e. when group name changes)
			String group = userPropertyHandler.getGroup();
			FormLayoutContainer groupContainer;
			if(groupContainerMap.containsKey(group)) {
				groupContainer = groupContainerMap.get(group);
			} else {
				groupContainer = FormLayoutContainer.createDefaultFormLayout("group." + group, getTranslator());
				groupContainer.setFormTitle(translate("form.group." + group));
				formLayout.add(groupContainer);
				groupContainerMap.put(group, groupContainer);
				if(firstGroup) {
					groupContainer.setFormContextHelp("manual_user/personal/Configuration/");
					firstGroup = false;
				}
			}
			
			if (homepagePropertyHanders.contains(userPropertyHandler)) {
				// add checkbox to container if configured for homepage usage identifier
				String checkboxName = userPropertyHandler.getName();
				MultipleSelectionElement publishCheckbox = uifactory.addCheckboxesHorizontal(checkboxName,
						userPropertyHandler.i18nFormElementLabelKey(),
						groupContainer, checkKeys , checkValues);
				
				boolean isEnabled = conf.isEnabled(userPropertyHandler.getName());
				publishCheckbox.select(checkKeys[0], isEnabled);
				publishCheckbox.setUserObject(userPropertyHandler.getName());
							
				// Mandatory homepage properties can not be changed by user
				if (userManager.isMandatoryUserProperty(HomePageConfig.class.getCanonicalName(), userPropertyHandler)) {
					publishCheckbox.select(checkKeys[0], true);
					publishCheckbox.setEnabled(false);
				} else {
					publishCheckbox.addActionListener(FormEvent.ONCHANGE);
				}
			}
		}
		
		String previewPage = velocity_root + "/homepage_preview.html";
		previewContainer = FormLayoutContainer.createCustomFormLayout("preview", getTranslator(), previewPage);
		previewContainer.setFormTitle(translate("tab.preview"));
		previewContainer.setRootForm(mainForm);
		formLayout.add(previewContainer);
		updatePreview(ureq);
	}

	protected void updatePreview(UserRequest ureq) {
		HomePageConfig conf = hpcm.loadConfigFor(identityToModify);
		updatePreview(ureq, conf);
	}
	
	protected void updatePreview(UserRequest ureq, HomePageConfig conf) {
		if(previewEl != null) {
			previewContainer.remove(previewEl);
			removeAsListenerAndDispose(previewEl.getController());
		}
		
		HomePageDisplayController displayCtrl = new HomePageDisplayController(ureq, getWindowControl(), identityToModify, conf);
		listenTo(displayCtrl);
		previewEl = new HomePageFormItem(displayCtrl);
		previewContainer.add(previewEl);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof MultipleSelectionElement) {
			MultipleSelectionElement publishCheckbox = (MultipleSelectionElement)source;
			if (publishCheckbox.isEnabled()) {
				boolean enabled = publishCheckbox.isAtLeastSelected(1);
				String propName = (String)publishCheckbox.getUserObject();
				
				//load and update config
				HomePageConfig conf = hpcm.loadConfigFor(identityToModify);
				conf.setEnabled(propName, enabled);
				hpcm.saveConfigTo(identityToModify, conf);
				updatePreview(ureq, conf);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	public class HomePageFormItem extends FormItemImpl {
		
		private final HomePageDisplayController previewCtrl;
		
		public HomePageFormItem(HomePageDisplayController previewCtrl) {
			super("homepage");
			this.previewCtrl = previewCtrl;
		}
		
		public HomePageDisplayController getController() {
			return previewCtrl;
		}

		@Override
		protected Component getFormItemComponent() {
			return previewCtrl.getInitialComponent();
		}

		@Override
		protected void rootFormAvailable() {
			//
		}

		@Override
		public void evalFormRequest(UserRequest ureq) {
			//
		}

		@Override
		public void reset() {
			//
		}
	}
}