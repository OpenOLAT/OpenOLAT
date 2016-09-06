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
package org.olat.ims.qti21.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.references.Reference;
import org.olat.resource.references.ReferenceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ConfirmDeleteDataController extends FormBasicController {
	
	private final RepositoryEntry testEntry;
	
	@Autowired
	private ReferenceManager referenceManager;
	
	public QTI21ConfirmDeleteDataController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry) {
		super(ureq, wControl, "confirm_delete");
		this.testEntry = testEntry;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
	
		StringBuilder sb = new StringBuilder();
		List<Reference> referencees = referenceManager.getReferencesTo(testEntry.getOlatResource());
		for(Reference ref:referencees) {
			if ("CourseModule".equals(ref.getSource().getResourceableTypeName())) {
				if(sb.length() > 0) sb.append(", ");
				try {
					ICourse course = CourseFactory.loadCourse(ref.getSource().getResourceableId());
					sb.append(StringHelper.escapeHtml(course.getCourseTitle()));
				} catch(CorruptedCourseException e) {
					logError("", e);
				}
			}
		}

		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("referencees", sb.toString());
		}

		final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		formLayout.add(buttonGroupLayout);
		
		if(referencees.size() == 0) {
			uifactory.addFormSubmitButton("delete", buttonGroupLayout);
		}
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
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
