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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.UserShortDescription;
import org.olat.admin.user.UserShortDescription.Builder;
import org.olat.admin.user.UserShortDescription.Rows;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.ImmunityProofService;
import org.olat.modules.immunityproof.ImmunityProofModule.ImmunityProofLevel;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.model.RollCallSecurityCallbackImpl;
import org.olat.modules.lecture.ui.SingleParticipantRollCallsDataModel.RollCallsCols;
import org.olat.modules.lecture.ui.coach.AbsenceNoticeDetailsCalloutController;
import org.olat.modules.lecture.ui.component.ImmunityProofLevelCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockRollCallStatusItem;
import org.olat.modules.lecture.ui.component.LectureBlockTimesCellRenderer;
import org.olat.user.DisplayPortraitController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SingleParticipantRollCallsController extends FormBasicController {

	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };
	
	public static final int CHECKBOX_OFFSET = 1000;
	
	private FlexiTableElement tableEl;
	private SingleParticipantRollCallsDataModel tableModel;
	
	private int counter = 0;
	private final int maxNumOfLectures;
	private final boolean hasCompulsory;
	private final Identity calledIdentity;
	private List<LectureBlockRollCall> rollCalls;
	private final List<LectureBlock> lectureBlocks;
	private final boolean authorizedAbsenceEnabled;
	private final boolean absenceDefaultAuthorized;
	private final Map<LectureBlock,RollCallSecurityCallback> secCallbacks;
	
	private ReasonController reasonCtrl;
	private CloseableCalloutWindowController noticeCalloutCtrl;
	private CloseableCalloutWindowController reasonCalloutCtrl;
	private AbsenceNoticeDetailsCalloutController noticeDetailsCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private ImmunityProofModule immunityProofModule;
	@Autowired
	private ImmunityProofService immunityProofService;
	
	public SingleParticipantRollCallsController(UserRequest ureq, WindowControl wControl, Identity calledIdentity,
			List<LectureBlock> lectureBlocks, List<LectureBlockRollCall> rollCalls) {
		super(ureq, wControl, "calls");
		this.calledIdentity = calledIdentity;
		this.lectureBlocks = new ArrayList<>(lectureBlocks);
		this.rollCalls = new ArrayList<>(rollCalls);
		
		hasCompulsory = lectureBlocks.stream()
				.anyMatch(LectureBlock::isCompulsory);
		maxNumOfLectures = lectureBlocks.stream()
				.mapToInt(LectureBlock::getCalculatedLecturesNumber)
				.max().orElse(0);
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		secCallbacks = calculateSecurityCallback();
		initForm(ureq);
		loadModel();
	}
	
	private Map<LectureBlock,RollCallSecurityCallback> calculateSecurityCallback() {
		Map<LectureBlock,RollCallSecurityCallback> secCallbackMap = new HashMap<>();
		for(LectureBlock lectureBlock:lectureBlocks) {
			boolean entryAdmin = false; // TODO absences
			boolean masterCoach = false;
			boolean teacher = true;
			RollCallSecurityCallback secCallback = new RollCallSecurityCallbackImpl(entryAdmin, masterCoach, teacher, lectureBlock, lectureModule);
			secCallbackMap.put(lectureBlock, secCallback);
		}
		return secCallbackMap;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// identity screen and title
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("date", getDate());
			
			DisplayPortraitController portraitCtr = new DisplayPortraitController(ureq, getWindowControl(), calledIdentity, true, false);
			listenTo(portraitCtr);
			layoutCont.getFormItemComponent().put("portrait", portraitCtr.getInitialComponent());
			
			Rows immunoRow = getImmunoRow(ureq);
			UserShortDescription userDescr = new UserShortDescription(ureq, getWindowControl(), calledIdentity, immunoRow);
			listenTo(userDescr);
			layoutCont.getFormItemComponent().put("userDescr", userDescr.getInitialComponent());
		}
		
		// roll call table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCallsCols.entry));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCallsCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCallsCols.lecturesBlock));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCallsCols.times, new LectureBlockTimesCellRenderer(true, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCallsCols.teacher, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		
		boolean canViewAuthorizedAbsences = secCallbacks.values()
				.stream().anyMatch(RollCallSecurityCallback::canViewAuthorizedAbsences);
		boolean canEditAuthorizedAbsences = secCallbacks.values()
				.stream().anyMatch(RollCallSecurityCallback::canEditAuthorizedAbsences);
		
		if(hasCompulsory) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCallsCols.status));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCallsCols.numOfAbsences));
			
			for(int i=0; i<maxNumOfLectures; i++) {
				DefaultFlexiColumnModel col = new DefaultFlexiColumnModel("table.header.lecture." + (i+1), i + CHECKBOX_OFFSET, true, "lecture." + (i+1));
				col.setAlwaysVisible(true);
				columnsModel.addFlexiColumnModel(col);
			}
			
			//all button
			DefaultFlexiColumnModel allCol = new DefaultFlexiColumnModel(RollCallsCols.all);
			allCol.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(allCol);
			if(canViewAuthorizedAbsences || canEditAuthorizedAbsences) {
				DefaultFlexiColumnModel authorizedCol = new DefaultFlexiColumnModel(RollCallsCols.authorizedAbsence);
				authorizedCol.setAlwaysVisible(true);
				columnsModel.addFlexiColumnModel(authorizedCol);
			}
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCallsCols.comment));
		
		tableModel = new SingleParticipantRollCallsDataModel(columnsModel, userManager);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", formLayout);
	}
	
	private String getDate() {
		Date startDate = lectureBlocks.get(0).getStartDate();
		Formatter formatter = Formatter.getInstance(getLocale());
		String date = formatter.formatDate(startDate);
		String startDayOfWeek = formatter.dayOfWeek(startDate);
		
		String[] args = new String[] {
				date,						// 0
				startDayOfWeek				// 1
		};
		return translate("lecture.block.date", args);
	}
	
	private Rows getImmunoRow(UserRequest ureq) {
		Builder rowsBuilder = Rows.builder();
		if (immunityProofModule.isEnabled()) {
			ImmunityProofLevel proofLevel = null;
			for(LectureBlock lectureBlock:lectureBlocks) {
				if(CalendarUtils.isSameDay(lectureBlock.getStartDate(), ureq.getRequestTimestamp())) {
					ImmunityProof proof = immunityProofService.getImmunityProof(calledIdentity);
					proofLevel = immunityProofService.getImmunityProofLevel(proof);
					break;
				}
			}
			if(proofLevel != null) {
				rowsBuilder.addRowBefore(translate("immuno.status"), ImmunityProofLevelCellRenderer.renderImmunityProofLevel(proofLevel, getTranslator()));
			}
		}
		return rowsBuilder.build();
	}
	
	private void loadModel() {
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setLectureBlocks(lectureBlocks);
		List<LectureBlockWithTeachers> lectureBlocksWithTeachers = lectureService.getLectureBlocksWithTeachers(searchParams);
		
		
		List<SingleParticipantRollCallRow> rows = new ArrayList<>(lectureBlocks.size());
		for(LectureBlock lectureBlock:lectureBlocks) {
			LectureBlockRollCall lectureCall = null;
			for(LectureBlockRollCall call:rollCalls) {
				if(call.getLectureBlock().equals(lectureBlock)) {
					lectureCall = call;
				}
			}
			
			List<Identity> teachers = null;
			for(LectureBlockWithTeachers lectureBlockWithTeachers:lectureBlocksWithTeachers) {
				if(lectureBlockWithTeachers.getLectureBlock().equals(lectureBlock)) {
					teachers = lectureBlockWithTeachers.getTeachers();
				}
			}

			AbsenceNotice notice = null;
			if(lectureCall != null) {
				notice = lectureCall.getAbsenceNotice();
			} else {
				notice = lectureService.getAbsenceNotice(calledIdentity, lectureBlock);
			}
			RollCallSecurityCallback secCallback = secCallbacks.get(lectureBlock);
			rows.add(forgeRow(lectureBlock, lectureCall, notice, teachers, secCallback));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private SingleParticipantRollCallRow forgeRow(LectureBlock lectureBlock, LectureBlockRollCall rollCall,
			AbsenceNotice notice, List<Identity> teachers, RollCallSecurityCallback secCallback) {
		int numOfLectures = lectureBlock.getCalculatedLecturesNumber();
		SingleParticipantRollCallRow row = new SingleParticipantRollCallRow(lectureBlock, notice, numOfLectures, teachers);
		
		int numOfChecks = lectureBlock.isCompulsory() ? numOfLectures : 0;
		MultipleSelectionElement[] checks = new MultipleSelectionElement[numOfChecks];
		List<Integer> absences = rollCall == null ? Collections.emptyList() : rollCall.getLecturesAbsentList();
		int numOfAbsences = 0;
		
		for(int i=0; i<numOfChecks; i++) {
			String checkId = "check_".concat(Integer.toString(++counter));
			MultipleSelectionElement check = uifactory.addCheckboxesHorizontal(checkId, null, flc, onKeys, onValues);
			check.setDomReplacementWrapperRequired(false);
			check.addActionListener(FormEvent.ONCHANGE);
			check.setEnabled(secCallback.canEditAbsences() && notice == null);
			check.setUserObject(row);
			check.setAjaxOnly(true);
			if(absences.contains(i) || notice != null) {
				check.select(onKeys[0], true);
				numOfAbsences++;
			}
			checks[i] = check;
			flc.add(check);
		}
		row.setChecks(checks);

		LectureBlockRollCallStatusItem statusEl = new LectureBlockRollCallStatusItem("status_".concat(Integer.toString(++counter)),
				row, authorizedAbsenceEnabled, absenceDefaultAuthorized, getTranslator());
		row.setRollCallStatusEl(statusEl);
		
		StaticTextElement numOfAbsencesEl = uifactory.addStaticTextElement("num_of_ab_".concat(Integer.toString(++counter)), null, Integer.toString(numOfAbsences), flc);
		numOfAbsencesEl.setDomWrapperElement(DomWrapperElement.span);
		row.setNumOfAbsencesEl(numOfAbsencesEl);
		
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
			authorizedAbsencedEl.setEnabled(secCallback.canEdit() && secCallback.canEditAuthorizedAbsences() && notice == null);
			
			boolean hasAuthorization = rollCall != null && rollCall.getAbsenceAuthorized() != null
					&& rollCall.getAbsenceAuthorized().booleanValue();
			if(hasAuthorization) {
				authorizedAbsencedEl.select(onKeys[0], true);
			}
			row.setAuthorizedAbsence(authorizedAbsencedEl);
			flc.add(authorizedAbsencedEl);
			
			if(notice != null) {
				String noticeId = "notice_".concat(Integer.toString(++counter));
				FormLink noticeLink = uifactory.addFormLink(noticeId, "", null, absenceCont, Link.LINK | Link.NONTRANSLATED);
				noticeLink.setTitle(translate("reason"));
				noticeLink.setDomReplacementWrapperRequired(false);
				noticeLink.setIconLeftCSS("o_icon o_icon_info");
				noticeLink.setUserObject(row);
				row.setNoticeLink(noticeLink);
			} else {
				String reasonId = "abs_reason_".concat(Integer.toString(++counter));
				FormLink reasonLink = uifactory.addFormLink(reasonId, "", null, absenceCont, Link.BUTTON_XSMALL | Link.NONTRANSLATED);
				reasonLink.setTitle(translate("reason"));
				reasonLink.setDomReplacementWrapperRequired(false);
				reasonLink.setIconLeftCSS("o_icon o_icon_notes");
				reasonLink.setVisible(hasAuthorization);
				reasonLink.setUserObject(row);
				row.setReasonLink(reasonLink);
			}
			
			row.setAuthorizedAbsenceCont(absenceCont);
			absenceCont.contextPut("row", row);
		}
		
		if(secCallback.canEditAbsences() && notice == null) {
			FormLink allLink = uifactory.addFormLink("all_".concat(Integer.toString(++counter)), "all", null, flc, Link.LINK);
			allLink.setTitle("all.desc");
			allLink.setDomReplacementWrapperRequired(false);
			allLink.setUserObject(row);
			row.setAllLink(allLink);
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
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		for(int i=tableModel.getRowCount(); i-->0; ) {
			SingleParticipantRollCallRow row = tableModel.getObject(i);
			
			if(row.getAuthorizedAbsence() != null) {
				row.getAuthorizedAbsence().clearError();
			}
			
			if(row.getRollCall() == null) {
				//??? stop?
			} else if(!absenceDefaultAuthorized) {
				String reason = row.getRollCall().getAbsenceReason();
				if(row.getAbsenceNotice() == null && row.getAuthorizedAbsence() != null
						&& row.getAuthorizedAbsence().isAtLeastSelected(1) && !StringHelper.containsNonWhitespace(reason)) {
					row.getAuthorizedAbsence().setErrorKey("error.reason.mandatory", null);
					allOk &= false;
				}
			}
		}

		return allOk;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(reasonCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doReason((SingleParticipantRollCallRow)reasonCtrl.getRollCallRow(), reasonCtrl.getReason(), reasonCtrl.getAbsenceCategory());
			}
			reasonCalloutCtrl.deactivate();
			cleanUp();
		} else if(reasonCalloutCtrl == source || noticeCalloutCtrl == null) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(reasonCalloutCtrl);
		removeAsListenerAndDispose(noticeCalloutCtrl);
		removeAsListenerAndDispose(noticeDetailsCtrl);
		removeAsListenerAndDispose(reasonCtrl);
		reasonCalloutCtrl = null;
		noticeCalloutCtrl = null;
		noticeDetailsCtrl = null;
		reasonCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof MultipleSelectionElement) {
			MultipleSelectionElement check = (MultipleSelectionElement)source;
			SingleParticipantRollCallRow row = (SingleParticipantRollCallRow)check.getUserObject();
			if(row.getAuthorizedAbsence() == check) {
				doAuthorizedAbsence(row, check);
				if(check.isAtLeastSelected(1)) {
					doCalloutReasonAbsence(ureq, check.getFormDispatchId() + "_C_0", row);
				}
			} else {
				doCheckRow(row, check);
				recalculateNumOfAbsences(row);
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if(cmd != null) {
				if(cmd.startsWith("abs_reason_")) {
					SingleParticipantRollCallRow row = (SingleParticipantRollCallRow)link.getUserObject();
					doCalloutReasonAbsence(ureq, link.getFormDispatchId(), row);
				} else if(cmd.startsWith("all_")) {
					SingleParticipantRollCallRow row = (SingleParticipantRollCallRow)link.getUserObject();
					doCheckAllRow(row);
					recalculateNumOfAbsences(row);
				} else if(cmd.startsWith("notice_")) {
					doCalloutAbsenceNotice(ureq, link.getFormDispatchId(), (SingleParticipantRollCallRow)link.getUserObject());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		saveLectureBlockRollCalls();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void recalculateNumOfAbsences(SingleParticipantRollCallRow row) {
		if(row.getNumOfAbsencesEl() == null) return;
		
		MultipleSelectionElement[] checks = row.getChecks();
		int numOfAbsences = 0;
		if(checks != null) {
			for(MultipleSelectionElement check:checks) {
				if(check.isAtLeastSelected(1)) {
					numOfAbsences++;
				}
			}
		}
		row.getNumOfAbsencesEl().setValue(Integer.toString(numOfAbsences));
	}
	
	private void doCalloutAbsenceNotice(UserRequest ureq, String elementId, SingleParticipantRollCallRow row) {
		noticeDetailsCtrl = new AbsenceNoticeDetailsCalloutController(ureq, getWindowControl(), row.getAbsenceNotice());
		listenTo(noticeDetailsCtrl);

		noticeCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				noticeDetailsCtrl.getInitialComponent(), elementId, "", true, "");
		listenTo(noticeCalloutCtrl);
		noticeCalloutCtrl.activate();
	}
	
	private void doReason(SingleParticipantRollCallRow row, String reason, AbsenceCategory category) {
		LectureBlockRollCall rollCall = row.getRollCall();
		String before = lectureService.toAuditXml(rollCall);
		if(rollCall == null) {
			row.getAuthorizedAbsence().select(onKeys[0], true);
			rollCall = lectureService.getOrCreateRollCall(calledIdentity, row.getLectureBlock(), true, reason, category);
			lectureService.auditLog(LectureBlockAuditLog.Action.createRollCall, before, lectureService.toAuditXml(rollCall),
					reason, row.getLectureBlock(), rollCall, row.getLectureBlock().getEntry(), calledIdentity, getIdentity());
		} else {
			rollCall.setAbsenceReason(reason);
			rollCall.setAbsenceCategory(category);
			rollCall = lectureService.updateRollCall(rollCall);
			lectureService.auditLog(LectureBlockAuditLog.Action.updateRollCall, before, lectureService.toAuditXml(rollCall),
					reason, row.getLectureBlock(), rollCall, row.getLectureBlock().getEntry(), calledIdentity, getIdentity());
		}
		row.setRollCall(rollCall);
	}
	
	private void doCheckAllRow(SingleParticipantRollCallRow row) {
		int numOfLectures = row.getNumOfLectures();
		List<Integer> allAbsences = new ArrayList<>(numOfLectures);
		for(int i=0; i<numOfLectures; i++) {
			allAbsences.add(i);
		}
		LectureBlockRollCall rollCall = lectureService.addRollCall(calledIdentity, row.getLectureBlock(), row.getRollCall(), null, allAbsences);
		for(MultipleSelectionElement check:row.getChecks()) {
			check.select(onKeys[0], true);
		}
		row.setRollCall(rollCall);
		if(authorizedAbsenceEnabled) {
			if(rollCall.getAbsenceAuthorized() != null && rollCall.getAbsenceAuthorized().booleanValue()) {
				row.getAuthorizedAbsence().select(onKeys[0], true);
			} else {
				row.getAuthorizedAbsence().uncheckAll();
			}
			row.getAuthorizedAbsenceCont().setDirty(true);
		}
		row.getRollCallStatusEl().getComponent().setDirty(true);
		tableEl.reloadData();
		flc.setDirty(true);
	}
	
	private void doCheckRow(SingleParticipantRollCallRow row, MultipleSelectionElement check) {
		int index = row.getIndexOfCheck(check);
		LectureBlock lectureBlock = row.getLectureBlock();
		List<Integer> indexList = Collections.singletonList(index);
		
		LectureBlockRollCall rollCall;
		String before = lectureService.toAuditXml(row.getRollCall());
		if(check.isAtLeastSelected(1)) {
			rollCall = lectureService.addRollCall(calledIdentity, lectureBlock, row.getRollCall(), indexList);
			lectureService.auditLog(LectureBlockAuditLog.Action.addToRollCall, before, lectureService.toAuditXml(rollCall),
					Integer.toString(index), lectureBlock, rollCall, lectureBlock.getEntry(), calledIdentity, getIdentity());
		} else {
			rollCall = lectureService.removeRollCall(calledIdentity, lectureBlock, row.getRollCall(), indexList);
			lectureService.auditLog(LectureBlockAuditLog.Action.removeFromRollCall, before, lectureService.toAuditXml(rollCall),
					Integer.toString(index), lectureBlock, rollCall, lectureBlock.getEntry(), calledIdentity, getIdentity());
		}
		row.setRollCall(rollCall);
		if(authorizedAbsenceEnabled && row.getAuthorizedAbsence() != null) {
			if(rollCall.getAbsenceAuthorized() != null && rollCall.getAbsenceAuthorized().booleanValue()) {
				row.getAuthorizedAbsence().select(onKeys[0], true);
			} else {
				row.getAuthorizedAbsence().uncheckAll();
			}
			row.getAuthorizedAbsenceCont().setDirty(true);
		}
		row.getRollCallStatusEl().getComponent().setDirty(true);
	}
	
	private void doAuthorizedAbsence(SingleParticipantRollCallRow row, MultipleSelectionElement check) {
		LectureBlockRollCall rollCall = row.getRollCall();
		LectureBlock lectureBlock = row.getLectureBlock();
		boolean authorized = check.isAtLeastSelected(1);
		
		if(rollCall == null) {
			rollCall = lectureService.getOrCreateRollCall(calledIdentity, lectureBlock, authorized, null, null);
			lectureService.auditLog(LectureBlockAuditLog.Action.createRollCall, null, lectureService.toAuditXml(rollCall),
					authorized ? "true" : "false", lectureBlock, rollCall, lectureBlock.getEntry(), calledIdentity, getIdentity());
		} else {
			String before = lectureService.toAuditXml(rollCall);
			rollCall.setAbsenceAuthorized(authorized);
			rollCall = lectureService.updateRollCall(rollCall);
			lectureService.auditLog(LectureBlockAuditLog.Action.updateAuthorizedAbsence, before, lectureService.toAuditXml(rollCall),
					authorized ? "true" : "false", lectureBlock, rollCall, lectureBlock.getEntry(), calledIdentity, getIdentity());
		}

		row.getReasonLink().setVisible(authorized);
		row.getAuthorizedAbsenceCont().setDirty(true);
		row.getAuthorizedAbsence().clearError();
		row.setRollCall(rollCall);
		row.getRollCallStatusEl().getComponent().setDirty(true);
	}
	
	private void doCalloutReasonAbsence(UserRequest ureq, String elementId, SingleParticipantRollCallRow row) {
		RollCallSecurityCallback secCallback = secCallbacks.get(row.getLectureBlock());
		boolean canEdit = secCallback.canEdit() && secCallback.canEditAuthorizedAbsences();
		reasonCtrl = new ReasonController(ureq, getWindowControl(), row, canEdit);
		listenTo(reasonCtrl);

		reasonCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				reasonCtrl.getInitialComponent(), elementId, "", true, "");
		listenTo(reasonCalloutCtrl);
		reasonCalloutCtrl.activate();
	}
	
	private void saveLectureBlockRollCalls() {
		for(int i=tableModel.getRowCount(); i-->0; ) {
			SingleParticipantRollCallRow row = tableModel.getObject(i);
			
			int numOfChecks = row.getChecks().length;
			List<Integer> absenceList = new ArrayList<>(numOfChecks);
			for(int j=0; j<numOfChecks; j++) {
				if(row.getCheck(j).isAtLeastSelected(1)) {
					absenceList.add(j);
				}
			}
			
			String comment = row.getCommentEl().getValue();
			LectureBlockRollCall rollCall = lectureService.addRollCall(calledIdentity, row.getLectureBlock(), row.getRollCall(),
					comment, absenceList);
			row.setRollCall(rollCall);
		}
	}
}
