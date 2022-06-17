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

package org.olat.resource.accesscontrol.provider.free.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ui.AccessEvent;
import org.olat.resource.accesscontrol.ui.FormController;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Description:<br>
 * Free access
 * 
 * <P>
 * Initial Date:  15 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class FreeSubmitController extends FormBasicController implements FormController {
	
	private final OfferAccess link;
	
	@Autowired
	private ACService acService;

	public FreeSubmitController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		super(ureq, wControl, "submit");
		this.link = link;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addFormSubmitButton("access.button", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		AccessResult result = acService.accessResource(getIdentity(), link, null);
		
		if(result.isAccessible()) {
			fireEvent(ureq, AccessEvent.ACCESS_OK_EVENT);
		} else {
			fireEvent(ureq, new AccessEvent(AccessEvent.ACCESS_FAILED));
		}
	}
}