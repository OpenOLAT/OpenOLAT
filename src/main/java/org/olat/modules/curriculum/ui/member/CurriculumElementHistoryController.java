/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;

/**
 * 
 * Initial date: 4 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementHistoryController extends AbstractHistoryController implements Activateable2 {
	
	private FormLink allLevelsButton;
	private FormLink thisLevelButton;
	
	private List<CurriculumElement> descendants;
	
	public CurriculumElementHistoryController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement) {
		super(ureq, wControl, "curriculum_element_history", curriculumElement);

		descendants = curriculumService.getCurriculumElementsDescendants(curriculumElement);
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initButtonsForm(formLayout);
		initTableForm(formLayout, ureq);
	}
	
	private void initButtonsForm(FormItemContainer formLayout) {
		allLevelsButton = uifactory.addFormLink("search.all.levels", formLayout, Link.BUTTON);
		allLevelsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_structure");
		allLevelsButton.setPrimary(true);
		thisLevelButton = uifactory.addFormLink("search.this.level", formLayout, Link.BUTTON);
		thisLevelButton.setIconLeftCSS("o_icon o_icon-fw o_icon_exact_location");
	}
	
	private void initTableForm(FormItemContainer formLayout, UserRequest ureq) {
		initTable(formLayout, true, true);
		initFilters();
		initFiltersPresets();
		
		tableEl.setAndLoadPersistedPreferences(ureq, "cpl-element-memberships-history-v1");
	}
	
	@Override
	protected CurriculumElementMembershipHistorySearchParameters getSearchParameters() {
		List<CurriculumElement> elements = getSearchCurriculumElements();
		CurriculumElementMembershipHistorySearchParameters searchParams = new CurriculumElementMembershipHistorySearchParameters();
		searchParams.setElements(elements);
		return searchParams;
	}
	
	private List<CurriculumElement> getSearchCurriculumElements() {
		List<CurriculumElement> elements;
		if(thisLevelButton.getComponent().isPrimary()) {
			elements = List.of(curriculumElement);
		} else {
			elements = new ArrayList<>(descendants);
			elements.add(curriculumElement);
		}
		return elements;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		if(tableEl.getSelectedFilterTab() == null) {
			tableEl.setSelectedFilterTab(ureq, allTab);
			loadModel(true);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(allLevelsButton == source) {
			doToggleLevels(false);
		} else if(thisLevelButton == source) {
			doToggleLevels(true);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doToggleLevels(boolean thisLevel) {
		allLevelsButton.setPrimary(!thisLevel);
		thisLevelButton.setPrimary(thisLevel);
		tableEl.setColumnModelVisible(curriculumElementCol, !thisLevel);
		loadModel(true);
	}
}
