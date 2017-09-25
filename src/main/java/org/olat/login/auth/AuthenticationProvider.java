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
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.login.auth;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.StartupException;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Value;

/**
 * Initial Date:  04.08.2004
 *
 * @author Mike Stock<br>
 * Comment:
 * An authentication provider authenticates users. It is initialized with the providerConfig.
 */

public class AuthenticationProvider implements ControllerCreator{

	private String name;
	private String clazz;
	private String iconCssClass;
	private boolean enabled;
	private boolean isDefault;
	@Value("${instance.issuer.identifier:null}")
	private String issuerIdentifier;


	/**
	 * [used by spring]
	 * Authentication provider implementation. Gets its config from spring config file.
	 * @param providerConfig
	 */
	protected AuthenticationProvider(String name, String clazz, boolean enabled, boolean isDefault, String iconCssClass) {
		this.name = name;
		this.clazz = clazz;
		this.enabled = enabled;
		this.isDefault = isDefault;
		this.iconCssClass = iconCssClass;
		// double check config
		if (name == null || clazz == null) {
			throw new StartupException("Invalid AuthProvider: " + name + ". Please fix!");
		}
	}

	/**
	 * @return True if this auth provider is enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @return True if this auth provider is the default provider
	 */
	public boolean isDefault() {
		return isDefault;
	}

	public boolean accept(@SuppressWarnings("unused") String subProviderName) {
		return false;
	}

	/**
	 * @return Name used to identify this authprovider.
	 */
	public String getName() {
		return name;
	}

	public String getIconCssClass() {
		return iconCssClass;
	}

	/**
	 * [used by velocity]
	 * @param language
	 * @return Description text.
	 */
	public String getDescription(Locale locale) {
		Translator trans = getPackageTranslatorForLocale(locale);
		String desc = trans.translate("authentication.provider.description");
		return desc;
	}

	/**
	 * [used by velocity]
	 * @param language
	 * @return Link text used to display a link to switch to this authentication provider.
	 */
	public String getLinktext(Locale locale) {
		Translator trans = getPackageTranslatorForLocale(locale);
		String text = trans.translate("authentication.provider.linkText");
		return text;
	}

	@Override
	public Controller createController(UserRequest lureq, WindowControl lwControl) {
		AutoCreator ac = new AutoCreator();
		ac.setClassName(clazz);
		return ac.createController(lureq, lwControl);
	}

	/**
	 * @param locale
	 * @return a translator for the package matching the authenticationProvider
	 */
	private Translator getPackageTranslatorForLocale (Locale locale) {
		Class<?> authProvClass = null;
		try {
			authProvClass = Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			throw new OLATRuntimeException("classname::" + clazz + " does no exist", e);
		}
		return Util.createPackageTranslator(authProvClass, locale);
	}

	/**
	 * The issuer is the entity that issues a set of claims. An issuer
	 * identifier is a case sensitive URL using the https scheme that contains
	 * scheme, host, and optionally, port number and path components and no
	 * query or fragment components.
	 * <p>
	 * The default implementation returns the property
	 * instance.issuer.identifier.
	 *
	 * @param identityEnvironment
	 * @return the issuer identifier
	 */
	public String getIssuerIdentifier(IdentityEnvironment identityEnvironment) {
		return issuerIdentifier;
	}

}
