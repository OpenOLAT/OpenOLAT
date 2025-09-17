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
package org.olat.modules.forms.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorkbookStyles;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.openxml.workbookstyle.CellStyle;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormStatistic;
import org.olat.modules.forms.Figure;
import org.olat.modules.forms.Figures;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.Disclaimer;
import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.HTMLParagraph;
import org.olat.modules.forms.model.xml.HTMLRaw;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.SessionInformations;
import org.olat.modules.forms.model.xml.SessionInformations.InformationType;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.Spacer;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.model.xml.Title;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormExcelExport {
	
	private static final Logger log = Tracing.createLoggerFor(EvaluationFormExcelExport.class);
	private static final List<String> MERGE_TYPES = List.of(HTMLParagraph.TYPE, HTMLRaw.TYPE);

	protected final Translator translator;
	private final RepositoryEntry formEntry;
	private final String fileName;
	private final UserColumns userColumns;
	private final List<AbstractElement> elements;
	private final EvaluationFormResponses responses;
	protected final List<EvaluationFormSession> sessions;
	private final EvaluationFormStatistic statistic;
	private final List<String> mergedElementIds = new ArrayList<>();
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public EvaluationFormExcelExport(Locale locale, RepositoryEntry formEntry, Form form,
			SessionFilter filter, Comparator<EvaluationFormSession> comparator, UserColumns userColumns, String fileName) {
		this.translator = Util.createPackageTranslator(EvaluationFormExcelExport.class, locale);
		this.formEntry = formEntry;
		this.fileName = createFileName(fileName);
		this.userColumns = userColumns;

		CoreSpringFactory.autowireObject(this);
		elements = evaluationFormManager.getUncontainerizedElements(form);
		responses = evaluationFormManager.loadResponsesBySessions(filter);
		sessions = evaluationFormManager.loadSessionsFiltered(filter, 0, -1);
		statistic = evaluationFormManager.getSessionsStatistic(filter);
		if (comparator != null) {
			sessions.sort(comparator);
		}
	}
	
	private String createFileName(String surveyName) {
		return new StringBuilder()
				.append(StringHelper.transformDisplayNameToFileSystemName(surveyName))
				.append("_")
				.append(Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())))
				.append(".xlsx")
				.toString();
	}
	
	public String getFileName() {
		return fileName;
	}

	public OpenXMLWorkbookResource createMediaResource() {
		return new OpenXMLWorkbookResource(fileName) {
			@Override
			protected void generate(OutputStream out) {
				createWorkbook(out);
			}
		};
	}
	
	public void export(ZipOutputStream out, String currentPath) throws IOException {
		String name = ZipUtil.concat(currentPath, fileName);
		try (OutputStream shieldOut = new ShieldOutputStream(out)) {
			out.putNextEntry(new ZipEntry(name));
			createWorkbook(shieldOut);
			out.closeEntry();
		} catch (IOException e) {
			log.error("", e);
		}
	}

	public void createWorkbook(OutputStream out) {
		List<String> sheetNames = getWorksheetNames();
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, sheetNames.size(), sheetNames)) {
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			
			mergedElementIds.clear();
			addHeader(workbook, exportSheet);
			addContent(workbook, exportSheet);
			if (sheetNames.size() > 1) {
				for (int i = 1; i < sheetNames.size() - 1; i++) {
					exportSheet = workbook.nextWorksheet();
					addCustomWorksheet(workbook, exportSheet, i);
				}
			}
			
			OpenXMLWorksheet metadataSheet = workbook.nextWorksheet();
			addMetadataContent(workbook, metadataSheet);
		} catch (IOException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	protected List<String> getWorksheetNames() {
		return List.of("response", "metadata");
	}
	
	/**
	 * @param workbook
	 * @param exportSheet
	 * @param sheetNum first custom sheet has num 1
	 */
	protected void addCustomWorksheet(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet, int sheetNum) {
		//
	}
	
	private void addHeader(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		Row headerRow = exportSheet.newRow();
		
		AtomicInteger col = new AtomicInteger();
		userColumns.addHeaderColumns(headerRow, col, workbook.getStyles());
		for (AbstractElement element: elements) {
			String elementType = element.getType();
			switch (elementType) {
			case Title.TYPE:
				addTitleHeader(workbook, headerRow, col, (Title) element);
				break;
			case HTMLParagraph.TYPE:
				addHtmlParagraphHeader(workbook, headerRow, col, (HTMLParagraph) element);
				break;
			case HTMLRaw.TYPE:
				addHtmlRawHeader(workbook, headerRow, col, (HTMLRaw) element);
				break;
			case Spacer.TYPE:
				col.getAndIncrement(); // empty column
				break;
			case TextInput.TYPE:
				col.getAndIncrement(); // no header
				break;
			case FileUpload.TYPE:
				mergeColumn(element, col);
				break;
			case Disclaimer.TYPE:
				addDisclaimerHeader(workbook, headerRow, col, (Disclaimer) element);
				break;
			case SessionInformations.TYPE:
				addSessionInformationsHeader(workbook, headerRow, col, (SessionInformations) element);
				break;
			case SingleChoice.TYPE:
				mergeColumn(element, col);
				break;
			case MultipleChoice.TYPE:
				mergeColumn(element, col);
				break;
			case Rubric.TYPE:
				addRubricHeader(workbook, headerRow, col, (Rubric) element);
				break;
			default:
				break;
			}
		}
		addCustomHeader(workbook, headerRow, col);
	}

	/**
	 * 
	 *
	 * @param workbook
	 * @param headerRow
	 * @param col
	 */
	protected void addCustomHeader(OpenXMLWorkbook workbook, Row headerRow, AtomicInteger col) {
		//
	}

	private void mergeColumn(AbstractElement element, AtomicInteger col) {
		int index = elements.indexOf(element);
		if (index > 1) {
			AbstractElement previousElement = elements.get(index - 1);
			if (MERGE_TYPES.contains(previousElement.getType())) {
				mergedElementIds.add(element.getId());
				return;
			}
		}
		// no merge => new column
		col.getAndIncrement();
	}
	
	private void decrementMergedColumn(AbstractElement element, AtomicInteger col) {
		if (mergedElementIds.contains(element.getId())) {
			col.getAndDecrement();
		}
	}

	private void addContent(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		for (EvaluationFormSession session: sessions) {
			Row row = exportSheet.newRow();
			AtomicInteger col = new AtomicInteger();
			userColumns.addColumns(session, row, col, workbook.getStyles());
			addResponses(workbook, session, row, col);
		}
	}
	
	private void addResponses(OpenXMLWorkbook workbook, EvaluationFormSession session, Row row, AtomicInteger col) {
		for (AbstractElement element: elements) {
			String elementType = element.getType();
			switch (elementType) {
			case Title.TYPE:
				col.getAndIncrement(); // no cell value
				break;
			case HTMLParagraph.TYPE:
				col.getAndIncrement(); // no cell value
				break;
			case HTMLRaw.TYPE:
				col.getAndIncrement(); // no cell value
				break;
			case Spacer.TYPE:
				col.getAndIncrement(); // empty column
				break;
			case TextInput.TYPE:
				addTextInput(workbook, session, row, col, (TextInput) element);
				break;
			case FileUpload.TYPE:
				addFileUpload(workbook, session, row, col, (FileUpload) element);
				break;
			case Disclaimer.TYPE:
				addDisclaimer(session, row, col, (Disclaimer) element);
				break;
			case SessionInformations.TYPE:
				addSessionInformations(workbook, session, row, col, (SessionInformations) element);
				break;
			case SingleChoice.TYPE:
				addSingleChoice(workbook, session, row, col, (SingleChoice) element);
				break;
			case MultipleChoice.TYPE:
				addMultipleChoice(workbook, session, row, col, (MultipleChoice) element);
				break;
			case Rubric.TYPE:
				addRubric(session, row, col, (Rubric) element);
				break;
			default:
				break;
			}
		}
		addCustomColumns(workbook, session, row, col);
	}

	/**
	 *
	 * @param workbook 
	 * @param session
	 * @param row
	 * @param col
	 */
	protected void addCustomColumns(OpenXMLWorkbook workbook, EvaluationFormSession session, Row row, AtomicInteger col) {
		//
	}

	private void addTitleHeader(OpenXMLWorkbook workbook, Row row, AtomicInteger col, Title title) {
		String content = title.getContent();
		content = FilterFactory.getHtmlTagAndDescapingFilter().filter(content);
		row.addCell(col.getAndIncrement(), content, workbook.getStyles().getBottomAlignStyle());
	}

	private void addHtmlParagraphHeader(OpenXMLWorkbook workbook, Row row, AtomicInteger col, HTMLParagraph htmlParagraph) {
		String content = htmlParagraph.getContent();
		content = FilterFactory.getHtmlTagAndDescapingFilter().filter(content);
		row.addCell(col.getAndIncrement(), content, workbook.getStyles().getBottomAlignStyle());
	}

	private void addHtmlRawHeader(OpenXMLWorkbook workbook, Row row, AtomicInteger col, HTMLRaw htmlRaw) {
		String content = htmlRaw.getContent();
		content = FilterFactory.getHtmlTagAndDescapingFilter().filter(content);
		row.addCell(col.getAndIncrement(), content, workbook.getStyles().getBottomAlignStyle());
	}
	
	private void addDisclaimerHeader(OpenXMLWorkbook workbook, Row row, AtomicInteger col, Disclaimer disclaimer) {
		String content = disclaimer.getAgreement();
		content = FilterFactory.getHtmlTagAndDescapingFilter().filter(content);
		row.addCell(col.getAndIncrement(), content, workbook.getStyles().getBottomAlignStyle());
	}
	
	private void addSessionInformationsHeader(OpenXMLWorkbook workbook, Row row, AtomicInteger col,
			SessionInformations sessionInformations) {
		for (InformationType informationType: sessionInformations.getInformationTypes()) {
			row.addCell(col.getAndIncrement(), informationType.name(), workbook.getStyles().getBottomAlignStyle());
		}
	}
	
	private void addRubricHeader(OpenXMLWorkbook workbook, Row row, AtomicInteger col, Rubric rubric) {
		for (Slider slider: rubric.getSliders()) {
			row.addCell(col.getAndIncrement(), getSliderLabel(slider), workbook.getStyles().getBottomAlignStyle());
		}
	}

	private void addTextInput(OpenXMLWorkbook workbook, EvaluationFormSession session, Row row, AtomicInteger col,
			TextInput textinput) {
		EvaluationFormResponse response = responses.getResponse(session, textinput.getId());
		if (response != null) {
			if (textinput.isDate()) {
				Date date = evaluationFormManager.getDate(response);
				if (date != null) {
					row.addCell(col.get(), date, workbook.getStyles().getDateStyle());
				}
			} else {
				String value = response.getStringuifiedResponse();
				if (StringHelper.containsNonWhitespace(value)) {
					value = value.replaceAll("\n", "");
					row.addCell(col.get(), value, workbook.getStyles().getTopAlignStyle());
				}
			}
		}
		col.getAndIncrement();
	}

	private void addFileUpload(OpenXMLWorkbook workbook, EvaluationFormSession session, Row row, AtomicInteger col,
			FileUpload fileUpload) {
		EvaluationFormResponse response = responses.getResponse(session, fileUpload.getId());
		decrementMergedColumn(fileUpload, col);
		if (response != null) {
			String value = response.getStringuifiedResponse();
			if (StringHelper.containsNonWhitespace(value)) {
				row.addCell(col.get(), value, workbook.getStyles().getTopAlignStyle());
			}
		}
		col.getAndIncrement();
	}

	private void addDisclaimer(EvaluationFormSession session, Row row, AtomicInteger col, Disclaimer disclaimer) {
		EvaluationFormResponse response = responses.getResponse(session, disclaimer.getId());
		if (response != null) {
			String value = response.getStringuifiedResponse();
			if (StringHelper.containsNonWhitespace(value)) {
				row.addCell(col.get(), value);
			}
		}
		col.getAndIncrement();
	}

	private void addSessionInformations(OpenXMLWorkbook workbook, EvaluationFormSession session, Row row,
			AtomicInteger col, SessionInformations sessionInformations) {
		for (InformationType informationType: sessionInformations.getInformationTypes()) {
			String value = SessionInformationsUIFactory.getValue(informationType, session);
			if (StringHelper.containsNonWhitespace(value)) {
				row.addCell(col.get(), value, workbook.getStyles().getTopAlignStyle());
			}
			col.getAndIncrement();
		}
	}

	private void addSingleChoice(OpenXMLWorkbook workbook, EvaluationFormSession session, Row row, AtomicInteger col,
			SingleChoice singleChoice) {
		Map<String, String> keyToValue = singleChoice.getChoices().asList().stream()
				.collect(Collectors.toMap(Choice::getId, Choice::getValue));
		EvaluationFormResponse response = responses.getResponse(session, singleChoice.getId());
		decrementMergedColumn(singleChoice, col);
		if (response != null) {
			String value = keyToValue.get(response.getStringuifiedResponse());
			if (value != null) {
				row.addCell(col.get(), value, workbook.getStyles().getTopAlignStyle());
			}
		}
		col.getAndIncrement();
	}

	private void addMultipleChoice(OpenXMLWorkbook workbook, EvaluationFormSession session, Row row, AtomicInteger col,
			MultipleChoice multipleChoice) {
		Map<String, String> keyToValue = multipleChoice.getChoices().asList().stream()
				.collect(Collectors.toMap(Choice::getId, Choice::getValue));
		List<EvaluationFormResponse> responseList = responses.getResponses(session, multipleChoice.getId());
		List<String> values = new ArrayList<>(responseList.size());
		decrementMergedColumn(multipleChoice, col);
		for (EvaluationFormResponse response: responseList) {
			String key = response.getStringuifiedResponse();
			String value = keyToValue.get(response.getStringuifiedResponse());
			if (value != null) {
				values.add(value);
			} else {
				values.add(key);
			}
		}
		String joinedValues = values.stream().collect(Collectors.joining(","));
		if (StringHelper.containsNonWhitespace(joinedValues)) {
			row.addCell(col.get(), joinedValues, workbook.getStyles().getTopAlignStyle());
		}
		col.getAndIncrement();
	}

	private void addRubric(EvaluationFormSession session, Row row, AtomicInteger col, Rubric rubric) {
		for (Slider slider: rubric.getSliders()) {
			addSlider(session, row, col	, rubric, slider);
		}
	}

	private void addSlider(EvaluationFormSession session, Row row, AtomicInteger col, Rubric rubric, Slider slider) {
		EvaluationFormResponse response = responses.getResponse(session, slider.getId());
		if (response != null) {
			BigDecimal value = response.getNumericalResponse();
			if (value != null) {
				double scaledValue = rubric.getScaleType().getStepValue(rubric.getSteps(), value.intValue());
				row.addCell(col.get(), scaledValue, null);
			}
		}
		col.getAndIncrement();
	}

	private String getSliderLabel(Slider slider) {
		StringBuilder sb = new StringBuilder();
		if (StringHelper.containsNonWhitespace(slider.getStartLabel()) && StringHelper.containsNonWhitespace(slider.getEndLabel())) {
			sb.append(slider.getStartLabel()).append(" / ").append(slider.getEndLabel());
		} else if (StringHelper.containsNonWhitespace(slider.getStartLabel())) {
			sb.append(slider.getStartLabel());
		} else if (StringHelper.containsNonWhitespace(slider.getEndLabel())) {
			sb.append(slider.getEndLabel());
		}
		return FilterFactory.getHtmlTagAndDescapingFilter().filter(sb.toString());
	}
	
	protected void addMetadataContent(OpenXMLWorkbook workbook, OpenXMLWorksheet sheet) {
		Row row = sheet.newRow();
		row.addCell(0, translator.translate("report.excel.form"), workbook.getStyles().getTopAlignStyle());
		row.addCell(1, formEntry.getDisplayname() , workbook.getStyles().getTopAlignStyle());
		row = sheet.newRow();
		row.addCell(0, translator.translate("report.excel.form.id"), workbook.getStyles().getTopAlignStyle());
		row.addCell(1, formEntry.getKey(), workbook.getStyles().getIntegerStyle());
		
		List<Figure> figures = FiguresFactory.createFigures(translator, getCustomFigures(), statistic);
		for (Figure figure : figures) {
			row = sheet.newRow();
			row.addCell(0, figure.getName(), workbook.getStyles().getTopAlignStyle());
			
			CellStyle valueStyle = StringHelper.isLong(figure.getValue())
					? workbook.getStyles().getIntegerStyle()
					: workbook.getStyles().getTopAlignStyle();
			row.addCell(1, figure.getValue(), valueStyle);
		}
	}
	
	protected Figures getCustomFigures() {
		return null;
	}

	public interface UserColumns {
		
		public void addHeaderColumns(Row row, AtomicInteger col, OpenXMLWorkbookStyles styles);
		
		public void addColumns(EvaluationFormSession session, Row row, AtomicInteger col, OpenXMLWorkbookStyles styles);
		
	}
}
