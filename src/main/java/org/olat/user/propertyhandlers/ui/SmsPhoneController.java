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
package org.olat.user.propertyhandlers.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.User;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 7 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SmsPhoneController extends BasicController {
	
	private final VelocityContainer mainVC;

	private SmsPhoneConfirmController confirmCtrl;
	private final SmsPhoneSendController sendTokenCtrl;
	
	public SmsPhoneController(UserRequest ureq, WindowControl wControl, UserPropertyHandler handler, User userToChange) {
		super(ureq, wControl);

		sendTokenCtrl = new SmsPhoneSendController(ureq, getWindowControl(), handler, userToChange);
		listenTo(sendTokenCtrl);
		
		mainVC = createVelocityContainer("edit_sms");
		mainVC.put("cmp", sendTokenCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}
	
	public String getPhone() {
		return sendTokenCtrl.getPhone();
	}
	
	@Override
	public WindowControl getWindowControl() {
		return super.getWindowControl();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == sendTokenCtrl) {
			if(event == Event.DONE_EVENT) {
				doConfirm(ureq);
			} else {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} else if(source == confirmCtrl) {
			fireEvent(ureq, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private void doConfirm(UserRequest ureq) {
		if(confirmCtrl == null) {
			confirmCtrl = new SmsPhoneConfirmController(ureq, getWindowControl(), sendTokenCtrl.getSentToken());
			listenTo(confirmCtrl);
		}
		mainVC.put("cmp", confirmCtrl.getInitialComponent());
	}
}
