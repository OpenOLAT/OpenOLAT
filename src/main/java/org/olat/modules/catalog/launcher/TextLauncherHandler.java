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
package org.olat.modules.catalog.launcher;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.ui.CatalogLauncherTextController;
import org.olat.modules.catalog.ui.admin.CatalogLauncherTextEditController;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 8 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TextLauncherHandler implements CatalogLauncherHandler {
	
	public static final String I18N_PREFIX = "launcher.text.text.id";
	public static final String TYPE = "text";
	
	private static final XStream configXstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] { Config.class };
		configXstream.addPermission(new ExplicitTypePermission(types));
		configXstream.alias("config", Config.class);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public int getSortOrder() {
		return 1000;
	}

	@Override
	public boolean isMultiInstance() {
		return true;
	}

	@Override
	public String getTypeI18nKey() {
		return "launcher.text.type";
	}

	@Override
	public String getAddI18nKey() {
		return "launcher.text.add";
	}

	@Override
	public String getEditI18nKey() {
		return "launcher.text.edit";
	}

	@Override
	public String getDetails(Translator translator, CatalogLauncher catalogLauncher) {
		Config config = fromXML(catalogLauncher.getConfig());
		String text = translator.translate(I18N_PREFIX + config.i18nSuffix);
		return Formatter.truncate(StringHelper.truncateText(StringHelper.xssScan(text)), 50);
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, CatalogLauncher catalogLauncher) {
		return new CatalogLauncherTextEditController(ureq, wControl, this, catalogLauncher);
	}

	@Override
	public Controller createRunController(UserRequest ureq, WindowControl wControl, Translator translator,
			CatalogLauncher catalogLauncher, CatalogRepositoryEntrySearchParams defaultSearchParams) {
		Config config = fromXML(catalogLauncher.getConfig());
		String text = translator.translate(I18N_PREFIX + config.i18nSuffix);
		return StringHelper.containsNonWhitespace(text)? new CatalogLauncherTextController(ureq, wControl, text): null;
	}
	
	public Config fromXML(String xml) {
		if (StringHelper.containsNonWhitespace(xml)) {
			return (Config)configXstream.fromXML(xml);
		}
		return new Config();
	}

	public String toXML(Config config) {
		return configXstream.toXML(config);
	}
	
	public static final class Config {
		
		private String i18nSuffix;

		public String getI18nSuffix() {
			return i18nSuffix;
		}

		public void setI18nSuffix(String i18nSuffix) {
			this.i18nSuffix = i18nSuffix;
		}
		
	}

}
