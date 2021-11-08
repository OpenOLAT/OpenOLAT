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

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RollCallInterceptorController extends FormBasicController implements SupportsAfterLoginInterceptor {
	
	private FormLink startWizardButton;
	
	private LectureBlock lectureBlockToStart;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	
	public RollCallInterceptorController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "rollcall_interceptor");
		
		initForm(ureq);
	}

	@Override
	public boolean isUserInteractionRequired(UserRequest ureq) {
		if(lectureModule.isEnabled()) {
			List<LectureBlock> lectureBlocks = lectureService.getRollCallAsTeacher(ureq.getIdentity());
			if(!lectureBlocks.isEmpty()) {
				Formatter format = Formatter.getInstance(getLocale());
				
				lectureBlockToStart = lectureBlocks.get(0);
				String[] args = new String[] {
						StringHelper.escapeHtml(lectureBlockToStart.getEntry().getDisplayname()),
						lectureBlockToStart.getEntry().getExternalRef() == null ? "" : StringHelper.escapeHtml(lectureBlockToStart.getEntry().getExternalRef()),
						StringHelper.escapeHtml(lectureBlockToStart.getTitle()),
						(lectureBlockToStart.getStartDate() == null ? "" : format.formatDate(lectureBlockToStart.getStartDate())),
						(lectureBlockToStart.getStartDate() == null ? "" : format.formatTimeShort(lectureBlockToStart.getStartDate())),
						(lectureBlockToStart.getEndDate() == null ? "" : format.formatTimeShort(lectureBlockToStart.getEndDate()))
				};
				flc.contextPut("message", translate("interceptor.start", args));
			}
		}
		return lectureBlockToStart != null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("show.lectures", formLayout);
		startWizardButton = uifactory.addFormLink("start.wizard", formLayout, Link.BUTTON);
		startWizardButton.setElementCssClass("o_sel_lecture_start_wizard");
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
		doStart(ureq, false);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == startWizardButton) {
			fireEvent (ureq, Event.DONE_EVENT);
			doStart(ureq, true);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doStart(UserRequest ureq, boolean wizard) {
		if(lectureBlockToStart != null) {
			try {
				String businessPath = "[RepositoryEntry:" + lectureBlockToStart.getEntry().getKey() + "][LectureBlock:" + lectureBlockToStart.getKey() + "]";
				if(wizard) {
					businessPath += "[StartWizard:0]";
				} else {
					businessPath += "[Start:0]";
				}
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			} catch (Exception e) {
				logError("Error while resuming", e);
			}
		}
	}
}
