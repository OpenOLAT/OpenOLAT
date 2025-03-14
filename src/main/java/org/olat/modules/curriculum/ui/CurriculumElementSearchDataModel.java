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

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.id.context.BusinessControlFactory;

/**
 * 
 * Initial date: 10 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementSearchDataModel extends DefaultFlexiTableDataModel<CurriculumElementSearchRow>
implements SortableFlexiTableDataModel<CurriculumElementSearchRow>, FlexiBusinessPathModel {
	
	private static final SearchCols[] COLS = SearchCols.values();
	
	public CurriculumElementSearchDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CurriculumElementSearchRow> views = new CurriculumElementSearchTableModelSortDelegate(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public String getUrl(Component source, Object object, String action) {
		if(object instanceof CurriculumElementSearchRow row && action != null) {
			if(CurriculumSearchManagerController.CMD_CURRICULUM.equals(action)) {
				String bPath = CurriculumHelper.getCurriculumBusinessPath(row.getCurriculumKey());
				return BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(bPath);
			}
			if(CurriculumSearchManagerController.CMD_SELECT.equals(action)) {
				String bPath = "[CurriculumAdmin:0][Curriculum:" + row.getCurriculumKey() + "][Implementations:0][CurriculumElement:" + row.getKey() + "][Overview:0]";	
				return BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(bPath);
			}
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementSearchRow element = getObject(row);
		return getValueAt(element, col);
	}

	@Override
	public Object getValueAt(CurriculumElementSearchRow row, int col) {
		return switch(COLS[col]) {
			case key -> row.getKey();
			case curriculum -> row.getCurriculumName();
			case displayName -> row.getDisplayName();
			case externalRef -> row.getIdentifier();
			case externalId -> row.getExternalID();
			case beginDate -> row.getBeginDate();
			case endDate -> row.getEndDate();
			case typeDisplayName -> row.getCurriculumElementTypeDisplayName();
			case resources -> row.getResourcesLink();
			case status -> row.getStatus();
			case numOfMembers -> row.getNumOfMembers();
			case numOfParticipants -> row.getNumOfParticipants();
			case numOfCoaches -> row.getNumOfCoaches();
			case numOfOwners -> row.getNumOfOwners();
			case numOfCurriculumElementOwners -> row.getNumOfCurriculumElementOwners();
			case numOfMasterCoaches -> row.getNumOfMasterCoaches();
			case structure -> row.getStructureLink();
			case tools -> row.getToolsLink();
			default -> "ERROR";
		};
	}
	
	public enum SearchCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		curriculum("table.header.curriculum"),
		displayName("table.header.curriculum.element.display.name"),
		externalRef("table.header.external.ref"),
		externalId("table.header.external.id"),
		beginDate("table.header.begin.date"),
		endDate("table.header.end.date"),
		typeDisplayName("table.header.type"),
		resources("table.header.resources"),
		numOfMembers("table.header.num.of.members"),
		numOfParticipants("table.header.num.of.participants"),
		numOfCoaches("table.header.num.of.coaches"),
		numOfOwners("table.header.num.of.owners"),
		numOfCurriculumElementOwners("table.header.num.of.curriculumelementowners"),
		numOfMasterCoaches("table.header.num.of.mastercoaches"),
		structure("table.header.structure"),
		status("table.header.status"),
		tools("table.header.tools");
		
		private final String i18nHeaderKey;
		
		private SearchCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
