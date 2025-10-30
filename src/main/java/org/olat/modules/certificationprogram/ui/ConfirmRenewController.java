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
package org.olat.modules.certificationprogram.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.id.Identity;
import org.olat.modules.certificationprogram.CertificationCoordinator;
import org.olat.modules.certificationprogram.CertificationCoordinator.RequestMode;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ConfirmRenewController extends ConfirmationController {
	
	private final Identity certifiedIdentity;
	private final CertificationProgram certificationProgram;

	@Autowired
	private CertificationCoordinator certificationOrchestrator;
	
	public ConfirmRenewController(UserRequest ureq, WindowControl wControl, String message, String confirmation,
			String confirmButton, CertificationProgram certificationProgram, Identity certifiedIdentity) {
		super(ureq, wControl, message, confirmation, confirmButton, ButtonType.submitPrimary);
		this.certifiedIdentity = certifiedIdentity;
		this.certificationProgram = certificationProgram;
	}

	@Override
	protected void doAction(UserRequest ureq) {
		boolean produced = certificationOrchestrator
				.processCertificationRequest(certifiedIdentity, certificationProgram, RequestMode.COACH, ureq.getRequestTimestamp(), getIdentity());
		if(produced) {
			getLogger().info("Renew certificate of {} in certification program {}", certifiedIdentity.getKey(), certificationProgram.getKey());
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			fireEvent(ureq, Event.FAILED_EVENT);
		}
	}
}
