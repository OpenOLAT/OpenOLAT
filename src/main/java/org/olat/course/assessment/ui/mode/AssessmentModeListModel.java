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
package org.olat.course.assessment.ui.mode;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.model.EnhancedStatus;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeListModel extends DefaultFlexiTableDataModel<AssessmentMode> implements SortableFlexiTableDataModel<AssessmentMode> {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentModeListModel.class);
	private static final Cols[] COLS = Cols.values();
	
	private final Translator translator;
	private final AssessmentModeCoordinationService coordinationService;
	
	public AssessmentModeListModel(FlexiTableColumnModel columnsModel, Translator translator,
			AssessmentModeCoordinationService coordinationService) {
		super(columnsModel);
		this.translator = translator;
		this.coordinationService = coordinationService;
	}

	@Override
	public AssessmentModeListModel createCopyWithEmptyList() {
		return new AssessmentModeListModel(getTableColumnModel(),translator,  coordinationService);
	}

	@Override
	public Object getValueAt(int row, int col) {
		AssessmentMode mode = getObject(row);
		return getValueAt(mode, col);
	}
		
	@Override
	public Object getValueAt(AssessmentMode mode, int col) {
		switch(COLS[col]) {
			case status: return getStatus(mode);
			case course: return mode.getRepositoryEntry().getDisplayname();
			case externalId: return mode.getRepositoryEntry().getExternalId();
			case externalRef: return mode.getRepositoryEntry().getExternalRef();
			case name: return mode.getName();
			case begin: return mode.getBegin();
			case end: return mode.getEnd();
			case leadTime: return mode.getLeadTime();
			case followupTime: return mode.getFollowupTime();
			case target: return mode.getTargetAudience();
			case start: return canStart(mode);
			case stop: return canStop(mode);
			default: return "ERROR";
		}
	}
	
	private boolean canStart(AssessmentMode mode) {
		boolean canStart = mode.isManualBeginEnd();
		if(canStart) {
			canStart = coordinationService.canStart(mode);
		}
		return canStart;
	}
	
	private boolean canStop(AssessmentMode mode) {
		boolean canStop = mode.isManualBeginEnd();
		if(canStop) {
			canStop = coordinationService.canStop(mode);
		}
		return canStop;
	}
	
	private EnhancedStatus getStatus(AssessmentMode mode) {
		List<String> warnings = null;
		try {
			if(StringHelper.containsNonWhitespace(mode.getStartElement())) {
				ICourse course = CourseFactory.loadCourse(mode.getRepositoryEntry());
				CourseNode node = course.getRunStructure().getNode(mode.getStartElement());
				if(node == null) {
					warnings = new ArrayList<>(2);
					warnings.add(translator.translate("warning.missing.start.element"));
				}
			}
			if(StringHelper.containsNonWhitespace(mode.getElementList())) {
				ICourse course = CourseFactory.loadCourse(mode.getRepositoryEntry());
				String elements = mode.getElementList();
				for(String element:elements.split(",")) {
					CourseNode node = course.getRunStructure().getNode(element);
					if(node == null) {
						if(warnings == null) {
							warnings = new ArrayList<>(2);
						}
						warnings.add(translator.translate("warning.missing.element"));
						break;
					}
				}
			}
		} catch (CorruptedCourseException e) {
			log.error("", e);
			if(warnings == null) {
				warnings = new ArrayList<>(2);
			}
			warnings.add(translator.translate("cif.error.corrupted"));
		}
		return new EnhancedStatus(mode.getStatus(), mode.getEndStatus(), warnings);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<AssessmentMode> views = new AssessmentModeListModelSort(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	public boolean updateModeStatus(TransientAssessmentMode modeToUpdate) {
		boolean updated = false;
		
		List<AssessmentMode> modes = getObjects();
		for(AssessmentMode mode:modes) {
			if(mode.getKey().equals(modeToUpdate.getModeKey())) {
				if(mode.getStatus() != modeToUpdate.getStatus()) {
					mode.setStatus(modeToUpdate.getStatus());
					updated = true;
				}
			}
		}
		
		return updated;
	}
	
	public enum Cols implements FlexiSortableColumnDef {
		status("table.header.status"),
		course("table.header.course"),
		externalId("table.header.externalId"),
		externalRef("table.header.externalRef"),
		name("table.header.name"),
		begin("table.header.begin"),
		end("table.header.end"),
		leadTime("table.header.leadTime"),
		followupTime("table.header.followupTime"),
		target("table.header.target"),
		start(""),
		stop("");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
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
