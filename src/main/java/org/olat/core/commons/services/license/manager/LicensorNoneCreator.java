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
package org.olat.core.commons.services.license.manager;

import java.util.Locale;

import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.commons.services.license.ui.LicenseAdminConfigController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 28.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
class LicensorNoneCreator implements LicensorCreator {

	static final String NONE_CREATOR_TYPE = "none";

	@Override
	public String getType() {
		return NONE_CREATOR_TYPE;
	}

	@Override
	public String getName(Locale locale) {
		Translator translator = Util.createPackageTranslator(LicenseAdminConfigController.class, locale);
		return translator.translate("licensor.creator.none");
	}

	@Override
	public int getSortOrder() {
		return 100;
	}

	@Override
	public String create(LicenseHandler handler, Identity identity) {
		return null;
	}

}
