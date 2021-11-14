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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumCopySettings;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopySettingsController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] resourceKeys = new String[] {
			CopyResources.dont.name(), CopyResources.relation.name(), CopyResources.resource.name()
		};
	
	private SingleSelection resourcesEl;
	private MultipleSelectionElement datesEl;
	private MultipleSelectionElement taxonomyEl;

	private DialogBoxController confirmCopyCtrl;
	
	private final CurriculumElement element;
	
	@Autowired
	private RepositoryService repositoryService;
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
		
		String[] resourceValues = new String[] {
			translate("copy.resources.dont"), translate("copy.resources.relation"), translate("copy.resources.resource")
		};
		resourcesEl = uifactory.addRadiosHorizontal("copy.resources", "copy.resources", formLayout, resourceKeys, resourceValues);
		resourcesEl.select(CopyResources.relation.name(), true);
		
		taxonomyEl = uifactory.addCheckboxesHorizontal("copy.taxonomy.levels", formLayout, onKeys, onKeys);
		taxonomyEl.select(onKeys[0], true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("copy", buttonsCont);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmCopyCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				doCopy(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(!resourcesEl.isOneSelected()) {
			resourcesEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		CopyResources copyResources = getCopyResources();
		if(copyResources == CopyResources.resource) {
			copyOrConfirmPartialCopy(ureq);
		} else {
			doCopy(ureq);
		}	
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private CopyResources getCopyResources() {
		return CopyResources.valueOf(resourcesEl.getSelectedKey(), CopyResources.relation);
	}
	
	private void copyOrConfirmPartialCopy(UserRequest ureq) {
		List<RepositoryEntry> entriesToCopy = currciulumService.getRepositoryEntriesWithDescendants(element);
		List<RepositoryEntry> forbiddenEntries = new ArrayList<>();
		for(RepositoryEntry entryToCopy:entriesToCopy) {
			if(!repositoryService.canCopy(entryToCopy, getIdentity())) {
				forbiddenEntries.add(entryToCopy);
			}
		}
		
		if(forbiddenEntries.isEmpty()) {
			doCopy(ureq);
		} else {
			String title = translate("confirm.partial.copy.title");
			String text = translate("confirm.partial.copy.text");
			confirmCopyCtrl = activateOkCancelDialog(ureq, title, text, confirmCopyCtrl);
		}
	}
	
	private void doCopy(UserRequest ureq) {
		CopyResources copyResources = getCopyResources();
		CurriculumElement elementToClone = currciulumService.getCurriculumElement(element);
		Curriculum curriculum = elementToClone.getCurriculum();
		CurriculumElement parentElement = elementToClone.getParent();
		CurriculumCopySettings settings = new CurriculumCopySettings();
		settings.setCopyDates(datesEl.isAtLeastSelected(1));
		settings.setCopyResources(copyResources);
		settings.setCopyTaxonomy(taxonomyEl.isAtLeastSelected(1));
		currciulumService.cloneCurriculumElement(curriculum, parentElement, elementToClone, settings, getIdentity());
		fireEvent(ureq, Event.DONE_EVENT);
		showInfo("info.curriculum.element.copy");
	}
}
