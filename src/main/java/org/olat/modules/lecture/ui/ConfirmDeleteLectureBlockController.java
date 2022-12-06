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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockManagedFlag;
import org.olat.modules.lecture.LectureService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteLectureBlockController extends FormBasicController {
	
	private static final String[] confirmKeys = new String[] { "confirm" };
	
	private FormLink deleteButton;
	private MultipleSelectionElement confirmEl;
	
	private final List<LectureBlock> blocks;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	
	public ConfirmDeleteLectureBlockController(UserRequest ureq, WindowControl wControl, List<LectureBlock> blocks) {
		super(ureq, wControl, "confirm_delete");
		this.blocks = blocks;
		initForm(ureq);
	}
	
	public List<LectureBlock> getBlocks() {
		return blocks;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			StringBuilder titles = new StringBuilder(128);
			for(LectureBlock block:blocks) {
				if(titles.length() > 0) titles.append(", ");
				titles.append(StringHelper.escapeHtml(block.getTitle()));
			}
			String text = translate("confirm.delete.lectures", titles.toString());
			((FormLayoutContainer)formLayout).contextPut("msg", text);
		}
		
		List<AbsenceNotice> notices = lectureService.getAbsenceNoticeUniquelyRelatedTo(blocks);
		if(!notices.isEmpty()) {
			// confirmation delete notices
			Set<Identity> identities = new HashSet<>();
			for(AbsenceNotice notice:notices) {
				identities.add(notice.getIdentity());
			}

			StringBuilder names = new StringBuilder(128);
			for(Identity identity:identities) {
				String name = userManager.getUserDisplayName(identity);
				if(names.length() > 0) names.append(", ");
				names.append(name);
			}

			String text = translate("confirm.delete.lectures.notices", Integer.toString(notices.size()),  names.toString());
			((FormLayoutContainer)formLayout).contextPut("noticeMsg", text);

			String[] confirmationValues = new String[] {translate("confirm.delete.lectures.notices.confirmation") };
			confirmEl = uifactory.addCheckboxesHorizontal("confirm", null, formLayout, confirmKeys, confirmationValues);
		}
		deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(confirmEl != null) {
			confirmEl.clearError();
			if(!confirmEl.isAtLeastSelected(1)) {
				confirmEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteButton == source) {
			if(validateFormLogic(ureq)) {
				doDeleteLectureBlocks(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doDeleteLectureBlocks(UserRequest ureq) {
		for(LectureBlock block:blocks) {
			if(LectureBlockManagedFlag.isManaged(block, LectureBlockManagedFlag.delete)) {
				continue;
			}
			lectureService.deleteLectureBlock(block, getIdentity());
			logAudit("Lecture block deleted: " + block);
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_DELETED, getClass(),
					CoreLoggingResourceable.wrap(block, OlatResourceableType.lectureBlock, block.getTitle()));
		}
		showInfo("lecture.deleted");
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
