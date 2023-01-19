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

import java.util.Date;
import java.util.Optional;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.modules.video.ui.component.SelectTimeCommand;
import org.olat.modules.video.ui.question.NewQuestionEvent;
import org.olat.modules.video.ui.question.NewQuestionItemCalloutController;
import org.olat.modules.video.ui.question.VideoQuestionRowComparator;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-01-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class QuestionsHeaderController extends FormBasicController {
	private final String videoElementId;
	private SingleSelection questionsDropdown;
	private FormLink addQuestionButton;
	private NewQuestionItemCalloutController newQuestionCtrl;
	private CloseableCalloutWindowController ccwc;
	private SelectionValues questionsKV = new SelectionValues();
	private VideoQuestions questions;
	@Autowired
	private VideoManager videoManager;
	private final RepositoryEntry repositoryEntry;
	private String questionId;
	private String currentTimeCode;

	protected QuestionsHeaderController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
										String videoElementId) {
		super(ureq, wControl, "questions-header");
		this.repositoryEntry = repositoryEntry;
		this.videoElementId = videoElementId;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		questionsDropdown = uifactory.addDropdownSingleselect("questions", "form.question.title",
				formLayout, questionsKV.keys(), questionsKV.values());
		questionsDropdown.addActionListener(FormEvent.ONCHANGE);
		questionsDropdown.setEscapeHtml(false);

		addQuestionButton = uifactory.addFormLink("addQuestion", "form.question.add",
				"form.question.add", formLayout, Link.BUTTON);
	}

	private void loadModel() {
		setQuestions(videoManager.loadQuestions(repositoryEntry.getOlatResource()));
	}

	private void setQuestions(VideoQuestions questions) {
		this.questions = questions;

		questionsKV = new SelectionValues();
		questions
				.getQuestions()
				.stream()
				.sorted(new VideoQuestionRowComparator())
				.forEach((q) -> questionsKV.add(SelectionValues.entry(q.getId(), q.getTitle())));
		flc.contextPut("hasQuestions", !questionsKV.isEmpty());
		questionsDropdown.setKeysAndValues(questionsKV.keys(), questionsKV.values(), null);
		if (questions.getQuestions().stream().noneMatch(q -> q.getId().equals(questionId))) {
			questionId = null;
		}
		if (questionId == null && !questionsKV.isEmpty()) {
			questionId = questionsKV.keys()[0];
		}
		if (questionId != null) {
			questionsDropdown.select(questionId, true);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (newQuestionCtrl == source) {
			ccwc.deactivate();
			cleanUp();
			if (event instanceof NewQuestionEvent newQuestionEvent) {
				doNewQuestion(ureq, newQuestionEvent.getQuestion());
			}
		} else if (ccwc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(newQuestionCtrl);
		ccwc = null;
		newQuestionCtrl = null;
	}

	private void doNewQuestion(UserRequest ureq, VideoQuestion newQuestion) {
		newQuestion.setBegin(new Date(getCurrentTime()));
		questions.getQuestions().add(newQuestion);
		questionId = newQuestion.getId();
		videoManager.saveQuestions(questions, repositoryEntry.getOlatResource());
		loadModel();
		fireEvent(ureq, new EditQuestionEvent(null, questionId, repositoryEntry));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (addQuestionButton == source) {
			doAddQuestion(ureq);
		} else if (questionsDropdown == source) {
			if (questionsDropdown.isOneSelected()) {
				questionId = questionsDropdown.getSelectedKey();
				getOptionalQuestion()
						.ifPresent(q -> fireEvent(ureq, new QuestionSelectedEvent(q.getId(), q.getBegin().getTime())));
				setTimeToQuestion();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private Optional<VideoQuestion> getOptionalQuestion() {
		if (questionId == null) {
			return Optional.empty();
		}
		return questions.getQuestions().stream().filter(q -> questionId.equals(q.getId())).findFirst();
	}

	private void setTimeToQuestion() {
		getOptionalQuestion().ifPresent(q -> {
			long timeInSeconds = q.getBegin().getTime() / 1000;
			SelectTimeCommand selectTimeCommand = new SelectTimeCommand(videoElementId, timeInSeconds);
			getWindowControl().getWindowBackOffice().sendCommandTo(selectTimeCommand);
		});
	}

	private void doAddQuestion(UserRequest ureq) {
		newQuestionCtrl = new NewQuestionItemCalloutController(ureq, getWindowControl(), repositoryEntry);
		listenTo(newQuestionCtrl);

		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), newQuestionCtrl.getInitialComponent(),
				addQuestionButton.getFormDispatchId(), "", true, "",
				new CalloutSettings(false));
		listenTo(ccwc);
		ccwc.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
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
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
		if (questionId != null) {
			questionsDropdown.select(questionId, true);
		}
	}

	public String getQuestionId() {
		return questionId;
	}

	public void reload() {
		loadModel();
	}

	public void handleDeleted(String questionId) {
		questions.getQuestions().removeIf(q -> q.getId().equals(questionId));
		setQuestions(questions);
	}
}
