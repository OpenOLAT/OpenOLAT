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

import static org.olat.modules.catalog.ui.CatalogLauncherRepositoryEntriesController.PREFERRED_NUMBER_CARDS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.ui.CatalogEntryState;
import org.olat.modules.catalog.ui.CatalogLauncherRepositoryEntriesController;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.catalog.ui.admin.CatalogLauncherStaticEditController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class StaticHandler implements CatalogLauncherHandler {
	
	public static final String TYPE = "static";
	private static final String SEPARTOR = ",";
	
	@Autowired
	private RepositoryService repositoryService;

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
		return 120;
	}

	@Override
	public boolean isMultiInstance() {
		return true;
	}

	@Override
	public String getTypeI18nKey() {
		return "launcher.static.type";
	}

	@Override
	public String getAddI18nKey() {
		return "launcher.static.add";
	}

	@Override
	public String getEditI18nKey() {
		return "launcher.static.edit";
	}

	@Override
	public String getDetails(Translator translator, CatalogLauncher catalogLauncher) {
		List<RepositoryEntry> repositoryEntries = getRepositoryEntries(catalogLauncher);
		return translator.translate("launcher.static.num.resources", String.valueOf(repositoryEntries.size()));
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, CatalogLauncher catalogLauncher) {
		return new CatalogLauncherStaticEditController(ureq, wControl, this, catalogLauncher);
	}

	@Override
	public Controller createRunController(UserRequest ureq, WindowControl wControl, Translator translator,
			CatalogLauncher catalogLauncher, List<CatalogEntry> catalogEntries, boolean webPublish) {
		Map<Long, CatalogEntry> keyToRe = catalogEntries.stream()
				.filter(entry -> entry.getRepositoryEntryKey() != null)
				.collect(Collectors.toMap(CatalogEntry::getRepositoryEntryKey, Function.identity()));

		List<Long> reKeys = getRepositoryEntryKeys(catalogLauncher);
		
		// Keep sort order
		List<CatalogEntry> launcherEntries = reKeys.stream()
				.map(key -> keyToRe.get(key))
				.filter(Objects::nonNull)
				.limit(PREFERRED_NUMBER_CARDS)
				.collect(Collectors.toList());
		if (launcherEntries.isEmpty()) {
			return null;
		}
		
		String launcherName = CatalogV2UIFactory.translateLauncherName(translator, this, catalogLauncher);
		CatalogEntryState state = new CatalogEntryState();
		state.setSpecialFilterRepositoryEntryLabel(launcherName);
		state.setSpecialFilterRepositoryEntryKeys(reKeys);
		return new CatalogLauncherRepositoryEntriesController(ureq, wControl, launcherEntries, launcherName, true,
				webPublish, state);
	}

	public List<RepositoryEntry> getRepositoryEntries(CatalogLauncher catalogLauncher) {
		List<Long> reKeys = getRepositoryEntryKeys(catalogLauncher);
		Map<Long, RepositoryEntry> keyToRe = repositoryService.loadByKeys(reKeys).stream()
				.collect(Collectors.toMap(RepositoryEntry::getKey, Function.identity()));
		
		// Keep sort order
		List<RepositoryEntry> entries = reKeys.stream()
				.map(key -> keyToRe.get(key))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		return entries;
	}

	private List<Long> getRepositoryEntryKeys(CatalogLauncher catalogLauncher) {
		if (catalogLauncher == null || !StringHelper.containsNonWhitespace(catalogLauncher.getConfig())) {
			return new ArrayList<>(1);
		}
		
		List<Long> reKeys = Arrays.stream(catalogLauncher.getConfig().split(SEPARTOR))
				.filter(StringHelper::isLong)
				.map(Long::valueOf)
				.collect(Collectors.toList());
		return reKeys;
	}

	public String getConfig(List<RepositoryEntry> repositoryEntries) {
		return !repositoryEntries.isEmpty()
				? repositoryEntries.stream().map(re -> re.getKey().toString()).collect(Collectors.joining(SEPARTOR))
				: null;
	}

}
