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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;
import org.olat.modules.openmeetings.OpenMeetingsModule;
import org.olat.modules.openmeetings.manager.OpenMeetingsManager;
import org.olat.modules.openmeetings.manager.OpenMeetingsException;

/**
 * 
 * Initial date: 06.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsRunController extends FormBasicController {

	private FormLink openLink;
	
	private final Long roomId;
	private final OpenMeetingsModule openMeetingsModule;
	private final OpenMeetingsManager openMeetingsManager;

	public OpenMeetingsRunController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores,
			String subIdentifier, String resourceName, boolean admin) {
		super(ureq, wControl, "room");

		openMeetingsModule = CoreSpringFactory.getImpl(OpenMeetingsModule.class);
		openMeetingsManager = CoreSpringFactory.getImpl(OpenMeetingsManager.class);
		roomId = openMeetingsManager.getRoomId(group, ores, subIdentifier);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if(!openMeetingsModule.isEnabled()) {
				layoutCont.contextPut("disabled", Boolean.TRUE);
			} else if(roomId == null) {
				layoutCont.contextPut("norroom", Boolean.TRUE);
			} else {

				FormLayoutContainer roomCont = FormLayoutContainer.createDefaultFormLayout("openroom", getTranslator());
				layoutCont.add(roomCont);
				
				String name = "Hello";
				uifactory.addStaticTextElement("room.name", "room.name", name, roomCont);
				openLink = uifactory.addFormLink("open", "open.room", null, roomCont, Link.BUTTON);
				((Link)openLink.getComponent()).setTarget("openmeetings");
			}
		}
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

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == openLink) {
			doOpenRoom(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	private void doOpenRoom(UserRequest ureq) {	
		if(roomId == null && roomId.longValue() <= 0) {
		
		}
		try {
			String securedHash = openMeetingsManager.setUserToRoom(getIdentity(), roomId, true);
			String url = openMeetingsManager.getURL(getIdentity(), roomId.longValue(), securedHash, getLocale());
			RedirectMediaResource redirect = new RedirectMediaResource(url);
			ureq.getDispatchResult().setResultingMediaResource(redirect);
		} catch (OpenMeetingsException e) {
			showError(e.getType().i18nKey());
		} 
	}
}
