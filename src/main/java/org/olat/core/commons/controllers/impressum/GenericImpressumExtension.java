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
package org.olat.core.commons.controllers.impressum;

import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.vfs.LocalFolderImpl;

/* 
 * Initial date: 12 Apr 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 */
public class GenericImpressumExtension extends GenericActionExtension {

	protected final ImpressumModule impressumModule;
	protected final I18nModule i18nModule;
	private static final String INDEX_HTML = "index_%s.html";

	public GenericImpressumExtension(ImpressumModule impressumModule, I18nModule i18nModule) {
		this.impressumModule = impressumModule;
		this.i18nModule = i18nModule;
	}

	protected boolean isModuleEnabled(LocalFolderImpl baseFolder, UserRequest ureq) {
		boolean enabled = false;
		
		if (baseFolder.isSafeHtmlFile(String.format(INDEX_HTML, ureq.getLocale().getLanguage()))) {
			enabled = true;
		} else if (baseFolder.isSafeHtmlFile(String.format(INDEX_HTML, I18nModule.getDefaultLocale().getLanguage()))) {
			enabled = true;
		} else {
			for (String locale : i18nModule.getEnabledLanguageKeys()) {
				if (baseFolder.isSafeHtmlFile(String.format(INDEX_HTML, locale))) {
					enabled = true;
					break;
				}
			}
		}
		return enabled;
	}
}
