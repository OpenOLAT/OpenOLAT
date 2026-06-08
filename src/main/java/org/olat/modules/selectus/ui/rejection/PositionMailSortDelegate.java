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
package org.olat.modules.selectus.ui.rejection;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.MailLogInfos;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.UserRatingMapper;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.olat.modules.selectus.ui.comparator.ApplicationCategoryInfosListComparator;
import org.olat.modules.selectus.ui.comparator.LastnameComparator;
import org.olat.modules.selectus.ui.rating.UserMapperCommitteeRatingComparator;
import org.olat.modules.selectus.ui.rejection.PositionMailCenterDataModel.Fields;

/**
 * 
 * Initial date: 29 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionMailSortDelegate extends SortableFlexiTableModelDelegate<MailLogInfos> {
	
	private static final Fields[] FIELDS = Fields.values();
	
	private final IdComparator idComparator = new IdComparator();
	private final LastnameComparator lastnameComparator = new LastnameComparator();
	private final LastnameRowComparator lastnameRowComparator = new LastnameRowComparator();
	private final UserMapperCommitteeRatingComparator committeeRatingComparator = new UserMapperCommitteeRatingComparator();
	
	public PositionMailSortDelegate(SortKey orderBy, PositionMailCenterDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<MailLogInfos> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex >= 0 && columnIndex < FIELDS.length) {
			Fields column = FIELDS[columnIndex];
			switch(column) {
				case id: Collections.sort(rows, new IdComparator()); break;
				case title: Collections.sort(rows, new TitleComparator()); break;
				case firstName: Collections.sort(rows, new FirstnameComparator()); break;
				case lastName: Collections.sort(rows, new LastnameRowComparator()); break;
				case yearOfBirth: Collections.sort(rows, new YearOfBirthComparator()); break;
				case highestDegreeYear: Collections.sort(rows, new YearComparator(Fields.highestDegreeYear)); break;
				case highestDegreeYearPhD: Collections.sort(rows, new YearComparator(Fields.highestDegreeYearPhD)); break;
				case committeeRating: Collections.sort(rows, new CommitteeRatingComparator()); break;
				case decision: Collections.sort(rows, new DecisionComparator()); break;
				case categories: Collections.sort(rows, new ApplicationCategoriesComparator(isAsc())); break;
				default: super.sort(rows);
			}
		} else if(columnIndex >= ApplicationAttributesDelegate.COLS_OFFSET) {
			super.sort(rows);
		}
	}
	
	private class ApplicationCategoriesComparator implements Comparator<MailLogInfos> {

		private final ApplicationCategoryInfosListComparator comparator;
		
		public ApplicationCategoriesComparator(boolean asc) {
			comparator = new ApplicationCategoryInfosListComparator(asc);
		}

		@Override
		public int compare(MailLogInfos o1, MailLogInfos o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			List<ApplicationCategoryInfos> c1 = ((PositionMailCenterDataModel)getTableModel()).getCategories(o1);
			List<ApplicationCategoryInfos> c2 = ((PositionMailCenterDataModel)getTableModel()).getCategories(o2);
			int c = comparator.compare(c1, c2);
			if(c == 0) {
				return lastnameRowComparator.compare(o1, o2);
			}
			if(c == 0) {
				c = idComparator.compare(o1, o2);
			}
			return c;
		}
	}
	
	private class YearOfBirthComparator implements Comparator<MailLogInfos> {
		
		@Override
		public int compare(MailLogInfos o1, MailLogInfos o2) {
			Date d1 = o1.getApplication().getPerson().getBirthday();
			Date d2 = o2.getApplication().getPerson().getBirthday();
			int c = compareDateAndTimestamps(d1, d2);
			if(c == 0) {
				c = lastnameComparator.compare(o1.getApplication(), o2.getApplication());
			}
			return c;
		}
	}
	
	private class YearComparator implements Comparator<MailLogInfos> {
		
		private final Fields field;
		
		public YearComparator(Fields field) {
			this.field = field;
		}
		
		@Override
		public int compare(MailLogInfos o1, MailLogInfos o2) {
			Object d1 = getTableModel().getValueAt(o1, field.ordinal());
			Object d2 = getTableModel().getValueAt(o2, field.ordinal());
			if(d1 instanceof Integer && d2 instanceof Integer) {
				Integer y1 = (Integer)d1;
				Integer y2 = (Integer)d2;
				return y1.compareTo(y2);
			} else if(d1 instanceof Integer) {
				return 1;
			} else if(d2 instanceof Integer) {
				return -1;
			}
			return 0;
		}
	}
	
	private class CommitteeRatingComparator implements Comparator<MailLogInfos> {
		@Override
		public int compare(MailLogInfos o1, MailLogInfos o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			Object a = getTableModel().getValueAt(o1, Fields.committeeRating.ordinal());
			Object b = getTableModel().getValueAt(o2, Fields.committeeRating.ordinal());
			int c = 0;
			if(a instanceof UserRatingMapper && b instanceof UserRatingMapper) {
				c = committeeRatingComparator.compare((UserRatingMapper)a, (UserRatingMapper)b);
			}
			if(c == 0) {
				c = -lastnameRowComparator.compare(o1, o2);
			}
			if(c == 0) {
				c = idComparator.compare(o1, o2);
			}
			return c;
		}
	}
	
	private class IdComparator implements Comparator<MailLogInfos> {

		@Override
		public int compare(MailLogInfos a1, MailLogInfos a2) {
			if(a1 == null || a2 == null) {
				return compareNullObjects(a1, a2);
			}

			Integer i1 = a1.getApplication().getId();
			Integer i2 = a2.getApplication().getId();
			if(i1 == null) return 1;
			if(i2 == null) return -1;

			int result = i1.compareTo(i2);
			if(result == 0) {
				Long k1 = a1.getApplication().getKey();
				Long k2 = a2.getApplication().getKey();
				return k1.compareTo(k2);
			}
			return result;
		}
	}
	
	private class DecisionComparator implements Comparator<MailLogInfos> {

		@Override
		public int compare(MailLogInfos a1, MailLogInfos a2) {
			if(a1 == null || a2 == null) {
				return compareNullObjects(a1, a2);
			}
			
			Integer d1 = a1.getApplication().getDecision();
			Integer d2 = a2.getApplication().getDecision();
			if(d1 == null || d2 == null) {
				return compareNullObjects(d1, d2);
			}
			
			int c = Integer.compare(d1.intValue(), d2.intValue());
			if(c == 0) {
				return lastnameRowComparator.compare(a1, a2);
			}
			return c;
		}
	}
	
	private class TitleComparator implements Comparator<MailLogInfos> {
		
		@Override
		public int compare(MailLogInfos a1, MailLogInfos a2) {
			if(a1 == null) return 1;
			if(a2 == null) return -1;
			
			Person p1 = a1.getApplication().getPerson();
			Person p2 = a2.getApplication().getPerson();
			if(p1 == null) return 1;
			if(p2 == null) return -1;
			
			String t1 = p1.getTitle();
			String t2 = p2.getTitle();
			if(!StringHelper.containsNonWhitespace(t1)) return 1;
			if(!StringHelper.containsNonWhitespace(t2)) return -1;
			int result = t1.compareToIgnoreCase(t2);
			if(result == 0) {
				return lastnameRowComparator.compare(a1, a2);
			}
			return result;
		}
	}
	
	private class FirstnameComparator implements Comparator<MailLogInfos> {

		@Override
		public int compare(MailLogInfos a1, MailLogInfos a2) {
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
	
	private class LastnameRowComparator implements Comparator<MailLogInfos> {

		@Override
		public int compare(MailLogInfos a1, MailLogInfos a2) {
			if(a1 == null || a2 == null) {
				return compareNullObjects(a1, a2);
			}
			int c = lastnameComparator.compare(a1.getApplication(), a2.getApplication());
			if(c == 0) {
				c = idComparator.compare(a1, a2);
			}
			return c;
		}
	}
}