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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

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
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
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
	public final static Event QUESTION_DELETED_EVENT = new Event("question.deleted");
	public final static Event QUESTION_ADDED_EVENT = new Event("question.added");
	private FormLink previousQuestionButton;
	private SingleSelection questionsDropdown;
	private FormLink nextQuestionButton;
	private FormLink addQuestionButton;
	private NewQuestionItemCalloutController newQuestionCtrl;
	private CloseableCalloutWindowController ccwc;
	private SelectionValues questionsKV = new SelectionValues();
	private VideoQuestions questions;
	@Autowired
	private VideoModule videoModule;
	private final RepositoryEntry repositoryEntry;
	private String questionId;
	private String currentTimeCode;
	private FormLink commandsButton;
	private HeaderCommandsController commandsController;
	private final SimpleDateFormat timeFormat;

	protected QuestionsHeaderController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl, "questions_header");
		this.repositoryEntry = repositoryEntry;

		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		initForm(ureq);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(newQuestionCtrl);
		removeAsListenerAndDispose(commandsController);
		ccwc = null;
		newQuestionCtrl = null;
		commandsController = null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousQuestionButton = uifactory.addFormLink("previousQuestion", "", "",
				formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		previousQuestionButton.setIconRightCSS("o_icon o_icon_back");

		questionsDropdown = uifactory.addDropdownSingleselect("questions", "form.question.title",
				formLayout, questionsKV.keys(), questionsKV.values());
		questionsDropdown.addActionListener(FormEvent.ONCHANGE);
		questionsDropdown.setEscapeHtml(false);

		nextQuestionButton = uifactory.addFormLink("nextQuestion", "", "",
				formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		nextQuestionButton.setIconRightCSS("o_icon o_icon_start");

		addQuestionButton = uifactory.addFormLink("addQuestion", "form.question.add",
				"form.question.add", formLayout, Link.BUTTON);

		commandsButton = uifactory.addFormLink("commands", "", "", formLayout,
				Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		commandsButton.setIconRightCSS("o_icon o_icon_commands");
	}

	public void setQuestions(VideoQuestions questions) {
		this.questions = questions;
		setValues();
	}

	public VideoQuestions getQuestions() {
		return questions;
	}

	private void setValues() {
		questionsKV = new SelectionValues();
		questions
				.getQuestions()
				.stream()
				.sorted(new VideoQuestionRowComparator())
				.forEach((q) -> questionsKV.add(SelectionValues.entry(q.getId(), timeFormat.format(q.getBegin()) + " - " + q.getTitle())));
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

		int selectedIndex = -1;
		for (int i = 0; i < questionsKV.size(); i++) {
			if (questionsKV.keys()[i].equals(questionId)) {
				selectedIndex = i;
				break;
			}
		}

		if (selectedIndex != -1) {
			previousQuestionButton.setEnabled(selectedIndex > 0);
			nextQuestionButton.setEnabled(selectedIndex < (questionsKV.size() - 1));
		}

		commandsButton.setEnabled(!questionsKV.isEmpty());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (addQuestionButton == source) {
			doAddQuestion(ureq);
		} else if (commandsButton == source) {
			doCommands(ureq);
		} else if (questionsDropdown == source) {
			if (questionsDropdown.isOneSelected()) {
				questionId = questionsDropdown.getSelectedKey();
				handleQuestionSelected(ureq);
			}
		} else if (nextQuestionButton == source) {
			doNextQuestion(ureq);
		} else if (previousQuestionButton == source) {
			doPreviousQuestion(ureq);
		}

		super.formInnerEvent(ureq, source, event);
	}

	private void handleQuestionSelected(UserRequest ureq) {
		getOptionalQuestion()
				.ifPresent(q -> fireEvent(ureq, new QuestionSelectedEvent(q.getId(), q.getBegin().getTime())));
	}

	private void doPreviousQuestion(UserRequest ureq) {
		String[] keys = questionsDropdown.getKeys();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (questionId != null && questionId.equals(key)) {
				int newIndex = i - 1;
				if (newIndex >= 0) {
					questionId = keys[newIndex];
					setValues();
					handleQuestionSelected(ureq);
				}
				break;
			}
		}
	}

	private void doNextQuestion(UserRequest ureq) {
		String[] keys = questionsDropdown.getKeys();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (questionId != null && questionId.equals(key)) {
				int newIndex = i + 1;
				if (newIndex < keys.length) {
					questionId = keys[newIndex];
					setValues();
					handleQuestionSelected(ureq);
				}
				break;
			}
		}
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

	private void doCommands(UserRequest ureq) {
		commandsController = new HeaderCommandsController(ureq, getWindowControl());
		listenTo(commandsController);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), commandsController.getInitialComponent(),
				commandsButton.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
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
		} else if (commandsController == source) {
			if (HeaderCommandsController.DELETE_EVENT.getCommand().equals(event.getCommand())) {
				doDeleteQuestion(ureq);
			}
			ccwc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void doNewQuestion(UserRequest ureq, VideoQuestion newQuestion) {
		newQuestion.setBegin(new Date(getCurrentTime()));
		newQuestion.setStyle(videoModule.getMarkerStyles().get(0));
		questions.getQuestions().add(newQuestion);
		questionId = newQuestion.getId();
		setValues();
		fireEvent(ureq, QUESTION_ADDED_EVENT);
	}

	private void doDeleteQuestion(UserRequest ureq) {
		if (questionId == null) {
			return;
		}

		questions.getQuestions().removeIf(q -> q.getId().equals(questionId));
		if (questions.getQuestions().isEmpty()) {
			questionId = null;
		} else {
			questionId = questions.getQuestions().get(0).getId();
		}
		setValues();
		fireEvent(ureq, QUESTION_DELETED_EVENT);
	}

	private Optional<VideoQuestion> getOptionalQuestion() {
		if (questionId == null) {
			return Optional.empty();
		}
		return questions.getQuestions().stream().filter(q -> questionId.equals(q.getId())).findFirst();
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
			setValues();
		}
	}

	public String getQuestionId() {
		return questionId;
	}

	public void handleDeleted(String questionId) {
		questions.getQuestions().removeIf(q -> q.getId().equals(questionId));
		setQuestions(questions);
	}
}
