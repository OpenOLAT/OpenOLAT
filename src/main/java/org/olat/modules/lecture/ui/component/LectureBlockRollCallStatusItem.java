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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 20 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockRollCallStatusItem extends FormItemImpl {
	
	private final LectureBlockRollCallStatusComponent component;
	
	public LectureBlockRollCallStatusItem(String name, RollCallItem rollCall,
			boolean authorizedAbsenceEnabled, boolean absenceDefaultAuthorized, Translator translator) {
		super(name);
		component = new LectureBlockRollCallStatusComponent(null, this, translator);
		component.setRollCall(rollCall);
		component.setAuthorizedAbsenceEnabled(authorizedAbsenceEnabled);
		component.setAbsenceDefaultAuthorized(absenceDefaultAuthorized);
		component.setDomReplacementWrapperRequired(false);
		setTranslator(translator);
	}

	public RollCallItem getRollCall() {
		return component.getRollCall();
	}

	public int getPlannedLecturesNumber() {
		return component.getPlannedLecturesNumber();
	}

	public boolean isAuthorizedAbsenceEnabled() {
		return component.isAuthorizedAbsenceEnabled();
	}

	public boolean isAbsenceDefaultAuthorized() {
		return component.isAbsenceDefaultAuthorized();
	}
	
	public boolean isWithNumOfLectures() {
		return component.isWithNumOfLectures();
	}

	public void setWithNumOfLectures(boolean withNumOfLectures) {
		component.setWithNumOfLectures(withNumOfLectures);
	}

	public boolean isWithExplanation() {
		return component.isWithExplanation();
	}

	public void setWithExplanation(boolean withExplanation) {
		component.setWithExplanation(withExplanation);
	}

	@Override
	protected LectureBlockRollCallStatusComponent getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void reset() {
		//
	}
}
