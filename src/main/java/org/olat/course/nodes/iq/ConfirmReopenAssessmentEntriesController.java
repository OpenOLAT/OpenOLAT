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
package org.olat.course.nodes.iq;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.ims.qti21.ui.ConfirmReopenAssessmentEntryController;

/**
 * 
 * Initial date: 28 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmReopenAssessmentEntriesController extends FormBasicController {
	
	private FormLink readOnlyButton;
	
	private Object userObject;
	private final long numOfAssessmentEntriesDone;
	
	/**
	 * Confirm reopen of the assessment entries .
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 */
	public ConfirmReopenAssessmentEntriesController(UserRequest ureq, WindowControl wControl, long numOfAssessmentEntriesDone) {
		super(ureq, wControl, "confirm_reopen_assessments", Util
				.createPackageTranslator(ConfirmReopenAssessmentEntryController.class, ureq.getLocale()));
		this.numOfAssessmentEntriesDone = numOfAssessmentEntriesDone;
		initForm(ureq);
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String msg;
			if(numOfAssessmentEntriesDone == 1) {
				msg = translate("reopen.assessment.text");
			} else {
				msg = translate("reopen.assessments.text", new String[] { Long.toString(numOfAssessmentEntriesDone) });
			}
			layoutCont.contextPut("msg", msg);
		}
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("reopen.assessment", formLayout);
		readOnlyButton = uifactory.addFormLink("correction.readonly", formLayout, Link.BUTTON);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(readOnlyButton == source) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
