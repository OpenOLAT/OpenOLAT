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
package org.olat.modules.lecture.ui.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesPermissionsSettingsAdminController extends FormBasicController {

	private static final String[] onKeys = new String[] { "on" };
	
	private MultipleSelectionElement teacherCanAuthorizeAbsenceEl;
	private MultipleSelectionElement teacherCanSeeAppealEl;
	private MultipleSelectionElement teacherCanAuthorizeAppealEl;
	
	private MultipleSelectionElement masterCoachCanSeeAbsenceEl;
	private MultipleSelectionElement masterCoachCanRecordNoticeEl;
	private MultipleSelectionElement masterCoachCanAuthorizeAbsenceEl;
	private MultipleSelectionElement masterCoachCanSeeAppealEl;
	private MultipleSelectionElement masterCoachCanAuthorizeAppealEl;
	private MultipleSelectionElement masterCoachCanReopenLectureBlocksEl;
	
	private MultipleSelectionElement participantCanNoticeEl;
	
	@Autowired
	private LectureModule lectureModule;
	
	public LecturesPermissionsSettingsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		
		initForm(ureq);
		initializeValues();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("lectures.admin.permissions.title");
		formLayout.setElementCssClass("o_sel_lecture_permissions");
		
		String[] onValues = new String[] { translate("on") };
		teacherCanAuthorizeAbsenceEl = uifactory.addCheckboxesHorizontal("lecture.teacher.can.authorize.absence", formLayout, onKeys, onValues);
		teacherCanSeeAppealEl = uifactory.addCheckboxesHorizontal("lecture.teacher.can.see.appeal", formLayout, onKeys, onValues);
		teacherCanAuthorizeAppealEl = uifactory.addCheckboxesHorizontal("lecture.teacher.can.authorize.appeal", formLayout, onKeys, onValues);
		
		uifactory.addSpacerElement("space-1", formLayout, true);
		
		masterCoachCanSeeAbsenceEl = uifactory.addCheckboxesHorizontal("lecture.mastercoach.can.see.absence", formLayout, onKeys, onValues);
		masterCoachCanRecordNoticeEl = uifactory.addCheckboxesHorizontal("lecture.mastercoach.can.record.notice", formLayout, onKeys, onValues);
		masterCoachCanAuthorizeAbsenceEl = uifactory.addCheckboxesHorizontal("lecture.mastercoach.can.authorize.absence", formLayout, onKeys, onValues);
		masterCoachCanSeeAppealEl = uifactory.addCheckboxesHorizontal("lecture.mastercoach.can.see.appeal", formLayout, onKeys, onValues);
		masterCoachCanAuthorizeAppealEl = uifactory.addCheckboxesHorizontal("lecture.mastercoach.can.authorize.appeal", formLayout, onKeys, onValues);
		masterCoachCanReopenLectureBlocksEl = uifactory.addCheckboxesHorizontal("lecture.mastercoach.can.reopen.lecture.blocks", formLayout, onKeys, onValues);

		uifactory.addSpacerElement("space-1", formLayout, true);
		
		participantCanNoticeEl = uifactory.addCheckboxesHorizontal("lecture.participant.can.notice", formLayout, onKeys, onValues);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setElementCssClass("o_sel_lecture_save_permissions");
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void initializeValues() {
		boolean authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		boolean appealEnabled = lectureModule.isAbsenceAppealEnabled();

		initializeValue(teacherCanAuthorizeAbsenceEl, lectureModule.isTeacherCanAuthorizedAbsence(), authorizedAbsenceEnabled);
		initializeValue(teacherCanSeeAppealEl, lectureModule.isTeacherCanSeeAppeal(), appealEnabled);
		initializeValue(teacherCanAuthorizeAppealEl, lectureModule.isTeacherCanAuthorizedAppeal(), appealEnabled);
		
		initializeValue(masterCoachCanSeeAbsenceEl, lectureModule.isMasterCoachCanSeeAbsence(), true);
		initializeValue(masterCoachCanRecordNoticeEl, lectureModule.isMasterCoachCanRecordNotice(), true);
		initializeValue(masterCoachCanAuthorizeAbsenceEl, lectureModule.isMasterCoachCanAuthorizedAbsence(), authorizedAbsenceEnabled);
		initializeValue(masterCoachCanSeeAppealEl, lectureModule.isMasterCoachCanSeeAppeal(), appealEnabled);
		initializeValue(masterCoachCanAuthorizeAppealEl, lectureModule.isMasterCoachCanAuthorizedAppeal(), appealEnabled);
		initializeValue(masterCoachCanReopenLectureBlocksEl, lectureModule.isMasterCoachCanReopenLectureBlocks(), true);
		
		initializeValue(participantCanNoticeEl, lectureModule.isParticipantCanNotice(), appealEnabled);
	}
	
	private void initializeValue(MultipleSelectionElement el, boolean value, boolean visible) {
		if(value) {
			el.select(onKeys[0], true);
		} else {
			el.uncheckAll();
		}
		el.setVisible(visible);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		boolean appealEnabled = lectureModule.isAbsenceAppealEnabled();
		
		lectureModule.setTeacherCanAuthorizedAbsence(authorizedAbsenceEnabled && teacherCanAuthorizeAbsenceEl.isAtLeastSelected(1));
		lectureModule.setTeacherCanSeeAppeal(appealEnabled && teacherCanSeeAppealEl.isAtLeastSelected(1));
		lectureModule.setTeacherCanAuthorizedAppeal(appealEnabled && teacherCanAuthorizeAppealEl.isAtLeastSelected(1));
		
		lectureModule.setMasterCoachCanSeeAbsence(masterCoachCanSeeAbsenceEl.isAtLeastSelected(1));
		lectureModule.setMasterCoachCanRecordNotice(masterCoachCanRecordNoticeEl.isAtLeastSelected(1));
		lectureModule.setMasterCoachCanAuthorizedAbsence(authorizedAbsenceEnabled && masterCoachCanAuthorizeAbsenceEl.isAtLeastSelected(1));
		lectureModule.setMasterCoachCanSeeAppeal(appealEnabled && masterCoachCanSeeAppealEl.isAtLeastSelected(1));
		lectureModule.setMasterCoachCanAuthorizedAppeal(appealEnabled && masterCoachCanAuthorizeAppealEl.isAtLeastSelected(1));
		lectureModule.setMasterCoachCanReopenLectureBlocks(appealEnabled && masterCoachCanReopenLectureBlocksEl.isAtLeastSelected(1));

		lectureModule.setParticipantCanNotice(participantCanNoticeEl.isAtLeastSelected(1));
	}
}
