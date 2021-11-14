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
package org.olat.modules.lecture.ui;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.Reason;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CloseRollCallConfirmationController extends FormBasicController {

	private FormLink quickSaveButton;
	private TextElement blockCommentEl;
	private SingleSelection effectiveLecturesEl;
	private SingleSelection effectiveEndReasonEl;
	private TextElement effectiveEndHourEl;
	private TextElement effectiveEndMinuteEl;
	
	private LectureBlock lectureBlock;
	private final RollCallSecurityCallback secCallback;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	
	public CloseRollCallConfirmationController(UserRequest ureq, WindowControl wControl,
			LectureBlock lectureBlock, RollCallSecurityCallback secCallback) {
		super(ureq, wControl);
		this.lectureBlock = lectureBlock;
		this.secCallback = secCallback;
		
		initForm(ureq);
	}
	
	public LectureBlock getLectureBLock() {
		return lectureBlock;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_lecture_confirm_close_form");
		if(lectureModule.isStatusPartiallyDoneEnabled()) {
			int plannedLectures = lectureBlock.getPlannedLecturesNumber();
			String[] effectiveKeys = new String[plannedLectures];
			for(int i=plannedLectures; i-->0; ) {
				effectiveKeys[i] = Integer.toString(i + 1);
			}
			effectiveLecturesEl = uifactory.addDropdownSingleselect("effective.lectures", formLayout, effectiveKeys, effectiveKeys, null);
			int selectedEffectiveLectures = lectureBlock.getPlannedLecturesNumber();
			if(lectureBlock.getEffectiveLecturesNumber() > 0) {
				selectedEffectiveLectures = lectureBlock.getEffectiveLecturesNumber();
			}
			String selectedKey = Integer.toString(selectedEffectiveLectures);
			for(String effectiveKey:effectiveKeys) {
				if(effectiveKey.equals(selectedKey)) {
					effectiveLecturesEl.select(effectiveKey, true);
					break;
				}
			}
		}
		
		String datePage = velocity_root + "/date_end.html";
		FormLayoutContainer dateCont = FormLayoutContainer.createCustomFormLayout("start_end", getTranslator(), datePage);
		dateCont.setLabel("lecture.block.effective.end", null);
		formLayout.add(dateCont);
		
		effectiveEndHourEl = uifactory.addTextElement("lecture.end.hour", null, 2, "", dateCont);
		effectiveEndHourEl.setDomReplacementWrapperRequired(false);
		effectiveEndHourEl.setDisplaySize(2);
		effectiveEndHourEl.setEnabled(secCallback.canEdit());
		effectiveEndMinuteEl = uifactory.addTextElement("lecture.end.minute", null, 2, "", dateCont);
		effectiveEndMinuteEl.setDomReplacementWrapperRequired(false);
		effectiveEndMinuteEl.setDisplaySize(2);
		effectiveEndMinuteEl.setEnabled(secCallback.canEdit());

		Calendar cal = Calendar.getInstance();
		if(lectureBlock.getEffectiveEndDate() != null) {
			cal.setTime(lectureBlock.getEffectiveEndDate());
		} else if(lectureBlock.getEndDate() != null) {
			cal.setTime(lectureBlock.getEndDate());
		}
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		effectiveEndHourEl.setValue(Integer.toString(hour));
		
		String minuteStr = Integer.toString(minute);
		if(minuteStr.length() == 1) {
			minuteStr = "0" + minuteStr;
		}
		effectiveEndMinuteEl.setValue(minuteStr);

		List<Reason> allReasons = lectureService.getAllReasons();
		if(!allReasons.isEmpty()) {
			if(allReasons.size() > 2) {
				Collections.sort(allReasons, new ReasonComparator());
			}
			
			int numOfReasons = allReasons.size();
			SelectionValues reasonKeyValues = new SelectionValues();
			reasonKeyValues.add(SelectionValues.entry("-", "-"));
			for(int i=numOfReasons; i-->0; ) {
				Reason reason = allReasons.get(i);
				if(reason.isEnabled() || reason.equals(lectureBlock.getReasonEffectiveEnd())) {
					reasonKeyValues.add(SelectionValues.entry(reason.getKey().toString(), reason.getTitle()));
				}
			}
			effectiveEndReasonEl = uifactory.addDropdownSingleselect("effective.reason", "lecture.block.effective.reason", formLayout,
					reasonKeyValues.keys(), reasonKeyValues.values(), null);
			effectiveEndReasonEl.setEnabled(secCallback.canEdit());
			effectiveEndReasonEl.setVisible(reasonKeyValues.size() > 1);
			if(lectureBlock.getReasonEffectiveEnd() != null) {
				String selectedReasonKey = lectureBlock.getReasonEffectiveEnd().getKey().toString();
				for(String reasonKey:reasonKeyValues.keys()) {
					if(reasonKey.equals(selectedReasonKey)) {
						effectiveEndReasonEl.select(reasonKey, true);
						break;
					}
				}
			}
		}
		
		String blockComment = lectureBlock.getComment();
		blockCommentEl = uifactory.addTextAreaElement("lecture.block.comment", 4, 72, blockComment, formLayout);
		blockCommentEl.setEnabled(secCallback.canEdit());

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		quickSaveButton = uifactory.addFormLink("save.temporary", buttonsCont, Link.BUTTON);
		uifactory.addFormSubmitButton("close.lecture.blocks", buttonsCont);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String before = lectureService.toAuditXml(lectureBlock);
		commitLectureBlocks();
		lectureBlock = lectureService.close(lectureBlock, getIdentity());
		fireEvent(ureq, Event.DONE_EVENT);

		String after = lectureService.toAuditXml(lectureBlock);
		lectureService.auditLog(LectureBlockAuditLog.Action.closeLectureBlock, before, after, null, lectureBlock, null, lectureBlock.getEntry(), null, getIdentity());

		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_ROLL_CALL_CLOSED, getClass(),
				CoreLoggingResourceable.wrap(lectureBlock, OlatResourceableType.lectureBlock, lectureBlock.getTitle()));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(quickSaveButton == source) {
			doQuickSave(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		effectiveEndHourEl.clearError();
		//need to be the first validation
		if(StringHelper.containsNonWhitespace(effectiveEndHourEl.getValue())
				|| StringHelper.containsNonWhitespace(effectiveEndMinuteEl.getValue())) {
			allOk &= validateInt(effectiveEndHourEl, 24);
			allOk &= validateInt(effectiveEndMinuteEl, 60);
		} else {
			effectiveEndHourEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if(effectiveEndReasonEl != null) {
			effectiveEndReasonEl.clearError();
			if(effectiveEndReasonEl.isVisible() && !effectiveEndReasonEl.isOneSelected() || effectiveEndReasonEl.isSelected(0)) {
				effectiveEndReasonEl.setErrorKey("error.reason.mandatory", null);
				allOk &= false;
			}
		}
		
		if(effectiveLecturesEl != null) {
			effectiveLecturesEl.clearError();
			if(!effectiveLecturesEl.isOneSelected()) {
				effectiveLecturesEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
	
		return allOk;
	}
	
	private boolean validateInt(TextElement element, int max) {
		boolean allOk = true;
		
		element.clearError();
		if(StringHelper.containsNonWhitespace(element.getValue())) {
			try {
				int val = Integer.parseInt(element.getValue());
				if(val < 0 || val > max) {
					element.setErrorKey("error.integer.between", new String[] { "0", Integer.toString(max)} );
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				element.setErrorKey("error.integer.between", new String[] { "0", Integer.toString(max)} );
				allOk &= false;
			}
		} else {
			element.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private Date getEffectiveEndDate() {
		Date effectiveEndDate = null;
		if(StringHelper.containsNonWhitespace(effectiveEndHourEl.getValue())
				&& StringHelper.containsNonWhitespace(effectiveEndMinuteEl.getValue())) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(lectureBlock.getStartDate());
			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(effectiveEndHourEl.getValue()));
			cal.set(Calendar.MINUTE, Integer.parseInt(effectiveEndMinuteEl.getValue()));
			effectiveEndDate = cal.getTime();
		}
		return effectiveEndDate;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doQuickSave(UserRequest ureq) {
		String before = lectureService.toAuditXml(lectureBlock);
		commitLectureBlocks();
		lectureBlock = lectureService.save(lectureBlock, null);
		lectureService.recalculateSummary(lectureBlock.getEntry());
		fireEvent(ureq, Event.DONE_EVENT);

		String after = lectureService.toAuditXml(lectureBlock);
		lectureService.auditLog(LectureBlockAuditLog.Action.saveLectureBlock, before, after, null, lectureBlock, null, lectureBlock.getEntry(), null, getIdentity());
	}
	
	private void commitLectureBlocks() {
		lectureBlock.setComment(blockCommentEl.getValue());
		
		int effectiveLectures = lectureBlock.getPlannedLecturesNumber();
		if(effectiveLecturesEl != null) {
			try {
				String selectedKey = effectiveLecturesEl.getSelectedKey();
				effectiveLectures = Integer.parseInt(selectedKey);
			} catch(Exception ex) {
				logError("", ex);
			}
		}
		lectureBlock.setEffectiveLecturesNumber(effectiveLectures);
		Date effectiveEndDate = getEffectiveEndDate();
		if(effectiveEndDate == null) {
			lectureBlock.setReasonEffectiveEnd(null);
		} else {
			lectureBlock.setEffectiveEndDate(effectiveEndDate);
			if(effectiveEndReasonEl == null || !effectiveEndReasonEl.isVisible()
					|| "-".equals(effectiveEndReasonEl.getSelectedKey())) {
				lectureBlock.setReasonEffectiveEnd(null);
			} else {
				Long reasonKey = Long.valueOf(effectiveEndReasonEl.getSelectedKey());
				Reason selectedReason = lectureService.getReason(reasonKey);
				lectureBlock.setReasonEffectiveEnd(selectedReason);
			}
		}
	}
}
