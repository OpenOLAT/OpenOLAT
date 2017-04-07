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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Last step of the roll call wizard.
 * 
 * Initial date: 7 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherCloseRollCallController extends FormBasicController {
	
	private SingleSelection statusEl;
	private TextElement blockCommentEl;
	
	private LectureBlock lectureBlock;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	
	public TeacherCloseRollCallController(UserRequest ureq, WindowControl wControl, LectureBlock lectureBlock) {
		super(ureq, wControl);
		this.lectureBlock = lectureBlock;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] statusKeys = getAvailableStatus();
		String[] statusValues = new String[statusKeys.length];
		for(int i=statusKeys.length; i-->0; ) {
			statusValues[i] = translate(statusKeys[i]);
		}
		statusEl = uifactory.addDropdownSingleselect("lecture.block.status", "lecture.block.status", formLayout, statusKeys, statusValues, null);
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

		String comment = lectureBlock.getComment();
		blockCommentEl = uifactory.addTextAreaElement("comment", "lecture.block.comment", 2000, 4, 36, false, comment, formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
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

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		if(!statusEl.isOneSelected()) {
			statusEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		lectureBlock.setStatus(LectureBlockStatus.valueOf(statusEl.getSelectedKey()));
		lectureBlock.setComment(blockCommentEl.getValue());
		lectureBlock = lectureService.save(lectureBlock, null);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
