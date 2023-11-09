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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseModule;
import org.olat.repository.ui.author.AuthoringEntryRow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Aug 24, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class MigrationSelectionController extends FormBasicController {

	private SingleSelection designEl;
	private final AuthoringEntryRow row;

	@Autowired
	private CourseModule courseModule;

	public MigrationSelectionController(UserRequest ureq, WindowControl wControl, AuthoringEntryRow row) {
		super(ureq, wControl);
		this.row = row;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
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
		uifactory.addFormSubmitButton("apply", buttonLayoutCont);
		uifactory.addFormCancelButton("cancel", buttonLayoutCont, ureq, getWindowControl());
	}

	public SingleSelection getDesignEl() {
		return designEl;
	}

	/**
	 * only necessary for authorListCtrl
	 * @return row object if authorListCtrl otherwise null
	 */
	public AuthoringEntryRow getRow() {
		return row;
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
