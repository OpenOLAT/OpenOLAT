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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.FormSection;
import org.olat.core.gui.components.form.flexible.impl.elements.DeleteFileElementEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.util.SelectionValues;
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
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.RepositoryEntryCreditPointConfiguration;
import org.olat.modules.curriculum.TaughtBy;
import org.olat.modules.edusharing.EdusharingProvider;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.ui.author.MediaContainerFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryInfoController extends FormBasicController {
	
	private static final Set<String> imageMimeTypes = Set.of("image/gif", "image/jpg", "image/jpeg", "image/png");
	private static final Set<String> videoMimeTypes = Set.of("video/mp4");

	
	private static final int picUploadlimitKB = 5120;
	private static final int movieUploadlimitKB = 102400;

	private static final String EVENTS_KEY = "events";
	private static final String MEET_TEACHERS_KEY = "meetteachers";
	private static final String CERTIFICATE_KEY = "certificate";
	private static final String CREDIT_POINTS_KEY = "creditpoints";

	private final boolean readOnly;
	private VFSContainer mediaContainer;
	private RepositoryEntry repositoryEntry;
	private boolean creditPointsAvailable;

	private FileElement fileUpload;
	private FormToggle withMovieEl;
	private FileElement movieUpload;
	private TextElement externalRef;
	private TextElement displayName;
	private TextElement teaser;
	private RichTextElement description;
	private MultipleSelectionElement showInfoEl;
	private MultipleSelectionElement taughtByEl;
	private RichTextElement objectives;
	private RichTextElement requirements;
	private RichTextElement credits;

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	@Autowired
	private CreditPointService creditPointService;
	@Autowired
	private LectureService lectureService;


	/**
	 * Create a repository add controller that adds the given resourceable.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param sourceEntry
	 */
	public RepositoryEntryInfoController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl);
		setBasePackage(RepositoryService.class);
		this.repositoryEntry = entry;
		this.readOnly = readOnly;
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
		setFormContextHelp("manual_user/learningresources/Course_Settings_Info/");
		formLayout.setElementCssClass("o_sel_edit_repositoryentry");
		setFormTitle("details.info.title");

		displayName = uifactory.addTextElement("cif.displayname", "cif.displayname", 100, repositoryEntry.getDisplayname(), formLayout);
		displayName.setDisplaySize(30);
		displayName.setMandatory(true);
		displayName.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.title) && !readOnly);

		String extRef = repositoryEntry.getExternalRef();
		if(StringHelper.containsNonWhitespace(repositoryEntry.getManagedFlagsString()) || readOnly) {
			if(StringHelper.containsNonWhitespace(extRef)) {
				uifactory.addStaticTextElement("cif.externalref", extRef, formLayout);
			}
		} else {
			externalRef = uifactory.addTextElement("cif.externalref", "cif.externalref", 255, extRef, formLayout);
			externalRef.setHelpText(translate("cif.externalref.hover"));
			externalRef.setHelpUrlForManualPage("manual_user/learningresources/Course_Settings_Info/");
		}

		boolean managed = RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);

		VFSLeaf img = repositoryManager.getImage(repositoryEntry);
		fileUpload = uifactory.addFileElement(getWindowControl(), getIdentity(), "rentry.pic", "rentry.pic", formLayout);
		fileUpload.setExampleKey("rentry.pic.example", new String[] {RepositoryManager.PICTURE_WIDTH + "x" + (RepositoryManager.PICTURE_HEIGHT)});
		fileUpload.setMaxUploadSizeKB(picUploadlimitKB, null, null);
		fileUpload.setPreview(usess, true);
		fileUpload.setShowInputIfFileUploaded(false);
		fileUpload.addActionListener(FormEvent.ONCHANGE);
		fileUpload.setDeleteEnabled(!managed);
		if(img instanceof LocalFileImpl) {
			fileUpload.setPreview(usess, true);
			fileUpload.setInitialFile(((LocalFileImpl)img).getBasefile());
		}
		fileUpload.setVisible(!managed && !readOnly);
		fileUpload.limitToMimeType(imageMimeTypes, "error.mimetype", new String[]{ imageMimeTypes.toString()} );

		VFSLeaf movie = repositoryService.getIntroductionMovie(repositoryEntry);
		withMovieEl = uifactory.addToggleButton("with.teaser.movie", "cif.with.teaser.movie", translate("on"), translate("off"), formLayout);
		withMovieEl.setEnabled(!managed && !readOnly);
		withMovieEl.addActionListener(FormEvent.ONCHANGE);
		withMovieEl.toggle(movie != null);

		movieUpload = uifactory.addFileElement(getWindowControl(), getIdentity(), "rentry.movie", "rentry.movie", formLayout);
		movieUpload.setExampleKey("rentry.movie.example", new String[] {"3:2"});
		movieUpload.setMaxUploadSizeKB(movieUploadlimitKB, null, null);
		movieUpload.setPreview(usess, true);
		movieUpload.setShowInputIfFileUploaded(false);
		movieUpload.addActionListener(FormEvent.ONCHANGE);
		movieUpload.setDeleteEnabled(!managed);
		if(movie instanceof LocalFileImpl) {
			movieUpload.setPreview(usess, true);
			movieUpload.setInitialFile(((LocalFileImpl)movie).getBasefile());
		}
		movieUpload.setVisible(!managed && !readOnly && withMovieEl.isOn());
		movieUpload.limitToMimeType(videoMimeTypes, "error.mimetype", new String[]{ videoMimeTypes.toString()} );

		teaser = uifactory.addTextElement("cif.teaser", "cif.teaser", 150, repositoryEntry.getTeaser(), formLayout);
		teaser.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.teaser) && !readOnly);

		RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(repositoryEntry);
		mediaContainer = handler.getMediaContainer(repositoryEntry);
		if(mediaContainer != null && mediaContainer.getName().equals("media")) {
			mediaContainer = mediaContainer.getParentContainer();
			mediaContainer.setDefaultItemFilter(new MediaContainerFilter(mediaContainer));
		}

		String desc = (repositoryEntry.getDescription() != null ? repositoryEntry.getDescription() : " ");
		description = uifactory.addRichTextElementForStringData("cif.description", "cif.description",
				desc, 10, -1, false, mediaContainer, null, formLayout, usess, getWindowControl());
		description.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.description) && !readOnly);
		description.getEditorConfiguration().setFileBrowserUploadRelPath("media");
		description.getEditorConfiguration().setPathInStatusBar(false);
		EdusharingProvider provider = new RepositoryEdusharingProvider(repositoryEntry, "repository-info");
		description.getEditorConfiguration().enableEdusharing(getIdentity(), provider);

		if(CourseModule.getCourseTypeName().equals(repositoryEntry.getOlatResource().getResourceableTypeName())) {
			initCourse(formLayout, usess, ureq);
		}

		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
		formLayout.add("buttonContainer", buttonContainer);
		buttonContainer.setElementCssClass("o_sel_repo_save_details");
		buttonContainer.setVisible(!readOnly);
		FormSubmit submit = uifactory.addFormSubmitButton("submit", buttonContainer);
		submit.setVisible(!managed && !readOnly);
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
	}
	
	private void initCourse(FormItemContainer formLayout, UserSession usess, UserRequest ureq) {
		FormSection displayCont = uifactory.addFormSection("display", translate("cif.display.settings"), formLayout, FormSection.Level.SUB_TITLE);

		SelectionValues showInfoPK = new SelectionValues();
		showInfoPK.add(SelectionValues.entry(EVENTS_KEY, translate("cif.events")));
		showInfoPK.add(SelectionValues.entry(MEET_TEACHERS_KEY, translate("cif.meet.your.teachers")));
		showInfoPK.add(SelectionValues.entry(CERTIFICATE_KEY, translate("details.certificate")));
		RepositoryEntryCreditPointConfiguration creditPointConfig = creditPointService.getConfiguration(repositoryEntry);
		creditPointsAvailable = creditPointConfig != null && creditPointConfig.isEnabled();
		if(creditPointsAvailable) {
			showInfoPK.add(SelectionValues.entry(CREDIT_POINTS_KEY, translate("details.benefits.credit.points")));
		}
		showInfoEl = uifactory.addCheckboxesVertical("show.info", "cif.display.on.info.page", displayCont,
				showInfoPK.keys(), showInfoPK.values(), 1);
		showInfoEl.setHelpText(translate("cif.display.on.info.page.help"));
		showInfoEl.addActionListener(FormEvent.ONCLICK);
		showInfoEl.setEnabled(EVENTS_KEY, !readOnly && !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.showLectures));
		showInfoEl.setEnabled(MEET_TEACHERS_KEY, !readOnly && !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.taughtBy));
		showInfoEl.setEnabled(CERTIFICATE_KEY, !readOnly && !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.showCertificate));
		if(creditPointsAvailable) {
			showInfoEl.setEnabled(CREDIT_POINTS_KEY, !readOnly && !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.showCreditPoints));
		}
		boolean meetTeachers = !repositoryEntry.getTaughtBys().isEmpty();
		showInfoEl.select(EVENTS_KEY, repositoryEntry.isShowLectures());
		showInfoEl.select(MEET_TEACHERS_KEY, meetTeachers);
		showInfoEl.select(CERTIFICATE_KEY, repositoryEntry.isShowCertificateBenefit());
		if(creditPointsAvailable) {
			showInfoEl.select(CREDIT_POINTS_KEY, repositoryEntry.isShowCreditPointsBenefit());
		}

		List<LectureBlock> lectureBlocks = lectureService.isRepositoryEntryLectureEnabled(repositoryEntry)
				? lectureService.getLectureBlocks(repositoryEntry) : List.of();
		Map<TaughtBy,Integer> taughtByCounts = getTaughtByCounts(lectureBlocks);
		SelectionValues taughtBySV = new SelectionValues();
		TaughtBy.ALL.forEach(taughtBy -> taughtBySV.add(SelectionValues.entry(
				taughtBy.name(),
				translate("cif.taught.by." + taughtBy.name(), String.valueOf(taughtByCounts.getOrDefault(taughtBy, Integer.valueOf(0)))))));
		taughtByEl = uifactory.addCheckboxesVertical("taught.by", "cif.taught.by", displayCont, taughtBySV.keys(), taughtBySV.values(), 1);
		taughtByEl.setHelpText(translate("cif.taught.by.help"));
		taughtByEl.setEnabled(!readOnly && !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.taughtBy));
		repositoryEntry.getTaughtBys().forEach(taughtBy -> taughtByEl.select(taughtBy.name(), true));
		taughtByEl.setVisible(meetTeachers);

		FormSection detailsCont = uifactory.addFormSection("details", translate("cif.details"), formLayout, FormSection.Level.SUB_TITLE);
		detailsCont.setCollapsible(true);
		detailsCont.setCollapsed(true);
		detailsCont.setPersistedStatusId(ureq, "repositoryentry.infos.details");

		String obj = (repositoryEntry.getObjectives() != null ? repositoryEntry.getObjectives() : " ");
		objectives = uifactory.addRichTextElementForStringData("cif.objectives", "cif.objectives",
				obj, 10, -1, false, mediaContainer, null, detailsCont, usess, getWindowControl());
		objectives.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.objectives));
		objectives.getEditorConfiguration().setFileBrowserUploadRelPath("media");
		objectives.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		objectives.setEnabled(!readOnly);

		String req = (repositoryEntry.getRequirements() != null ? repositoryEntry.getRequirements() : " ");
		requirements = uifactory.addRichTextElementForStringData("cif.requirements", "cif.requirements",
				req, 10, -1,  false, mediaContainer, null, detailsCont, usess, getWindowControl());
		requirements.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.requirements));
		requirements.getEditorConfiguration().setFileBrowserUploadRelPath("media");
		requirements.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		requirements.setMaxLength(2000);
		requirements.setEnabled(!readOnly);

		String cred = (repositoryEntry.getCredits() != null ? repositoryEntry.getCredits() : " ");
		credits = uifactory.addRichTextElementForStringData("cif.credits", "cif.credits",
				cred, 10, -1,  false, mediaContainer, null, detailsCont, usess, getWindowControl());
		credits.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.credits));
		credits.getEditorConfiguration().setFileBrowserUploadRelPath("media");
		credits.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		credits.setMaxLength(2000);
		credits.setEnabled(!readOnly);
	}

	private Map<TaughtBy, Integer> getTaughtByCounts(List<LectureBlock> lectureBlocks) {
		Map<TaughtBy, Integer> taughtByCount = new HashMap<>(TaughtBy.ALL.size());
		taughtByCount.put(TaughtBy.coaches, Integer.valueOf(
				repositoryService.getMembers(repositoryEntry, RepositoryEntryRelationType.all, GroupRoles.coach.name()).size()));
		taughtByCount.put(TaughtBy.owners, Integer.valueOf(
				repositoryService.getMembers(repositoryEntry, RepositoryEntryRelationType.all, GroupRoles.owner.name()).size()));
		taughtByCount.put(TaughtBy.teachers, Integer.valueOf(lectureService.getTeachers(lectureBlocks).size()));
		return taughtByCount;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= RepositoyUIFactory.validateTextElement(displayName, true, 110);
		allOk &= RepositoyUIFactory.validateTextElement(description, false, 80000);
		allOk &= RepositoyUIFactory.validateTextElement(objectives, false, 2000);
		allOk &= RepositoyUIFactory.validateTextElement(requirements, false, 2000);
		allOk &= RepositoyUIFactory.validateTextElement(credits, false, 2000);
		allOk &= RepositoyUIFactory.validateTextElement(externalRef, false, 255);
		allOk &= RepositoyUIFactory.validateTextElement(teaser, false, 200);

		if(taughtByEl != null) {
			taughtByEl.clearError();
			if(taughtByEl.isVisible() && taughtByEl.isEnabled() && taughtByEl.getSelectedKeys().isEmpty()) {
				taughtByEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == showInfoEl) {
			taughtByEl.setVisible(showInfoEl.getSelectedKeys().contains(MEET_TEACHERS_KEY));
		} else if (source == withMovieEl) {
			movieUpload.setVisible(withMovieEl.isOn());
		} else if (source == fileUpload) {
			if(DeleteFileElementEvent.DELETE.equals(event.getCommand())) {
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
			if(DeleteFileElementEvent.DELETE.equals(event.getCommand())) {
				movieUpload.clearError();
				VFSLeaf movie = repositoryService.getIntroductionMovie(repositoryEntry);
				if(movieUpload.getUploadFile() != null && movieUpload.getUploadFile() != movieUpload.getInitialFile()) {
					movieUpload.reset();
					if(movie != null) {
						movieUpload.setInitialFile(((LocalFileImpl)movie).getBasefile());
					}
				} else if(movie != null) {
					movie.deleteSilently();
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
			boolean ok = repositoryManager.setImage(newFile, repositoryEntry, getIdentity());
			if (!ok) {
				showWarning("cif.error.image");
			} else {
				VFSLeaf image = repositoryManager.getImage(repositoryEntry);
				if(image instanceof  LocalFileImpl localImage) {
					fileUpload.setInitialFile(localImage.getBasefile());
					fileUpload.reset();
				}
			}
			tmpContainer.deleteSilently();
		}

		if(!withMovieEl.isOn()) {
			VFSLeaf movie = repositoryService.getIntroductionMovie(repositoryEntry);
			if(movie != null) {
				movie.deleteSilently();
			}
		} else {
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
		}

		String displayname = displayName.getValue().trim();
		repositoryEntry.setDisplayname(displayname);

		if(externalRef != null && externalRef.isEnabled()) {
			String ref = externalRef.getValue().trim();
			repositoryEntry.setExternalRef(ref);
		}
		
		repositoryEntry.setTeaser(teaser.getValue());
		
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
				repositoryEntry.getDescription(), repositoryEntry.getTeaser(), repositoryEntry.getObjectives(),
				repositoryEntry.getRequirements(), repositoryEntry.getCredits(), repositoryEntry.getMainLanguage(),
				repositoryEntry.getLocation(), repositoryEntry.getExpenditureOfWork(), repositoryEntry.getLifecycle(),
				null, null, repositoryEntry.getEducationalType());
		if(repositoryEntry == null) {
			showWarning("repositoryentry.not.existing");
			fireEvent(ureq, Event.CLOSE_EVENT);
		} else {
			if(showInfoEl != null) {
				Collection<String> selectedInfo = showInfoEl.getSelectedKeys();
				String taughtByValue = selectedInfo.contains(MEET_TEACHERS_KEY)
						? TaughtBy.join(taughtByEl.getSelectedKeys().stream().map(TaughtBy::valueOf).collect(Collectors.toSet()))
						: null;
				boolean showCreditPoints = creditPointsAvailable
						? selectedInfo.contains(CREDIT_POINTS_KEY)
						: repositoryEntry.isShowCreditPointsBenefit();
				repositoryEntry = repositoryManager.setInfoPageSettings(repositoryEntry, selectedInfo.contains(EVENTS_KEY),
						selectedInfo.contains(CERTIFICATE_KEY), showCreditPoints, taughtByValue);
			}
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