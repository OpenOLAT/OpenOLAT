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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmExtraTimeController  extends FormBasicController {
	
	private TextElement extraTimeInMinEl;
	private List<AssessmentMode> assessmentModes;
	private List<AssessmentTestSession> testSessions;
	
	private final RepositoryEntry entry;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;
	
	public ConfirmExtraTimeController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, List<AssessmentTestSession> testSessions) {
		super(ureq, wControl, "confirm_extra_time");
		this.entry = entry;
		this.testSessions = testSessions;
		assessmentModes = assessmentModeManager.getCurrentAssessmentMode(entry, new Date());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("fullnames", sessionToFullnames());
			if(!assessmentModes.isEmpty()) {
				currentAssessmentModeMessage(layoutCont);
			}
		}
		
		int extraTime = 0;
		for(AssessmentTestSession testSession:testSessions) {
			if(testSession.getExtraTime() != null && extraTime < testSession.getExtraTime().intValue()) {
				extraTime = testSession.getExtraTime().intValue();
			}
		}
		
		String maxExtraTime = extraTime == 0 ? "" : Integer.toString(extraTime / 60);
		extraTimeInMinEl = uifactory.addTextElement("extra.time.minutes", null, 5, maxExtraTime, formLayout);
		extraTimeInMinEl.setDisplaySize(5);
		extraTimeInMinEl.setDomReplacementWrapperRequired(false);
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("extra.time", formLayout);
	}
	
	private void currentAssessmentModeMessage(FormLayoutContainer layoutCont) {
		Formatter formatter = Formatter.getInstance(getLocale());
		List<String> modes = new ArrayList<>();
		for(AssessmentMode assessmentMode:assessmentModes) {
			String title = assessmentMode.getName();
			String begin = formatter.formatDateAndTime(assessmentMode.getBegin());
			String end = formatter.formatDateAndTime(assessmentMode.getEnd());
			modes.add(translate("warning.assessment.mode.date", title, begin, end));
		}
		layoutCont.contextPut("assessmemtModeMessages", modes);
	}
	
	private String sessionToFullnames() {
		StringBuilder sb = new StringBuilder();
		
		for(AssessmentTestSession testSession:testSessions) {
			Identity identity = testSession.getIdentity();
			if(identity != null) {
				String fullname = userManager.getUserDisplayName(identity);
				if(sb.length() > 0) sb.append(", ");
				sb.append(fullname);
			}
		}

		return sb.toString();
	}
	
	/**
	 * 
	 * @return The extra time in seconds.
	 */
	public int getExtraTime() {
		String val = extraTimeInMinEl.getValue();
		int inMinute = Integer.parseInt(val);
		return inMinute * 60;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		extraTimeInMinEl.clearError();
		if(StringHelper.containsNonWhitespace(extraTimeInMinEl.getValue())) {
			try {
				int time = Integer.parseInt(extraTimeInMinEl.getValue());
				if(time <= 0) {
					allOk &= false;
					extraTimeInMinEl.setErrorKey("form.error.nointeger");
				}
			} catch(Exception e) {
				extraTimeInMinEl.setErrorKey("form.error.nointeger");
				allOk &= false;
			}
		} else {
			extraTimeInMinEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		int extraTime = getExtraTime();
		for (AssessmentTestSession testSession:testSessions) {
			qtiService.extraTimeAssessmentTestSession(testSession, extraTime, getIdentity());
		}
		dbInstance.commit();
		assessmentModeCoordinationService.sendEvent(entry);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
