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

import static org.olat.modules.catalog.ui.CatalogLauncherCatalogEntryController.PREFERRED_NUMBER_CARDS;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.ui.CatalogLauncherCatalogEntryController;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.catalog.ui.admin.CatalogLauncherRandomEditController;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RandomHandler implements CatalogLauncherHandler {
	
	private static final String TYPE = "random";
	
	private static final XStream configXstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] { Config.class };
		configXstream.addPermission(new ExplicitTypePermission(types));
		configXstream.alias("config", Config.class);
	}
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
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
		return 180;
	}

	@Override
	public boolean isMultiInstance() {
		return false;
	}

	@Override
	public String getTypeI18nKey() {
		return "launcher.random.type";
	}

	@Override
	public String getAddI18nKey() {
		return "launcher.random.add";
	}

	@Override
	public String getEditI18nKey() {
		return "launcher.random.edit";
	}

	@Override
	public String getDetails(Translator translator, CatalogLauncher catalogLauncher) {
		Translator repositoyTranslator = Util.createPackageTranslator(RepositoryService.class, translator.getLocale());
		StringBuilder sb = new StringBuilder();
		boolean lineBrake = false;
		
		Config config = fromXML(catalogLauncher.getConfig());
		if (config.getEducationalTypeKeys() != null && !config.getEducationalTypeKeys().isEmpty()) {
			String educationalTypes = repositoryManager.getAllEducationalTypes().stream()
					.filter(type -> config.getEducationalTypeKeys().contains(type.getKey()))
					.map(type -> StringHelper.escapeHtml(repositoyTranslator.translate(RepositoyUIFactory.getI18nKey(type))))
					.sorted()
					.collect(Collectors.joining(", "));
			if (StringHelper.containsNonWhitespace(educationalTypes)) {
				sb.append(translator.translate("admin.educational.types.list", educationalTypes));
				lineBrake = true;
			}
		}
		
		if (config.getResourceTypes() != null && !config.getResourceTypes().isEmpty()) {
			String types = repositoryHandlerFactory.getOrderRepositoryHandlers().stream()
					.map(handler -> handler.getHandler().getSupportedType())
					.filter(type -> config.getResourceTypes().contains(type))
					.sorted((t1, t2) -> repositoyTranslator.translate(t1).compareTo(repositoyTranslator.translate(t2)))
					.map(type -> "<i class=\"" + "o_icon o_icon-fw ".concat(RepositoyUIFactory.getIconCssClass(type)) + "\"> </i>" + repositoyTranslator.translate(type))
					.collect(Collectors.joining(", "));
			if (StringHelper.containsNonWhitespace(types)) {
				if (lineBrake) sb.append("<br>");
				sb.append(translator.translate("admin.resource.types.list", types));
			}
		}
		
		return sb.toString();
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, CatalogLauncher catalogLauncher) {
		return new CatalogLauncherRandomEditController(ureq, wControl, this, catalogLauncher);
	}

	@Override
	public Controller createRunController(UserRequest ureq, WindowControl wControl, Translator translator,
			CatalogLauncher catalogLauncher, List<CatalogEntry> catalogEntries, boolean webPublish) {
		Config config = fromXML(catalogLauncher.getConfig());
		Predicate<? super CatalogEntry> resourceTypeFilter = createResourceTypeFilter(config);
		Predicate<? super CatalogEntry> educationalTypeFilter = createEducationalTypeFilter(config);
		
		List<CatalogEntry> launcherEntries = catalogEntries.stream()
				.filter(entry -> entry.getRepositoryEntryKey() != null)
				.collect(Collectors.toList());
		
		Collections.shuffle(launcherEntries);
		launcherEntries = launcherEntries.stream()
				.filter(resourceTypeFilter)
				.filter(educationalTypeFilter)
				.limit(PREFERRED_NUMBER_CARDS)
				.toList();
		if (launcherEntries.isEmpty()) {
			return null;
		}
		
		String launcherName = CatalogV2UIFactory.translateLauncherName(translator, this, catalogLauncher);
		return new CatalogLauncherCatalogEntryController(ureq, wControl, launcherEntries, launcherName, false,
				webPublish, null);
	}
	
	private Predicate<? super CatalogEntry> createResourceTypeFilter(Config config) {
		if (config.getResourceTypes() == null || config.getResourceTypes().isEmpty()) {
			return entry -> true;
		}
		
		return entry -> config.getResourceTypes().contains(entry.getOlatResource().getResourceableTypeName());
	}
	
	private Predicate<? super CatalogEntry> createEducationalTypeFilter(Config config) {
		if (config.getEducationalTypeKeys() == null || config.getEducationalTypeKeys().isEmpty()) {
			return entry -> true;
		}
		
		return entry -> entry.getEducationalType() != null
				&& config.getEducationalTypeKeys().contains(entry.getEducationalType().getKey());
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
		
		private Collection<Long> educationalTypeKeys;
		private Collection<String> resourceTypes;
		
		public Collection<Long> getEducationalTypeKeys() {
			return educationalTypeKeys;
		}
		
		public void setEducationalTypeKeys(Collection<Long> educationalTypeKeys) {
			this.educationalTypeKeys = educationalTypeKeys;
		}
		
		public Collection<String> getResourceTypes() {
			return resourceTypes;
		}
		
		public void setResourceTypes(Collection<String> resourceTypes) {
			this.resourceTypes = resourceTypes;
		}
		
	}

}
