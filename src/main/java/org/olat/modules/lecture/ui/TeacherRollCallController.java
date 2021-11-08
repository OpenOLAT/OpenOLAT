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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.ImmunityProofModule.ImmunityProofLevel;
import org.olat.modules.immunityproof.ImmunityProofService;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.ui.TeacherRollCallDataModel.RollCols;
import org.olat.modules.lecture.ui.coach.AbsenceNoticeDetailsCalloutController;
import org.olat.modules.lecture.ui.component.ImmunityProofLevelCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockRollCallStatusItem;
import org.olat.modules.lecture.ui.event.ReopenLectureBlockEvent;
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
	private FormSubmit quickSaveButton;
	private FormLink backLink;
	private FormLink reopenButton;
	private FormLink expandButton;
	private FormLink cancelLectureBlockButton;
	private FormLink closeLectureBlocksButton;
	
	private ReasonController reasonCtrl;
	private CloseableModalController cmc;
	private CloseableCalloutWindowController noticeCalloutCtrl;
	private CloseableCalloutWindowController reasonCalloutCtrl;
	private CloseRollCallConfirmationController closeRollCallCtrl;
	private CancelRollCallConfirmationController cancelRollCallCtrl;
	private AbsenceNoticeDetailsCalloutController noticeDetailsCtrl;
	
	private int counter = 0;
	private final boolean withBack;
	private LectureBlock lectureBlock;
	private List<UserPropertyHandler> userPropertyHandlers;
	private RollCallSecurityCallback secCallback;
	private final boolean authorizedAbsenceEnabled;
	private final boolean absenceDefaultAuthorized;
	
	private int numOfLectures;
	private List<Identity> participants;
	private boolean immunoStatusEnabled;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private ImmunityProofModule immunityProofModule;
	@Autowired
	private ImmunityProofService immunityProofService;
	
	public TeacherRollCallController(UserRequest ureq, WindowControl wControl,
			LectureBlock block, List<Identity> participants, RollCallSecurityCallback secCallback, boolean withBack) {
		super(ureq, wControl, "rollcall");
		
		this.lectureBlock = block;
		this.secCallback = secCallback;
		this.participants = participants;
		this.withBack = withBack;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		immunoStatusEnabled = immunityProofModule.isEnabled() && block.isRunningAt(ureq.getRequestTimestamp());
		
		numOfLectures = lectureBlock.getCalculatedLecturesNumber();

		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);

		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		
		initForm(ureq);
		loadModel();
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			StringBuilder sb = new StringBuilder();
			List<Identity> teachers = lectureService.getTeachers(lectureBlock);
			for(Identity teacher:teachers) {
				sb.append("<span><i class='o_icon o_icon-fw o_icon_user'> </i> ")
				  .append(StringHelper.escapeHtml(userManager.getUserDisplayName(teacher)))
				  .append("</span> ");
			}
			
			Formatter formatter = Formatter.getInstance(getLocale());
			String date = formatter.formatDate(lectureBlock.getStartDate());
			String startTime = formatter.formatTimeShort(lectureBlock.getStartDate());
			String endTime = formatter.formatTimeShort(lectureBlock.getEndDate());
			String startDayOfWeek = formatter.dayOfWeek(lectureBlock.getStartDate());
			
			String[] args = new String[] {
					lectureBlock.getTitle(),	// 0	
					sb.toString(),				// 1
					date,						// 2
					startTime,					// 3
					endTime,					// 4
					startDayOfWeek				// 5
			};
		
			layoutCont.contextPut("date", date);
			layoutCont.contextPut("startTime", startTime);
			layoutCont.contextPut("endTime", endTime);
			layoutCont.contextPut("dateAndTime", translate("lecture.block.dateAndTime", args));
			layoutCont.contextPut("teachers", sb.toString());
			String numOfLecturesI18n = lectureBlock.getPlannedLecturesNumber() > 1 ? "rollcall.lectures" : "rollcall.lecture";
			layoutCont.contextPut("numOfLectures", translate(numOfLecturesI18n,
					new String[] { Integer.toString(lectureBlock.getPlannedLecturesNumber()) }));
			layoutCont.contextPut("lectureBlockTitle", StringHelper.escapeHtml(lectureBlock.getTitle()));
			layoutCont.contextPut("lectureBlockExternalId", StringHelper.escapeHtml(lectureBlock.getExternalId()));
			StringBuilder description = Formatter.stripTabsAndReturns(Formatter.formatURLsAsLinks(lectureBlock.getDescription(), true));
			layoutCont.contextPut("lectureBlockDescription", StringHelper.xssScan(description));
			StringBuilder preparation = Formatter.stripTabsAndReturns(Formatter.formatURLsAsLinks(lectureBlock.getPreparation(), true));
			layoutCont.contextPut("lectureBlockPreparation", StringHelper.xssScan(preparation));
			layoutCont.contextPut("lectureBlockLocation", StringHelper.escapeHtml(lectureBlock.getLocation()));
			layoutCont.contextPut("lectureBlock",lectureBlock);
			layoutCont.contextPut("lectureBlockOptional", !lectureBlock.isCompulsory());
			layoutCont.setFormTitle(translate("lecture.block", args));
			layoutCont.setFormDescription(StringHelper.escapeJavaScript(lectureBlock.getDescription()));
			String i18nInfos = lectureBlock.getPlannedLecturesNumber() == 1 ? "rollcall.coach.hint" : "rollcall.coach.hints";
			layoutCont.contextPut("lecturesInfos", translate(i18nInfos, new String[] { Integer.toString(lectureBlock.getPlannedLecturesNumber())}));
		}
		
		expandButton = uifactory.addFormLink("expandButton", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		expandButton.setIconLeftCSS("o_icon o_icon_lg o_icon_details_collaps");
		expandButton.setElementCssClass("o_button_details");
		expandButton.setUserObject(Boolean.TRUE);
		
		if(withBack) {
			backLink = uifactory.addFormLink("back", formLayout, Link.LINK_BACK);
		}
		
		// table
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		if(immunoStatusEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCols.immunoStatus,
					new ImmunityProofLevelCellRenderer(getTranslator())));
		}

		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			columnsModel.addFlexiColumnModel(col);
			colPos++;
			
			if(!options.hasDefaultOrderBy() || UserConstants.LASTNAME.equals(propName)) {
				options.setDefaultOrderBy(new SortKey(propName, true));
			}
		}
		
		if(lectureBlock.isCompulsory()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCols.status));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCols.numOfAbsences));
			
			for(int i=0; i<numOfLectures; i++) {
				String lecture = Integer.toString(i + 1);
				DefaultFlexiColumnModel col = new DefaultFlexiColumnModel("table.header.lecture.".concat(lecture), i + CHECKBOX_OFFSET, true, "lecture.".concat(lecture));
				col.setHeaderLabel(translate("table.header.lecture.number", new String[] { lecture }));
				col.setHeaderTooltip(translate("table.header.lecture.number.tooltip", new String[] { lecture }));
				col.setAlwaysVisible(true);
				columnsModel.addFlexiColumnModel(col);
			}
			
			//all button
			DefaultFlexiColumnModel allCol = new DefaultFlexiColumnModel(RollCols.all);
			allCol.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(allCol);
			if(secCallback.canViewAuthorizedAbsences() || secCallback.canEditAuthorizedAbsences()) {
				DefaultFlexiColumnModel authorizedCol = new DefaultFlexiColumnModel(RollCols.authorizedAbsence);
				authorizedCol.setAlwaysVisible(true);
				columnsModel.addFlexiColumnModel(authorizedCol);
			}
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCols.comment));

		tableModel = new TeacherRollCallDataModel(columnsModel, secCallback, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setSortSettings(options);
		tableEl.setAndLoadPersistedPreferences(ureq, "teacher-roll-call-v2");
		
		//buttons
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		quickSaveButton = uifactory.addFormSubmitButton("save", "save.temporary", formLayout);
		quickSaveButton.setElementCssClass("o_sel_lecture_quick_save");
		closeLectureBlocksButton = uifactory.addFormLink("close.lecture.blocks", formLayout, Link.BUTTON);
		closeLectureBlocksButton.setElementCssClass("o_sel_lecture_close");
		if(lectureModule.isStatusCancelledEnabled()) {
			cancelLectureBlockButton = uifactory.addFormLink("cancel.lecture.blocks", formLayout, Link.BUTTON);
		}
		reopenButton = uifactory.addFormLink("reopen.lecture.blocks", formLayout, Link.BUTTON);
		reopenButton.setElementCssClass("o_sel_lecture_reopen");
		updateUI();
	}
	
	private void updateUI() {
		quickSaveButton.setVisible(secCallback.canEdit());
		closeLectureBlocksButton.setVisible(secCallback.canEdit());
		if(cancelLectureBlockButton != null) {
			cancelLectureBlockButton.setVisible(secCallback.canEdit());
		}
		reopenButton.setVisible(secCallback.canReopen());
		
		List<TeacherRollCallRow> rows = tableModel.getObjects();
		if(rows != null) {
			for(TeacherRollCallRow row:rows) {
				MultipleSelectionElement[] checks = row.getChecks();
				if(checks != null) {
					for(MultipleSelectionElement check:checks) {
						check.setEnabled(secCallback.canEditAbsences());
						check.getComponent().setDirty(true);
					}
				}
				row.getCommentEl().setEnabled(secCallback.canEdit());
				
				if(row.getAuthorizedAbsence() != null) {
					row.getAuthorizedAbsence().setEnabled(secCallback.canEdit() && secCallback.canEditAuthorizedAbsences());
				}
			}
		}
		
		tableEl.reset(false, false, true);
	}

	private void loadModel() {
		List<AbsenceNotice> notices = lectureService.getAbsenceNoticeRelatedTo(lectureBlock);

		List<LectureBlockRollCall> rollCalls = lectureService.getRollCalls(lectureBlock);
		Map<Identity,LectureBlockRollCall> rollCallMap = new HashMap<>();
		for(LectureBlockRollCall rollCall:rollCalls) {
			rollCallMap.put(rollCall.getIdentity(), rollCall);
		}

		Map<Long,ImmunityProofLevel> immunoStatusMap = getImmunoLevels();
		List<TeacherRollCallRow> rows = new ArrayList<>(participants.size());
		for(Identity participant:participants) {
			AbsenceNotice notice = notices.stream()
					.filter(n -> n.getIdentity().equals(participant))
					.findFirst().orElse(null);
			LectureBlockRollCall rollCall = rollCallMap.get(participant);
			ImmunityProofLevel immunoStatus = immunoStatusMap.get(participant.getKey());
			rows.add(forgeRow(participant, rollCall, notice, immunoStatus));
		}
		tableModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}
	
	private Map<Long,ImmunityProofLevel> getImmunoLevels() {
		Map<Long, ImmunityProofLevel> immunoStatusMap;
		if(immunoStatusEnabled) {
			List<ImmunityProof> proofs = immunityProofService.getImmunityProofs(participants);
			Map<Long,ImmunityProof> identityKeyToProof = proofs.stream()
					.collect(Collectors.toMap(p -> p.getIdentity().getKey(), p -> p, (u, v) -> u));
			
			immunoStatusMap = new HashMap<>();
			for(Identity participant:participants) {
				ImmunityProof proof = identityKeyToProof.get(participant.getKey());
				ImmunityProofLevel level = immunityProofService.getImmunityProofLevel(proof);
				immunoStatusMap.put(participant.getKey(), level);	
			}	
		} else {
			immunoStatusMap = Map.of();
		}
		return immunoStatusMap;
	}
	
	private TeacherRollCallRow forgeRow(Identity participant, LectureBlockRollCall rollCall, AbsenceNotice notice, ImmunityProofLevel immunoStatus) {
		TeacherRollCallRow row = new TeacherRollCallRow(rollCall, participant, notice, immunoStatus, userPropertyHandlers, getLocale());
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
		
		StaticTextElement numOfAbsencesEl = uifactory.addStaticTextElement("num_of_ab_" + (++counter), null, Integer.toString(numOfAbsences), flc);
		numOfAbsencesEl.setDomWrapperElement(DomWrapperElement.span);
		row.setNumOfAbsencesEl(numOfAbsencesEl);
		
		LectureBlockRollCallStatusItem statusEl = new LectureBlockRollCallStatusItem("status_".concat(Integer.toString(++counter)),
				row, authorizedAbsenceEnabled, absenceDefaultAuthorized, getTranslator());
		row.setRollCallStatusEl(statusEl);
		
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
			if(hasAuthorization || notice != null) {
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
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(reasonCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doReason((TeacherRollCallRow)reasonCtrl.getRollCallRow(), reasonCtrl.getReason(), reasonCtrl.getAbsenceCategory());
			}
			reasonCalloutCtrl.deactivate();
			cleanUp();
		} else if(noticeDetailsCtrl == source) {
			noticeCalloutCtrl.deactivate();
			cleanUp();
		} else if(reasonCalloutCtrl == source || noticeCalloutCtrl == source) {
			cleanUp();
		} else if(closeRollCallCtrl == source) {
			lectureBlock = closeRollCallCtrl.getLectureBLock();
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(cancelRollCallCtrl == source) {
			lectureBlock = cancelRollCallCtrl.getLectureBlock();
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cancelRollCallCtrl);
		removeAsListenerAndDispose(reasonCalloutCtrl);
		removeAsListenerAndDispose(closeRollCallCtrl);
		removeAsListenerAndDispose(noticeCalloutCtrl);
		removeAsListenerAndDispose(noticeDetailsCtrl);
		removeAsListenerAndDispose(reasonCtrl);
		removeAsListenerAndDispose(cmc);
		cancelRollCallCtrl = null;
		reasonCalloutCtrl = null;
		closeRollCallCtrl = null;
		noticeCalloutCtrl = null;
		noticeDetailsCtrl = null;
		reasonCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		for(int i=tableModel.getRowCount(); i-->0; ) {
			TeacherRollCallRow row = tableModel.getObject(i);
			
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof MultipleSelectionElement) {
			MultipleSelectionElement check = (MultipleSelectionElement)source;
			TeacherRollCallRow row = (TeacherRollCallRow)check.getUserObject();
			if(row.getAuthorizedAbsence() == check) {
				doAuthorizedAbsence(row, check);
				if(check.isAtLeastSelected(1)) {
					doCalloutReasonAbsence(ureq, check.getFormDispatchId() + "_C_0", row);
				}
			} else {
				doCheckRow(row, check);
			}
		} else if(expandButton == source) {
			doToggleExpand();
		} else if(backLink == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(reopenButton == source) {
			doReopen(ureq);
		} else if(closeLectureBlocksButton == source) {
			if(validateFormLogic(ureq)) {
				saveLectureBlocks();
				doConfirmCloseLectureBlock(ureq);
			}
		} else if(cancelLectureBlockButton == source) {
			saveLectureBlocks();
			doConfirmCancelLectureBlock(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if(cmd != null) {
				if(cmd.startsWith("abs_reason_")) {
					doCalloutReasonAbsence(ureq, link.getFormDispatchId(), (TeacherRollCallRow)link.getUserObject());
				} else if(cmd.startsWith("all_")) {
					doCheckAllRow((TeacherRollCallRow)link.getUserObject());
				} else if(cmd.startsWith("notice_")) {
					doCalloutAbsenceNotice(ureq, link.getFormDispatchId(), (TeacherRollCallRow)link.getUserObject());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		saveLectureBlocks();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void recalculateNumOfAbsences(TeacherRollCallRow row) {
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
	
	private void saveLectureBlocks() {
		String before = lectureService.toAuditXml(lectureBlock);
		
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
		
		if(lectureBlock.getRollCallStatus() == null) {
			lectureBlock.setRollCallStatus(LectureRollCallStatus.open);
		}
		if(lectureBlock.getStatus() == null || lectureBlock.getStatus() == LectureBlockStatus.active) {
			lectureBlock.setStatus(LectureBlockStatus.active);
		}
		lectureBlock = lectureService.save(lectureBlock, null);
		lectureService.recalculateSummary(lectureBlock.getEntry());
		
		String after = lectureService.toAuditXml(lectureBlock);
		lectureService.auditLog(LectureBlockAuditLog.Action.saveLectureBlock, before, after, null, lectureBlock, null, lectureBlock.getEntry(), null, getIdentity());
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doToggleExpand() {
		Boolean expanded = (Boolean)expandButton.getUserObject();
		boolean nextState = !expanded.booleanValue();
		String nextCssState = nextState ? "o_icon_details_collaps" : "o_icon_details_expand";
		expandButton.setIconLeftCSS("o_icon o_icon_lg ".concat(nextCssState));
		expandButton.setUserObject(Boolean.valueOf(nextState));
		flc.contextPut("expandedDescription", Boolean.valueOf(nextState));
	}
	
	private void doCheckAllRow(TeacherRollCallRow row) {
		List<Integer> allAbsences = new ArrayList<>(numOfLectures);
		for(int i=0; i<numOfLectures; i++) {
			allAbsences.add(i);
		}
		LectureBlockRollCall rollCall = lectureService.addRollCall(row.getIdentity(), lectureBlock, row.getRollCall(), null, allAbsences);
		for(MultipleSelectionElement check:row.getChecks()) {
			check.select(onKeys[0], true);
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
		recalculateNumOfAbsences(row);
		tableEl.reloadData();
		flc.setDirty(true);
	}
	
	private void doCheckRow(TeacherRollCallRow row, MultipleSelectionElement check) {
		int index = row.getIndexOfCheck(check);
		List<Integer> indexList = Collections.singletonList(index);
		
		LectureBlockRollCall rollCall;
		String before = lectureService.toAuditXml(row.getRollCall());
		if(check.isAtLeastSelected(1)) {
			rollCall = lectureService.addRollCall(row.getIdentity(), lectureBlock, row.getRollCall(), indexList);
			lectureService.auditLog(LectureBlockAuditLog.Action.addToRollCall, before, lectureService.toAuditXml(rollCall),
					Integer.toString(index), lectureBlock, rollCall, lectureBlock.getEntry(), row.getIdentity(), getIdentity());
		} else {
			rollCall = lectureService.removeRollCall(row.getIdentity(), lectureBlock, row.getRollCall(), indexList);
			lectureService.auditLog(LectureBlockAuditLog.Action.removeFromRollCall, before, lectureService.toAuditXml(rollCall),
					Integer.toString(index), lectureBlock, rollCall, lectureBlock.getEntry(), row.getIdentity(), getIdentity());
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
		recalculateNumOfAbsences(row);
	}
	
	private void doAuthorizedAbsence(TeacherRollCallRow row, MultipleSelectionElement check) {
		LectureBlockRollCall rollCall = row.getRollCall();
		boolean authorized = check.isAtLeastSelected(1);
		if(rollCall == null) {
			rollCall = lectureService.getOrCreateRollCall(row.getIdentity(), lectureBlock, authorized, null, null);
			lectureService.auditLog(LectureBlockAuditLog.Action.createRollCall, null, lectureService.toAuditXml(rollCall),
					authorized ? "true" : "false", lectureBlock, rollCall, lectureBlock.getEntry(), row.getIdentity(), getIdentity());
		} else {
			String before = lectureService.toAuditXml(rollCall);
			rollCall.setAbsenceAuthorized(authorized);
			rollCall = lectureService.updateRollCall(rollCall);
			lectureService.auditLog(LectureBlockAuditLog.Action.updateAuthorizedAbsence, before, lectureService.toAuditXml(rollCall),
					authorized ? "true" : "false", lectureBlock, rollCall, lectureBlock.getEntry(), row.getIdentity(), getIdentity());
		}

		row.getReasonLink().setVisible(authorized);
		row.getAuthorizedAbsenceCont().setDirty(true);
		row.getAuthorizedAbsence().clearError();
		row.setRollCall(rollCall);
		row.getRollCallStatusEl().getComponent().setDirty(true);
	}
	
	private void doCalloutReasonAbsence(UserRequest ureq, String elementId, TeacherRollCallRow row) {
		boolean canEdit = secCallback.canEdit() && secCallback.canEditAuthorizedAbsences();
		reasonCtrl = new ReasonController(ureq, getWindowControl(), row, canEdit);
		listenTo(reasonCtrl);

		reasonCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				reasonCtrl.getInitialComponent(), elementId, "", true, "");
		listenTo(reasonCalloutCtrl);
		reasonCalloutCtrl.activate();
	}
	
	private void doReason(TeacherRollCallRow row, String reason, AbsenceCategory category) {
		LectureBlockRollCall rollCall = row.getRollCall();
		String before = lectureService.toAuditXml(rollCall);
		if(rollCall == null) {
			row.getAuthorizedAbsence().select(onKeys[0], true);
			rollCall = lectureService.getOrCreateRollCall(row.getIdentity(), lectureBlock, true, reason, category);
			lectureService.auditLog(LectureBlockAuditLog.Action.createRollCall, before, lectureService.toAuditXml(rollCall),
					reason, lectureBlock, rollCall, lectureBlock.getEntry(), row.getIdentity(), getIdentity());
		} else {
			rollCall.setAbsenceReason(reason);
			rollCall.setAbsenceCategory(category);
			rollCall = lectureService.updateRollCall(rollCall);
			lectureService.auditLog(LectureBlockAuditLog.Action.updateRollCall, before, lectureService.toAuditXml(rollCall),
					reason, lectureBlock, rollCall, lectureBlock.getEntry(), row.getIdentity(), getIdentity());
		}

		row.setRollCall(rollCall);
	}
	
	private void doCalloutAbsenceNotice(UserRequest ureq, String elementId, TeacherRollCallRow row) {
		noticeDetailsCtrl = new AbsenceNoticeDetailsCalloutController(ureq, getWindowControl(), row.getAbsenceNotice());
		listenTo(noticeDetailsCtrl);

		noticeCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				noticeDetailsCtrl.getInitialComponent(), elementId, "", true, "");
		listenTo(noticeCalloutCtrl);
		noticeCalloutCtrl.activate();
	}
	
	private void doConfirmCloseLectureBlock(UserRequest ureq) {
		if(guardModalController(closeRollCallCtrl)) return;
		
		closeRollCallCtrl = new CloseRollCallConfirmationController(ureq, getWindowControl(), lectureBlock, secCallback);
		listenTo(closeRollCallCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", closeRollCallCtrl.getInitialComponent(), true, translate("close.lecture.blocks"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmCancelLectureBlock(UserRequest ureq) {
		if(guardModalController(cancelRollCallCtrl)) return;
		
		cancelRollCallCtrl = new CancelRollCallConfirmationController(ureq, getWindowControl(), lectureBlock, secCallback);
		listenTo(cancelRollCallCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", cancelRollCallCtrl.getInitialComponent(), true, translate("cancel.lecture.blocks"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doReopen(UserRequest ureq) {
		String before = lectureService.toAuditXml(lectureBlock);
		lectureBlock.setRollCallStatus(LectureRollCallStatus.reopen);
		if(lectureBlock.getStatus() == LectureBlockStatus.cancelled) {
			lectureBlock.setStatus(LectureBlockStatus.active);
		}
		
		lectureBlock = lectureService.save(lectureBlock, null);
		secCallback.updateLectureBlock(lectureBlock);
		updateUI();
		fireEvent(ureq, new ReopenLectureBlockEvent());
		
		String after = lectureService.toAuditXml(lectureBlock);
		lectureService.auditLog(LectureBlockAuditLog.Action.reopenLectureBlock, before, after, null, lectureBlock, null, lectureBlock.getEntry(), null, getIdentity());
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_ROLL_CALL_REOPENED, getClass(),
				CoreLoggingResourceable.wrap(lectureBlock, OlatResourceableType.lectureBlock, lectureBlock.getTitle()));
	}
}
