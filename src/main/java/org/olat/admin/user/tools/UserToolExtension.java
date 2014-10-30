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
package org.olat.admin.user.tools;

import java.util.Locale;

import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 22.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserToolExtension extends GenericActionExtension {
	
	private String category;
	private String uniqueExtensionID;
	
	public boolean isShortCutOnly() {
		return false;
	}
	
	public String getShortCutCssId() {
		return null;
	}
	
	public String getShortCutCssClass() {
		return null;
	}

	@Override
	public String getUniqueExtensionID() {
		String id;
		if(StringHelper.containsNonWhitespace(uniqueExtensionID)) {
			id = uniqueExtensionID;
		} else {
			id = super.getUniqueExtensionID();
		}
		return UserToolsModule.stripToolKey(id);
	}
	
	public void setUniqueExtensionID(String uniqueExtensionID) {
		this.uniqueExtensionID = uniqueExtensionID;
	}
	
	public String getLabel(Locale locale) {
		return getActionText(locale);
	}
	
	public UserToolCategory getUserToolCategory() {
		UserToolCategory cat;
		if(StringHelper.containsNonWhitespace(category)) {
			cat = UserToolCategory.valueOf(category);
		} else {
			cat = UserToolCategory.personal;
		}
		return cat;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public UserTool createUserTool(@SuppressWarnings("unused") UserRequest ureq,
			WindowControl wControl, Locale locale) {
		return new UserToolImpl(this, wControl, locale);
	}
}
