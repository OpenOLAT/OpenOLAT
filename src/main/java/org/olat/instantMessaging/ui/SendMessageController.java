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
package org.olat.instantMessaging.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.InstantMessageTypeEnum;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.RosterEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SendMessageController extends FormBasicController {
	
	private TextElement textEl;
	
	private final String fromMe;
	private final List<RosterRow> rows;
	
	@Autowired
	private InstantMessagingService imService;
	
	public SendMessageController(UserRequest ureq, WindowControl wControl, List<RosterRow> rows, String fromMe) {
		super(ureq, wControl);
		this.rows = rows;
		this.fromMe = fromMe;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		textEl = uifactory.addTextAreaElement("message", "im.message", 4096, 3, 60, false, false, true, "", formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("msg.send", buttonsCont);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String msg = textEl.getValue();
		for(RosterRow row:rows) {
			List<RosterEntry> entries = row.getRoster().getNonVipEntries();
			for(RosterEntry entry:entries) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance(entry.getResourceTypeName(), entry.getResourceId());
				imService.sendMessage(getIdentity(), fromMe, false, msg, InstantMessageTypeEnum.text,
						ores, entry.getResSubPath(), entry.getChannel(), null);
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
