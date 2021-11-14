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
package org.olat.modules.quality.generator.ui;

import static org.olat.modules.quality.ui.QualityUIFactory.emptyArray;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.ui.CurriculumTreeModel;
import org.olat.modules.quality.ui.QualityUIFactory;
import org.olat.modules.quality.ui.QualityUIFactory.KeysValues;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class CurriculumElementSelectionController extends FormBasicController {
	
	private SingleSelection curriculumEl;
	private SingleSelection curriculumElementEl;

	private List<? extends OrganisationRef> organisationRefs;
	
	@Autowired
	private CurriculumService curriculumService;

	CurriculumElementSelectionController(UserRequest ureq, WindowControl windowControl,
			List<? extends OrganisationRef> organisationRefs) {
		super(ureq, windowControl);
		this.organisationRefs = organisationRefs;
		initForm(ureq);
	}
	
	String getCurriculumElementKey() {
		if (curriculumElementEl.isOneSelected()) {
			return curriculumElementEl.getSelectedKey();
		}
		return null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		CurriculumSearchParameters params = new CurriculumSearchParameters();
		params.setOrganisations(organisationRefs);
		List<Curriculum> curriculums = curriculumService.getCurriculums(params);
		KeysValues curriculumKeysValues = QualityUIFactory.getCurriculumKeysValues(curriculums, null);
		String[] curriculumKeys = curriculumKeysValues.getKeys();
		curriculumEl = uifactory.addDropdownSingleselect("curriculum.element.select.curriculum", formLayout,
				curriculumKeys, curriculumKeysValues.getValues());
		if (curriculumKeys.length > 0) {
			curriculumEl.select(curriculumKeys[0], true);
		}
		curriculumEl.addActionListener(FormEvent.ONCHANGE);
		
		curriculumElementEl = uifactory.addDropdownSingleselect(
				"curriculum.element.select.curriculum.element", formLayout, emptyArray(), emptyArray());
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("curriculum.element.select.button", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		updateUI();
	}

	private void updateUI() {
		if (curriculumEl.isOneSelected()) {
			String curriculumKey = curriculumEl.getSelectedKey();
			CurriculumRef curriculumRef = QualityUIFactory.getCurriculumRef(curriculumKey);
			Curriculum curriculum = curriculumService.getCurriculum(curriculumRef);
			if (curriculum != null) {
				List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElements(curriculum,
						CurriculumElementStatus.notDeleted());
				CurriculumTreeModel curriculumTreeModel = new CurriculumTreeModel(curriculum);
				curriculumTreeModel.loadTreeModel(curriculumElements);
				KeysValues curriculumElementKeysValues = QualityUIFactory
						.getCurriculumElementKeysValues(curriculumTreeModel, null);
				curriculumElementEl.setKeysAndValues(curriculumElementKeysValues.getKeys(),
						curriculumElementKeysValues.getValues(), null);
			}
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == curriculumEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

}
