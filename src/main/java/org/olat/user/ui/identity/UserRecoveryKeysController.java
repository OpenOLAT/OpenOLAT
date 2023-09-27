/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user.ui.identity;

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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.login.webauthn.ui.RecoveryKeysController;

/**
 * 
 * Initial date: 25 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserRecoveryKeysController extends FormBasicController {
	
	private FormLink newRecoveryKeysButton;

	private CloseableModalController cmc;
	private RecoveryKeysController recoveryKeysCtrl;
	
	public UserRecoveryKeysController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "recoverykeys");
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		newRecoveryKeysButton = uifactory.addFormLink("generate.recovery.keys", formLayout, Link.BUTTON);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(recoveryKeysCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(recoveryKeysCtrl);
		removeAsListenerAndDispose(cmc);
		recoveryKeysCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == newRecoveryKeysButton) {
			doGenerateRecoveryKes(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doGenerateRecoveryKes(UserRequest ureq) {
		recoveryKeysCtrl = new RecoveryKeysController(ureq, getWindowControl());
		listenTo(recoveryKeysCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), recoveryKeysCtrl.getInitialComponent(),
				true, translate("generate.recovery.keys"));
		cmc.activate();
		listenTo(cmc);
	}
}
