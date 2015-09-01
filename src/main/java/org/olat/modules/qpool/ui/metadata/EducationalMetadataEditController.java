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
package org.olat.modules.qpool.ui.metadata;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.manager.MetadataConverterHelper;
import org.olat.modules.qpool.model.LOMDuration;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QItemEdited;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EducationalMetadataEditController extends FormBasicController {
	
	private SingleSelection contextEl;
	private IntegerElement learningTimeDayElement, learningTimeHourElement, learningTimeMinuteElement, learningTimeSecondElement;
	private FormLayoutContainer learningTimeContainer;
	
	private QuestionItem item;
	private final QPoolService qpoolService;

	public EducationalMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.item = item;
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("educational");

		List<QEducationalContext> levels = qpoolService.getAllEducationlContexts();
		String[] contextKeys = new String[ levels.size() ];
		String[] contextValues = new String[ levels.size() ];
		int count = 0;
		for(QEducationalContext level:levels) {
			contextKeys[count] = level.getLevel();
			String translation = translate("item.level." + level.getLevel().toLowerCase());
			if(translation.length() > 128) {
				translation = level.getLevel();
			}
			contextValues[count++] = translation;
		}
		contextEl = uifactory.addDropdownSingleselect("educational.context", "educational.context", formLayout, contextKeys, contextValues, null);
		contextEl.setEnabled(count > 0);
		
		String page = velocity_root + "/learning_time.html";
		learningTimeContainer = FormLayoutContainer.createCustomFormLayout("learningTime", getTranslator(), page);
		((AbstractComponent)learningTimeContainer.getComponent()).setDomReplacementWrapperRequired(false);
		learningTimeContainer.setRootForm(mainForm);
		learningTimeContainer.setLabel("educational.learningTime", null);
		formLayout.add(learningTimeContainer);
		
		LOMDuration duration = MetadataConverterHelper.convertDuration(item.getEducationalLearningTime());
		learningTimeDayElement = uifactory.addIntegerElement("learningTime.day", "", duration.getDay(), learningTimeContainer);
		((AbstractComponent)learningTimeDayElement.getComponent()).setDomReplacementWrapperRequired(false);
		learningTimeDayElement.setDisplaySize(3);
		learningTimeDayElement.setMandatory(true);
		
		learningTimeHourElement = uifactory.addIntegerElement("learningTime.hour", "", duration.getHour(), learningTimeContainer);
		((AbstractComponent)learningTimeHourElement.getComponent()).setDomReplacementWrapperRequired(false);
		learningTimeHourElement.setDisplaySize(3);
		learningTimeHourElement.setMandatory(true);
		
		learningTimeMinuteElement = uifactory.addIntegerElement("learningTime.minute", "", duration.getMinute(), learningTimeContainer);
		((AbstractComponent)learningTimeMinuteElement.getComponent()).setDomReplacementWrapperRequired(false);
		learningTimeMinuteElement.setDisplaySize(3);
		learningTimeMinuteElement.setMandatory(true);
		
		learningTimeSecondElement = uifactory.addIntegerElement("learningTime.second", "", duration.getSeconds(), learningTimeContainer);
		((AbstractComponent)learningTimeSecondElement.getComponent()).setDomReplacementWrapperRequired(false);
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
				QEducationalContext context = qpoolService.getEducationlContextByLevel(contextEl.getSelectedKey());
				itemImpl.setEducationalContext(context);
			} else {
				itemImpl.setEducationalContext(null);
			}
			
			int day = learningTimeDayElement.getIntValue();
			int hour = learningTimeHourElement.getIntValue();
			int minute = learningTimeMinuteElement.getIntValue();
			int seconds = learningTimeSecondElement.getIntValue();
			String timeStr = MetadataConverterHelper.convertDuration(day, hour, minute, seconds);
			itemImpl.setEducationalLearningTime(timeStr);
		}
		item = qpoolService.updateItem(item);
		fireEvent(ureq, new QItemEdited(item));
	}
}