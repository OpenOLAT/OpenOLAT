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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.ui.CatalogLauncherTaxonomyController;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.catalog.ui.admin.CatalogLauncherTaxonomyEditController;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 8 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyLevelLauncherHandler implements CatalogLauncherHandler {
	
	public static final String TYPE = "taxonomy.level";
	
	private static final XStream configXstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] { Config.class };
		configXstream.addPermission(new ExplicitTypePermission(types));
		configXstream.alias("config", Config.class);
	}
	
	@Autowired
	private CatalogV2Service catalogService;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private RepositoryModule repositoryModule;
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
		return taxonomyModule.isEnabled() && !repositoryModule.getTaxonomyRefs().isEmpty();
	}

	@Override
	public int getSortOrder() {
		return 500;
	}

	@Override
	public boolean isMultiInstance() {
		return true;
	}

	@Override
	public String getTypeI18nKey() {
		return "launcher.taxonomy.level.type";
	}

	@Override
	public String getAddI18nKey() {
		return "launcher.taxonomy.level.add";
	}

	@Override
	public String getEditI18nKey() {
		return "launcher.taxonomy.level.edit";
	}

	@Override
	public String getDetails(Translator translator, CatalogLauncher catalogLauncher) {
		Translator repositoyTranslator = Util.createPackageTranslator(RepositoryService.class, translator.getLocale());
		StringBuilder sb = new StringBuilder();
		
		Config config = fromXML(catalogLauncher.getConfig());
		
		Taxonomy taxonomy = getTaxonomy(config);
		if (taxonomy != null) {
			sb.append(translator.translate("admin.taxonomy.levels.list", taxonomy.getDisplayName()));
		} else {
			TaxonomyLevel taxonomyLevel = getTaxonomyLevel(config);
			if (taxonomyLevel != null) {
				sb.append(translator.translate("admin.taxonomy.levels.list", TaxonomyUIFactory.translateDisplayName(translator, taxonomyLevel)));
			} else {
				sb.append("-");
			}
		}
		
		if (config.getEducationalTypeKeys() != null && !config.getEducationalTypeKeys().isEmpty()) {
			String educationalTypes = repositoryManager.getAllEducationalTypes().stream()
					.filter(type -> config.getEducationalTypeKeys().contains(type.getKey()))
					.map(type -> repositoyTranslator.translate(RepositoyUIFactory.getI18nKey(type)))
					.sorted()
					.collect(Collectors.joining(", "));
			if (StringHelper.containsNonWhitespace(educationalTypes)) {
				sb.append("<br>");
				sb.append(translator.translate("admin.educational.types.list", educationalTypes));
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
				sb.append("<br>");
				sb.append(translator.translate("admin.resource.types.list", types));
			}
		}
		
		return sb.toString();
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, CatalogLauncher catalogLauncher) {
		return new CatalogLauncherTaxonomyEditController(ureq, wControl, this, catalogLauncher);
	}

	@Override
	public Controller createRunController(UserRequest ureq, WindowControl wControl, Translator translator,
			CatalogLauncher catalogLauncher, CatalogRepositoryEntrySearchParams defaultSearchParams) {
		Config config = fromXML(catalogLauncher.getConfig());
		
		List<TaxonomyLevel> taxonomyLevels = getChildren(config);
		catalogService.excludeLevelsWithoutOffers(taxonomyLevels, defaultSearchParams);
		if (taxonomyLevels == null) return null;
		
		taxonomyLevels.sort(CatalogV2UIFactory.getTaxonomyLevelComparator(translator));
		String launcherName = CatalogV2UIFactory.translateLauncherName(translator, this, catalogLauncher);

		return new CatalogLauncherTaxonomyController(ureq, wControl, launcherName, taxonomyLevels,
				config.getEducationalTypeKeys(), config.getResourceTypes());
	}

	private List<TaxonomyLevel> getChildren(Config config) {
		Taxonomy taxonomy = getTaxonomy(config);
		if (taxonomy != null) {
			return taxonomyLevelDao.getChildren(taxonomy);
		}
		
		TaxonomyLevel taxonomyLevel = getTaxonomyLevel(config);
		if (taxonomyLevel != null) {
			return taxonomyLevelDao.getChildren(taxonomyLevel);
		}
		
		return null;
	}

	/**
	 * @return the list of TaxonomyLevel from the second to most level to the TaxonomyLevel of the key (if found)
	 *         and the belonging restrictions.
	 */
	public Levels getTaxonomyLevels(CatalogLauncher catalogLauncher, Long key, CatalogRepositoryEntrySearchParams searchParams) {
		Config config = fromXML(catalogLauncher.getConfig());
		TaxonomyLevel configTaxonomyLevel = null;
		List<TaxonomyLevel> descendants = null;
		Taxonomy taxonomy = getTaxonomy(config);
		if (taxonomy != null) {
			descendants = taxonomyLevelDao.getLevels(Collections.singletonList(taxonomy));
		} else {
			configTaxonomyLevel = getTaxonomyLevel(config);
			if (configTaxonomyLevel != null) {
				descendants =  taxonomyLevelDao.getDescendants(configTaxonomyLevel, null);
			}
		}
		catalogService.excludeLevelsWithoutOffers(descendants, searchParams);
		if (descendants == null) return null;
		
		Optional<TaxonomyLevel> taxonomyLevel = descendants.stream()
			.filter(level -> key.equals(level.getKey()))
			.findFirst();
		if (taxonomyLevel.isEmpty()) {
			return null;
		}
		List<TaxonomyLevel> taxonomyLevels = new ArrayList<>();
		addParent(taxonomyLevels, taxonomyLevel.get(), configTaxonomyLevel);
		Collections.reverse(taxonomyLevels);
		return new Levels(taxonomyLevels, config.getEducationalTypeKeys(), config.getResourceTypes());
	}

	private void addParent(List<TaxonomyLevel> taxonomyLevels, TaxonomyLevel taxonomyLevel, TaxonomyLevel configTaxonomyLevel) {
		if (taxonomyLevel != configTaxonomyLevel) {
			taxonomyLevels.add(taxonomyLevel);
			addParent(taxonomyLevels, taxonomyLevel.getParent(), configTaxonomyLevel);
		}
	}

	private Taxonomy getTaxonomy(Config config) {
		if (config != null && config.getTaxonomyKey() != null) {
			return taxonomyService.getTaxonomy(() -> config.getTaxonomyKey());
		}
		return null;
	}

	private TaxonomyLevel getTaxonomyLevel(Config config) {
		if (config != null && config.getTaxonomyLevelKey() != null) {
			return taxonomyService.getTaxonomyLevel(() -> config.getTaxonomyLevelKey());
		}
		return null;
	}

	public Config fromXML(String xml) {
		try {
			return (Config)configXstream.fromXML(xml);
		} catch (XStreamException se) {
			return getConfigFromString(xml);
		}
	}
	
	public Config getConfigFromString(String configStr) {
		if (StringHelper.containsNonWhitespace(configStr)) {
			if (configStr.startsWith("Taxonomy::")) {
				String taxonomyKeyStr = configStr.substring("Taxonomy::".length());
				if (StringHelper.isLong(taxonomyKeyStr)) {
					Config config = new Config();
					config.setTaxonomyKey(Long.valueOf(taxonomyKeyStr));
					return config;
				}
			} else if (StringHelper.isLong(configStr)) {
				Config config = new Config();
				config.setTaxonomyLevelKey(Long.valueOf(configStr));
				return config;
			}
		}
		
		return null;
	}

	public String toXML(Config config) {
		return configXstream.toXML(config);
	}
	
	public static final class Config {
		
		private Long taxonomyKey;
		private Long taxonomyLevelKey;
		private Collection<Long> educationalTypeKeys;
		private Collection<String> resourceTypes;
		
		public Long getTaxonomyKey() {
			return taxonomyKey;
		}
		
		public void setTaxonomyKey(Long taxonomyKey) {
			this.taxonomyKey = taxonomyKey;
		}
		
		public Long getTaxonomyLevelKey() {
			return taxonomyLevelKey;
		}
		
		public void setTaxonomyLevelKey(Long taxonomyLevelKey) {
			this.taxonomyLevelKey = taxonomyLevelKey;
		}
		
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
	
	public static final class Levels {
		
		private final List<TaxonomyLevel> taxonomyLevels;
		private final Collection<Long> educationalTypeKeys;
		private final Collection<String> resourceTypes;
		
		public Levels(List<TaxonomyLevel> taxonomyLevels, Collection<Long> educationalTypeKeys, Collection<String> resourceTypes) {
			this.taxonomyLevels = taxonomyLevels;
			this.educationalTypeKeys = educationalTypeKeys;
			this.resourceTypes = resourceTypes;
		}

		public List<TaxonomyLevel> getTaxonomyLevels() {
			return taxonomyLevels;
		}

		public Collection<Long> getEducationalTypeKeys() {
			return educationalTypeKeys;
		}

		public Collection<String> getResourceTypes() {
			return resourceTypes;
		}
		
	}

}
