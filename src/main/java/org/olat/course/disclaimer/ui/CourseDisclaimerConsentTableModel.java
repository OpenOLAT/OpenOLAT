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
package org.olat.course.disclaimer.ui;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/* 
 * Date: 22 Mar 2020<br>
 * @author Alexander Boeckle
 */
public class CourseDisclaimerConsentTableModel extends DefaultFlexiTableDataModel<CourseDisclaimerConsenstPropertiesRow>
implements SortableFlexiTableDataModel<CourseDisclaimerConsenstPropertiesRow>, FlexiTableCssDelegate {

	private static final Logger log = Tracing.createLoggerFor(CourseDisclaimerConsentTableModel.class);
	
	private Translator translator;	
	private Locale locale;
	private List<CourseDisclaimerConsenstPropertiesRow> backupList;
	
	public CourseDisclaimerConsentTableModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
		this.locale = translator.getLocale();
	}

	@Override
	public Object getValueAt(int row, int col) {
		CourseDisclaimerConsenstPropertiesRow consentRow = getObject(row);
		return getValueAt(consentRow, col);
	}

	@Override
	public Object getValueAt(CourseDisclaimerConsenstPropertiesRow row, int col) {
		if (col >= CourseDisclaimerConsentOverviewController.USER_PROPS_OFFSET) {
			int propPos = col - CourseDisclaimerConsentOverviewController.USER_PROPS_OFFSET;
			return row.getIdentityProp(propPos);
		}
		
		switch (ConsentCols.values()[col]) {
			case consent:
				Date consentDate = row.getConsentDate();
				if (consentDate == null) {
					return translator.translate("consent.rejected");
				} else {					
					return row.getConsentDate();
				}
			case tools:
				return row.getToolsLink();
			default:
				return "ERROR";
		}
	}

	@Override
	public void sort(SortKey orderBy) {
		List<CourseDisclaimerConsenstPropertiesRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}
	
	public void search(final String searchString, UserRequest ureq) {
		if(StringHelper.containsNonWhitespace(searchString)) {
			try {
				List<CourseDisclaimerConsenstPropertiesRow> filteredList;
				if(StringHelper.isLong(searchString)) {
					Long identityKey = Long.valueOf(searchString);
					filteredList = backupList.stream()
						.filter(entry ->  entry.getIdentityKey().equals(identityKey))
						.collect(Collectors.toList());
				} else {
					final String loweredSearchString = searchString.toLowerCase();
					filteredList = backupList.stream()
						.filter(entry -> contains(loweredSearchString, entry))
						.collect(Collectors.toList());
				}
				super.setObjects(filteredList);
			} catch (Exception e) {
				resetSearch();
				log.error("", ureq.getUserSession().getIdentity());
				log.error("Searchstring: ", searchString);
				log.error("", e);
			}
		} else {
			resetSearch();
		}
	}
	
	public void resetSearch() {
		super.setObjects(backupList);
	}
	
	public static boolean contains(String searchValue, CourseDisclaimerConsenstPropertiesRow entry) {
		String[] userProperties = entry.getIdentityProps();
		for(int i=userProperties.length; i-->0; ) {
			String userProp = userProperties[i];
			if(userProp != null && userProp.toLowerCase().contains(searchValue)) {
				return true;
			}
		}
		return false;
	}
	
	public void setBackupList(List<CourseDisclaimerConsenstPropertiesRow> backupList) {
		this.backupList = backupList;
	}
	
	public enum ConsentCols implements FlexiSortableColumnDef {
		consent("consent"),
		tools("tools");

		private final String i18nKey;

		private ConsentCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		// mark consents without date as rejected
		CourseDisclaimerConsenstPropertiesRow consentRow = getObject(pos);
		if (consentRow.getConsentDate() == null) {			
			return "o_marked_deleted";
		}
		return null;
	}
}
