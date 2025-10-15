/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.taxonomy.ui.component;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectDisplayValues;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption.ObjectOptionValues;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionSource;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.modules.taxonomy.ui.CompetenceBrowserController;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

/**
 * 
 * Initial date: Sep 24, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TaxonomyLevelSelectionSource implements ObjectSelectionSource {
	
	private final Translator translator;
	private final Collator collator;
	private final Collection<TaxonomyLevel> selectedLevels;
	private final Supplier<Collection<TaxonomyLevel>> levelsSupplier;
	private String ariaTitleLabel;
	private final String optionsLabel;
	private final String browserTableHeader;
	private Predicate<TaxonomyLevel> optionsFilter = level -> true;
	private Collection<TaxonomyLevel> allTaxonomyLevels;
	private List<Taxonomy> allTaxonomies;
	private List<ObjectOptionValues> options;
	
	public TaxonomyLevelSelectionSource(Locale locale, Collection<TaxonomyLevel> selectedLevels,
			Supplier<Collection<TaxonomyLevel>> levelsSupplier, String optionsLabel, String browserTableHeader) {
		translator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
		collator = Collator.getInstance(locale);
		
		this.selectedLevels = selectedLevels;
		this.levelsSupplier = levelsSupplier;
		this.optionsLabel = optionsLabel;
		this.browserTableHeader = browserTableHeader;
	}

	public void setAriaTitleLabel(String ariaTitleLabel) {
		this.ariaTitleLabel = ariaTitleLabel;
	}

	public void setOptionsFilter(Predicate<TaxonomyLevel> optionsFilter) {
		this.optionsFilter = optionsFilter;
		this.options = null;
	}

	@Override
	public ObjectDisplayValues getDefaultDisplayValue() {
		List<String> titles = selectedLevels.stream()
				.map(this::getDisplayName)
				.toList();
		String title = TaxonomyUIFactory.getTags(translator.getLocale(), titles);
		title = wrapTagsCss(title);
		
		String ariaTitle = selectedLevels.stream()
				.map(this::getDisplayName)
				.filter(Objects::nonNull)
				.sorted(collator)
				.collect(Collectors.joining(", "));
		
		return new ObjectDisplayValues(title, EscapeMode.none, ariaTitle, ariaTitleLabel);
	}

	@Override
	public ObjectDisplayValues getDisplayValue(Collection<String> keys) {
		initOptions();
		
		List<String> titles = options.stream()
				.filter(option -> keys.contains(option.getKey()))
				.map(ObjectOptionValues::getTitle)
				.toList();
		String title = TaxonomyUIFactory.getTags(translator.getLocale(), titles);
		title = wrapTagsCss(title);
		
		String ariaTitle = options.stream()
				.filter(option -> keys.contains(option.getKey()))
				.map(ObjectOptionValues::getTitle)
				.filter(Objects::nonNull)
				.sorted(collator)
				.collect(Collectors.joining(", "));
		
		return new ObjectDisplayValues(title, EscapeMode.none, ariaTitle, ariaTitleLabel);
	}

	@Override
	public Collection<String> getDefaultSelectedKeys() {
		return selectedLevels.stream().map(level -> level.getKey().toString()).toList();
	}
	
	@Override
	public final String getOptionsLabel(Locale locale) {
		return optionsLabel;
	}
	
	@Override
	public List<? extends ObjectOption> getOptions() {
		initOptions();
		return options;
	}
	
	private void initOptions() {
		if (options != null) {
			return;
		}
		
		allTaxonomyLevels = levelsSupplier.get();
		allTaxonomies = allTaxonomyLevels.stream().map(TaxonomyLevel::getTaxonomy).distinct().collect(Collectors.toList());
		
		Collection<TaxonomyLevel> allLevels = new HashSet<>(allTaxonomyLevels);
		
		Set<TaxonomyLevel> additionalLevels = new HashSet<>(selectedLevels);
		additionalLevels.removeAll(allLevels);
		allLevels.addAll(additionalLevels);
		
		options = toOptions(allLevels);
	}
	
	private List<ObjectOptionValues> toOptions(Collection<TaxonomyLevel> levels) {
		List<ObjectOptionValues> options = new ArrayList<>(levels.size());
		for (TaxonomyLevel level : levels) {
			if (optionsFilter.test(level)) {
				String title = getDisplayName(level);
				
				List<String> displayNamePath = getDisplayNamePath(level);
				String subTitle = ObjectOption.createShortPath(displayNamePath, Function.identity());
				String subTitleFull = ObjectOption.createFullPath(displayNamePath, Function.identity());
				
				ObjectOptionValues option = new ObjectOptionValues(level.getKey().toString(), title, subTitle, subTitleFull);
				
				options.add(option);
			}
		}
		
		options.sort(new PathTitleComparator());
		
		return options;
	}
	
	public List<String> getDisplayNamePath(TaxonomyLevel taxonomyLevel) {
		List<String> displayNamePath = new ArrayList<>();
		addParent(displayNamePath, taxonomyLevel);
		Collections.reverse(displayNamePath);
		return displayNamePath;
	}
	
	private void addParent(List<String> displayNamePath, TaxonomyLevel taxonomyLevel) {
		TaxonomyLevel parent = taxonomyLevel.getParent();
		if (parent != null) {
			displayNamePath.add(getDisplayName(parent));
			addParent(displayNamePath, parent);
		}
	}
	
	private String getDisplayName(TaxonomyLevel level) {
		return TaxonomyUIFactory.translateDisplayName(translator, level, level::getIdentifier);
	}
	
	private String wrapTagsCss(String tags) {
		return StringHelper.containsNonWhitespace(tags)
				? "<span class=\"o_taxonomy_selection_tags\">" + tags + "</span>"
				: null;
	}
	
	private final static class PathTitleComparator implements Comparator<ObjectOptionValues> {
		
		@Override
		public int compare(ObjectOptionValues o1, ObjectOptionValues o2) {
			
			// Use title fallback, otherwise all root options will be at the top of the list.
			String subTitleFull1 = StringHelper.containsNonWhitespace(o1.getSubTitleFull())? o1.getSubTitleFull(): o1.getTitle();
			String subTitleFull2 = StringHelper.containsNonWhitespace(o2.getSubTitleFull())? o2.getSubTitleFull(): o2.getTitle();
			
			int c = subTitleFull1.compareToIgnoreCase(subTitleFull2);
			if (c == 0) {
				c = o1.getTitle().compareToIgnoreCase(o2.getTitle());
			}
			return c;
		}
		
	}

	@Override
	public boolean isBrowserAvailable() {
		return true;
	}

	@Override
	public ControllerCreator getBrowserCreator(boolean multiSelection) {
		return (UserRequest lureq, WindowControl lwControl) -> 
				new CompetenceBrowserController(lureq, lwControl, allTaxonomies, allTaxonomyLevels, true, multiSelection, browserTableHeader);
	}
	
	public static final TaxonomyLevelRef toRef(String key) {
		return new TaxonomyLevelRefImpl(Long.valueOf(key));
	}
	
	public static final Set<TaxonomyLevelRef> toRefs(Collection<String> keys) {
		return keys.stream()
				.map(key -> (TaxonomyLevelRef)new TaxonomyLevelRefImpl(Long.valueOf(key)))
				.collect(Collectors.toSet());
	}

}
