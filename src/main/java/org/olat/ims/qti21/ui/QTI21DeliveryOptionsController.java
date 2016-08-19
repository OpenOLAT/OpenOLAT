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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.ShowResultsOnFinish;
import org.olat.ims.qti21.QTI21Service;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21DeliveryOptionsController extends FormBasicController implements Activateable2 {
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };

	private MultipleSelectionElement showTitlesEl, showMenuEl;
	private MultipleSelectionElement personalNotesEl;
	private MultipleSelectionElement enableCancelEl, enableSuspendEl;
	private MultipleSelectionElement limitAttemptsEl, blockAfterSuccessEl;
	private MultipleSelectionElement displayQuestionProgressEl, displayScoreProgressEl;
	private MultipleSelectionElement showResultsOnFinishEl;
	private MultipleSelectionElement allowAnonymEl;
	private SingleSelection typeShowResultsOnFinishEl;
	private TextElement maxAttemptsEl;
	
	private boolean changes;
	private final RepositoryEntry testEntry;
	private final QTI21DeliveryOptions deliveryOptions;
	
	@Autowired
	private QTI21Service qtiService;
	
	public QTI21DeliveryOptionsController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry) {
		super(ureq, wControl);
		this.testEntry = testEntry;
		deliveryOptions = qtiService.getDeliveryOptions(testEntry);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.options");

		limitAttemptsEl = uifactory.addCheckboxesHorizontal("limitAttempts", "qti.form.limit.attempts", formLayout, onKeys, onValues);
		limitAttemptsEl.addActionListener(FormEvent.ONCLICK);
		String maxAttemptsValue = "";
		int maxAttempts =  deliveryOptions.getMaxAttempts();
		if(maxAttempts > 0) {
			limitAttemptsEl.select(onKeys[0], true);
		}
		maxAttemptsEl = uifactory.addTextElement("maxAttempts", "qti.form.attempts", 8, maxAttemptsValue, formLayout);	
		maxAttemptsEl.setDisplaySize(2);
		maxAttemptsEl.setVisible(maxAttempts > 0);
		
		blockAfterSuccessEl = uifactory.addCheckboxesHorizontal("blockAfterSuccess", "qti.form.block.afterSuccess", formLayout, onKeys, onValues);
		if(deliveryOptions.isBlockAfterSuccess()) {
			blockAfterSuccessEl.select(onKeys[0], true);
		}
		
		allowAnonymEl = uifactory.addCheckboxesHorizontal("allowAnonym", "qti.form.allow.anonym", formLayout, onKeys, onValues);
		if(deliveryOptions.isAllowAnonym()) {
			allowAnonymEl.select(onKeys[0], true);
		}

		showTitlesEl = uifactory.addCheckboxesHorizontal("showTitles", "qti.form.questiontitle", formLayout, onKeys, onValues);
		if(deliveryOptions.isShowTitles()) {
			showTitlesEl.select(onKeys[0], true);
		}
		
		showMenuEl = uifactory.addCheckboxesHorizontal("showMenu", "qti.form.menudisplay", formLayout, onKeys, onValues);
		if(deliveryOptions.isShowMenu()) {
			showMenuEl.select(onKeys[0], true);
		}
		
		personalNotesEl = uifactory.addCheckboxesHorizontal("personalNotes", "qti.form.auto.memofield", formLayout, onKeys, onValues);
		if(deliveryOptions.isPersonalNotes()) {
			personalNotesEl.select(onKeys[0], true);
		}
		
		displayQuestionProgressEl = uifactory.addCheckboxesHorizontal("questionProgress", "qti.form.questionprogress", formLayout, onKeys, onValues);
		if(deliveryOptions.isDisplayQuestionProgress()) {
			displayQuestionProgressEl.select(onKeys[0], true);
		}
		
		displayScoreProgressEl = uifactory.addCheckboxesHorizontal("scoreProgress", "qti.form.scoreprogress", formLayout, onKeys, onValues);
		if(deliveryOptions.isDisplayScoreProgress()) {
			displayScoreProgressEl.select(onKeys[0], true);
		}
		
		enableSuspendEl = uifactory.addCheckboxesHorizontal("suspend", "qti.form.enablesuspend", formLayout, onKeys, onValues);
		if(deliveryOptions.isEnableSuspend()) {
			enableSuspendEl.select(onKeys[0], true);
		}
		
		enableCancelEl = uifactory.addCheckboxesHorizontal("cancel", "qti.form.enablecancel", formLayout, onKeys, onValues);
		if(deliveryOptions.isEnableCancel()) {
			enableCancelEl.select(onKeys[0], true);
		}
		
		showResultsOnFinishEl = uifactory.addCheckboxesHorizontal("resultOnFiniish", "qti.form.results.onfinish", formLayout, onKeys, onValues);
		showResultsOnFinishEl.addActionListener(FormEvent.ONCHANGE);
		if(deliveryOptions.getShowResultsOnFinish() != null && !ShowResultsOnFinish.none.equals(deliveryOptions.getShowResultsOnFinish())) {
			showResultsOnFinishEl.select(onKeys[0], true);
		}
		
		String[] typeShowResultsOnFinishKeys = new String[] {
			ShowResultsOnFinish.compact.name(), ShowResultsOnFinish.sections.name(), ShowResultsOnFinish.details.name()
		};
		String[] typeShowResultsOnFinishValues = new String[] {
			translate("qti.form.summary.compact"), translate("qti.form.summary.section"), translate("qti.form.summary.detailed")
		};
		typeShowResultsOnFinishEl = uifactory.addRadiosVertical("typeResultOnFiniish", "qti.form.summary", formLayout, typeShowResultsOnFinishKeys, typeShowResultsOnFinishValues);
		typeShowResultsOnFinishEl.setVisible(showResultsOnFinishEl.isAtLeastSelected(1));
		if(deliveryOptions.getShowResultsOnFinish() != null && !ShowResultsOnFinish.none.equals(deliveryOptions.getShowResultsOnFinish())) {
			typeShowResultsOnFinishEl.select(deliveryOptions.getShowResultsOnFinish().name(), true);
		} else {
			typeShowResultsOnFinishEl.select(ShowResultsOnFinish.compact.name(), true);
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
	
	public boolean hasChanges() {
		return changes;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		if(limitAttemptsEl.isAtLeastSelected(1)) {
			maxAttemptsEl.clearError();
			if(StringHelper.containsNonWhitespace(maxAttemptsEl.getValue())) {
				try {
					Integer.parseInt(maxAttemptsEl.getValue());
				} catch(NumberFormatException e) {
					maxAttemptsEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} else {
				maxAttemptsEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(limitAttemptsEl == source) {
			maxAttemptsEl.setVisible(limitAttemptsEl.isAtLeastSelected(1));
		}
		if(showResultsOnFinishEl == source) {
			typeShowResultsOnFinishEl.setVisible(showResultsOnFinishEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(limitAttemptsEl.isAtLeastSelected(1)) {
			deliveryOptions.setMaxAttempts(Integer.parseInt(maxAttemptsEl.getValue()));
		} else {
			deliveryOptions.setMaxAttempts(0);
		}
		deliveryOptions.setBlockAfterSuccess(blockAfterSuccessEl.isAtLeastSelected(1));
		deliveryOptions.setShowMenu(showMenuEl.isAtLeastSelected(1));
		deliveryOptions.setShowTitles(showTitlesEl.isAtLeastSelected(1));
		deliveryOptions.setPersonalNotes(personalNotesEl.isAtLeastSelected(1));
		deliveryOptions.setEnableCancel(enableCancelEl.isAtLeastSelected(1));
		deliveryOptions.setEnableSuspend(enableSuspendEl.isAtLeastSelected(1));
		deliveryOptions.setDisplayQuestionProgress(displayQuestionProgressEl.isAtLeastSelected(1));
		deliveryOptions.setDisplayScoreProgress(displayScoreProgressEl.isAtLeastSelected(1));
		deliveryOptions.setAllowAnonym(allowAnonymEl.isAtLeastSelected(1));
		if(showResultsOnFinishEl.isAtLeastSelected(1)) {
			if(typeShowResultsOnFinishEl.isOneSelected()) {
				String selectedType = typeShowResultsOnFinishEl.getSelectedKey();
				deliveryOptions.setShowResultsOnFinish(ShowResultsOnFinish.valueOf(selectedType));
			} else {
				deliveryOptions.setShowResultsOnFinish(ShowResultsOnFinish.compact);
			}
		} else {
			deliveryOptions.setShowResultsOnFinish(ShowResultsOnFinish.none);
		}
		qtiService.setDeliveryOptions(testEntry, deliveryOptions);
		changes = true;
		fireEvent(ureq, Event.DONE_EVENT);
	}
}