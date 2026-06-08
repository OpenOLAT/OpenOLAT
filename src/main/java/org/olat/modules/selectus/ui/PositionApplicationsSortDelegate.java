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
package org.olat.modules.selectus.ui;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.ApplicationRefereeStats;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.ui.PositionApplicationsDataModel.Fields;
import org.olat.modules.selectus.ui.comparator.AppToCategoryListComparator;
import org.olat.modules.selectus.ui.comparator.LastnameComparator;
import org.olat.modules.selectus.ui.model.ApplicationRow;
import org.olat.modules.selectus.ui.rating.UserMapperCommitteeRatingComparator;

/**
 * 
 * Initial date: 26 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionApplicationsSortDelegate extends SortableFlexiTableModelDelegate<ApplicationRow> {
	
	private final IdComparator idComparator = new IdComparator();
	private final LastnameComparator lastnameComparator = new LastnameComparator();
	private final LastnameRowComparator lastnameRowComparator = new LastnameRowComparator();
	private final UserMapperCommitteeRatingComparator committeeRatingComparator = new UserMapperCommitteeRatingComparator();
	
	private final PositionApplicationsDataModel appsTableModel;
	
	public PositionApplicationsSortDelegate(SortKey orderBy, PositionApplicationsDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
		appsTableModel = tableModel;
	}
	
	@Override
	protected void sort(List<ApplicationRow> rows) {
		if(getOrderBy().getKey().equals(Fields.myAssignment.name())) {
			Collections.sort(rows, new MyAssignmentsComparator());
		} else {
			int columnIndex = getColumnIndex();
			if(columnIndex < Fields.values().length) {
				Fields column = Fields.values()[columnIndex];
				switch(column) {
					case id: Collections.sort(rows, new IdComparator()); break;
					case title: Collections.sort(rows, new TitleComparator()); break;
					case firstName: Collections.sort(rows, new FirstnameComparator()); break;
					case lastName: Collections.sort(rows, new LastnameRowComparator()); break;
					case yearOfBirth: Collections.sort(rows, new YearOfBirthComparator()); break;
					case highestDegreeYear: Collections.sort(rows, new YearComparator(Fields.highestDegreeYear)); break;
					case highestDegreeYearPhD: Collections.sort(rows, new YearComparator(Fields.highestDegreeYearPhD)); break;
					case myRating: Collections.sort(rows, new MyRatingComparator()); break;
					case committeeRating: Collections.sort(rows, new CommitteeRatingComparator()); break;
					case experts: Collections.sort(rows, new ExpertsComparator()); break;
					case recommendations: Collections.sort(rows, new RecommendationsComparator()); break;
					case comparativeExperts: Collections.sort(rows, new ComparativeExpertsComparator()); break;
					case decision: {
						synchronized(appsTableModel.decisionLock) {
							Collections.sort(rows, new DecisionComparator()); 
						}
						break;
					}
					case categories: Collections.sort(rows, new ApplicationCategoriesComparator(isAsc())); break;
					default: super.sort(rows);
				}
			} else {
				super.sort(rows);
			}
		}
	}

	private class ApplicationCategoriesComparator implements Comparator<ApplicationRow> {
		
		private final AppToCategoryListComparator catComparator;
		
		public ApplicationCategoriesComparator(boolean ascendent) {
			catComparator = new AppToCategoryListComparator(ascendent);
		}

		@Override
		public int compare(ApplicationRow o1, ApplicationRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			int c = catComparator.compare(o1.getCategories(), o2.getCategories());
			if(c == 0) {
				c = -lastnameRowComparator.compare(o1, o2);
			}
			if(c == 0) {
				c = idComparator.compare(o1, o2);
			}
			return c;
		}
	}
	
	private class ExpertsComparator implements Comparator<ApplicationRow> {
		
		@Override
		public int compare(ApplicationRow o1, ApplicationRow o2) {
			ApplicationRefereeStats d1 = o1.getRefereesStats();
			ApplicationRefereeStats d2 = o2.getRefereesStats();
			if(d1 == null || d2 == null) {
				return compareNullObjects(d1, d2);
			}
			
			int t1 = d1.getNumOfExperts() + d1.getNumOfSubmittedExperts();
			int t2 = d2.getNumOfExperts() + d2.getNumOfSubmittedExperts();
			return Integer.compare(t1, t2);
		}
	}
	
	private class ComparativeExpertsComparator implements Comparator<ApplicationRow> {
		
		@Override
		public int compare(ApplicationRow o1, ApplicationRow o2) {
			ApplicationRefereeStats d1 = o1.getRefereesStats();
			ApplicationRefereeStats d2 = o2.getRefereesStats();
			if(d1 == null || d2 == null) {
				return compareNullObjects(d1, d2);
			}
			
			int t1 = d1.getNumOfComparativeExperts() + d1.getNumOfSubmittedComparativeExperts();
			int t2 = d2.getNumOfComparativeExperts() + d2.getNumOfSubmittedComparativeExperts();
			return Integer.compare(t1, t2);
		}
	}
	
	private class MyAssignmentsComparator implements Comparator<ApplicationRow> {

		@Override
		public int compare(ApplicationRow o1, ApplicationRow o2) {
			UserRating rate1 = o1.getCurrentRating();
			UserRating rate2 = o2.getCurrentRating();
			
			boolean hasRate1 = rate1 != null && (rate1.getRating().intValue() > 0 || rate1.getRating().intValue() == RecruitingService.ABSTENTION);
			boolean hasRate2 = rate2 != null && (rate2.getRating().intValue() > 0 || rate2.getRating().intValue() == RecruitingService.ABSTENTION);
			
			int c = Boolean.compare(hasRate1, hasRate2);
			if(c == 0) {
				if(hasRate1) {
					c = Boolean.compare(o1.isReviewed(), o2.isReviewed());
				} else {
					c = -Boolean.compare(o1.isReviewed(), o2.isReviewed());
				}
			}
			if(c == 0) {
				c = idComparator.compare(o1, o2);
			}		
			return c;
		}
	}
	
	private class RecommendationsComparator implements Comparator<ApplicationRow> {
		
		@Override
		public int compare(ApplicationRow o1, ApplicationRow o2) {
			ApplicationRefereeStats d1 = o1.getRefereesStats();
			ApplicationRefereeStats d2 = o2.getRefereesStats();
			if(d1 == null || d2 == null) {
				return compareNullObjects(d1, d2);
			}
			
			int t1 = d1.getNumOfRecommendations() + d1.getNumOfSubmittedRecommendations();
			int t2 = d2.getNumOfRecommendations() + d2.getNumOfSubmittedRecommendations();
			return Integer.compare(t1, t2);
		}
	}
	
	private class YearOfBirthComparator implements Comparator<ApplicationRow> {
		
		@Override
		public int compare(ApplicationRow o1, ApplicationRow o2) {
			Date d1 = o1.getApplication().getPerson().getBirthday();
			Date d2 = o2.getApplication().getPerson().getBirthday();
			int c = compareDateAndTimestamps(d1, d2);
			if(c == 0) {
				c = lastnameComparator.compare(o1.getApplication(), o2.getApplication());
			}
			return c;
		}
	}
	
	private class YearComparator implements Comparator<ApplicationRow> {
		
		private final Fields field;
		private final Calendar cal = Calendar.getInstance();
		
		public YearComparator(Fields field) {
			this.field = field;
		}
		
		@Override
		public int compare(ApplicationRow o1, ApplicationRow o2) {
			Object d1 = getTableModel().getValueAt(o1, field.ordinal());
			Object d2 = getTableModel().getValueAt(o2, field.ordinal());
			
			int c = compareNullObjects(d1, d2);
			if(c == 0) {
				if(d1 instanceof Date && d2 instanceof Date) {
					Date date1 = (Date)d1;
					Date date2 = (Date)d2;
					cal.setTime(date1);
					d1 = cal.get(Calendar.YEAR);
					cal.setTime(date2);
					d2 = cal.get(Calendar.YEAR);
				}
				if(d1 instanceof Integer && d2 instanceof Integer) {
					Integer y1 = (Integer)d1;
					Integer y2 = (Integer)d2;
					c = y1.compareTo(y2);
				}
			}
			if(c == 0) {
				c = lastnameRowComparator.compare(o1, o2);
			}
			return c;
		}
	}

	private class MyRatingComparator implements Comparator<ApplicationRow> {
		@Override
		public int compare(ApplicationRow o1, ApplicationRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			double r1 = getRating(o1);
			double r2 = getRating(o2);
			int c = Double.compare(r1, r2);
			if(c == 0) {
				c = lastnameRowComparator.compare(o1, o2);
			}
			if(c == 0) {
				c = idComparator.compare(o1, o2);
			}
			return c;
		}
		
		private double getRating(ApplicationRow row) {
			double rating = 0.0d;
			if(row.getRatingItem() != null) {
				rating = Math.round(row.getRatingItem().getCurrentRating());
			} else if(row.getCurrentRating() != null && row.getCurrentRating().getRating() != null) {
				rating = row.getCurrentRating().getRating().intValue();
			}
			
			if(!row.isAllowed()) {
				rating -= 0.1d;
			}
			return rating;
		}
	}
	
	private class CommitteeRatingComparator implements Comparator<ApplicationRow> {
		@Override
		public int compare(ApplicationRow o1, ApplicationRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			int c = committeeRatingComparator.compare(o1.getUserRatingMapper(), o2.getUserRatingMapper());
			if(c == 0) {
				c = -lastnameRowComparator.compare(o1, o2);
			}
			if(c == 0) {
				c = idComparator.compare(o1, o2);
			}
			return c;
		}
	}
	
	private class IdComparator implements Comparator<ApplicationRow> {

		@Override
		public int compare(ApplicationRow a1, ApplicationRow a2) {
			if(a1 == null || a2 == null) {
				return compareNullObjects(a1, a2);
			}

			Integer i1 = a1.getApplication().getId();
			Integer i2 = a2.getApplication().getId();
			int c = 0;
			if(i1 == null || i2 == null) {
				c = compareNullObjects(i1, i2);
			} else {
				c = i1.compareTo(i2);
			}
			if(c == 0) {
				c = compareLongs(a1.getApplication().getKey(), a2.getApplication().getKey());
			}
			return c;
		}
	}
	
	private class DecisionComparator implements Comparator<ApplicationRow> {

		@Override
		public int compare(ApplicationRow a1, ApplicationRow a2) {
			if(a1 == null || a2 == null) {
				return compareNullObjects(a1, a2);
			}
			
			Integer d1 = a1.getDecision();
			Integer d2 = a2.getDecision();
			if(d1 == null || d2 == null) {
				return compareNullObjects(d1, d2);
			}
			
			int c = Integer.compare(d1.intValue(), d2.intValue());
			if(c == 0) {
				c = lastnameRowComparator.compare(a1, a2);
			}
			return c;
		}
	}
	
	private class TitleComparator implements Comparator<ApplicationRow> {
		@Override
		public int compare(ApplicationRow a1, ApplicationRow a2) {
			if(a1 == null || a2 == null) {
				return compareNullObjects(a1, a2);
			}

			Person p1 = a1.getApplication().getPerson();
			Person p2 = a2.getApplication().getPerson();
			
			int c = 0;
			if(p1 == null || p2 == null) {
				c = compareNullObjects(p1, p2);
			} else {
				c = compareString(p1.getTitle(), p2.getTitle());
			}
			if(c == 0) {
				c = lastnameRowComparator.compare(a1, a2);
			}
			return c;
		}
	}
	
	private class FirstnameComparator implements Comparator<ApplicationRow> {
		@Override
		public int compare(ApplicationRow a1, ApplicationRow a2) {
			if(a1 == null || a2 == null) {
				return compareNullObjects(a1, a2);
			}
			
			Person p1 = a1.getApplication().getPerson();
			Person p2 = a2.getApplication().getPerson();
			
			int c = 0;
			if(p1 == null || p2 == null) {
				c = compareNullObjects(p1, p2);
			} else {
				c = compareString(p1.getFirstName(), p2.getFirstName());
			}

			if(c == 0) {
				c = lastnameRowComparator.compare(a1, a2);
			}
			return c;
		}
	}
	
	private class LastnameRowComparator implements Comparator<ApplicationRow> {
		@Override
		public int compare(ApplicationRow a1, ApplicationRow a2) {
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