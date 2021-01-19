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
package org.olat.modules.quality.ui.wizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.ui.CurriculumTreeModel;
import org.olat.modules.quality.ui.ParticipationListController;
import org.olat.modules.quality.ui.QualityUIFactory;
import org.olat.modules.quality.ui.QualityUIFactory.KeysValues;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AddCurriculumElementUserSelectionController extends StepFormBasicController {
	
	private static final String[] EMPTY_ARRAY = new String[] {};
	
	private SingleSelection curriculumEl;
	private SingleSelection curriculumElementEl;
	
	private CurriculumElementContext context;
	private final List<? extends OrganisationRef> organisationRefs;

	@Autowired
	private CurriculumService curriculumService;

	public AddCurriculumElementUserSelectionController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, List<? extends OrganisationRef> organisationRefs) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		this.organisationRefs = organisationRefs;
		setTranslator(Util.createPackageTranslator(ParticipationListController.class, getLocale(), getTranslator()));
		context = (CurriculumElementContext) getFromRunContext("context");
		initForm(ureq);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		String selectedKey = curriculumElementEl.getSelectedKey();
		CurriculumElementRef elementRef = QualityUIFactory.getCurriculumElementRef(selectedKey);
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(elementRef);
		context.setCurriculumElement(curriculumElement);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		CurriculumSearchParameters params = new CurriculumSearchParameters();
		params.setOrganisations(organisationRefs);
		List<Curriculum> curriculums = curriculumService.getCurriculums(params);
		KeysValues curriculumKeysValues = QualityUIFactory.getCurriculumKeysValues(curriculums, null);
		curriculumEl = uifactory.addDropdownSingleselect("participation.user.curele.add.choose.curriculum", formLayout,
				curriculumKeysValues.getKeys(), curriculumKeysValues.getValues());
		curriculumEl.addActionListener(FormEvent.ONCHANGE);
		
		curriculumElementEl = uifactory.addDropdownSingleselect(
				"participation.user.curele.add.choose.curriculum.element", formLayout, EMPTY_ARRAY, EMPTY_ARRAY);
		updateUI();
	}

	private void updateUI() {
		if (curriculumEl.isOneSelected()) {
			String curriculumKey = curriculumEl.getSelectedKey();
			CurriculumRef curriculumRef = QualityUIFactory.getCurriculumRef(curriculumKey);
			Curriculum curriculum = curriculumService.getCurriculum(curriculumRef);
			if (curriculum != null) {
				List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElements(curriculum, CurriculumElementStatus.notDeleted());
				CurriculumTreeModel curriculumTreeModel = new CurriculumTreeModel(curriculum);
				curriculumTreeModel.loadTreeModel(curriculumElements);
				KeysValues curriculumElementKeysValues = QualityUIFactory.getCurriculumElementKeysValues(curriculumTreeModel, null);
				curriculumElementEl.setKeysAndValues(curriculumElementKeysValues.getKeys(), curriculumElementKeysValues.getValues(), null);
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
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		curriculumElementEl.clearError();
		if (!curriculumElementEl.isOneSelected()) {
			curriculumElementEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
