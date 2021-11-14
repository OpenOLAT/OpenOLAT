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
package org.olat.course.nodes.livestream.ui;

import java.net.URL;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.livestream.LiveStreamService;
import org.olat.course.nodes.livestream.model.UrlTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UrlTemplateEditController extends FormBasicController {

	private TextElement nameEl;
	private TextElement url1El;
	private TextElement url2El;

	private UrlTemplate urlTemplate;
	
	@Autowired
	private LiveStreamService liveStreamService;

	public UrlTemplateEditController(UserRequest ureq, WindowControl wControl, UrlTemplate urlTemplate) {
		super(ureq, wControl);
		this.urlTemplate = urlTemplate;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name = urlTemplate != null? urlTemplate.getName(): null;
		nameEl = uifactory.addTextElement("url.template.name", 138, name, formLayout);
		nameEl.setMandatory(true);
		
		String url1 = urlTemplate != null? urlTemplate.getUrl1(): null;
		url1El = uifactory.addTextElement("url.template.url1", 2000, url1, formLayout);

		String url2 = urlTemplate != null? urlTemplate.getUrl2(): null;
		url2El = uifactory.addTextElement("url.template.url2", 2000, url2, formLayout);
		
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
		
		url1El.clearError();
		url2El.clearError();
		if (!StringHelper.containsNonWhitespace(url1El.getValue()) && !StringHelper.containsNonWhitespace(url2El.getValue())) {
			url1El.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			allOk &= validateUrl(url1El);
			allOk &= validateUrl(url2El);
		}
		
		return allOk;
	}
	
	private boolean validateUrl(TextElement textEl) {
		boolean allOk = true;
		
		if (StringHelper.containsNonWhitespace(textEl.getValue())) {
			try {
				new URL(textEl.getValue()).toURI();
			} catch(Exception e) {
				textEl.setErrorKey("error.url.not.valid", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (urlTemplate == null) {
			urlTemplate = liveStreamService.createUrlTemplate(nameEl.getValue());
		}
		
		urlTemplate.setName(nameEl.getValue());
		urlTemplate.setUrl1(url1El.getValue());
		urlTemplate.setUrl2(url2El.getValue());
		
		urlTemplate = liveStreamService.updateUrlTemplate(urlTemplate);
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}
}
