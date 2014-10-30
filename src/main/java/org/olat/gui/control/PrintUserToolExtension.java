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
package org.olat.gui.control;

import java.util.Locale;

import org.olat.admin.user.tools.UserTool;
import org.olat.admin.user.tools.UserToolCategory;
import org.olat.admin.user.tools.UserToolExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 29.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PrintUserToolExtension extends UserToolExtension {

	@Override
	public String getShortCutCssId() {
		return "o_navbar_print";
	}
	
	@Override
	public String getShortCutCssClass() {
		return null;
	}
	
	@Override
	public UserToolCategory getUserToolCategory() {
		return UserToolCategory.system;
	}

	@Override
	public String getUniqueExtensionID() {
		return "org.olat.home.HomeMainController:org.olat.gui.control.PrintUserToolExtension";
	}

	@Override
	public UserTool createUserTool(UserRequest ureq, WindowControl wControl, Locale locale) {
		return new PrintUserTool();
	}
}
