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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.Reason;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.ui.TeacherRollCallDataModel.RollCols;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherRollCallController extends FormBasicController {
	
	protected static final String USER_PROPS_ID = TeacherRollCallController.class.getCanonicalName();
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final int CHECKBOX_OFFSET = 1000;
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };
	
	private FlexiTableElement tableEl;
	private TeacherRollCallDataModel tableModel;
	private TextElement blokcCommentEl;
	private TextElement effectiveEndHourEl, effectiveEndMinuteEl;
	private SingleSelection statusEl, effectiveEndReasonEl, rollCallStatusEl;
	
	private ReasonController reasonCtrl;
	private CloseableCalloutWindowController reasonCalloutCtrl;
	
	private int counter = 0;
	private LectureBlock lectureBlock;
	private final boolean isAdministrativeUser;
	private List<UserPropertyHandler> userPropertyHandlers;
	private RollCallSecurityCallback secCallback;
	
	private List<Identity> participants;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public TeacherRollCallController(UserRequest ureq, WindowControl wControl,
			LectureBlock block, List<Identity> participants, RollCallSecurityCallback secCallback) {
		super(ureq, wControl, "rollcall");
		
		this.lectureBlock = block;
		this.secCallback = secCallback;
		this.participants = participants;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// form for the lecture block
		FormLayoutContainer blockCont = FormLayoutContainer.createDefaultFormLayout("block", getTranslator());
		blockCont.setRootForm(mainForm);
		blockCont.setFormTitle(translate("lecture.block"));
		formLayout.add("block", blockCont);
		
		uifactory.addStaticTextElement("lecture.title", lectureBlock.getTitle(), blockCont);
		
		String[] statusKeys = getAvailableStatus();
		String[] statusValues = new String[statusKeys.length];
		for(int i=statusKeys.length; i-->0; ) {
			statusValues[i] = translate(statusKeys[i]);
		}
		statusEl = uifactory.addDropdownSingleselect("status", "lecture.block.status", blockCont, statusKeys, statusValues, null);
		boolean statusFound = false;
		if(lectureBlock.getStatus() != null) {
			String lectureBlockStatus = lectureBlock.getStatus().name();
			for(int i=statusKeys.length; i-->0; ) {
				if(lectureBlockStatus.equals(statusKeys[i])) {
					statusEl.select(statusKeys[i], true);
					statusFound = true;
					break;
				}
			}
		}
		if(!statusFound) {
			statusEl.select(statusKeys[0], true);
		}

		String datePage = velocity_root + "/date_start_end.html";
		FormLayoutContainer dateCont = FormLayoutContainer.createCustomFormLayout("start_end", getTranslator(), datePage);
		dateCont.setLabel("lecture.block.effective.end", null);
		blockCont.add(dateCont);
		
		effectiveEndHourEl = uifactory.addTextElement("lecture.end.hour", null, 2, "", dateCont);
		effectiveEndHourEl.setDomReplacementWrapperRequired(false);
		effectiveEndHourEl.setDisplaySize(2);
		effectiveEndHourEl.setEnabled(secCallback.canEdit());
		effectiveEndMinuteEl = uifactory.addTextElement("lecture.end.minute", null, 2, "", dateCont);
		effectiveEndMinuteEl.setDomReplacementWrapperRequired(false);
		effectiveEndMinuteEl.setDisplaySize(2);
		effectiveEndMinuteEl.setEnabled(secCallback.canEdit());
		if(lectureBlock != null) {
			Calendar cal = Calendar.getInstance();
			if(lectureBlock.getEffectiveEndDate() != null) {
				cal.setTime(lectureBlock.getEffectiveEndDate());
			} else if(lectureBlock.getEndDate() != null) {
				cal.setTime(lectureBlock.getEndDate());
			}
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);
			effectiveEndHourEl.setValue(Integer.toString(hour));
			effectiveEndMinuteEl.setValue(Integer.toString(minute));
		}
		
		List<String> reasonKeyList = new ArrayList<>();
		List<String> reasonValueList = new ArrayList<>();
		reasonKeyList.add("-");
		reasonValueList.add("-");
		
		List<Reason> allReasons = lectureService.getAllReasons();
		for(Reason reason:allReasons) {
			reasonKeyList.add(reason.getKey().toString());
			reasonValueList.add(reason.getTitle());
		}
		effectiveEndReasonEl = uifactory.addDropdownSingleselect("effective.reason", "lecture.block.effective.reason", blockCont,
				reasonKeyList.toArray(new String[reasonKeyList.size()]), reasonValueList.toArray(new String[reasonValueList.size()]), null);
		effectiveEndReasonEl.setEnabled(secCallback.canEdit());
		boolean found = false;
		if(lectureBlock.getReasonEffectiveEnd() != null) {
			String selectedReasonKey = lectureBlock.getReasonEffectiveEnd().getKey().toString();
			for(String reasonKey:reasonKeyList) {
				if(reasonKey.equals(selectedReasonKey)) {
					effectiveEndReasonEl.select(reasonKey, true);
					found = true;
					break;
				}
			}
		}
		if(!found) {
			effectiveEndReasonEl.select(reasonKeyList.get(0), true);
		}

		String blockComment = lectureBlock.getComment();
		blokcCommentEl = uifactory.addTextElement("block.comment", "lecture.block.comment", 256, blockComment, blockCont);
		blokcCommentEl.setEnabled(secCallback.canEdit());
		
		//roll call
		FormLayoutContainer rollCallStatusCont = FormLayoutContainer.createDefaultFormLayout("rollcallStatus", getTranslator());
		formLayout.add(rollCallStatusCont);
		formLayout.add("rollcallStatus", rollCallStatusCont);
		rollCallStatusCont.setRootForm(mainForm);
		rollCallStatusCont.setFormTitle(translate("rollcall"));

		String[] rollCallKeys = new String[]{
				LectureRollCallStatus.open.name(), LectureRollCallStatus.reopen.name(),
				LectureRollCallStatus.closed.name(), LectureRollCallStatus.autoclosed.name()
		};
		String[] rollCallValues = new String[rollCallKeys.length];
		for(int i=rollCallKeys.length; i-->0; ) {
			rollCallValues[i] = translate(rollCallKeys[i]);
		}
		rollCallStatusEl = uifactory.addDropdownSingleselect("rollcall.status", "rollcall.status", rollCallStatusCont, rollCallKeys, rollCallValues, null);
		rollCallStatusEl.setMandatory(true);
		LectureRollCallStatus rollCallStatus = lectureBlock.getRollCallStatus() == null ? LectureRollCallStatus.open : lectureBlock.getRollCallStatus();
		for(int i=rollCallKeys.length; i-->0; ) {
			if(rollCallStatus.name().equals(rollCallKeys[i])) {
				rollCallStatusEl.select(rollCallKeys[i], true);
			}
		}
		
		// table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCols.username));
		}
		
		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}
		
		for(int i=0; i<lectureBlock.getPlannedLecturesNumber(); i++) {
			FlexiColumnModel col = new DefaultFlexiColumnModel("table.header.lecture." + (i+1), i + CHECKBOX_OFFSET, true, "lecture." + (i+1));
			columnsModel.addFlexiColumnModel(col);
		}
		
		//all button
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("all", translate("all"), "all"));
		if(secCallback.canViewAuthorizedAbsences() || secCallback.canEditAuthorizedAbsences()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCols.authorizedAbsence));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCols.comment));

		tableModel = new TeacherRollCallDataModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(found);
		uifactory.addFormSubmitButton("save", "save", formLayout);
	}
	
	private String[] getAvailableStatus() {
		List<String> statusList = new ArrayList<>();
		statusList.add(LectureBlockStatus.active.name());
		if(lectureModule.isStatusPartiallyDoneEnabled()) {
			statusList.add(LectureBlockStatus.partiallydone.name());
		}
		statusList.add(LectureBlockStatus.done.name());
		if(lectureModule.isStatusCancelledEnabled()) {
			statusList.add(LectureBlockStatus.cancelled.name());
		}
		return statusList.toArray(new String[statusList.size()]);
	}
	
	private void loadModel() {
		List<LectureBlockRollCall> rollCalls = lectureService.getRollCalls(lectureBlock);
		Map<Identity,LectureBlockRollCall> rollCallMap = new HashMap<>();
		for(LectureBlockRollCall rollCall:rollCalls) {
			rollCallMap.put(rollCall.getIdentity(), rollCall);
		}
		
		List<TeacherRollCallRow> rows = new ArrayList<>(participants.size());
		for(Identity participant:participants) {
			LectureBlockRollCall rollCall = rollCallMap.get(participant);
			rows.add(forgeRow(participant, rollCall));
		}
		tableModel.setObjects(rows);
	}
	
	private TeacherRollCallRow forgeRow(Identity participant, LectureBlockRollCall rollCall) {
		TeacherRollCallRow row = new TeacherRollCallRow(rollCall, participant, userPropertyHandlers, getLocale());
		
		int numOfChecks = lectureBlock.getPlannedLecturesNumber();
		MultipleSelectionElement[] checks = new MultipleSelectionElement[numOfChecks];
		List<Integer> absences = rollCall == null ? Collections.emptyList() : rollCall.getLecturesAbsentList();
		
		for(int i=0; i<numOfChecks; i++) {
			String checkId = "check_".concat(Integer.toString(++counter));
			MultipleSelectionElement check = uifactory.addCheckboxesHorizontal(checkId, null, flc, onKeys, onValues);
			check.setDomReplacementWrapperRequired(false);
			check.addActionListener(FormEvent.ONCHANGE);
			check.setEnabled(secCallback.canEditAbsences());
			check.setUserObject(row);
			check.setAjaxOnly(true);
			if(absences.contains(i)) {
				check.select(onKeys[0], true);
			}
			checks[i] = check;
			flc.add(check);
		}
		row.setChecks(checks);
		
		if(secCallback.canEditAuthorizedAbsences() || secCallback.canViewAuthorizedAbsences()) {
			String page = velocity_root + "/authorized_absence_cell.html";
			FormLayoutContainer absenceCont = FormLayoutContainer.createCustomFormLayout("auth_cont_".concat(Integer.toString(++counter)), getTranslator(), page);
			absenceCont.setRootForm(mainForm);
			flc.add(absenceCont);
			
			String authorizedAbsencedId = "auth_abs_".concat(Integer.toString(++counter));
			MultipleSelectionElement authorizedAbsencedEl = uifactory.addCheckboxesHorizontal(authorizedAbsencedId, null, absenceCont, onKeys, onValues);
			authorizedAbsencedEl.setDomReplacementWrapperRequired(false);
			authorizedAbsencedEl.addActionListener(FormEvent.ONCHANGE);
			authorizedAbsencedEl.setUserObject(row);
			authorizedAbsencedEl.setAjaxOnly(true);
			authorizedAbsencedEl.setEnabled(secCallback.canEdit() && secCallback.canEditAuthorizedAbsences());
			
			boolean hasAuthorization = rollCall != null && rollCall.getAbsenceAuthorized() != null
					&& rollCall.getAbsenceAuthorized().booleanValue();
			if(hasAuthorization) {
				authorizedAbsencedEl.select(onKeys[0], true);
			}
			row.setAuthorizedAbsence(authorizedAbsencedEl);
			flc.add(authorizedAbsencedEl);

			String reasonId = "abs_reason_".concat(Integer.toString(++counter));
			FormLink reasonLink = uifactory.addFormLink(reasonId, "reason", null, absenceCont, Link.BUTTON_XSMALL);
			reasonLink.setDomReplacementWrapperRequired(false);
			reasonLink.setIconLeftCSS("o_icon o_icon_notes");
			reasonLink.setVisible(hasAuthorization);
			reasonLink.setUserObject(row);
			row.setReasonLink(reasonLink);
			
			row.setAuthorizedAbsenceCont(absenceCont);
			absenceCont.contextPut("row", row);
		}

		String comment = rollCall == null ? "" : rollCall.getComment();
		String commentId = "comment_".concat(Integer.toString(++counter));
		TextElement commentEl = uifactory.addTextElement(commentId, commentId, null, 128, comment, flc);
		commentEl.setDomReplacementWrapperRequired(false);
		commentEl.setEnabled(secCallback.canEdit());
		row.setCommentEl(commentEl);
		flc.add(commentEl);
		return row;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent event) {
		if(!(source instanceof MultipleSelectionElement)) {
			super.propagateDirtinessToContainer(source, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(reasonCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doReason(reasonCtrl.getTeacherRollCallRow(), reasonCtrl.getReason());
			}
			reasonCalloutCtrl.deactivate();
			cleanUp();
		} else if(reasonCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(reasonCalloutCtrl);
		removeAsListenerAndDispose(reasonCtrl);
		reasonCalloutCtrl = null;
		reasonCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				TeacherRollCallRow row = tableModel.getObject(se.getIndex());
				if("all".equals(cmd)) {
					doCheckAllRow(row);
				}
			}
		} else if(source instanceof MultipleSelectionElement) {
			MultipleSelectionElement check = (MultipleSelectionElement)source;
			TeacherRollCallRow row = (TeacherRollCallRow)check.getUserObject();
			if(row.getAuthorizedAbsence() == check) {
				doAuthorizedAbsence(row, check);
			} else {
				doCheckRow(row, check);
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if(cmd != null && cmd.startsWith("abs_reason_")) {
				TeacherRollCallRow row = (TeacherRollCallRow)link.getUserObject();
				doCalloutReasonAbsence(ureq, link, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		
		boolean fullValidation = false;
		if(!statusEl.isOneSelected()) {
			statusEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			fullValidation = LectureBlockStatus.done.name().equals(statusEl.getSelectedKey());
		}
		
		if(!rollCallStatusEl.isOneSelected()) {
			rollCallStatusEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		//block form
		if(StringHelper.containsNonWhitespace(effectiveEndHourEl.getValue())
				|| StringHelper.containsNonWhitespace(effectiveEndMinuteEl.getValue())) {
			allOk &= validateInt(effectiveEndHourEl, 24, fullValidation);
			allOk &= validateInt(effectiveEndMinuteEl, 60, fullValidation);
			
			if(fullValidation && (!effectiveEndReasonEl.isOneSelected() || effectiveEndReasonEl.isSelected(0))) {
				effectiveEndReasonEl.setErrorKey("error.reason.mandatory", null);
				allOk &= false;
			}
		} else if(fullValidation) {
			effectiveEndHourEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		// table
		if(fullValidation) {
			for(int i=tableModel.getRowCount(); i-->0; ) {
				TeacherRollCallRow row = tableModel.getObject(i);
				row.getAuthorizedAbsence().clearError();
				
				if(row.getRollCall() == null) {
					//??? stop?
				} else {
					String reason = row.getRollCall().getAbsenceReason();
					if(row.getAuthorizedAbsence().isAtLeastSelected(1) && !StringHelper.containsNonWhitespace(reason)) {
						row.getAuthorizedAbsence().setErrorKey("error.reason.mandatory", null);
						allOk &= false;
					}
				}
			}
		}

		return allOk & super.validateFormLogic(ureq);
	}
	
	private boolean validateInt(TextElement element, int max, boolean mandatory) {
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
		} else if(mandatory) {
			element.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(int i=tableModel.getRowCount(); i-->0; ) {
			TeacherRollCallRow row = tableModel.getObject(i);
			
			int numOfChecks = row.getChecks().length;
			List<Integer> absenceList = new ArrayList<>(numOfChecks);
			for(int j=0; j<numOfChecks; j++) {
				if(row.getCheck(j).isAtLeastSelected(1)) {
					absenceList.add(j);
				}
			}
			
			String comment = row.getCommentEl().getValue();
			LectureBlockRollCall rollCall = lectureService.addRollCall(row.getIdentity(), lectureBlock, row.getRollCall(),
					comment, absenceList);
			row.setRollCall(rollCall);
		}

		lectureBlock = lectureService.getLectureBlock(lectureBlock);
		lectureBlock.setComment(blokcCommentEl.getValue());
		lectureBlock.setStatus(LectureBlockStatus.valueOf(statusEl.getSelectedKey()));
		lectureBlock.setRollCallStatus(LectureRollCallStatus.valueOf(rollCallStatusEl.getSelectedKey()));
		Date effectiveEndDate = getEffectiveEndDate();
		if(effectiveEndDate == null) {
			lectureBlock.setReasonEffectiveEnd(null);
		} else {
			lectureBlock.setEffectiveEndDate(effectiveEndDate);
			if("-".equals(effectiveEndReasonEl.getSelectedKey())) {
				lectureBlock.setReasonEffectiveEnd(null);
			} else {
				Long reasonKey = new Long(effectiveEndReasonEl.getSelectedKey());
				Reason selectedReason = lectureService.getReason(reasonKey);
				lectureBlock.setReasonEffectiveEnd(selectedReason);
			}
		}

		lectureBlock = lectureService.save(lectureBlock, null);
		lectureService.recalculateSummary(lectureBlock.getEntry());
		fireEvent(ureq, Event.DONE_EVENT);
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
	
	private void doCheckAllRow(TeacherRollCallRow row) {
		List<Integer> allIndex = new ArrayList<>(lectureBlock.getPlannedLecturesNumber());
		for(int i=0; i<lectureBlock.getPlannedLecturesNumber(); i++) {
			allIndex.add(i);
		}
		LectureBlockRollCall rollCall = lectureService.addRollCall(row.getIdentity(), lectureBlock, row.getRollCall(), null, allIndex);
		for(MultipleSelectionElement check:row.getChecks()) {
			check.select(onKeys[0], true);
		}
		row.setRollCall(rollCall);
		tableEl.reloadData();
		flc.setDirty(true);
	}
	
	private void doCheckRow(TeacherRollCallRow row, MultipleSelectionElement check) {
		int index = row.getIndexOfCheck(check);
		List<Integer> indexList = Collections.singletonList(index);
		
		LectureBlockRollCall rollCall;
		if(check.isAtLeastSelected(1)) {
			rollCall = lectureService.addRollCall(row.getIdentity(), lectureBlock, row.getRollCall(), indexList);
		} else {
			rollCall = lectureService.removeRollCall(row.getIdentity(), lectureBlock, row.getRollCall(), indexList);
		}
		row.setRollCall(rollCall);	
	}
	
	private void doAuthorizedAbsence(TeacherRollCallRow row, MultipleSelectionElement check) {
		LectureBlockRollCall rollCall = row.getRollCall();
		boolean authorized = check.isAtLeastSelected(1);
		if(rollCall == null) {
			rollCall = lectureService.getOrCreateRollCall(row.getIdentity(), lectureBlock, authorized, null);		
		} else {
			rollCall.setAbsenceAuthorized(authorized);
			rollCall = lectureService.updateRollCall(rollCall);
		}
		row.getReasonLink().setVisible(authorized);
		row.getAuthorizedAbsenceCont().setDirty(true);
		row.getAuthorizedAbsence().clearError();
		row.setRollCall(rollCall);
	}
	
	private void doCalloutReasonAbsence(UserRequest ureq, FormLink link, TeacherRollCallRow row) {
		boolean canEdit = secCallback.canEdit() && secCallback.canEditAuthorizedAbsences();
		reasonCtrl = new ReasonController(ureq, getWindowControl(), row, canEdit);
		listenTo(reasonCtrl);

		reasonCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				reasonCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(reasonCalloutCtrl);
		reasonCalloutCtrl.activate();
	}
	
	private void doReason(TeacherRollCallRow row, String reason) {
		LectureBlockRollCall rollCall = row.getRollCall();
		if(rollCall == null) {
			row.getAuthorizedAbsence().select(onKeys[0], true);
			rollCall = lectureService.getOrCreateRollCall(row.getIdentity(), lectureBlock, true, reason);		
		} else {
			rollCall.setAbsenceReason(reason);
			rollCall = lectureService.updateRollCall(rollCall);
		}
		row.setRollCall(rollCall);
	}
}
