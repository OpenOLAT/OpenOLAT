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
package org.olat.course.nodes.gta.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.ui.tool.AssessmentForm.DocumentWrapper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 May 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EditAssessmentDocumentController extends FormBasicController {
	
	private FileElement groupUploadDocsEl;

	private final GTACourseNode gtaNode;
	private final AssessmentRow row;
	private final ICourse course;
	private final boolean readOnly;
	private final UserCourseEnvironment userCourseEnv;
	private final File assessmentDocsTmpDir;

	private int counter = 0;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public EditAssessmentDocumentController(UserRequest ureq, WindowControl wControl, OLATResourceable courseOres,
			GTACourseNode gtaNode, AssessmentRow row, boolean readOnly) {
		super(ureq, wControl, "edit_assessment_docs");
		this.gtaNode = gtaNode;
		this.row = row;
		this.readOnly = readOnly;
		course = CourseFactory.loadCourse(courseOres);
		userCourseEnv = row.getUserCourseEnvironment(course);
		assessmentDocsTmpDir = FileUtils.createTempDir("gtaassessmentdocs", null, null);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String mapperUri = registerCacheableMapper(ureq, null, new DocumentMapper());
		flc.contextPut("mapperUri", mapperUri);
		List<File> currentAssessmentDocs = courseAssessmentService.getIndividualAssessmentDocuments(gtaNode, userCourseEnv);
		for (File assessmentDoc : currentAssessmentDocs) {
			File targetFile = new File(assessmentDocsTmpDir, assessmentDoc.getName());
			FileUtils.copyFileToFile(assessmentDoc, targetFile, false);
			updateAssessmentDocsUI();
		}
		
		if (!readOnly) {
			groupUploadDocsEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "upload", null, formLayout);
			groupUploadDocsEl.addActionListener(FormEvent.ONCHANGE);

			uifactory.addFormSubmitButton("save", formLayout);
			uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(groupUploadDocsEl == source) {
			if(groupUploadDocsEl.getUploadFile() != null && StringHelper.containsNonWhitespace(groupUploadDocsEl.getUploadFileName())) {
				groupUploadDocsEl.moveUploadFileTo(assessmentDocsTmpDir);
				updateAssessmentDocsUI();
				groupUploadDocsEl.reset();
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if(link.getCmd() != null && link.getCmd().startsWith("delete_doc_")) {
				DocumentWrapper wrapper = (DocumentWrapper)link.getUserObject();
				doDeleteGroupAssessmentDoc(wrapper.getDocument());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		File[] assessmentDocs = assessmentDocsTmpDir.listFiles();
		List<File> currentAssessmentDocs = courseAssessmentService.getIndividualAssessmentDocuments(gtaNode, userCourseEnv);
		for (File currentAssessmentDoc : currentAssessmentDocs) {
			courseAssessmentService.removeIndividualAssessmentDocument(gtaNode, currentAssessmentDoc,
					userCourseEnv, getIdentity());
		}
		for (File assessmentDoc : assessmentDocs) {
			courseAssessmentService.addIndividualAssessmentDocument(gtaNode, assessmentDoc,
					assessmentDoc.getName(), userCourseEnv, getIdentity());
		}
		
		if(assessmentDocs.length == 0) {
			row.getAssessmentDocsEditLink().setIconLeftCSS("o_icon o_filetype_file");
		} else {
			row.getAssessmentDocsEditLink().setIconLeftCSS("o_icon o_icon_files");
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		if (assessmentDocsTmpDir != null && assessmentDocsTmpDir.exists()) {
			FileUtils.deleteDirsAndFiles(assessmentDocsTmpDir, true, true);
		}
	}
	
	private void updateAssessmentDocsUI() {
		File[] documents = assessmentDocsTmpDir.listFiles();
		List<DocumentWrapper> wrappers = new ArrayList<>(documents.length);
		for (File document : documents) {
			DocumentWrapper wrapper = new DocumentWrapper(document);
			wrappers.add(wrapper);
			
			if (!readOnly) {
				FormLink deleteButton = uifactory.addFormLink("delete_doc_" + (++counter), "delete", null, flc, Link.BUTTON_XSMALL);
				deleteButton.setUserObject(wrappers);
				wrapper.setDeleteButton(deleteButton);
			}
		}
		flc.contextPut("documents", wrappers);
	}
	
	private void doDeleteGroupAssessmentDoc(File document) {
		FileUtils.deleteFile(document);
		updateAssessmentDocsUI();
	}

	
	public class DocumentMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(StringHelper.containsNonWhitespace(relPath)) {
				if(relPath.startsWith("/")) {
					relPath = relPath.substring(1, relPath.length());
				}
			
				@SuppressWarnings("unchecked")
				List<DocumentWrapper> wrappers = (List<DocumentWrapper>)flc.contextGet("documents");
				if(wrappers != null) {
					for(DocumentWrapper wrapper:wrappers) {
						if(relPath.equals(wrapper.getFilename())) {
							return new FileMediaResource(wrapper.getDocument(), true);
						}
					}
				}
			}
			return new NotFoundMediaResource();
		}
	}

}
