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
package org.olat.modules.project.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.OrganisationUIFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectImageType;
import org.olat.modules.project.ProjectCopyService;
import org.olat.modules.project.ProjectModule;
import org.olat.modules.project.ProjectService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjProjectEditController extends FormBasicController {
	
	private static final Set<String> IMAGE_MIME_TYPES = Set.of("image/gif", "image/jpg", "image/jpeg", "image/png");
	private static final OrganisationRoles[] ROLES_PROJECT_MANAGER = { OrganisationRoles.administrator,
			OrganisationRoles.projectmanager };
	private static final String TEMPLATE_KEY = "template";
	private static final String TEMPLATE_PUBLIC_KEY = "template.public";

	private StaticTextElement ownerEl;
	private FormLink ownerSelectLink;
	private MultipleSelectionElement templateEl;
	private TextElement titleEl;
	private TextElement externalRefEl;
	private TextElement teaserEl;
	private TextAreaElement descriptionEl;
	private FileElement avatarImageEl;
	private FileElement backgroundImageEl;
	private FormLayoutContainer orgCont;
	private MultiSelectionFilterElement organisationsEl;
	
	private CloseableModalController cmc;
	private UserSearchController userSearchCtrl;

	private final ProjProject initialProject;
	private final String initialTitle;
	private ProjProject targetProject;
	private final boolean readOnly;
	private final boolean creatorForEnabled;
	private final boolean copyArtefacts;
	private List<Organisation> organisations;
	private Identity owner;
	
	@Autowired
	private ProjectModule projectModule;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjectCopyService projectCopyService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public static ProjProjectEditController createCreateCtrl(UserRequest ureq, WindowControl wControl, boolean createForEnabled) {
		return new ProjProjectEditController(ureq, wControl, createForEnabled);
	}

	private ProjProjectEditController(UserRequest ureq, WindowControl wControl, boolean createForEnabled) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.initialProject = null;
		this.initialTitle = null;
		this.readOnly = false;
		this.creatorForEnabled = createForEnabled;
		this.copyArtefacts = false;
		this.owner = getIdentity();
		initForm(ureq);
	}
	
	public static ProjProjectEditController createEditCtrl(UserRequest ureq, WindowControl wControl, ProjProject project, boolean readOnly) {
		return new ProjProjectEditController(ureq, wControl, project, readOnly);
	}

	private ProjProjectEditController(UserRequest ureq, WindowControl wControl, ProjProject project, boolean readOnly) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.initialProject = project;
		this.initialTitle = project.getTitle();
		this.targetProject = project;
		this.readOnly = readOnly;
		this.creatorForEnabled = false;
		this.copyArtefacts = false;
		this.owner = getIdentity();
		initForm(ureq);
	}
	
	public static ProjProjectEditController createCopyCtrl(UserRequest ureq, WindowControl wControl, ProjProject initialProject) {
		return new ProjProjectEditController(ureq, wControl, initialProject);
	}
	
	private ProjProjectEditController(UserRequest ureq, WindowControl wControl, ProjProject initialProject) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.initialProject = initialProject;
		this.initialTitle = translate("project.title.copy", StringHelper.blankIfNull(initialProject.getTitle()));
		this.readOnly = false;
		this.creatorForEnabled = true;
		this.copyArtefacts = true;
		this.owner = getIdentity();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (creatorForEnabled) {
			ownerEl = uifactory.addStaticTextElement("project.create.for", "", formLayout);
			
			FormLayoutContainer ownerCont = FormLayoutContainer.createButtonLayout("ownerCont", getTranslator());
			ownerCont.setRootForm(mainForm);
			formLayout.add("ownerCont", ownerCont);
			
			ownerSelectLink = uifactory.addFormLink("project.owner.select", ownerCont, Link.BUTTON);
		}
		
		if (copyArtefacts) {
			SelectionValues templateSV = new SelectionValues();
			templateSV.add(SelectionValues.entry(TEMPLATE_KEY, translate("project.template.private")));
			templateSV.add(SelectionValues.entry(TEMPLATE_PUBLIC_KEY, translate("project.template.public")));
			templateEl = uifactory.addCheckboxesVertical("project.template", formLayout, templateSV.keys(), templateSV.values(), 1);
		}
		
		titleEl = uifactory.addTextElement("project.title", 100, initialTitle, formLayout);
		titleEl.setMandatory(true);
		titleEl.setEnabled(!readOnly);
		
		String externalRef = initialProject != null? initialProject.getExternalRef(): null;
		externalRefEl = uifactory.addTextElement("project.external.ref", 100, externalRef, formLayout);
		externalRefEl.setEnabled(!readOnly);
		
		String teaser = initialProject != null? initialProject.getTeaser(): null;
		teaserEl = uifactory.addTextElement("project.teaser", 150, teaser, formLayout);
		teaserEl.setEnabled(!readOnly);
		
		String description = initialProject != null? initialProject.getDescription(): null;
		descriptionEl = uifactory.addTextAreaElement("project.description", 4, 6, description, formLayout);
		descriptionEl.setEnabled(!readOnly);
		
		avatarImageEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "project.image.avatar", formLayout);
		avatarImageEl.setMaxUploadSizeKB(2048, null, null);
		avatarImageEl.setExampleKey("project.image.avatar.example", null);
		avatarImageEl.limitToMimeType(IMAGE_MIME_TYPES, "error.mimetype", new String[]{ IMAGE_MIME_TYPES.toString()} );
		avatarImageEl.setReplaceButton(true);
		avatarImageEl.setDeleteEnabled(true);
		avatarImageEl.setPreview(ureq.getUserSession(), true);
		avatarImageEl.addActionListener(FormEvent.ONCHANGE);
		avatarImageEl.setEnabled(!readOnly);
		VFSLeaf avatarImage = projectService.getProjectImage(initialProject, ProjProjectImageType.avatar);
		if (avatarImage instanceof LocalFileImpl localFile) {
			avatarImageEl.setInitialFile(localFile.getBasefile());
		}
		
		backgroundImageEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "project.image.background", formLayout);
		backgroundImageEl.setMaxUploadSizeKB(2048, null, null);
		backgroundImageEl.setExampleKey("project.image.background.example", null);
		backgroundImageEl.limitToMimeType(IMAGE_MIME_TYPES, "error.mimetype", new String[]{ IMAGE_MIME_TYPES.toString()} );
		backgroundImageEl.setReplaceButton(true);
		backgroundImageEl.setDeleteEnabled(true);
		backgroundImageEl.setPreview(ureq.getUserSession(), true);
		backgroundImageEl.addActionListener(FormEvent.ONCHANGE);
		backgroundImageEl.setEnabled(!readOnly);
		VFSLeaf backgroundImage = projectService.getProjectImage(initialProject, ProjProjectImageType.background);
		if (backgroundImage instanceof LocalFileImpl localFile) {
			backgroundImageEl.setInitialFile(localFile.getBasefile());
		}
		
		if (organisationModule.isEnabled()) {
			orgCont = FormLayoutContainer.createVerticalFormLayout("orgCont", getTranslator());
			orgCont.setRootForm(mainForm);
			formLayout.add(orgCont);
			
			if (!creatorForEnabled) {
				initOrganisations(ureq);
			}
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		if (readOnly) {
			uifactory.addFormCancelButton("close", buttonLayout, ureq, getWindowControl());
		} else {
			uifactory.addFormSubmitButton("save", buttonLayout);
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		}

		updateOwnerUI();
	}

	private void initOrganisations(UserRequest ureq) {
		if (orgCont == null) return;
		
		OrganisationRoles[] orgRoles;
		Set<OrganisationRoles> createRoles = projectModule.getCreateRoles();
		if (!createRoles.isEmpty()) {
			Set<OrganisationRoles> allRoles = new HashSet<>(createRoles);
			allRoles.addAll(Arrays.asList(ROLES_PROJECT_MANAGER));
			orgRoles = allRoles.stream().toArray(OrganisationRoles[]::new);
		} else {
			orgRoles = ROLES_PROJECT_MANAGER;
		}
		organisations = organisationService.getOrganisations(getIdentity(), ureq.getUserSession().getRoles(), orgRoles);
		List<Organisation> projectOrganisations = projectService.getOrganisations(initialProject);
		
		if (!projectOrganisations.isEmpty()) {
			for (Organisation projectOrganisation : projectOrganisations) {
				if (projectOrganisation != null && !organisations.contains(projectOrganisation)) {
					organisations.add(projectOrganisation);
				}
			}
		}
		
		SelectionValues orgSV = OrganisationUIFactory.createSelectionValues(organisations);
		organisationsEl = uifactory.addCheckboxesFilterDropdown("organisations", "project.organisations", orgCont, getWindowControl(), orgSV);
		organisationsEl.setMandatory(true);
		organisationsEl.setEnabled(!readOnly);
		projectOrganisations.forEach(organisation -> organisationsEl.select(organisation.getKey().toString(), true));
	}
	
	private void initUserOrganisations() {
		if (orgCont == null) return;
		
		organisations = organisationService.getOrganisations(owner, OrganisationRoles.user);
		
		orgCont.remove("organisations");
		SelectionValues orgSV = OrganisationUIFactory.createSelectionValues(organisations);
		organisationsEl = uifactory.addCheckboxesFilterDropdown("organisations", "project.organisations", orgCont, getWindowControl(), orgSV);
		organisationsEl.setMandatory(true);
		organisationsEl.setEnabled(!readOnly);
		organisations.forEach(organisation -> organisationsEl.select(organisation.getKey().toString(), true));
	}
	
	private void updateOwnerUI() {
		if (ownerEl != null && owner != null) {
			ownerEl.setValue(userManager.getUserDisplayName(owner.getKey()));
			if (creatorForEnabled && organisationModule.isEnabled()) {
				initUserOrganisations();
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (userSearchCtrl == source) {
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent)event;
				Identity choosenIdentity = singleEvent.getChosenIdentity();
				if (choosenIdentity != null) {
					owner = choosenIdentity;
					updateOwnerUI();
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(cmc);
		userSearchCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == ownerSelectLink) {
			doSelectOwner(ureq);
		} else if (source == avatarImageEl) {
			if (FileElementEvent.DELETE.equals(event.getCommand())) {
				avatarImageEl.setInitialFile(null);
				if (avatarImageEl.getUploadFile() != null) {
					avatarImageEl.reset();
				}
				avatarImageEl.clearError();
				markDirty();
			} else if (avatarImageEl.isUploadSuccess()) {
				avatarImageEl.clearError();
				markDirty();
			}
		} else if (source == backgroundImageEl) {
			if (FileElementEvent.DELETE.equals(event.getCommand())) {
				backgroundImageEl.setInitialFile(null);
				if (backgroundImageEl.getUploadFile() != null) {
					backgroundImageEl.reset();
				}
				backgroundImageEl.clearError();
				markDirty();
			} else if (backgroundImageEl.isUploadSuccess()) {
				backgroundImageEl.clearError();
				markDirty();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk =  super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if (!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		avatarImageEl.validate();
		backgroundImageEl.validate();
		
		if (organisationsEl != null) {
			organisationsEl.clearError();
			if (organisationsEl.getSelectedKeys().isEmpty()) {
				organisationsEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (targetProject == null) {
			targetProject = projectService.createProject(getIdentity(), owner);
		}
		
		boolean templatePrivate = false;
		boolean templatePublic = false;
		if (templateEl != null && templateEl.isVisible()) {
			templatePrivate = templateEl.getSelectedKeys().contains(TEMPLATE_KEY);
			templatePublic = templateEl.getSelectedKeys().contains(TEMPLATE_PUBLIC_KEY);
		}
		targetProject = projectService.updateProject(getIdentity(), targetProject,
				externalRefEl.getValue(),
				titleEl.getValue(),
				teaserEl.getValue(),
				descriptionEl.getValue(),
				templatePrivate,
				templatePublic);
		if (targetProject == null) {
			fireEvent(ureq, FormEvent.DONE_EVENT);
			return;
		}
		
		Collection<Organisation> selectedOrganisations = null;
		if (organisationsEl != null) {
			Collection<String> selectedOrgKeys = organisationsEl.getSelectedKeys();
			selectedOrganisations = organisations.stream()
					.filter(org -> selectedOrgKeys.contains(org.getKey().toString()))
					.collect(Collectors.toList());
		}
		// Call always the update method to init the default organisation.
		projectService.updateProjectOrganisations(getIdentity(), targetProject, selectedOrganisations);
		
		if (avatarImageEl.getUploadFile() != null) {
			projectService.storeProjectImage(getIdentity(), targetProject, ProjProjectImageType.avatar, avatarImageEl.getUploadFile(), avatarImageEl.getUploadFileName());
		} else if (copyArtefacts && avatarImageEl.getInitialFile() != null) {
			projectService.storeProjectImage(getIdentity(), targetProject, ProjProjectImageType.avatar, avatarImageEl.getInitialFile(), avatarImageEl.getInitialFile().getName());
		} else if (avatarImageEl.getInitialFile() == null) {
			projectService.deleteProjectImage(getIdentity(), targetProject, ProjProjectImageType.avatar);
		}
		
		if (backgroundImageEl.getUploadFile() != null) {
			projectService.storeProjectImage(getIdentity(), targetProject, ProjProjectImageType.background, backgroundImageEl.getUploadFile(), backgroundImageEl.getUploadFileName());
		} else if (copyArtefacts && backgroundImageEl.getInitialFile() != null) {
			projectService.storeProjectImage(getIdentity(), targetProject, ProjProjectImageType.background, backgroundImageEl.getInitialFile(), backgroundImageEl.getInitialFile().getName());
		} else if (backgroundImageEl.getInitialFile() == null) {
			projectService.deleteProjectImage(getIdentity(), targetProject, ProjProjectImageType.background);
		}
		
		if (copyArtefacts) {
			projectCopyService.copyProjectArtefacts(owner, initialProject, targetProject);
		}
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}
	
	private void doSelectOwner(UserRequest ureq) {
		if (guardModalController(userSearchCtrl)) return;
		
		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, false, false);
		listenTo(userSearchCtrl);
		
		String title = translate("project.owner.select.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}
