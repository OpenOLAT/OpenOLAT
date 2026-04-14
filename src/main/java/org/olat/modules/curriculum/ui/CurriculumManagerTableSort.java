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

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.curriculum.model.CurriculumImplementationsStatistics;

/**
 *
 * Initial date: 4 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumManagerTableSort extends SortableFlexiTableModelDelegate<CurriculumRow> {

	public static final String SORT_RELEVANCE = "relevance";

	public CurriculumManagerTableSort(SortKey orderBy, CurriculumManagerDataModel model, Locale locale) {
		super(orderBy, model, locale);
	}

	@Override
	protected void sort(List<CurriculumRow> rows) {
		if(SORT_RELEVANCE.equals(getOrderBy().getKey())) {
			rows.sort(new RelevanceComparator());
		} else {
			super.sort(rows);
		}
	}

	private class RelevanceComparator implements Comparator<CurriculumRow> {

		@Override
		public int compare(CurriculumRow a, CurriculumRow b) {
			int c = Integer.compare(relevanceGroup(a), relevanceGroup(b));
			if(c != 0) return c;
			return compareString(a.getDisplayName(), b.getDisplayName());
		}

		private int relevanceGroup(CurriculumRow row) {
			CurriculumImplementationsStatistics stats = row.getImplementationsStatistics();
			if(stats.numOfPreparationRootElements() > 0
					|| stats.numOfProvisionalRootElements() > 0
					|| stats.numOfConfirmedRootElements() > 0) {
				return 0;
			}
			if(stats.numOfCancelledRootElements() > 0 || stats.numOfFinishedRootElements() > 0) {
				return 1;
			}
			return 2;
		}
	}

}
