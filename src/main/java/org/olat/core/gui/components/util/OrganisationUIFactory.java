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
package org.olat.core.gui.components.util;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.id.Organisation;

/**
 * 
 * Initial date: 19 Sep 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationUIFactory {
	
	public static SelectionValues createSelectionValues(Collection<Organisation> organisations, Locale locale) {
		Collator collator = Collator.getInstance(locale);
		SelectionValues organisationSV = new SelectionValues();
		
		toOrganisationItems(organisations).stream()
				.sorted((o1, o2) -> collator.compare(o1.getPathSort(), o2.getPathSort()))
				.forEach(organisationItem -> organisationSV.add(new SelectionValue(
						organisationItem.getOrganisation().getKey().toString(),
						organisationItem.getPathDoted(),
						organisationItem.getOrganisation().getDisplayName())));
		
		return organisationSV;
	}

	public static List<OrganisationItem> toOrganisationItems(Collection<Organisation> organisations) {
		return organisations.stream()
				.map(OrganisationItem::new)
				.collect(Collectors.toList());
	}
	
	public static final class OrganisationItem {
		
		private final Organisation organisation;
		private final String pathNames;
		private final String pathDoted;
		private final String pathSort;
		
		public OrganisationItem(Organisation organisation) {
			this.organisation = organisation;
			
			List<String> names = new ArrayList<>();
			addNameRecursive(names, organisation);
			Collections.reverse(names);
			
			pathNames = names.stream().collect(Collectors.joining(" / "));
			pathSort = names.stream().collect(Collectors.joining(":::"));
			
			StringBuilder sb = new StringBuilder();
			if (names.size() == 1) {
				sb.append(names.get(0));
			} else if (names.size() == 2) {
				sb.append(names.get(0));
				sb.append(" / ");
				sb.append(names.get(1));
			} else if (names.size() > 2) {
				sb.append(names.get(0));
				sb.append(" / ... / ");
				sb.append(names.get(names.size() - 1));
			}
			pathDoted = sb.toString();
		}

		private void addNameRecursive(List<String> names, Organisation organisation) {
			names.add(organisation.getDisplayName());
			if (organisation.getParent() != null) {
				addNameRecursive(names, organisation.getParent());
			}
		}

		public Organisation getOrganisation() {
			return organisation;
		}

		public String getPathNames() {
			return pathNames;
		}

		public String getPathDoted() {
			return pathDoted;
		}

		public String getPathSort() {
			return pathSort;
		}
		
	}
}
