/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodeaccess.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Aug 24, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class MigrationSelectionController extends FormBasicController {

	private TextElement displayNameEl;
	private TextElement extRefEl;
	private SingleSelection designEl;
	private final RepositoryEntry repositoryEntry;

	@Autowired
	private CourseModule courseModule;

	public MigrationSelectionController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.repositoryEntry = repositoryEntry;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String displayName = getCopyText(repositoryEntry.getDisplayname(), 100);
		displayNameEl = uifactory.addTextElement("title", "cif.displayname", 100, displayName, formLayout);
		displayNameEl.setDisplaySize(30);
		displayNameEl.setMandatory(true);
		
		String extRef = getCopyText(repositoryEntry.getExternalRef(), 255);
		extRefEl = uifactory.addTextElement("ext.ref", "cif.externalref.long", 255, extRef, formLayout);
		extRefEl.setHelpText(translate("cif.externalref.hover"));
		
		SelectionValues designKV = new SelectionValues();
		designKV.add(new SelectionValues.SelectionValue(CourseModule.COURSE_TYPE_PATH, translate("course.design.path"), translate("course.design.path.desc"), "o_course_design_path_icon", null, true));
		designKV.add(new SelectionValues.SelectionValue(CourseModule.COURSE_TYPE_PROGRESS, translate("course.design.progress"), translate("course.design.progress.desc"), "o_course_design_progress_icon", null, true));
		designEl = uifactory.addCardSingleSelectHorizontal("course.design", "course.design", formLayout, designKV);
		designEl.setElementCssClass("o_course_design");
		String defaultCourseType = courseModule.getCourseTypeDefault();
		if (!StringHelper.containsNonWhitespace(defaultCourseType) || CourseModule.COURSE_TYPE_CLASSIC.equals(defaultCourseType)) {
			defaultCourseType = CourseModule.COURSE_TYPE_PATH;
		}
		designEl.select(defaultCourseType, true);

		// buttons
		FormLayoutContainer buttonLayoutCont = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayoutCont);
		uifactory.addFormSubmitButton("duplicate.and.convert", "duplicate.and.convert", buttonLayoutCont);
		uifactory.addFormCancelButton("cancel", buttonLayoutCont, ureq, getWindowControl());
	}

	private String getCopyText(String text, int maxLength) {
		if (!StringHelper.containsNonWhitespace(text)) {
			return text;
		}
		String copyText = translate("copy.entry", text);
		int overlap = Math.max(copyText.length() - maxLength, 0);
		if (overlap == 0) {
			return copyText;
		}
		String truncatedText = text.substring(0, text.length() - overlap);
		return translate("copy.entry", truncatedText);
	}

	public SingleSelection getDesignEl() {
		return designEl;
	}

	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}
	
	public String getTitle() {
		return displayNameEl.getValue();
	}
	
	public String getExtRef() {
		return extRefEl.getValue();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		displayNameEl.clearError();
		if (displayNameEl.isEmpty()) {
			displayNameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
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
