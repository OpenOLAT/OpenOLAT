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
import java.util.List;
import java.util.TimeZone;

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.color.ColorUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.ui.assessment.components.QuestionTypeFlexiCellRenderer;
import org.olat.ims.qti21.ui.editor.AssessmentItemEditorController;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2022-11-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class QuestionController extends FormBasicController {
	public static final String EDIT_ACTION = "edit";
	private VideoQuestion question;
	private VideoQuestions questions;
	private final RepositoryEntry repositoryEntry;
	private TextElement startEl;
	private TextElement timeLimitEl;
	private ColorPickerElement colorPicker;
	private MultipleSelectionElement options;
	private final SelectionValues optionsKV;
	@Autowired
	private ColorService colorService;
	private final SimpleDateFormat timeFormat;
	private QuestionTableModel tableModel;
	private FlexiTableElement questionTable;
	private final long videoDurationInSeconds;
	private final String videoElementId;


	public QuestionController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
							  VideoQuestion question, VideoQuestions questions, long videoDurationInSeconds,
							  String videoElementId) {
		super(ureq, wControl, "question");
		this.repositoryEntry = repositoryEntry;
		this.question = question;
		this.questions = questions;
		this.videoDurationInSeconds = videoDurationInSeconds;
		this.videoElementId = videoElementId;

		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		optionsKV = new SelectionValues();
		optionsKV.add(SelectionValues.entry("allowSkipping", translate("form.question.allowSkipping")));
		optionsKV.add(SelectionValues.entry("allowNewAttempt", translate("form.question.allowNewAttempt")));

		initForm(ureq);
		setValues();
	}

	public void setQuestion(VideoQuestion question) {
		this.question = question;
		setValues();
	}

	public VideoQuestion getQuestion() {
		return question;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		startEl = uifactory.addTextElement("start", "form.common.start", 8,
				"00:00:00", formLayout);
		startEl.setMandatory(true);

		ApplyPositionButtonController startApplyPositionButtonController = new ApplyPositionButtonController(ureq,
				getWindowControl(), startEl.getFormDispatchId(), videoElementId, mainForm.getDispatchFieldId(), false);
		flc.put("startApplyPosition", startApplyPositionButtonController.getInitialComponent());

		timeLimitEl = uifactory.addTextElement("timeLimit", "form.question.timeLimit", 8,
				"", formLayout);
		timeLimitEl.setExampleKey("form.question.timeLimit.example", null);

		List<String> colorNames = colorService.getColors();
		List<ColorPickerElement.Color> colors = ColorUIFactory.createColors(colorNames, getLocale());

		colorPicker = uifactory.addColorPickerElement("color", "form.common.color", formLayout,
				colors);

		options = uifactory.addCheckboxesVertical("options", "form.question.options", formLayout,
				optionsKV.keys(), optionsKV.values(), 1);

		initTable(ureq, formLayout);

		uifactory.addFormSubmitButton("save", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	private void initTable(UserRequest ureq, FormItemContainer formLayout) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		Translator assessmentTranslator = Util.createPackageTranslator(AssessmentItemEditorController.class,
				ureq.getLocale());

		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionTableModel.QuestionColDef.question));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionTableModel.QuestionColDef.type,
				new QuestionTypeFlexiCellRenderer(assessmentTranslator)));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuestionTableModel.QuestionColDef.score));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(
				QuestionTableModel.QuestionColDef.edit.i18nHeaderKey(),
				translate(QuestionTableModel.QuestionColDef.edit.i18nHeaderKey()), EDIT_ACTION));

		tableModel = new QuestionTableModel(columnModel);
		questionTable = uifactory.addTableElement(getWindowControl(), "questionTable", tableModel,
				getTranslator(), formLayout);
		questionTable.setCustomizeColumns(false);
		questionTable.setNumOfRowsEnabled(false);
	}

	private void setValues() {
		if (question == null) {
			return;
		}

		startEl.setValue(timeFormat.format(question.getBegin()));
		if (question.getTimeLimit() <= 0) {
			timeLimitEl.setValue("");
		} else {
			timeLimitEl.setValue(Long.toString(question.getTimeLimit()));
		}
		if (question.getStyle() != null) {
			colorPicker.setColor(VideoModule.getColorFromMarkerStyle(question.getStyle()));
		}
		options.select(optionsKV.keys()[0], question.isAllowSkipping());
		options.select(optionsKV.keys()[1], question.isAllowNewAttempt());
		tableModel.setObjects(List.of(question));
		questionTable.reloadData();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (event instanceof SelectionEvent selectionEvent) {
			VideoQuestion question = tableModel.getObject(selectionEvent.getIndex());
			if (EDIT_ACTION.equals(selectionEvent.getCommand())) {
				doEdit(ureq, question);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doEdit(UserRequest ureq, VideoQuestion question) {
		fireEvent(ureq, new EditQuestionEvent(question.getId(), repositoryEntry));
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateStart();
		allOk &= validateTimeLimit();

		return allOk;
	}

	private boolean validateStart() {
		boolean allOk = true;
		startEl.clearError();
		if (!StringHelper.containsNonWhitespace(startEl.getValue())) {
			startEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		} else {
			try {
				long timeInSeconds = timeFormat.parse(startEl.getValue()).getTime() / 1000;
				if (timeInSeconds < 0 || (videoDurationInSeconds > 0 && timeInSeconds > videoDurationInSeconds)) {
					startEl.setErrorKey("form.error.timeNotValid");
					allOk = false;
				}
				allOk &= validateOnlyQuestion(startEl);
			} catch (Exception e) {
				startEl.setErrorKey("form.error.timeFormat");
				allOk = false;
			}
		}
		return allOk;
	}

	private boolean validateOnlyQuestion(TextElement timeEl) throws ParseException {
		if (question == null) {
			return true;
		}

		long timeInSeconds = timeFormat.parse(timeEl.getValue()).getTime() / 1000;
		boolean allOk = questions.getQuestions().stream()
				.noneMatch(q -> !q.getId().equals(question.getId()) && (q.getBegin().getTime() / 1000) == timeInSeconds);

		if (!allOk) {
			timeEl.setErrorKey("form.question.error.annoterQuestionExists");
		}
		return allOk;
	}

	private boolean validateTimeLimit() {
		boolean allOk = true;
		timeLimitEl.clearError();
		if (StringHelper.containsNonWhitespace(timeLimitEl.getValue())) {
			if (!StringHelper.isLong(timeLimitEl.getValue())) {
				timeLimitEl.setErrorKey("form.error.nointeger");
				allOk = false;
			} else if (Long.parseLong(timeLimitEl.getValue()) <= 0) {
				timeLimitEl.setErrorKey("form.error.timeNotValid");
				allOk = false;
			}
		}
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (question == null) {
			return;
		}

		try {
			question.setBegin(timeFormat.parse(startEl.getValue()));
			long timeLimit = -1;
			if (StringHelper.containsNonWhitespace(timeLimitEl.getValue())) {
				try {
					timeLimit = Long.parseLong(timeLimitEl.getValue());
				} catch (NumberFormatException e) {
					logError("", e);
				}
			}
			question.setTimeLimit(timeLimit);
			question.setStyle(VideoModule.getMarkerStyleFromColor(colorPicker.getColor().id()));
			question.setAllowSkipping(options.isKeySelected(optionsKV.keys()[0]));
			question.setAllowNewAttempt(options.isKeySelected(optionsKV.keys()[1]));
			fireEvent(ureq, Event.DONE_EVENT);
		} catch (ParseException e) {
			logError("", e);
		}
	}

	public void setQuestions(VideoQuestions questions) {
		this.questions = questions;
	}
}
