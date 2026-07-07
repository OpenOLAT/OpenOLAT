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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.ai.AiEssayGenerationService;
import org.olat.core.commons.services.ai.essay.AiBloomLevel;
import org.olat.core.commons.services.ai.essay.EssayGenerationService;
import org.olat.core.commons.services.ai.essay.EssayGenerationService.GenerationRequest;
import org.olat.core.commons.services.ai.ui.AiAdminController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.UploadFileElementEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.util.Util;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.docxToMarkdown.DocxToMarkdownResult;
import org.olat.core.util.docxToMarkdown.DocxToMarkdownService;
import org.olat.modules.ceditor.ContentEditorModule;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.manager.EssayGenerationQuizPartSinkImpl;
import org.olat.modules.ceditor.manager.MarkdownImportResult;
import org.olat.modules.ceditor.manager.MarkdownImportService;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.ceditor.model.jpa.ContainerPart;
import org.olat.modules.ceditor.model.jpa.QuizPart;
import org.olat.modules.ceditor.ui.event.MarkdownImportDoneEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller for importing content into the content editor.
 * Provides file upload and text paste options. Supports Markdown files (.md),
 * ZIP archives with markdown + images, and Word documents (.docx).
 *
 * Initial date: 2026-03-11<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownImportController extends FormBasicController {

	private static final Set<String> UPLOAD_MIME_TYPES = Set.of(
		"text/markdown", "text/x-markdown", "text/plain", "application/octet-stream",
		"application/zip", "application/x-zip-compressed",
		"application/vnd.openxmlformats-officedocument.wordprocessingml.document"
	);

	/** Default upload size limit in KB (50 MB). Used as fallback for download limits in MarkdownPagePartVisitor. */
	public static final int MAX_UPLOAD_SIZE_KB = 51200;

	private static final String MODE_FILE = "file";
	private static final String MODE_TEXT = "text";

	private final Page page;
	private final OLATResourceable aiOres;
	private final String subIdent;
	private final String targetContainerId;
	private final int targetColumn;
	private final String referenceElementId;
	private final PageElementTarget target;
	/** When false, the AI question generation form section (toggle + count fields)
	 *  is not added to the form at all. Used by contexts where authors are not
	 *  allowed to create quiz elements (e.g. e-portfolio: students are authors). */
	private final boolean allowAiQuestionGeneration;

	private SingleSelection modeEl;
	private FileElement fileUploadEl;
	private TextAreaElement markdownTextEl;
	private FormToggle aiGenerateEl;
	private IntegerElement aiMcCountEl;
	private IntegerElement aiEssayCountEl;
	private MultipleSelectionElement aiBloomEl;
	private SingleSelection aiDifficultyEl;
	private TextAreaElement aiObjectivesEl;

	private static final int DEFAULT_AI_MC_COUNT = 2;
	private static final int DEFAULT_AI_ESSAY_COUNT = 2;
	private static final int MIN_AI_COUNT = 0;
	private static final int MAX_AI_COUNT = 5;

	private File tempUnzipDir;

	@Autowired
	private ContentEditorModule contentEditorModule;
	@Autowired
	private MarkdownImportService markdownImportService;
	@Autowired
	private DocxToMarkdownService docxToMarkdownService;
	@Autowired
	private PageService pageService;
	@Autowired
	private EssayGenerationService essayGenerationService;
	@Autowired
	private AiEssayGenerationService aiEssayGenerationService;

	public MarkdownImportController(UserRequest ureq, WindowControl wControl, Page page, OLATResourceable aiOres,
			String subIdent, String targetContainerId, int targetColumn,
			String referenceElementId, PageElementTarget target,
			boolean allowAiQuestionGeneration) {
		super(ureq, wControl, Util.createPackageTranslator(AiAdminController.class, ureq.getLocale()));
		this.page = page;
		this.aiOres = aiOres;
		this.subIdent = subIdent;
		this.targetContainerId = targetContainerId;
		this.targetColumn = targetColumn;
		this.referenceElementId = referenceElementId;
		this.target = target;
		this.allowAiQuestionGeneration = allowAiQuestionGeneration;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_import_markdown_form");
		
		//Mode selection: file upload or text paste
		String[] modeKeys = { MODE_FILE, MODE_TEXT };
		String[] modeTitles = { translate("import.mode.file"), translate("import.mode.text") };
		String[] modeDescriptions = { translate("import.mode.file.desc"), translate("import.mode.text.desc") };
		String[] modeIcons = { "o_icon o_icon_upload", "o_icon o_icon_edit" };
		modeEl = uifactory.addCardSingleSelectHorizontal("import.mode", null, formLayout,
				modeKeys, modeTitles, modeDescriptions, modeIcons);
		modeEl.select(MODE_FILE, true);
		modeEl.addActionListener(FormEvent.ONCHANGE);

		// File upload — use the larger of the two limits to allow both file types
		int maxUploadKB = Math.max(contentEditorModule.getImportLimitMdKB(), contentEditorModule.getImportLimitDocxKB());
		fileUploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "import.file", formLayout);
		fileUploadEl.setMandatory(true);
		fileUploadEl.limitToMimeType(UPLOAD_MIME_TYPES, "import.file.error", null);
		fileUploadEl.setMaxUploadSizeKB(maxUploadKB, "import.file.toolarge", null);
		fileUploadEl.addActionListener(FormEvent.ONCHANGE);

		// Text input
		markdownTextEl = uifactory.addTextAreaElement("import.text", "import.text", -1, 15, 80, false, false, "", formLayout);
		markdownTextEl.setElementCssClass("o_sel_import_text");

		markdownTextEl.setVisible(false);

		// Optional: generate AI questions from imported content (MVP toggle).
		// Toggle + two count fields (MC + essay). Fields are visible only when
		// the toggle is on; counts default to 2 and are validated on submit.
		if (isAiQuestionGenerationAvailable()) {
			aiGenerateEl = uifactory.addToggleButton("import.ai.generate", "import.ai.generate.label",
					translate("on"), translate("off"), formLayout);
			aiGenerateEl.setHelpText(translate("import.ai.generate.help"));
			aiGenerateEl.addActionListener(FormEvent.ONCHANGE);
			// MVP default: AI question generation is opted-in by default so the
			// MC + essay count fields render visible from the start.
			aiGenerateEl.toggleOn();

			aiMcCountEl = uifactory.addIntegerElement("import.ai.generate.mc.count",
					"import.ai.generate.mc.count", DEFAULT_AI_MC_COUNT, formLayout);
			aiMcCountEl.setDisplaySize(3);

			aiEssayCountEl = uifactory.addIntegerElement("import.ai.generate.essay.count",
					"import.ai.generate.essay.count", DEFAULT_AI_ESSAY_COUNT, formLayout);
			aiEssayCountEl.setDisplaySize(3);

			// Bloom levels — multi-select checkboxes, default UNDERSTAND + APPLY.
			// All AI labels resolve via the AI bundle, set as fallback translator on the controller.
			SelectionValues bloomKV = new SelectionValues();
			for (AiBloomLevel level : AiBloomLevel.values()) {
				bloomKV.add(SelectionValues.entry(level.name(), translate("bloom." + level.name().toLowerCase())));
			}
			aiBloomEl = uifactory.addCheckboxesHorizontal("ai.bloom", "ai.bloom.label",
					formLayout, bloomKV.keys(), bloomKV.values());
			aiBloomEl.setHelpTextKey("ai.bloom.help", null);
			aiBloomEl.select(AiBloomLevel.UNDERSTAND.name(), true);
			aiBloomEl.select(AiBloomLevel.APPLY.name(), true);

			// Target difficulty — single-select dropdown, default unspecified
			String[] difficultyKeys = { "unspecified", "1", "2", "3", "4", "5" };
			String[] difficultyValues = {
				translate("ai.difficulty.unspecified"),
				translate("difficulty.1"),
				translate("difficulty.2"),
				translate("difficulty.3"),
				translate("difficulty.4"),
				translate("difficulty.5")
			};
			aiDifficultyEl = uifactory.addDropdownSingleselect("ai.difficulty", "ai.difficulty.label",
					formLayout, difficultyKeys, difficultyValues);
			aiDifficultyEl.setHelpTextKey("ai.difficulty.help", null);
			// Page generation defaults to quiz mode (difficulty 1): easy, short-answer questions.
			aiDifficultyEl.select("1", true);

			// Learning objectives — optional textarea
			aiObjectivesEl = uifactory.addTextAreaElement("ai.objectives", "ai.objectives.label",
					-1, 6, 80, false, false, "", formLayout);
			aiObjectivesEl.setPlaceholderKey("ai.objectives.placeholder", null);
			aiObjectivesEl.setHelpTextKey("ai.objectives.help", null);

			updateAiCountsVisibility();
		}

		// Buttons
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("import.submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == modeEl) {
			boolean isFileMode = MODE_FILE.equals(modeEl.getSelectedKey());
			fileUploadEl.setVisible(isFileMode);
			fileUploadEl.setMandatory(isFileMode);
			markdownTextEl.setVisible(!isFileMode);
			flc.setDirty(true);
		}
		if (source == aiGenerateEl) {
			updateAiCountsVisibility();
			flc.setDirty(true);
		}
		if (event instanceof UploadFileElementEvent) {
			// Show beta warning when a .docx file is uploaded
			String filename = fileUploadEl.getUploadFileName();
			boolean isDocx = filename != null && filename.toLowerCase().endsWith(".docx");
			if (isDocx) {
				fileUploadEl.setWarningKey("import.docx.beta.warning");
			} else {
				fileUploadEl.clearWarning();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if (MODE_FILE.equals(modeEl.getSelectedKey())) {
			fileUploadEl.clearError();
			if (!fileUploadEl.isUploadSuccess()) {
				fileUploadEl.setErrorKey("import.file.missing");
				allOk = false;
			}
		} else {
			markdownTextEl.clearError();
			if (!StringHelper.containsNonWhitespace(markdownTextEl.getValue())) {
				markdownTextEl.setErrorKey("import.markdown.empty");
				allOk = false;
			}
		}

		// Range-validate AI question counts only when the toggle is on —
		// otherwise the fields are hidden and their values don't matter.
		if (aiGenerateEl != null && aiGenerateEl.isOn()) {
			if (aiMcCountEl != null) {
				aiMcCountEl.clearError();
				if (!isInAiCountRange(aiMcCountEl)) {
					aiMcCountEl.setErrorKey("import.ai.generate.count.error.range");
					allOk = false;
				}
			}
			if (aiEssayCountEl != null) {
				aiEssayCountEl.clearError();
				if (!isInAiCountRange(aiEssayCountEl)) {
					aiEssayCountEl.setErrorKey("import.ai.generate.count.error.range");
					allOk = false;
				}
			}
			// Require at least one question across the two legs (same rule
			// as the question pool import dialog).
			if (allOk && aiMcCountEl != null && aiEssayCountEl != null
					&& totalAiQuestionCount(aiMcCountEl.getValue(), aiEssayCountEl.getValue()) <= 0) {
				aiMcCountEl.setErrorKey("import.ai.generate.count.error.zero");
				allOk = false;
			}
		}

		return allOk;
	}

	/** True if the element holds an integer in [MIN_AI_COUNT, MAX_AI_COUNT]. */
	private boolean isInAiCountRange(IntegerElement el) {
		String raw = el.getValue();
		if (!StringHelper.containsNonWhitespace(raw)) {
			return false;
		}
		try {
			int v = Integer.parseInt(raw.trim());
			return v >= MIN_AI_COUNT && v <= MAX_AI_COUNT;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/** Sum of the two requested AI question counts; blank or unparsable legs count as zero. */
	static int totalAiQuestionCount(String mcRaw, String essayRaw) {
		return parseCountOrZero(mcRaw) + parseCountOrZero(essayRaw);
	}

	private static int parseCountOrZero(String raw) {
		if (!StringHelper.containsNonWhitespace(raw)) {
			return 0;
		}
		try {
			return Integer.parseInt(raw.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/** Show AI controls only while the AI toggle is on. */
	private void updateAiCountsVisibility() {
		if (aiGenerateEl == null) return;
		boolean on = aiGenerateEl.isOn();
		if (aiMcCountEl != null) {
			aiMcCountEl.setVisible(on);
		}
		if (aiEssayCountEl != null) {
			aiEssayCountEl.setVisible(on);
		}
		if (aiBloomEl != null) {
			aiBloomEl.setVisible(on);
		}
		if (aiDifficultyEl != null) {
			aiDifficultyEl.setVisible(on);
		}
		if (aiObjectivesEl != null) {
			aiObjectivesEl.setVisible(on);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String markdown;
		File basePath = null;
		List<String> extraWarnings = new ArrayList<>();

		if (MODE_FILE.equals(modeEl.getSelectedKey()) && fileUploadEl.isUploadSuccess()) {
			File uploaded = fileUploadEl.getUploadFile();
			String filename = fileUploadEl.getUploadFileName();

			if (filename != null && filename.toLowerCase().endsWith(".docx")) {
				// DOCX → Markdown conversion (images extracted to temp dir)
				DocxToMarkdownResult docxResult = docxToMarkdownService.convert(uploaded);
				if (docxResult.hasMessages()) {
					String messagesText = docxResult.renderMessagesAsText(getTranslator());
					if (!messagesText.isEmpty()) {
						extraWarnings.add(messagesText);
					}
				}
				markdown = docxResult.markdown();
				basePath = docxResult.basePath();
				// Track temp dir for cleanup
				if (basePath != null && tempUnzipDir == null) {
					tempUnzipDir = basePath;
				}
			} else if (filename != null && filename.toLowerCase().endsWith(".zip")) {
				File mdFile = extractMarkdownFromZip(uploaded);
				if (mdFile == null) {
					return;
				}
				uploaded = mdFile;
				basePath = mdFile.getParentFile();
				try {
					markdown = Files.readString(uploaded.toPath(), StandardCharsets.UTF_8);
				} catch (Exception e) {
					logError("Failed to read uploaded markdown file", e);
					showError("import.markdown.read.error");
					return;
				}
			} else {
				basePath = uploaded.getParentFile();
				try {
					markdown = Files.readString(uploaded.toPath(), StandardCharsets.UTF_8);
				} catch (Exception e) {
					logError("Failed to read uploaded markdown file", e);
					showError("import.markdown.read.error");
					return;
				}
			}
		} else {
			markdown = markdownTextEl.getValue();
		}

		MarkdownImportResult result = markdownImportService.convertAndPersist(markdown, page, getIdentity(), aiOres,
				subIdent, basePath, getLocale(), targetContainerId, targetColumn, referenceElementId, target);

		// Optional post-step: if the author asked for AI questions, append a
		// placeholder QuizPart inside the same ContainerPart that wraps the
		// imported parts (so import + AI questions render as one logical block)
		// and submit a generation job. The completion hook
		// (EssayGenerationQuizPartSinkImpl) attaches the accepted drafts as
		// QTI essay items asynchronously.
		if (isAiGenerationRequested() && isAiQuestionGenerationAvailable()) {
			try {
				submitAiQuestionGeneration(markdown, result.container());
			} catch (Exception e) {
				logError("Failed to submit AI essay generation job for page " + page.getKey(), e);
				// Fall through: import itself succeeded.
			}
		}

		if (result.aiMetadataJobs() > 0) {
			// Image metadata is enriched by asynchronous background tasks —
			// tell the user why the imported images have no AI metadata yet.
			showInfo("import.ai.metadata.background");
		}

		fireEvent(ureq, new MarkdownImportDoneEvent(result.warnings()));
	}

	private boolean isAiGenerationRequested() {
		return aiGenerateEl != null && aiGenerateEl.isOn();
	}

	/**
	 * AI question generation is only offered if (1) the caller allows it,
	 * (2) the provider is configured, and (3) we have a resolvable
	 * RepositoryEntry key (required so the jobs + grading rows can be linked
	 * back). The caller-allowed gate is what hides the section in contexts
	 * where authors must not create quiz elements (e.g. the e-portfolio).
	 */
	private boolean isAiQuestionGenerationAvailable() {
		if (!allowAiQuestionGeneration) return false;
		if (aiOres == null || aiOres.getResourceableId() == null) return false;
		try {
			return aiEssayGenerationService != null && aiEssayGenerationService.isEnabled();
		} catch (Exception e) {
			return false;
		}
	}

	private void submitAiQuestionGeneration(String markdown, ContainerPart importContainer) {
		if (!StringHelper.containsNonWhitespace(markdown)) return;

		// Append the placeholder QuizPart inside the same container the import
		// service used for the imported parts (so the placeholder renders as
		// the last element of that block). If no container is available (e.g.
		// the markdown produced no parts), fall back to a page-level append.
		QuizPart placeholder = new QuizPart();
		QuizSettings settings = placeholder.getSettings();
		settings.setTitle(EssayGenerationQuizPartSinkImpl.GENERATING_TITLE_MARKER + " "
				+ translate("import.ai.generate.placeholder.title"));
		settings.setDescription(translate("import.ai.generate.placeholder.description"));
		placeholder.setSettings(settings);
		if (importContainer != null) {
			placeholder = markdownImportService.appendNewPartToContainer(page, importContainer, placeholder);
		} else {
			placeholder = (QuizPart) pageService.appendNewPagePart(page, placeholder);
		}

		Long repoEntryKey = aiOres.getResourceableId();
		int essayCount = aiEssayCountEl != null ? aiEssayCountEl.getIntValue() : DEFAULT_AI_ESSAY_COUNT;
		int mcCount = aiMcCountEl != null ? aiMcCountEl.getIntValue() : DEFAULT_AI_MC_COUNT;

		// Parse Bloom levels
		List<AiBloomLevel> bloomLevels = aiBloomEl == null ? List.of(AiBloomLevel.UNDERSTAND, AiBloomLevel.APPLY)
				: aiBloomEl.getSelectedKeys().stream()
						.map(AiBloomLevel::valueOf)
						.toList();

		// Parse target difficulty (null when unspecified)
		Integer targetDifficulty = null;
		if (aiDifficultyEl != null && aiDifficultyEl.isOneSelected()
				&& !"unspecified".equals(aiDifficultyEl.getSelectedKey())) {
			targetDifficulty = Integer.parseInt(aiDifficultyEl.getSelectedKey());
		}

		// Parse learning objectives (one per line, trim, drop empties)
		List<String> learningObjectives = List.of();
		if (aiObjectivesEl != null && StringHelper.containsNonWhitespace(aiObjectivesEl.getValue())) {
			learningObjectives = java.util.Arrays.stream(aiObjectivesEl.getValue().split("\n"))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.toList();
		}

		GenerationRequest request = GenerationRequest.forQuizPart(
				markdown, repoEntryKey, getLocale(), getIdentity(),
				page.getKey(), placeholder.getKey(), essayCount, mcCount,
				bloomLevels, targetDifficulty, learningObjectives);
		essayGenerationService.submit(request);
	}

	/**
	 * Extract a single .md file from a ZIP archive.
	 */
	private File extractMarkdownFromZip(File zipFile) {
		try {
			tempUnzipDir = Files.createTempDirectory("md_import_").toFile();
			File extractDir = new File(tempUnzipDir, "content");
			extractDir.mkdirs();

			if (!ZipUtil.unzip(zipFile, extractDir)) {
				showError("import.markdown.zip.error");
				return null;
			}

			List<Path> mdFiles = new ArrayList<>();
			Files.walkFileTree(extractDir.toPath(), new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					String dirName = dir.getFileName().toString();
					if (dirName.startsWith(".") || "__MACOSX".equals(dirName)) {
						return FileVisitResult.SKIP_SUBTREE;
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					String name = file.getFileName().toString();
					if (name.startsWith(".")) {
						return FileVisitResult.CONTINUE;
					}
					String nameLower = name.toLowerCase();
					if (nameLower.endsWith(".md") || nameLower.endsWith(".markdown")) {
						mdFiles.add(file);
					}
					return FileVisitResult.CONTINUE;
				}
			});

			if (mdFiles.isEmpty()) {
				showError("import.markdown.zip.nomd");
				return null;
			}
			if (mdFiles.size() > 1) {
				showError("import.markdown.zip.multiplemd");
				return null;
			}

			return mdFiles.get(0).toFile();
		} catch (IOException e) {
			logError("Failed to extract ZIP file", e);
			showError("import.markdown.zip.error");
			return null;
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		if (tempUnzipDir != null) {
			FileUtils.deleteDirsAndFiles(tempUnzipDir, true, true);
			tempUnzipDir = null;
		}
		super.doDispose();
	}
}
