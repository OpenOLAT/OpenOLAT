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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.UploadFileElementEvent;
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
import org.olat.modules.ceditor.manager.MarkdownImportResult;
import org.olat.modules.ceditor.manager.MarkdownImportService;
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

	private SingleSelection modeEl;
	private FileElement fileUploadEl;
	private TextAreaElement markdownTextEl;

	private File tempUnzipDir;

	@Autowired
	private ContentEditorModule contentEditorModule;
	@Autowired
	private MarkdownImportService markdownImportService;
	@Autowired
	private DocxToMarkdownService docxToMarkdownService;

	public MarkdownImportController(UserRequest ureq, WindowControl wControl, Page page, OLATResourceable aiOres,
			String subIdent, String targetContainerId, int targetColumn,
			String referenceElementId, PageElementTarget target) {
		super(ureq, wControl);
		this.page = page;
		this.aiOres = aiOres;
		this.subIdent = subIdent;
		this.targetContainerId = targetContainerId;
		this.targetColumn = targetColumn;
		this.referenceElementId = referenceElementId;
		this.target = target;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Mode selection: file upload or text paste
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
		markdownTextEl.setVisible(false);

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

		return allOk;
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
		fireEvent(ureq, new MarkdownImportDoneEvent(result.warnings()));
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
