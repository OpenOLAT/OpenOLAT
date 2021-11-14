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
package org.olat.modules.video.spi.youtube.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.video.spi.youtube.YoutubeProvider;
import org.olat.modules.video.spi.youtube.model.YoutubeMetadata;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class YoutubeAdminController extends FormBasicController {
	
	private TextElement apiKeyEl;
	private FormLink checkButton;
	
	@Autowired
	private YoutubeProvider youtubeProvider;
	
	public YoutubeAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("api.configuration");
		setFormInfo("api.configuration.explain");
		
		String apiKey = youtubeProvider.getApiKey();
		apiKeyEl = uifactory.addTextElement("api.key", 512, apiKey, formLayout);
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsLayout);
		uifactory.addFormSubmitButton("save", buttonsLayout);
		uifactory.addFormResetButton("api.remove", "api.remove", buttonsLayout);
		checkButton = uifactory.addFormLink("api.check", buttonsLayout, Link.BUTTON);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(checkButton == source) {
			doCheck();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formResetted(UserRequest ureq) {
		apiKeyEl.setValue("");
		youtubeProvider.setApiKey(null);
		showInfo("save.admin.settings");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		youtubeProvider.setApiKey(apiKeyEl.getValue());
		showInfo("save.admin.settings");
	}
	
	private void doCheck() {
		String thUrl = "https://youtu.be/T2rGplgQ3cA";
		YoutubeMetadata data = youtubeProvider.getSnippet(thUrl, apiKeyEl.getValue());
		if(data != null && StringHelper.containsNonWhitespace(data.getTitle())) {
			showInfo("api.check.success");
		} else {
			showWarning("api.check.failed");
		}
	}
}
