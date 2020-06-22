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
package org.olat.course.nodes.appointments.ui;

import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.Group;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.course.nodes.appointments.AppointmentsService;
import org.olat.course.nodes.appointments.Topic;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 Jun 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GroupsEditController extends FormBasicController {
	
	private static final String[] ON_KEYS = new String[] { "on" };

	private MultipleSelectionElement courseEl;
	private MultipleSelectionElement businessGroupEl;
	private MultipleSelectionElement curriculumEl;

	private final Topic topic;
	private final RepositoryEntry entry;
	private Map<String, Group> keyToBussinesGroups;
	private Map<String, Group> keyToCurriculumElementGroup;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CurriculumService curriculumService;

	public GroupsEditController(UserRequest ureq, WindowControl wControl, Topic topic) {
		super(ureq, wControl);
		this.topic = topic;
		entry = topic.getEntry();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<Group> groups = appointmentsService.getGroupRestrictions(topic);
		
		courseEl = uifactory.addCheckboxesVertical("groups.course", formLayout, ON_KEYS,
				TranslatorHelper.translateAll(getTranslator(), ON_KEYS), 1);
		Group entryBaseGroup = repositoryService.getDefaultGroup(entry);
		if (groups.contains(entryBaseGroup)) {
			courseEl.select(courseEl.getKey(0), true);
		}
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> businessGroups = businessGroupService.findBusinessGroups(params, entry, 0, -1, BusinessGroupOrder.nameAsc);
		if (!businessGroups.isEmpty()) {
			KeyValues businessGroupKV = new KeyValues();
			keyToBussinesGroups = new HashMap<>();
			businessGroups.forEach(bg -> {
				String key = bg.getBaseGroup().getKey().toString();
				businessGroupKV.add(entry(key, bg.getName()));
				keyToBussinesGroups.put(key, bg.getBaseGroup());
			});
			businessGroupEl = uifactory.addCheckboxesVertical("groups.business.groups", formLayout,
					businessGroupKV.keys(), businessGroupKV.values(), 2);
			Set<String> keys = businessGroupEl.getKeys();
			for (Group group : groups) {
				String key = group.getKey().toString();
				if (keys.contains(key)) {
					businessGroupEl.select(key, true);
				}
			}
		}
		
		List<CurriculumElement> elements = curriculumService.getCurriculumElements(entry);
		if (!elements.isEmpty()) {
			KeyValues curriculumKV = new KeyValues();
			keyToCurriculumElementGroup = new HashMap<>();
			elements.forEach(curEle -> {
				String key = curEle.getGroup().getKey().toString();
				curriculumKV.add(entry(key, curEle.getDisplayName()));
				keyToCurriculumElementGroup.put(key, curEle.getGroup());
			});
			curriculumEl = uifactory.addCheckboxesVertical("groups.curriculum", formLayout, curriculumKV.keys(),
					curriculumKV.values(), 2);
			Set<String> keys = curriculumEl.getKeys();
			for (Group group : groups) {
				String key = group.getKey().toString();
				if (keys.contains(key)) {
					curriculumEl.select(key, true);
				}
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<Group> groups = new ArrayList<>();
		if (courseEl.isAtLeastSelected(1)) {
			Group entryBaseGroup = repositoryService.getDefaultGroup(entry);
			groups.add(entryBaseGroup);
		}
		if (keyToBussinesGroups != null) {
			for (String key : businessGroupEl.getSelectedKeys()) {
				Group group = keyToBussinesGroups.get(key);
				groups.add(group);
			}
		}
		if (keyToCurriculumElementGroup != null) {
			for (String key : curriculumEl.getSelectedKeys()) {
				Group group = keyToCurriculumElementGroup.get(key);
				groups.add(group);
			}
		}
		appointmentsService.restrictTopic(topic, groups);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
