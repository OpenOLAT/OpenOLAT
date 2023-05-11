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
import java.io.Serial;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.olat.core.commons.services.color.ColorService;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.pool.QTI21QPoolServiceProvider;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.ui.SelectItemController;
import org.olat.modules.qpool.ui.events.QItemViewEvent;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.modules.video.ui.question.NewQuestionEvent;
import org.olat.modules.video.ui.question.NewQuestionItemCalloutController;
import org.olat.modules.video.ui.question.VideoQuestionRowComparator;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.RootNodeLookup;

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
	private ColorService colorService;
	private final RepositoryEntry repositoryEntry;
	private String questionId;
	private String currentTimeCode;
	private FormLink commandsButton;
	private HeaderCommandsController commandsController;
	private final SimpleDateFormat timeFormat;
	private SelectItemController selectItemController;
	@Autowired
	private QPoolService questionPoolService;
	@Autowired
	private QTI21QPoolServiceProvider qti21QPoolServiceProvider;
	@Autowired
	private QTI21Service qti21Service;
	@Autowired
	private VideoManager videoManager;
	private CloseableModalController cmc;

	protected QuestionsHeaderController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl, "questions_header");
		this.repositoryEntry = repositoryEntry;

		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		initForm(ureq);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(newQuestionCtrl);
		removeAsListenerAndDispose(commandsController);
		removeAsListenerAndDispose(selectItemController);
		ccwc = null;
		cmc = null;
		newQuestionCtrl = null;
		commandsController = null;
		selectItemController = null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousQuestionButton = uifactory.addFormLink("previousQuestion", "", "",
				formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		previousQuestionButton.setIconRightCSS("o_icon o_icon_back");
		previousQuestionButton.setForceOwnDirtyFormWarning(true);

		questionsDropdown = uifactory.addDropdownSingleselect("questions", "form.question.title",
				formLayout, questionsKV.keys(), questionsKV.values());
		questionsDropdown.addActionListener(FormEvent.ONCHANGE);
		questionsDropdown.setEscapeHtml(false);

		nextQuestionButton = uifactory.addFormLink("nextQuestion", "", "",
				formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		nextQuestionButton.setIconRightCSS("o_icon o_icon_start");
		nextQuestionButton.setForceOwnDirtyFormWarning(true);

		addQuestionButton = uifactory.addFormLink("addQuestion", "form.add", "form.add", formLayout, Link.BUTTON);
		addQuestionButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");

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
				setValues();
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
		commandsController = new HeaderCommandsController(ureq, getWindowControl(), true, true);
		commandsController.setCanDelete(!questionsKV.isEmpty());
		commandsController.setCanExport(!questionsKV.isEmpty());
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
			ccwc.deactivate();
			cleanUp();
			if (HeaderCommandsController.DELETE_EVENT.getCommand().equals(event.getCommand())) {
				doDeleteQuestion(ureq);
			} else if (HeaderCommandsController.IMPORT_EVENT == event) {
				doImport(ureq);
			} else if (HeaderCommandsController.EXPORT_EVENT == event) {
				doExport();
			} else if (HeaderCommandsController.EXPORT_ALL_EVENT == event) {
				doExportAll();
			}
		} else if (cmc == source) {
			cleanUp();
		} else if (selectItemController == source) {
			cmc.deactivate();
			cleanUp();
			if (event instanceof QItemViewEvent qItemViewEvent) {
				if ("select-item".equals(event.getCommand())) {
					fireEvent(ureq, new QuestionsImportedEvent(qItemViewEvent.getItemList()));
				}
			}
		}
		super.event(ureq, source, event);
	}

	private void doNewQuestion(UserRequest ureq, VideoQuestion newQuestion) {
		newQuestion.setBegin(new Date(getCurrentTime()));
		newQuestion.setStyle(VideoModule.getMarkerStyleFromColor(colorService.getColors().get(0)));
		questions.getQuestions().add(newQuestion);
		questionId = newQuestion.getId();
		setValues();
		fireEvent(ureq, QUESTION_ADDED_EVENT);
	}

	private void doDeleteQuestion(UserRequest ureq) {
		if (questionId == null) {
			return;
		}

		if (questionsKV.isEmpty()) {
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

	private void doImport(UserRequest ureq) {
		if (selectItemController != null) {
			return;
		}

		List<QItemType> itemTypes = questionPoolService.getAllItemTypes();
		List<QItemType> excludedItemTypes = new ArrayList<>();
		for(QItemType t:itemTypes) {
			if(t.getType().equalsIgnoreCase(QuestionType.DRAWING.name())
					|| t.getType().equalsIgnoreCase(QuestionType.ESSAY.name())
					|| t.getType().equalsIgnoreCase(QuestionType.UPLOAD.name())) {
				excludedItemTypes.add(t);
			}
		}

		selectItemController = new SelectItemController(ureq, getWindowControl(), QTI21Constants.QTI_21_FORMAT,
				excludedItemTypes);
		listenTo(selectItemController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectItemController.getInitialComponent(), true, translate("form.common.import"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doExport() {
		getOptionalQuestion().ifPresent(question -> {
			doExport(question);
			showInfo("tools.export.pool.success.one");
		});
	}

	private void doExport(VideoQuestion question) {
		File assessmentDirectory = videoManager.getAssessmentDirectory(repositoryEntry.getOlatResource());
		File assessmentItemDirectory = new File(assessmentDirectory, question.getQuestionRootPath());
		File assessmentItemFile = new File(assessmentItemDirectory, question.getQuestionFilename());
		URI assessmentKey = assessmentItemFile.toURI();
		ResolvedAssessmentItem resolvedAssessmentItem = qti21Service.loadAndResolveAssessmentItem(assessmentKey, assessmentItemDirectory);
		RootNodeLookup<AssessmentItem> rootNode = resolvedAssessmentItem.getItemLookup();
		AssessmentItem assessmentItem = rootNode.extractIfSuccessful();
		File itemFile = new File(rootNode.getSystemId());
		ManifestMetadataBuilder manifestMetadataBuilder = new ManifestMetadataBuilder();
		qti21QPoolServiceProvider.importAssessmentItemRef(getIdentity(), assessmentItem, itemFile, manifestMetadataBuilder, getLocale());
	}

	private void doExportAll() {
		for (VideoQuestion question : questions.getQuestions()) {
			doExport(question);
		}
		if (questions.getQuestions().size() == 1) {
			showInfo("tools.export.pool.success.one");
		} else {
			showInfo("tools.export.pool.success", Integer.toString(questions.getQuestions().size()));
		}
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

	public static class QuestionsImportedEvent extends Event {
		@Serial
		private static final long serialVersionUID = 1257611132460737138L;
		private static final String COMMAND = "question.selected";
		private final List<QuestionItemView> itemList;

		public QuestionsImportedEvent(List<QuestionItemView> itemList) {
			super(COMMAND);
			this.itemList = itemList;
		}

		public List<QuestionItemView> getItemList() {
			return itemList;
		}
	}
}
