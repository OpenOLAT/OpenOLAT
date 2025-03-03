/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.registration;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Feb 25, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RegistrationExternalAdminController extends FormBasicController {


	@Autowired
	private RegistrationModule registrationModule;

	public RegistrationExternalAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_external");

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer externalContainer = FormLayoutContainer.createDefaultFormLayout("externalCont", getTranslator());
		externalContainer.setRootForm(mainForm);
		externalContainer.setFormTitle(translate("remote.login.title"));
		formLayout.add(externalContainer);

		StaticTextElement regExampleStaticEl = uifactory.addStaticTextElement("admin.registration.code.reg", generateExampleCode(), externalContainer);
		regExampleStaticEl.setVisible(registrationModule.isSelfRegistrationLinkEnabled());
		regExampleStaticEl.setElementCssClass("o_code_self_reg");

		String remoteExample = generateRemoteLoginExampleCode();
		TextAreaElement signInExampleEl = uifactory.addTextAreaElement("remotelogin.example", "admin.registration.code.login", 64000, 4, 65, true, false, remoteExample, externalContainer);
		signInExampleEl.setVisible(registrationModule.isSelfRegistrationLinkEnabled());

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		formLayout.add(buttonsCont);
	}

	private String generateExampleCode() {
		String selfRegLink = Settings.getServerContextPathURI() + "/url/registration/0";

		StringBuilder code = new StringBuilder();
		code.append("<div class=\"o_copy_code\">")
				.append("    <a href=\"javascript:;\" id=\"o_selfreglink\">")
				.append("        <i class=\"o_icon o_icon-lg o_icon-fw o_icon_qrcode\" aria-hidden='true' title='")
				.append("            $r.translateInAttribute(\"qr.code\")'>&nbsp;</i>")
				.append("        <span class='sr-only'>$r.translate(\"link.resource\")</span>")
				.append("    </a>")
				.append("    <input type=\"text\" value=\"").append(selfRegLink).append("\" onclick=\"this.select()\" aria-labelledby=\"o_selfreglink\"/>")
				.append("    <script>")
				.append("        jQuery(function() {")
				.append("            o_QRCodePopup('o_selfreglink', '").append(selfRegLink).append("', 'right');")
				.append("        });")
				.append("    </script>")
				.append("</div>");

		return code.toString();
	}

	private String generateRemoteLoginExampleCode() {
		StringBuilder code = new StringBuilder();
		code.append("<form name=\"olatremotelogin\" action=\"")
				.append(Settings.getServerContextPathURI()).append("/remotelogin/")
				.append("\" method=\"post\" target=\"OpenOLAT\" onsubmit=\"var openolat=window.open('','OpenOLAT', 'location=no,menubar=no,resizable=yes,toolbar=no,statusbar=no,scrollbars=yes'); openolat.focus();\">\n")
				.append("  Benutzername <input type=\"text\" name=\"username\">")
				.append("  Passwort <input type=\"password\" name=\"pwd\">")
				.append("  <input type=\"submit\" value=\"Login\">\n")
				.append("</form>");
		return code.toString();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
