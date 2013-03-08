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
package org.olat.modules.qpool.ui.edit;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionPoolService;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.MetadatasController;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EducationalMetadataEditController extends FormBasicController {
	
	private SingleSelection contextEl;
	private TextElement learningTimeDayElement, learningTimeHourElement, learningTimeMinuteElement, learningTimeSecondElement;
	private FormLayoutContainer learningTimeContainer;
	
	private QuestionItem item;
	private final QuestionPoolService qpoolService;

	public EducationalMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MetadatasController.class, ureq.getLocale(), getTranslator()));
		
		this.item = item;
		qpoolService = CoreSpringFactory.getImpl(QuestionPoolService.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("educational");
		
		String[] contextKeys = new String[]{ "" };
		contextEl = uifactory.addDropdownSingleselect("educational.context", "educational.context", formLayout, contextKeys, contextKeys, null);

		//learningTimeEl = uifactory.addTextElement("educational.learningTime", "educational.learningTime", 1000, "", formLayout);

		String page = velocity_root + "/learning_time.html";
		learningTimeContainer = FormLayoutContainer.createCustomFormLayout("learningTime", getTranslator(), page);
		learningTimeContainer.setRootForm(mainForm);
		learningTimeContainer.setLabel("educational.learningTime", null);
		formLayout.add(learningTimeContainer);
		
		String day = "d";
		String hour = "h";
		String minute = "m";
		String second = "s";
		
		learningTimeDayElement = uifactory.addTextElement("learningTime.day", "", 2, day, learningTimeContainer);
		learningTimeDayElement.setDisplaySize(3);
		learningTimeDayElement.setMandatory(true);
		
		learningTimeHourElement = uifactory.addTextElement("learningTime.hour", "", 2, hour, learningTimeContainer);
		learningTimeHourElement.setDisplaySize(3);
		learningTimeHourElement.setMandatory(true);
		
		learningTimeMinuteElement = uifactory.addTextElement("learningTime.minute", "", 2, minute, learningTimeContainer);
		learningTimeMinuteElement.setDisplaySize(3);
		learningTimeMinuteElement.setMandatory(true);
		
		learningTimeSecondElement = uifactory.addTextElement("learningTime.second", "", 2, second, learningTimeContainer);
		learningTimeSecondElement.setDisplaySize(3);
		learningTimeSecondElement.setMandatory(true);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			if(contextEl.isOneSelected()) {
				itemImpl.setEducationalContext(contextEl.getSelectedKey());
			} else {
				itemImpl.setEducationalContext(null);
			}
			
			//itemImpl.setEducationalLearningTime(learningTimeEl.getValue());
		}
		item = qpoolService.updateItem(item);
		fireEvent(ureq, new QItemEdited(item));
	}
}