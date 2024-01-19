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
package org.olat.course.assessment.ui.inspection.elements;

import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionRow;

/**
 * 
 * Initial date: 20 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InspectionPeriodCellRenderer implements FlexiCellRenderer {
	
	private final Formatter format;
	
	public InspectionPeriodCellRenderer(Locale locale) {
		format = Formatter.getInstance(locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue == null) {
			cellValue = source.getFormItem().getTableDataModel().getObject(row);
		}

		if(cellValue instanceof AssessmentInspectionRow inspectionRow) {
			render(target, inspectionRow.getFromDate(), inspectionRow.getToDate());
		}
	}
	
	private void render(StringOutput target, Date start, Date end) {
		if(start != null) {
			target.append(format.formatDateAndTime(start));
			if(end != null) {
				target.append(" - ");
			}
		}
		if(end != null) {
			if(DateUtils.isSameDay(start, end)) {
				target.append(format.formatTimeShort(end));
			} else {
				target.append(format.formatDateAndTime(end));
			}	
		}
	}
}
