/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * http://www.frentix.com
 * <p>
 */
package org.olat.ldap.ui;

import java.util.Locale;

import org.olat.admin.SystemAdminMainController;
import org.olat.core.extensions.AbstractExtension;
import org.olat.core.extensions.Extension;
import org.olat.core.extensions.ExtensionElement;
import org.olat.core.extensions.action.ActionExtension;
import org.olat.core.extensions.helpers.ExtensionElements;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.ldap.LDAPLoginModule;

/**
 * Description:<br>
 * The LDAP admin extension launches the LDAPAdminController to manage the LDAP
 * connection
 * 
 * <P>
 * Initial Date: 21.08.2008 <br>
 * 
 * @author gnaegi
 */
public class LDAPAdminExtension extends AbstractExtension implements Extension {
	/**
	 * @see org.olat.core.configuration.AbstractConfigOnOff#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return LDAPLoginModule.isLDAPEnabled(); 
	}

	private ExtensionElements elements = new ExtensionElements();
	/**
	 * Constructor to create an extension that registers in the admin site
	 */
	public LDAPAdminExtension() {
		elements.putExtensionElement(SystemAdminMainController.class.getName(), new ActionExtension() {

			/**
			 * @see org.olat.core.extensions.action.ActionExtension#getActionText(java.util.Locale)
			 */
			public String getActionText(Locale loc) {
				Translator transl = Util.createPackageTranslator(LDAPAdminExtension.class, loc);
				return transl.translate("admin.menu.ldap");
			}

			/**
			 * @see org.olat.core.extensions.action.ActionExtension#getDescription(java.util.Locale)
			 */
			public String getDescription(Locale loc) {
				Translator transl = Util.createPackageTranslator(LDAPAdminExtension.class, loc);
				return transl.translate("admin.menu.ldap.desc");
			}

			/**
			 * @see org.olat.core.extensions.action.ActionExtension#createController(org.olat.core.gui.UserRequest,
			 *      org.olat.core.gui.control.WindowControl, java.lang.Object)
			 */
			public Controller createController(UserRequest ureq, WindowControl control, @SuppressWarnings("unused")
			Object arg) {
				return new LDAPAdminController(ureq, control);
			}

		});
	}

	/**
	 * @see org.olat.core.extensions.Extension#getExtensionFor(java.lang.String)
	 */
	public ExtensionElement getExtensionFor(String extensionPoint) {
		if (isEnabled()) return elements.getExtensionElement(extensionPoint);
		else return null;
	}
	
	
	
}
