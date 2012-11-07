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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;
import org.olat.modules.openmeetings.manager.OpenMeetingsManager;
import org.olat.modules.openmeetings.manager.OpenMeetingsNotAvailableException;

/**
 * 
 * Initial date: 06.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsRunController extends FormBasicController {

	private final Long roomId;
	private final OpenMeetingsManager openMeetingsManager;

	public OpenMeetingsRunController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores,
			String subIdentifier, String resourceName, boolean admin) {
		super(ureq, wControl);
		
		openMeetingsManager = CoreSpringFactory.getImpl(OpenMeetingsManager.class);
		roomId = openMeetingsManager.getRoomId(group, ores, subIdentifier);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("open.room", "open.room", buttonsContainer);
		
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(roomId != null && roomId.longValue() > 0) {
			doOpenRoom(ureq);
		}
	}
	
	private void doOpenRoom(UserRequest ureq) {	
		try {
			String securedHash = openMeetingsManager.setUser(getIdentity(), roomId);
			String url = openMeetingsManager.getURL(getIdentity(), roomId.longValue(), securedHash, getLocale());
			RedirectMediaResource redirect = new RedirectMediaResource(url);
			ureq.getDispatchResult().setResultingMediaResource(redirect);
		} catch (Exception e) {
			showError(OpenMeetingsNotAvailableException.I18N_KEY);
		} 
	}
}
