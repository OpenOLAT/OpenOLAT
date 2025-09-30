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
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeModelBuilder;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.tree.TreeHelper;
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
	private List<TaxonomyLevelOption> options;
	
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
		String title = selectedLevels.stream()
				.map(level -> createLevelDisplayTitle(createTitle(level)))
				.filter(Objects::nonNull)
				.sorted(collator)
				.collect(Collectors.joining());
		title = createDisplayTitle(title);
		
		String ariaTitle = selectedLevels.stream()
				.map(this::createTitle)
				.filter(Objects::nonNull)
				.sorted(collator)
				.collect(Collectors.joining(", "));
		
		return new ObjectDisplayValues(title, EscapeMode.none, ariaTitle, ariaTitleLabel);
	}

	@Override
	public ObjectDisplayValues getDisplayValue(Collection<String> keys) {
		initOptions();
		
		String title = options.stream()
				.filter(option -> keys.contains(option.getKey()))
				.map(TaxonomyLevelOption::getDisplayTitle)
				.filter(Objects::nonNull)
				.sorted(collator)
				.collect(Collectors.joining());
		title = createDisplayTitle(title);
		
		String ariaTitle = options.stream()
				.filter(option -> keys.contains(option.getKey()))
				.map(TaxonomyLevelOption::getAriaTitle)
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
	
	private List<TaxonomyLevelOption> toOptions(Collection<TaxonomyLevel> levels) {
		Function<TaxonomyLevel, String> keyExtractor = level -> level.getKey().toString();
		Function<TaxonomyLevel, TaxonomyLevel> parentExtractor = TaxonomyLevel::getParent;
		Function<TaxonomyLevel, GenericTreeNode> toNode = organisation -> {
			GenericTreeNode newNode = new GenericTreeNode();
			newNode.setTitle(createTitle(organisation));
			return newNode;
		};
		GenericTreeModelBuilder<TaxonomyLevel> treeModelBuilder = new GenericTreeModelBuilder<>(keyExtractor, parentExtractor, toNode);
		GenericTreeModel treeModel = treeModelBuilder.build(levels);
		
		List<TaxonomyLevelOption> options = new ArrayList<>(levels.size());
		for (TaxonomyLevel level : levels) {
			if (optionsFilter.test(level)) {
				TreeNode treeNode = treeModel.getNodeById(keyExtractor.apply(level));
				List<TreeNode> treePath = TreeHelper.getTreePath(treeNode);
				treePath = TreeHelper.getTreePath(treeNode).subList(1, treePath.size() - 1); // Do not display leading slash
				
				String title = createTitle(level);
				String subTitle = ObjectOption.createShortPath(treePath);
				String subTitleFull = ObjectOption.createFullPath(treePath);
				String displayTitle = createLevelDisplayTitle(title);
				TaxonomyLevelOption option = new TaxonomyLevelOption(keyExtractor.apply(level), title, subTitle, subTitleFull, displayTitle, title);
				
				options.add(option);
			}
		}
		
		options.sort(new PathTitleComparator());
		
		return options;
	}
	
	public String createTitle(TaxonomyLevel level) {
		return TaxonomyUIFactory.translateDisplayName(translator, level, level::getIdentifier);
	}
	
	private String createLevelDisplayTitle(String title) {
		return StringHelper.containsNonWhitespace(title)
				? "<span class=\"o_taxonomy_level_selection_tag o_tag\">" + StringHelper.escapeHtml(title) + "</span>"
				: null;
	}
	
	private String createDisplayTitle(String title) {
		return StringHelper.containsNonWhitespace(title)
				? "<span class=\"o_taxonomy_level_selection_tags o_taxonomy_tags\">" + title + "</span>"
				: null;
	}
	
	private static final class TaxonomyLevelOption extends ObjectOptionValues {
		
		private final String displayTitle;
		private final String ariaTitle;
		
		public TaxonomyLevelOption(String key, String title, String subTitle, String subTitleFull, String displayTitle, String ariaTitle) {
			super(key, title, subTitle, subTitleFull);
			this.displayTitle = displayTitle;
			this.ariaTitle = ariaTitle;
		}
		
		public String getDisplayTitle() {
			return displayTitle;
		}

		public String getAriaTitle() {
			return ariaTitle;
		}
		
	}
	
	private final static class PathTitleComparator implements Comparator<TaxonomyLevelOption> {
		
		@Override
		public int compare(TaxonomyLevelOption o1, TaxonomyLevelOption o2) {
			
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
	public ControllerCreator getBrowserCreator() {
		return (UserRequest lureq, WindowControl lwControl) -> 
				new CompetenceBrowserController(lureq, lwControl, allTaxonomies, allTaxonomyLevels, true, browserTableHeader);
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
