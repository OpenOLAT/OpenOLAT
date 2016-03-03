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
package org.olat.ims.qti21.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21OptionsController extends FormBasicController implements Activateable2 {
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };
	
	private MultipleSelectionElement enableSuspendEl, displayQuestionProgressEl, displayScoreProgressEl;
	
	private final RepositoryEntry testEntry;
	private final QTI21DeliveryOptions deliveryOptions;
	
	@Autowired
	private QTI21Service qtiService;
	
	public QTI21OptionsController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry) {
		super(ureq, wControl);
		this.testEntry = testEntry;
		this.deliveryOptions = qtiService.getDeliveryOptions(testEntry);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.options");
		
		displayQuestionProgressEl = uifactory.addCheckboxesHorizontal("questionProgress", "qti.form.questionprogress", formLayout, onKeys, onValues);
		if(deliveryOptions.getDisplayQuestionProgress() != null && deliveryOptions.getDisplayQuestionProgress().booleanValue()) {
			displayQuestionProgressEl.select(onKeys[0], true);
		}
		
		displayScoreProgressEl = uifactory.addCheckboxesHorizontal("scoreProgress", "qti.form.scoreprogress", formLayout, onKeys, onValues);
		if(deliveryOptions.getDisplayScoreProgress() != null && deliveryOptions.getDisplayScoreProgress().booleanValue()) {
			displayScoreProgressEl.select(onKeys[0], true);
		}
		
		enableSuspendEl = uifactory.addCheckboxesHorizontal("suspend", "qti.form.enablesuspend", formLayout, onKeys, onValues);
		if(deliveryOptions.getEnableSuspend() != null && deliveryOptions.getEnableSuspend().booleanValue()) {
			enableSuspendEl.select(onKeys[0], true);
		}
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsLayout.setRootForm(mainForm);
		formLayout.add(buttonsLayout);
		uifactory.addFormSubmitButton("save", buttonsLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(enableSuspendEl.isAtLeastSelected(1)) {
			deliveryOptions.setEnableSuspend(Boolean.TRUE);
		} else {
			deliveryOptions.setEnableSuspend(Boolean.FALSE);
		}
		
		if(displayQuestionProgressEl.isAtLeastSelected(1)) {
			deliveryOptions.setDisplayQuestionProgress(Boolean.TRUE);
		} else {
			deliveryOptions.setDisplayQuestionProgress(Boolean.FALSE);
		}
		
		if(displayScoreProgressEl.isAtLeastSelected(1)) {
			deliveryOptions.setDisplayScoreProgress(Boolean.TRUE);
		} else {
			deliveryOptions.setDisplayScoreProgress(Boolean.FALSE);
		}
		
		qtiService.setDeliveryOptions(testEntry, deliveryOptions);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}