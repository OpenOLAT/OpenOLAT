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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.manager.ContentEditorQti;
import org.olat.modules.ceditor.manager.EssayGenerationQuizPartSinkImpl;
import org.olat.modules.ceditor.model.QuizElement;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.ceditor.model.jpa.QuizPart;
import org.olat.modules.ceditor.ui.component.ContentEditorFragment;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.ui.SelectItemController;
import org.olat.modules.qpool.ui.events.QItemViewEvent;
import org.olat.modules.video.ui.editor.HeaderCommandsController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-03-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class QuizEditorController extends FormBasicController implements PageElementEditorController {

	private static final int MAX_NUMBER_OF_QUESTIONS = 20;
	private static final String UP_ACTION = "up";
	private static final String DOWN_ACTION = "down";
	private static final String EDIT_ACTION = "edit";
	private static final String CMD_TOOLS = "tools";

	private QuizPart quizPart;
	private final PageElementStore<QuizElement> store;
	private QuestionModel tableModel;
	private FlexiTableElement tableEl;
	private FormLink addQuestionButton;
	private FormLink commandsButton;

	/** AI generation polling — re-checks the QuizPart row periodically while
	 *  the placeholder marker is still on its title. Each poll re-fetches the
	 *  entity from the DB and re-renders the editor. Capped to roughly the
	 *  generation timeout (~3 minutes at the 3-second cadence). */
	private static final int AI_GEN_POLL_DELAY_MS = 3000;
	private static final int MAX_AI_GEN_POLL_ATTEMPTS = 90;
	private FormLink aiGenerationPollLink;
	private int aiGenerationPollAttempts = 0;
	private boolean aiGenerationPollTimedOut = false;

	private HeaderCommandsController commandsController;
	private CloseableCalloutWindowController ccwc;
	private SelectItemController selectItemController;
	private CloseableModalController cmc;
	private NewQuestionItemCalloutController newQuestionController;
	private EditQuestionController editQuestionController;
	private ToolsController toolsController;

	@Autowired
	private QPoolService questionPoolService;
	@Autowired
	private ContentEditorQti contentEditorQti;
	@Autowired
	private DB dbInstance;
	@Autowired
	private org.olat.core.commons.services.ai.AiModule aiModule;

	public QuizEditorController(UserRequest ureq, WindowControl wControl,
								QuizPart quizPart, PageElementStore<QuizElement> store) {
		super(ureq, wControl, "quiz_editor");
		// Refresh from DB so the editor never shows "generating" based on a
		// stale in-memory copy from the page editor cache. The AI sink
		// removes the GENERATING_TITLE_MARKER asynchronously after the job
		// finishes, but the page editor may still hold the pre-update copy.
		// em.find() on a managed entity returns the cached instance, so
		// detach first to force a real DB read.
		var em = dbInstance.getCurrentEntityManager();
		Long key = quizPart.getKey();
		if (key != null) {
			if (em.contains(quizPart)) {
				em.refresh(quizPart);
			} else {
				QuizPart fresh = em.find(QuizPart.class, key);
				if (fresh != null) {
					quizPart = fresh;
				}
			}
		}
		this.quizPart = quizPart;
		this.store = store;

		initForm(ureq);
		loadModel();

		setBlockLayoutClass(quizPart.getSettings());

		finishQuizInitialization(ureq);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(commandsController);
		removeAsListenerAndDispose(selectItemController);
		removeAsListenerAndDispose(editQuestionController);
		removeAsListenerAndDispose(newQuestionController);
		removeAsListenerAndDispose(toolsController);
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(cmc);
		commandsController = null;
		selectItemController = null;
		editQuestionController = null;
		newQuestionController = null;
		toolsController = null;
		ccwc = null;
		cmc = null;
	}

	private void setBlockLayoutClass(QuizSettings quizSettings) {
		flc.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(quizSettings, false));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addQuestionButton = uifactory.addFormLink("addQuestion", "addremove.add.text", "", formLayout, Link.BUTTON);
		addQuestionButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		addQuestionButton.setIconRightCSS("o_icon o_icon_caret");

		commandsButton = uifactory.addFormLink("commands", "", "", formLayout,
				Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		commandsButton.setIconRightCSS("o_icon o_icon_commands");
		commandsButton.setTitle("action.more");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel upColumn = new DefaultFlexiColumnModel(QuestionModel.QuestionColumns.up);
		upColumn.setCellRenderer(new BooleanCellRenderer(new StaticFlexiCellRenderer(null, UP_ACTION, null,
				"o_icon o_icon o_icon-lg o_icon_move_up", translate("quit.up.title")), null));
		upColumn.setIconHeader("o_icon o_icon o_icon-lg o_icon_move_up");
		upColumn.setColumnCssClass("o_up");
		upColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(upColumn);
		
		DefaultFlexiColumnModel downColumn = new DefaultFlexiColumnModel(QuestionModel.QuestionColumns.down);
		downColumn.setCellRenderer(new BooleanCellRenderer(new StaticFlexiCellRenderer(null, DOWN_ACTION, null,
				"o_icon o_icon o_icon-lg o_icon_move_down", translate("quit.down.title")), null));
		downColumn.setIconHeader("o_icon o_icon o_icon-lg o_icon_move_down");
		downColumn.setColumnCssClass("o_down");
		downColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(downColumn);
		
		FlexiCellRenderer titleRenderer = new StaticFlexiCellRenderer(EDIT_ACTION, new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionModel.QuestionColumns.title.i18nHeaderKey(),
				QuestionModel.QuestionColumns.title.ordinal(), EDIT_ACTION, titleRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionModel.QuestionColumns.type));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(QuestionModel.QuestionColumns.tools));

		tableModel = new QuestionModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "quiz.questions", tableModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		updateUI();
	}

	private void loadModel() {
		List<QuestionRow> questionRows = new ArrayList<>();

		for (QuizQuestion quizQuestion : quizPart.getSettings().getQuestions()) {
			questionRows.add(new QuestionRow(quizQuestion));
		}

		tableModel.setObjects(questionRows);
		tableEl.reset();
		addTools();
	}

	private void addTools() {
		for (QuestionRow questionRow : tableModel.getObjects()) {
			String toolId = "tool_" + questionRow.getId();
			FormLink toolLink = (FormLink) tableEl.getFormComponent(toolId);
			if (toolLink == null) {
				toolLink = uifactory.addFormLink(toolId, CMD_TOOLS, "", tableEl,
						Link.LINK | Link.NONTRANSLATED);
				toolLink.setTranslator(getTranslator());
				toolLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
				toolLink.setTitle(translate("action.more"));
			}
			toolLink.setUserObject(questionRow);
			questionRow.setToolLink(toolLink);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof ContentEditorFragment && event instanceof ChangePartEvent) {
			loadModel();
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof QuizInspectorController && event instanceof ChangePartEvent changePartEvent &&
				changePartEvent.getElement() instanceof QuizPart updatedQuizPart) {
			if (updatedQuizPart.equals(quizPart)) {
				quizPart = updatedQuizPart;
				updateUI();
				setBlockLayoutClass(quizPart.getSettings());
			}
		} else if (commandsController == source) {
			ccwc.deactivate();
			if (HeaderCommandsController.IMPORT_EVENT == event) {
				doImport(ureq);
			} else if (HeaderCommandsController.EXPORT_ALL_EVENT == event) {
				doExportAll();
			}
		} else if (ccwc == source) {
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		} else if (selectItemController == source) {
			cmc.deactivate();
			cleanUp();
			if (event instanceof QItemViewEvent qItemViewEvent) {
				if ("select-item".equals(event.getCommand())) {
					doImport(ureq, qItemViewEvent.getItemList());
				}
			}
			updateUI();
		} else if (newQuestionController == source) {
			QuizQuestion quizQuestion = newQuestionController.getQuizQuestion();
			ccwc.deactivate();
			cleanUp();
			if (event == FormEvent.DONE_EVENT) {
				doNewQuestion(ureq, quizQuestion);
				updateUI();
			}
		} else if (editQuestionController == source) {
			QuizQuestion quizQuestion = editQuestionController.getQuizQuestion();
			cmc.deactivate();
			cleanUp();
			if (event == FormEvent.DONE_EVENT) {
				doSaveQuestion(ureq, quizQuestion);
				updateUI();
			}
		} else if (toolsController == source) {
			QuestionRow questionRow = toolsController.getRow();
			ccwc.deactivate();
			cleanUp();
			if (ToolsController.EDIT_EVENT == event) {
				doEditQuestion(ureq, questionRow.getQuizQuestion());
			} else if (ToolsController.DELETE_EVENT == event) {
				doDeleteQuestion(ureq, questionRow.getQuizQuestion());
			} else if (ToolsController.DUPLICATE_EVENT == event) {
				doDuplicateQuestion(ureq, questionRow.getQuizQuestion());
			}
			updateUI();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (aiGenerationPollLink != null && source == aiGenerationPollLink) {
			doPollAiGeneration();
		} else if (addQuestionButton == source) {
			doAddQuestion(ureq);
		} else if (commandsButton == source) {
			doCommands(ureq);
		} else if (event instanceof SelectionEvent selectionEvent) {
			QuizQuestion quizQuestion = quizPart.getSettings().getQuestions().get(selectionEvent.getIndex());
			if (EDIT_ACTION.equals(selectionEvent.getCommand())) {
				doEditQuestion(ureq, quizQuestion);
			} else if (UP_ACTION.equals(selectionEvent.getCommand())) {
				doUp(ureq, quizQuestion);
			} else if (DOWN_ACTION.equals(selectionEvent.getCommand())) {
				doDown(ureq, quizQuestion);
			}
		} else if (source instanceof FormLink formLink &&
				CMD_TOOLS.equals(formLink.getCmd()) && formLink.getUserObject() instanceof QuestionRow questionRow) {
			doOpenTools(ureq, formLink, questionRow);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void updateUI() {
		String rawTitle = quizPart.getSettings().getTitle();
		String aiState = "";
		String displayTitle = rawTitle;
		if (rawTitle != null && rawTitle.startsWith(EssayGenerationQuizPartSinkImpl.GENERATING_TITLE_MARKER)) {
			aiState = "generating";
			displayTitle = rawTitle.substring(EssayGenerationQuizPartSinkImpl.GENERATING_TITLE_MARKER.length()).trim();
		} else if (rawTitle != null && rawTitle.startsWith(EssayGenerationQuizPartSinkImpl.FAILED_TITLE_MARKER)) {
			aiState = "failed";
			displayTitle = rawTitle.substring(EssayGenerationQuizPartSinkImpl.FAILED_TITLE_MARKER.length()).trim();
		}
		if ("generating".equals(aiState) && aiGenerationPollTimedOut) {
			// Client-side poll cap reached: render the stalled hint without the
			// poll script so the timer is not re-armed. The job may still finish
			// server-side; reopening the editor resumes polling.
			aiState = "stalled";
		}
		flc.contextPut("aiState", aiState);
		flc.contextPut("title", displayTitle);
		flc.contextPut("aiGenerationWaitingLabel", translate("ai.generation.editor.waiting"));
		if ("generating".equals(aiState)) {
			if (aiGenerationPollLink == null) {
				aiGenerationPollLink = uifactory.addFormLink(
						"ai.generation.poll", "ai.generation.poll", "", "",
						flc, Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
				aiGenerationPollLink.setElementCssClass("o_ai_generation_poll");
				// Timer-driven link — must not trigger the dirty-form warning
				// while the author has unsaved edits in the surrounding form.
				if (aiGenerationPollLink instanceof FormLinkImpl formLinkImpl) {
					formLinkImpl.getComponent().setSuppressDirtyFormWarning(true);
				}
			}
			// First poll fires fast so any state that changed while the editor
			// was hidden (e.g. AI finished during page-editor pre-render) is
			// reflected as soon as the user opens edit mode. Subsequent polls
			// fall back to the normal cadence to avoid hammering the server
			// during a long generation run.
			int delay = aiGenerationPollAttempts == 0 ? 500 : AI_GEN_POLL_DELAY_MS;
			flc.contextPut("aiGenerationPollDelayMs", Integer.valueOf(delay));
		} else {
			flc.contextPut("aiGenerationPollDelayMs", Integer.valueOf(0));
		}
		addQuestionButton.setEnabled(canAddQuestions()
				&& !"generating".equals(aiState) && !"stalled".equals(aiState));
	}

	/** Poll tick — invoked by the hidden link clicked from a JS setTimeout
	 *  while the placeholder marker is still on the title. Re-fetches the
	 *  QuizPart from the DB and refreshes the editor. */
	private void doPollAiGeneration() {
		aiGenerationPollAttempts++;
		QuizPart fresh = dbInstance.getCurrentEntityManager().find(QuizPart.class, quizPart.getKey());
		if (fresh != null) {
			quizPart = fresh;
		}
		if (aiGenerationPollAttempts >= MAX_AI_GEN_POLL_ATTEMPTS) {
			// Client-side cap: stop re-arming the poll timer but don't fake a
			// failure — the job may still finish on the batch queue. Reopening
			// the editor resumes polling.
			aiGenerationPollTimedOut = true;
		}
		loadModel();
		updateUI();
		flc.setDirty(true);
	}

	private boolean canAddQuestions() {
		return quizPart.getSettings().getQuestions().size() < MAX_NUMBER_OF_QUESTIONS;
	}

	private void doAddQuestion(UserRequest ureq) {
		finishQuizInitialization(ureq);

		newQuestionController = new NewQuestionItemCalloutController(ureq, getWindowControl(), quizPart);
		listenTo(newQuestionController);

		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), newQuestionController.getInitialComponent(),
				addQuestionButton.getFormDispatchId(), "", true, "",
				new CalloutSettings(false));
		listenTo(ccwc);
		ccwc.activate();
	}

	private void finishQuizInitialization(UserRequest ureq) {
		if (contentEditorQti.finishInitialization(quizPart, getLocale())) {
			quizPart = (QuizPart) store.savePageElement(quizPart);
			dbInstance.commit();
			loadModel();

			fireEvent(ureq, new ChangePartEvent(quizPart));
		}
	}

	private void doCommands(UserRequest ureq) {
		commandsController = new HeaderCommandsController(ureq, getWindowControl(), canAddQuestions(),
				false, true, false);
		listenTo(commandsController);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), commandsController.getInitialComponent(),
				commandsButton.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}

	private void doImport(UserRequest ureq) {
		if (selectItemController != null) {
			return;
		}

		// Essay items are only allowed in the pool import when AI essay
		// grading is configured — otherwise the imported essay would have
		// no automatic correction path inside the page-quiz runtime.
		boolean essayAllowed = aiModule != null && aiModule.isEssayGradingEnabled();
		List<QItemType> itemTypes = questionPoolService.getAllItemTypes();
		List<QItemType> excludedItemTypes = new ArrayList<>();
		for (QItemType qItemType : itemTypes) {
			if (qItemType.getType().equalsIgnoreCase(QuestionType.SC.name())
					|| qItemType.getType().equalsIgnoreCase(QuestionType.MC.name())
					|| qItemType.getType().equalsIgnoreCase(QuestionType.FIB.name())
					|| qItemType.getType().equalsIgnoreCase(QuestionType.NUMERICAL.name())
					|| qItemType.getType().equalsIgnoreCase(QuestionType.INLINECHOICE.name())) {
				continue;
			}
			if (essayAllowed && qItemType.getType().equalsIgnoreCase(QuestionType.ESSAY.name())) {
				continue;
			}
			excludedItemTypes.add(qItemType);
		}

		selectItemController = new SelectItemController(ureq, getWindowControl(), QTI21Constants.QTI_21_FORMAT,
				excludedItemTypes);
		listenTo(selectItemController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectItemController.getInitialComponent(), true, translate("tools.import.qpool"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doImport(UserRequest ureq, List<QuestionItemView> questionItems) {
		QuizSettings quizSettings = quizPart.getSettings();
		List<QuizQuestion> questions = quizSettings.getQuestions();
		int maxNumberToImport = MAX_NUMBER_OF_QUESTIONS - questions.size();
		List<QuizQuestion> importedQuestions = contentEditorQti.importQuestions(quizPart, questionItems,
				maxNumberToImport, getLocale());
		questions.addAll(importedQuestions);

		storeSettings(ureq, quizSettings);
	}

	private void doExportAll() {
		QuizSettings quizSettings = quizPart.getSettings();
		List<QuizQuestion> questions = quizSettings.getQuestions();
		for (QuizQuestion question : questions) {
			doExport(question);
		}
		if (questions.size() == 1) {
			showInfo("quiz.export.pool.success.one");
		} else {
			showInfo("quiz.export.pool.success", Integer.toString(questions.size()));
		}
	}

	private void doExport(QuizQuestion question) {
		contentEditorQti.exportQuestion(quizPart, question, getLocale(), getIdentity());
	}

	private void doNewQuestion(UserRequest ureq, QuizQuestion quizQuestion) {
		QuizSettings quizSettings = quizPart.getSettings();
		List<QuizQuestion> questions = quizSettings.getQuestions();
		questions.add(quizQuestion);

		storeSettings(ureq, quizSettings);

		doEditQuestion(ureq, quizQuestion);
	}

	private void doEditQuestion(UserRequest ureq, QuizQuestion quizQuestion) {
		editQuestionController = new EditQuestionController(ureq, getWindowControl(), quizPart, quizQuestion);
		listenTo(editQuestionController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editQuestionController.getInitialComponent(), displayAsOverlay(quizQuestion), translate("add.quiz"));
		cmc.activate();
		listenTo(cmc);
	}

	private boolean displayAsOverlay(QuizQuestion quizQuestion) {
		QTI21QuestionType type = QTI21QuestionType.safeValueOf(quizQuestion.getType());
		if (type != null) {
			// Opening a text gap or numerical gap question as overlay leads to layer changes in the
			// guistackmodalpanel (which holds both tiny and the gap value input popup). This is not a problem as
			// such, but since we set the value in tiny by JS, we first execute the JS and then re-render the
			// entire modal stack.
			if (type == QTI21QuestionType.fib || type == QTI21QuestionType.numerical) {
				return false;
			}
		}
		return true;
	}

	private void doDeleteQuestion(UserRequest ureq, QuizQuestion quizQuestion) {
		QuizSettings quizSettings = quizPart.getSettings();
		contentEditorQti.deleteQuestion(quizPart, quizQuestion);
		List<QuizQuestion> newQuestions = quizSettings.getQuestions().stream()
				.filter(q -> !q.getId().equals(quizQuestion.getId())).collect(Collectors.toList());
		quizSettings.setQuestions(newQuestions);

		storeSettings(ureq, quizSettings);
	}

	private void storeSettings(UserRequest ureq, QuizSettings quizSettings) {
		quizPart.setSettings(quizSettings);
		quizPart = (QuizPart) store.savePageElement(quizPart);
		dbInstance.commit();
		loadModel();

		fireEvent(ureq, new ChangePartEvent(quizPart));
	}

	private void doDuplicateQuestion(UserRequest ureq, QuizQuestion quizQuestion) {
		QuizSettings quizSettings = quizPart.getSettings();

		List<QuizQuestion> quizQuestions = quizSettings.getQuestions();
		int originalIndex = quizQuestions.indexOf(quizQuestion);
		QuizQuestion clonedQuestion = contentEditorQti.cloneQuestion(quizPart, quizQuestion, getTranslator());
		quizQuestions.add(originalIndex + 1, clonedQuestion);

		storeSettings(ureq, quizSettings);
	}

	private void doDown(UserRequest ureq, QuizQuestion quizQuestion) {
		QuizSettings quizSettings = quizPart.getSettings();

		List<QuizQuestion> quizQuestions = quizSettings.getQuestions();
		int originalIndex = quizQuestions.indexOf(quizQuestion);
		quizQuestions.remove(quizQuestion);
		int newIndex = Math.min(originalIndex + 1, quizQuestions.size());
		quizQuestions.add(newIndex, quizQuestion);

		storeSettings(ureq, quizSettings);
	}

	private void doUp(UserRequest ureq, QuizQuestion quizQuestion) {
		QuizSettings quizSettings = quizPart.getSettings();

		List<QuizQuestion> quizQuestions = quizSettings.getQuestions();
		int originalIndex = quizQuestions.indexOf(quizQuestion);
		quizQuestions.remove(quizQuestion);
		int newIndex = Math.max(originalIndex - 1, 0);
		quizQuestions.add(newIndex, quizQuestion);

		storeSettings(ureq, quizSettings);
	}

	private void doSaveQuestion(UserRequest ureq, QuizQuestion quizQuestion) {
		QuizSettings quizSettings = quizPart.getSettings();

		List<QuizQuestion> quizQuestions = quizSettings.getQuestions();
		int index = quizQuestions.indexOf(quizQuestion);
		quizQuestions.remove(quizQuestion);
		quizQuestions.add(index, quizQuestion);

		storeSettings(ureq, quizSettings);
	}

	private void doOpenTools(UserRequest ureq, FormLink formLink, QuestionRow questionRow) {
		toolsController = new ToolsController(ureq, getWindowControl(), questionRow);
		listenTo(toolsController);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), toolsController.getInitialComponent(),
				formLink.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}

	private class ToolsController extends BasicController {
		private static final Event EDIT_EVENT = new Event("edit");
		private static final Event DUPLICATE_EVENT = new Event("duplicate");
		private static final Event DELETE_EVENT = new Event("delete");
		private final Link editLink;
		private Link duplicateLink;
		private final Link deleteLink;
		private final QuestionRow row;

		protected ToolsController(UserRequest ureq, WindowControl wControl, QuestionRow row) {
			super(ureq, wControl);

			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("quiz_table_row_tools");

			editLink = LinkFactory.createLink("edit", "edit", getTranslator(), mainVC, this, Link.LINK);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			mainVC.put("edit", editLink);

			if (canAddQuestions()) {
				duplicateLink = LinkFactory.createLink("duplicate", "duplicate", getTranslator(), mainVC, this, Link.LINK);
				duplicateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_duplicate");
				mainVC.put("duplicate", duplicateLink);
			}

			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			mainVC.put("delete", deleteLink);

			putInitialPanel(mainVC);
		}

		public QuestionRow getRow() {
			return row;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (editLink == source) {
				fireEvent(ureq, EDIT_EVENT);
			} else if (duplicateLink == source) {
				fireEvent(ureq, DUPLICATE_EVENT);
			} else if (deleteLink == source) {
				fireEvent(ureq, DELETE_EVENT);
			}
		}
	}
}
