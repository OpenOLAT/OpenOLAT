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
package org.olat.course.nodes.gta.ui.component;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.ui.CoachedElementRow;

/**
 * 
 * Initial date: 3 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SubmissionDateCellRenderer implements FlexiCellRenderer {
	
	private final Formatter formatter;
	private final Translator translator;
	
	public SubmissionDateCellRenderer(Translator translator) {
		this.translator = translator;
		formatter = Formatter.getInstance(translator.getLocale());
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {

		Object object = source.getFlexiTableElement().getTableDataModel().getObject(row);
		if(object instanceof CoachedElementRow) {
			CoachedElementRow ciRow = (CoachedElementRow)object;
			TaskProcess status = ciRow.getTaskStatus();
			if(status == null || status == TaskProcess.assignment || status == TaskProcess.submit) {
				//do nothing
			} else  {
				Date date = ciRow.getSyntheticSubmissionDate();
				if(date != null) {
					if(ciRow.getHasSubmittedDocuments()) {
						target.append(formatter.formatDate(date));
						// Submission only
						Date submissionDate = ciRow.getCollectionDate() != null ? ciRow.getCollectionDate() : ciRow.getSubmissionDate();
						marker(target, submissionDate, ciRow.getSubmissionDueDate(), ciRow.getLateSubmissionDueDate());
					} else {
						target.append(translator.translate("no.submission"));
					}
				}
			}
		}
	}
	
	private void marker(StringOutput target, Date submissionDate, DueDate dueDate, DueDate lateDueDate) {
		if(submissionDate == null) return;
		
		Date date = dueDate == null ? null : dueDate.getReferenceDueDate();
		Date lateDate = lateDueDate == null ? null : lateDueDate.getReferenceDueDate();
		Date extensionDate = dueDate == null ? null : dueDate.getOverridenDueDate();
		
		if(extensionDate != null
				&& (date == null || date.before(extensionDate))
				&& (lateDate == null || lateDate.before(extensionDate))) {
			target.append("&#160;<span class='o_labeled_light o_process_status_extended'>").append(translator.translate("label.extended")).append("</span>");
		} else if(date != null && lateDate != null && submissionDate.after(date)) {
			target.append("&#160;<span class='o_labeled_light o_process_status_late'>").append(translator.translate("label.late")).append("</span>");
		}
	}
	
	public static Date cascading(CoachedElementRow ciRow) {
		Date date = ciRow.getSubmissionDate();
		if(date == null || (ciRow.getSubmissionRevisionsDate() != null && ciRow.getSubmissionRevisionsDate().after(date))) {
			date = ciRow.getSubmissionRevisionsDate();
		}
		if(date == null || (ciRow.getCollectionDate() != null && ciRow.getCollectionDate().after(date))) {
			date = ciRow.getCollectionDate();
		}
		return date;
	}
}
