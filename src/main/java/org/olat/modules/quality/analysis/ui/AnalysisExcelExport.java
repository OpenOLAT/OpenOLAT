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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.Figures;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextToTaxonomyLevel;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisExcelExport extends EvaluationFormExcelExport {
	
	private final Figures analysisFigures;
	private final List<QualityContext> contexts;
	private final Map<EvaluationFormSession, QualityDataCollection> sessionToDataCollection;
	private final List<QualityContextToTaxonomyLevel> contextTaxonomyLevel;
	private final List<TaxonomyLevel> taxonomyLevels;
	private final List<String> worksheetNames;

	@Autowired
	private QualityService qualityService;

	public AnalysisExcelExport(Locale locale, RepositoryEntry formEntry, Form form, SessionFilter filter,
			Comparator<EvaluationFormSession> comparator, UserColumns userColumns, String fileName,
			Figures analysisFigures) {
		super(locale, formEntry, form, filter, comparator, userColumns, fileName);
		this.analysisFigures = analysisFigures;
		
		contexts = qualityService.loadContextBySessions(sessions);
		sessionToDataCollection = contexts.stream()
				.collect(Collectors.toMap(
						QualityContext::getEvaluationFormSession,
						QualityContext::getDataCollection,
						(u, v) -> u));
		
		contextTaxonomyLevel = qualityService.loadContextTaxonomyLevel(sessions);
		contextTaxonomyLevel.sort(Comparator
				.comparingLong((QualityContextToTaxonomyLevel ctl) -> ctl.getContext().getEvaluationFormSession().getKey())
				.thenComparingLong((QualityContextToTaxonomyLevel ctl) -> ctl.getTaxonomyLevel().getKey())
			);
		taxonomyLevels = contextTaxonomyLevel.stream()
				.map(QualityContextToTaxonomyLevel::getTaxonomyLevel)
				.distinct()
				.sorted(Comparator.comparingLong((TaxonomyLevel level) -> level.getKey()))
				.toList();
		
		worksheetNames = new ArrayList<>();
		worksheetNames.add(super.getWorksheetNames().get(0));
		worksheetNames.add("context");
		if (!contextTaxonomyLevel.isEmpty()) {
			worksheetNames.add("context_taxonomy");
			worksheetNames.add("taxonomy");
		}
		worksheetNames.add(super.getWorksheetNames().get(1));
	}
	
	@Override
	protected List<String> getWorksheetNames() {
		return worksheetNames;
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
			addContextContent(workbook, exportSheet);
		}
		if (!contextTaxonomyLevel.isEmpty()) {
			if (sheetNum == 2) {
				addContextTaxonomyHeader(workbook, exportSheet);
				addContextTaxonomyContent(workbook, exportSheet);
			}
			if (sheetNum == 3) {
				addTaxonomyHeader(workbook, exportSheet);
				addTaxonomyContent(workbook, exportSheet);
			}
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

	private void addContextContent(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
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
	
	private void addContextTaxonomyHeader(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		Row headerRow = exportSheet.newRow();
		
		AtomicInteger col = new AtomicInteger();
		headerRow.addCell(col.getAndIncrement(), "oo_session_id", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_taxonomy_level_id", workbook.getStyles().getBottomAlignStyle());
	}

	private void addContextTaxonomyContent(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		for (QualityContextToTaxonomyLevel contextToTaxonomyLevel : contextTaxonomyLevel) {
			Row row = exportSheet.newRow();
			row.addCell(0, contextToTaxonomyLevel.getContext().getEvaluationFormSession().getKey(), workbook.getStyles().getIntegerStyle());
			row.addCell(1, contextToTaxonomyLevel.getTaxonomyLevel().getKey(), workbook.getStyles().getIntegerStyle());
		}
	}
	
	private void addTaxonomyHeader(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		Row headerRow = exportSheet.newRow();
		
		AtomicInteger col = new AtomicInteger();
		headerRow.addCell(col.getAndIncrement(), "oo_taxonomy_level_id", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_taxonomy_level_external_id", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_taxonomy_level_identitifer", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_taxonomy_level_path", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_taxonomy_level_title", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_taxonomy_type_title", workbook.getStyles().getBottomAlignStyle());
		headerRow.addCell(col.getAndIncrement(), "oo_taxonomy_level_sort_order", workbook.getStyles().getBottomAlignStyle());
	}

	private void addTaxonomyContent(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		Translator taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, translator.getLocale());
		
		for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
			Row row = exportSheet.newRow();
			AtomicInteger col = new AtomicInteger();
			row.addCell(col.getAndIncrement(), taxonomyLevel.getKey(), workbook.getStyles().getIntegerStyle());
			row.addCell(col.getAndIncrement(), taxonomyLevel.getExternalId());
			row.addCell(col.getAndIncrement(), taxonomyLevel.getIdentifier());
			row.addCell(col.getAndIncrement(), taxonomyLevel.getMaterializedPathIdentifiers());
			String levelTitle = TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, taxonomyLevel);
			row.addCell(col.getAndIncrement(), levelTitle);
			String typeTitle = taxonomyLevel.getType() != null? taxonomyLevel.getType().getDisplayName(): null;
			row.addCell(col.getAndIncrement(), typeTitle);
			row.addCell(col.getAndIncrement(), taxonomyLevel.getSortOrder(), workbook.getStyles().getIntegerStyle());
		}
	}

	@Override
	protected Figures getCustomFigures() {
		return analysisFigures;
	}

}
