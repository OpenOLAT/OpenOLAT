/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.repository.controllers;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.olat.ControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.image.ImageFormItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.NamedFileMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CourseModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryIconRenderer;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;

/**
 * Description:<br>
 * 
 * @author Ingmar Kroll
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 * 
 */
public class RepositoryEditDescriptionController extends FormBasicController {

	private final boolean isSubWorkflow;
	private RepositoryEntry repositoryEntry;
	private final String repoEntryType;

	private static final int picUploadlimitKB = 1024;

	private FileElement fileUpload;
	private TextElement displayName;
	private RichTextElement description;
	private SingleSelection dateTypesEl, publicDatesEl;
	private DateChooser startDateEl, endDateEl;
	private ImageFormItem imageEl;
	private FormLink deleteImage;
	private FormSubmit submit;
	private FormLayoutContainer descCont, privateDatesCont;
	
	private static final String[] dateKeys = new String[]{ "none", "private", "public"};

	private final UserManager userManager;
	private final RepositoryManager repositoryManager;
	private final RepositoryEntryLifecycleDAO lifecycleDao;

	/**
	 * Create a repository add controller that adds the given resourceable.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param sourceEntry
	 */
	public RepositoryEditDescriptionController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean isSubWorkflow) {
		super(ureq, wControl, "bgrep");
		setBasePackage(RepositoryManager.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		lifecycleDao = CoreSpringFactory.getImpl(RepositoryEntryLifecycleDAO.class);
		this.isSubWorkflow = isSubWorkflow;
		this.repositoryEntry = entry;
		repoEntryType = repositoryEntry.getOlatResource().getResourceableTypeName();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer && isSubWorkflow) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", repositoryEntry.getDisplayname());
		}
		
		descCont = FormLayoutContainer.createDefaultFormLayout("desc", getTranslator());
		descCont.setFormContextHelp("org.olat.repository","rep-meta-desc.html","help.hover.lifecycle");
		descCont.setRootForm(mainForm);
		formLayout.add("desc", descCont);

		String id = repositoryEntry.getResourceableId() == null ? "-" : repositoryEntry.getResourceableId().toString();
		uifactory.addStaticTextElement("cif.id", id, descCont);

		String initalAuthor = repositoryEntry.getInitialAuthor() == null ? "-" : repositoryEntry.getInitialAuthor();
		if(repositoryEntry.getInitialAuthor() != null) {
			initalAuthor = userManager.getUserDisplayName(initalAuthor);
		}
		initalAuthor = StringHelper.escapeHtml(initalAuthor);
		uifactory.addStaticTextElement("cif.initialAuthor", initalAuthor, descCont);
		// Add resource type
		String typeName = null;
		OLATResource res = repositoryEntry.getOlatResource();
		if (res != null) typeName = res.getResourceableTypeName();
		StringBuilder typeDisplayText = new StringBuilder(100);
		if (typeName != null) { // add image and typename code
			RepositoryEntryIconRenderer reir = new RepositoryEntryIconRenderer(getLocale());
			typeDisplayText.append("<span class=\"b_with_small_icon_left ");
			typeDisplayText.append(reir.getIconCssClass(repositoryEntry));
			typeDisplayText.append("\">");
			String tName = ControllerFactory.translateResourceableTypeName(typeName, getLocale());
			typeDisplayText.append(tName);
			typeDisplayText.append("</span>");
		} else {
			typeDisplayText.append(translate("cif.type.na"));
		}
		uifactory.addStaticExampleText("cif.type", typeDisplayText.toString(), descCont);
		
		uifactory.addSpacerElement("spacer1", descCont, false);

		displayName = uifactory.addTextElement("cif.displayname", "cif.displayname", 100, repositoryEntry.getDisplayname(), descCont);
		displayName.setDisplaySize(30);
		displayName.setMandatory(true);
		displayName.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.title));

		String desc = (repositoryEntry.getDescription() != null ? repositoryEntry.getDescription() : " ");
		description = uifactory.addRichTextElementForStringDataMinimalistic("cif.description", "cif.description",
				desc, 10, -1, false, descCont, ureq.getUserSession(), getWindowControl());
		description.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.description));
		description.setMandatory(true);

		uifactory.addSpacerElement("spacer2", descCont, false);
		
		if(CourseModule.getCourseTypeName().equals(repoEntryType)) {
			String[] dateValues = new String[] {
					translate("cif.dates.none"),
					translate("cif.dates.private"),
					translate("cif.dates.public")	
			};
			dateTypesEl = uifactory.addRadiosVertical("cif.dates", descCont, dateKeys, dateValues);
			if(repositoryEntry.getLifecycle() == null) {
				dateTypesEl.select("none", true);
			} else if(repositoryEntry.getLifecycle().isPrivateCycle()) {
				dateTypesEl.select("private", true);
			} else {
				dateTypesEl.select("public", true);
			}
			dateTypesEl.addActionListener(this, FormEvent.ONCHANGE);
	
			List<RepositoryEntryLifecycle> cycles = lifecycleDao.loadPublicLifecycle();
			String[] publicKeys = new String[cycles.size()];
			String[] publicValues = new String[cycles.size()];
			int count = 0;	
			for(RepositoryEntryLifecycle cycle:cycles) {
				publicKeys[count] = cycle.getKey().toString();
				publicValues[count++] = cycle.getLabel();
			}
			publicDatesEl = uifactory.addDropdownSingleselect("cif.public.dates", descCont, publicKeys, publicValues, null);
	
			String privateDatePage = velocity_root + "/cycle_dates.html";
			privateDatesCont = FormLayoutContainer.createCustomFormLayout("private.date", getTranslator(), privateDatePage);
			privateDatesCont.setRootForm(mainForm);
			privateDatesCont.setLabel("cif.private.dates", null);
			descCont.add("private.date", privateDatesCont);
			
			startDateEl = uifactory.addDateChooser("date.start", "cif.date.start", null, privateDatesCont);
			endDateEl = uifactory.addDateChooser("date.end", "cif.date.end", null, privateDatesCont);
			
			if(repositoryEntry.getLifecycle() != null) {
				RepositoryEntryLifecycle lifecycle = repositoryEntry.getLifecycle();
				if(lifecycle.isPrivateCycle()) {
					startDateEl.setDate(lifecycle.getValidFrom());
					endDateEl.setDate(lifecycle.getValidTo());
				} else {
					String key = lifecycle.getKey().toString();
					for(String publicKey:publicKeys) {
						if(key.equals(publicKey)) {
							publicDatesEl.select(key, true);
							break;
						}
					}
				}
			}
	
			updateDatesVisibility();
			uifactory.addSpacerElement("spacer3", descCont, false);
		}
		
		boolean managed = RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);
		
		VFSLeaf img = repositoryManager.getImage(repositoryEntry);
		imageEl = new ImageFormItem("imageEl");
		imageEl.setLabel("rentry.pic", null);
		
		if(img == null) {
			imageEl.setVisible(false);
		} else {
			imageEl.setMediaResource(new VFSMediaResource(img));
			imageEl.setMaxWithAndHeightToFitWithin(400, 200);
		}
		descCont.add(imageEl);
		
		deleteImage = uifactory.addFormLink("delete", "cmd.delete", null, descCont, Link.BUTTON);
		deleteImage.setVisible(img != null && !managed);

		fileUpload = uifactory.addFileElement("rentry.pic", "rentry.pic", descCont);
		if(img != null) {
			fileUpload.setLabel(null, null);
		}
		fileUpload.setMaxUploadSizeKB(picUploadlimitKB, null, null);
		fileUpload.addActionListener(this, FormEvent.ONCHANGE);
		fileUpload.setVisible(!managed);
		
		Set<String> mimeTypes = new HashSet<String>();
		mimeTypes.add("image/gif");
		mimeTypes.add("image/jpg");
		mimeTypes.add("image/jpeg");
		mimeTypes.add("image/png");
		fileUpload.limitToMimeType(mimeTypes, null, null);

		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
		formLayout.add("buttonContainer", buttonContainer);
		buttonContainer.setElementCssClass("o_sel_repo_save_details");
		submit = uifactory.addFormSubmitButton("submit", buttonContainer);
		submit.setVisible(!managed);
		if (!isSubWorkflow) {
			uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
		}
	}
	
	private void updateDatesVisibility() {
		if(dateTypesEl.isOneSelected()) {
			String type = dateTypesEl.getSelectedKey();
			if("none".equals(type)) {
				publicDatesEl.setVisible(false);
				privateDatesCont.setVisible(false);
			} else if("public".equals(type)) {
				publicDatesEl.setVisible(true);
				privateDatesCont.setVisible(false);
			} else if("private".equals(type)) {
				publicDatesEl.setVisible(false);
				privateDatesCont.setVisible(true);
			}
		}
		descCont.setDirty(true);
	}

	@Override
	protected void doDispose() {
		// Controllers autodisposed by basic controller
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		// Check for empty display name
		boolean allOk = true;
		if (!StringHelper.containsNonWhitespace(displayName.getValue())) {
			displayName.setErrorKey("cif.error.displayname.empty", new String[] {});
			allOk = false;
		} else if (displayName.hasError()) {
			allOk = false;
		} else {
			displayName.clearError();
		}
		
		// Check for empty description
		if (!StringHelper.containsNonWhitespace(description.getValue())) {
			description.setErrorKey("cif.error.description.empty", new String[] {});
			allOk = false;
		} else {
			description.clearError();
		}
		
		// Ok, passed all checks
		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == dateTypesEl) {
			updateDatesVisibility();
		} else if (source == fileUpload) {
			if (fileUpload.isUploadSuccess()) {
				File uploadedFile = fileUpload.getUploadFile();
				imageEl.setMediaResource(new NamedFileMediaResource(uploadedFile, fileUpload.getName(), "", false));
				imageEl.setMaxWithAndHeightToFitWithin(400, 200);
				imageEl.setVisible(true);
				imageEl.getComponent().setDirty(true);
				deleteImage.setVisible(true);
				fileUpload.setLabel(null, null);
				flc.setDirty(true);
			}
		} else if (source == deleteImage) {

			VFSLeaf img = repositoryManager.getImage(repositoryEntry);
			
			if(fileUpload.getUploadFile() != null) {
				fileUpload.reset();
				
				if(img == null) {
					imageEl.setVisible(false);
					deleteImage.setVisible(false);
					fileUpload.setLabel("rentry.pic", null);
				} else {
					imageEl.setMediaResource(new VFSMediaResource(img));
					imageEl.setMaxWithAndHeightToFitWithin(400, 200);
					imageEl.setVisible(true);
					imageEl.setLabel("rentry.pic", null);
					deleteImage.setVisible(true);
					fileUpload.setLabel(null, null);
				}
			} else if(img != null) {
				repositoryManager.deleteImage(repositoryEntry);
				
				imageEl.setVisible(false);
				deleteImage.setVisible(false);
				fileUpload.setLabel("rentry.pic", null);
			}

			flc.setDirty(true);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		File uploadedFile = fileUpload.getUploadFile();
		if(uploadedFile != null) {
			VFSContainer tmpHome = new LocalFolderImpl(new File(WebappHelper.getTmpDir()));
			VFSContainer container = tmpHome.createChildContainer(UUID.randomUUID().toString());
			VFSLeaf newFile = fileUpload.moveUploadFileTo(container);//give it it's real name and extension
			boolean ok = repositoryManager.setImage((VFSLeaf)newFile, repositoryEntry);
			if (!ok) {
				showError("Failed");
			}
			container.delete();
		}
		
		String displayname = displayName.getValue().trim();
		String desc = description.getValue().trim();
		
		if(dateTypesEl != null) {
			String type = "none";
			if(dateTypesEl.isOneSelected()) {
				type = dateTypesEl.getSelectedKey();
			}
			
			if("none".equals(type)) {
				RepositoryEntryLifecycle cycle = repositoryEntry.getLifecycle();
				repositoryEntry.setLifecycle(null);
				if(cycle != null && cycle.isPrivateCycle()) {
					lifecycleDao.deleteLifecycle(cycle);
				}
			} else if("public".equals(type)) {
				String key = publicDatesEl.getSelectedKey();
				if(StringHelper.isLong(key)) {
					Long cycleKey = Long.parseLong(key);
					RepositoryEntryLifecycle cycle = lifecycleDao.loadById(cycleKey);
					repositoryEntry.setLifecycle(cycle);
				}
			} else if("private".equals(type)) {
				Date start = startDateEl.getDate();
				Date end = endDateEl.getDate();
				RepositoryEntryLifecycle cycle = repositoryEntry.getLifecycle();
				if(cycle == null || !cycle.isPrivateCycle()) {
					String softKey = "lf_" + repositoryEntry.getSoftkey();
					cycle = lifecycleDao.create(displayname, softKey, true, start, end);
				} else {
					cycle.setValidFrom(start);
					cycle.setValidTo(end);
					cycle = lifecycleDao.updateLifecycle(cycle);
				}
				repositoryEntry.setLifecycle(cycle);
			}
		}
		
		repositoryEntry.setDisplayname(displayname);
		repositoryEntry.setDescription(desc);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	/**
	 * @return Returns the repositoryEntry.
	 */
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}
}