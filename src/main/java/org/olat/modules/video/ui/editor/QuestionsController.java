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
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.core.commons.services.color.ColorService;
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
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.pool.QTI21QPoolServiceProvider;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.modules.video.model.VideoQuestionImpl;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;

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
	private String currentTimeCode;
	@Autowired
	private QTI21QPoolServiceProvider qti21QPoolServiceProvider;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private ColorService colorService;

	protected QuestionsController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
								  long videoDurationInSeconds, String videoElementId) {
		super(ureq, wControl);
		this.repositoryEntry = repositoryEntry;
		mainVC = createVelocityContainer("questions");

		questions = videoManager.loadQuestions(repositoryEntry.getOlatResource());
		question = questions.getQuestions().stream().findFirst().orElse(null);

		questionsHeaderController = new QuestionsHeaderController(ureq, wControl, repositoryEntry);
		questionsHeaderController.setQuestions(questions);
		listenTo(questionsHeaderController);
		mainVC.put("header", questionsHeaderController.getInitialComponent());

		questionController = new QuestionController(ureq, wControl, repositoryEntry, question, videoDurationInSeconds,
				videoElementId);
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
			} else if (event instanceof QuestionsHeaderController.QuestionsImportedEvent questionsImportedEvent) {
				importQuestions(ureq, questionsImportedEvent.getItemList());
			}
		}

		super.event(ureq, source, event);
	}

	private void importQuestions(UserRequest ureq, List<QuestionItemView> items) {
		File assessmentDir = videoManager.getAssessmentDirectory(repositoryEntry.getOlatResource());
		long currentTime = getCurrentTime();

		VideoQuestion firstQuestion = null;
		for (QuestionItemView item : items) {
			VideoQuestion importedQuestion = doCopyQItem(item, assessmentDir, currentTime);
			if (firstQuestion == null && importedQuestion != null) {
				firstQuestion = importedQuestion;
			}
			currentTime += 10L;
		}

		questionsHeaderController.setQuestions(questions);
		reloadQuestions(ureq);
		if (firstQuestion != null) {
			String questionId = firstQuestion.getId();
			showQuestion(questionId);
			questionController.setQuestion(question);
			fireEvent(ureq, new EditQuestionEvent(questionId, repositoryEntry));
		}
	}

	private VideoQuestion doCopyQItem(QuestionItemView item, File assessmentDir, long begin) {
		try {
			QuestionItemFull qItem = qti21QPoolServiceProvider.getFullQuestionItem(item);

			String itemDir = buildItemDirectory(qItem);
			VideoQuestionImpl question = new VideoQuestionImpl();
			question.setId(itemDir);
			question.setBegin(new Date());
			question.setQuestionRootPath(itemDir);
			question.setQuestionFilename(qItem.getRootFilename());
			question.setBegin(new Date(begin));
			question.setTimeLimit(-1);
			question.setStyle(VideoModule.getMarkerStyleFromColor(colorService.getColors().get(0)));

			File itemDirectory = new File(assessmentDir, itemDir);
			itemDirectory.mkdir();
			AssessmentItem assessmentItem = qti21QPoolServiceProvider.exportToQTIEditor(qItem, getLocale(), itemDirectory);
			File itemFile = new File(itemDirectory, qItem.getRootFilename());
			qtiService.persistAssessmentObject(itemFile, assessmentItem);

			question.setTitle(assessmentItem.getTitle());
			question.setAssessmentItemIdentifier(assessmentItem.getIdentifier());
			question.setType(QTI21QuestionType.getTypeRelax(assessmentItem).name());
			Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
			question.setMaxScore(maxScore);
			questions.getQuestions().add(question);
			videoManager.saveQuestions(questions, repositoryEntry.getOlatResource());
			return question;
		} catch (IOException e) {
			logError("", e);
			return null;
		}
	}

	private String buildItemDirectory(QuestionItemFull qItem) {
		StringBuilder sb = new StringBuilder(48);
		if (qItem.getType() != null && StringHelper.containsNonWhitespace(qItem.getType().getType())) {
			sb.append(qItem.getType().getType());
		}  else {
			sb.append(QTI21QuestionType.unkown.name());
		}

		if (StringHelper.containsNonWhitespace(qItem.getIdentifier())) {
			sb.append(qItem.getIdentifier().replace("-", ""));
		} else {
			sb.append(UUID.randomUUID().toString().replace("-", ""));
		}
		return sb.toString();
	}

	private void reloadQuestions(UserRequest ureq) {
		fireEvent(ureq, RELOAD_QUESTIONS_EVENT);
	}

	private long getCurrentTime() {
		long time = 0;
		if (currentTimeCode != null) {
			time = Math.round(Double.parseDouble(currentTimeCode)) * 1000L;
		}
		return time;
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		this.currentTimeCode = currentTimeCode;
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
