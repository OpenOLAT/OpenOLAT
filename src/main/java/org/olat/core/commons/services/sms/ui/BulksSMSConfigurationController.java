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
package org.olat.core.commons.services.sms.ui;

import org.olat.core.commons.services.sms.spi.BulkSMSProvider;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulksSMSConfigurationController extends AbstractSMSConfigurationController {
	
	private TextElement tokenIdEl;
	private TextElement tokenSecretEl;
	
	@Autowired
	private BulkSMSProvider bulkSmsProvider;
	
	public BulksSMSConfigurationController(UserRequest ureq, WindowControl wControl, Form form) {
		super(ureq, wControl, form);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String tokenId = bulkSmsProvider.getTokenId();
		tokenIdEl = uifactory.addTextElement("bulksms.token.id", 128, tokenId, formLayout);
		tokenIdEl.setMandatory(true);
		
		String tokenSecret = bulkSmsProvider.getTokenSecret();
		tokenSecretEl = uifactory.addTextElement("bulksms.token.secret", 128, tokenSecret, formLayout);
		tokenSecretEl.setMandatory(true);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		tokenIdEl.clearError();
		if(!StringHelper.containsNonWhitespace(tokenIdEl.getValue())) {
			tokenIdEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		tokenSecretEl.clearError();
		if(!StringHelper.containsNonWhitespace(tokenSecretEl.getValue())) {
			tokenSecretEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		bulkSmsProvider.setTokenId(tokenIdEl.getValue());
		bulkSmsProvider.setTokenSecret(tokenSecretEl.getValue());
	}
}
