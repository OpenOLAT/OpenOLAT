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
package org.olat.course.assessment.bulk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.assessment.model.BulkAssessmentDatas;
import org.olat.course.assessment.model.BulkAssessmentRow;
import org.olat.course.assessment.model.BulkAssessmentSettings;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 *
 * Initial date: 18.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DataStepForm extends StepFormBasicController {

	private static final String[] keys = new String[] { "tab", "comma" };
	private static final String[] statusKeys = new String[] { AssessmentEntryStatus.done.name(), AssessmentEntryStatus.inReview.name(), "not" };
	private static final String[] visibilityKeys = new String[] { "visible", "notvisible", "notchanged" };
	private static final String[] submissionKeys = new String[] { "accept", "notchanged" };

	private TextElement dataEl;
	private FileElement returnFileEl;
	private SingleSelection delimiter;
	private SingleSelection statusEl;
	private SingleSelection visibilityEl;
	private SingleSelection acceptSubmissionEl;

	private VFSLeaf targetArchive;
	private BulkAssessmentDatas savedDatas;
	private final CourseNode courseNode;
	private final boolean canEditUserVisibility;
	private VFSContainer bulkAssessmentTmpDir;

	public DataStepForm(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, boolean canEditUserVisibility, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

		this.canEditUserVisibility = canEditUserVisibility;
		courseNode = (CourseNode)getFromRunContext("courseNode");

		initForm(ureq);
	}

	public DataStepForm(UserRequest ureq, WindowControl wControl, CourseNode courseNode, BulkAssessmentDatas savedDatas,
			StepsRunContext runContext, boolean canEditUserVisibility, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

		this.savedDatas = savedDatas;
		this.courseNode = courseNode;
		this.canEditUserVisibility = canEditUserVisibility;
		addToRunContext("courseNode", courseNode);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_bulk_assessment_data");
		
		// hide data input field in case the element does not have any score, passed or comment field enabled
		BulkAssessmentSettings settings = new BulkAssessmentSettings(courseNode);
		boolean onlyReturnFiles = (!settings.isHasScore() && !settings.isHasPassed() && !settings.isHasUserComment());

		setFormTitle("data.title");
		if (!onlyReturnFiles) {
			setFormDescription("data.description");
		}
		setFormContextHelp("... create a bulk assessment for submission tasks");

		String dataVal = "";
		if(savedDatas != null && StringHelper.containsNonWhitespace(savedDatas.getDataBackupFile())) {
			VFSLeaf file = VFSManager.olatRootLeaf(savedDatas.getDataBackupFile());
			try(InputStream in = file.getInputStream()) {
				dataVal = IOUtils.toString(in, StandardCharsets.UTF_8);
			} catch (IOException e) {
				logError("", e);
			}
		}

		dataEl = uifactory.addTextAreaElement("data", "data", -1, 6, 60, true, false, dataVal, formLayout);
		dataEl.showLabel(false);

		String[] values = new String[] {translate("form.step3.delimiter.tab"),translate("form.step3.delimiter.comma")};
		delimiter = uifactory.addRadiosVertical("delimiter", "form.step3.delimiter", formLayout, keys, values);
		// preset delimiter type to first appearance of either tab or comma when data is available, default to tab for no data
		int firstComma = dataVal.indexOf(',');
		int firstTab = dataVal.indexOf('\t');
		if (firstComma > -1 && (firstTab == -1 || firstTab > firstComma )) {
			delimiter.select("comma", true);
		} else {
			delimiter.select("tab", true);
		}
		
		String[] statusValues = new String[] {
				translate("form.step3.status.assessed"), translate("form.step3.status.review"),
				translate("form.step3.status.dont.change")
		};
		statusEl = uifactory.addRadiosVertical("form.step3.status", "form.step3.status", formLayout, statusKeys, statusValues);
		statusEl.select(statusKeys[statusKeys.length - 1], true);
		
		if (canEditUserVisibility) {
			String[] visibilityValues = new String[] {
					translate("form.step3.visibility.visible"), translate("form.step3.visibility.notvisible"),
					translate("form.step3.visibility.dont.change")
			};
			visibilityEl = uifactory.addRadiosVertical("form.step3.visibility", "form.step3.visibility", formLayout, visibilityKeys, visibilityValues);
			visibilityEl.select(visibilityKeys[visibilityKeys.length - 1], true);
		}
		
		if(courseNode instanceof GTACourseNode) {
			String[] submissionValues = new String[] {
				translate("form.step3.submission.accept"), translate("form.step3.submission.dont.change")
			};
			acceptSubmissionEl = uifactory.addRadiosVertical("form.step3.submission", "form.step3.submission", formLayout, submissionKeys, submissionValues);
			acceptSubmissionEl.select(submissionKeys[submissionKeys.length - 1], true);
			acceptSubmissionEl.setHelpTextKey("form.step3.submission.help", null);
		}

		// hide data input field in case the element does not have any score, passed or comment field enabled
		if (onlyReturnFiles) {
			dataEl.setVisible(false);
			delimiter.setVisible(false);
		}

		// return files only when configured
		if(settings.isHasReturnFiles()) {
			returnFileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "returnfiles", "return.files", formLayout);
			Set<String> mimes = new HashSet<>();
			mimes.add(WebappHelper.getMimeType("file.zip"));
			returnFileEl.limitToMimeType(mimes, "return.mime", null);
			if(savedDatas != null && StringHelper.containsNonWhitespace(savedDatas.getReturnFiles())) {
				targetArchive = VFSManager.olatRootLeaf(savedDatas.getReturnFiles());
				if(targetArchive.exists()) {
					returnFileEl.setInitialFile(((LocalFileImpl)targetArchive).getBasefile());
				}
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(returnFileEl != null && returnFileEl.isUploadSuccess()) {
			returnFileEl.clearError();
			if(returnFileEl.getUploadFile() != null && returnFileEl.getUploadFile().exists()
					&& !ZipUtil.isReadable(returnFileEl.getUploadFile())) {
				returnFileEl.setErrorKey("error.zip", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String val = dataEl.getValue();
		if (!StringHelper.containsNonWhitespace(val) && (returnFileEl != null ? !returnFileEl.isUploadSuccess() : true)) {
			// do not proceed when nothing in input field and no file uploaded
			setFormWarning("form.step2.error");
			return;
		}
		
		setFormWarning(null); // reset error
		BulkAssessmentDatas datas = (BulkAssessmentDatas)getFromRunContext("datas");
		if(datas == null) {
			datas = new BulkAssessmentDatas();
		}
		
		if(statusEl.isOneSelected()) {
			String selectedStatus = statusEl.getSelectedKey();
			if(AssessmentEntryStatus.isValueOf(selectedStatus)) {
				datas.setStatus(AssessmentEntryStatus.valueOf(selectedStatus));
			}
		}
		
		if(visibilityEl != null && visibilityEl.isOneSelected()) {
			String selectedVisibility = visibilityEl.getSelectedKey();
			if("visible".equals(selectedVisibility)) {
				datas.setVisibility(Boolean.TRUE);
			} else if("notvisible".equals(selectedVisibility)) {
				datas.setVisibility(Boolean.FALSE);
			}
		}
		
		if(acceptSubmissionEl != null && acceptSubmissionEl.isOneSelected()) {
			datas.setAcceptSubmission(acceptSubmissionEl.isSelected(0));
		}

		if(bulkAssessmentTmpDir == null) {
			VFSContainer bulkAssessmentDir = VFSManager.olatRootContainer("/bulkassessment/", null);
			bulkAssessmentTmpDir = bulkAssessmentDir.createChildContainer(UUID.randomUUID().toString());
		}

		backupInputDatas(val, datas, bulkAssessmentTmpDir);
		List<String[]> splittedRows = splitRawData(val);
		addToRunContext("splittedRows", splittedRows);
		List<BulkAssessmentRow> rows = new ArrayList<>(100);
		if(returnFileEl != null) {
			processReturnFiles(datas, rows, bulkAssessmentTmpDir);
		}
		datas.setRows(rows);
		addToRunContext("datas", datas);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	private List<String[]> splitRawData(String idata) {
		String[] lines = idata.split("\r?\n");
		int numOfLines = lines.length;

		List<String[]> rows = new ArrayList<>(numOfLines);

		String d;
		if (delimiter.getSelectedKey().startsWith("t")) {
			d = "\t";
		} else {
			d = ",";
		}

		for (int i = 0; i < numOfLines; i++) {
			String line = lines[i];
			if(StringHelper.containsNonWhitespace(line)){
				String[] values = line.split(d,-1);
				rows.add(values);
			}
		}
		return rows;
	}

	/**
	 * Backup the input field for later editing purpose
	 * @param val
	 * @param datas
	 */
	private void backupInputDatas(String val, BulkAssessmentDatas datas, VFSContainer tmpDir) {
		VFSLeaf inputFile = null;
		if(StringHelper.containsNonWhitespace(datas.getDataBackupFile())) {
			inputFile = VFSManager.olatRootLeaf(datas.getDataBackupFile());
		}
		if(inputFile == null) {
			String inputFilename = UUID.randomUUID().toString() + ".csv";
			inputFile = tmpDir.createChildLeaf(inputFilename);
		}

		try(OutputStream out = inputFile.getOutputStream(false)) {
			IOUtils.write(val, out, StandardCharsets.UTF_8);
			datas.setDataBackupFile(inputFile.getRelPath());
		} catch (IOException e) {
			logError("", e);
		}
	}

	private void processReturnFiles(BulkAssessmentDatas datas, List<BulkAssessmentRow> rows, VFSContainer tmpDir) {
		File uploadedFile = returnFileEl.getUploadFile();
		if(uploadedFile == null) {
			File initialFile = returnFileEl.getInitialFile();
			if(initialFile != null && initialFile.exists()) {
				datas.setReturnFiles(targetArchive.getRelPath());
				processReturnFiles(targetArchive, rows);
			}
		} else if(uploadedFile.exists()) {
			//transfer to secured
			try {
				String uploadedFilename = returnFileEl.getUploadFileName();
				if(!StringHelper.containsNonWhitespace(uploadedFilename)) {
					uploadedFilename = "bulkAssessment.zip";
				}

				VFSItem currentTarget = tmpDir.resolve(uploadedFilename);
				if(!isSame(currentTarget, uploadedFile)) {
					if(currentTarget != null && currentTarget.exists()) {
						currentTarget.deleteSilently();
					}

					targetArchive = tmpDir.createChildLeaf(uploadedFilename);
					copyUploadFile(datas, uploadedFile, rows);
				} else {
					datas.setReturnFiles(targetArchive.getRelPath());
					processReturnFiles(targetArchive, rows);
				}
			} catch (IOException e) {
				logError("", e);
			}
		}
	}
	
	private void copyUploadFile(BulkAssessmentDatas datas, File uploadedFile, List<BulkAssessmentRow> rows) throws IOException {
		try(FileInputStream inStream = new FileInputStream(uploadedFile)) {
			if(VFSManager.copyContent(inStream, targetArchive, getIdentity())) {
				datas.setReturnFiles(targetArchive.getRelPath());
				processReturnFiles(targetArchive, rows);
			}
		} catch(IOException e) {
			logError("", e);
			throw e;
		}
	}

	private boolean isSame(VFSItem currentTarget, File uploadedFile) {
		if(currentTarget instanceof LocalImpl) {
			LocalImpl local = (LocalImpl)currentTarget;
			File currentFile = local.getBasefile();
			if(currentFile.length() == uploadedFile.length()) {
				try {
					return org.apache.commons.io.FileUtils.contentEquals(currentFile, uploadedFile);
				} catch (IOException e) {
					logError("", e);
					//do nothing -> return false at the end
				}
			}
		}
		return false;
	}

	private void processReturnFiles(VFSLeaf target, List<BulkAssessmentRow> rows) {
		Map<String, BulkAssessmentRow> assessedIdToRow = new HashMap<>();
		for(BulkAssessmentRow row:rows) {
			assessedIdToRow.put(row.getAssessedId(), row);
		}

		if(target.exists()) {
			File parentTarget = ((LocalImpl)target).getBasefile().getParentFile();

			ZipEntry entry;
			try(InputStream is = target.getInputStream();
					ZipInputStream zis = new ZipInputStream(is)) {
				byte[] b = new byte[FileUtils.BSIZE];
				while ((entry = zis.getNextEntry()) != null) {//TODO zip
					if(!entry.isDirectory()) {
						while (zis.read(b) > 0) {
							//continue
						}

						Path op = new File(parentTarget, entry.getName()).toPath();
						if(!Files.isHidden(op) && !op.toFile().isDirectory()) {
							Path parentDir = op.getParent();
							String assessedId = parentDir.getFileName().toString();
							String filename = op.getFileName().toString();

							BulkAssessmentRow row;
							if(assessedIdToRow.containsKey(assessedId)) {
								row = assessedIdToRow.get(assessedId);
							} else {
								row = new BulkAssessmentRow();
								row.setAssessedId(assessedId);
								assessedIdToRow.put(assessedId, row);
								rows.add(row);
							}

							if(row.getReturnFiles() == null) {
								row.setReturnFiles(new ArrayList<String>(2));
							}
							row.getReturnFiles().add(filename);
						}
					}
				}
			} catch(Exception e) {
				logError("", e);
			}
		}
	}
}