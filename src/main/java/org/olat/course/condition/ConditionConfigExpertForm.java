/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.condition;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.condition.interpreter.ConditionErrorMessage;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class ConditionConfigExpertForm extends FormBasicController {

	private TextElement tprecond;
	private UserCourseEnvironment euce;
	private Condition cond;
	private String conditionId;

	/**
	 * Constructor for the condition configuration form in expert mode
	 * 
	 * @param name The form name
	 * @param cond The condition that is used to initialize the form
	 * @param trans
	 */
	public ConditionConfigExpertForm(UserRequest ureq, WindowControl wControl, Condition cond, UserCourseEnvironment euce) {
		super(ureq, wControl);
		this.euce = euce;
		this.cond = cond;
		this.conditionId = cond.getConditionId();
		initForm(ureq);
	}

	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		
		if (tprecond.isEmpty()) {
			// user leaves it blank to cancel
			// the precondition
			return true;
		}
		
		/*
		 *  not empty, nowOLAT - Demo Course_1264602504362 test precondition syntax and for existing soft references
		 */
		CourseEditorEnv cev = euce.getCourseEditorEnv();
		ConditionExpression ce = new ConditionExpression(conditionId,tprecond.getValue());
		ConditionErrorMessage[] cerrmsg = cev.validateConditionExpression(ce);
		/*
		 * display any error detected in the condition expression testing.
		 */
		if (cerrmsg != null && cerrmsg.length >0) {
			//the error messOLAT - Demo Course_1264602504362age
			tprecond.setErrorKey(cerrmsg[0].getErrorKey(), cerrmsg[0].getErrorKeyParams());
			if (cerrmsg[0].getSolutionMsgKey() != null && !"".equals(cerrmsg[0].getSolutionMsgKey())) {
				//and a hint or example to clarify the error message
				tprecond.setExampleKey(cerrmsg[0].getSolutionMsgKey(), cerrmsg[0].getErrorKeyParams());
			}
			return false;
		}
		//reset HINTS
		tprecond.setExampleKey("xx", new String[]{""});
		return true;
	}

	/**
	 * Update a condition using the data in the current form
	 * 
	 * @param cond The condition that should be updated
	 * @return Condition The updated condition or null if condition was empty
	 */
	public Condition updateConditionFromFormData(Condition cond) {
		if (cond == null) cond = new Condition();

		cond.setConditionExpression(tprecond.getValue());
		cond.setExpertMode(true);
		cond.setConditionId(conditionId);
		if (StringHelper.containsNonWhitespace(cond.getConditionExpression())) { return cond; }
		cond.setConditionExpression(null);
		return cond;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tprecond = uifactory.addTextAreaElement("precond","form.expert.condition" , 5000, 6, 45, true, false, (cond == null ? "" : cond.getConditionExpression()), formLayout);
		uifactory.addFormSubmitButton("save", "save", formLayout);
	}
}