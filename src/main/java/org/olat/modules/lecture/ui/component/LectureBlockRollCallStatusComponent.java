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
package org.olat.modules.lecture.ui.component;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.ui.TeacherRollCallRow;

/**
 * 
 * Initial date: 20 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockRollCallStatusComponent extends FormBaseComponentImpl {
	
	private static final ComponentRenderer RENDERER = new LectureBlockRollCallStatusComponentRenderer();
	private final LectureBlockRollCallStatusItem element;
	
	private TeacherRollCallRow rollCall;
	private boolean authorizedAbsenceEnabled;
	private boolean absenceDefaultAuthorized;
	
	public LectureBlockRollCallStatusComponent(String id, LectureBlockRollCallStatusItem element, Translator translator) {
		super(id, element.getName());
		this.element = element;
		setTranslator(translator);
	}
	
	public TeacherRollCallRow getRollCall() {
		return rollCall;
	}

	public void setRollCall(TeacherRollCallRow rollCall) {
		this.rollCall = rollCall;
	}

	public int getPlannedLecturesNumber() {
		return rollCall.getChecks().length;
	}

	public boolean isAuthorizedAbsenceEnabled() {
		return authorizedAbsenceEnabled;
	}

	public void setAuthorizedAbsenceEnabled(boolean authorizedAbsenceEnabled) {
		this.authorizedAbsenceEnabled = authorizedAbsenceEnabled;
	}

	public boolean isAbsenceDefaultAuthorized() {
		return absenceDefaultAuthorized;
	}

	public void setAbsenceDefaultAuthorized(boolean absenceDefaultAuthorized) {
		this.absenceDefaultAuthorized = absenceDefaultAuthorized;
	}
	
	public boolean isLecturesAuthorizedAbsent() {
		if(rollCall.getAuthorizedAbsence() != null) {
			rollCall.getAuthorizedAbsence().isAtLeastSelected(1);
		}
		LectureBlockRollCall call = rollCall.getRollCall();
		return call == null || call.getAbsenceAuthorized() == null
				? false : call.getAbsenceAuthorized().booleanValue(); 
	}
	
	public int getLecturesAttendedNumber() {
		int numOfChecks = rollCall.getChecks().length;
		int absence = 0;
		for(int j=0; j<numOfChecks; j++) {
			if(rollCall.getCheck(j).isAtLeastSelected(1)) {
				absence++;
			}
		}
		return numOfChecks - absence;
	}

	public LectureBlockRollCallStatusItem getLectureBlockRollCallStatusItem() {
		return element;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
