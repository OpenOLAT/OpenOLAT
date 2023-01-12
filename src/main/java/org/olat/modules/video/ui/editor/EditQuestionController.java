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
package org.olat.modules.video.ui.editor;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.editor.AssessmentItemEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * Initial date: 2023-01-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditQuestionController extends BasicController {


	private final String questionId;
	private final VideoQuestion question;
	private final RepositoryEntry repositoryEntry;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private QTI21Service qtiService;
	private AssessmentItemEditorController itemEditorCtrl;


	protected EditQuestionController(UserRequest ureq, WindowControl wControl, VideoQuestion question,
									 String questionId, RepositoryEntry repositoryEntry) {
		super(ureq, wControl);
		this.questionId = questionId;
		this.question = videoManager.loadQuestions(repositoryEntry.getOlatResource()).getQuestions().stream()
				.filter(q -> q.getId().equals(questionId)).findFirst().get();
		this.repositoryEntry = repositoryEntry;

		createItemEditor(ureq);

		VelocityContainer mainVC = createVelocityContainer("edit_question");
		mainVC.put("editor", itemEditorCtrl.getInitialComponent());
		mainVC.contextPut("iconClass", itemEditorCtrl.getType().getCssClass());
		mainVC.contextPut("title", itemEditorCtrl.getTitle());
		putInitialPanel(mainVC);
	}

	private void createItemEditor(UserRequest ureq) {
		File rootDirectory = videoManager.getQuestionDirectory(repositoryEntry.getOlatResource(), question);
		VFSContainer rootContainer = videoManager.getQuestionContainer(repositoryEntry.getOlatResource(), question);
		File itemFile = new File(rootDirectory, question.getQuestionFilename());
		ResolvedAssessmentItem resolvedAssessmentItem = qtiService.loadAndResolveAssessmentItem(itemFile.toURI(), rootDirectory);
		itemEditorCtrl = new AssessmentItemEditorController(ureq, getWindowControl(),
				resolvedAssessmentItem, rootDirectory, rootContainer, itemFile, false, false);
		listenTo(itemEditorCtrl);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (itemEditorCtrl == source) {
			if (event instanceof AssessmentItemEvent) {
				doSaveQuestion(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	private void doSaveQuestion(UserRequest ureq) {
		AssessmentItem assessmentItem = itemEditorCtrl.getAssessmentItem();
		question.setTitle(assessmentItem.getTitle());
		question.setAssessmentItemIdentifier(assessmentItem.getIdentifier());
		Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
		question.setMaxScore(maxScore);
		VideoQuestions questions = videoManager.loadQuestions(repositoryEntry.getOlatResource());
		questions.getQuestions().remove(question);
		questions.getQuestions().add(question);
		videoManager.saveQuestions(questions, repositoryEntry.getOlatResource());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	public String getQuestionId() {
		return questionId;
	}
}
