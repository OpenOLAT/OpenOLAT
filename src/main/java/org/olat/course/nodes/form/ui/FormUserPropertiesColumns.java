/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.form.ui;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.openxml.OpenXMLWorkbookStyles;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.ui.UserPropertiesColumns;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: Jan 28, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FormUserPropertiesColumns extends UserPropertiesColumns {

	private final Translator translator;
	private final boolean multiParticipation;

	public FormUserPropertiesColumns(List<UserPropertyHandler> userPropertyHandlers, Translator translator,
			boolean multiParticipation) {
		super(userPropertyHandlers, translator);
		this.translator = translator;
		this.multiParticipation = multiParticipation;
	}

	@Override
	public void addHeaderColumns(Row row, AtomicInteger col, OpenXMLWorkbookStyles styles) {
		super.addHeaderColumns(row, col, styles);
		
		if (multiParticipation) {
			row.addCell(col.getAndIncrement(), translator.translate("table.header.submission.number"), styles.getBottomAlignStyle());
		}
		row.addCell(col.getAndIncrement(), translator.translate("table.header.submission.date"), styles.getBottomAlignStyle());
	}

	@Override
	public void addColumns(EvaluationFormSession session, Row row, AtomicInteger col, OpenXMLWorkbookStyles styles) {
		super.addColumns(session, row, col, styles);
		
		if (multiParticipation) {
			row.addCell(col.getAndIncrement(), String.valueOf(session.getParticipation().getRun()));
		}
		row.addCell(col.getAndIncrement(), session.getSubmissionDate(), styles.getDateTimeStyle());
	}

}
