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
package org.olat.group.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.quota.QuotaConstants;
import org.olat.collaboration.CalendarToolSettingsController;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.collaboration.CollaborationToolsSettingsController;
import org.olat.collaboration.FolderToolSettingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGConfigToolsStepController extends StepFormBasicController {

	private String[] enableKeys = new String[]{"on", "off" };
	private String[] enableValues = new String[]{"on", "off" };

	private final List<SingleSelection> enableList = new ArrayList<>();
	private final List<MultipleSelectionElement> toolList = new ArrayList<>();
	
	@Autowired
	private QuotaManager quotaManager;
	
	public BGConfigToolsStepController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(CollaborationToolsSettingsController.class, getLocale(), getTranslator()));
		
		enableValues = new String[]{
				translate("config.tools.on"), translate("config.tools.off")
		};
		
		initForm(ureq);
	}
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean first = true;
		
		String containerPage = velocity_root + "/tool_config_container.html";
		Roles roles = ureq.getUserSession().getRoles();
		
		String[] availableTools = CollaborationToolsFactory.getInstance().getAvailableTools().clone();
		for (String k : availableTools) {
			String[] keys = new String[]{ "on" };
			String[] values = new String[]{ translate("collabtools.named." + k) };
			
			String i18n = first ? "config.tools.desc" : null;
			MultipleSelectionElement selectEl = uifactory.addCheckboxesHorizontal(k, i18n, formLayout, keys, values);
			selectEl.addActionListener(FormEvent.ONCHANGE);
			toolList.add(selectEl);
			
			ToolConfig config = new ToolConfig(k);
			config.configContainer = 
					FormLayoutContainer.createCustomFormLayout("config.container." + k, getTranslator(), containerPage);
			config.configContainer.contextPut("tool", k);
			config.configContainer.setVisible(false);
			config.configContainer.setRootForm(mainForm);
			formLayout.add(config.configContainer);
			config.enableEl = uifactory.addRadiosHorizontal("config.enable." + k, null, config.configContainer, enableKeys, enableValues);
			config.enableEl.addActionListener(FormEvent.ONCHANGE);
			config.enableEl.select("off", true);
			enableList.add(config.enableEl);
			config.enableEl.setUserObject(config);
			
			if (k.equals(CollaborationTools.TOOL_CALENDAR)) {
				config.calendarCtrl = new CalendarToolSettingsController(ureq, getWindowControl(), mainForm, CollaborationTools.CALENDAR_ACCESS_OWNERS);
				config.configContainer.add("calendar", config.calendarCtrl.getInitialFormItem());
				config.calendarCtrl.getInitialFormItem().setVisible(false);
			} else if (k.equals(CollaborationTools.TOOL_FOLDER)) {
				//add folder access configuration
				config.folderCtrl = new FolderToolSettingsController(ureq, getWindowControl(), mainForm, CollaborationTools.FOLDER_ACCESS_OWNERS);
				config.configContainer.add("folder", config.folderCtrl.getInitialFormItem());
				config.folderCtrl.getInitialFormItem().setVisible(false);
				
				//add quota configuration for admin only
				if(quotaManager.hasMinimalRolesToEditquota(roles)) {
					Quota quota = quotaManager.createQuota(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS, null, null);
					config.quotaCtrl = new BGConfigQuotaController(ureq, getWindowControl(), quota, mainForm);
					config.configContainer.add("quota", config.quotaCtrl.getInitialFormItem());
					config.quotaCtrl.getInitialFormItem().setVisible(false);
				}
			}

			selectEl.setUserObject(config);		
			first = false;
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(toolList.contains(source)) {
			ToolConfig config = (ToolConfig)source.getUserObject();
			config.toogleVisible();
		} else if (enableList.contains(source)) {
			SingleSelection enableEl = (SingleSelection)source;
			boolean visible = enableEl.isSelected(0);
			ToolConfig config = (ToolConfig)source.getUserObject();
			config.setAdditionalControllerVisibility(visible);	
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		BGConfigBusinessGroup configuration = new BGConfigBusinessGroup();
		Roles roles = ureq.getUserSession().getRoles();
		for(MultipleSelectionElement toolEl:toolList) {
			if(toolEl.isAtLeastSelected(1)) {
				ToolConfig config = (ToolConfig)toolEl.getUserObject();
				String tool = config.getToolKey();
				
				boolean enabled = config.enableEl.isSelected(0);
				if(enabled) {
					configuration.getToolsToEnable().add(tool);
				} else {
					configuration.getToolsToDisable().add(tool);
				}
				
				if (tool.equals(CollaborationTools.TOOL_CALENDAR)) {
					configuration.setCalendarAccess(config.calendarCtrl.getCalendarAccess());
				} else if (tool.equals(CollaborationTools.TOOL_FOLDER)) {
					configuration.setFolderAccess(config.folderCtrl.getFolderAccess());
					//only admin are allowed to configure quota
					if(quotaManager.hasMinimalRolesToEditquota(roles) && config.quotaCtrl != null) {
						Long quotaKB = config.quotaCtrl.getQuotaKB();
						Long ulLimit = config.quotaCtrl.getULLimit();
						Quota quota = quotaManager.createQuota(null, quotaKB, ulLimit);
						configuration.setQuota(quota);
					}
				}
			}
		}
		
		addToRunContext("configuration", configuration);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private class ToolConfig {
		
		private final String toolKey;
		
		private SingleSelection enableEl;
		private FormLayoutContainer configContainer;

		private BGConfigQuotaController quotaCtrl;
		private FolderToolSettingsController folderCtrl;
		private CalendarToolSettingsController calendarCtrl;
		
		public ToolConfig(String toolKey) {
			this.toolKey = toolKey;
		}
		
		public String getToolKey() {
			return toolKey;
		}

		public void toogleVisible() {
			boolean visible = configContainer.isVisible();
			configContainer.setVisible(!visible);
		}
		
		public void setAdditionalControllerVisibility(boolean visible) {
			if(calendarCtrl != null) {
				calendarCtrl.getInitialFormItem().setVisible(visible);
			}
			if(folderCtrl != null) {
				folderCtrl.getInitialFormItem().setVisible(visible);
			}
			if(quotaCtrl != null) {
				quotaCtrl.getInitialFormItem().setVisible(visible);
			}
			
		}
	}
}
