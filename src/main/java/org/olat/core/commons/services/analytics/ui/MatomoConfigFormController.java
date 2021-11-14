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
package org.olat.core.commons.services.analytics.ui;

import org.olat.core.commons.services.analytics.spi.MatomoSPI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MatomoConfigFormController extends FormBasicController {
	
	private TextElement siteIdEl;
	private TextElement trackerUrlEl;
	
	@Autowired
	private MatomoSPI matomoModule;
	
	public MatomoConfigFormController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("matomo.title");		
		setFormDescription("matomo.desc");
		
		String siteId = matomoModule.getSiteId();
		siteIdEl = uifactory.addTextElement("matomo.site.id", 6, siteId, formLayout);
		String trackerUrl = matomoModule.getTrackerUrl();
		trackerUrlEl = uifactory.addTextElement("matomo.tracker.url", 128, trackerUrl, flc);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		siteIdEl.clearError();
		if(!StringHelper.containsNonWhitespace(siteIdEl.getValue())) {
			siteIdEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(!StringHelper.isLong(siteIdEl.getValue())) {
			siteIdEl.setErrorKey("form.error.nointeger", null);
			allOk &= false;
		}
		
		trackerUrlEl.clearError();
		if(!StringHelper.containsNonWhitespace(trackerUrlEl.getValue())) {
			trackerUrlEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		matomoModule.setSiteId(siteIdEl.getValue());
		matomoModule.setTrackerUrl(trackerUrlEl.getValue());
	}
}
