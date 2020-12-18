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
package org.olat.course.wizard.ui;

import java.util.function.Supplier;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.editor.EditorMainController;
import org.olat.course.editor.NodeConfigFormController;
import org.olat.course.wizard.CourseNodeTitleContext;
import org.olat.course.wizard.CourseWizardService;
import org.olat.repository.wizard.ui.ReferencableEntriesStepController;
import org.olat.repository.wizard.ui.RepositoryEntryOverviewController.MoreFigures;

/**
 * 
 * Initial date: 17 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TitleAndEntryController extends StepFormBasicController {
	
	private FormLayoutContainer nodeCont;
	private TextElement shortTitleEl;

	private final ReferencableEntriesStepController referencableCtrl;
	
	private final String i18nFormTitle;
	private final CourseNodeTitleContext titleContext;

	public TitleAndEntryController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			String runContextKey, Supplier<Object> contextCreator, String i18nFormTitle, String limitTypes,
			MoreFigures moreFigures) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(CourseWizardService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(EditorMainController.class, getLocale(), getTranslator()));
		this.i18nFormTitle = i18nFormTitle;
		titleContext = (CourseNodeTitleContext)getOrCreateFromRunContext(runContextKey, contextCreator);
		
		referencableCtrl = new ReferencableEntriesStepController(ureq, wControl, rootForm, runContext, runContextKey,
				contextCreator, limitTypes, moreFigures);
		listenTo(referencableCtrl);

		initForm(ureq);
		updateUI(true);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle(i18nFormTitle);
		
		nodeCont = FormLayoutContainer.createDefaultFormLayout("node", getTranslator());
		nodeCont.setRootForm(mainForm);
		formLayout.add(nodeCont);
		
		shortTitleEl = uifactory.addTextElement("nodeConfigForm.menutitle", "nodeConfigForm.menutitle",
				NodeConfigFormController.SHORT_TITLE_MAX_LENGTH, titleContext.getShortTitle(), nodeCont);
		shortTitleEl.setMandatory(true);
		shortTitleEl.setCheckVisibleLength(true);
		
		formLayout.add(referencableCtrl.getInitialFormItem());
	}
	
	private void updateUI(boolean updateTitle) {
		nodeCont.setVisible(!referencableCtrl.isSearch());
		if (updateTitle && !StringHelper.containsNonWhitespace(shortTitleEl.getValue()) && referencableCtrl.getEntry() != null) {
			shortTitleEl.setValue(referencableCtrl.getEntry().getDisplayname());
		}
		flc.setDirty(true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == referencableCtrl) {
			if (event == ReferencableEntriesStepController.SERACH_STARTED_EVENT) {
				updateUI(false);
			} else if (event == ReferencableEntriesStepController.SERACH_CANCELLED_EVENT) {
				updateUI(true);
			} else if (event == ReferencableEntriesStepController.SERACH_DONE_EVENT) {
				updateUI(true);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		shortTitleEl.clearError();
		if (!StringHelper.containsNonWhitespace(shortTitleEl.getValue())) {
			shortTitleEl.setErrorKey("nodeConfigForm.menumust", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String shortTitle = shortTitleEl.getValue();
		titleContext.setShortTitle(shortTitle);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
