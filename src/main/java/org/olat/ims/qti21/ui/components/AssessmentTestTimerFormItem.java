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
package org.olat.ims.qti21.ui.components;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController.QtiWorksStatus;

/**
 * 
 * Initial date: 27 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestTimerFormItem extends FormItemImpl  {
	
	private final AssessmentTestTimerComponent component;
	private final AssessmentObjectFormItem assessmentFormItem;
	
	public AssessmentTestTimerFormItem(String name, QtiWorksStatus qtiWorksStatus, AssessmentObjectFormItem assessmentFormItem) {
		super(name);
		component = new AssessmentTestTimerComponent(name, qtiWorksStatus, this);
		component.setDomReplacementWrapperRequired(false);
		this.assessmentFormItem = assessmentFormItem;
	}
	
	public QtiWorksStatus getQtiWorksStatus() {
		return component.getQtiWorksStatus();
	}
	
	public AssessmentObjectFormItem getQtiRun() {
		return assessmentFormItem;
	}

	@Override
	protected Component getFormItemComponent() {
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
