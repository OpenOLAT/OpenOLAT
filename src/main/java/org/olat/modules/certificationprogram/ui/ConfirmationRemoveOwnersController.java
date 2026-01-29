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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.id.Identity;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ConfirmationRemoveOwnersController extends ConfirmationController {
	
	private final List<Identity> identities;
	private final CertificationProgram certificationProgram;
	
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public ConfirmationRemoveOwnersController(UserRequest ureq, WindowControl wControl, String message, String confirmation,
			String confirmButton, CertificationProgram certificationProgram, List<Identity> identities) {
		super(ureq, wControl, message, confirmation, confirmButton, ButtonType.danger);
		this.identities = identities;
		this.certificationProgram = certificationProgram;
	}

	@Override
	protected void doAction(UserRequest ureq) {
		for(Identity identity: identities) {
			certificationProgramService.removeCertificationProgramOwner(certificationProgram, identity, getIdentity());
			getLogger().info("Remove owner {} from certification program {}", identity.getKey(), certificationProgram.getKey());
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
