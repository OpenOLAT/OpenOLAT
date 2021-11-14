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
package org.olat.ims.qti21.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AlienItemAnalyzer.Report;

/**
 * 
 * Initial date: 30 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UnkownItemConversionConfirmationController extends FormBasicController {

	private final Report report;
	private SingleSelection alternativeEl;
	
	public UnkownItemConversionConfirmationController(UserRequest ureq, WindowControl wControl, Report report) {
		super(ureq, wControl, "unkown_assessment_item_confirmation");
		this.report = report;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("warnings", report.getWarnings());
			layoutCont.contextPut("questionType", translate("new." + report.getType().name()));
		}
		
		if(report.getAlternatives().size() > 0) {
			String[] theKeys = new String[report.getAlternatives().size()];
			String[] theValues = new String[theKeys.length];
			for(int i=0; i<report.getAlternatives().size(); i++) {
				QTI21QuestionType alternative = report.getAlternatives().get(i);
				theKeys[i] = alternative.name();
				theValues[i] = translate("new." + alternative.name());
			}
			alternativeEl = uifactory.addDropdownSingleselect("questions.alternative", formLayout, theKeys, theValues, null);
			alternativeEl.setDomReplacementWrapperRequired(false);
		}

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("convert", formLayout);
	}
	
	public QTI21QuestionType getSelectedQuestionType() {
		if(alternativeEl != null && alternativeEl.isOneSelected()) {
			return QTI21QuestionType.valueOf(alternativeEl.getSelectedKey());
		}
		return report.getType();
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
