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
package org.olat.modules.lecture.ui.coach;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmAuthorizeAbsenceNoticeController extends FormBasicController {
	
	private final List<AbsenceNotice> notices;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	
	public ConfirmAuthorizeAbsenceNoticeController(UserRequest ureq, WindowControl wControl, List<AbsenceNotice> notices) {
		super(ureq, wControl, "authorize_notices", Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.notices = new ArrayList<>(notices);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			StringBuilder sb = new StringBuilder();
			for(AbsenceNotice notice:notices) {
				String fullName = userManager.getUserDisplayName(notice.getIdentity());
				if(sb.indexOf(fullName) < 0) {
					if(sb.length() > 0) sb.append("; ");
					sb.append(fullName);
				}
			}
			String message = translate("confirm.authorize", new String[] { sb.toString() });
			layoutCont.contextPut("message", message);
		}
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("absences.batch.authorize", formLayout);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(AbsenceNotice notice:notices) {
			lectureService.updateAbsenceNoticeAuthorization(notice, getIdentity(), Boolean.TRUE, getIdentity());
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
