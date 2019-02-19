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
package org.olat.modules.curriculum.ui.copy;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumCopySettings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopySettingsController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private MultipleSelectionElement datesEl;
	private MultipleSelectionElement taxonomyEl;
	private MultipleSelectionElement linkToResourcesEl;
	
	private final CurriculumElement element;
	
	@Autowired
	private CurriculumService currciulumService;
	
	public CopySettingsController(UserRequest ureq, WindowControl wControl, CurriculumElement element) {
		super(ureq, wControl);
		this.element = element;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		datesEl = uifactory.addCheckboxesHorizontal("copy.dates", formLayout, onKeys, onKeys);
		
		linkToResourcesEl = uifactory.addCheckboxesHorizontal("copy.link.resources", formLayout, onKeys, onKeys);
		linkToResourcesEl.select(onKeys[0], true);
		
		taxonomyEl = uifactory.addCheckboxesHorizontal("copy.taxonomy.levels", formLayout, onKeys, onKeys);
		taxonomyEl.select(onKeys[0], true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("copy", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		CurriculumElement elementToClone = currciulumService.getCurriculumElement(element);
		Curriculum curriculum = elementToClone.getCurriculum();
		CurriculumElement parentElement = elementToClone.getParent();
		CurriculumCopySettings settings = new CurriculumCopySettings();
		settings.setCopyDates(datesEl.isAtLeastSelected(1));
		settings.setCopyLinkToResources(linkToResourcesEl.isAtLeastSelected(1));
		settings.setCopyTaxonomy(taxonomyEl.isAtLeastSelected(1));
		currciulumService.cloneCurriculumElement(curriculum, parentElement, elementToClone, settings);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
