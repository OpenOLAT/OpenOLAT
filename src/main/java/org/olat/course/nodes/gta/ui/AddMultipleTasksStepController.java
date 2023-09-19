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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Aug 07, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AddMultipleTasksStepController extends StepFormBasicController {

	private static final Logger log = Tracing.createLoggerFor(AddMultipleTasksStepController.class);

	private final StepsRunContext runContext;
	private final File tasksFolder;
	@Autowired
	protected GTAManager gtaManager;
	private FileElement zipUploadEl;
	private List<String> uploadedFileNames;

	public AddMultipleTasksStepController(UserRequest ureq, WindowControl wControl, Form rootForm,
										  StepsRunContext runContext, CourseEnvironment courseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

		this.runContext = runContext;
		tasksFolder = gtaManager.getTasksDirectory(courseEnv, gtaNode);
		uploadedFileNames = new ArrayList<>();
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (zipUploadEl.isUploadSuccess() && !zipUploadEl.hasError()) {
			runContext.put("fileNames", uploadedFileNames);
			runContext.put("tasksFolder", tasksFolder);
			runContext.put("zipFile", zipUploadEl);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("wizard.step0.info");

		FormLayoutContainer uploadCont = uifactory.addDefaultFormLayout("def.upload.cont", null, formLayout);

		zipUploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "wizard.step0.upload", uploadCont);
		zipUploadEl.limitToMimeType(Collections.singleton("application/zip"), "wizard.step0.upload.wrongfiletype", null);
		zipUploadEl.setMandatory(true, "wizard.step0.upload.empty");
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if (zipUploadEl.getUploadFile() != null) {
			allOk = extractAssignmentMediaFileNames();
		}

		if (uploadedFileNames.isEmpty() && !zipUploadEl.hasError()) {
			zipUploadEl.setErrorKey("error.upload.empty");
		} else {
			List<String> fileNames = new ArrayList<>();
			for (String uploadedFileName : uploadedFileNames) {
				if (!FileUtils.validateFilename(uploadedFileName)) {
					zipUploadEl.setErrorKey("error.file.invalid");
					allOk = false;
				}

				// check if assignments already exist
				File target = new File(tasksFolder, uploadedFileName);
				if (target.exists()) {
					fileNames.add(uploadedFileName);
				}
				if (!fileNames.isEmpty()) {
					if (fileNames.size() > 1) {
						zipUploadEl.setErrorKey("error.files.exist", String.join(", ", fileNames));
					} else {
						zipUploadEl.setErrorKey("error.file.exists", fileNames.get(0));
					}
					allOk = false;
				}
			}
		}

		return allOk;
	}

	private boolean extractAssignmentMediaFileNames() {
		boolean allOk = true;

		uploadedFileNames.clear();
		try (ZipInputStream zis = new ZipInputStream(zipUploadEl.getUploadInputStream())) {
			ZipEntry zipEntry = zis.getNextEntry();

			while (zipEntry != null) {
				if (!zipEntry.isDirectory()
						&& !zipEntry.getName().startsWith("__MACOSX")
						&& !zipEntry.getName().contains(".DS_Store")) {
					String fileName;
					if (StringUtils.countMatches(zipEntry.getName(), "/") > 1) {
						String filePath = zipEntry.getName().substring(zipEntry.getName().indexOf("/") + 1);
						fileName = filePath.replace("/", "_").replace(" ", "_");
					} else {
						fileName = zipEntry.getName().replaceAll(".*/", "");
					}
					uploadedFileNames.add(fileName);
				}

				zis.closeEntry();
				zipEntry = zis.getNextEntry();
			}
		} catch (IOException e) {
			allOk = false;
			log.error("Error processing Zip-File as Stream: {}", e.getMessage());
			zipUploadEl.reset();
			zipUploadEl.setErrorKey("wizard.step0.upload.error");
		}

		return allOk;
	}
}
