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
package org.olat.modules.assessment.ui.component;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 18 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningProgressItem extends FormItemImpl {

	private final LearningProgressComponent component;
	
	public LearningProgressItem(String name, Locale locale) {
		super(name);
		component = new LearningProgressComponent(name, locale);
	}

	public Boolean getFullyAssessed() {
		return component.getFullyAssessed();
	}

	public void setFullyAssessed(Boolean fullyAssessed) {
		component.setFullyAssessed(fullyAssessed);
	}

	public AssessmentEntryStatus getStatus() {
		return component.getStatus();
	}

	public void setStatus(AssessmentEntryStatus status) {
		component.setStatus(status);
	}

	public float getCompletion() {
		return component.getCompletion();
	}

	public void setCompletion(float completion) {
		component.setCompletion(completion);
	}

	public void setCompletion(Double completion) {
		component.setCompletion(completion);
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void reset() {
		//
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

}
