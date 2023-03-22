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
package org.olat.course.assessment.ui.reset;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetCourse;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetParticipants;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmResetDataController extends FormBasicController {
	
	private MultipleSelectionElement confirmationEl;
	
	private final boolean withButtons;
	private final ResetDataContext dataContext;
	private final AssessmentToolSecurityCallback secCallback;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	/**
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param dataContext The context with eventually selected participants...
	 * @param secCallback Only required if all participants is selected
	 */
	public ConfirmResetDataController(UserRequest ureq, WindowControl wControl, ResetDataContext dataContext,
			AssessmentToolSecurityCallback secCallback) {
		super(ureq, wControl, "confirmation_reset_data");
		withButtons = true;
		this.dataContext = dataContext;
		this.secCallback = secCallback;
		
		initForm(ureq);
	}
	
	/**
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param rootForm The form of the wizard
	 * @param dataContext The context with eventually selected participants...
	 * @param secCallback Only required if all participants is selected
	 */
	public ConfirmResetDataController(UserRequest ureq, WindowControl wControl, Form rootForm, ResetDataContext dataContext,
			AssessmentToolSecurityCallback secCallback) {
		super(ureq, wControl, LAYOUT_CUSTOM, "confirmation_reset_data", rootForm);
		withButtons = false;
		this.dataContext = dataContext;
		this.secCallback = secCallback;
		
		initForm(ureq);
	}
	
	public ResetDataContext getDataContext() {
		return dataContext;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			long numOfParticipants;
			if(dataContext.getResetParticipants() == ResetParticipants.all) {
				if(secCallback != null) {
					RepositoryEntry courseEntry = dataContext.getRepositoryEntry();
					SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, null, null, secCallback);
					numOfParticipants = assessmentToolManager.countAssessedIdentities(ureq.getIdentity(), params);
				} else {
					numOfParticipants = -1;
				}
			} else {
				numOfParticipants = dataContext.getSelectedParticipants().size();
			}

			String message = "ERROR";
			if(dataContext.getResetCourse() == ResetCourse.all) {
				if(numOfParticipants > 1) {
					message = translate("confirmation.message.course.participants.plural", Long.toString(numOfParticipants));
				} else if(numOfParticipants == 1) {
					String fullName = userManager.getUserDisplayName(dataContext.getSelectedParticipants().get(0));
					message = translate("confirmation.message.course.participant.singular", fullName);
				}
			} else {
				int numOfCourseElements = dataContext.getCourseNodes().size();
				if(numOfCourseElements > 1) {
					if(numOfParticipants > 1) {
						message = translate("confirmation.message.elements.participants.plural",
								Long.toString(numOfParticipants), Integer.toString(numOfCourseElements));
					} else if(numOfParticipants == 1) {
						String fullName = userManager.getUserDisplayName(dataContext.getSelectedParticipants().get(0));
						message = translate("confirmation.message.elements.participant.singular",
								fullName, Integer.toString(numOfCourseElements));
					}
				} else if(numOfCourseElements == 1) {
					String courseNode = dataContext.getCourseNodes().get(0).getShortTitle();
					if(numOfParticipants > 1) {
						message = translate("confirmation.message.element.participants.plural",
								Long.toString(numOfParticipants), courseNode);
					} else if(numOfParticipants == 1) {
						String fullName = userManager.getUserDisplayName(dataContext.getSelectedParticipants().get(0));
						message = translate("confirmation.message.element.participant.singular", fullName, courseNode);
					}
				}
			}
			
			layoutCont.contextPut("msgCourseElements", message);
		}
		
		SelectionValues confirmationKV = new SelectionValues();
		confirmationKV.add(SelectionValues.entry("ok", translate("confirmation.check.value")));
		confirmationEl = uifactory.addCheckboxesHorizontal("confirmation.check", "confirmation.check.label", formLayout,
				confirmationKV.keys(), confirmationKV.values());
		
		if(withButtons) {
			FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
			uifactory.addFormSubmitButton("reset.data", buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		confirmationEl.clearError();
		if(!confirmationEl.isAtLeastSelected(1)) {
			confirmationEl.setErrorKey("form.legende.mandatory");
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
