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
package org.olat.course.nodes.gta.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FileElementInfos;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.model.TaskDefinition;

/**
 * Initial date: Aug 07, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AddMetadataStepController extends StepFormBasicController {

	private static final Logger log = Tracing.createLoggerFor(AddMetadataStepController.class);

	private static final String METADATA_UNIVERSAL_DESC = "wizard.step1.univ.desc";
	private static final String METADATA_INDIVIDUAL_DESC = "wizard.step1.individual.desc";

	private SingleSelection descriptionSelectionEl;
	private SpacerElement spacerEl;
	private TextElement universalDescTextEl;
	private List<TextElement> titleElList;
	private List<TextElement> descElList;

	private final File tasksFolder;
	private final FileElement filesUploadEl;
	private final StepsRunContext runContext;
	private final List<TaskDefinition> taskList;
	private final List<String> uploadedFileNames;

	public AddMetadataStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		taskList = new ArrayList<>();
		this.runContext = runContext;
		filesUploadEl = (FileElement) runContext.get("uploadedFiles");
		uploadedFileNames = (List<String>) runContext.get("fileNames");
		tasksFolder = (File) runContext.get("tasksFolder");

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("wizard.step1.info");

		FormLayoutContainer metadataCont = uifactory.addDefaultFormLayout("def.metadata.cont", null, formLayout);

		SelectionValues descSV = new SelectionValues();
		descSV.add(entry(METADATA_UNIVERSAL_DESC, translate(METADATA_UNIVERSAL_DESC)));
		descSV.add(entry(METADATA_INDIVIDUAL_DESC, translate(METADATA_INDIVIDUAL_DESC)));
		descriptionSelectionEl = uifactory.addRadiosVertical("task.description", metadataCont, descSV.keys(), descSV.values());
		descriptionSelectionEl.select(METADATA_UNIVERSAL_DESC, true);
		descriptionSelectionEl.addActionListener(FormEvent.ONCHANGE);

		universalDescTextEl = uifactory.addTextAreaElement("metadata.desc", null,
				2048, 3, -1, true, false, "", metadataCont);
		universalDescTextEl.setVisible(false);

		spacerEl = uifactory.addSpacerElement("spacerElement", metadataCont, true);

		titleElList = new ArrayList<>(uploadedFileNames.size());
		descElList = new ArrayList<>(uploadedFileNames.size());
		for (String taskFileName : uploadedFileNames) {
			titleElList.add(uifactory.addTextElement(taskFileName, null, 256, taskFileName, metadataCont));
			titleElList.get(uploadedFileNames.indexOf(taskFileName)).setLabel("wizard.step1.indi.file.title", new String[]{taskFileName});
			titleElList.get(uploadedFileNames.indexOf(taskFileName)).setMandatory(true);
			descElList.add(uifactory.addTextAreaElement(taskFileName + "/desc", "task.description",
					2048, 3, -1, true, false, "", metadataCont));
			descElList.get(uploadedFileNames.indexOf(taskFileName)).setVisible(false);
		}

		updateVisibility();
	}

	private void updateVisibility() {
		universalDescTextEl.setVisible(descriptionSelectionEl.isKeySelected(METADATA_UNIVERSAL_DESC));
		spacerEl.setVisible(descriptionSelectionEl.isKeySelected(METADATA_UNIVERSAL_DESC));
		descElList.forEach(d -> d.setVisible(descriptionSelectionEl.isKeySelected(METADATA_INDIVIDUAL_DESC)));
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk;
		for (TextElement titleEl : titleElList) {
			TaskDefinition task = new TaskDefinition();
			String title = StringHelper.xssScan(titleEl.getValue());
			task.setTitle(title);
			task.setFilename(titleEl.getName());

			String description;
			if (descriptionSelectionEl.isKeySelected(METADATA_INDIVIDUAL_DESC)) {
				description = StringHelper.xssScan(descElList.get(titleElList.indexOf(titleEl)).getValue());
			} else {
				description = StringHelper.xssScan(universalDescTextEl.getValue());
			}
			task.setDescription(description);
			taskList.add(task);
		}

		if (!filesUploadEl.isMultiFileUpload()
				&& filesUploadEl.getUploadMimeType().equals("application/zip")) {
			// first 'extraction' in AddMultipleTasksStepController for getting the values (title) for this step
			// second extraction after completion and for adding new tasks/files to OO
			allOk = extractAssignmentMediaFiles();
		} else {
			allOk = uploadAssignmentMediaFiles();
		}

		return allOk;
	}

	private boolean uploadAssignmentMediaFiles() {
		boolean allOk = true;

		try {
			for (FileElementInfos fileElementInfos : filesUploadEl.getUploadFilesInfos()) {
				// adding files to system
				File target = new File(tasksFolder, fileElementInfos.fileName());
				Files.copy(fileElementInfos.file().toPath(), target.toPath());
			}
		} catch (IOException e) {
			allOk = false;
			log.error("Error processing files: {}", e.getMessage());
		}

		return allOk;
	}

	private boolean extractAssignmentMediaFiles() {
		boolean allOk = true;

		try (ZipInputStream zis = new ZipInputStream(filesUploadEl.getUploadInputStream())) {
			ZipEntry zipEntry = zis.getNextEntry();

			while (zipEntry != null) {
				if (!zipEntry.isDirectory()
						&& !zipEntry.getName().startsWith("__MACOSX")
						&& !zipEntry.getName().contains(".DS_Store")) {
					String fileName;
					// for retrieving filename, check if file is inside of > 1 folders
					if (StringUtils.countMatches(zipEntry.getName(), "/") > 1) {
						String filePath = zipEntry.getName().substring(zipEntry.getName().indexOf("/") + 1);
						fileName = filePath.replace("/", "_").replace(" ", "_");
					} else {
						fileName = zipEntry.getName().replaceAll(".*/", "");
					}
					// adding files to system
					File target = new File(tasksFolder, fileName);
					Files.copy(zis, target.toPath());
				}

				zis.closeEntry();
				zipEntry = zis.getNextEntry();
			}
		} catch (IOException e) {
			allOk = false;
			log.error("Error processing Zip-File as Stream: {}", e.getMessage());
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		runContext.put("taskList", taskList);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == descriptionSelectionEl) {
			updateVisibility();
		}
	}
}
