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
package org.olat.repository.ui.settings;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseModule;
import org.olat.modules.edusharing.EdusharingProvider;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.MediaContainerFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryInfoController extends FormBasicController {
	
	private static final Set<String> imageMimeTypes = new HashSet<>();
	static {
		imageMimeTypes.add("image/gif");
		imageMimeTypes.add("image/jpg");
		imageMimeTypes.add("image/jpeg");
		imageMimeTypes.add("image/png");
	}
	
	private static final int picUploadlimitKB = 5120;
	private static final int movieUploadlimitKB = 102400;

	private VFSContainer mediaContainer;
	private RepositoryEntry repositoryEntry;

	private FileElement fileUpload;
	private FileElement movieUpload;
	private TextElement externalRef;
	private TextElement displayName;
	private RichTextElement description;
	private RichTextElement objectives;
	private RichTextElement requirements;
	private RichTextElement credits;

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;


	/**
	 * Create a repository add controller that adds the given resourceable.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param sourceEntry
	 */
	public RepositoryEntryInfoController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		setBasePackage(RepositoryService.class);
		this.repositoryEntry = entry;
		initForm(ureq);
	}

	/**
	 * @return Returns the repositoryEntry.
	 */
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		setFormContextHelp("Set up info page");
		formLayout.setElementCssClass("o_sel_edit_repositoryentry");
		setFormTitle("details.info.title");

		displayName = uifactory.addTextElement("cif.displayname", "cif.displayname", 100, repositoryEntry.getDisplayname(), formLayout);
		displayName.setDisplaySize(30);
		displayName.setMandatory(true);
		displayName.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.title));
		
		String extRef = repositoryEntry.getExternalRef();
		if(StringHelper.containsNonWhitespace(repositoryEntry.getManagedFlagsString())) {
			if(StringHelper.containsNonWhitespace(extRef)) {
				uifactory.addStaticTextElement("cif.externalref", extRef, formLayout);
			}
		} else {
			externalRef = uifactory.addTextElement("cif.externalref", "cif.externalref", 100, extRef, formLayout);
			externalRef.setHelpText(translate("cif.externalref.hover"));
			externalRef.setHelpUrlForManualPage("Set up info page");
			externalRef.setDisplaySize(30);
		}

		RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(repositoryEntry);
		mediaContainer = handler.getMediaContainer(repositoryEntry);
		if(mediaContainer != null && mediaContainer.getName().equals("media")) {
			mediaContainer = mediaContainer.getParentContainer();
			mediaContainer.setDefaultItemFilter(new MediaContainerFilter(mediaContainer));
		}
		
		String desc = (repositoryEntry.getDescription() != null ? repositoryEntry.getDescription() : " ");
		description = uifactory.addRichTextElementForStringData("cif.description", "cif.description",
				desc, 10, -1, false, mediaContainer, null, formLayout, usess, getWindowControl());
		description.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.description));
		description.getEditorConfiguration().setFileBrowserUploadRelPath("media");
		description.getEditorConfiguration().setPathInStatusBar(false);
		EdusharingProvider provider = new RepositoryEdusharingProvider(repositoryEntry, "repository-info");
		description.getEditorConfiguration().enableEdusharing(getIdentity(), provider);

		if(CourseModule.getCourseTypeName().equals(repositoryEntry.getOlatResource().getResourceableTypeName())) {
			initCourse(formLayout, usess);
		}
		
		boolean managed = RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);
		
		VFSLeaf img = repositoryManager.getImage(repositoryEntry);
		fileUpload = uifactory.addFileElement(getWindowControl(), "rentry.pic", "rentry.pic", formLayout);
		fileUpload.setExampleKey("rentry.pic.example", new String[] {RepositoryManager.PICTURE_WIDTH + "x" + (RepositoryManager.PICTURE_HEIGHT)});
		fileUpload.setMaxUploadSizeKB(picUploadlimitKB, null, null);
		fileUpload.setPreview(usess, true);
		fileUpload.addActionListener(FormEvent.ONCHANGE);
		fileUpload.setDeleteEnabled(!managed);
		if(img instanceof LocalFileImpl) {
			fileUpload.setPreview(usess, true);
			fileUpload.setInitialFile(((LocalFileImpl)img).getBasefile());
		}
		fileUpload.setVisible(!managed);
		fileUpload.limitToMimeType(imageMimeTypes, "cif.error.mimetype", new String[]{ imageMimeTypes.toString()} );

		VFSLeaf movie = repositoryService.getIntroductionMovie(repositoryEntry);
		movieUpload = uifactory.addFileElement(getWindowControl(), "rentry.movie", "rentry.movie", formLayout);
		movieUpload.setExampleKey("rentry.movie.example", new String[] {"3:2"});
		movieUpload.setMaxUploadSizeKB(movieUploadlimitKB, null, null);
		movieUpload.setPreview(usess, true);
		movieUpload.addActionListener(FormEvent.ONCHANGE);
		movieUpload.setDeleteEnabled(!managed);
		if(movie instanceof LocalFileImpl) {
			movieUpload.setPreview(usess, true);
			movieUpload.setInitialFile(((LocalFileImpl)movie).getBasefile());
		}
		movieUpload.setVisible(!managed);

		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
		formLayout.add("buttonContainer", buttonContainer);
		buttonContainer.setElementCssClass("o_sel_repo_save_details");
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
		FormSubmit submit = uifactory.addFormSubmitButton("submit", buttonContainer);
		submit.setVisible(!managed);
	}
	
	private void initCourse(FormItemContainer formLayout, UserSession usess) {
		String obj = (repositoryEntry.getObjectives() != null ? repositoryEntry.getObjectives() : " ");
		objectives = uifactory.addRichTextElementForStringData("cif.objectives", "cif.objectives",
				obj, 10, -1, false, mediaContainer, null, formLayout, usess, getWindowControl());
		objectives.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.objectives));
		objectives.getEditorConfiguration().setFileBrowserUploadRelPath("media");
		objectives.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		
		String req = (repositoryEntry.getRequirements() != null ? repositoryEntry.getRequirements() : " ");
		requirements = uifactory.addRichTextElementForStringData("cif.requirements", "cif.requirements",
				req, 10, -1,  false, mediaContainer, null, formLayout, usess, getWindowControl());
		requirements.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.requirements));
		requirements.getEditorConfiguration().setFileBrowserUploadRelPath("media");
		requirements.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		requirements.setMaxLength(2000);
		
		String cred = (repositoryEntry.getCredits() != null ? repositoryEntry.getCredits() : " ");
		credits = uifactory.addRichTextElementForStringData("cif.credits", "cif.credits",
				cred, 10, -1,  false, mediaContainer, null, formLayout, usess, getWindowControl());
		credits.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.credits));
		credits.getEditorConfiguration().setFileBrowserUploadRelPath("media");
		credits.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		credits.setMaxLength(2000);
	}

	@Override
	protected void doDispose() {
		// Controllers autodisposed by basic controller
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		// Check for empty display name
		if (!StringHelper.containsNonWhitespace(displayName.getValue())) {
			displayName.setErrorKey("cif.error.displayname.empty", new String[] {});
			allOk = false;
		} else if (displayName.hasError()) {
			allOk = false;
		} else {
			displayName.clearError();
		}

		allOk &= validateTextElement(objectives, 2000);
		allOk &= validateTextElement(requirements, 2000);
		allOk &= validateTextElement(credits, 2000);
		allOk &= validateTextElement(externalRef, 58);

		return allOk;
	}
	
	private boolean validateTextElement(TextElement el, int maxLength) {
		boolean ok;
		if(el == null) {
			ok = true;
		} else {
			String val = el.getValue();
			el.clearError();
			if(val != null && val.length() > maxLength) {
				el.setErrorKey("input.toolong", new String[]{ Integer.toString(maxLength) });
				ok = false;
			} else {
				ok = true;
			}
		}
		return ok;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == fileUpload) {
			if(FileElementEvent.DELETE.equals(event.getCommand())) {
				fileUpload.clearError();
				VFSLeaf img = repositoryManager.getImage(repositoryEntry);
				if(fileUpload.getUploadFile() != null && fileUpload.getUploadFile() != fileUpload.getInitialFile()) {
					fileUpload.reset();
					if(img != null) {
						fileUpload.setInitialFile(((LocalFileImpl)img).getBasefile());
					}
				} else if(img != null) {
					repositoryManager.deleteImage(repositoryEntry);
					fileUpload.setInitialFile(null);
				}
				flc.setDirty(true);	
			}
		} else if (source == movieUpload) {
			if(FileElementEvent.DELETE.equals(event.getCommand())) {
				movieUpload.clearError();
				VFSLeaf movie = repositoryService.getIntroductionMovie(repositoryEntry);
				if(movieUpload.getUploadFile() != null && movieUpload.getUploadFile() != movieUpload.getInitialFile()) {
					movieUpload.reset();
					if(movie != null) {
						movieUpload.setInitialFile(((LocalFileImpl)movie).getBasefile());
					}
				} else if(movie != null) {
					movie.delete();
					movieUpload.setInitialFile(null);
				}
				flc.setDirty(true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {

		File uploadedImage = fileUpload.getUploadFile();
		if(uploadedImage != null && uploadedImage.exists()) {
			VFSContainer tmpHome = new LocalFolderImpl(new File(WebappHelper.getTmpDir()));
			VFSContainer tmpContainer = tmpHome.createChildContainer(UUID.randomUUID().toString());
			VFSLeaf newFile = fileUpload.moveUploadFileTo(tmpContainer);//give it it's real name and extension
			boolean ok = repositoryManager.setImage(newFile, repositoryEntry);
			if (!ok) {
				showWarning("cif.error.image");
			} else {
				VFSLeaf image = repositoryManager.getImage(repositoryEntry);
				if(image instanceof  LocalFileImpl) {
					fileUpload.setInitialFile(((LocalFileImpl)image).getBasefile());
				}
			}
			tmpContainer.deleteSilently();
		}

		File uploadedMovie = movieUpload.getUploadFile();
		if(uploadedMovie != null && uploadedMovie.exists()) {
			VFSContainer m = (VFSContainer)mediaContainer.resolve("media");
			VFSLeaf newFile = movieUpload.moveUploadFileTo(m);
			if (newFile == null) {
				showWarning("cif.error.movie");
			} else {
				String filename = movieUpload.getUploadFileName();
				String extension = FileUtils.getFileSuffix(filename);
				newFile.rename(repositoryEntry.getKey() + "." + extension);
			}
		}

		String displayname = displayName.getValue().trim();
		repositoryEntry.setDisplayname(displayname);

		if(externalRef != null && externalRef.isEnabled()) {
			String ref = externalRef.getValue().trim();
			repositoryEntry.setExternalRef(ref);
		}
		
		String desc = description.getValue().trim();
		repositoryEntry.setDescription(desc);

		if(objectives != null) {
			String obj = objectives.getValue().trim();
			repositoryEntry.setObjectives(obj);
		}
		if(requirements != null) {
			String req = requirements.getValue().trim();
			repositoryEntry.setRequirements(req);
		}
		if(credits != null) {
			String cred = credits.getValue().trim();
			repositoryEntry.setCredits(cred);
		}

		repositoryEntry = repositoryManager.setDescriptionAndName(repositoryEntry,
				repositoryEntry.getDisplayname(), repositoryEntry.getExternalRef(), repositoryEntry.getAuthors(),
				repositoryEntry.getDescription(), repositoryEntry.getObjectives(), repositoryEntry.getRequirements(),
				repositoryEntry.getCredits(), repositoryEntry.getMainLanguage(), repositoryEntry.getLocation(),
				repositoryEntry.getExpenditureOfWork(), repositoryEntry.getLifecycle(), null, null);
		if(repositoryEntry == null) {
			showWarning("repositoryentry.not.existing");
			fireEvent(ureq, Event.CLOSE_EVENT);
		} else {
			fireEvent(ureq, new ReloadSettingsEvent(false, false, false, true));
			MultiUserEvent modifiedEvent = new EntryChangedEvent(repositoryEntry, getIdentity(), Change.modifiedDescription, "authoring");
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(modifiedEvent, RepositoryService.REPOSITORY_EVENT_ORES);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}