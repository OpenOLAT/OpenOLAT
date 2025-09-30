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
package org.olat.user.ui.organisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectDisplayValues;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption.ObjectOptionValues;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionSource;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeModelBuilder;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.tree.TreeHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Sep 10, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OrganisationSelectionSource implements ObjectSelectionSource {
	
	private final Set<String> defaultSelectedKeys;
	private final Supplier<Collection<? extends OrganisationRef>> organisationsSupplier;
	private List<OrganisationOption> options;
	
	@Autowired
	private OrganisationOptionCache optionCache;

	public OrganisationSelectionSource(Collection<? extends OrganisationRef> selectedOrganisations,
			Supplier<Collection<? extends OrganisationRef>> organisationSupplier) {
		CoreSpringFactory.autowireObject(this);
		
		this.defaultSelectedKeys = selectedOrganisations.stream()
				.map(org -> org.getKey().toString())
				.collect(Collectors.toSet());
		this.organisationsSupplier = organisationSupplier;
	}
	
	@Override
	public ObjectDisplayValues getDefaultDisplayValue() {
		String title = optionCache.getOptions(defaultSelectedKeys).stream()
				.map(OrganisationOption::getDisplayTitle)
				.sorted()
				.collect(Collectors.joining(", "));
		return new ObjectDisplayValues(title, title);
	}

	@Override
	public ObjectDisplayValues getDisplayValue(Collection<String> keys) {
		initOptions();
		
		String title = options.stream()
				.filter(option -> keys.contains(option.getKey()))
				.map(OrganisationOption::getDisplayTitle)
				.sorted()
				.collect(Collectors.joining(", "));
		return new ObjectDisplayValues(title, title);
	}

	@Override
	public Collection<String> getDefaultSelectedKeys() {
		return defaultSelectedKeys;
	}
	
	@Override
	public String getOptionsLabel(Locale locale) {
		return Util.createPackageTranslator(OrganisationOverviewController.class, locale).translate("option.label.organisations");
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
		
		Collection<String> organisationKeys = organisationsSupplier.get().stream()
				.map(org -> org.getKey().toString())
				.collect(Collectors.toSet());
		
		// It is possible that organizations are selected to which one actually does not
		// have access, since the corresponding permissions are missing. These must
		// remain and be available as an option.
		Set<String> additionalOrganisationKeys = new HashSet<>(defaultSelectedKeys);
		additionalOrganisationKeys.removeAll(organisationKeys);
		organisationKeys.addAll(additionalOrganisationKeys);
		
		options = optionCache.getOptions(organisationKeys);
	}
	
	static List<OrganisationOption> toOptions(Collection<Organisation> organisations) {
		Function<Organisation, String> keyExtractor = organisation -> organisation.getKey().toString();
		Function<Organisation, Organisation> parentExtractor = Organisation::getParent;
		Function<Organisation, GenericTreeNode> toNode = organisation -> {
			GenericTreeNode newNode = new GenericTreeNode();
			newNode.setTitle(createTitle(organisation));
			return newNode;
		};
		GenericTreeModelBuilder<Organisation> treeModelBuilder = new GenericTreeModelBuilder<>(keyExtractor, parentExtractor, toNode);
		GenericTreeModel treeModel = treeModelBuilder.build(organisations);
		
		List<OrganisationOption> options = new ArrayList<>(organisations.size());
		for (Organisation organisation : organisations) {
			TreeNode treeNode = treeModel.getNodeById(keyExtractor.apply(organisation));
			int totalNodeCount = TreeHelper.totalNodeCount(treeNode) - 1; // Do not count myself.
			List<TreeNode> treePath = TreeHelper.getTreePath(treeNode);
			treePath = TreeHelper.getTreePath(treeNode).subList(1, treePath.size() - 1); // Do not display leading slash
			
			String title = createOptionTitle(organisation, totalNodeCount);
			String subTitle = ObjectOption.createShortPath(treePath);
			String subTitleFull = ObjectOption.createFullPath(treePath);
			OrganisationOption option = new OrganisationOption(keyExtractor.apply(organisation), title, subTitle, subTitleFull, organisation.getDisplayName());
			
			options.add(option);
		}
		
		options.sort(new PathTitleComparator());
		
		return options;
	}

	public static String createTitle(Organisation organisation) {
		return organisation.getDisplayName();
	}

	private static String createOptionTitle(Organisation organisation, int totalNodeCount) {
		String title = createTitle(organisation);
		if (StringHelper.containsNonWhitespace(organisation.getLocation())) {
			title += " · " + organisation.getLocation();
		}
		if (totalNodeCount > 0) {
			title += " · #" + totalNodeCount;
		}
		return title;
	}
	
	private final static class PathTitleComparator implements Comparator<OrganisationOption> {
		
		@Override
		public int compare(OrganisationOption o1, OrganisationOption o2) {
			
			// Use title fallback, otherwise all root options will be at the top of the list.
			String subTitleFull1 = StringHelper.containsNonWhitespace(o1.getSubTitleFull())? o1.getSubTitleFull(): o1.getDisplayTitle();
			String subTitleFull2 = StringHelper.containsNonWhitespace(o2.getSubTitleFull())? o2.getSubTitleFull(): o2.getDisplayTitle();
			
			int c = subTitleFull1.compareToIgnoreCase(subTitleFull2);
			if (c == 0) {
				c = o1.getDisplayTitle().compareToIgnoreCase(o2.getDisplayTitle());
			}
			return c;
		}
		
	}
	
	static final class OrganisationOption extends ObjectOptionValues {
		
		private final String displayTitle;

		public OrganisationOption(String key, String title, String subTitle, String subTitleFull, String displayTitle) {
			super(key, title, subTitle, subTitleFull);
			this.displayTitle = displayTitle;
		}

		public String getDisplayTitle() {
			return displayTitle;
		}
		
	}
	
	public static final OrganisationRef toRef(String key) {
		return new OrganisationRefImpl(Long.valueOf(key));
	}
	
	public static final Collection<? extends OrganisationRef> toRefs(Collection<String> keys) {
		return keys.stream().map(key -> new OrganisationRefImpl(Long.valueOf(key))) .toList();
	}

}
