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
package org.olat.admin.security;

import java.util.UUID;

import org.olat.basesecurity.MediaServer;
import org.olat.basesecurity.MediaServerModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-10-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditMediaServerController extends FormBasicController {

	private MediaServer mediaServer;
	private TextElement nameEl;
	private TextElement domainEl;

	@Autowired
	private MediaServerModule mediaServerModule;

	protected EditMediaServerController(UserRequest ureq, WindowControl wControl, MediaServer mediaServer) {
		super(ureq, wControl);
		this.mediaServer = mediaServer;
		initForm(ureq);
	}

	public MediaServer getMediaServer() {
		return mediaServer;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name = mediaServer != null ? mediaServer.getName() : null;
		nameEl = uifactory.addTextElement("media.server.name", 80, name, formLayout);
		nameEl.setMandatory(true);

		String domain = mediaServer != null ? mediaServer.getDomain() : null;
		domainEl = uifactory.addTextElement("media.server.domain", 256, domain, formLayout);
		domainEl.setMandatory(true);
		domainEl.setExampleKey("media.server.domain.example", null);

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		nameEl.clearError();
		if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		domainEl.clearError();
		if (!StringHelper.containsNonWhitespace(domainEl.getValue())) {
			domainEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if (!mediaServerModule.isValidDomain(domainEl.getValue())) {
			domainEl.setErrorKey("media.server.domain.error", null);
			allOk &= false;
		}


		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (mediaServer == null) {
			mediaServer = new MediaServer();
			mediaServer.setId(UUID.randomUUID().toString());
		}

		mediaServer.setName(nameEl.getValue());
		mediaServer.setDomain(domainEl.getValue());

		fireEvent(ureq, Event.DONE_EVENT);
	}
}
