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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOptionGroup;
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
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
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
	private final String browserTableHeader;
	private Predicate<TaxonomyLevel> optionsFilter = level -> true;
	private Collection<TaxonomyLevel> allTaxonomyLevels;
	private List<Taxonomy> allTaxonomies;
	private List<ObjectOptionValues> options;
	private Map<Long, List<ObjectOptionValues>> taxonomyKeyToOptions;
	private List<ObjectOptionGroup> groups;

	public TaxonomyLevelSelectionSource(Locale locale, Collection<TaxonomyLevel> selectedLevels,
			Supplier<Collection<TaxonomyLevel>> levelsSupplier, String browserTableHeader) {
		translator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
		collator = Collator.getInstance(locale);
		
		this.selectedLevels = selectedLevels;
		this.levelsSupplier = levelsSupplier;
		this.browserTableHeader = browserTableHeader;
	}

	public void setAriaTitleLabel(String ariaTitleLabel) {
		this.ariaTitleLabel = ariaTitleLabel;
	}

	public void setOptionsFilter(Predicate<TaxonomyLevel> optionsFilter) {
		this.optionsFilter = optionsFilter;
		this.options = null;
		this.taxonomyKeyToOptions = null;
		this.groups = null;
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
	public List<ObjectOptionGroup> getOptionGroups(Locale locale) {
		initOptions();
		return groups;
	}

	private void initOptions() {
		if (options != null) {
			return;
		}

		allTaxonomyLevels = levelsSupplier.get();
		allTaxonomies = allTaxonomyLevels.stream().map(TaxonomyLevel::getTaxonomy).distinct().collect(Collectors.toList());

		Set<TaxonomyLevel> allLevels = new HashSet<>(allTaxonomyLevels);
		allLevels.addAll(selectedLevels);

		Map<Long, List<TaxonomyLevel>> levelsByTaxonomyKey = allLevels.stream()
				.filter(l -> l.getTaxonomy() != null)
				.collect(Collectors.groupingBy(l -> l.getTaxonomy().getKey()));

		options = new ArrayList<>();
		taxonomyKeyToOptions = new LinkedHashMap<>();
		for (Taxonomy taxonomy : allTaxonomies) {
			List<TaxonomyLevel> taxonomyLevels = levelsByTaxonomyKey.getOrDefault(taxonomy.getKey(), List.of());
			List<ObjectOptionValues> taxonomyOptions = toHierarchicalOptions(taxonomyLevels);
			taxonomyKeyToOptions.put(taxonomy.getKey(), taxonomyOptions);
			options.addAll(taxonomyOptions);
		}

		boolean multiTaxonomy = allTaxonomies.size() > 1;
		groups = new ArrayList<>(allTaxonomies.size());
		for (Taxonomy taxonomy : allTaxonomies) {
			String label = taxonomy.getDisplayName();
			String subLabel = multiTaxonomy && StringHelper.containsNonWhitespace(taxonomy.getIdentifier())
					? taxonomy.getIdentifier()
					: null;
			List<ObjectOptionValues> groupOptions = taxonomyKeyToOptions.getOrDefault(taxonomy.getKey(), List.of());
			groups.add(ObjectOptionGroup.of(label, subLabel, groupOptions));
		}
		groups.sort(Comparator.comparing(ObjectOptionGroup::getLabel, Comparator.nullsLast(collator)));
	}

	private List<ObjectOptionValues> toHierarchicalOptions(Collection<TaxonomyLevel> levels) {
		Set<TaxonomyLevel> visibleSet = new HashSet<>();
		for (TaxonomyLevel l : levels) {
			if (optionsFilter.test(l)) {
				visibleSet.add(l);
			}
		}
		if (visibleSet.isEmpty()) {
			return List.of();
		}

		Function<TaxonomyLevel, String> keyExtractor = l -> l.getKey().toString();
		Function<TaxonomyLevel, TaxonomyLevel> parentExtractor = l -> {
			TaxonomyLevel p = l.getParent();
			while (p != null && !visibleSet.contains(p)) {
				p = p.getParent();
			}
			return p;
		};
		Function<TaxonomyLevel, GenericTreeNode> toNode = l -> {
			GenericTreeNode n = new GenericTreeNode();
			n.setTitle(getDisplayName(l));
			n.setUserObject(l);
			return n;
		};

		Comparator<INode> siblingComparator = (n1, n2) -> {
			TaxonomyLevel l1 = (TaxonomyLevel) ((TreeNode) n1).getUserObject();
			TaxonomyLevel l2 = (TaxonomyLevel) ((TreeNode) n2).getUserObject();
			Integer s1 = l1.getSortOrder();
			Integer s2 = l2.getSortOrder();
			int c;
			if (s1 == null || s2 == null) {
				c = (s1 == null ? 1 : 0) - (s2 == null ? 1 : 0);
			} else {
				c = s1.compareTo(s2);
			}
			if (c != 0) {
				return c;
			}
			return collator.compare(getDisplayName(l1), getDisplayName(l2));
		};

		GenericTreeModelBuilder<TaxonomyLevel> builder =
				new GenericTreeModelBuilder<>(keyExtractor, parentExtractor, toNode);
		GenericTreeModel treeModel = builder.build(visibleSet, siblingComparator);

		List<ObjectOptionValues> result = new ArrayList<>();
		new TreeVisitor(node -> {
			if (node instanceof TreeNode tn && tn.getUserObject() instanceof TaxonomyLevel level) {
				String title = getDisplayName(level);
				List<String> displayNamePath = getIdentifierPath(level);
				String subTitle = ObjectOption.createFullPath(displayNamePath, Function.identity(), false);
				result.add(new ObjectOptionValues(level.getKey().toString(), title, subTitle));
			}
		}, treeModel.getRootNode(), false).visitAll();

		return result;
	}
	
	public List<String> getIdentifierPath(TaxonomyLevel taxonomyLevel) {
		List<String> displayNamePath = new ArrayList<>();
		addIdentifierAndParents(displayNamePath, taxonomyLevel);
		Collections.reverse(displayNamePath);
		return displayNamePath;
	}
	
	private void addIdentifierAndParents(List<String> displayNamePath, TaxonomyLevel taxonomyLevel) {
		String identifier = taxonomyLevel.getIdentifier();
		if (!StringHelper.containsNonWhitespace(identifier)) {
			identifier = getDisplayName(taxonomyLevel);
		}
		displayNamePath.add(identifier);
		
		TaxonomyLevel parent = taxonomyLevel.getParent();
		if (parent != null) {
			addIdentifierAndParents(displayNamePath, parent);
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
	

	@Override
	public boolean isBrowserAvailable() {
		return true;
	}

	@Override
	public ControllerCreator getBrowserCreator(boolean multiSelection, Collection<String> selectedKeys) {
		Set<Long> preselectedKeys = selectedKeys.stream()
				.map(key -> Long.valueOf(key))
				.collect(Collectors.toSet());
		return (UserRequest lureq, WindowControl lwControl) -> {
			initOptions();
			return new CompetenceBrowserController(lureq, lwControl, allTaxonomies, allTaxonomyLevels, true, multiSelection, browserTableHeader, preselectedKeys);
		};
	}

	@Override
	public void addMissingOptions(Collection<String> keys) {
		//
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
