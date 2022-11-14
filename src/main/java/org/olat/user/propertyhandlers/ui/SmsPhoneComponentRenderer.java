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
package org.olat.user.propertyhandlers.ui;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 7 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SmsPhoneComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		SmsPhoneComponent smsCmp = (SmsPhoneComponent)source;
		SmsPhoneElement smsFte = smsCmp.getFormItem();
		String id = smsCmp.getFormDispatchId();
		String phoneNumber = smsFte.getPhone();
		if(!StringHelper.containsNonWhitespace(phoneNumber)) {
			phoneNumber = smsFte.getTranslator().translate("sms.phone.not.available");
		}
		
		sb.append("<p id=\"").append(id).append("\" ")
		  .append(" class='form-control-static ");
		if(StringHelper.containsNonWhitespace(smsCmp.getElementCssClass())) {
			sb.append(smsCmp.getElementCssClass());
		}
		sb.append("'>").append(phoneNumber).append("</p>");
		
		sb.append("<div class='o_form_example help-block'>").append(smsFte.getTranslator().translate("sms.phone.hint")).append("</div>");

		if(smsCmp.isEnabled()) {
			sb.append("<div class='form-inline'>");
			FormLink editLink = smsFte.getEditLink();
			if(editLink != null && editLink.isVisible()) {
				Component cmp = editLink.getComponent();
				cmp.getHTMLRendererSingleton().render(renderer, sb, cmp, ubu, translator, renderResult, args);
				cmp.setDirty(false);
			}
	
			if(StringHelper.containsNonWhitespace(phoneNumber)) {
				sb.append("&nbsp;");
				FormLink removeLink = smsFte.getRemoveLink();
				if(removeLink != null && removeLink.isVisible()) {
					Component cmp = removeLink.getComponent();
					cmp.getHTMLRendererSingleton().render(renderer, sb, cmp, ubu, translator, renderResult, args);
					cmp.setDirty(false);
				}
			}
			sb.append("</div>");
		}
	}
}
