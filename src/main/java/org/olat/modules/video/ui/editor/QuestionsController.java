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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.AbstractFlexiTableRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-01-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class QuestionsController extends BasicController {
	public static final Event RELOAD_QUESTIONS_EVENT = new Event("video.edit.reload.questions");
	private final VelocityContainer mainVC;
	private final RepositoryEntry repositoryEntry;
	private final QuestionsHeaderController questionsHeaderController;
	private final QuestionController questionController;
	private VideoQuestions questions;
	private VideoQuestion question;
	@Autowired
	private VideoManager videoManager;

	protected QuestionsController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl);
		this.repositoryEntry = repositoryEntry;
		mainVC = createVelocityContainer("questions");

		questions = videoManager.loadQuestions(repositoryEntry.getOlatResource());
		question = questions.getQuestions().stream().findFirst().orElse(null);

		questionsHeaderController = new QuestionsHeaderController(ureq, wControl, repositoryEntry);
		questionsHeaderController.setQuestions(questions);
		listenTo(questionsHeaderController);
		mainVC.put("header", questionsHeaderController.getInitialComponent());

		questionController = new QuestionController(ureq, wControl, repositoryEntry, question);
		listenTo(questionController);
		if (question != null) {
			mainVC.put("question", questionController.getInitialComponent());
		} else {
			mainVC.remove("question");
		}

		Translator tableTranslator = Util.createPackageTranslator(AbstractFlexiTableRenderer.class, ureq.getLocale());
		EmptyStateConfig emptyStateConfig = EmptyStateConfig
				.builder()
				.withIconCss("o_icon_empty_objects")
				.withIndicatorIconCss("o_icon_empty_indicator")
				.withMessageTranslated(tableTranslator.translate("default.tableEmptyMessage"))
				.build();
		EmptyStateFactory.create("emptyState", mainVC, this, emptyStateConfig);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (questionController == source) {
			if (event == Event.DONE_EVENT) {
				question = questionController.getQuestion();
				videoManager.saveQuestions(questions, repositoryEntry.getOlatResource());
				questionsHeaderController.setQuestions(questions);
				reloadQuestions(ureq);
				fireEvent(ureq, new QuestionSelectedEvent(question.getId(), question.getBegin().getTime()));
			} else if (event instanceof EditQuestionEvent) {
				fireEvent(ureq, event);
			}
		} else if (questionsHeaderController == source) {
			if (event instanceof QuestionSelectedEvent questionSelectedEvent) {
				questions.getQuestions().stream().filter(q -> q.getId().equals(questionSelectedEvent.getId()))
						.findFirst().ifPresent(q -> {
							questionController.setQuestion(q);
							fireEvent(ureq, questionSelectedEvent);
						});
			} else if (event == QuestionsHeaderController.QUESTION_ADDED_EVENT ||
					event == QuestionsHeaderController.QUESTION_DELETED_EVENT) {
				this.questions = questionsHeaderController.getQuestions();
				String newQuestionId = questionsHeaderController.getQuestionId();
				showQuestion(newQuestionId);
				questionController.setQuestion(question);
				videoManager.saveQuestions(questions, repositoryEntry.getOlatResource());
				reloadQuestions(ureq);
				if (question != null) {
					if (event == QuestionsHeaderController.QUESTION_DELETED_EVENT) {
						fireEvent(ureq, new QuestionSelectedEvent(question.getId(), question.getBegin().getTime()));
					}
					if (event == QuestionsHeaderController.QUESTION_ADDED_EVENT) {
						fireEvent(ureq, new EditQuestionEvent(question.getId(), repositoryEntry));
					}
				}
			}
		}

		super.event(ureq, source, event);
	}

	private void reloadQuestions(UserRequest ureq) {
		fireEvent(ureq, RELOAD_QUESTIONS_EVENT);
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		questionsHeaderController.setCurrentTimeCode(currentTimeCode);
	}

	public void showQuestion(String questionId) {
		this.question = questions.getQuestions().stream().filter(q -> q.getId().equals(questionId)).findFirst()
				.orElse(null);
		if (question != null) {
			questionsHeaderController.setQuestionId(question.getId());
			questionController.setQuestion(question);
			mainVC.put("question", questionController.getInitialComponent());
		} else {
			questionsHeaderController.setQuestionId(null);
			mainVC.remove("question");
		}
	}

	/**
	 * This call tells the controller that questions have been updated outside its controller hierarchy.
	 */
	public void updateQuestion(String questionId) {
		questions = videoManager.loadQuestions(repositoryEntry.getOlatResource());
		questionsHeaderController.setQuestions(questions);
		questions.getQuestions().stream().filter(q -> q.getId().equals(questionId)).findFirst()
				.ifPresent(q -> {
					questionController.setQuestion(q);
					mainVC.put("question", questionController.getInitialComponent());
				});
	}

	public void handleDeleted(String questionId) {
		questionsHeaderController.handleDeleted(questionId);
		String currentQuestionId = questionsHeaderController.getQuestionId();
		showQuestion(currentQuestionId);
	}

	public void sendSelectionEvent(UserRequest ureq) {
		if (question != null) {
			fireEvent(ureq, new QuestionSelectedEvent(question.getId(), question.getBegin().getTime()));
		}
	}
}