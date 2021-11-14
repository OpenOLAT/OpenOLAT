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
package org.olat.course.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.modules.curriculum.CurriculumElement;

/**
 * 
 * Initial date: 17 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementSelectionController extends FormBasicController {

	private MultipleSelectionElement entrySelector;
	private final CourseGroupManager courseGrpMngr;

	private String[] curriculumElementNames;
	private String[] curriculumElementKeys;

	public CurriculumElementSelectionController(UserRequest ureq, WindowControl wControl,
			CourseGroupManager courseGrpMngr, List<Long> selectionKeys) {
		super(ureq, wControl, "group_or_area_selection");
		this.courseGrpMngr = courseGrpMngr;

		loadNamesAndKeys();
		initForm(ureq);
		
		for (Long selectionKey: selectionKeys) {
			entrySelector.select(selectionKey.toString(), true);
		}
	}
	
	private void loadNamesAndKeys() {
		List<CurriculumElement> curriculumElements = courseGrpMngr.getAllCurriculumElements();
		curriculumElementNames = new String[curriculumElements.size()];
		curriculumElementKeys = new String[curriculumElements.size()];
		for(int i=curriculumElements.size(); i-->0; ) {
			curriculumElementNames[i] = curriculumElements.get(i).getDisplayName();
			curriculumElementKeys[i] = curriculumElements.get(i).getKey().toString();
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		entrySelector = uifactory.addCheckboxesVertical("entries",  null, formLayout, curriculumElementKeys, curriculumElementNames, 1);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("subm", "apply", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	public List<String> getSelectedNames() {
		List<String> selectedNames = new ArrayList<>();
		for(int i=0; i<curriculumElementKeys.length; i++) {
			if(entrySelector.isSelected(i)) {
				selectedNames.add(curriculumElementNames[i]);
			}
		}
		return selectedNames;
	}
	
	public List<Long> getSelectedKeys() {
		Collection<String> selectedKeys = entrySelector.getSelectedKeys();
		List<Long> groupKeys = new ArrayList<>();
		for(String selectedKey:selectedKeys) {
			groupKeys.add(Long.valueOf(selectedKey));
		}
		return groupKeys;
	}
}