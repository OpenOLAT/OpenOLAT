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
package org.olat.group.ui.edit;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.GroupLoggingAction;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupAreasController extends FormBasicController {

	private BusinessGroup businessGroup;
	private final BGAreaManager areaManager;
	private final BusinessGroupService businessGroupService;
	
	public BusinessGroupAreasController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl);
		
		this.businessGroup = businessGroup;
		areaManager = CoreSpringFactory.getImpl(BGAreaManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
		initForm(ureq);
		updateBusinessGroup(businessGroup);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("fieldset.legend.areas");
		setFormContextHelp("org.olat.group.ui.edit", "grp-select-area.html", "help.hover.bgArea");
	}
	
	private void updateBusinessGroup(BusinessGroup businessGroup) {
		this.businessGroup = businessGroup;
		for(FormItem item: flc.getFormComponents().values()) {
			flc.remove(item);
		}
		
		List<BGArea> selectedAreas = areaManager.findBGAreasOfBusinessGroup(businessGroup);
		List<RepositoryEntry> entries = businessGroupService.findRepositoryEntries(Collections.singletonList(businessGroup), 0, -1);
		for(RepositoryEntry entry:entries) {
			List<BGArea> areas = areaManager.findBGAreasInContext(entry.getOlatResource());
			if(areas.isEmpty()) continue;
			String[] keys = new String[areas.size()];
			String[] values = new String[areas.size()];
			for(int i=areas.size(); i-->0; ) {
				keys[i] = areas.get(i).getKey().toString();
				values[i] = areas.get(i).getName();
			}
			MultipleSelectionElement el = uifactory.addCheckboxesVertical("repo_" + entry.getKey(), null, flc, keys, values, null, 1);
			el.setLabel(entry.getDisplayname(), null, false);
			el.showLabel(true);
			el.setUserObject(entry.getOlatResource());
			el.addActionListener(this, FormEvent.ONCHANGE);
			
			for(String key:keys) {
				for(BGArea area:selectedAreas) {
					if(key.equals(area.getKey().toString())) {
						el.select(key, true);
						break;
					}
				}
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof MultipleSelectionElement) {
			MultipleSelectionElement mse = (MultipleSelectionElement)source;
			Set<String> selectedAreaKeys = mse.getSelectedKeys();
			Set<String> allAreaKeys = mse.getKeys();
			List<BGArea> currentSelectedAreas = areaManager.findBGAreasOfBusinessGroup(businessGroup);
			Map<String, BGArea> currentSelectedAreaKeys = new HashMap<String, BGArea>();
			for(BGArea area:currentSelectedAreas) {
				currentSelectedAreaKeys.put(area.getKey().toString(), area);
			}

			for(String areaKey:allAreaKeys) {
				boolean selected = selectedAreaKeys.contains(areaKey);
				boolean currentlySelected = currentSelectedAreaKeys.containsKey(areaKey);
				if (selected && !currentlySelected) {
					// add relation:
					BGArea area = areaManager.loadArea(new Long(areaKey));
					areaManager.addBGToBGArea(businessGroup, area);
					
					ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_AREA_UPDATED, getClass(),
							LoggingResourceable.wrap(area));
				} else if (!selected && currentlySelected) {
					// remove relation:
					BGArea area = currentSelectedAreaKeys.get(areaKey);
					areaManager.removeBGFromArea(businessGroup, area);
					
					ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_AREA_UPDATED, getClass(),
							LoggingResourceable.wrap(area));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
}
