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
package org.olat.course.nodes.videotask.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmChangeModeController extends FormBasicController {
	
	private final String subIdent;
	private final String currentMode;
	private final RepositoryEntry entry;
	private final long numOfParticipants;
	
	@Autowired
	private VideoAssessmentService videoAssessmentService;
	
	public ConfirmChangeModeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdent, String currentMode,
			long numOfParticipants) {
		super(ureq, wControl, "confirm_change_mode");
		this.entry = entry;
		this.subIdent = subIdent;
		this.currentMode = currentMode;
		this.numOfParticipants = numOfParticipants;
		initForm(ureq);
	}
	
	public String getCurrentMode() {
		return currentMode;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			String i18nKey = numOfParticipants <= 1 ? "change.mode.descr.singular" : "change.mode.descr.plural";
			String msg = translate(i18nKey, Long.toString(numOfParticipants));
			layoutCont.contextPut("msg", msg);
		}
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("change.mode", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		videoAssessmentService.deleteTaskSessions(entry, subIdent);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
