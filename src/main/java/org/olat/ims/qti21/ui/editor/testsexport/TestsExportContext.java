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
package org.olat.ims.qti21.ui.editor.testsexport;

import java.io.File;
import java.util.Date;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.QTI21Service;
import org.olat.repository.RepositoryEntry;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestPlanner;
import uk.ac.ed.ph.jqtiplus.running.TestProcessingInitializer;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.running.TestSessionControllerSettings;
import uk.ac.ed.ph.jqtiplus.state.TestPlan;
import uk.ac.ed.ph.jqtiplus.state.TestProcessingMap;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * 
 * Initial date: 9 nov. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TestsExportContext {
	
	private final File unzippedDirRoot;
	private final VFSContainer unzippedContRoot;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	
	private final RepositoryEntry testEntry;
	
	private int numOfTests;
	private String languageKey;
	private Locale locale;
	
	private boolean coverSheet = true;
	private boolean additionalSheet = true;
	
	private boolean serialNumber = true;
	private boolean placeholderNames = true;
	private boolean placeholderDate = true;
	private boolean placeholderCandidateNumber = true;
	
	private boolean maxScore = true;
	private boolean cutValue = true;
	private boolean numOfQuestions = true;
	private boolean description = true;
	private boolean expenditureOfTime = true;
	
	private String filePrefix;
	
	private String field1;
	private String field2;
	private String procedure;
	
	private String titleValue;
	private String identifierValue;
	private String descriptionValue;
	private String additionalSheetValue;
	
	private String timeValue;
	private String numOfQuestionsValue;
	private String maxScoreValue;
	private String cutValueValue;
	
	public TestsExportContext(RepositoryEntry testEntry, ResolvedAssessmentTest resolvedAssessmentTest,
			VFSContainer unzippedContRoot, File unzippedDirRoot) {
		this.resolvedAssessmentTest = resolvedAssessmentTest;
		this.unzippedDirRoot = unzippedDirRoot;
		this.unzippedContRoot = unzippedContRoot;
		this.testEntry = testEntry;
	}
	
	public VFSContainer getUnzippedContRoot() {
		return unzippedContRoot;
	}

	public ResolvedAssessmentTest getResolvedAssessmentTest() {
		return resolvedAssessmentTest;
	}

	public File getUnzippedDirRoot() {
		return unzippedDirRoot;
	}
	
	public RepositoryEntry getTestEntry() {
		return testEntry;
	}

	public int getNumOfTests() {
		return numOfTests;
	}

	public void setNumOfTests(int numOfTests) {
		this.numOfTests = numOfTests;
	}

	public String getLanguageKey() {
		return languageKey;
	}
	
	public Locale getLocale() {
		return locale;
	}

	public void setLanguage(String languageKey, Locale locale) {
		this.languageKey = languageKey;
		this.locale = locale;
	}

	public boolean isCoverSheet() {
		return coverSheet;
	}

	public void setCoverSheet(boolean coverSheet) {
		this.coverSheet = coverSheet;
	}

	public boolean isAdditionalSheet() {
		return additionalSheet;
	}

	public void setAdditionalSheet(boolean additionalSheet) {
		this.additionalSheet = additionalSheet;
	}

	public boolean isSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(boolean serialNumber) {
		this.serialNumber = serialNumber;
	}

	public boolean isPlaceholderNames() {
		return placeholderNames;
	}

	public void setPlaceholderNames(boolean placeholderNames) {
		this.placeholderNames = placeholderNames;
	}

	public boolean isPlaceholderDate() {
		return placeholderDate;
	}

	public void setPlaceholderDate(boolean placeholderDate) {
		this.placeholderDate = placeholderDate;
	}

	public boolean isPlaceholderCandidateNumber() {
		return placeholderCandidateNumber;
	}

	public void setPlaceholderCandidateNumber(boolean placeholderCandidateNumber) {
		this.placeholderCandidateNumber = placeholderCandidateNumber;
	}

	public boolean isMaxScore() {
		return maxScore;
	}

	public void setMaxScore(boolean maxScore) {
		this.maxScore = maxScore;
	}

	public boolean isCutValue() {
		return cutValue;
	}

	public void setCutValue(boolean cutValue) {
		this.cutValue = cutValue;
	}

	public boolean isNumOfQuestions() {
		return numOfQuestions;
	}

	public void setNumOfQuestions(boolean numOfQuestions) {
		this.numOfQuestions = numOfQuestions;
	}

	public boolean isDescription() {
		return description;
	}

	public void setDescription(boolean description) {
		this.description = description;
	}

	public boolean isExpenditureOfTime() {
		return expenditureOfTime;
	}

	public void setExpenditureOfTime(boolean expenditureOfTime) {
		this.expenditureOfTime = expenditureOfTime;
	}

	public String getFilePrefix() {
		return filePrefix;
	}

	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
	}

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}

	public String getField2() {
		return field2;
	}

	public void setField2(String field2) {
		this.field2 = field2;
	}

	public String getProcedure() {
		return procedure;
	}

	public void setProcedure(String procedure) {
		this.procedure = procedure;
	}

	public String getTitleValue() {
		return titleValue;
	}

	public void setTitleValue(String titleValue) {
		this.titleValue = titleValue;
	}

	public String getDescriptionValue() {
		return descriptionValue;
	}

	public void setDescriptionValue(String descriptionValue) {
		this.descriptionValue = descriptionValue;
	}

	public String getIdentifierValue() {
		return identifierValue;
	}

	public void setIdentifierValue(String identifierValue) {
		this.identifierValue = identifierValue;
	}

	public String getTimeValue() {
		return timeValue;
	}

	public void setTimeValue(String timeValue) {
		this.timeValue = timeValue;
	}

	public String getNumOfQuestionsValue() {
		return numOfQuestionsValue;
	}

	public void setNumOfQuestionsValue(String numOfQuestionsValue) {
		this.numOfQuestionsValue = numOfQuestionsValue;
	}

	public String getMaxScoreValue() {
		return maxScoreValue;
	}

	public void setMaxScoreValue(String maxScoreValue) {
		this.maxScoreValue = maxScoreValue;
	}

	public String getCutValueValue() {
		return cutValueValue;
	}

	public void setCutValueValue(String cutValueValue) {
		this.cutValueValue = cutValueValue;
	}

	public String getAdditionalSheetValue() {
		return additionalSheetValue;
	}

	public void setAdditionalSheetValue(String additionalSheetValue) {
		this.additionalSheetValue = additionalSheetValue;
	}
	
	public TestSessionController createTestSessionState() {
		QTI21Service qtiService = CoreSpringFactory.getImpl(QTI21Service.class);
		
		Date timestamp = new Date();
		
		TestProcessingInitializer initializer = new TestProcessingInitializer(resolvedAssessmentTest, true);
		TestProcessingMap testProcessingMap = initializer.initialize();
		/* Generate a test plan for this session */
		final TestPlanner testPlanner = new TestPlanner(testProcessingMap);
		final TestPlan testPlan = testPlanner.generateTestPlan();
		TestSessionState testSessionState = new TestSessionState(testPlan);

		final TestSessionControllerSettings testSessionControllerSettings = new TestSessionControllerSettings();
		final TestSessionController testSessionController = new TestSessionController(qtiService.jqtiExtensionManager(),
				testSessionControllerSettings, testProcessingMap, testSessionState);
        
		testSessionState = testSessionController.getTestSessionState();
		testSessionController.initialize(timestamp);
		final int testPartCount = testSessionController.enterTest(timestamp);
		if (testPartCount == 1) {
			testSessionController.enterNextAvailableTestPart(timestamp);
		}
		return testSessionController;
	}
	
	public String getSerialNumber(int i) {
		StringBuilder sb = new StringBuilder();
		if(filePrefix != null) {
			sb.append(filePrefix);
		}
		sb.append("_");
		if(i < 10) {
			sb.append("000");
		} else if(i < 100) {
			sb.append("00");
			
		} else if(i < 1000) {
			sb.append("0");
		}
		sb.append(i);
		return sb.toString();
	}
}
