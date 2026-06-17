/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.decision;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.comparator.ApplicationCategoryInfosListComparator;
import org.olat.modules.selectus.ui.decision.DecisionToolDataModel.RubricCols;
import org.olat.modules.selectus.ui.rating.UserMapperCommitteeRatingComparator;

/**
 * 
 * Initial date: 18 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DecisionToolSortDelegate extends SortableFlexiTableModelDelegate<ApplicationRubricsRow> {

	private final LastnameComparator lastnameComparator = new LastnameComparator();
	private final UserMapperCommitteeRatingComparator committeeRatingComparator = new UserMapperCommitteeRatingComparator();

	
	public DecisionToolSortDelegate(SortKey orderBy, DecisionToolDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<ApplicationRubricsRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex >= 0 && columnIndex < RubricCols.values().length) {
			switch(RubricCols.values()[columnIndex]) {
				case id: Collections.sort(rows, new IdComparator()); break;
				case firstName: Collections.sort(rows, new FirstnameComparator()); break;
				case lastName: Collections.sort(rows, new LastnameComparator()); break;
				case committeeRating: Collections.sort(rows, new CommitteeRatingComparator()); break;
				case categories: Collections.sort(rows, new ApplicationCategoriesComparator(isAsc())); break;
				case sum: Collections.sort(rows, new DefaultComparator()); break;
				default: super.sort(rows);
			}
		}
		
		if(columnIndex >= DecisionToolDataModel.RUBRIC_OFFSET) {
			int rubricIndex = columnIndex - DecisionToolDataModel.RUBRIC_OFFSET;
			Collections.sort(rows, new RubricRatingComparator(rubricIndex));
		}
	}
	
	private class ApplicationCategoriesComparator implements Comparator<ApplicationRubricsRow> {
		
		private final ApplicationCategoryInfosListComparator comparator;
		
		public ApplicationCategoriesComparator(boolean asc) {
			comparator = new ApplicationCategoryInfosListComparator(asc);
		}

		@Override
		public int compare(ApplicationRubricsRow o1, ApplicationRubricsRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			List<ApplicationCategoryInfos> c1 = ((DecisionToolDataModel)getTableModel()).getCategories(o1);
			List<ApplicationCategoryInfos> c2 = ((DecisionToolDataModel)getTableModel()).getCategories(o2);
			int c = comparator.compare(c1, c2);
			if(c == 0) {
				return lastnameComparator.compare(o1, o2);
			}
			return c;
		}
	}
	
	private class RubricRatingComparator implements Comparator<ApplicationRubricsRow> {
		
		private final int rubricIndex;
		
		public RubricRatingComparator(int rubricIndex) {
			this.rubricIndex = rubricIndex;
		}

		@Override
		public int compare(ApplicationRubricsRow o1, ApplicationRubricsRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			ApplicationRubric appRu1 = o1.getApplicationRubric(rubricIndex);
			ApplicationRubric appRu2 = o2.getApplicationRubric(rubricIndex);
			if(appRu1 == null || appRu2 == null) {
				return compareNullObjects(appRu1, appRu2);
			}
			double n1 = appRu1.getNumericalNormalizedValue();
			double n2 = appRu2.getNumericalNormalizedValue();
			int c = Double.compare(n1, n2);
			if(c == 0) {
				String v1 = appRu1.getValue();
				String v2 = appRu2.getValue();
				c = compareString(v1, v2);
			}
			return c;
		}
	}
	
	private class CommitteeRatingComparator implements Comparator<ApplicationRubricsRow> {
		@Override
		public int compare(ApplicationRubricsRow o1, ApplicationRubricsRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			return committeeRatingComparator.compare(o1.getUserRatingMapper(), o2.getUserRatingMapper());
		}
	}
	
	private class IdComparator implements Comparator<ApplicationRubricsRow> {

		@Override
		public int compare(ApplicationRubricsRow a1, ApplicationRubricsRow a2) {
			if(a1 == null || a2 == null) {
				return compareNullObjects(a1, a2);
			}

			Integer i1 = a1.getApplication().getId();
			Integer i2 = a2.getApplication().getId();
			if(i1 == null) return 1;
			if(i2 == null) return -1;

			int result = i1.compareTo(i2);
			if(result == 0) {
				return lastnameComparator.compare(a1, a2);
			}
			return result;
		}
	}

	private class FirstnameComparator implements Comparator<ApplicationRubricsRow> {

		@Override
		public int compare(ApplicationRubricsRow a1, ApplicationRubricsRow a2) {
			if(a1 == null) return 1;
			if(a2 == null) return -1;
			
			Person p1 = a1.getApplication().getPerson();
			Person p2 = a2.getApplication().getPerson();
			if(p1 == null) return 1;
			if(p2 == null) return -1;
			
			String f1 = p1.getFirstName();
			String f2 = p2.getFirstName();
			if(f1 == null) return 1;
			if(f2 == null) return -1;
			int result = f1.compareToIgnoreCase(f2);
			if(result == 0) {
				String l1 = p1.getLastName();
				String l2 = p2.getLastName();
				if(l1 == null) return 1;
				if(l2 == null) return -1;
				return l1.compareToIgnoreCase(l2);
			}
			return result;
		}
	}
	
	private class LastnameComparator implements Comparator<ApplicationRubricsRow> {

		@Override
		public int compare(ApplicationRubricsRow a1, ApplicationRubricsRow a2) {
			if(a1 == null) return 1;
			if(a2 == null) return -1;
			
			Person p1 = a1.getApplication().getPerson();
			Person p2 = a2.getApplication().getPerson();
			if(p1 == null) return 1;
			if(p2 == null) return -1;
			
			String l1 = p1.getLastName();
			String l2 = p2.getLastName();
			if(l1 == null) return 1;
			if(l2 == null) return -1;
			int result = l1.compareToIgnoreCase(l2);
			if(result == 0) {
				String f1 = p1.getFirstName();
				String f2 = p2.getFirstName();
				return f1.compareToIgnoreCase(f2);
			}
			return result;
		}
	}
}