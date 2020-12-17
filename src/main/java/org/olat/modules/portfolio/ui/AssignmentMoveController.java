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
package org.olat.modules.portfolio.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.model.SectionKeyRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentMoveController extends FormBasicController {

	private SingleSelection sectionsEl;
	
	private Binder binder;
	private Section section;
	private Assignment assignment;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public AssignmentMoveController(UserRequest ureq, WindowControl wControl, Assignment assignment, Section currentSection) {
		super(ureq, wControl);
		this.assignment = assignment;
		section = portfolioService.getSection(currentSection);
		binder = section.getBinder();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_edit_assignment_form");

		List<Section> sections = portfolioService.getSections(binder);
		
		String selectedKey = null;
		int numOfSections = sections.size();
		String[] theKeys = new String[numOfSections];
		String[] theValues = new String[numOfSections];
		for (int i = 0; i < numOfSections; i++) {
			Long sectionKey = sections.get(i).getKey();
			theKeys[i] = sectionKey.toString();
			theValues[i] = (i + 1) + ". " + sections.get(i).getTitle();
			if (section != null && section.getKey().equals(sectionKey)) {
				selectedKey = theKeys[i];
			}
		}
		
		sectionsEl = uifactory.addDropdownSingleselect("sections", "page.sections", formLayout, theKeys, theValues, null);
		if (selectedKey != null) {
			sectionsEl.select(selectedKey, true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);

		uifactory.addFormSubmitButton("move", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private Long getSelectedSectionKey() {
		Long selectSection = null;
		if (sectionsEl != null && sectionsEl.isOneSelected() && sectionsEl.isEnabled() && sectionsEl.isVisible()) {
			String selectedKey = sectionsEl.getSelectedKey();
			selectSection = Long.valueOf(selectedKey);
		}
		return selectSection;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		Long newParentSectionKey = getSelectedSectionKey();
		if(newParentSectionKey != null) {
			if(newParentSectionKey.equals(section.getKey())) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} else {
				portfolioService.moveAssignment(section, assignment, new SectionKeyRef(newParentSectionKey));
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
