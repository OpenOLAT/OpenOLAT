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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.manager.MarkdownImportResult;
import org.olat.modules.ceditor.manager.MarkdownImportService;
import org.olat.modules.ceditor.ui.event.MarkdownImportDoneEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller for importing markdown content into the content editor.
 * Provides file upload and text paste options. Supports direct .md files
 * and .zip archives containing a single .md file with relative image assets.
 *
 * Initial date: 2026-03-11<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownImportController extends FormBasicController {

	private static final Set<String> UPLOAD_MIME_TYPES = Set.of(
		"text/markdown", "text/x-markdown", "text/plain", "application/octet-stream",
		"application/zip", "application/x-zip-compressed"
	);

	/** Maximum upload size in KB (50 MB). Shared with MarkdownPagePartVisitor for download limits. */
	public static final int MAX_UPLOAD_SIZE_KB = 51200;

	private static final String MODE_FILE = "file";
	private static final String MODE_TEXT = "text";

	private final Page page;

	private SingleSelection modeEl;
	private FileElement fileUploadEl;
	private TextAreaElement markdownTextEl;

	private File tempUnzipDir;

	@Autowired
	private MarkdownImportService markdownImportService;

	public MarkdownImportController(UserRequest ureq, WindowControl wControl, Page page) {
		super(ureq, wControl);
		this.page = page;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] modeKeys = { MODE_FILE, MODE_TEXT };
		String[] modeValues = { translate("import.markdown.mode.file"), translate("import.markdown.mode.text") };
		modeEl = uifactory.addRadiosHorizontal("import.mode", "import.markdown.mode", formLayout, modeKeys, modeValues);
		modeEl.select(MODE_FILE, true);
		modeEl.addActionListener(FormEvent.ONCHANGE);

		fileUploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "import.markdown.file", formLayout);
		fileUploadEl.setMandatory(true);
		fileUploadEl.limitToMimeType(UPLOAD_MIME_TYPES, "import.markdown.file.error", null);
		fileUploadEl.setMaxUploadSizeKB(MAX_UPLOAD_SIZE_KB, "import.markdown.file.toolarge", null);

		markdownTextEl = uifactory.addTextAreaElement("import.markdown.text", "import.markdown.text", -1, 15, 80, false, false, "", formLayout);
		markdownTextEl.setVisible(false);

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("import.markdown.submit", buttonLayout);
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
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if (MODE_FILE.equals(modeEl.getSelectedKey())) {
			fileUploadEl.clearError();
			if (!fileUploadEl.isUploadSuccess()) {
				fileUploadEl.setErrorKey("import.markdown.file.missing");
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

		if (MODE_FILE.equals(modeEl.getSelectedKey()) && fileUploadEl.isUploadSuccess()) {
			File uploaded = fileUploadEl.getUploadFile();
			String filename = fileUploadEl.getUploadFileName();

			if (filename != null && filename.toLowerCase().endsWith(".zip")) {
				File mdFile = extractMarkdownFromZip(uploaded);
				if (mdFile == null) {
					return;
				}
				uploaded = mdFile;
				basePath = mdFile.getParentFile();
			} else {
				basePath = uploaded.getParentFile();
			}

			try {
				markdown = Files.readString(uploaded.toPath(), StandardCharsets.UTF_8);
			} catch (Exception e) {
				logError("Failed to read uploaded markdown file", e);
				showError("import.markdown.read.error");
				return;
			}
		} else {
			markdown = markdownTextEl.getValue();
		}

		MarkdownImportResult result = markdownImportService.convertAndPersist(markdown, page, getIdentity(), basePath, getLocale());
		fireEvent(ureq, new MarkdownImportDoneEvent(result.warnings()));
	}

	/**
	 * Extract a single .md file from a ZIP archive. The ZIP is extracted into a
	 * temporary directory. If no .md file or more than one .md file is found,
	 * an error is shown and null is returned.
	 */
	private File extractMarkdownFromZip(File zipFile) {
		try {
			tempUnzipDir = Files.createTempDirectory("md_import_").toFile();
			// Unzip into a subdirectory to prevent overwriting if ZIP has no root folder
			File extractDir = new File(tempUnzipDir, "content");
			extractDir.mkdirs();

			if (!ZipUtil.unzip(zipFile, extractDir)) {
				showError("import.markdown.zip.error");
				return null;
			}

			// Find all .md files in the extracted content
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
