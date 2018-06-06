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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.Disclaimer;
import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.model.xml.Form;
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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormExport extends OpenXMLWorkbookResource {
	
	private static final OLog log = Tracing.createLoggerFor(EvaluationFormExport.class);

	private final Form form;
	private final List<EvaluationFormSession> sessions;
	private final ReportHelper reportHelper;
	private final EvaluationFormResponses responses;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public EvaluationFormExport(Form form, List<EvaluationFormSession> sessions, ReportHelper reportHelper, String surveyName) {
		super(label(surveyName));
		this.form = form;
		this.sessions = sessions;
		this.reportHelper = reportHelper;
		CoreSpringFactory.autowireObject(this);
		responses = evaluationFormManager.loadResponsesBySessions(sessions);
	}
	
	private static final String label(String surveyName) {
		return StringHelper.transformDisplayNameToFileSystemName(surveyName)
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
	}

	@Override
	protected void generate(OutputStream out) {
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			addHeader(exportSheet);
			addContent(exportSheet);
		} catch (IOException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void addHeader(OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		
		Row headerRow = exportSheet.newRow();
		
		int col = 1;
		for (EvaluationFormSession session: sessions) {
			String name = reportHelper.getLegend(session).getName();
			if (StringHelper.containsNonWhitespace(name)) {
				headerRow.addCell(col, name);
			}
			col++;
		}
	}

	private void addContent(OpenXMLWorksheet exportSheet) {
		for (AbstractElement element: form.getElements()) {
			String elementType = element.getType();
			switch (elementType) {
			case Title.TYPE:
				addTitle(exportSheet, (Title) element);
				break;
			case HTMLRaw.TYPE:
				addHtmlRaw(exportSheet, (HTMLRaw) element);
				break;
			case Spacer.TYPE:
				exportSheet.newRow();// empty row
				break;
			case TextInput.TYPE:
				addTextInput(exportSheet, (TextInput) element);
				break;
			case FileUpload.TYPE:
				addFileUpload(exportSheet, (FileUpload) element);
				break;
			case Disclaimer.TYPE:
				addDisclaimer(exportSheet, (Disclaimer) element);
				break;
			case SessionInformations.TYPE:
				addSessionInformations(exportSheet, (SessionInformations) element);
				break;
			case SingleChoice.TYPE:
				addSingleChoice(exportSheet, (SingleChoice) element);
				break;
			case MultipleChoice.TYPE:
				addMultipleChoice(exportSheet, (MultipleChoice) element);
				break;
			case Rubric.TYPE:
				addRubric(exportSheet, (Rubric) element);
				break;
			default:
				break;
			}
		}
	}

	private void addTitle(OpenXMLWorksheet exportSheet, Title title) {
		String content = title.getContent();
		exportSheet.newRow().addCell(0, content);
	}

	private void addHtmlRaw(OpenXMLWorksheet exportSheet, HTMLRaw htmlRaw) {
		String content = htmlRaw.getContent();
		exportSheet.newRow().addCell(0, content);
	}

	private void addTextInput(OpenXMLWorksheet exportSheet, TextInput textinput) {
		Row row = exportSheet.newRow();
		int col = 1;
		for (EvaluationFormSession session: sessions) {
			EvaluationFormResponse response = responses.getResponse(session, textinput.getId());
			if (response != null) {
				String value = response.getStringuifiedResponse();
				if (StringHelper.containsNonWhitespace(value)) {
					row.addCell(col, value.replaceAll("\n", ""));
				}
			}
			col++;
		}
	}

	private void addFileUpload(OpenXMLWorksheet exportSheet, FileUpload fileUpload) {
		Row row = exportSheet.newRow();
		int col = 1;
		for (EvaluationFormSession session: sessions) {
			EvaluationFormResponse response = responses.getResponse(session, fileUpload.getId());
			if (response != null) {
				String value = response.getStringuifiedResponse();
				if (StringHelper.containsNonWhitespace(value)) {
					row.addCell(col, value);
				}
			}
			col++;
		}
	}

	private void addDisclaimer(OpenXMLWorksheet exportSheet, Disclaimer disclaimer) {
		Row row = exportSheet.newRow();
		int col = 0;
		row.addCell(col++, disclaimer.getAgreement());
		for (EvaluationFormSession session: sessions) {
			EvaluationFormResponse response = responses.getResponse(session, disclaimer.getId());
			if (response != null) {
				String value = response.getStringuifiedResponse();
				if (StringHelper.containsNonWhitespace(value)) {
					row.addCell(col, value);
				}
			}
			col++;
		}
	}

	private void addSessionInformations(OpenXMLWorksheet exportSheet, SessionInformations sessionInformations) {
		for (InformationType informationType: sessionInformations.getInformationTypes()) {
			Row row = exportSheet.newRow();
			int col = 0;
			row.addCell(col++, informationType.name());
			for (EvaluationFormSession session: sessions) {
				String value = SessionInformationsUIFactory.getValue(informationType, session);
				if (StringHelper.containsNonWhitespace(value)) {
					row.addCell(col, value);
				}
				col++;
			}
		}
	}

	private void addSingleChoice(OpenXMLWorksheet exportSheet, SingleChoice singleChoice) {
		Row row = exportSheet.newRow();
		int col = 1;
		Map<String, String> keyToValue = singleChoice.getChoices().asList().stream()
				.collect(Collectors.toMap(Choice::getId, Choice::getValue));
		for (EvaluationFormSession session: sessions) {
			EvaluationFormResponse response = responses.getResponse(session, singleChoice.getId());
			if (response != null) {
				String value = keyToValue.get(response.getStringuifiedResponse());
				if (value != null) {
					row.addCell(col, value);
				}
			}
			col++;
		}
	}

	private void addMultipleChoice(OpenXMLWorksheet exportSheet, MultipleChoice multipleChoice) {
		Row row = exportSheet.newRow();
		int col = 1;
		Map<String, String> keyToValue = multipleChoice.getChoices().asList().stream()
				.collect(Collectors.toMap(Choice::getId, Choice::getValue));
		for (EvaluationFormSession session: sessions) {
			List<EvaluationFormResponse> responseList = responses.getResponses(session, multipleChoice.getId());
			List<String> values = new ArrayList<>(responseList.size());
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
				row.addCell(col, joinedValues);
			}
			col++;
		}
	}

	private void addRubric(OpenXMLWorksheet exportSheet, Rubric rubric) {
		for (Slider slider: rubric.getSliders()) {
			addSlider(exportSheet, slider);
		}
	}

	private void addSlider(OpenXMLWorksheet exportSheet, Slider slider) {
		Row row = exportSheet.newRow();
		int col = 0;
		row.addCell(col++, getSliderLabel(slider));
		for (EvaluationFormSession session: sessions) {
			EvaluationFormResponse response = responses.getResponse(session, slider.getId());
			if (response != null) {
				BigDecimal value = response.getNumericalResponse();
				if (value != null) {
					row.addCell(col, value.intValue(), null);
				}
			}
			col++;
		}
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
		return sb.toString();
	}
}
