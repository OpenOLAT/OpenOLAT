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
package org.olat.modules.ceditor.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.editor.AssessmentItemEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.olat.modules.ceditor.manager.ContentEditorQti;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.jpa.QuizPart;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * Initial date: 2024-03-26<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditQuestionController extends BasicController {

	private final QuizPart quiz;
	private final QuizQuestion quizQuestion;
	private AssessmentItemEditorController itemEditorCtrl;

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private ContentEditorQti contentEditorQti;

	public EditQuestionController(UserRequest ureq, WindowControl wControl, QuizPart quiz, QuizQuestion quizQuestion) {
		super(ureq, wControl);
		this.quiz = quiz;
		this.quizQuestion = quizQuestion;

		createItemEditor(ureq);

		VelocityContainer mainVC = createVelocityContainer("quiz_edit_question");
		mainVC.put("editor", itemEditorCtrl.getInitialComponent());
		mainVC.contextPut("iconClass", itemEditorCtrl.getType().getCssClass());
		mainVC.contextPut("title", itemEditorCtrl.getTitle());
		putInitialPanel(mainVC);
	}

	private void createItemEditor(UserRequest ureq) {
		ContentEditorQti.QuizQuestionStorageInfo storageInfo = contentEditorQti.getStorageInfo(quiz, quizQuestion);
		ResolvedAssessmentItem resolvedAssessmentItem = qtiService.loadAndResolveAssessmentItem(
				storageInfo.questionFile().toURI(), storageInfo.questionDirectory());
		itemEditorCtrl = new AssessmentItemEditorController(ureq, getWindowControl(),
				resolvedAssessmentItem, storageInfo.questionDirectory(), storageInfo.questionContainer(),
				storageInfo.questionFile(), false, false);
		listenTo(itemEditorCtrl);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (itemEditorCtrl == source) {
			if (event instanceof AssessmentItemEvent) {
				doSaveQuestion(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	private void doSaveQuestion(UserRequest ureq) {
		AssessmentItem assessmentItem = itemEditorCtrl.getAssessmentItem();
		quizQuestion.setTitle(assessmentItem.getTitle());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	public QuizQuestion getQuizQuestion() {
		return quizQuestion;
	}
}
