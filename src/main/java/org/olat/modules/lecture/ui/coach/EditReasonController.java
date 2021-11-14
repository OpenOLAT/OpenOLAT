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
package org.olat.modules.lecture.ui.coach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.EditAbsenceNoticeWrapper;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditReasonController extends FormBasicController {
	
	private static final String[] typeKeys = new String[] {
		AbsenceNoticeType.absence.name(), AbsenceNoticeType.notified.name(), AbsenceNoticeType.dispensation.name()
	};
	private static final String[] authorizedKeys = new String[] { "autorized" };
	
	private TextElement reasonEl;
	private SingleSelection typeEl;
	private FileElement documentUploadEl;
	private FormLayoutContainer filesLayout;
	private SingleSelection absenceCategoriesEl;
	private MultipleSelectionElement authorizedEl;

	private String mapperUri;
	private final boolean wizard;
	private final Identity noticedIdentity;
	private VFSContainer tempUploadFolder;
	private final VFSContainer documentContainer;
	private final EditAbsenceNoticeWrapper noticeWrapper;
	private final LecturesSecurityCallback secCallback;
	private List<AbsenceCategory> absenceCategories;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	
	public EditReasonController(UserRequest ureq, WindowControl wControl, Form rootForm,
			EditAbsenceNoticeWrapper noticeWrapper, LecturesSecurityCallback secCallback, boolean wizard) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		setTranslator(Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.wizard = wizard;
		this.secCallback = secCallback;
		this.noticeWrapper = noticeWrapper;
		this.noticedIdentity = noticeWrapper.getIdentity();
		documentContainer = lectureService.getAbsenceNoticeAttachmentsContainer(noticeWrapper.getAbsenceNotice());
		absenceCategories = lectureService.getAbsencesCategories(null);
		initForm(ureq);
		updateAttachments(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		mainForm.setMultipartEnabled(true);
		
		if(wizard) {
			formLayout.setElementCssClass("o_sel_absence_edit_reason");
			setFormTitle("notice.reason.title");
		}
		
		String fullName = userManager.getUserDisplayName(noticedIdentity);
		uifactory.addStaticTextElement("noticed.identity", fullName, formLayout);
		
		String[] typeValues = new String[] {
			translate("noticed.type.absence"), translate("noticed.type.notified"), translate("noticed.type.dispensation")
		};
		typeEl = uifactory.addRadiosHorizontal("noticed.type", "noticed.type", formLayout, typeKeys, typeValues);
		typeEl.setVisible(secCallback.viewAs() != LectureRoles.participant);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		if(noticeWrapper.getAbsenceNoticeType() != null) {
			typeEl.select(noticeWrapper.getAbsenceNoticeType().name(), true);
		}
		String[] authorizedValues = new String[] { translate("noticed.autorized.yes") };
		authorizedEl = uifactory.addCheckboxesHorizontal("noticed.autorized", null, formLayout, authorizedKeys, authorizedValues);
		authorizedEl.setVisible(secCallback.viewAs() != LectureRoles.participant);
		if(noticeWrapper.getAuthorized() != null && noticeWrapper.getAuthorized().booleanValue()) {
			authorizedEl.select(authorizedKeys[0], true);
		}

		AbsenceCategory currentCategory = noticeWrapper.getAbsenceCategory();
		SelectionValues absenceKeyValues = new SelectionValues();
		for(AbsenceCategory absenceCategory: absenceCategories) {
			if(absenceCategory.isEnabled() || absenceCategory.equals(currentCategory)) {
				absenceKeyValues.add(SelectionValues.entry(absenceCategory.getKey().toString(), absenceCategory.getTitle()));
			}
		}
		absenceCategoriesEl = uifactory.addDropdownSingleselect("absence.category", "noticed.category", formLayout,
				absenceKeyValues.keys(), absenceKeyValues.values());
		absenceCategoriesEl.setDomReplacementWrapperRequired(false);
		absenceCategoriesEl.setVisible(!absenceKeyValues.isEmpty());
		absenceCategoriesEl.setMandatory(true);
		if(currentCategory != null) {
			for(AbsenceCategory absenceCategory: absenceCategories) {
				if(absenceCategory.equals(currentCategory)) {
					absenceCategoriesEl.select(absenceCategory.getKey().toString(), true);
				}
			}
		}

		String currentReason = noticeWrapper.getAbsenceReason();
		reasonEl = uifactory.addTextAreaElement("reason", "noticed.reason", 2048, 4, 36, false, false, currentReason, formLayout);
		
		String editPage = Util.getPackageVelocityRoot(getClass()) + "/notice_files.html";
		filesLayout = FormLayoutContainer.createCustomFormLayout("filesLayout", getTranslator(), editPage);
		filesLayout.setLabel("attachment.upload", null);
		formLayout.add(filesLayout);

		documentUploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "attachment.upload", formLayout);
		documentUploadEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	protected void deleteTempStorage() {
		if(tempUploadFolder != null) {
			tempUploadFolder.delete();
		}
	}
	
	public AbsenceCategory getAbsenceCategory() {
		if(!absenceCategoriesEl.isVisible() || !absenceCategoriesEl.isOneSelected()) return null;
		
		String selectedKey = absenceCategoriesEl.getSelectedKey();
		if(StringHelper.isLong(selectedKey)) {
			Long categoryKey = Long.valueOf(selectedKey);
			for(AbsenceCategory absenceCategory:absenceCategories) {
				if(absenceCategory.getKey().equals(categoryKey)) {
					return absenceCategory;
				}
			}
		}
		return null;
	}
	
	private void updateAttachments(UserRequest ureq) {
		if(mapperUri == null) {
			mapperUri = registerCacheableMapper(ureq, "assigment-" + CodeHelper.getRAMUniqueID(), new DocumentMapper());
			filesLayout.contextPut("mapperUri", mapperUri);
		}
		
		List<VFSItem> files = new ArrayList<>();
		if(documentContainer != null) {
			List<VFSItem> currentItems = documentContainer.getItems(new VFSSystemItemFilter());
			List<VFSItem> deletedItems = noticeWrapper.getAttachmentsToDelete();
			for(VFSItem currentItem:currentItems) {
				boolean deleted = false;
				for(VFSItem deletedItem:deletedItems) {
					if(deletedItem.isSame(currentItem)) {
						deleted = true;
					}
				}
				if(!deleted) {
					files.add(currentItem);
				}
			}
			
		}
		// add files from TempFolder
		if(tempUploadFolder != null) {
			files.addAll(tempUploadFolder.getItems(new VFSSystemItemFilter()));
		}
		
		Collections.sort(files, new Comparator<VFSItem>(){
			final Collator c = Collator.getInstance(getLocale());
			@Override
			public int compare(final VFSItem o1, final VFSItem o2) {
				return c.compare((o1).getName(), (o2).getName());
			}
		});		

		filesLayout.contextPut("files", files);

		// add delete links for each attachment if user is allowed to see them
		int count = 0;
		for (VFSItem file : files) {
			FormLink deleteLink = uifactory.addFormLink("delete_" + (++count), filesLayout, Link.BUTTON_XSMALL);
			deleteLink.setUserObject(file);
			deleteLink.setI18nKey("delete");
		}

		boolean hasFile = !files.isEmpty();
		filesLayout.setVisible(hasFile);
		filesLayout.showLabel(hasFile);
		documentUploadEl.showLabel(!hasFile);
		documentUploadEl.getComponent().setDirty(true);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		absenceCategoriesEl.clearError();
		if(absenceCategoriesEl.isVisible() && !absenceCategoriesEl.isOneSelected()) {
			absenceCategoriesEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		typeEl.clearError();
		if(typeEl.isVisible() && !typeEl.isOneSelected()) {
			typeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(typeEl == source) {
			boolean forceAuthorized = typeEl.isOneSelected()
					&& AbsenceNoticeType.dispensation.name().equals(typeEl.getSelectedKey());
			if(forceAuthorized && !authorizedEl.isAtLeastSelected(1)) {
				authorizedEl.select(authorizedKeys[0], true);
			}
			authorizedEl.setEnabled(!forceAuthorized);
		} else if (source == documentUploadEl) {
			if (documentUploadEl.isUploadSuccess()) {
				doUploadDocument(ureq);
			}
		} else if (source instanceof FormLink) {
			FormLink activeLink = (FormLink) source;
			Object uobject = activeLink.getUserObject();
			if (uobject instanceof VFSLeaf) {
				VFSLeaf file = (VFSLeaf)uobject;
				if(tempUploadFolder != null && tempUploadFolder.resolve(file.getName()) != null) {
					file.delete();
				} else {
					noticeWrapper.getAttachmentsToDelete().add(file);
				}
			}
			updateAttachments(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		noticeWrapper.setAbsenceReason(reasonEl.getValue());
		noticeWrapper.setAbsenceCategory(getAbsenceCategory());
		if(authorizedEl.isVisible()) {
			noticeWrapper.setAuthorized(authorizedEl.isAtLeastSelected(1));
		} else {
			noticeWrapper.setAuthorized(null);
		}
		if(typeEl.isVisible()) {
			noticeWrapper.setAbsenceNoticeType(AbsenceNoticeType.valueOf(typeEl.getSelectedKey()));
		} else if(noticeWrapper.getAbsenceNoticeType() != null)  {
			noticeWrapper.setAbsenceNoticeType(noticeWrapper.getAbsenceNoticeType());
		}
	}
	
	private void doUploadDocument(UserRequest ureq) {
		String fileName = documentUploadEl.getUploadFileName();
		// checking tmp-folder and msg-container for filename
		boolean fileExists = false;
		if ((tempUploadFolder != null && tempUploadFolder.resolve(fileName) != null)
				|| (documentContainer != null && documentContainer.resolve(fileName) != null)) {
			fileExists = true;
		}

		if (fileExists) {
			try {
				Files.delete(documentUploadEl.getUploadFile().toPath());
				documentUploadEl.setErrorKey("attachments.error.file.exists", null);
				documentUploadEl.showError(true);
			} catch (IOException e) {
				logError("Cannot delete uploaded file", e);
			}
		} else {
			// files got stored in an extra tempFolder, to use the same
			// fileUploader multiple times
			if(tempUploadFolder == null) {
				tempUploadFolder = VFSManager.olatRootContainer(File.separator + "tmp/" + CodeHelper.getGlobalForeverUniqueID() + "/", null);
				noticeWrapper.setTempUploadFolder(tempUploadFolder);
			}
			documentUploadEl.moveUploadFileTo(tempUploadFolder);
			documentUploadEl.showError(false);
			documentUploadEl.reset();
			updateAttachments(ureq);
			showInfo("attachments.upload.successful", fileName);
		}
	}
	
	public class DocumentMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(relPath.startsWith("/")) {
				relPath = relPath.substring(1, relPath.length());
			}
			
			@SuppressWarnings("unchecked")
			List<VFSItem> files = (List<VFSItem>)filesLayout.contextGet("files");
			if(files != null) {
				for(VFSItem file:files) {
					if(relPath.equalsIgnoreCase(file.getName()) && file instanceof VFSLeaf) {
						return new VFSMediaResource((VFSLeaf)file);
					}
				}
			}
			return null;
		}
	}
}
