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
package org.olat.user.propertyhandlers;

import java.util.Locale;

import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;

/**
 * <h3>Description:</h3>
 * The skype field provides a user property that contains a valid Skype ID. 
 * <p>
 * Initial Date: 27.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class SkypePropertyHandler extends Generic127CharTextPropertyHandler {
	
	private static final int MAX_LENGTH = 127;
	
	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		String skypeid = getUserProperty(user, locale);
		if(StringHelper.containsNonWhitespace(skypeid)) {
			skypeid = StringHelper.escapeHtml(skypeid);
			StringBuilder sb = new StringBuilder();
			sb.append("<div id=\"SkypeButton_Call_").append(skypeid).append("_1\" class=\"o_skype_button\">")
			  .append("<script>\n")
			  .append("try{ jQuery.getScript('https://secure.skypeassets.com/i/scom/js/skype-uri.js', function() {\n")
			  .append("Skype.ui({\n")
			  .append(" \"name\": \"dropdown\",")
			  .append(" \"element\": \"SkypeButton_Call_").append(skypeid).append("_1\",")
			  .append(" \"participants\": [\"").append(skypeid).append("\"]")
			  .append("});")
			  .append("});} catch(e) {}")
			  .append("</script>")
			  .append("</div>&nbsp;<span>").append(skypeid).append("</span>");
			return sb.toString();
		}
		return null;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		if(!super.isValidValue(user, value, validationError, locale)) return false;
		
		if(StringHelper.containsNonWhitespace(value) && value.length() > MAX_LENGTH) {
			validationError.setErrorKey("general.error.max." + MAX_LENGTH);
			return false;
		}
		return true;
	}

	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser,
			FormItemContainer formItemContainer) {
		org.olat.core.gui.components.form.flexible.elements.TextElement textElement = (org.olat.core.gui.components.form.flexible.elements.TextElement)super.addFormItem(locale, user, usageIdentifyer, isAdministrativeUser, formItemContainer);
		textElement.setMaxLength(MAX_LENGTH);
		if(!UserManager.getInstance().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
			textElement.setExampleKey("form.example.skypename", null);
		}
		return textElement;
	}
}
