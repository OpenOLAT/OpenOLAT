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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.modules.video.ui.VideoSettingsController;
import org.olat.modules.video.ui.component.SelectTimeCommand;
import org.olat.modules.video.ui.question.NewQuestionEvent;
import org.olat.modules.video.ui.question.NewQuestionItemCalloutController;
import org.olat.modules.video.ui.question.VideoQuestionRowComparator;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2022-11-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class QuizController extends FormBasicController {
	public static final Event RELOAD_QUESTIONS_EVENT = new Event("video.edit.reload.questions");
	public static final String EDIT_ACTION = "edit";
	private final RepositoryEntry repositoryEntry;
	private final String videoElementId;
	private SingleSelection questionsDropdown;
	private SelectionValues questionsKV = new SelectionValues();
	private FormLink addQuestionButton;
	private NewQuestionItemCalloutController newQuestionCtrl;
	private CloseableCalloutWindowController ccwc;
	private TextElement startEl;
	private TextElement timeLimitEl;
	private SingleSelection colorDropdown;
	private SelectionValues colorsKV = new SelectionValues();
	private MultipleSelectionElement options;
	private final SelectionValues optionsKV;
	private FormSubmit saveButton;
	private FormCancel cancelButton;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private VideoModule videoModule;
	private VideoQuestions videoQuestions;
	private String questionId;
	private final SimpleDateFormat timeFormat;
	private String currentTimeCode;
	private QuestionTableModel tableModel;
	private FlexiTableElement questionTable;

	public QuizController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
						  String videoElementId) {
		super(ureq, wControl, "quiz");
		this.repositoryEntry = repositoryEntry;
		this.videoElementId = videoElementId;
		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		colorsKV = new SelectionValues();
		Translator videoTranslator = Util.createPackageTranslator(VideoSettingsController.class, ureq.getLocale());
		for (String color : videoModule.getMarkerStyles()) {
			colorsKV.add(SelectionValues.entry(color, videoTranslator.translate("video.marker.style.".concat(color))));
		}

		optionsKV = new SelectionValues();
		optionsKV.add(SelectionValues.entry("allowSkipping", translate("form.question.allowSkipping")));
		optionsKV.add(SelectionValues.entry("allowNewAttempt", translate("form.question.allowNewAttempt")));

		initForm(ureq);
		loadModel();
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		System.err.println("quiz: current time code: " + currentTimeCode);
		this.currentTimeCode = currentTimeCode;
	}

	private long getCurrentTime() {
		long time = 0;
		if (currentTimeCode != null) {
			time = Math.round(Double.parseDouble(currentTimeCode)) * 1000L;
		}
		return time;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		questionsDropdown = uifactory.addDropdownSingleselect("questions", "form.question.title",
				formLayout, questionsKV.keys(), questionsKV.values());
		questionsDropdown.addActionListener(FormEvent.ONCHANGE);
		questionsDropdown.setEscapeHtml(false);

		addQuestionButton = uifactory.addFormLink("addQuestion", "form.question.add",
				"form.question.add", formLayout, Link.BUTTON);

		startEl = uifactory.addTextElement("start", "form.question.start", 8,
				"00:00:00", formLayout);
		startEl.setMandatory(true);

		timeLimitEl = uifactory.addTextElement("timeLimit", "form.question.timeLimit", 8,
				"", formLayout);
		timeLimitEl.setExampleKey("form.question.timeLimit.example", null);

		colorDropdown = uifactory.addDropdownSingleselect("color", "form.question.color", formLayout,
				colorsKV.keys(), colorsKV.values());

		options = uifactory.addCheckboxesVertical("options", "form.question.options", formLayout,
				optionsKV.keys(), optionsKV.values(), 1);

		initTable(formLayout);

		saveButton = uifactory.addFormSubmitButton("save", formLayout);
		cancelButton = uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	private void initTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionTableModel.QuestionColDef.question));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionTableModel.QuestionColDef.type));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionTableModel.QuestionColDef.score));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(
				QuestionTableModel.QuestionColDef.edit.i18nHeaderKey(),
				translate(QuestionTableModel.QuestionColDef.edit.i18nHeaderKey()), EDIT_ACTION));

		tableModel = new QuestionTableModel(columnModel);
		questionTable = uifactory.addTableElement(getWindowControl(), "questionTable", tableModel,
				getTranslator(), formLayout);
		questionTable.setCustomizeColumns(false);
		questionTable.setNumOfRowsEnabled(false);

		flc.contextPut("questionTableHasData", false);
	}

	private void loadModel() {
		questionsKV = new SelectionValues();
		videoQuestions = videoManager.loadQuestions(repositoryEntry.getOlatResource());
		videoQuestions
				.getQuestions()
				.stream()
				.sorted(new VideoQuestionRowComparator())
				.forEach((q) -> questionsKV.add(SelectionValues.entry(q.getId(), q.getTitle())));
		questionsDropdown.setKeysAndValues(questionsKV.keys(), questionsKV.values(), null);
		if (questionId == null && !questionsKV.isEmpty()) {
			questionId = questionsKV.keys()[0];
		}
		setValues();
	}

	private void setValues() {
		if (questionId != null) {
			videoQuestions.getQuestions().stream().filter((q) -> questionId.equals(q.getId())).findFirst()
					.ifPresent((q)->setValues(q));
		}
	}

	private void setValues(VideoQuestion videoQuestion) {
		startEl.setValue(timeFormat.format(videoQuestion.getBegin()));
		timeLimitEl.setValue("" + videoQuestion.getTimeLimit());
		if (videoQuestion.getStyle() != null) {
			colorDropdown.select(videoQuestion.getStyle(), true);
			colorDropdown.getComponent().setDirty(true);
		}
		if (!colorDropdown.isOneSelected() && !colorsKV.isEmpty()) {
			colorDropdown.select(colorsKV.keys()[0], true);
			colorDropdown.getComponent().setDirty(true);
		}
		options.select(optionsKV.keys()[0], videoQuestion.isAllowSkipping());
		options.select(optionsKV.keys()[1], videoQuestion.isAllowNewAttempt());
		tableModel.setObjects(List.of(videoQuestion));
		flc.contextPut("questionTableHasData", true);
		questionTable.reloadData();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (newQuestionCtrl == source) {
			ccwc.deactivate();
			cleanUp();
			if (event instanceof NewQuestionEvent) {
				doNewQuestion(ureq, ((NewQuestionEvent) event).getQuestion());
			}
		}
		super.event(ureq, source, event);
	}

	private void doNewQuestion(UserRequest ureq, VideoQuestion newQuestion) {
		newQuestion.setBegin(new Date(getCurrentTime()));
		videoQuestions.getQuestions().add(newQuestion);
		videoManager.saveQuestions(videoQuestions, repositoryEntry.getOlatResource());
		loadModel();
		fireEvent(ureq, new EditQuestionEvent(null, questionId, repositoryEntry));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (addQuestionButton == source) {
			doAddQuestion(ureq);
		} else if (newQuestionCtrl == source) {
			ccwc.deactivate();
			cleanUp();
		} else if (questionsDropdown == source) {
			if (questionsDropdown.isOneSelected()) {
				questionId = questionsDropdown.getSelectedKey();
				setValues();
				setTimeToQuestion();
			}
		} else if (event instanceof SelectionEvent) {
			SelectionEvent selectionEvent = (SelectionEvent) event;
			VideoQuestion question = tableModel.getObject(selectionEvent.getIndex());
			if (EDIT_ACTION.equals(selectionEvent.getCommand())) {
				doEdit(ureq, question);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doEdit(UserRequest ureq, VideoQuestion question) {
		fireEvent(ureq, new EditQuestionEvent(null, question.getId(), repositoryEntry));
	}

	private void setTimeToQuestion() {
		try {
			Date start = timeFormat.parse(startEl.getValue());
			long timeInSeconds = start.getTime() / 1000;
			SelectTimeCommand selectTimeCommand = new SelectTimeCommand(videoElementId, timeInSeconds);
			getWindowControl().getWindowBackOffice().sendCommandTo(selectTimeCommand);
		} catch (ParseException e) {
			logError("", e);
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(newQuestionCtrl);
		ccwc = null;
		newQuestionCtrl = null;
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
		if (questionId != null) {
			videoQuestions.getQuestions().stream().filter((q) -> questionId.equals(q.getId())).findFirst()
					.ifPresent(q -> {
						try {
							q.setBegin(timeFormat.parse(startEl.getValue()));
							long timeLimit = -1;
							if (StringHelper.containsNonWhitespace(timeLimitEl.getValue())) {
								try {
									timeLimit = Long.parseLong(timeLimitEl.getValue());
								} catch (NumberFormatException e) {
									logError("", e);
								}
							}
							q.setTimeLimit(timeLimit);
							q.setStyle(colorDropdown.getSelectedKey());
							q.setAllowSkipping(options.isKeySelected(optionsKV.keys()[0]));
							q.setAllowNewAttempt(options.isKeySelected(optionsKV.keys()[1]));
							videoManager.saveQuestions(videoQuestions, repositoryEntry.getOlatResource());
							loadModel();
							reloadQuestions(ureq);
						} catch (ParseException e) {
							logError("", e);
						}
					});
		}
	}

	private void reloadQuestions(UserRequest ureq) {
		fireEvent(ureq, RELOAD_QUESTIONS_EVENT);
	}

	public void updateQuestion(String questionId) {
		loadModel();
	}
}
