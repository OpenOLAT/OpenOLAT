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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.olat.modules.catalog.ui.CatalogLauncherCatalogEntryController;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.catalog.ui.admin.CatalogLauncherStaticCurriculumElementEditController;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 Mar 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class StaticCurriculumElementHandler implements CatalogLauncherHandler {
	
	public static final String TYPE = "staticcurriculumelement";
	private static final String SEPARTOR = ",";
	
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isEnabled() {
		return curriculumModule.isEnabled();
	}

	@Override
	public int getSortOrder() {
		return 122;
	}

	@Override
	public boolean isMultiInstance() {
		return true;
	}

	@Override
	public String getTypeI18nKey() {
		return "launcher.staticce.type";
	}

	@Override
	public String getAddI18nKey() {
		return "launcher.staticce.add";
	}

	@Override
	public String getEditI18nKey() {
		return "launcher.staticce.edit";
	}

	@Override
	public String getDetails(Translator translator, CatalogLauncher catalogLauncher) {
		List<CurriculumElement> curriculumElements = getCurriculumElements(catalogLauncher);
		return translator.translate("launcher.staticce.num", String.valueOf(curriculumElements.size()));
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, CatalogLauncher catalogLauncher) {
		return new CatalogLauncherStaticCurriculumElementEditController(ureq, wControl, this, catalogLauncher);
	}

	@Override
	public Controller createRunController(UserRequest ureq, WindowControl wControl, Translator translator,
			CatalogLauncher catalogLauncher, List<CatalogEntry> catalogEntries, boolean webPublish) {
		Map<Long, CatalogEntry> keyToCe = catalogEntries.stream()
				.filter(entry -> entry.getCurriculumElementKey() != null)
				.collect(Collectors.toMap(CatalogEntry::getCurriculumElementKey, Function.identity()));

		List<Long> ceKeys = getCurriculumElementKeys(catalogLauncher);
		
		// Keep sort order
		List<CatalogEntry> launcherEntries = ceKeys.stream()
				.map(key -> keyToCe.get(key))
				.filter(Objects::nonNull)
				.limit(PREFERRED_NUMBER_CARDS)
				.collect(Collectors.toList());
		if (launcherEntries.isEmpty()) {
			return null;
		}
		
		String launcherName = CatalogV2UIFactory.translateLauncherName(translator, this, catalogLauncher);

		Collection<Long> resourceKeys = ceKeys.stream()
				.map(key -> keyToCe.get(key))
				.filter(Objects::nonNull)
				.map(ce -> ce.getOlatResource().getKey())
				.toList();
		CatalogEntryState state = new CatalogEntryState();
		state.setSpecialFilterLabel(launcherName);
		state.setSpecialFilterResourceKeys(resourceKeys);
		return new CatalogLauncherCatalogEntryController(ureq, wControl, launcherEntries, launcherName, true,
				webPublish, state);
	}

	public List<CurriculumElement> getCurriculumElements(CatalogLauncher catalogLauncher) {
		List<Long> ceKeys = getCurriculumElementKeys(catalogLauncher);
		List<CurriculumElementRefImpl> ceRefs = ceKeys.stream().map(CurriculumElementRefImpl::new).toList();
		Map<Long, CurriculumElement> keyToRe = curriculumService.getCurriculumElements(ceRefs).stream()
				.collect(Collectors.toMap(CurriculumElement::getKey, Function.identity()));
		
		// Keep sort order
		List<CurriculumElement> elements = ceKeys.stream()
				.map(key -> keyToRe.get(key))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		return elements;
	}

	private List<Long> getCurriculumElementKeys(CatalogLauncher catalogLauncher) {
		if (catalogLauncher == null || !StringHelper.containsNonWhitespace(catalogLauncher.getConfig())) {
			return new ArrayList<>(1);
		}
		
		List<Long> reKeys = Arrays.stream(catalogLauncher.getConfig().split(SEPARTOR))
				.filter(StringHelper::isLong)
				.map(Long::valueOf)
				.collect(Collectors.toList());
		return reKeys;
	}

	public String getConfig(List<CurriculumElement> curriculumElements) {
		return !curriculumElements.isEmpty()
				? curriculumElements.stream().map(ce -> ce.getKey().toString()).collect(Collectors.joining(SEPARTOR))
				: null;
	}

}
