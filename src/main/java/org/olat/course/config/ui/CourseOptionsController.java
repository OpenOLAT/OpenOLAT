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
package org.olat.course.config.ui;

import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.ui.events.CalendarGUIModifiedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.course.config.CourseConfigEvent.CourseConfigType;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.RunMainController;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.fileresource.types.GlossaryResource;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.modules.glossary.GlossaryManager;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.resource.references.Reference;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseOptionsController extends FormBasicController {
	private static final OLog log = Tracing.createLoggerFor(CourseOptionsController.class);
	private static final String COMMAND_REMOVE = "command.glossary.remove";
	private static final String COMMAND_ADD = "command.glossary.add";
	
	private static final String[] onKeys = new String[] {"xx"};
	private final String[] onValues;

	private SelectionElement menuEl, toolbarEl, breadCrumbEl, calendarEl, searchEl, chatEl;
	private FormLink addGlossaryCommand, removeGlossaryCommand;
	private StaticTextElement glossaryNameEl;
	private FormLink saveButton;
	private FormLayoutContainer saveCont, calendarCont, searchCont, chatCont, glossaryCont, sharedFolderCont;
	
	private FormLink addFolderCommand, removeFolderCommand;
	private StaticTextElement folderNameEl;
	private MultipleSelectionElement folderReadOnlyEl;

	private LockResult lockEntry;
	private final boolean editable;
	private CourseConfig courseConfig;
	private final RepositoryEntry entry;
	private boolean hasFolderNode = false;


	private CloseableModalController cmc;
	private ReferencableEntriesSearchController glossarySearchCtr, folderSearchCtr;

	@Autowired
	private UserManager userManager;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private RepositoryManager repositoryService;
	private DialogBoxController folderRefRemoveWarnBox, folderRefAddWarnBox;

	/**
	 * @param name
	 * @param chatEnabled
	 */
	public CourseOptionsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, CourseConfig courseConfig, boolean canEdit) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(RunMainController.class, getLocale(), getTranslator()));
		this.courseConfig = courseConfig;
		this.entry = entry;
		this.onValues = new String[] {translate("on")};
		
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker()
				.acquireLock(entry.getOlatResource(), getIdentity(), CourseFactory.COURSE_EDITOR_LOCK);
		editable = (lockEntry != null && lockEntry.isSuccess()) && canEdit;

		initForm(ureq);
		updateToolbar();

		//glossary setup
		boolean managedGlossary = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.glossary);
		if (courseConfig.hasGlossary()) {
			RepositoryEntry repoEntry = repositoryService.lookupRepositoryEntryBySoftkey(courseConfig.getGlossarySoftKey(), false);
			if (repoEntry == null) {
				// Something is wrong here, maybe the glossary has been deleted. Try to
				// remove glossary from configuration
				doRemoveGlossary();
				log.warn("Course with ID::" + entry.getOlatResource().getResourceableId() + " had a config for a glossary softkey::"
						+ courseConfig.getGlossarySoftKey() + " but no such glossary was found");				
			} else if(editable) {
				glossaryNameEl.setValue(StringHelper.escapeHtml(repoEntry.getDisplayname()));
				glossaryNameEl.setUserObject(repoEntry);
				removeGlossaryCommand.setVisible(editable && !managedGlossary);
			}
		} else if(editable && !managedGlossary) {
			removeGlossaryCommand.setVisible(false);
			addGlossaryCommand.setVisible(editable);
		}
		
		//shared folder
		boolean managedFolder = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.resourcefolder);
		if (courseConfig.hasCustomSharedFolder()) {
			RepositoryEntry repoEntry = repositoryService.lookupRepositoryEntryBySoftkey(courseConfig.getSharedFolderSoftkey(), false);
			if (repoEntry == null) {
				// Something is wrong here, maybe the glossary has been deleted.
				// Try to remove shared folder from configuration
				doRemoveSharedFolder();			
			} else if(editable) {
				folderNameEl.setValue(StringHelper.escapeHtml(repoEntry.getDisplayname()));
				folderNameEl.setUserObject(repoEntry);
				removeFolderCommand.setVisible(editable && !managedFolder);
				
				RepositoryEntrySecurity reSecurity = repositoryService.isAllowed(ureq, repoEntry);
				folderReadOnlyEl.setVisible(true);
				folderReadOnlyEl.setEnabled(editable && reSecurity.isEntryAdmin());
			}
		} else if(editable && !managedFolder) {
			removeFolderCommand.setVisible(false);
			addFolderCommand.setVisible(editable);
			folderReadOnlyEl.setVisible(false);
		}
		
		if(lockEntry != null && !lockEntry.isSuccess()) {
			String lockerName = "???";
			if(lockEntry.getOwner() != null) {
				lockerName = userManager.getUserDisplayName(lockEntry.getOwner());
			}
			showWarning("error.editoralreadylocked", new String[] { lockerName });
		}
	}
	
	@Override
	protected void doDispose() {
		if (lockEntry != null && lockEntry.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FormLayoutContainer menuCont = FormLayoutContainer.createDefaultFormLayout("menutool", getTranslator());
		menuCont.setRootForm(mainForm);
		formLayout.add(menuCont);

		menuEl = uifactory.addCheckboxesHorizontal("menuIsOn", "chkbx.menu.onoff", menuCont, onKeys, onValues);
		menuEl.select(onKeys[0], courseConfig.isMenuEnabled());
		menuEl.addActionListener(FormEvent.ONCHANGE);
		menuEl.setEnabled(editable);
		
		toolbarEl = uifactory.addCheckboxesHorizontal("toolbarIsOn", "chkbx.toolbar.onoff", menuCont, onKeys, onValues);
		toolbarEl.select(onKeys[0], courseConfig.isToolbarEnabled());
		toolbarEl.addActionListener(FormEvent.ONCHANGE);

		breadCrumbEl = uifactory.addCheckboxesHorizontal("breadCrumbIsOn", "chkbx.breadcrumb.onoff", menuCont, onKeys, onValues);
		breadCrumbEl.select(onKeys[0], courseConfig.isBreadCrumbEnabled());
		breadCrumbEl.addActionListener(FormEvent.ONCHANGE);

		boolean canHideToolbar = true;
		if(calendarModule.isEnabled() && calendarModule.isEnableCourseToolCalendar()) {
			//calendar
			calendarCont = FormLayoutContainer.createDefaultFormLayout("cal", getTranslator());
			calendarCont.setRootForm(mainForm);
			formLayout.add(calendarCont);
			calendarCont.setFormContextHelp("Course Settings#_optionen");

			boolean calendarEnabled = courseConfig.isCalendarEnabled();
			boolean managedCal = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.calendar);
			calendarEl = uifactory.addCheckboxesHorizontal("calIsOn", "chkbx.calendar.onoff", calendarCont, onKeys, onValues);
			calendarEl.setElementCssClass("o_sel_course_options_calendar");
			calendarEl.addActionListener(FormEvent.ONCHANGE);
			calendarEl.select("xx", calendarEnabled);
			calendarEl.setEnabled(editable && !managedCal);
			
			if(managedCal && calendarEnabled) {
				canHideToolbar &= false;
			}
		}		
		//searchbar
		searchCont = FormLayoutContainer.createDefaultFormLayout("search", getTranslator());
		searchCont.setRootForm(mainForm);
		formLayout.add(searchCont);
		
		boolean searchEnabled = courseConfig.isCourseSearchEnabled();
		boolean managedSearch = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.search);
		searchEl = uifactory.addCheckboxesHorizontal("searchIsOn", "chkbx.search.onoff", searchCont, onKeys, onValues);
		searchEl.addActionListener(FormEvent.ONCHANGE);
		searchEl.select(onKeys[0], searchEnabled);
		searchEl.setEnabled(editable && !managedSearch);
		
		if(managedSearch && searchEnabled) {
			canHideToolbar &= false;
		}
		
		//chat
		chatCont = FormLayoutContainer.createDefaultFormLayout("chat", getTranslator());
		chatCont.setRootForm(mainForm);
		formLayout.add(chatCont);

		boolean chatEnabled = courseConfig.isChatEnabled();
		boolean managedChat = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.chat);
		chatEl = uifactory.addCheckboxesHorizontal("chatIsOn", "chkbx.chat.onoff", chatCont, onKeys, onValues);
		chatEl.addActionListener(FormEvent.ONCHANGE);
		chatEl.select("xx", chatEnabled);
		chatEl.setEnabled(editable && !managedChat);
		
		if(managedChat && chatEnabled) {
			canHideToolbar &= false;
		}
		
		//glossary
		glossaryCont = FormLayoutContainer.createDefaultFormLayout("glossary", getTranslator());
		glossaryCont.setRootForm(mainForm);
		formLayout.add(glossaryCont);

		glossaryNameEl = uifactory.addStaticTextElement("glossaryName", "glossary.isconfigured",
				translate("glossary.no.glossary"), glossaryCont);

		boolean managedGlossary = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.glossary);
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		glossaryCont.add(buttonsCont);
		removeGlossaryCommand = uifactory.addFormLink(COMMAND_REMOVE, buttonsCont, Link.BUTTON);
		removeGlossaryCommand.setVisible(editable && !managedGlossary);
		addGlossaryCommand = uifactory.addFormLink(COMMAND_ADD, buttonsCont, Link.BUTTON);
		addGlossaryCommand.setVisible(editable && !managedGlossary);
		
		if(managedGlossary && StringHelper.containsNonWhitespace(courseConfig.getGlossarySoftKey())) {
			canHideToolbar &= false;
		}
		toolbarEl.setEnabled(editable && canHideToolbar);
		breadCrumbEl.setEnabled(editable && canHideToolbar); //same rule as for toolbar
		
		//shared folder
		boolean managedFolder = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.resourcefolder);
		sharedFolderCont = FormLayoutContainer.createDefaultFormLayout("sharedfolder", getTranslator());
		sharedFolderCont.setRootForm(mainForm);
		formLayout.add(sharedFolderCont);

		folderNameEl = uifactory.addStaticTextElement("folderName", "sf.resourcetitle",
				translate("sf.notconfigured"), sharedFolderCont);
		folderNameEl.setHelpText(translate("sf.resourcetitle.helptext"));
		folderNameEl.setHelpUrlForManualPage("Course Settings#_detail_ressourcen");
		
		String[] readOnlyValues = new String[]{ translate("sf.resource.readonly") };
		folderReadOnlyEl = uifactory.addCheckboxesHorizontal("sf.resource.readonly", sharedFolderCont, onKeys, readOnlyValues);
		folderReadOnlyEl.addActionListener(FormEvent.ONCHANGE);
		folderReadOnlyEl.setLabel(null, null);
		folderReadOnlyEl.setEnabled(false);
		if(courseConfig.isSharedFolderReadOnlyMount()) {
			folderReadOnlyEl.select(onKeys[0], true);
		}
		
		FormLayoutContainer buttons2Cont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		sharedFolderCont.add(buttons2Cont);
		
		removeFolderCommand = uifactory.addFormLink("sf.unselectsfresource", buttons2Cont, Link.BUTTON);
		removeFolderCommand.setVisible(editable && !managedFolder);
		addFolderCommand = uifactory.addFormLink("sf.changesfresource", buttons2Cont, Link.BUTTON);
		addFolderCommand.setVisible(editable && !managedFolder);

		if(editable) {
			saveCont = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
			saveCont.setRootForm(mainForm);
			formLayout.add(saveCont);
			saveButton = uifactory.addFormLink("save", saveCont, Link.BUTTON);
			saveButton.setElementCssClass("o_sel_course_options_save");
			saveButton.setPrimary(true);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == glossarySearchCtr) {
			cmc.deactivate();
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry repoEntry = glossarySearchCtr.getSelectedEntry();
				doSelectGlossary(repoEntry);
				setSaveButtonDirty();
			}
			cleanUp();
		} else if (source == folderSearchCtr) {
			cmc.deactivate();
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry repoEntry = folderSearchCtr.getSelectedEntry();
				doSelectSharedFolder(ureq, repoEntry);
				setSaveButtonDirty();
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if(source == folderRefRemoveWarnBox) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doRemoveSharedFolder();
				setSaveButtonDirty();
			}
		} else if(source == folderRefAddWarnBox) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				folderSearchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq, SharedFolderFileResource.TYPE_NAME, translate("select"));
				listenTo(folderSearchCtr);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), folderSearchCtr.getInitialComponent());
				listenTo(cmc);
				cmc.activate();
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(glossarySearchCtr);
		removeAsListenerAndDispose(folderSearchCtr);
		removeAsListenerAndDispose(cmc);
		glossarySearchCtr = null;
		folderSearchCtr = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addGlossaryCommand) {
			glossarySearchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq, GlossaryResource.TYPE_NAME, translate("select"));			
			listenTo(glossarySearchCtr);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), glossarySearchCtr.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		} else if (source == removeGlossaryCommand) {
			doRemoveGlossary();
			setSaveButtonDirty();
		} else if (source == addFolderCommand) {
			if(checkForFolderNodesAdd(ureq)  ){
				folderSearchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq, SharedFolderFileResource.TYPE_NAME, translate("select"));
				listenTo(folderSearchCtr);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), folderSearchCtr.getInitialComponent());
				listenTo(cmc);
				cmc.activate();
			}
		} else if (source == removeFolderCommand) {
			if(checkForFolderNodesRemove(ureq)){
				doRemoveSharedFolder();
				setSaveButtonDirty();
			}
		} else if(toolbarEl == source) {
			if(!toolbarEl.isSelected(0)) {
				showWarning("chkbx.toolbar.off.warning");
			}
			updateToolbar();
			setSaveButtonDirty();
		} else if(breadCrumbEl == source) {
			setSaveButtonDirty();
		} else if (source instanceof SelectionElement || source == folderReadOnlyEl || source == menuEl) {
			setSaveButtonDirty();
		}  else if(saveButton == source) {
			doSave(ureq);
		}
	}
	
	private void updateToolbar() {
		boolean enabled = toolbarEl.isSelected(0);
		if(calendarCont != null) {
			calendarCont.setVisible(enabled);
		}
		chatCont.setVisible(enabled);
		searchCont.setVisible(enabled);
		glossaryCont.setVisible(enabled);
	}


	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}

	private boolean checkForFolderNodesAdd(UserRequest ureq) {
		OLATResourceable courseOres = entry.getOlatResource();
		ICourse course = CourseFactory.loadCourse(courseOres.getResourceableId());
		CourseNode rootNode = course.getCourseEnvironment().getRunStructure().getRootNode();
		if(checkFolderNodes(rootNode, course)&& folderNameEl.getUserObject() != null){
			folderRefAddWarnBox = activateYesNoDialog(ureq, translate("warning.folderRef.title"),	"<div class=\"o_error\">"+translate("warning.folderRefAdd")+"</div>", folderRefAddWarnBox);
			folderRefAddWarnBox.setCssClass("o_icon_warn");
			return false;
		}
		return true;
	}

	private boolean checkForFolderNodesRemove(UserRequest ureq) {
		OLATResourceable courseOres = entry.getOlatResource();
		ICourse course = CourseFactory.loadCourse(courseOres.getResourceableId());
		CourseNode rootNode = course.getCourseEnvironment().getRunStructure().getRootNode();

		if(checkFolderNodes(rootNode, course)){
			folderRefRemoveWarnBox = activateYesNoDialog(ureq, translate("warning.folderRef.title"),	"<div class=\"o_error\">"+translate("warning.folderRef")+"</div>", folderRefRemoveWarnBox);
			return false;
		}else{
			return true;
		}
	}

	private boolean checkFolderNodes(INode rootNode, ICourse course){
		hasFolderNode = false;
		Visitor visitor = new Visitor() {
			public void visit(INode node) {
				CourseEditorTreeNode courseNode = (CourseEditorTreeNode) course.getEditorTreeModel().getNodeById(node.getIdent());
				if(!courseNode.isDeleted() && courseNode.getCourseNode() instanceof BCCourseNode){
					BCCourseNode bcNode = (BCCourseNode) courseNode.getCourseNode();
					if (bcNode.isSharedFolder()) {
						hasFolderNode = true;
					}
				}
			}
		};

		TreeVisitor v = new TreeVisitor(visitor, rootNode, false);
		v.visitAll();
		return hasFolderNode;
	}

	private void setSaveButtonDirty() {
		if(saveButton != null) {
			saveButton.setCustomEnabledLinkCSS("btn btn-primary o_button_dirty");
		}
	}
	
	private void doSave(UserRequest ureq) {
		doChangeConfig(ureq);
		saveButton.setCustomEnabledLinkCSS("btn btn-primary");
	}
	
	private void doChangeConfig(UserRequest ureq) {
		OLATResourceable courseOres = entry.getOlatResource();
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		courseConfig = course.getCourseEnvironment().getCourseConfig();
		
		boolean menuEnabled = menuEl.isSelected(0);
		courseConfig.setMenuEnabled(menuEnabled);
		boolean toolbarEnabled = toolbarEl.isSelected(0);
		courseConfig.setToolbarEnabled(toolbarEnabled);
		boolean breadCrumbEnabled = breadCrumbEl.isSelected(0);
		courseConfig.setBreadCrumbEnabled(breadCrumbEnabled);
		
		boolean enableSearch = searchEl.isSelected(0);
		boolean updateSearch = courseConfig.isCourseSearchEnabled() != enableSearch;
		courseConfig.setCourseSearchEnabled(enableSearch && toolbarEnabled);
		
		boolean enableChat = chatEl.isSelected(0);
		boolean updateChat = courseConfig.isChatEnabled() != enableChat;
		courseConfig.setChatIsEnabled(enableChat && toolbarEnabled);
		
		boolean enableCalendar = calendarEl == null ? false : calendarEl.isSelected(0);
		boolean updateCalendar = courseConfig.isCalendarEnabled() != enableCalendar && calendarModule.isEnableCourseToolCalendar();
		courseConfig.setCalendarEnabled(enableCalendar && toolbarEnabled);
		
		String currentGlossarySoftKey = courseConfig.getGlossarySoftKey();
		RepositoryEntry glossary = (RepositoryEntry)glossaryNameEl.getUserObject();
		String newGlossarySoftKey = (glossary == null || !toolbarEnabled) ? null : glossary.getSoftkey();
		boolean updateGlossary = (currentGlossarySoftKey == null && newGlossarySoftKey != null)
			|| (currentGlossarySoftKey != null && newGlossarySoftKey == null)
			|| (newGlossarySoftKey != null && !newGlossarySoftKey.equals(currentGlossarySoftKey));

		courseConfig.setGlossarySoftKey(newGlossarySoftKey);
		
		
		String currentFolderSoftKey = courseConfig.getSharedFolderSoftkey();
		RepositoryEntry folder = (RepositoryEntry)folderNameEl.getUserObject();
		String newFolderSoftKey = folder == null ? null : folder.getSoftkey();
		boolean updateFolder = (currentFolderSoftKey == null && newFolderSoftKey != null)
				|| (currentFolderSoftKey != null && newFolderSoftKey == null)
				|| (currentFolderSoftKey != null && !currentFolderSoftKey.equals(newFolderSoftKey));

		courseConfig.setSharedFolderSoftkey(newFolderSoftKey);
		if(folderReadOnlyEl.isEnabled()) {
			courseConfig.setSharedFolderReadOnlyMount(folderReadOnlyEl.isAtLeastSelected(1));
		} else {
			courseConfig.setSharedFolderReadOnlyMount(true);
		}

		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		
		if(updateSearch) {
			ILoggingAction loggingAction =  enableSearch ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_COURSESEARCH_ENABLED :
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_COURSESEARCH_DISABLED;
	  		ThreadLocalUserActivityLogger.log(loggingAction, getClass());
	  		
	        CoordinatorManager.getInstance().getCoordinator().getEventBus()
        		.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.search, course.getResourceableId()), course);
		}

		if(updateChat) {
			ILoggingAction loggingAction =  enableChat ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_IM_ENABLED :
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_IM_DISABLED;
	  		ThreadLocalUserActivityLogger.log(loggingAction, getClass());

	        CoordinatorManager.getInstance().getCoordinator().getEventBus()
	        	.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.chat, course.getResourceableId()), course);
		}
		
		if(updateCalendar) {
			ILoggingAction loggingAction = enableCalendar ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_CALENDAR_ENABLED :
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_CALENDAR_DISABLED;

			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
	        CoordinatorManager.getInstance().getCoordinator().getEventBus()
	        	.fireEventToListenersOf(new CalendarGUIModifiedEvent(), OresHelper.lookupType(CalendarManager.class));
	        CoordinatorManager.getInstance().getCoordinator().getEventBus()
	        	.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.calendar, course.getResourceableId()), course);
		}
		
		if(updateGlossary) {
			ILoggingAction loggingAction = (newGlossarySoftKey == null) ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_GLOSSARY_DISABLED :
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_GLOSSARY_ENABLED;
			
			LoggingResourceable lri = null;
			if(newGlossarySoftKey != null) {
				lri = LoggingResourceable.wrapNonOlatResource(StringResourceableType.glossarySoftKey, newGlossarySoftKey, newGlossarySoftKey);
			} else if (currentGlossarySoftKey != null) {
				lri = LoggingResourceable.wrapNonOlatResource(StringResourceableType.glossarySoftKey, currentGlossarySoftKey, currentGlossarySoftKey);
			}
			if (lri != null) {
				ThreadLocalUserActivityLogger.log(loggingAction, getClass(), lri);
			}

			// remove references
			List<Reference> repoRefs = referenceManager.getReferences(course);
			for (Reference ref:repoRefs) {
				if (ref.getUserdata().equals(GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER)) {
					referenceManager.delete(ref);
				}
			}
			// update references
			if(glossary != null) {
				referenceManager.addReference(course, glossary.getOlatResource(), GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER); 
			}

	        CoordinatorManager.getInstance().getCoordinator().getEventBus()
	        	.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.glossary, course.getResourceableId()), course);
		}
		
		if(updateFolder) {
			List<Reference> repoRefs = referenceManager.getReferences(course);
			for (Reference ref:repoRefs) {
				if (ref.getUserdata().equals(SharedFolderManager.SHAREDFOLDERREF)) {
					referenceManager.delete(ref);
				}
			}

			if(folder != null) {
				referenceManager.addReference(course, folder.getOlatResource(), SharedFolderManager.SHAREDFOLDERREF);
				ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_SHARED_FOLDER_REMOVED,
						getClass(), LoggingResourceable.wrapBCFile(folder.getDisplayname()));
			} else {
				ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_SHARED_FOLDER_ADDED,
						getClass(), LoggingResourceable.wrapBCFile(""));
			}
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	/**
	 * Updates config with selected glossary
	 * 
	 * @param repoEntry
	 * @param ureq
	 */
	private void doSelectGlossary(RepositoryEntry repoEntry) {
		glossaryNameEl.setValue(StringHelper.escapeHtml(repoEntry.getDisplayname()));
		glossaryNameEl.setUserObject(repoEntry);
		removeGlossaryCommand.setVisible(true);
	}

	/**
	 * Removes the current glossary from the configuration
	 * 
	 * @param ureq
	 */
	private void doRemoveGlossary() {			
		glossaryNameEl.setValue(translate("glossary.no.glossary"));
		glossaryNameEl.setUserObject(null);
		removeGlossaryCommand.setVisible(false);
	}
	
	private void doSelectSharedFolder(UserRequest ureq, RepositoryEntry repoEntry) {
		folderNameEl.setValue(StringHelper.escapeHtml(repoEntry.getDisplayname()));
		folderNameEl.setUserObject(repoEntry);
		removeFolderCommand.setVisible(true);
		
		RepositoryEntrySecurity reSecurity = repositoryService.isAllowed(ureq, repoEntry);
		folderReadOnlyEl.setVisible(true);
		folderReadOnlyEl.setEnabled(reSecurity.isEntryAdmin());
		folderReadOnlyEl.select(onKeys[0], true);
		sharedFolderCont.setDirty(true);
	}
	
	private void doRemoveSharedFolder() {			
		folderNameEl.setValue(translate("sf.notconfigured"));
		folderNameEl.setUserObject(null);
		removeFolderCommand.setVisible(false);
		folderReadOnlyEl.setVisible(false);
	}

}