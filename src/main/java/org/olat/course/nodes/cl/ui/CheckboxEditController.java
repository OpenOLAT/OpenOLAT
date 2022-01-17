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
package org.olat.course.nodes.cl.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckboxEditController extends FormBasicController {
	
	private FormLink deleteLink;
	private FormLink downloadFileLink;
	private TextElement titleEl, pointsEl;
	private SingleSelection releaseEl, labelEl;
	private MultipleSelectionElement awardPointEl;
	private RichTextElement descriptionEl;
	private FileElement fileEl;
	
	private List<String> filesToDelete = new ArrayList<>();
	
	private final Checkbox checkbox;
	private final boolean withScore;
	private final boolean newCheckbox;
	private final OLATResourceable courseOres;
	private final CheckListCourseNode courseNode;
	
	@Autowired
	private CheckboxManager checkboxManager;
	
	public CheckboxEditController(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres,
			Checkbox checkbox, boolean newCheckbox, boolean withScore) {
		this(ureq, wControl, courseOres, null, checkbox, newCheckbox, withScore);
	}
	
	public CheckboxEditController(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres, CheckListCourseNode courseNode,
			Checkbox checkbox, boolean newCheckbox, boolean withScore) {
		super(ureq, wControl);
		this.checkbox = checkbox;
		this.withScore = withScore;
		this.courseOres = courseOres;
		this.courseNode = courseNode;
		this.newCheckbox = newCheckbox;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_cl_edit_checkbox_form");
		String title = checkbox.getTitle();
		titleEl = uifactory.addTextElement("checkbox.title", "checkbox.title", 255, title, formLayout);
		titleEl.setElementCssClass("o_sel_cl_checkbox_title");
		
		String[] releaseKeys = new String[] {
				CheckboxReleaseEnum.userAndCoach.name(), CheckboxReleaseEnum.coachOnly.name()
		};
		String[] releaseValues = new String[] {
				translate("release.userAndCoach"), translate("release.coachOnly")
		};
		releaseEl = uifactory.addDropdownSingleselect("release", formLayout, releaseKeys, releaseValues, null);
		if(checkbox.getRelease() != null) {
			releaseEl.select(checkbox.getRelease().name(), true);
		}

		String[] labelKeys = new String[CheckboxLabelEnum.values().length];
		String[] labelValues = new String[CheckboxLabelEnum.values().length];
		for(int i=CheckboxLabelEnum.values().length; i-->0; ){
			labelKeys[i] = CheckboxLabelEnum.values()[i].name();
			labelValues[i] = translate(CheckboxLabelEnum.values()[i].i18nKey());
		}
		labelEl = uifactory.addDropdownSingleselect("label", formLayout, labelKeys, labelValues, null);
		if(checkbox.getLabel() != null) {
			labelEl.select(checkbox.getLabel().name(), true);
		}
		
		String[] onKeys = new String[] { "on" };
		String[] onValues = new String[] { translate("award.point.on") };
		awardPointEl = uifactory.addCheckboxesHorizontal("points", formLayout, onKeys, onValues);
		awardPointEl.setElementCssClass("o_sel_cl_checkbox_award_points");
		awardPointEl.setVisible(withScore);
		awardPointEl.addActionListener(FormEvent.ONCHANGE);
		if(checkbox.getPoints() != null) {
			awardPointEl.select(onKeys[0], true);
		}
		String points = checkbox.getPoints() == null ? null : Float.toString(checkbox.getPoints().floatValue());
		pointsEl = uifactory.addTextElement("numofpoints", null, 10, points, formLayout);
		pointsEl.setElementCssClass("o_sel_cl_checkbox_points");
		pointsEl.setVisible(withScore && awardPointEl.isAtLeastSelected(1));
		pointsEl.setDisplaySize(5);
		
		String desc = checkbox.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("description", "description", desc, 10, -1, formLayout,
				getWindowControl());
		descriptionEl.getEditorConfiguration().enableMathEditor();

		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "file", formLayout);
		fileEl.setDeleteEnabled(true);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		if(courseNode != null && checkbox != null && StringHelper.containsNonWhitespace(checkbox.getFilename())) {
			CourseEnvironment courseEnv = CourseFactory.loadCourse(courseOres).getCourseEnvironment();
			File directory = checkboxManager.getFileDirectory(courseEnv, courseNode);
			fileEl.setInitialFile(new File(directory, checkbox.getFilename()));	
		}

		downloadFileLink = uifactory.addFormLink("download", checkbox.getFilename(), null, formLayout, Link.NONTRANSLATED);
		downloadFileLink.setVisible(fileEl.getInitialFile() != null);
		downloadFileLink.setIconLeftCSS("o_icon o_icon_download");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		deleteLink = uifactory.addFormLink("delete", buttonsCont, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	public boolean isNewCheckbox() {
		return newCheckbox;
	}
	
	public Checkbox getCheckbox() {
		return checkbox;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		pointsEl.clearError();
		if(awardPointEl.isAtLeastSelected(1)) {
			try {
				Float.parseFloat(pointsEl.getValue());
			} catch (NumberFormatException e) {
				pointsEl.setErrorKey("form.error.wrongFloat", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		checkbox.setTitle(titleEl.getValue());
		String releaseKey = releaseEl.getSelectedKey();
		checkbox.setRelease(CheckboxReleaseEnum.valueOf(releaseKey));
		String labelKey = labelEl.getSelectedKey();
		checkbox.setLabel(CheckboxLabelEnum.valueOf(labelKey));
		if(awardPointEl.isAtLeastSelected(1)) {
			Float points = null;
			try {
				points = Float.valueOf(Float.parseFloat(pointsEl.getValue()));
			} catch (NumberFormatException e) {
				//check in validation
			}
			checkbox.setPoints(points);	
		} else {
			checkbox.setPoints(null);
		}
		checkbox.setDescription(descriptionEl.getValue());
		
		deleteFiles();
		
		if(fileEl.getUploadFile() == null && fileEl.getInitialFile() == null) {
			checkbox.setFilename(null);
		} else if(fileEl.getUploadFile() != null) {
			String filename = fileEl.getUploadFileName();
			checkbox.setFilename(filename);
			
			VFSContainer container = getFileContainer();
			VFSLeaf leaf = container.createChildLeaf(filename);
			File uploadedFile = fileEl.getUploadFile();
			try(InputStream inStream = new FileInputStream(uploadedFile)) {
				VFSManager.copyContent(inStream, leaf, getIdentity());
			} catch (IOException e) {
				logError("", e);
			}
		}
		
		if(courseNode != null) {
			ILoggingAction action = newCheckbox ? CourseLoggingAction.CHECKLIST_CHECKBOX_CREATED : CourseLoggingAction.CHECKLIST_CHECKBOX_UPDATED;
			ThreadLocalUserActivityLogger.log(action, getClass(), LoggingResourceable.wrap(courseNode),
				LoggingResourceable.wrapNonOlatResource(StringResourceableType.checkbox, checkbox.getCheckboxId(), checkbox.getTitle()));
		}

		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void deleteFiles() {
		File directory = getFileDirectory();
		if(courseNode != null) {
			CheckboxList list = (CheckboxList)courseNode.getModuleConfiguration().get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
			if(list != null && list.getList() != null) {
				for(Checkbox box:list.getList()) {
					if((checkbox == null || !checkbox.getCheckboxId().equals(box.getCheckboxId()))
							&& StringHelper.containsNonWhitespace(box.getFilename())) {
						filesToDelete.remove(box.getFilename());
					}
				}
			}
		}

		for(String filenameToDelete: filesToDelete) {
			File fileToDelete = new File(directory, filenameToDelete);
			try {
				Files.deleteIfExists(fileToDelete.toPath());
			} catch (IOException e) {
				logError("Cannot delete file: " + fileToDelete, e);
			}
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteLink == source) {
			fireEvent(ureq, new Event("delete"));
			if(courseNode != null) {
				ThreadLocalUserActivityLogger.log(CourseLoggingAction.CHECKLIST_CHECKBOX_DELETED, getClass(), LoggingResourceable.wrap(courseNode),
						LoggingResourceable.wrapNonOlatResource(StringResourceableType.checkbox, checkbox.getCheckboxId(), checkbox.getTitle()));
			}
		} else if(downloadFileLink == source) {
			doDownloadFile(ureq);
		} else if(awardPointEl == source) {
			pointsEl.setVisible(withScore && awardPointEl.isAtLeastSelected(1));
		} else if(fileEl == source) {
			if(FileElementEvent.DELETE.equals(event.getCommand())) {
				if(fileEl.getInitialFile() != null) {
					filesToDelete.add(fileEl.getInitialFile().getName());
				}
				
				
				fileEl.clearError();
				if(fileEl.getUploadFile() != null && fileEl.getUploadFile() != fileEl.getInitialFile()) {
					fileEl.reset();
				} else {
					fileEl.setInitialFile(null);
				}
				flc.setDirty(true);
			} else if(fileEl.isUploadSuccess()) {
				filesToDelete.remove(fileEl.getUploadFileName());
			}
			
			downloadFileLink.setVisible(fileEl.getInitialFile() != null);
			
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doDownloadFile(UserRequest ureq) {
		VFSContainer container = getFileContainer();
		VFSItem item = container.resolve(checkbox.getFilename());
		if(item instanceof VFSLeaf) {
			VFSMediaResource rsrc = new VFSMediaResource((VFSLeaf)item);
			rsrc.setDownloadable(true);
			ureq.getDispatchResult().setResultingMediaResource(rsrc);
		}
	}
	
	private VFSContainer getFileContainer() {
		VFSContainer container;
		if(courseNode == null) {
			File tmp = new File(FolderConfig.getCanonicalTmpDir(), checkbox.getCheckboxId());
			container = new LocalFolderImpl(tmp);
		} else {
			ICourse course = CourseFactory.loadCourse(courseOres);
			CourseEnvironment courseEnv = course.getCourseEnvironment();
			container = checkboxManager.getFileContainer(courseEnv, courseNode);
		}
		return container;
	}
	
	private File getFileDirectory() {
		File directory;
		if(courseNode == null) {
			directory = new File(FolderConfig.getCanonicalTmpDir(), checkbox.getCheckboxId());
		} else {
			ICourse course = CourseFactory.loadCourse(courseOres);
			CourseEnvironment courseEnv = course.getCourseEnvironment();
			directory = checkboxManager.getFileDirectory(courseEnv, courseNode);
		}
		return directory;
	}
}
