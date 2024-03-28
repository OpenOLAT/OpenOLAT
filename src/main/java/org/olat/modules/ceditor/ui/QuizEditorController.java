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
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
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
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.manager.ContentEditorQti;
import org.olat.modules.ceditor.model.QuizElement;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.ceditor.model.jpa.QuizPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.qpool.QPoolService;
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

	public QuizEditorController(UserRequest ureq, WindowControl wControl,
								QuizPart quizPart, PageElementStore<QuizElement> store) {
		super(ureq, wControl, "quiz_editor");
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

		commandsButton = uifactory.addFormLink("commands", "", "", formLayout,
				Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		commandsButton.setIconRightCSS("o_icon o_icon_commands");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionModel.QuestionColumns.up.getI18nKey(),
				QuestionModel.QuestionColumns.up.ordinal(), UP_ACTION, new BooleanCellRenderer(
						new StaticFlexiCellRenderer(translate("quiz.up"), UP_ACTION), null
		)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionModel.QuestionColumns.down.getI18nKey(),
				QuestionModel.QuestionColumns.down.ordinal(), DOWN_ACTION, new BooleanCellRenderer(
				new StaticFlexiCellRenderer(translate("quiz.down"), DOWN_ACTION), null
		)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionModel.QuestionColumns.title.getI18nKey(), QuestionModel.QuestionColumns.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionModel.QuestionColumns.type.getI18nKey(), QuestionModel.QuestionColumns.type.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionModel.QuestionColumns.edit.getI18nKey(),
				QuestionModel.QuestionColumns.edit.ordinal(), EDIT_ACTION, new BooleanCellRenderer(
				new StaticFlexiCellRenderer(translate("quiz.edit"), EDIT_ACTION), null
		)));
		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(QuestionModel.QuestionColumns.tools.getI18nKey(),
				QuestionModel.QuestionColumns.tools.ordinal());
		toolsColumn.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsColumn.setColumnCssClass("o_icon-fws o_col_sticky_right o_col_action");
		columnsModel.addFlexiColumnModel(toolsColumn);

		tableModel = new QuestionModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "quiz.questions", tableModel, getTranslator(), formLayout);
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
				toolLink.setTitle(translate("quiz.tools"));
			}
			toolLink.setUserObject(questionRow);
			questionRow.setToolLink(toolLink);
		}
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
				}
			}
		} else if (newQuestionController == source) {
			QuizQuestion quizQuestion = newQuestionController.getQuizQuestion();
			ccwc.deactivate();
			cleanUp();
			if (event == FormEvent.DONE_EVENT) {
				doNewQuestion(ureq, quizQuestion);
			}
		} else if (editQuestionController == source) {
			QuizQuestion quizQuestion = editQuestionController.getQuizQuestion();
			cmc.deactivate();
			cleanUp();
			if (event == FormEvent.DONE_EVENT) {
				doSaveQuestion(ureq, quizQuestion);
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
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (addQuestionButton == source) {
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
		flc.contextPut("title", quizPart.getSettings().getTitle());
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
		commandsController = new HeaderCommandsController(ureq, getWindowControl(), true, true, false);
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

		List<QItemType> itemTypes = questionPoolService.getAllItemTypes();
		List<QItemType> excludedItemTypes = new ArrayList<>();
		for (QItemType qItemType : itemTypes) {
			if (qItemType.getType().equalsIgnoreCase(QuestionType.DRAWING.name())
					|| qItemType.getType().equalsIgnoreCase(QuestionType.ESSAY.name())
					|| qItemType.getType().equalsIgnoreCase(QuestionType.UPLOAD.name())) {
				excludedItemTypes.add(qItemType);
			}
		}

		selectItemController = new SelectItemController(ureq, getWindowControl(), QTI21Constants.QTI_21_FORMAT,
				excludedItemTypes);
		listenTo(selectItemController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectItemController.getInitialComponent(), true, translate("tools.import.qpool"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doNewQuestion(UserRequest ureq, QuizQuestion quizQuestion) {
		QuizSettings quizSettings = quizPart.getSettings();
		List<QuizQuestion> questions = quizSettings.getQuestions();
		questions.add(quizQuestion);
		quizPart.setSettings(quizSettings);
		quizPart = (QuizPart) store.savePageElement(quizPart);
		dbInstance.commit();
		loadModel();

		fireEvent(ureq, new ChangePartEvent(quizPart));

		doEditQuestion(ureq, quizQuestion);
	}

	private void doEditQuestion(UserRequest ureq, QuizQuestion quizQuestion) {
		editQuestionController = new EditQuestionController(ureq, getWindowControl(), quizPart, quizQuestion);
		listenTo(editQuestionController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editQuestionController.getInitialComponent(), true, translate("add.quiz"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doDeleteQuestion(UserRequest ureq, QuizQuestion quizQuestion) {
		QuizSettings quizSettings = quizPart.getSettings();
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
		List<QuizQuestion> newQuestions = quizSettings.getQuestions().stream()
				.filter(q -> !q.getId().equals(quizQuestion.getId())).collect(Collectors.toList());
		newQuestions.add(quizQuestion);
		quizSettings.setQuestions(newQuestions);

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

	private static class ToolsController extends BasicController {
		private static final Event EDIT_EVENT = new Event("edit");
		private static final Event DUPLICATE_EVENT = new Event("duplicate");
		private static final Event DELETE_EVENT = new Event("delete");
		private final Link editLink;
		private final Link duplicateLink;
		private final Link deleteLink;
		private final QuestionRow row;

		protected ToolsController(UserRequest ureq, WindowControl wControl, QuestionRow row) {
			super(ureq, wControl);

			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("quiz_table_row_tools");

			editLink = LinkFactory.createLink("edit", "edit", getTranslator(), mainVC, this, Link.LINK);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			mainVC.put("edit", editLink);

			duplicateLink = LinkFactory.createLink("duplicate", "duplicate", getTranslator(), mainVC, this, Link.LINK);
			duplicateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_duplicate");
			mainVC.put("duplicate", duplicateLink);

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
