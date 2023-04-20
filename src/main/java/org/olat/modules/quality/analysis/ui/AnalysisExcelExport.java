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
package org.olat.modules.quality.analysis.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisExcelExport extends EvaluationFormExcelExport {
	
	private final List<QualityContext> contexts;
	private final Map<EvaluationFormSession, QualityDataCollection> sessionToDataCollection;

	@Autowired
	private QualityService qualityService;

	public AnalysisExcelExport(Form form, SessionFilter filter, Comparator<EvaluationFormSession> comparator,
			UserColumns userColumns, String fileName) {
		super(form, filter, comparator, userColumns, fileName);
		
		contexts = qualityService.loadContextBySessions(sessions);
		sessionToDataCollection = contexts.stream()
				.collect(Collectors.toMap(
						QualityContext::getEvaluationFormSession,
						QualityContext::getDataCollection,
						(u, v) -> u));
	}
	
	@Override
	protected List<String> getWorksheetNames() {
		return List.of(super.getWorksheetNames().get(0), "context");
	}
	
	@Override
	protected void addCustomHeader(OpenXMLWorkbook workbook, Row headerRow, AtomicInteger col) {
		headerRow.addCell(col.getAndIncrement(), "oo_session_id", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_data_collection_id", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_data_collection_title", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_topic_repo_id", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_topic_repo_external_ref", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_topic_repo_external_id", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_topic_repo_title", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_topic_user_id", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_topic_user_external_id", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_topic_user_firstname", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_topic_user_lastname", workbook.getStyles().getBottomAlignStyle());
	}

	@Override
	protected void addCustomColumns(OpenXMLWorkbook workbook, EvaluationFormSession session, Row row,
			AtomicInteger col) {
		QualityDataCollection dataCollection = sessionToDataCollection.get(session);
		if (dataCollection == null) {
			return;
		}
		
		row.addCell(col.getAndIncrement(), session.getKey(), workbook.getStyles().getTopAlignStyle());
		row.addCell(col.getAndIncrement(), dataCollection.getKey(), workbook.getStyles().getTopAlignStyle());
		row.addCell(col.getAndIncrement(), dataCollection.getTitle(), workbook.getStyles().getTopAlignStyle());
		
		RepositoryEntry repositoryEntry = dataCollection.getTopicRepositoryEntry();
		if (repositoryEntry != null) {
			row.addCell(col.getAndIncrement(), repositoryEntry.getKey(), workbook.getStyles().getTopAlignStyle());
			row.addCell(col.getAndIncrement(), repositoryEntry.getExternalRef(), workbook.getStyles().getTopAlignStyle());
			row.addCell(col.getAndIncrement(), repositoryEntry.getExternalId(), workbook.getStyles().getTopAlignStyle());
			row.addCell(col.getAndIncrement(), repositoryEntry.getDisplayname(), workbook.getStyles().getTopAlignStyle());
		} else {
			col.getAndIncrement();
			col.getAndIncrement();
			col.getAndIncrement();
			col.getAndIncrement();
		}
		
		Identity identity = dataCollection.getTopicIdentity();
		if (identity != null) {
			row.addCell(col.getAndIncrement(), identity.getKey(), workbook.getStyles().getTopAlignStyle());
			row.addCell(col.getAndIncrement(), identity.getExternalId(), workbook.getStyles().getTopAlignStyle());
			row.addCell(col.getAndIncrement(), identity.getUser().getFirstName(), workbook.getStyles().getTopAlignStyle());
			row.addCell(col.getAndIncrement(), identity.getUser().getLastName(), workbook.getStyles().getTopAlignStyle());
		}
	}
	
	@Override
	protected void addCustomWorksheet(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet, int sheetNum) {
		if (sheetNum == 1) {
			addContextHeader(workbook, exportSheet);
			addContentContent(workbook, exportSheet);
		}
	}

	private void addContextHeader(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		Row headerRow = exportSheet.newRow();
		
		AtomicInteger col = new AtomicInteger();
		headerRow.addCell(col.getAndIncrement(), "oo_session_id", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_context_repo_id", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_context_repo_external_ref", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_context_repo_external_id", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_context_repo_title", workbook.getStyles().getBottomAlignStyle());
	}

	private void addContentContent(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		for (QualityContext context : contexts) {
			if (context.getEvaluationFormSession() != null) {
				if (context.getAudienceRepositoryEntry() != null) {
					RepositoryEntry repositoryEntry = context.getAudienceRepositoryEntry();
					Row row = exportSheet.newRow();
					AtomicInteger col = new AtomicInteger();
					row.addCell(col.getAndIncrement(), context.getEvaluationFormSession().getKey(), workbook.getStyles().getTopAlignStyle());
					row.addCell(col.getAndIncrement(), repositoryEntry.getKey(), workbook.getStyles().getTopAlignStyle());
					row.addCell(col.getAndIncrement(), repositoryEntry.getExternalRef(), workbook.getStyles().getTopAlignStyle());
					row.addCell(col.getAndIncrement(), repositoryEntry.getExternalId(), workbook.getStyles().getTopAlignStyle());
					row.addCell(col.getAndIncrement(), repositoryEntry.getDisplayname(), workbook.getStyles().getTopAlignStyle());
				}
			}
		}
	}

}
