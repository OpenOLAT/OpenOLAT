/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.assessment.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * 
 * @author Christian Guretzki
 */
public class EfficiencyStatementArchiver {
	
	private static final Logger log = Tracing.createLoggerFor(EfficiencyStatementArchiver.class);
	
	private final boolean withGrades;
	private Translator translator;
	private final List<UserPropertyHandler> userPropertyHandler;

	public EfficiencyStatementArchiver(Locale locale) {
		withGrades = CoreSpringFactory.getImpl(GradeModule.class).isEnabled();
		UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
		// fallback for user properties translation
		translator = userManager.getPropertyHandlerTranslator(
				Util.createPackageTranslator(EfficiencyStatement.class, locale,
				Util.createPackageTranslator(CourseNode.class, locale)));
		if (withGrades) {
			translator = Util.createPackageTranslator(GradeUIFactory.class, locale, translator);
		}
		// list of user property handlers used in this archiver
		userPropertyHandler = userManager.getUserPropertyHandlersFor(EfficiencyStatementArchiver.class.getCanonicalName(), true);
	}

	public File archive(List<EfficiencyStatement> efficiencyStatements, Identity identity, File archiveDir) {
		File archiveFile = new File(archiveDir, "EfficiencyStatements.xlsx");
		try(OutputStream out = new FileOutputStream(archiveFile);
			OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			
			appendIdentityIntro(identity, sheet);
			
			for(EfficiencyStatement efficiencyStatement:efficiencyStatements) {
				appendIntro(efficiencyStatement, sheet);
				appendDetailsHeader(sheet);
				appendDetailsTable(efficiencyStatement, sheet);
				sheet.newRow();
			}
		} catch (IOException e) {
			log.error("Unable to export xlsx", e);
		}
		return archiveFile;
	}

	private void appendDetailsHeader(OpenXMLWorksheet sheet) {
		Row row = sheet.newRow();
		int counter = 0;
		row.addCell(counter++, translator.translate("table.header.node"));
		row.addCell(counter++, translator.translate("table.header.details"));
		row.addCell(counter++, translator.translate("table.header.type")); 
		row.addCell(counter++, translator.translate("table.header.attempts"));
		row.addCell(counter++, translator.translate("table.header.score"));
		if (withGrades) {
			row.addCell(counter++, translator.translate("table.header.grade"));
		}
		row.addCell(counter++, translator.translate("table.header.passed"));
	}

	private void appendDetailsTable(EfficiencyStatement efficiencyStatement, OpenXMLWorksheet sheet) {
		for (Map<String,Object> nodeData : efficiencyStatement.getAssessmentNodes()) {
			Row row = sheet.newRow();
			int counter = 0;
			appendValue(nodeData, AssessmentHelper.KEY_TITLE_SHORT, counter++, row);
			appendValue(nodeData, AssessmentHelper.KEY_TITLE_LONG, counter++, row);
			appendTypeValue(nodeData, AssessmentHelper.KEY_TYPE, counter++, row);
			appendValue(nodeData, AssessmentHelper.KEY_ATTEMPTS, counter++, row);
			appendValue(nodeData, AssessmentHelper.KEY_SCORE, counter++, row);
			if (withGrades) {
				appendGradeValue(nodeData, counter++, row);
			}
			appendValue(nodeData, AssessmentHelper.KEY_PASSED, counter++, row);
		}
	}

	private void appendTypeValue(Map<String,Object> nodeData, String key, int col, Row row) {
		Object value = nodeData.get(key);
		if (value != null && (value instanceof String)) {
			String valueString = (String)value;
			if (valueString.equals("st")) {
				//
			} else {
				row.addCell(col,translator.translate("title_" + valueString));
			}
		}
	}

	private void appendGradeValue(Map<String,Object> nodeData, int col, Row row) {
		Object gradeValue = nodeData.get(AssessmentHelper.KEY_GRADE);
		if (gradeValue != null && (gradeValue instanceof String)) {
			String grade = (String)gradeValue;
			Object performanceClassIdentValue = nodeData.get(AssessmentHelper.KEY_PERFORMANCE_CLASS_IDENT);
			String performanceClassIdent = null;
			if (performanceClassIdentValue != null && (performanceClassIdentValue instanceof String)) {
				performanceClassIdent = (String) performanceClassIdentValue;
			}
			row.addCell(col, GradeUIFactory.translatePerformanceClass(translator, performanceClassIdent, grade));
		}
	}

	private void appendIdentityIntro(Identity identity, OpenXMLWorksheet sheet) {
		for (UserPropertyHandler propertyHandler : userPropertyHandler) {
			String label = translator.translate(propertyHandler.i18nColumnDescriptorLabelKey());
			String value = propertyHandler.getUserProperty(identity.getUser(), translator.getLocale());
			appendLine(label, (StringHelper.containsNonWhitespace(value) ? value : ""), sheet);
		}
	}
	
	private void appendIntro(EfficiencyStatement efficiencyStatement, OpenXMLWorksheet sheet) {
		appendLine(translator.translate("course"), efficiencyStatement.getCourseTitle() + "  (" + efficiencyStatement.getCourseRepoEntryKey().toString() +")", sheet);
		appendLine(translator.translate("date"), StringHelper.formatLocaleDateTime(efficiencyStatement.getLastUpdated(), I18nModule.getDefaultLocale()), sheet);
		
		List<Map<String,Object>> nodeDataList = efficiencyStatement.getAssessmentNodes();
		if(!nodeDataList.isEmpty()) {
			Map<String,Object> nodeData = nodeDataList.get(0);
			if (nodeData != null) {
				appendLabelValueLine(nodeData, translator.translate("table.header.score"), AssessmentHelper.KEY_SCORE, sheet);
				appendLabelValueLine(nodeData, translator.translate("table.header.passed"), AssessmentHelper.KEY_PASSED, sheet);
			}
		}
	}

	private void appendValue(Map<String,Object> nodeData, String key, int col, Row row) {
		Object value = nodeData.get(key);
		if(value instanceof String) {
			row.addCell(col, (String)value);
		} else if(value instanceof Number) {
			row.addCell(col, (Number)value, null);
		} else if(value instanceof Boolean) {
			row.addCell(col, ((Boolean)value).toString(), null);
		}
	}

	private void appendLabelValueLine(Map<String,Object> nodeData, String label, String key, OpenXMLWorksheet sheet) {
		Row row = sheet.newRow();
		row.addCell(0, label);
		appendValue(nodeData, key, 1, row);
	}

	private void appendLine(String label, String value, OpenXMLWorksheet sheet) {
		Row row = sheet.newRow();
		row.addCell(0, label);
		row.addCell(1, value);
	}
}