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
package org.olat.ims.qti21.ui.assessment;

import java.io.File;
import java.net.URI;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemCorrection;
import org.olat.ims.qti21.ui.components.ItemBodyResultFormItem;
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: Nov 11, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CorrectionIdentityAssessmentItemPrintController extends FormBasicController implements Controller {

	private final RepositoryEntry testEntry;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	private final ResolvedAssessmentItem resolvedAssessmentItem;
	private final CorrectionOverviewModel model;
	private final AssessmentItemCorrection itemCorrection;
	private final ResourceLocator inputResourceLocator;
	private final URI assessmentObjectUri;
	private String userDisplayIdentifier;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;

	public CorrectionIdentityAssessmentItemPrintController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry testEntry, ResolvedAssessmentTest resolvedAssessmentTest,
			ResolvedAssessmentItem resolvedAssessmentItem, CorrectionOverviewModel model,
			AssessmentItemCorrection itemCorrection, String userDisplayIdentifier) {
		super(ureq, wControl, "correction_identity_print");
		setTranslator(Util.createPackageTranslator(AssessmentTestDisplayController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessmentTestComposerController.class, getLocale(), getTranslator()));
		this.testEntry = testEntry;
		this.resolvedAssessmentTest = resolvedAssessmentTest;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		this.model = model;
		this.itemCorrection = itemCorrection;
		this.userDisplayIdentifier = userDisplayIdentifier;
		
		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
		inputResourceLocator = ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
		assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
		
		initForm(ureq);
	}

	@Override
	public void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initCourse(ureq, formLayout);
		initResponse(formLayout);
	}

	private void initCourse(UserRequest ureq, FormItemContainer formLayout) {
		if (model.getCourseNode() == null || model.getCourseEnvironment() == null) {
			return;
		}
		
		String userLableI18n = "print.participant.identifier";
		if (!StringHelper.containsNonWhitespace(userDisplayIdentifier)) {
			userLableI18n = "print.participant";
			userDisplayIdentifier = userManager.getUserDisplayName(itemCorrection.getAssessedIdentity().getKey());
		}
		
		CorrectionIdentityRepositoryEntryPrintController repositoryEntryCtrl = new CorrectionIdentityRepositoryEntryPrintController(
				ureq, getWindowControl(),
				model.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
				model.getCourseNode(),
				testEntry,
				itemCorrection.getTestSession(),
				userLableI18n,
				userDisplayIdentifier);
		listenTo(repositoryEntryCtrl);
		formLayout.add("repo", repositoryEntryCtrl.getInitialFormItem());
	}

	private void initResponse(FormItemContainer formLayout) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
			layoutCont.contextPut("questionTitle", assessmentItem.getTitle());
			
			Double minScore = QtiNodesExtractor.extractMinScore(assessmentItem);
			Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
			String score = translate("print.score.range", AssessmentHelper.getRoundedScore(minScore), AssessmentHelper.getRoundedScore(maxScore));
			layoutCont.contextPut("score", score);
		}
		
		ItemBodyResultFormItem responseFormItem = new ItemBodyResultFormItem("response", resolvedAssessmentItem);
		responseFormItem.setItemSessionState(itemCorrection.getItemSessionState());
		responseFormItem.setCandidateSessionContext(new TerminatedStaticCandidateSessionContext(itemCorrection.getTestSession()));
		responseFormItem.setResolvedAssessmentTest(resolvedAssessmentTest);
		responseFormItem.setResourceLocator(inputResourceLocator);
		responseFormItem.setAssessmentObjectUri(assessmentObjectUri);
		responseFormItem.setPdfExport(true);
		formLayout.add(responseFormItem);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
