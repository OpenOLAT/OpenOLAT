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
import java.util.List;
import java.util.Map;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.nodes.CourseNode;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * 
 * @author Christian Guretzki
 */

public class EfficiencyStatementArchiver {

	private static final String DELIMITER = "\t";
	private static final String EOL = "\n";
	private static final String EFFICIENCY_ARCHIVE_FILE = "efficiencyArchive.xls";

	private Translator translator;
	private List<UserPropertyHandler> userPropertyHandler;

	/**
	 * constructs an unitialised BusinessGroup, use setXXX for setting attributes
	 */
	public EfficiencyStatementArchiver() {
		Translator fallBackTranslator = Util.createPackageTranslator(CourseNode.class, I18nModule.getDefaultLocale());
		translator = Util.createPackageTranslator(this.getClass(), I18nModule.getDefaultLocale(), fallBackTranslator);
		// fallback for user properties translation
		translator = UserManager.getInstance().getPropertyHandlerTranslator(translator);		
		// list of user property handlers used in this archiver
		userPropertyHandler = UserManager.getInstance().getUserPropertyHandlersFor(EfficiencyStatementArchiver.class.getCanonicalName(), true);
	}

	public void archive(List<EfficiencyStatement> efficiencyStatements, Identity identity, File archiveFile) {
		FileUtils.save(new File(archiveFile, EFFICIENCY_ARCHIVE_FILE), toXls(efficiencyStatements, identity), "utf-8");
	}

	private String toXls(List<EfficiencyStatement> efficiencyStatements, Identity identity) {
		StringBuilder buf = new StringBuilder();
		buf.append(translator.translate("efficiencystatement.title"));
		appendIdentityIntro(buf, identity);
		for (EfficiencyStatement efficiencyStatement: efficiencyStatements) {
			buf.append(EOL);
			buf.append(EOL);
			appendIntro(buf, efficiencyStatement);
			buf.append(EOL);
			appendDetailsHeader(buf);
			appendDetailsTable(buf, efficiencyStatement);
		}
		return buf.toString();
	}

	private void appendDetailsHeader(StringBuilder buf) {
		buf.append( translator.translate("table.header.node") );
		buf.append(DELIMITER);
		buf.append( translator.translate("table.header.details") );
		buf.append(DELIMITER);
		buf.append( translator.translate("table.header.type") ); 
		buf.append(DELIMITER);
		buf.append( translator.translate("table.header.attempts") );
		buf.append(DELIMITER);
		buf.append( translator.translate("table.header.score") );
		buf.append(DELIMITER);
		buf.append( translator.translate("table.header.passed") );
		buf.append(EOL);	
	}

	private void appendDetailsTable(StringBuilder buf, EfficiencyStatement efficiencyStatement) {
		for (Map<String,Object> nodeData : efficiencyStatement.getAssessmentNodes()) {
			appendValue(buf, nodeData, AssessmentHelper.KEY_TITLE_SHORT);		
			appendValue(buf, nodeData, AssessmentHelper.KEY_TITLE_LONG);
			appendTypeValue(buf, nodeData, AssessmentHelper.KEY_TYPE);
			appendValue(buf, nodeData, AssessmentHelper.KEY_ATTEMPTS);
			appendValue(buf, nodeData, AssessmentHelper.KEY_SCORE);
			appendValue(buf, nodeData, AssessmentHelper.KEY_PASSED);
			buf.append(EOL);	
		}
	}

	private void appendTypeValue(StringBuilder buf, Map<String,Object> nodeData, String key_type) {
		Object value = nodeData.get(key_type);
		if (value != null && (value instanceof String)) {
			String valueString = (String)value;
			if (valueString.equals("st")) {
				buf.append("");
			} else {
				buf.append( translator.translate("title_" + valueString) );
			}
		} else {
			buf.append("");
		}
		buf.append(DELIMITER);
	}

	private void appendIdentityIntro(StringBuilder buf, Identity identity) {
		for (UserPropertyHandler propertyHandler : userPropertyHandler) {
			String label = translator.translate(propertyHandler.i18nColumnDescriptorLabelKey());
			String value = propertyHandler.getUserProperty(identity.getUser(), translator.getLocale());
			appendLine(buf, label, (StringHelper.containsNonWhitespace(value) ? value : ""));
		}
	}
	
	private void appendIntro(StringBuilder buf, EfficiencyStatement efficiencyStatement) {
		buf.append(EOL);
		appendLine(buf, translator.translate("course"), efficiencyStatement.getCourseTitle() + "  (" + efficiencyStatement.getCourseRepoEntryKey().toString() +")");
		appendLine(buf, translator.translate("date"), StringHelper.formatLocaleDateTime(efficiencyStatement.getLastUpdated(), I18nModule.getDefaultLocale()) );
		
		List<Map<String,Object>> nodeDataList = efficiencyStatement.getAssessmentNodes();
		if(nodeDataList.size() > 0) {
			Map<String,Object> nodeData = nodeDataList.get(0);
			if (nodeData != null) {
				appendLabelValueLine(buf, nodeData, translator.translate("table.header.score"), AssessmentHelper.KEY_SCORE);
				appendLabelValueLine(buf, nodeData, translator.translate("table.header.passed"), AssessmentHelper.KEY_PASSED);
			}
		}
	}

	private void appendValue(StringBuilder buf, Map<String,Object> nodeData, String key) {
		Object value = nodeData.get(key);
		if (value != null) {
			buf.append(value);		
		} else {
			buf.append("");
		}
		buf.append(DELIMITER);
	}

	private void appendLabelValueLine(StringBuilder buf, Map<String,Object> nodeData, String label, String key) {
		buf.append(label);
		buf.append(DELIMITER);
		Object value = nodeData.get(key);
		if (value != null) {
			buf.append(value.toString());		
		} else {
			buf.append("n/a");
		}
		buf.append(EOL);
	}

	private void appendLine(StringBuilder buf, String label, String value) {
		buf.append(label);
		buf.append(DELIMITER);
		buf.append(value);
		buf.append(DELIMITER);
		buf.append(EOL);
	}
}