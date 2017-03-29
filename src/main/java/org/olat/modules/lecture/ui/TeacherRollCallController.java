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

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
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
	
	private int counter = 0;
	private final LectureBlock lectureBlock;
	private final boolean isAdministrativeUser;
	private final boolean autorizedAbsenceEnabled;
	private List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public TeacherRollCallController(UserRequest ureq, WindowControl wControl, LectureBlock block) {
		super(ureq, wControl, "rollcall");
		this.lectureBlock = block;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		autorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
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
		if(autorizedAbsenceEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCols.authorizedAbsence));
			//reason
			
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RollCols.comment));

		tableModel = new TeacherRollCallDataModel(columnsModel); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		
		uifactory.addFormSubmitButton("save", "save", formLayout);
	}
	
	private void loadModel() {
		List<Identity> participants = lectureService.getParticipants(lectureBlock);
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
		TeacherRollCallRow row = new TeacherRollCallRow(participant, userPropertyHandlers, getLocale());
		
		int numOfChecks = lectureBlock.getPlannedLecturesNumber();
		MultipleSelectionElement[] checks = new MultipleSelectionElement[numOfChecks];
		List<Integer> attended = rollCall == null ? Collections.emptyList() : rollCall.getLecturesAttendedList();
		
		for(int i=0; i<numOfChecks; i++) {
			String checkId = "check_".concat(Integer.toString(++counter));
			MultipleSelectionElement check = uifactory.addCheckboxesHorizontal(checkId, null, flc, onKeys, onValues);
			check.setDomReplacementWrapperRequired(false);
			check.addActionListener(FormEvent.ONCHANGE);
			check.setUserObject(row);
			check.setAjaxOnly(true);
			if(attended.contains(i)) {
				check.select(onKeys[0], true);
			}
			checks[i] = check;
			flc.add(check);
		}
		row.setChecks(checks);
		
		if(autorizedAbsenceEnabled) {
			String authorizedAbsencedId = "auth_abs_".concat(Integer.toString(++counter));
			MultipleSelectionElement authorizedAbsencedEl = uifactory.addCheckboxesHorizontal(authorizedAbsencedId, null, flc, onKeys, onValues);
			authorizedAbsencedEl.setDomReplacementWrapperRequired(false);
			authorizedAbsencedEl.addActionListener(FormEvent.ONCHANGE);
			authorizedAbsencedEl.setUserObject(row);
			authorizedAbsencedEl.setAjaxOnly(true);
			if(rollCall != null && rollCall.getAbsenceAuthorized() != null && rollCall.getAbsenceAuthorized().booleanValue()) {
				authorizedAbsencedEl.select(onKeys[0], true);
			}
			row.setAuthorizedAbsence(authorizedAbsencedEl);
			flc.add(authorizedAbsencedEl);
		}

		String comment = rollCall == null ? "" : rollCall.getComment();
		String commentId = "comment_".concat(Integer.toString(++counter));
		TextElement commentEl = uifactory.addTextElement(commentId, commentId, null, 128, comment, flc);
		commentEl.setDomReplacementWrapperRequired(false);
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
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		for(int i=tableModel.getRowCount(); i-->0; ) {
			TeacherRollCallRow row = tableModel.getObject(i);
			if(row.getRollCall() == null) {
				//??? stop?
			} else {
				String reason = row.getRollCall().getAbsenceReason();
				if(row.getAuthorizedAbsence().isAtLeastSelected(1) && !StringHelper.containsNonWhitespace(reason)) {
					allOk &= false;
				}
			}
		}

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(int i=tableModel.getRowCount(); i-->0; ) {
			TeacherRollCallRow row = tableModel.getObject(i);
			
			LectureBlockRollCall rollCall;
			if(row.getRollCall() == null) {
				rollCall = lectureService.createRollCall(row.getIdentity(), lectureBlock, null);
			} else {
				rollCall = row.getRollCall();
			}
			rollCall.setComment(row.getCommentEl().getValue());
			lectureService.updateRollCall(rollCall);
		}
	}
	
	private void doCheckAllRow(TeacherRollCallRow row) {
		Integer[] allIndex = new Integer[lectureBlock.getPlannedLecturesNumber()];
		for(int i=allIndex.length; i-->0; ) {
			allIndex[i] = i;
		}
		LectureBlockRollCall rollCall = lectureService.addRollCall(row.getIdentity(), lectureBlock, row.getRollCall(), allIndex);
		for(MultipleSelectionElement check:row.getChecks()) {
			check.select(onKeys[0], true);
		}
		row.setRollCall(rollCall);
		tableEl.reloadData();
		flc.setDirty(true);
	}
	
	private void doCheckRow(TeacherRollCallRow row, MultipleSelectionElement check) {
		int index = row.getIndexOfCheck(check);
		
		LectureBlockRollCall rollCall;
		if(check.isAtLeastSelected(1)) {
			rollCall = lectureService.addRollCall(row.getIdentity(), lectureBlock, row.getRollCall(), index);
		} else {
			rollCall = lectureService.removeRollCall(row.getIdentity(), lectureBlock, row.getRollCall(), index);
		}
		row.setRollCall(rollCall);	
	}
	
	private void doAuthorizedAbsence(TeacherRollCallRow row, MultipleSelectionElement check) {
		LectureBlockRollCall rollCall = row.getRollCall();
		if(rollCall == null) {
			rollCall = lectureService.createRollCall(row.getIdentity(), lectureBlock, check.isAtLeastSelected(1));		
		} else {
			rollCall.setAbsenceAuthorized(check.isAtLeastSelected(1));
			rollCall = lectureService.updateRollCall(rollCall);
		}
		row.setRollCall(rollCall);
	}
}
