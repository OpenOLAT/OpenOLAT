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
package org.olat.repository.manager;

import java.util.Locale;

import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryModule;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 20.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class RepositoryEntryLicenseHandler implements LicenseHandler {

	@Override
	public String getType() {
		return "repository";
	}

	@Override
	public String getTitle(Locale locale) {
		Translator translator = Util.createPackageTranslator(RepositoryModule.class, locale);
		return translator.translate("license.admin.title");
	}

}
