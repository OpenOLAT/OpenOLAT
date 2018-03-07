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
package org.olat.ims.qti21.ui.assessment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.ui.assessment.CorrectionIdentityTableModel.IdentityCols;
import org.olat.ims.qti21.ui.assessment.model.CorrectionIdentityRow;

/**
 * 
 * Initial date: 5 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionIdentityTableSort extends SortableFlexiTableModelDelegate<CorrectionIdentityRow> {
	
	public CorrectionIdentityTableSort(SortKey orderBy, CorrectionIdentityTableModel model, Locale locale) {
		super(orderBy, model, locale);
	}
	
	@Override
	protected void sort(List<CorrectionIdentityRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex < CorrectionIdentityListController.USER_PROPS_OFFSET) {
			IdentityCols column = IdentityCols.values()[columnIndex];
			switch(column) {
				case corrected: Collections.sort(rows, new CorrectedComparator()); break;
				case notCorrected: Collections.sort(rows, new NotCorrectedComparator()); break;
				default: super.sort(rows);
			}
		} else {
			super.sort(rows);
		}
	}
	
	private int compareIdentity(CorrectionIdentityRow o1, CorrectionIdentityRow o2) {
		Identity i1 = o1.getIdentity();
		Identity i2 = o2.getIdentity();
		
		int c = 0;
		if(i1 == null || i2 == null) {
			c = compareNullObjects(i1, i2);
		} else {
			String l1 = i1.getUser().getLastName();
			String l2 = i2.getUser().getLastName();
			c = compareString(l1, l2);
			if(c == 0) {
				String f1 = i1.getUser().getFirstName();
				String f2 = i2.getUser().getFirstName();
				c = compareString(f1, f2);
			}
		}
		return c;
	}
	
	private class CorrectedComparator implements Comparator<CorrectionIdentityRow> {
		@Override
		public int compare(CorrectionIdentityRow o1, CorrectionIdentityRow o2) {
			int c = Integer.compare(o1.getNumCorrected(), o2.getNumCorrected());
			if(c == 0) {
				c = compareIdentity(o1, o2);
			}
			return c;
		}
	}
	
	private class NotCorrectedComparator implements Comparator<CorrectionIdentityRow> {
		@Override
		public int compare(CorrectionIdentityRow o1, CorrectionIdentityRow o2) {
			int c = Integer.compare(o1.getNumNotCorrected(), o2.getNumNotCorrected());
			if(c == 0) {
				c = compareIdentity(o1, o2);
			}
			return c;
		}
	}
}
