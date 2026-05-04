/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.qpool.ui;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;

import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.essay.AiRateLimitExceededException;
import org.olat.core.commons.services.ai.essay.EssayGenerationService;
import org.olat.core.commons.services.ai.essay.EssayGenerationService.GenerationRequest;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.docxToMarkdown.DocxToMarkdownResult;
import org.olat.core.util.docxToMarkdown.DocxToMarkdownService;
import org.olat.modules.qpool.ui.events.QPoolEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Modal dialog that lets pool authors generate a batch of AI questions
 * (multiple-choice + essay, when AI grading is configured) from a piece
 * of source text or an uploaded {@code .docx} / {@code .md} / plain-text
 * file.
 * <p>
 * Flow:
 * <ol>
 *   <li>The user chooses a source — file upload or text paste.</li>
 *   <li>{@code .docx} uploads are converted to Markdown server-side.</li>
 *   <li>The default question counts are picked from a length-based
 *       heuristic ({@link #suggestedEssayCount(int)} /
 *       {@link #suggestedMcCount(int)}) but can be overridden by the
 *       user. Essay counts are only offered when
 *       {@code aiModule.isEssayGradingEnabled()} is true.</li>
 *   <li>On submit the backend kicks off an async {@link EssayGenerationService}
 *       job, the dialog closes immediately with an info message; the
 *       generated items appear in the pool list once the job completes.</li>
 * </ol>
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class NewAiQuestionsImportController extends FormBasicController {

	private static final Set<String> UPLOAD_MIME_TYPES = Set.of(
			"text/markdown", "text/x-markdown", "text/plain", "application/octet-stream",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document");

	private static final String MODE_FILE = "file";
	private static final String MODE_TEXT = "text";

	/** Default upload size limit in KB (50 MB) — matches MarkdownImportController. */
	public static final int MAX_UPLOAD_SIZE_KB = 51200;
	/** Maximum source text length (chars) — same cap as the legacy NewAiItemController. */
	private static final int MAX_INPUT_LEN = 60000;
	/** Maximum number of questions per leg — UI cap. */
	private static final int MAX_AI_COUNT = 10;
	/** Minimum number of questions per leg (zero = opt-out of that leg). */
	private static final int MIN_AI_COUNT = 0;

	private SingleSelection modeEl;
	private FileElement fileUploadEl;
	private TextAreaElement contentEl;
	private IntegerElement aiMcCountEl;
	private IntegerElement aiEssayCountEl;

	private File tempFile;
	/** Last heuristic-derived defaults so we can re-suggest when the source changes. */
	private int suggestedEssay;
	private int suggestedMc;

	/** Optional taxonomy level the pool author was browsing — items get stamped with it. */
	private final Long taxonomyLevelKey;

	@Autowired
	private DocxToMarkdownService docxToMarkdownService;
	@Autowired
	private EssayGenerationService essayGenerationService;
	@Autowired
	private AiModule aiModule;

	public NewAiQuestionsImportController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null);
	}

	/**
	 * @param taxonomyLevelKey optional taxonomy level the user is browsing —
	 *                         when non-null the generated items are stamped
	 *                         with it; pass {@code null} to leave them unbound
	 */
	public NewAiQuestionsImportController(UserRequest ureq, WindowControl wControl, Long taxonomyLevelKey) {
		super(ureq, wControl);
		this.taxonomyLevelKey = taxonomyLevelKey;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_new_ai_questions_form");
		setFormDescription("ai.questions.desc");

		// Mode selection — file vs paste
		String[] modeKeys = { MODE_FILE, MODE_TEXT };
		String[] modeTitles = { translate("ai.questions.mode.file"), translate("ai.questions.mode.text") };
		String[] modeDescriptions = { translate("ai.questions.mode.file.desc"), translate("ai.questions.mode.text.desc") };
		String[] modeIcons = { "o_icon o_icon_upload", "o_icon o_icon_edit" };
		modeEl = uifactory.addCardSingleSelectHorizontal("ai.questions.mode", null, formLayout,
				modeKeys, modeTitles, modeDescriptions, modeIcons);
		modeEl.select(MODE_TEXT, true);
		modeEl.addActionListener(FormEvent.ONCHANGE);

		// File upload — supports .docx, .md, plain text
		fileUploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "ai.questions.file", formLayout);
		fileUploadEl.limitToMimeType(UPLOAD_MIME_TYPES, "ai.questions.file.error", null);
		fileUploadEl.setMaxUploadSizeKB(MAX_UPLOAD_SIZE_KB, "ai.questions.file.toolarge", null);
		fileUploadEl.addActionListener(FormEvent.ONCHANGE);
		fileUploadEl.setVisible(false);

		// Text paste
		contentEl = uifactory.addTextAreaElement("ai.questions.content", "ai.questions.content", -1, 12, 100, false, false, "", formLayout);
		contentEl.setPlaceholderKey("ai.questions.content.placeholder", null);
		contentEl.setNotLongerThanCheck(MAX_INPUT_LEN, "form.error.toolong");
		contentEl.setExampleKey("ai.questions.content.example", new String[]{ String.valueOf(MAX_INPUT_LEN) });
		contentEl.addActionListener(FormEvent.ONCHANGE);

		// MC count — always available
		aiMcCountEl = uifactory.addIntegerElement("ai.questions.count.mc",
				"ai.questions.count.mc", 0, formLayout);
		aiMcCountEl.setDisplaySize(3);
		aiMcCountEl.setHelpTextKey("ai.questions.count.help", null);

		// Essay count — only shown when essay grading is configured
		if (aiModule != null && aiModule.isEssayGradingEnabled()) {
			aiEssayCountEl = uifactory.addIntegerElement("ai.questions.count.essay",
					"ai.questions.count.essay", 0, formLayout);
			aiEssayCountEl.setDisplaySize(3);
			aiEssayCountEl.setHelpTextKey("ai.questions.count.help", null);
		}

		// Initial suggested counts based on an empty input (so all fields have a starting value).
		applySuggestedCounts(0);

		// Buttons
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("ai.questions.submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == modeEl) {
			boolean isFileMode = MODE_FILE.equals(modeEl.getSelectedKey());
			fileUploadEl.setVisible(isFileMode);
			fileUploadEl.setMandatory(isFileMode);
			contentEl.setVisible(!isFileMode);
			flc.setDirty(true);
		} else if (source == fileUploadEl) {
			// Pre-convert .docx the moment it is uploaded so we can update the
			// suggested counts based on the converted markdown's length. This
			// is the "pool only" pre-conversion path the design report agreed
			// on — the ceditor still defers conversion until submit.
			String filename = fileUploadEl.getUploadFileName();
			if (filename != null && filename.toLowerCase().endsWith(".docx")
					&& fileUploadEl.isUploadSuccess()) {
				String converted = convertDocxQuietly(fileUploadEl.getUploadFile());
				if (converted != null) {
					applySuggestedCounts(converted.length());
				}
			} else if (fileUploadEl.isUploadSuccess()) {
				applySuggestedCounts((int) fileUploadEl.getUploadFile().length());
			}
		} else if (source == contentEl) {
			applySuggestedCounts(contentEl.getValue() == null ? 0 : contentEl.getValue().length());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean ok = super.validateFormLogic(ureq);
		fileUploadEl.clearError();
		contentEl.clearError();
		if (MODE_FILE.equals(modeEl.getSelectedKey())) {
			if (!fileUploadEl.isUploadSuccess()) {
				fileUploadEl.setErrorKey("ai.questions.file.missing");
				ok = false;
			}
		} else if (!StringHelper.containsNonWhitespace(contentEl.getValue())) {
			contentEl.setErrorKey("ai.questions.content.empty");
			ok = false;
		}
		ok &= validateCount(aiMcCountEl);
		ok &= validateCount(aiEssayCountEl);
		// Require at least one question across the two legs.
		int total = readCount(aiMcCountEl) + readCount(aiEssayCountEl);
		if (total <= 0) {
			aiMcCountEl.setErrorKey("ai.questions.count.error.zero");
			ok = false;
		}
		return ok;
	}

	private boolean validateCount(IntegerElement el) {
		if (el == null) return true;
		el.clearError();
		String raw = el.getValue();
		if (!StringHelper.containsNonWhitespace(raw)) {
			el.setErrorKey("ai.questions.count.error.range");
			return false;
		}
		try {
			int v = Integer.parseInt(raw.trim());
			if (v < MIN_AI_COUNT || v > MAX_AI_COUNT) {
				el.setErrorKey("ai.questions.count.error.range");
				return false;
			}
		} catch (NumberFormatException e) {
			el.setErrorKey("ai.questions.count.error.range");
			return false;
		}
		return true;
	}

	private int readCount(IntegerElement el) {
		if (el == null) return 0;
		String raw = el.getValue();
		if (!StringHelper.containsNonWhitespace(raw)) {
			return 0;
		}
		try {
			return Integer.parseInt(raw.trim());
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String input = resolveInputText();
		if (!StringHelper.containsNonWhitespace(input)) {
			showError("ai.questions.content.empty");
			return;
		}
		int essayCount = aiEssayCountEl == null ? 0 : readCount(aiEssayCountEl);
		int mcCount = readCount(aiMcCountEl);
		GenerationRequest request = GenerationRequest.forPool(input,
				/* repositoryEntryKey */ null, getLocale(), getIdentity(),
				essayCount, mcCount, taxonomyLevelKey);
		try {
			essayGenerationService.submit(request);
		} catch (AiRateLimitExceededException rl) {
			logInfo("AI question generation throttled: " + rl.getMessage());
			showError("ai.questions.error.ratelimit");
			return;
		} catch (Exception e) {
			logError("Failed to submit AI question generation job", e);
			showError("ai.questions.error.submit");
			return;
		}
		// Job runs in the background; close the dialog and let the parent refresh
		// the list when the items eventually arrive.
		showInfo("ai.questions.submitted.background");
		fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_CREATED));
		fireEvent(ureq, Event.DONE_EVENT);
	}

	/** Read the source text — either from the uploaded file (with .docx
	 *  conversion when applicable) or the paste-mode textarea. */
	private String resolveInputText() {
		if (MODE_FILE.equals(modeEl.getSelectedKey()) && fileUploadEl.isUploadSuccess()) {
			File uploaded = fileUploadEl.getUploadFile();
			String filename = fileUploadEl.getUploadFileName();
			if (filename != null && filename.toLowerCase().endsWith(".docx")) {
				String converted = convertDocxQuietly(uploaded);
				return converted == null ? "" : converted;
			}
			try {
				return Files.readString(uploaded.toPath(), StandardCharsets.UTF_8);
			} catch (Exception e) {
				logError("Failed to read uploaded file", e);
				return "";
			}
		}
		return contentEl.getValue();
	}

	private String convertDocxQuietly(File docx) {
		try {
			DocxToMarkdownResult result = docxToMarkdownService.convert(docx);
			if (result.basePath() != null && tempFile == null) {
				tempFile = result.basePath();
			}
			return result.markdown();
		} catch (Exception e) {
			logWarn("DOCX conversion failed in AI question dialog", e);
			return null;
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		if (tempFile != null) {
			FileUtils.deleteDirsAndFiles(tempFile, true, true);
			tempFile = null;
		}
		super.doDispose();
	}

	// ------------------------------------------------------------ Heuristic

	/**
	 * Recompute the suggested {@code essay} / {@code mc} counts for the
	 * given input length and apply them to the form fields. Called every
	 * time the user changes the source so the defaults track the input.
	 */
	private void applySuggestedCounts(int length) {
		int previousEssay = suggestedEssay;
		int previousMc = suggestedMc;
		suggestedEssay = suggestedEssayCount(length);
		suggestedMc = suggestedMcCount(length);
		// Only override when the field is still showing the PREVIOUS suggestion
		// (or is empty / 0) — don't trample a value the user has just typed.
		applySuggestedValue(aiEssayCountEl, suggestedEssay, previousEssay);
		applySuggestedValue(aiMcCountEl, suggestedMc, previousMc);
	}

	private void applySuggestedValue(IntegerElement el, int suggested, int previousSuggestion) {
		if (el == null) return;
		String raw = el.getValue();
		if (!StringHelper.containsNonWhitespace(raw)) {
			el.setIntValue(suggested);
			return;
		}
		try {
			int current = Integer.parseInt(raw.trim());
			if (current == previousSuggestion || current == 0) {
				el.setIntValue(suggested);
			}
		} catch (NumberFormatException e) {
			el.setIntValue(suggested);
		}
	}

	/**
	 * Length-based default for essay questions. Buckets:
	 * <ul>
	 *   <li>&lt;200 → 1</li>
	 *   <li>200–500 → 2</li>
	 *   <li>500–1500 → 3</li>
	 *   <li>1500–4000 → 4</li>
	 *   <li>&gt;4000 → 5 (cap)</li>
	 * </ul>
	 * Returns {@code 0} when essay grading is not configured (the field is
	 * absent in that case anyway, but keeping it explicit avoids surprises
	 * when the heuristic is reused from tests).
	 */
	public static int suggestedEssayCount(int length) {
		if (length < 200) return 1;
		if (length < 500) return 2;
		if (length < 1500) return 3;
		if (length < 4000) return 4;
		return 5;
	}

	/**
	 * Length-based default for multiple-choice questions. Always 1 higher
	 * than the essay count and capped at 6:
	 * <ul>
	 *   <li>&lt;200 → 2</li>
	 *   <li>200–500 → 3</li>
	 *   <li>500–1500 → 4</li>
	 *   <li>1500–4000 → 5</li>
	 *   <li>&gt;4000 → 6 (cap)</li>
	 * </ul>
	 */
	public static int suggestedMcCount(int length) {
		if (length < 200) return 2;
		if (length < 500) return 3;
		if (length < 1500) return 4;
		if (length < 4000) return 5;
		return 6;
	}
}
