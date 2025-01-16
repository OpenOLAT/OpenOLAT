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
package org.olat.modules.portfolio.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.modules.ceditor.PageStatus;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.ui.BinderAssessmentController.AssessmentSectionWrapper;

/**
 * 
 * Initial date: 22.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class BinderAssessmentDataModel extends DefaultFlexiTableDataModel<AssessmentSectionWrapper> {
	
	private static final AssessmentSectionCols[] COLS = AssessmentSectionCols.values();
	
	public BinderAssessmentDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AssessmentSectionWrapper wrapper = getObject(row);
		return switch (COLS[col]) {
			case sectionName -> wrapper.getSectionTitle();
			case numOfPages -> wrapper.getNumOfPages();
			case newEntries -> wrapper.getPageUserStatusList().stream()
				.filter(p -> p != null)
				.filter(p -> p.equals(PageUserStatus.incoming))
				.count();
			case inProgress ->  wrapper.getPageUserStatusList().stream()
				.filter(p -> p != null)
				.filter(p -> p.equals(PageUserStatus.inProcess))
				.count();
			case done -> wrapper.getPageUserStatusList().stream()
				.filter(p -> p != null)
				.filter(p -> p.equals(PageUserStatus.done))
				.count();
			case draft -> wrapper.getSection().getPages().stream()
				.filter(p -> p != null)
				.filter(p -> p.getPageStatus() != null && p.getPageStatus().equals(PageStatus.draft))
				.count();
			case published ->  wrapper.getSection().getPages().stream()
				.filter(p -> p != null)
				.filter(p -> p.getPageStatus() != null &&  p.getPageStatus().equals(PageStatus.published))
				.count();
			case inRevision ->  wrapper.getSection().getPages().stream()
				.filter(p -> p != null)
				.filter(p -> p.getPageStatus() != null &&  p.getPageStatus().equals(PageStatus.inRevision))
				.count();
			case closed ->  wrapper.getSection().getPages().stream()
				.filter(p -> p != null)
				.filter(p -> p.getPageStatus() != null &&  p.getPageStatus().equals(PageStatus.closed))
				.count();
			case passed -> wrapper.getPassedEl() != null ? wrapper.getPassedEl() : wrapper.getPassed();
			case score -> wrapper.getScoreEl() != null ? wrapper.getScoreEl() : wrapper.getScore();
			case changeStatus -> getChangeStatus(wrapper);
			case openSection -> wrapper.getSectionLink();
			default -> "ERROR";
		};
	}
	
	private Object getChangeStatus(AssessmentSectionWrapper wrapper) {
		FormLink changeButton = wrapper.getButton();
		if (changeButton == null && wrapper.getSection() != null) {
			SectionStatus status = wrapper.getSection().getSectionStatus();
			if (status == null) {
				status = SectionStatus.notStarted;
			}
			return status;
		}
		return changeButton;
	}

	public enum AssessmentSectionCols implements FlexiSortableColumnDef {
		sectionName("table.header.section"),
		numOfPages("table.header.numpages"),
		newEntries("table.header.num.new.entries"),
		inProgress("table.header.num.in.progress"),
		done("table.header.num.done"),
		draft("table.header.num.draft"),
		published("table.header.num.published"),
		inRevision("table.header.num.in.revision"),
		closed("table.header.num.closed"),
		passed("table.header.passed"),
		score("table.header.score"),
		changeStatus("table.header.change.status"),
		openSection("table.header.action.open.section");
		
		private final String i18nKey;
		
		private AssessmentSectionCols(String i18nKey) {
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
}
