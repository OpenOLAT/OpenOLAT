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
package org.olat.ims.qti21.ui;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.olat.admin.user.UserShortDescription;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.components.AssessmentTestResultFormItem;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemVariable;
import uk.ac.ed.ph.jqtiplus.node.result.OutcomeVariable;
import uk.ac.ed.ph.jqtiplus.node.result.TestResult;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.value.BooleanValue;
import uk.ac.ed.ph.jqtiplus.value.NumberValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 21.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentResultController extends FormBasicController {
	
	private AssessmentTestResultFormItem qtiResultEl;

	private final String mapperUri;
	
	private final File fUnzippedDirRoot;
	
	private TestSessionState testSessionState;
	private AssessmentResult assessmentResult;
	private final UserShortDescription assessedIdentityInfosCtrl;
	
	@Autowired
	private QTI21Service qtiService;
	
	public AssessmentResultController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity,
			AssessmentTestSession candidateSession, File fUnzippedDirRoot, String mapperUri) {
		super(ureq, wControl, "assessment_results");
		this.mapperUri = mapperUri;
		this.fUnzippedDirRoot = fUnzippedDirRoot;
		
		assessedIdentityInfosCtrl = new UserShortDescription(ureq, getWindowControl(), assessedIdentity);
		listenTo(assessedIdentityInfosCtrl);
		
		testSessionState = qtiService.loadTestSessionState(candidateSession);
		assessmentResult = qtiService.getAssessmentResult(candidateSession);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
		final ResourceLocator inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);

		qtiResultEl = new AssessmentTestResultFormItem("qtiresults");
		qtiResultEl.setVisible(false);
		formLayout.add("qtiresults", qtiResultEl);
		
		qtiResultEl.setResourceLocator(inputResourceLocator);
		qtiResultEl.setAssessmentObjectUri(qtiService.createAssessmentObjectUri(fUnzippedDirRoot));
		qtiResultEl.setMapperUri(mapperUri);
		formLayout.add("qtiResults", qtiResultEl);
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.put("assessedIdentityInfos", assessedIdentityInfosCtrl.getInitialComponent());
			
			Results results = new Results(testSessionState);
			layoutCont.contextPut("testResults", results);
			TestResult testResult = assessmentResult.getTestResult();
			if(testResult != null) {
				extractOutcomeVariable(testResult.getItemVariables(), results);
			}
		}
	}
	
	private void extractOutcomeVariable(List<ItemVariable> itemVariables, Results results) {
		for(ItemVariable itemVariable:itemVariables) {
			if(itemVariable instanceof OutcomeVariable) {
				if(QTI21Constants.SCORE_IDENTIFIER.equals(itemVariable.getIdentifier())) {
					results.setScore(getOutcomeNumberVariable(itemVariable));
				} else if(QTI21Constants.MAXSCORE_IDENTIFIER.equals(itemVariable.getIdentifier())) {
					results.setMaxScore(getOutcomeNumberVariable(itemVariable));
				} else if(QTI21Constants.PASS_IDENTIFIER.equals(itemVariable.getIdentifier())) {
					results.setPass(getOutcomeBooleanVariable(itemVariable));
				}
			}
		}
	}
	
	private String getOutcomeNumberVariable(ItemVariable outcomeVariable) {
		Value value = outcomeVariable.getComputedValue();
		if(value instanceof NumberValue) {
			return AssessmentHelper.getRoundedScore(((NumberValue)value).doubleValue());
		}
		return null;
	}
	
	private Boolean getOutcomeBooleanVariable(ItemVariable outcomeVariable) {
		Value value = outcomeVariable.getComputedValue();
		if(value instanceof BooleanValue) {
			return ((BooleanValue)value).booleanValue();
		}
		return null;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public class Results {
		
		private final Date entryTime;
		private final Date endTime;
		private final Long duration;
		
		private String score;
		private String maxScore;
		
		private Boolean pass;
		
		public Results(TestSessionState testSessionState) {
			entryTime = testSessionState.getEntryTime();
			endTime = testSessionState.getEndTime();
			duration = testSessionState.getDurationAccumulated();
		}

		public Date getEntryTime() {
			return entryTime;
		}

		public Date getEndTime() {
			return endTime;
		}

		public Long getDuration() {
			return duration;
		}
		
		public boolean hasScore() {
			return StringHelper.containsNonWhitespace(score);
		}
		
		public String getScore() {
			return score;
		}

		public void setScore(String score) {
			this.score = score;
		}
		
		public boolean hasMaxScore() {
			return StringHelper.containsNonWhitespace(maxScore);
		}
		
		public String getMaxScore() {
			return maxScore;
		}

		public void setMaxScore(String maxScore) {
			this.maxScore = maxScore;
		}

		public boolean hasPass() {
			return pass != null;
		}
		
		public Boolean getPass() {
			return pass;
		}

		public void setPass(Boolean pass) {
			this.pass = pass;
		}
	}
}