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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementRepositoryTableModel extends DefaultFlexiTableDataModel<CurriculumElementRepositoryRow>
		implements SortableFlexiTableDataModel<CurriculumElementRepositoryRow>, FilterableFlexiTableModel, FlexiBusinessPathModel {

	private static final RepoCols[] COLS = RepoCols.values();

	private final Translator translator;
	private List<CurriculumElementRepositoryRow> backups;
	private final Map<String, String> fullNames = new HashMap<>();
	private final Map<Long, OLATResourceAccess> repoEntriesWithOffer = new HashMap<>();

	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AccessControlModule acModule;

	public CurriculumElementRepositoryTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		CoreSpringFactory.autowireObject(this);
		translator = Util.createPackageTranslator(RepositoryService.class, locale);
	}

	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			SortableFlexiTableModelDelegate<CurriculumElementRepositoryRow> sort = new SortableFlexiTableModelDelegate<>(orderBy, this, translator.getLocale());
			List<CurriculumElementRepositoryRow> sorted = sort.sort();
			super.setObjects(sorted);
		}
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if (StringHelper.containsNonWhitespace(searchString)) {
			List<CurriculumElementRepositoryRow> filteredRows = new ArrayList<>();
			searchString = searchString.toLowerCase();
			for (CurriculumElementRepositoryRow row : backups) {
				if (accept(searchString, row)) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	private boolean accept(String searchString, CurriculumElementRepositoryRow entry) {
		return accept(searchString, entry.getDisplayname()) || accept(searchString, entry.getExternalRef());
	}

	private boolean accept(String searchString, String value) {
		return StringHelper.containsNonWhitespace(value) && value.toLowerCase().contains(searchString);
	}

	@Override
	public String getUrl(Component source, Object object, String action) {
		if ("select".equals(action) && object instanceof CurriculumElementRepositoryRow repositoryRow) {
			String businessPath = "[RepositoryEntry:" + repositoryRow.getKey() + "]";
			return BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementRepositoryRow entry = getObject(row);
		return getValueAt(entry, col);
	}

	@Override
	public Object getValueAt(CurriculumElementRepositoryRow row, int col) {
		RepositoryEntry re = row.getRepositoryEntry();
		return switch (COLS[col]) {
			case ac -> getAccessControl(re);
			case repoEntry -> re;
			case displayname -> re.getDisplayname();
			case author -> getFullname(re.getInitialAuthor());
			case access -> re;
			case creationDate -> re.getCreationDate();
			case lastUsage -> re.getStatistics().getLastUsage();
			case externalId -> re.getExternalId();
			case externalRef -> re.getExternalRef();
			case lifecycleLabel -> getLifecycleLabel(re.getLifecycle());
			case lifecycleSoftKey -> getLifecycleSoftKey(re.getLifecycle());
			case lifecycleStart -> re.getLifecycle() == null ? null : re.getLifecycle().getValidFrom();
			case lifecycleEnd -> re.getLifecycle() == null ? null : re.getLifecycle().getValidTo();
			case runtimeType -> re.getRuntimeType();
			case instantiateTemplate -> row.getInstantiateLink();
			case resources -> row.getResourcesLink();
			case tools -> row.getToolsLink();
			default -> "ERROR";
		};
	}

	@Override
	public void setObjects(List<CurriculumElementRepositoryRow> objects) {
		backups = objects;
		super.setObjects(objects);
		repoEntriesWithOffer.clear();
		secondaryInformations(objects);
	}

	private void secondaryInformations(List<CurriculumElementRepositoryRow> repoEntries) {
		if (repoEntries == null || repoEntries.isEmpty())
			return;

		secondaryInformationsAccessControl(repoEntries);
		secondaryInformationsUsernames(repoEntries);
	}

	private void secondaryInformationsAccessControl(List<CurriculumElementRepositoryRow> entriesRows) {
		if (entriesRows == null || entriesRows.isEmpty() || !acModule.isEnabled()) {
			return;
		}

		List<RepositoryEntry> repoEntries = entriesRows.stream()
				.map(CurriculumElementRepositoryRow::getRepositoryEntry)
				.toList();
		List<OLATResourceAccess> withOffers = acService.filterRepositoryEntriesWithAC(repoEntries);
		for (OLATResourceAccess withOffer : withOffers) {
			repoEntriesWithOffer.put(withOffer.getResource().getKey(), withOffer);
		}
	}

	private void secondaryInformationsUsernames(List<CurriculumElementRepositoryRow> repoEntries) {
		if (repoEntries == null || repoEntries.isEmpty())
			return;

		Set<String> newNames = new HashSet<>();
		for (CurriculumElementRepositoryRow re : repoEntries) {
			final String author = re.getInitialAuthor();
			if (StringHelper.containsNonWhitespace(author) && !fullNames.containsKey(author)) {
				newNames.add(author);
			}
		}

		if (!newNames.isEmpty()) {
			Map<String, String> newFullnames = userManager.getUserDisplayNamesByUserName(newNames);
			fullNames.putAll(newFullnames);
		}
	}


	private Object getAccessControl(RepositoryEntry re) {
		if (re.isPublicVisible()) {
			return repoEntriesWithOffer.get(re.getOlatResource().getKey());
		}
		return Collections.singletonList("o_ac_membersonly");
	}

	private String getFullname(String author) {
		if (fullNames.containsKey(author)) {
			return fullNames.get(author);
		}
		return author;
	}
	
	private String getLifecycleLabel(RepositoryEntryLifecycle lf) {
		if (lf == null || lf.isPrivateCycle()) {
			return "";
		}
		return lf.getLabel();
	}
	
	private String getLifecycleSoftKey(RepositoryEntryLifecycle lf) {
		if (lf == null || lf.isPrivateCycle()) {
			return "";
		}
		return lf.getSoftKey();
	}

	public enum RepoCols implements FlexiSortableColumnDef {
		ac("table.header.ac"),
		repoEntry("table.header.typeimg"),
		displayname("table.header.displayname"),
		author("table.header.author"),
		access("table.header.access"),
		creationDate("table.header.date"),
		lastUsage("table.header.lastusage"),
		externalId("table.header.externalid"),
		externalRef("table.header.externalref"),
		lifecycleLabel("table.header.lifecycle.label"),
		lifecycleSoftKey("table.header.lifecycle.softkey"),
		lifecycleStart("table.header.lifecycle.start"),
		lifecycleEnd("table.header.lifecycle.end"),
		guests("table.header.guests"),
		runtimeType("table.header.runtime.type"),
		instantiateTemplate("table.header.instantiate.template"),
		resources("table.header.resources"),
		tools("table.header.tools");

		private final String i18nKey;

		private RepoCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return instantiateTemplate != this && tools != this;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
