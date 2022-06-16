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
package org.olat.course.nodes.practice.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.practice.PlayMode;

/**
 * 
 * Initial date: 1 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmCancelController extends FormBasicController {
	
	private PlayMode playMode;
	
	public ConfirmCancelController(UserRequest ureq, WindowControl wControl, PlayMode playMode) {
		super(ureq, wControl, "confirm_back");
		this.playMode = playMode;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if(playMode == PlayMode.freeShuffle) {
				layoutCont.contextPut("msg", translate("confirm.back.text"));
			} else {
				layoutCont.contextPut("msg", translate("confirm.back.text.individual"));
			}
		}
		
		FormCancel cancel = uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		cancel.setI18nKey("cancel.serie");
		FormSubmit submit = uifactory.addFormSubmitButton("back", formLayout);
		submit.setI18nKey("back.serie", null);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
