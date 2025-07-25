/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.author;

import static org.olat.core.gui.components.util.SelectionValues.VALUE_ASC;
import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.admin.help.ui.HelpAdminController;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.help.HelpLinkSPI;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseRenderer;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.SpacerItem;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl.SelectionMode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFilterTabPosition;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.archiver.wizard.BulkCoursesArchivesContext;
import org.olat.course.archiver.wizard.BulkCoursesArchivesFinishStepCallback;
import org.olat.course.archiver.wizard.BulkCoursesArchives_1_RepositoryEntriesStep;
import org.olat.course.config.CourseConfig;
import org.olat.course.core.CourseElement;
import org.olat.course.core.CourseElementSearchParams;
import org.olat.course.core.CourseNodeService;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessProviderIdentifier;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.ui.MigrationSelectionController;
import org.olat.course.nodeaccess.ui.UnsupportedCourseNodesController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.st.assessment.STLearningPathConfigs;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.login.LoginModule;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementWithParents;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.LifecycleModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExportLinkEnum;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.bulk.RepositoryBulkService;
import org.olat.repository.bulk.SettingsBulkEditables;
import org.olat.repository.bulk.model.SettingsContext;
import org.olat.repository.bulk.ui.SettingsStepsStep;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.handlers.EditionSupport;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.handlers.RepositoryHandlerFactory.OrderedRepositoryHandler;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.OrderBy;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.ResourceUsage;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.ui.author.AuthoringEntryDataModel.Cols;
import org.olat.repository.ui.author.AuthoringEntryDataSource.AuthorSourceFilter;
import org.olat.repository.ui.author.copy.CopyRepositoryEntryController;
import org.olat.repository.ui.author.copy.CopyRepositoryEntryWrapperController;
import org.olat.repository.wizard.RepositoryWizardProvider;
import org.olat.resource.references.Reference;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.comx
 *
 */
public class AuthorListController extends FormBasicController implements Activateable2, AuthoringEntryDataSourceUIFactory, FlexiTableCssDelegate {

	private static final Logger log = Tracing.createLoggerFor(AuthorListController.class);

	private FlexiTableElement tableEl;
	
	private FlexiFiltersTab myTab;
	private FlexiFiltersTab myCoursesTab;
	private FlexiFiltersTab bookmarkTab;
	private FlexiFiltersTab searchTab;
	private FlexiFiltersTab deletedTab;
	private AuthoringEntryDataModel model;
	private AuthoringEntryDataSource dataSource;

	private Controller toolsCtrl;
	private CloseableModalController cmc;
	private SendMailController sendMailCtrl;
	private ModifyStatusController modifyStatusCtrl;
	private StepsMainRunController wizardCtrl;
	private StepsMainRunController modifyOwnersWizardCtrl;
	private StepsMainRunController settingsWizardCtrl;
	private UserSearchController userSearchCtr;
	private DialogBoxController copyDialogCtrl;
	private ReferencesController referencesCtrl;
	private CopyRepositoryEntryController copyCtrl;
	private CopyRepositoryEntryWrapperController copyWrapperCtrl;
	private ConfirmCloseController closeCtrl;
	private ConfirmDeleteSoftlyController confirmDeleteCtrl;
	private ImportRepositoryEntryController importCtrl;
	private ImportURLRepositoryEntryController importUrlCtrl;
	private CreateEntryController createCtrl;
	private ConfirmRestoreController confirmRestoreCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ConfirmDeletePermanentlyController confirmDeletePermanentlyCtrl;
	private RepositoryEntrySmallDetailsController infosCtrl;
	private MigrationSelectionController migrationSelectionCtrl;
	private ModifyRuntimeTypeController modifyRuntimeTypeCtrl;
	private StepsMainRunController coursesArchivesWizard;
	
	private final Roles roles;
	private final boolean isGuestOnly;
	private boolean hasAuthorRight;
	private boolean hasAdministratorRight;
	private final AuthorListConfiguration configuration;
	
	private FormLink importLink;
	private FormLink importUrlLink;
	private FormLink copyButton;
	private FormLink selectButton;
	private FormLink settingsButton;
	private FormLink courseArchiveButton;
	private FormLink indexMetadataButton;
	private FormLink deleteButton;
	private FormLink restoreButton;
	private FormLink sendMailButton;
	private FormLink modifyStatusButton;
	private FormLink modifyOwnersButton;
	private FormLink coursesArchivesButton;
	private FormLink modifyRuntimeTypeButton;
	private FormLink deletePermanentlyButton;

	private LockResult lockResult;
	private final boolean taxonomyEnabled;
	private int counter = 0;
	//only used as marker for dirty, model cannot load specific rows
	private final List<Long> dirtyRows = new ArrayList<>();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryBulkService repositoryBulkService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;
	@Autowired
	private LearningPathService learningPathService;
	@Autowired
	private HelpModule helpModule;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private OAIPmhModule oaiPmhModule;
	@Autowired
	private CourseNodeService courseNodeService;
	@Autowired
	protected TaskExecutorManager taskExecutorManager;
	@Autowired
	private LifecycleModule lifecycleModule;

	public AuthorListController(UserRequest ureq, WindowControl wControl, SearchAuthorRepositoryEntryViewParams searchParams, AuthorListConfiguration configuration) {
		super(ureq, wControl, "entries");
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(),
				Util.createPackageTranslator(HelpAdminController.class, getLocale(), getTranslator())));

		OLATResourceable ores = OresHelper.createOLATResourceableType("RepositorySite");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		
		this.configuration = configuration;

		roles = ureq.getUserSession().getRoles();
		isGuestOnly = roles.isGuestOnly();
		hasAdministratorRight = roles.isAdministrator() || roles.isLearnResourceManager();
		hasAuthorRight =  hasAdministratorRight || roles.isAuthor();
		taxonomyEnabled = taxonomyModule.isEnabled() && !repositoryModule.getTaxonomyRefs().isEmpty();

		dataSource = new AuthoringEntryDataSource(searchParams, this, configuration, taxonomyEnabled);
		initForm(ureq);
		initTools(ureq, flc);
	}
	
	public AuthorListController(UserRequest ureq, WindowControl wControl, Form rootForm, SearchAuthorRepositoryEntryViewParams searchParams, AuthorListConfiguration configuration) {
		super(ureq, wControl, LAYOUT_CUSTOM, "entries", rootForm);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(),
				Util.createPackageTranslator(HelpAdminController.class, getLocale(), getTranslator())));

		OLATResourceable ores = OresHelper.createOLATResourceableType("RepositorySite");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		
		this.configuration = configuration;

		roles = ureq.getUserSession().getRoles();
		isGuestOnly = roles.isGuestOnly();
		hasAdministratorRight = roles.isAdministrator() || roles.isLearnResourceManager();
		hasAuthorRight =  hasAdministratorRight || roles.isAuthor();
		taxonomyEnabled = taxonomyModule.isEnabled() && !repositoryModule.getTaxonomyRefs().isEmpty();

		dataSource = new AuthoringEntryDataSource(searchParams, this, configuration, taxonomyEnabled);
		initForm(ureq);
		initTools(ureq, flc);
	}
	
	protected void initTools(UserRequest ureq, FormLayoutContainer formLayout) {
		if(hasAuthorRight) {
			List<OrderedRepositoryHandler> handlers = getAllowedRepositoryHandlers();
			initImportTools(handlers, formLayout);
			initCreateTools(handlers, formLayout);
		}

		if (configuration.isHelpCenter() && helpModule.isHelpEnabled()) {
			initHelpModuleTools(ureq, formLayout);
		}
		
		if (configuration.isMoreMenu()) {
			initMoreMenu(formLayout);
		}
	}
	
	private void initImportTools(List<OrderedRepositoryHandler> handlers, FormLayoutContainer formLayout) {	
		if(!configuration.isImportRessources()) return;
		importLink = uifactory.addFormLink("cmd.import.ressource", "cmd.import.ressource", null, formLayout, Link.BUTTON);
		importLink.setDomReplacementWrapperRequired(false);
		importLink.setIconLeftCSS("o_icon o_icon_import");
		importLink.setElementCssClass("o_sel_author_import");
		importLink.setVisible(isImportAllowed(handlers));

		DropdownItem importDropdown = uifactory.addDropdownMenu("import.dropdown", null, null, formLayout, getTranslator());
		importDropdown.setEmbbeded(true);
		importDropdown.setOrientation(DropdownOrientation.right);
		
		importUrlLink = uifactory.addFormLink("cmd.import.url.ressource", "cmd.import.url.ressource", null, formLayout, Link.LINK);
		importUrlLink.setDomReplacementWrapperRequired(false);
		importUrlLink.setIconLeftCSS("o_icon o_icon_link");
		importUrlLink.setElementCssClass("o_sel_author_url_import");
		importUrlLink.setVisible(isImportUrlAllowed(handlers));
		importDropdown.addElement(importUrlLink);
	}

	private void initCreateTools(List<OrderedRepositoryHandler> handlers, FormLayoutContainer formLayout) {
		if(!configuration.isCreateRessources()) return;
		
		List<OrderedRepositoryHandler> creatorHandlers = getAllowedRepositoryHandlersToCreate(handlers);
		
		if(creatorHandlers.size() == 1) {
			RepositoryHandler handler = creatorHandlers.get(0).getHandler();
			FormLink createLink = uifactory.addFormLink("cmd.create.ressource", handler.getSupportedType(), null, flc, Link.BUTTON);
			createLink.setIconLeftCSS("o_icon o_icon-fw " + RepositoyUIFactory.getIconCssClass(handler.getSupportedType()));
			createLink.setElementCssClass("o_sel_author_create-" + handler.getSupportedType());
			createLink.setUserObject(handler);

		} else if(creatorHandlers.size() > 1) {
			DropdownItem createDropdown = uifactory.addDropdownMenu("cmd.create.ressource", "cmd.create.ressource", null, formLayout,getTranslator());
			createDropdown.setElementCssClass("o_sel_author_create");
			createDropdown.setIconCSS("o_icon o_icon_add");
			int lastGroup = 0;
			for(OrderedRepositoryHandler orderedHandler:creatorHandlers) {
				RepositoryHandler handler = orderedHandler.getHandler();
				// for each 10-group, create a separator
				int group = orderedHandler.getOrder() / 10;
				if (group > lastGroup) {
					createDropdown.addElement(new SpacerItem("spacer" + orderedHandler.getOrder()));
					lastGroup = group;
				}
				addCreateLink(handler, createDropdown);
			}
		}
	}
	
	private void initHelpModuleTools(UserRequest ureq, FormLayoutContainer formLayout) {		
		List<HelpLinkSPI> helpLinks = helpModule.getAuthorSiteHelpPlugins();
		if (helpLinks.size() == 1) {
			Component helpCmp = helpLinks.get(0).getHelpUserTool(getWindowControl()).getMenuComponent(ureq, formLayout.getFormItemComponent(), false);
			formLayout.getFormItemComponent().put("help.single", helpCmp);
		} else if (helpLinks.size() > 1) {
			DropdownItem helpDropdown = uifactory.addDropdownMenu("help.list", "help.authoring", "help.authoring", formLayout, getTranslator());
			helpDropdown.setIconCSS("o_icon o_icon_help");
			helpDropdown.setOrientation(DropdownOrientation.right);
			for (HelpLinkSPI helpLinkSPI : helpLinks) {
				((Dropdown)helpDropdown.getComponent()).addComponent(helpLinkSPI
						.getHelpUserTool(getWindowControl()).getMenuComponent(ureq, formLayout.getFormItemComponent(), false));
			}
		}
	}
	
	private void initMoreMenu(FormLayoutContainer formLayout) {
		DropdownItem moreDropdown = uifactory.addDropdownMenuMore("more.menu", formLayout, getTranslator());
		moreDropdown.setEmbbeded(true);
		moreDropdown.setButton(true);
		
		courseArchiveButton = uifactory.addFormLink("course.archive", formLayout, Link.LINK); 
		courseArchiveButton.setIconLeftCSS("o_icon o_icon_coursearchive");
		moreDropdown.addElement(courseArchiveButton);
	}
	
	private List<OrderedRepositoryHandler> getAllowedRepositoryHandlers() {
		List<OrderedRepositoryHandler> allowedHandlers = new ArrayList<>();
		List<OrderedRepositoryHandler> handlers = repositoryHandlerFactory.getOrderRepositoryHandlers();
		for(OrderedRepositoryHandler orderedHandler:handlers) {
			RepositoryHandler handler = orderedHandler.getHandler();
			if(handler != null && configuration.isResourceTypeAllowed(handler.getSupportedType())) {
				allowedHandlers.add(orderedHandler);
			}
		}
		return allowedHandlers;
	}
	
	private boolean isImportAllowed(List<OrderedRepositoryHandler> handlers) {
		return configuration.isImportRessources() && handlers.stream()
				.anyMatch(handler -> handler.getHandler().supportImport());
	}
	
	private boolean isImportUrlAllowed(List<OrderedRepositoryHandler> handlers) {
		return configuration.isImportRessources() && handlers.stream()
				.anyMatch(handler -> handler.getHandler().supportImportUrl());
	}
	
	private List<OrderedRepositoryHandler> getAllowedRepositoryHandlersToCreate(List<OrderedRepositoryHandler> handlers) {
		List<OrderedRepositoryHandler> createHandlers = new ArrayList<>();
		for(OrderedRepositoryHandler orderedHandler:handlers) {
			RepositoryHandler handler = orderedHandler.getHandler();
			if(handler.supportCreate(getIdentity(), roles)) {
				createHandlers.add(orderedHandler);
			}
		}
		return createHandlers;
	}
	
	@Override
	public Translator getTranslator() {
		return super.getTranslator();
	}

	private void addCreateLink(RepositoryHandler handler, DropdownItem dropdown) {
		FormLink createLink = uifactory.addFormLink(handler.getSupportedType(), flc, Link.LINK);
		createLink.setIconLeftCSS("o_icon o_icon-fw " + RepositoyUIFactory.getIconCssClass(handler.getSupportedType()));
		createLink.setElementCssClass("o_sel_author_create-" + handler.getSupportedType());
		createLink.setUserObject(handler);
		dropdown.addElement(createLink);
	}
	
	public boolean isEmpty() {
		return dataSource.getRowCount() == 0;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			if(StringHelper.containsNonWhitespace(configuration.getI18nKeyTitle())) {
				layoutCont.contextPut("title", configuration.getI18nKeyTitle());
			}
		}

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18nKey(), Cols.key.ordinal(), true, OrderBy.key.name()));
		DefaultFlexiColumnModel markColumn = new DefaultFlexiColumnModel(configuration.isDefaultBookmark(),
				Cols.mark.i18nKey(), Cols.mark.ordinal(), true, OrderBy.favorit.name());
		markColumn.setExportable(false);
		markColumn.setIconHeader("o_icon o_icon_bookmark_header o_icon-lg");
		columnsModel.addFlexiColumnModel(markColumn);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(configuration.isDefaultIconType(),
				Cols.type.i18nKey(), Cols.type.ordinal(), true, OrderBy.type.name(),
				FlexiColumnModel.ALIGNMENT_LEFT, new TypeRenderer()));
		DefaultFlexiColumnModel technicalTypeColumnModel = new DefaultFlexiColumnModel(false, Cols.technicalType.i18nKey(), Cols.technicalType.ordinal(),
				true, OrderBy.technicalType.name());
		technicalTypeColumnModel.setCellRenderer(new TechnicalTypeRenderer());
		columnsModel.addFlexiColumnModel(technicalTypeColumnModel);
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer("select", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.displayName.i18nKey(), Cols.displayName.ordinal(), "select",
				true, OrderBy.displayname.name(), renderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.authors.i18nKey(), Cols.authors.ordinal(),
				true, OrderBy.authors.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.location.i18nKey(), Cols.location.ordinal(),
				true, OrderBy.location.name()));
		if(repositoryModule.isManagedRepositoryEntries()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalId.i18nKey(), Cols.externalId.ordinal(),
					true, OrderBy.externalId.name()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalRef.i18nKey(), Cols.externalRef.ordinal(),
				true, OrderBy.externalRef.name()));
		if (lifecycleModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lifecycleSoftkey.i18nKey(), Cols.lifecycleSoftkey.ordinal(),
					true, OrderBy.lifecycleSoftkey.name()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lifecycleLabel.i18nKey(), Cols.lifecycleLabel.ordinal(),
					true, OrderBy.lifecycleLabel.name()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lifecycleStart.i18nKey(), Cols.lifecycleStart.ordinal(),
				true, OrderBy.lifecycleStart.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lifecycleEnd.i18nKey(), Cols.lifecycleEnd.ordinal(),
				true, OrderBy.lifecycleEnd.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		
		if (taxonomyEnabled) {
			DefaultFlexiColumnModel taxonomyLevelColumnModel = new DefaultFlexiColumnModel(true, Cols.taxonomyLevels.i18nKey(),
					Cols.taxonomyLevels.ordinal(), false, null);
			taxonomyLevelColumnModel.setCellRenderer(new TaxonomyLevelRenderer(getLocale()));
			columnsModel.addFlexiColumnModel(taxonomyLevelColumnModel);
			DefaultFlexiColumnModel taxonomyLevelPathColumnModel = new DefaultFlexiColumnModel(configuration.isDefaultTaxonomyPath(),
					Cols.taxonomyPaths.i18nKey(), Cols.taxonomyPaths.ordinal(), false, null);
			taxonomyLevelPathColumnModel.setCellRenderer(new TaxonomyPathsRenderer(getLocale()));
			columnsModel.addFlexiColumnModel(taxonomyLevelPathColumnModel);
		}
		DefaultFlexiColumnModel educationalTypeColumnModel = new DefaultFlexiColumnModel(false, Cols.educationalType.i18nKey(),
				Cols.educationalType.ordinal(), false, null);
		educationalTypeColumnModel.setCellRenderer(new EducationalTypeRenderer());
		columnsModel.addFlexiColumnModel(educationalTypeColumnModel);
		DefaultFlexiColumnModel runtimeTypeColumnModel = new DefaultFlexiColumnModel(false, Cols.runtimeType.i18nKey(),
				Cols.runtimeType.ordinal(), false, null);
		runtimeTypeColumnModel.setCellRenderer(new RuntimeTypeRenderer());
		columnsModel.addFlexiColumnModel(runtimeTypeColumnModel);

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.author.i18nKey(), Cols.author.ordinal(),
				true, OrderBy.author.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.access.i18nKey(), Cols.access.ordinal(),
				true, OrderBy.access.name(), FlexiColumnModel.ALIGNMENT_LEFT, new AccessRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(configuration.isDefaultAccessControl(),
				Cols.ac.i18nKey(), Cols.ac.ordinal(), true, OrderBy.ac.name(), FlexiColumnModel.ALIGNMENT_LEFT, new ACRenderer()));
		if(loginModule.isGuestLoginEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(configuration.isDefaultGuest(),
					Cols.guests.i18nKey(), Cols.guests.ordinal(), true, OrderBy.guests.name(), FlexiColumnModel.ALIGNMENT_LEFT,
					new GuestAccessRenderer(getLocale())));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.creationDate.i18nKey(), Cols.creationDate.ordinal(),
				true, OrderBy.creationDate.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastUsage.i18nKey(), Cols.lastUsage.ordinal(),
				true, OrderBy.lastUsage.name()));
		if(!configuration.isOnlyAllowedResourceType("CourseModule")) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.references.i18nKey(), Cols.references.ordinal(),
					true, OrderBy.references.name()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.oerPub.i18nKey(), Cols.oerPub.ordinal(),
				true, OrderBy.oer.name(), FlexiColumnModel.ALIGNMENT_LEFT,
				new OerPubRenderer(getLocale())));
		if(lectureModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lectureInfos.i18nKey(), Cols.lectureInfos.ordinal(),
				true, OrderBy.lectureEnabled.name(), FlexiColumnModel.ALIGNMENT_LEFT, new LectureInfosRenderer(getTranslator())));
		}
		if (licenseModule.isEnabled(licenseHandler)) {
			columnsModel.addFlexiColumnModel(
					new DefaultFlexiColumnModel(false, false, Cols.license.i18nKey(), null, Cols.license.ordinal(),
							"license", false, null, FlexiColumnModel.ALIGNMENT_LEFT, new LicenseRenderer(getLocale())));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.deletedBy.i18nKey(), Cols.deletedBy.ordinal(),
				true, OrderBy.deletedBy.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, false, Cols.deletionDate.i18nKey(), null,
				Cols.deletionDate.ordinal(), null, true, OrderBy.deletionDate.name(),
				FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		
		initActionsColumns(columnsModel);
		
		model = new AuthoringEntryDataModel(dataSource, columnsModel, getIdentity(), ureq.getUserSession().getRoles());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		initFilterTabs();
		initFilters();
		tableEl.setCssDelegate(this);
		tableEl.setExportEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_coursetable");
		tableEl.setShowAllRowsEnabled(true);
		if(configuration.isSelectRepositoryEntries()) {
			tableEl.setSelection(true, configuration.getSelectRepositoryEntries() == SelectionMode.multi, configuration.isRowSelect());
		} else {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
		}
		tableEl.setSortSettings(new FlexiTableSortOptions(true, new SortKey(OrderBy.displayname.name(), true)));
		
		initBatchButtons(formLayout);
		
		tableEl.setAndLoadPersistedPreferences(ureq, configuration.getTableId());
	}
	
	protected final void initActionsColumns(FlexiTableColumnModel columnsModel) {
		if(configuration.isInfos()) {
			DefaultFlexiColumnModel infosColumn = new DefaultFlexiColumnModel(Cols.infos.i18nKey(), Cols.infos.ordinal(), "infos",
					new StaticFlexiCellRenderer("", "infos", null, "o_icon-lg o_icon_resource", translate("infos")));
			infosColumn.setIconHeader("o_icon o_icon-fw o_icon-lg o_icon_info_resource");
			infosColumn.setHeaderLabel(translate("infos"));
			infosColumn.setExportable(false);
			infosColumn.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(infosColumn);
		}
		if(configuration.isTools()) {
			DefaultFlexiColumnModel detailsColumn = new DefaultFlexiColumnModel(Cols.detailsSupported.i18nKey(), Cols.detailsSupported.ordinal(), "details",
					new StaticFlexiCellRenderer("", "details",  null, "o_icon-lg o_icon_details", translate("details")));
			detailsColumn.setIconHeader("o_icon o_icon-lg o_icon_details");
			detailsColumn.setHeaderLabel(translate("details"));
			detailsColumn.setAlwaysVisible(true);
			detailsColumn.setExportable(false);
			columnsModel.addFlexiColumnModel(detailsColumn);
			if(hasAuthorRight) {
				DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel(Cols.editionSupported.i18nKey(), Cols.editionSupported.ordinal(), "edit",
					new BooleanCellRenderer(new StaticFlexiCellRenderer("", "edit", null, "o_icon-lg o_icon_edit", translate("edit")), null));
				editColumn.setIconHeader("o_icon o_icon-fw o_icon-lg o_icon_edit");
				editColumn.setHeaderLabel(translate("edit"));
				editColumn.setAlwaysVisible(true);
				editColumn.setExportable(false);
				columnsModel.addFlexiColumnModel(editColumn);
				
				columnsModel.addFlexiColumnModel(new ActionsColumnModel(Cols.tools));
			}
		}
	}
	
	protected void initFilterTabs() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		if(!isGuestOnly) {
			bookmarkTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Bookmarks", translate("search.mark"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(AuthorSourceFilter.MARKED, "marked")));
			bookmarkTab.setElementCssClass("o_sel_author_bookmarks");
			bookmarkTab.setFiltersExpanded(true);
			tabs.add(bookmarkTab);
		}
		
		if(configuration.isResourceTypeAllowed("CourseModule")) {
			myCoursesTab = FlexiFiltersTabFactory.tabWithImplicitFilters("MyCourses", translate("search.my.courses"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(AuthorSourceFilter.OWNED, "owned"),
							FlexiTableFilterValue.valueOf(AuthorSourceFilter.TYPE, "CourseModule")));
			myCoursesTab.setElementCssClass("o_sel_author_courses");
			myCoursesTab.setFiltersExpanded(true);
			tabs.add(myCoursesTab);
		}
		
		if(!configuration.isOnlyAllowedResourceType("CourseModule")) {
			myTab = FlexiFiltersTabFactory.tabWithImplicitFilters("My", translate("search.my"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(AuthorSourceFilter.OWNED, "owned")));
			myTab.setElementCssClass("o_sel_author_my");
			myTab.setFiltersExpanded(true);
			tabs.add(myTab);
		}
		
		searchTab = FlexiFiltersTabFactory.tab("Search", translate("search.generic"), TabSelectionBehavior.clear);
		searchTab.setElementCssClass("o_sel_author_search");
		searchTab.setPosition(FlexiFilterTabPosition.right);
		searchTab.setLargeSearch(true);
		searchTab.setFiltersExpanded(true);
		tabs.add(searchTab);
		
		if(!configuration.isSelectRepositoryEntries() && (roles.isAuthor() || hasAdministratorRight)) {
			deletedTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Deleted", translate("search.deleted"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(AuthorSourceFilter.STATUS, RepositoryEntryStatusEnum.trash.name())));
			deletedTab.setElementCssClass("o_sel_author_deleted");
			deletedTab.setPosition(FlexiFilterTabPosition.right);
			deletedTab.setFiltersExpanded(true);
			tabs.add(deletedTab);
		}
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	protected void initFilters() {
		tableEl.setSearchEnabled(true);

		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		boolean admin = roles.isAdministrator() || roles.isSystemAdmin();
		// external id
		filters.add(new FlexiTableTextFilter(translate("cif.id"), AuthorSourceFilter.ID.name(), admin));
		
		// bookmarked
		SelectionValues markedKeyValue = new SelectionValues();
		markedKeyValue.add(SelectionValues.entry("marked", translate("search.mark")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("search.mark"),
				AuthorSourceFilter.MARKED.name(), markedKeyValue, true));
		
		// my resources
		SelectionValues myResourcesKeyValue = new SelectionValues();
		myResourcesKeyValue.add(SelectionValues.entry("owned", translate("cif.owned.resources.only")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("cif.owned.resources.only"),
				AuthorSourceFilter.OWNED.name(), myResourcesKeyValue, true));
		
		// author
		filters.add(new FlexiTableTextFilter(translate("cif.author.search"), AuthorSourceFilter.AUTHOR.name(), false));
		
		filters.add(new FlexiTableTextFilter(translate("cif.displayname"), AuthorSourceFilter.DISPLAYNAME.name(), false));
		
		filters.add(new FlexiTableTextFilter(translate("cif.description"), AuthorSourceFilter.DESCRIPTION.name(), false));
		
		// technical type
		SelectionValues technicalTypeKV = new SelectionValues();
		for (NodeAccessProviderIdentifier identifier : nodeAccessService.getNodeAccessProviderIdentifer()) {
			String name = identifier.getDisplayName(getLocale());
			technicalTypeKV.add(entry(identifier.getType(), name));
		}
		technicalTypeKV.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate("cif.technical.type"),
				AuthorSourceFilter.TECHNICALTYPE.name(), technicalTypeKV, true));

		// educational type
		SelectionValues educationalTypeKV = new SelectionValues();
		repositoryManager.getAllEducationalTypes()
				.forEach(type -> educationalTypeKV.add(entry(type.getKey().toString(), StringHelper.escapeHtml(translate(RepositoyUIFactory.getI18nKey(type))))));
		educationalTypeKV.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate("cif.educational.type"),
				AuthorSourceFilter.EDUCATIONALTYPE.name(), educationalTypeKV, true));
		
		// runtime type
		SelectionValues runtimeTypeKV = new SelectionValues();
		RepositoryEntryRuntimeType[] runtimeTypes = { RepositoryEntryRuntimeType.standalone, RepositoryEntryRuntimeType.embedded,
				RepositoryEntryRuntimeType.curricular, RepositoryEntryRuntimeType.template };
		for(RepositoryEntryRuntimeType runtimeType:runtimeTypes) {
			if(configuration.isRuntimeTypeAllowed(runtimeType)) {
				runtimeTypeKV.add(SelectionValues.entry(runtimeType.name(), translate("runtime.type.".concat(runtimeType.name()))));
			}
		}
		if(runtimeTypeKV.size() > 1) {
			filters.add(new FlexiTableSingleSelectionFilter(translate("cif.runtime.type"),
					AuthorSourceFilter.RUNTIMETYPE.name(), runtimeTypeKV, false));
		}

		// life-cycle
		SelectionValues lifecycleValues = new SelectionValues();
		lifecycleValues.add(SelectionValues.entry(RepositoryEntryStatusEnum.preparation.name(), translate(RepositoryEntryStatusEnum.preparation.i18nKey())));
		lifecycleValues.add(SelectionValues.entry(RepositoryEntryStatusEnum.review.name(), translate(RepositoryEntryStatusEnum.review.i18nKey())));
		lifecycleValues.add(SelectionValues.entry(RepositoryEntryStatusEnum.coachpublished.name(), translate(RepositoryEntryStatusEnum.coachpublished.i18nKey())));
		lifecycleValues.add(SelectionValues.entry(RepositoryEntryStatusEnum.published.name(), translate(RepositoryEntryStatusEnum.published.i18nKey())));
		lifecycleValues.add(SelectionValues.entry(RepositoryEntryStatusEnum.closed.name(), translate(RepositoryEntryStatusEnum.closed.i18nKey())));
		if(roles.isAuthor() || hasAdministratorRight) {
			lifecycleValues.add(SelectionValues.entry(RepositoryEntryStatusEnum.trash.name(), translate(RepositoryEntryStatusEnum.trash.i18nKey())));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("cif.resources.status"),
				AuthorSourceFilter.STATUS.name(), lifecycleValues, true));
		
		// type of resources
		SelectionValues resourceValues = new SelectionValues();
		List<OrderedRepositoryHandler> supportedHandlers = repositoryHandlerFactory.getOrderRepositoryHandlers();
		for(OrderedRepositoryHandler handler:supportedHandlers) {
			String type = handler.getHandler().getSupportedType();
			if(configuration.isResourceTypeAllowed(type)) {
				String iconLeftCss = RepositoyUIFactory.getIconCssClass(type);
				resourceValues.add(new SelectionValue(type, translate(type), null, "o_icon o_icon-fw ".concat(iconLeftCss), null, true));
			}
		}
		if(resourceValues.size() > 1) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("cif.type"),
				AuthorSourceFilter.TYPE.name(), resourceValues, true));
		}
		
		// organizations
		List<Organisation> organisations = organisationService.getOrganisations(getIdentity(), roles,
				OrganisationRoles.administrator, OrganisationRoles.principal, OrganisationRoles.learnresourcemanager, OrganisationRoles.author);
		List<Organisation> organisationList = new ArrayList<>(organisations);
		Collections.sort(organisationList, new OrganisationNameComparator(getLocale()));
		
		SelectionValues organisationValues = new SelectionValues();
		for(Organisation organisation:organisationList) {
			String key = organisation.getKey().toString();
			organisationValues.add(SelectionValues.entry(key, StringHelper.escapeHtml(organisation.getDisplayName())));
		}
		if(organisationValues.size() > 1) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("cif.organisations"),
				AuthorSourceFilter.ORGANISATION.name(), organisationValues, false));
		}

		// OER-release
		SelectionValues oerValues = new SelectionValues();
		oerValues.add(SelectionValues.entry(SearchAuthorRepositoryEntryViewParams.OERRelease.released.name(), translate("filter.released")));
		oerValues.add(SelectionValues.entry(SearchAuthorRepositoryEntryViewParams.OERRelease.notReleased.name(), translate("filter.not.released")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("details.index.metadata"),
				AuthorSourceFilter.OERRELASE.name(), oerValues, false));
		
		// taxonomy
		if (taxonomyEnabled) {
			SelectionValues taxonomyValues = getTaxonomyLevels();
			if(taxonomyValues != null) {
				filters.add(new FlexiTableMultiSelectionFilter(translate("table.header.taxonomy.paths"),
						AuthorSourceFilter.TAXONOMYLEVEL.name(), taxonomyValues, false));
			}
		}
		
		// license
		SelectionValues licenseValues = getLicenseValues();
		if(licenseValues != null) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("cif.license"),
					AuthorSourceFilter.LICENSE.name(), licenseValues, false));
		}

		SelectionValues usageValues = new SelectionValues();
		usageValues.add(SelectionValues.entry(ResourceUsage.used.name(), translate("cif.owned.resources.usage.used")));
		usageValues.add(SelectionValues.entry(ResourceUsage.notUsed.name(), translate("cif.owned.resources.usage.notUsed")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("cif.owned.resources.usage"),
				AuthorSourceFilter.USAGE.name(), usageValues, false));

		tableEl.setFilters(true, filters, true, false);
	}
	
	private SelectionValues getTaxonomyLevels() {
		List<TaxonomyRef> taxonomyRefs = repositoryModule.getTaxonomyRefs();
		if (taxonomyRefs.isEmpty()) {
			return null;
		}
		
		List<TaxonomyLevel> allTaxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomyRefs);
		return RepositoyUIFactory.createTaxonomyLevelKV(getTranslator(), allTaxonomyLevels);
	}
	
	private SelectionValues getLicenseValues() {
		if(!licenseModule.isEnabled(licenseHandler)) {
			return null;
		}
	
		List<LicenseType> activeLicenseTypes = licenseService.loadActiveLicenseTypes(licenseHandler);
		SelectionValues keyValues = new SelectionValues();
		for (LicenseType licenseType: activeLicenseTypes) {
			String key = String.valueOf(licenseType.getKey());
			String value = LicenseUIFactory.translate(licenseType, getLocale());
			keyValues.add(entry(key, StringHelper.escapeHtml(value)));
		}
		keyValues.sort(VALUE_ASC);	
		return keyValues;
	}
	
	protected void initBatchButtons(FormItemContainer formLayout) {
		if(hasAuthorRight && !configuration.isSelectRepositoryEntries()) {			
			sendMailButton = uifactory.addFormLink("tools.send.mail", formLayout, Link.BUTTON);
			tableEl.addBatchButton(sendMailButton);
			modifyStatusButton = uifactory.addFormLink("tools.modify.status", formLayout, Link.BUTTON);
			modifyStatusButton.setElementCssClass("o_sel_modify_status");
			tableEl.addBatchButton(modifyStatusButton);
			
			modifyRuntimeTypeButton = uifactory.addFormLink("tools.modify.runtime.type", formLayout, Link.BUTTON);
			modifyRuntimeTypeButton.setElementCssClass("o_sel_modify_runtime_type");
			tableEl.addBatchButton(modifyRuntimeTypeButton);
			
			modifyOwnersButton = uifactory.addFormLink("tools.modify.owners", formLayout, Link.BUTTON);
			modifyOwnersButton.setElementCssClass("o_sel_modify_owners");
			tableEl.addBatchButton(modifyOwnersButton);
			settingsButton = uifactory.addFormLink("settings.bulk", formLayout, Link.BUTTON);
			tableEl.addBatchButton(settingsButton);
			copyButton = uifactory.addFormLink("details.copy", formLayout, Link.BUTTON);
			tableEl.addBatchButton(copyButton);
			coursesArchivesButton = uifactory.addFormLink("courses.archives", formLayout, Link.BUTTON);
			tableEl.addBatchButton(coursesArchivesButton);
			deleteButton = uifactory.addFormLink("details.delete", formLayout, Link.BUTTON);
			tableEl.addBatchButton(deleteButton);
			restoreButton = uifactory.addFormLink("tools.restore", formLayout, Link.BUTTON);
			tableEl.addBatchButton(restoreButton);
			deletePermanentlyButton = uifactory.addFormLink("tools.delete.permanently", formLayout, Link.BUTTON);
			tableEl.addBatchButton(deletePermanentlyButton);
		}
		
		if(configuration.isBatchSelect()) {
			if(configuration.getSelectRepositoryEntries() == SelectionMode.multi) {
				selectButton = uifactory.addFormLink("tools.select.entries", formLayout, Link.BUTTON);
				tableEl.addBatchButton(selectButton);
			} else if(configuration.getSelectRepositoryEntries() == SelectionMode.single) {
				selectButton = uifactory.addFormLink("tools.select.entry", "tools.select.entry", null, formLayout, Link.BUTTON);
			}
		}
	}
	
	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		AuthoringEntryRow row = model.getObject(pos);
		if(row == null || row.getEntryStatus() == RepositoryEntryStatusEnum.trash
				|| row.getEntryStatus() == RepositoryEntryStatusEnum.deleted) {
			return "o_entry_deleted";
		}
		if(row.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
			return "o_entry_closed";
		}
		return null;
	}
	
	public boolean hasTab() {
		return tableEl.getSelectedFilterTab() != null;
	}
	
	public FlexiFiltersTab getFavoritTab() {
		return bookmarkTab;
	}
	
	public FlexiFiltersTab getMyTab() {
		return myTab;
	}
	
	public FlexiFiltersTab getMyCoursesTab() {
		return myCoursesTab;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String tabName = entry.getOLATResourceable().getResourceableTypeName();
			if(tableEl.getSelectedFilterTab() == null || !tableEl.getSelectedFilterTab().getId().equals(tabName)) {
				FlexiFiltersTab tab = tableEl.getFilterTabById(tabName);
				if(tab != null) {
					selectFilterTab(ureq, tab);
				} else {
					selectFilterTab(ureq, myTab);
				}
				dirtyRows.clear();// Selection of tab reload the data
			} else if("Deleted".equals(tabName)) {
				tableEl.addToHistory(ureq);
				reloadRows();
			} else {
				tableEl.addToHistory(ureq);
				reloadDirtyRows();
			}
		}
		
		if(state instanceof AuthorListState se) {
			if(se.getTableState() != null) {
				tableEl.setStateEntry(ureq, se.getTableState());
			}
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		} else if(createCtrl == source) {
			cmc.deactivate();
			if(Event.DONE_EVENT.equals(event)) {
				launchEditDescription(ureq, createCtrl.getAddedEntry());
				reloadRows();
				cleanUp();
			} else if(CreateRepositoryEntryController.CREATION_WIZARD.equals(event)) {
				doPostCreateWizard(ureq, createCtrl.getAddedEntry(), createCtrl.getHandler(), createCtrl.getWizardProvider());
			} else {
				cleanUp();
			}
		} else if(copyCtrl == source) {
			if (event == Event.CLOSE_EVENT) {
				if(cmc != null) {
					cmc.deactivate();
				}
			} else {
				if(cmc != null) {
					cmc.deactivate();
				}
				if (event == Event.DONE_EVENT) {
					reloadRows();
					launchEditDescription(ureq, copyCtrl.getCopiedEntry());
				}
				cleanUp();
			}
		} else if(copyWrapperCtrl == source) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				reloadRows();
			}
			cleanUp();
		} else if(importCtrl == source) {
			cmc.deactivate();
			if(Event.DONE_EVENT.equals(event)) {
				reloadRows();
				launchEditDescription(ureq, importCtrl.getImportedEntry());
			}
			cleanUp();
		} else if(importUrlCtrl == source ) {
			cmc.deactivate();
			if(Event.DONE_EVENT.equals(event)) {
				reloadRows();
				launchEditDescription(ureq, importUrlCtrl.getImportedEntry());
			}
			cleanUp();
		} else if(wizardCtrl == source) {
			if (event.equals(Event.CHANGED_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
				getWindowControl().pop();
				RepositoryEntry newEntry = (RepositoryEntry)wizardCtrl.getRunContext().get("authoringNewEntry");
				RepositoryHandler handler = (RepositoryHandler)wizardCtrl.getRunContext().get("repoHandler");
				releaseLock(handler);
				reloadRows();
				cleanUp();
				launchEditDescription(ureq, newEntry);
			}
		} else if(modifyStatusCtrl == source || modifyRuntimeTypeCtrl == source) {
			if (event == Event.DONE_EVENT) {
				reloadRows();
			}
			cmc.deactivate();
			cleanUp();
		} else if(modifyOwnersWizardCtrl == source) {
			if (event.equals(Event.CHANGED_EVENT) ) {
				getWindowControl().pop();
				reloadRows();
				cleanUp();
			} else if(event.equals(Event.CANCELLED_EVENT)) {
				getWindowControl().pop();
				cleanUp();
			}
		} else if(settingsWizardCtrl == source) {
			if (event.equals(Event.CHANGED_EVENT) ) {
				getWindowControl().pop();
				reloadRows();
				cleanUp();
			} else if(event.equals(Event.CANCELLED_EVENT)) {
				getWindowControl().pop();
				cleanUp();
			}
		} else if(userSearchCtr == source) {
			@SuppressWarnings("unchecked")
			List<AuthoringEntryRow> rows = (List<AuthoringEntryRow>)userSearchCtr.getUserObject();
			if (event instanceof MultiIdentityChosenEvent mice) {
				doAddOwners(mice.getChosenIdentities(), rows);
			} else if (event instanceof SingleIdentityChosenEvent sice) {
				List<Identity> futureOwners = Collections.singletonList(sice.getChosenIdentity());
				doAddOwners(futureOwners, rows);
			}
			cmc.deactivate();
			cleanUp();
		} else if(sendMailCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(referencesCtrl == source) {
			if(event == Event.DONE_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(closeCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
			} else if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				cmc.deactivate();
				reloadRows();
				cleanUp();
			}
		} else if(confirmDeleteCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
			} else if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				cmc.deactivate();
				reloadRows();
				cleanUp();
			}
		} else if(confirmDeletePermanentlyCtrl == source) {
			cmc.deactivate();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				reloadRows();
			}
			cleanUp();
		} else if(confirmRestoreCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				if(cmc != null) {
					cmc.deactivate();
				}
				reloadRows();
				cleanUp();
			} else if(event == Event.CANCELLED_EVENT) {
				if(cmc != null) {
					cmc.deactivate();
				}
				cleanUp();
			}
		} else if(copyDialogCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				@SuppressWarnings("unchecked")
				List<AuthoringEntryRow> rows = (List<AuthoringEntryRow>)copyDialogCtrl.getUserObject();
				doCompleteCopy(rows);
				reloadRows();
			}
		} else if (source == migrationSelectionCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				String selectedKey = migrationSelectionCtrl.getDesignEl().getSelectedKey();
				cmc.deactivate();
				cleanUp();
				doMigrate(ureq, selectedKey, migrationSelectionCtrl.getRow());
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if(source == coursesArchivesWizard) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT || event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
				cleanUp();
			}
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doCompleteCoursesArchives();
			}
		}
		super.event(ureq, source, event);
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(confirmDeletePermanentlyCtrl);
		removeAsListenerAndDispose(modifyOwnersWizardCtrl);
		removeAsListenerAndDispose(modifyRuntimeTypeCtrl);
		removeAsListenerAndDispose(coursesArchivesWizard);
		removeAsListenerAndDispose(settingsWizardCtrl);
		removeAsListenerAndDispose(confirmRestoreCtrl);
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(modifyStatusCtrl);
		removeAsListenerAndDispose(copyWrapperCtrl);
		removeAsListenerAndDispose(userSearchCtr);
		removeAsListenerAndDispose(importUrlCtrl);
		removeAsListenerAndDispose(sendMailCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(createCtrl);
		removeAsListenerAndDispose(importCtrl);
		removeAsListenerAndDispose(wizardCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(closeCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeletePermanentlyCtrl = null;
		modifyOwnersWizardCtrl = null;
		modifyRuntimeTypeCtrl = null;
		coursesArchivesWizard = null;
		settingsWizardCtrl = null;
		confirmRestoreCtrl = null;
		confirmDeleteCtrl = null;
		modifyStatusCtrl = null;
		copyWrapperCtrl = null;
		userSearchCtr = null;
		importUrlCtrl = null;
		sendMailCtrl = null;
		calloutCtrl = null;
		createCtrl = null;
		importCtrl = null;
		wizardCtrl = null;
		toolsCtrl = null;
		closeCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(modifyStatusButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows();
			if(!rows.isEmpty()) {
				doModifyStatus(ureq, rows);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if(modifyRuntimeTypeButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows();
			if(!rows.isEmpty()) {
				doModifyRuntimeType(ureq, rows);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if(modifyOwnersButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows();
			if(!rows.isEmpty()) {
				doModifyOwners(ureq, rows);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if(sendMailButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows();
			if(!rows.isEmpty()) {
				doSendMail(ureq, rows);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if(copyButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows();
			if(!rows.isEmpty()) {
				doConfirmCopy(ureq, rows);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if(coursesArchivesButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows("CourseModule");
			if(!rows.isEmpty()) {
				doCoursesArchives(ureq, rows);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if(settingsButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows();
			if(!rows.isEmpty()) {
				doChangeSettings(ureq, rows);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if (indexMetadataButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows();
			if (!rows.isEmpty()) {
				for (AuthoringEntryRow row : rows) {
					doIndexMetadata(row);
				}
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if (deleteButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows();
			if (!rows.isEmpty()) {
				doDelete(ureq, rows);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if (restoreButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows();
			if (!rows.isEmpty()) {
				doRestore(ureq, rows);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if (deletePermanentlyButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows();
			if (!rows.isEmpty()) {
				doDeletePermanently(ureq, rows);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if (importLink == source) {
			doImport(ureq);
		} else if (importUrlLink == source) {
			doImportUrl(ureq);
		} else if (selectButton == source) {
			List<AuthoringEntryRow> selectedRows = getMultiSelectedRows();
			if(configuration != null && configuration.getSelectRepositoryEntries() == SelectionMode.single) {
				if(selectedRows.size() == 1) {
					fireEvent(ureq, new AuthoringEntryRowSelectionEvent(selectedRows.get(0)));
				}
			} else {
				fireEvent(ureq, new AuthoringEntryRowsListSelectionEvent(selectedRows));
			}
		} else if(courseArchiveButton == source) {
			fireEvent(ureq, new AuthoringEvent(AuthoringEvent.COURSE_ARCHIVE_LIST));
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if ("mark".equals(cmd) && link.getUserObject() instanceof AuthoringEntryRow row) {
				boolean marked = doMark(ureq, row);
				link.setIconLeftCSS(marked ? "o_icon o_icon_bookmark o_icon-lg" : "o_icon o_icon_bookmark_add o_icon-lg");
				link.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
				link.getComponent().setDirty(true);
				row.setMarked(marked);
			} else if ("tools".equals(cmd) && link.getUserObject() instanceof AuthoringEntryRow row) {
				doOpenTools(ureq, row, link);
			} else if (("infos".equals(cmd) || "details".equals(cmd)) && link.getUserObject() instanceof AuthoringEntryRow row) {
				doOpenInfos(ureq, row, link);
			} else if ("references".equals(cmd) && link.getUserObject() instanceof AuthoringEntryRow row) {
				doOpenReferences(ureq, row, link);
			} else if (link.getUserObject() instanceof RepositoryHandler handler) {
				doCreate(ureq, handler);
			}
		} else if (source == tableEl) {
			if (event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				AuthoringEntryRow row = model.getObject(se.getIndex());
				if ("details".equals(cmd)) {
					launchDetails(ureq, row);
				} else if ("edit".equals(cmd)) {
					launchEditor(ureq, row);
				} else if ("select".equals(cmd)) {
					launch(ureq, row);
				}
			} else if (event instanceof FlexiTableFilterTabEvent fte) {
				doSelectFilterTab(fte.getTab());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//do not update the 
	}
	
	public void addDirtyRows(Long entryKey) {
		dirtyRows.add(entryKey);
	}
	
	protected void reloadDirtyRows() {
		if(!dirtyRows.isEmpty() && model.isAuthoringEntryRowLoaded(dirtyRows)) {
			reloadRows();
		}
	}
	
	protected void reloadRows() {
		tableEl.deselectAll();
		tableEl.reloadData();
		dirtyRows.clear();
		flc.setDirty(true);
	}
	
	public void setSelectedRow(AuthoringEntryRow row) {
		int index = model.getIndexOfObject(row);
		if(index >= 0) {
			tableEl.setMultiSelectedIndex(Set.of(Integer.valueOf(index)));
			flc.setDirty(true);
		}
	}

	private void doOpenTools(UserRequest ureq, AuthoringEntryRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		try {
			RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
			if(entry == null) {
				tableEl.reloadData();
				showWarning("repositoryentry.not.existing");
			} else  {
				if(entry.getEntryStatus() == RepositoryEntryStatusEnum.deleted
						|| entry.getEntryStatus() == RepositoryEntryStatusEnum.trash) {
					toolsCtrl = new DeletedToolsController(ureq, getWindowControl(), row, entry);
				} else {
					toolsCtrl = new ToolsController(ureq, getWindowControl(), row, entry);
				}
				listenTo(toolsCtrl);
				
				calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
						toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
				listenTo(calloutCtrl);
				calloutCtrl.activate();
			}
		} catch (CorruptedCourseException e) {
			log.warn("Course corrupted: {} ({})", row.getKey(), row.getOLATResourceable().getResourceableId(), e);
			showError("cif.error.corrupted");

		}
	}
	
	private void doOpenReferences(UserRequest ureq, AuthoringEntryRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
		if(entry == null) {
			tableEl.reloadData();
			showWarning("repositoryentry.not.existing");
		} else {
			referencesCtrl = new ReferencesController(ureq, getWindowControl(), entry);
			listenTo(referencesCtrl);
	
			calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					referencesCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(calloutCtrl);
			calloutCtrl.activate();
		}
	}
	
	private void doOpenInfos(UserRequest ureq, AuthoringEntryRow row, FormLink link) {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(infosCtrl);
		
		RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
		if(entry == null) {
			tableEl.reloadData();
			showWarning("repositoryentry.not.existing");
		} else {
			infosCtrl = new RepositoryEntrySmallDetailsController(ureq, getWindowControl(), entry);
			listenTo(infosCtrl);
			
			calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), infosCtrl.getInitialComponent(),
					link.getFormDispatchId(), null, true, null);
			listenTo(calloutCtrl);
			calloutCtrl.activate();
		}
	}
	
	private void doImport(UserRequest ureq) {
		if(guardModalController(importCtrl)) return;

		removeAsListenerAndDispose(importCtrl);
		importCtrl = new ImportRepositoryEntryController(ureq, getWindowControl());
		listenTo(importCtrl);
		removeAsListenerAndDispose(cmc);
		
		String title = translate("cmd.import.ressource");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), importCtrl.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doImportUrl(UserRequest ureq) {
		if(guardModalController(importUrlCtrl)) return;

		removeAsListenerAndDispose(importUrlCtrl);
		importUrlCtrl = new ImportURLRepositoryEntryController(ureq, getWindowControl());
		listenTo(importUrlCtrl);
		removeAsListenerAndDispose(cmc);
		
		String title = translate("cmd.import.url.ressource");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), importUrlCtrl.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreate(UserRequest ureq, RepositoryHandler handler) {
		if(guardModalController(createCtrl)) return;

		removeAsListenerAndDispose(createCtrl);
		createCtrl = handler.createCreateRepositoryEntryController(ureq, getWindowControl(), true);
		listenTo(createCtrl);
		removeAsListenerAndDispose(cmc);
		
		String title = translate(handler.getCreateLabelI18nKey());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), createCtrl.getInitialComponent(),
				true, title);
		cmc.setCustomWindowCSS("o_sel_author_create_popup");
		listenTo(cmc);
		cmc.activate();
	}
	
	protected boolean doMark(UserRequest ureq, AuthoringEntryRow row) {
		OLATResourceable item = OresHelper.createOLATResourceableInstance("RepositoryEntry", row.getKey());
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);

			dbInstance.commit();//before sending, save the changes
			EntryChangedEvent e = new EntryChangedEvent(row, getIdentity(), Change.removeBookmark, "author.title");
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
			return false;
		}
		
		String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
		markManager.setMark(item, getIdentity(), null, businessPath);
		
		dbInstance.commit();//before sending, save the changes
		EntryChangedEvent e = new EntryChangedEvent(row, getIdentity(), Change.addBookmark, "author.title");
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
		return true;
	}
	
	private void doPostCreateWizard(UserRequest ureq, RepositoryEntry newEntry, RepositoryHandler handler,
			RepositoryWizardProvider wizardProvider) {
		if(wizardCtrl != null) return;
		
		cleanUp();
		
		LockedRun run = () -> {
			wizardCtrl = wizardProvider.createWizardController(ureq, getWindowControl(), newEntry, getIdentity());
			wizardCtrl.getRunContext().put("authoringNewEntry", newEntry);
			wizardCtrl.getRunContext().put("repoHandler", handler);
			listenTo(wizardCtrl);
			getWindowControl().pushAsModalDialog(wizardCtrl.getInitialComponent());
		};
		
		OLATResourceable ores = newEntry.getOlatResource();
		lockAndRun(ureq, ores, handler, false, run);
	}
	
	private void lockAndRun(UserRequest ureq, OLATResourceable ores, RepositoryHandler handler, boolean releaseFinally, LockedRun run) {
		if (ores == null) {
			showError("error.wizard.start");
			return;
		}
		boolean isAlreadyLocked = handler.isLocked(ores);
		try {
			lockResult = handler.acquireLock(ores, ureq.getIdentity());
			if (lockResult == null || (lockResult != null && lockResult.isSuccess() && !isAlreadyLocked)) {
				run.run();
			} else if (lockResult != null && lockResult.isSuccess() && isAlreadyLocked) {
				String fullName = userManager.getUserDisplayName(lockResult .getOwner());
				showInfo("warning.course.alreadylocked.bySameUser", fullName);
				lockResult = null; // invalid lock, it was already locked
			} else {
				String fullName = userManager.getUserDisplayName(lockResult .getOwner());
				showInfo("warning.course.alreadylocked", fullName);
			}
		} finally {
			if (releaseFinally && (lockResult != null && lockResult.isSuccess() && !isAlreadyLocked)) {
				releaseLock(handler);
			}
		}
	}
	
	private void releaseLock(RepositoryHandler handler ) {
		handler.releaseLock(lockResult);
		lockResult = null;
	}
	
	public List<AuthoringEntryRow> getMultiSelectedRows() {
		return getMultiSelectedRows(row -> true);
	}
	
	public List<AuthoringEntryRow> getMultiSelectedRows(String typeName) {
		return getMultiSelectedRows(row ->
			row.getOLATResourceable().getResourceableTypeName().equals(typeName));
	}
	
	public List<AuthoringEntryRow> getMultiSelectedRows(Predicate<AuthoringEntryRow> acceptRow) {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<AuthoringEntryRow> rows = new ArrayList<>(selections.size());
		if(!selections.isEmpty()) {
			for(Integer i:selections) {
				
				AuthoringEntryRow row = model.getObject(i.intValue());
				if(row != null && acceptRow.test(row)) {
					rows.add(row);
				}
			}
		}
		return rows;
	}
	
	public void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if(tab == null) return;
		
		tableEl.setSelectedFilterTab(ureq, tab);
		doSelectFilterTab(tab);
	}
	
	private void doSelectFilterTab(FlexiFiltersTab tab) {
		if(copyButton != null) {
			copyButton.setVisible(deletedTab != tab);
		}
		if(deleteButton != null) {
			deleteButton.setVisible(deletedTab != tab);
		}
		if(restoreButton != null) {
			restoreButton.setVisible(deletedTab != tab);
		}
		if(sendMailButton != null) {
			sendMailButton.setVisible(deletedTab != tab);
		}
		if(modifyStatusButton != null) {
			modifyStatusButton.setVisible(deletedTab != tab);
		}
		if(modifyOwnersButton != null) {
			modifyOwnersButton.setVisible(deletedTab != tab);
		}
		if(restoreButton != null) {
			restoreButton.setVisible(deletedTab == tab);
		}
		if(deletePermanentlyButton != null) {
			deletePermanentlyButton.setVisible(deletedTab == tab);
		}
		if(searchTab == tab) {
			tableEl.setEmptyTableSettings("author.search.empty", "author.search.empty.hint", "o_CourseModule_icon");
		} else {
			tableEl.setEmptyTableSettings("author.list.empty", "author.list.empty.hint", "o_CourseModule_icon");
		}
	}
	
	private void doSendMail(UserRequest ureq, List<AuthoringEntryRow> rows) {
		if(guardModalController(sendMailCtrl)) return;
		
		rows.removeIf(row -> !canManage(row));
		
		if(rows.isEmpty()) {
			showWarning("bulk.update.nothing.applicable.selected");
		} else {
			sendMailCtrl = new SendMailController(ureq, getWindowControl(), rows);
			listenTo(sendMailCtrl);
			
			String title = translate("tools.send.mail");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), sendMailCtrl.getInitialComponent(),
					true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doModifyStatus(UserRequest ureq, List<AuthoringEntryRow> rows) {
		if(guardModalController(modifyStatusCtrl)) return;
		
		List<Long> rowKeys = new ArrayList<>(rows.size());
		for(AuthoringEntryRow row:rows) {
			if(canManage(row)) {
				rowKeys.add(row.getKey());
			}
		}
		List<RepositoryEntry> entries = repositoryManager.lookupRepositoryEntries(rowKeys);
		if(entries.isEmpty()) {
			showWarning("bulk.update.nothing.applicable.selected");
		} else {
			removeAsListenerAndDispose(modifyStatusCtrl);
			removeAsListenerAndDispose(cmc);
			
			modifyStatusCtrl = new ModifyStatusController(ureq, getWindowControl(), entries);
			listenTo(modifyStatusCtrl);
			
			String title = translate("tools.modify.status");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), modifyStatusCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doModifyRuntimeType(UserRequest ureq, List<AuthoringEntryRow> rows) {
		if(guardModalController(modifyStatusCtrl)) return;
		
		List<Long> rowKeys = new ArrayList<>(rows.size());
		for(AuthoringEntryRow row:rows) {
			if(canManage(row)) {
				rowKeys.add(row.getKey());
			}
		}
		List<RepositoryEntry> entries = repositoryManager.lookupRepositoryEntries(rowKeys);
		if(entries.isEmpty()) {
			showWarning("bulk.update.nothing.applicable.selected");
		} else {
			removeAsListenerAndDispose(modifyStatusCtrl);
			removeAsListenerAndDispose(cmc);
			
			modifyRuntimeTypeCtrl = new ModifyRuntimeTypeController(ureq, getWindowControl(), entries);
			listenTo(modifyRuntimeTypeCtrl);
			
			String title = translate("tools.modify.runtime.type");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), modifyRuntimeTypeCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doModifyOwners(UserRequest ureq, List<AuthoringEntryRow> rows) {
		if(guardModalController(modifyOwnersWizardCtrl)) return;
		
		List<AuthoringEntryRow> manageableRows = new ArrayList<>(rows.size());
		for(AuthoringEntryRow row:rows) {
			boolean managed = RepositoryEntryManagedFlag.isManaged(row.getManagedFlags(), RepositoryEntryManagedFlag.membersmanagement);
			if(!managed && canManage(row)) {
				manageableRows.add(row);
			}
		}
		
		if(manageableRows.isEmpty()) {
			showWarning("bulk.update.nothing.applicable.selected");
		} else {
			// Global context for wizard
			ModifyOwnersContext context = new ModifyOwnersContext();
			
			// List of owners of selected and manageable resources
			for (AuthoringEntryRow row : manageableRows) {
				List<Identity> owners = repositoryService.getMembers(row, RepositoryEntryRelationType.all, GroupRoles.owner.name());
				context.addOwnersAndResource(owners, row);
			}
			
			// Save them in the context for the wizard
			context.setAuthoringEntryRows(manageableRows);
			
			// Open new wizard
			CancelCallback cancelCallback = new CancelCallback();
			FinishedCallback finishedCallback = new FinishedCallback();
			ModifyOwnersStep1 step1 = new ModifyOwnersStep1(ureq, context);
			
			modifyOwnersWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), step1, finishedCallback, cancelCallback, translate("tools.modify.owners"), null);
			listenTo(modifyOwnersWizardCtrl);
	        getWindowControl().pushAsModalDialog(modifyOwnersWizardCtrl.getInitialComponent());
		}
	}
	
	private void doChangeSettings(UserRequest ureq, List<AuthoringEntryRow> rows) {
		if(guardModalController(settingsWizardCtrl)) return;
		
		List<Long> manageableEntryKeys = rows.stream()
				.filter(this::canManage)
				.map(AuthoringEntryRow::getKey)
				.collect(Collectors.toList());
		
		if(manageableEntryKeys.isEmpty()) {
			showWarning("bulk.update.nothing.applicable.selected");
		} else {
			List<RepositoryEntry> manageableEntries = repositoryService.loadByKeys(manageableEntryKeys);
			SettingsBulkEditables editables = repositoryBulkService.getSettingsBulkEditables(manageableEntries);
			if (!editables.isEditable()) {
				showWarning("bulk.update.nothing.applicable.selected");
			} else {
				SettingsContext settingsContext = new SettingsContext(manageableEntries);
				SettingsStepsStep step = new SettingsStepsStep(ureq, settingsContext, editables);
				settingsWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), step,
						new SettingsBulkCallback(), new CancelCallback(), translate("settings.bulk.title"), null);
				listenTo(settingsWizardCtrl);
				getWindowControl().pushAsModalDialog(settingsWizardCtrl.getInitialComponent());
			}
		}
	}
	
	private void doAddOwners(List<Identity> futureOwners, List<AuthoringEntryRow> rows) {
		for(AuthoringEntryRow row:rows) {
			RepositoryEntry re = repositoryService.loadByKey(row.getKey());
			for(Identity futureOwner:futureOwners) {
				repositoryService.addRole(futureOwner, re, GroupRoles.owner.name());
			}
		}
	}
	
	private void doConfirmCopy(UserRequest ureq, List<AuthoringEntryRow> rows) {
		boolean deleted = false;

		List<AuthoringEntryRow> copyableRows = new ArrayList<>(rows.size());
		for(AuthoringEntryRow row:rows) {
			RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
			if(entry == null) {
				deleted = true;
			} else {
				boolean canCopy = repositoryService.canCopy(entry, getIdentity());
				if(canCopy) {
					copyableRows.add(row);
				}
			}
		}
		
		if(deleted) {
			showWarning("repositoryentry.not.existing");
			tableEl.reloadData();
		} else if(copyableRows.isEmpty()) {
			showWarning("bulk.update.nothing.applicable.selected");
		} else {
			StringBuilder sb = new StringBuilder();
			for(AuthoringEntryRow row:copyableRows) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(StringHelper.escapeHtml(row.getDisplayname()));
			}
			
			String dialogText = (copyableRows.size() != rows.size())
					? translate("details.copy.confirm.warning", sb.toString()) : translate("details.copy.confirm", sb.toString());
			copyDialogCtrl = activateYesNoDialog(ureq, translate("details.copy"), dialogText, copyDialogCtrl);
			copyDialogCtrl.setUserObject(copyableRows);
		}
	}
	
	private void doCompleteCopy(List<AuthoringEntryRow> rows) {
		for(AuthoringEntryRow row:rows) {
			RepositoryEntry sourceEntry = repositoryService.loadByKey(row.getKey());
			String displayname = "Copy of " + sourceEntry.getDisplayname();
			if(displayname.length() > 99) {
				displayname = displayname.substring(0, 99);
			}
			repositoryService.copy(sourceEntry, getIdentity(), displayname, null);
		}
		
		showInfo("details.copy.success", new String[]{ Integer.toString(rows.size()) });
	}
	
	private void doCoursesArchives(UserRequest ureq, List<AuthoringEntryRow> rows) {
		removeAsListenerAndDispose(coursesArchivesWizard);
		
		List<Long> repositoryEntryKeys = rows.stream()
				.map(AuthoringEntryRow::getKey)
				.toList();
		List<RepositoryEntry> repositoryEntries = repositoryService.loadByKeys(repositoryEntryKeys);
		BulkCoursesArchivesContext context = BulkCoursesArchivesContext.defaultValues(repositoryEntries, roles);
		
		Step start = new BulkCoursesArchives_1_RepositoryEntriesStep(ureq, context);
		BulkCoursesArchivesFinishStepCallback finish = new BulkCoursesArchivesFinishStepCallback(context);
		coursesArchivesWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("wizard.bulk.courses.archives.title"), "");
		listenTo(coursesArchivesWizard);
		getWindowControl().pushAsModalDialog(coursesArchivesWizard.getInitialComponent());
	}
	
	private void doCompleteCoursesArchives() {
		String title = translate("wizard.complete.courses.archives.title");
		String text = translate("wizard.complete.courses.archives.desc");
		getWindowControl().setInfo(title, text);
	}
	
	private void doCloseResource(UserRequest ureq, AuthoringEntryRow row) {
		removeAsListenerAndDispose(closeCtrl);
		
		RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
		
		List<RepositoryEntry> entryToClose = Collections.singletonList(entry);
		closeCtrl = new ConfirmCloseController(ureq, getWindowControl(), entryToClose);
		listenTo(closeCtrl);
		
		String title = translate("read.only.header", entry.getDisplayname());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), closeCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doOverrideCloseResource(UserRequest ureq, AuthoringEntryRow row) {
		try {
			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(row.getResourceType());
			if(handler != null) {
				String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
				ureq.getUserSession().putEntry("override_readonly_" + row.getKey(), Boolean.TRUE);
				if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
					tableEl.reloadData();
				}
			}
		} catch (CorruptedCourseException e) {
			logError("Course corrupted: " + row.getKey() + " (" + row.getOLATResourceable().getResourceableId() + ")", e);
			showError("cif.error.corrupted");
		}
	}
	
	protected void doCopy(UserRequest ureq, AuthoringEntryRow row) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(copyCtrl);

		RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
		copyCtrl = new CopyRepositoryEntryController(ureq, getWindowControl(), entry);
		listenTo(copyCtrl);
		
		String title = translate("details.copy");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), copyCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void doCopyWithWizard(UserRequest ureq, AuthoringEntryRow row) {
		removeAsListenerAndDispose(copyWrapperCtrl);

		RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
		copyWrapperCtrl = new CopyRepositoryEntryWrapperController(ureq, getWindowControl(), entry, true);
		listenTo(copyWrapperCtrl);
	}

	private void showUnsupportedMessage(UserRequest ureq, List<CourseNode> unsupportedCourseNodes) {
		Controller unsupportedCourseNodesCtrl = new UnsupportedCourseNodesController(ureq, getWindowControl(), unsupportedCourseNodes);
		listenTo(unsupportedCourseNodesCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				unsupportedCourseNodesCtrl.getInitialComponent(), true, translate("unsupported.course.nodes.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doDelete(UserRequest ureq, List<AuthoringEntryRow> rows) {
		List<Long> deleteableRowKeys = new ArrayList<>(rows.size());
		for(AuthoringEntryRow row:rows) {
			boolean managed = RepositoryEntryManagedFlag.isManaged(row.getManagedFlags(), RepositoryEntryManagedFlag.delete);
			if(!managed && canManage(row)) {
				deleteableRowKeys.add(row.getKey());
			}
		}
		
		List<RepositoryEntry> entriesToDelete = repositoryManager.lookupRepositoryEntries(deleteableRowKeys);
		if(entriesToDelete.isEmpty()) {
			showWarning("bulk.update.nothing.applicable.selected");
		} else {
			removeAsListenerAndDispose(confirmDeleteCtrl);
			removeAsListenerAndDispose(cmc);
			
			confirmDeleteCtrl = new ConfirmDeleteSoftlyController(ureq, getWindowControl(), entriesToDelete, rows.size() != entriesToDelete.size());
			listenTo(confirmDeleteCtrl);
			
			String title = translate("details.delete");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doRestore(UserRequest ureq, List<AuthoringEntryRow> rows) {
		List<Long> deleteableRowKeys = new ArrayList<>(rows.size());
		for(AuthoringEntryRow row:rows) {
			boolean managed = RepositoryEntryManagedFlag.isManaged(row.getManagedFlags(), RepositoryEntryManagedFlag.delete);
			if(!managed && canManage(row)) {
				deleteableRowKeys.add(row.getKey());
			}
		}
		
		List<RepositoryEntry> entriesToRestore = repositoryManager.lookupRepositoryEntries(deleteableRowKeys);
		if(entriesToRestore.isEmpty()) {
			showWarning("bulk.update.nothing.applicable.selected");
		} else {
			removeAsListenerAndDispose(confirmRestoreCtrl);
			removeAsListenerAndDispose(cmc);
			
			confirmRestoreCtrl = new ConfirmRestoreController(ureq, getWindowControl(), entriesToRestore);
			listenTo(confirmRestoreCtrl);
			
			String title = translate("tools.restore");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmRestoreCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doDeletePermanently(UserRequest ureq, List<AuthoringEntryRow> rows) {
		List<Long> deleteableRowKeys = new ArrayList<>(rows.size());
		for(AuthoringEntryRow row:rows) {
			boolean managed = RepositoryEntryManagedFlag.isManaged(row.getManagedFlags(), RepositoryEntryManagedFlag.delete);
			if(!managed
					&& repositoryService.hasRoleExpanded(getIdentity(), row, OrganisationRoles.learnresourcemanager.name(), OrganisationRoles.administrator.name())
					&& !(row.getRuntimeType() == RepositoryEntryRuntimeType.template && repositoryService.isTemplateInUse(row))) {
				deleteableRowKeys.add(row.getKey());
			}
		}
		
		List<RepositoryEntry> entriesToDelete = repositoryManager.lookupRepositoryEntries(deleteableRowKeys);
		if(entriesToDelete.isEmpty()) {
			showWarning("bulk.update.nothing.applicable.selected");
		} else {
			removeAsListenerAndDispose(confirmDeletePermanentlyCtrl);
			removeAsListenerAndDispose(cmc);
			
			confirmDeletePermanentlyCtrl = new ConfirmDeletePermanentlyController(ureq, getWindowControl(), entriesToDelete, rows.size() != entriesToDelete.size());
			listenTo(confirmDeletePermanentlyCtrl);
			
			String title = translate("details.delete");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeletePermanentlyCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}

	protected void doIndexMetadata(AuthoringEntryRow row) {
		RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
		if (entry.getCanIndexMetadata()) {
			entry.setCanIndexMetadata(false);
		} else {
			entry.setCanIndexMetadata(true);
		}
		reloadRows();
	}
	
	protected void doDownload(UserRequest ureq, AuthoringEntryRow row) {
		RepositoryHandler typeToDownload = repositoryHandlerFactory.getRepositoryHandler(row.getResourceType());
		if (typeToDownload == null) {
			StringBuilder sb = new StringBuilder(translate("error.download"));
			sb.append(": No download handler for repository entry: ")
			  .append(row.getKey());
			showError(sb.toString());
			return;
		}
		
		RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
		OLATResourceable ores = entry.getOlatResource();
		
		LockedRun run = () -> {
			MediaResource mr = typeToDownload.getAsMediaResource(ores, RepositoryEntryImportExportLinkEnum.WITH_REFERENCE);
			if(mr!=null) {
				repositoryService.incrementDownloadCounter(entry);
				ureq.getDispatchResult().setResultingMediaResource(mr);
			} else {
				showError("error.export");
				fireEvent(ureq, Event.FAILED_EVENT);
			}
		};
		lockAndRun(ureq, ores, typeToDownload, true, run);
	}
	
	private void launch(UserRequest ureq, AuthoringEntryRow row) {
		try {
			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(row.getResourceType());
			if(handler != null) {
				if(configuration.isSelectRepositoryEntries()) {
					fireEvent(ureq, new AuthoringEntryRowSelectionEvent(row));	
				} else {
					String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
					if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
						tableEl.reloadData();
					}
				}
			}
		} catch (CorruptedCourseException e) {
			logError("Course corrupted: " + row.getKey() + " (" + row.getOLATResourceable().getResourceableId() + ")", e);
			showError("cif.error.corrupted");
		}
	}
	
	private void launch(UserRequest ureq, RepositoryEntryRef ref) {
		String businessPath = "[RepositoryEntry:" + ref.getKey() + "]";
		if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
			tableEl.reloadData();
		}
	}
	
	private void launchCatalog(UserRequest ureq, RepositoryEntryRef ref) {
		String businessPath = "[RepositoryEntry:" + ref.getKey() + "][Settings:0][Catalog:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void launchDetails(UserRequest ureq, RepositoryEntryRef ref) {
		String businessPath = "[RepositoryEntry:" + ref.getKey() + "][Infos:0]";
		if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
			tableEl.reloadData();
		}
	}
	
	private void launchEditDescription(UserRequest ureq, RepositoryEntry re) {
		if(re != null) {
			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(re);
			if(handler != null) {
				String businessPath = "[RepositoryEntry:" + re.getKey() + "][EditDescription:0]";
				if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
					tableEl.reloadData();
				}
			}
		}
	}
	
	private void launchEditDescription(UserRequest ureq, RepositoryEntryRef re) {
		String businessPath = "[RepositoryEntry:" + re.getKey() + "][EditDescription:0]";
		if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
			tableEl.reloadData();
		}
	}
	
	private void launchEditor(UserRequest ureq, AuthoringEntryRow row) {
		try {
			String businessPath = "[RepositoryEntry:" + row.getKey() + "][Editor:0]";
			if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
				tableEl.reloadData();
			}
		} catch (CorruptedCourseException e) {
			logError("Course corrupted: " + row.getKey() + " (" + row.getOLATResourceable().getResourceableId() + ")", e);
			showError("cif.error.corrupted");
		}
	}
	
	private void launchMembers(UserRequest ureq, AuthoringEntryRow row) {
		try {
			String businessPath = "[RepositoryEntry:" + row.getKey() + "][MembersMgmt:0]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} catch (CorruptedCourseException e) {
			logError("Course corrupted: " + row.getKey() + " (" + row.getOLATResourceable().getResourceableId() + ")", e);
			showError("cif.error.corrupted");
		}
	}

	private void launch(UserRequest ureq, CurriculumElement element) {
		String businessPath = "[CurriculumAdmin:0][Curriculum:" + element.getCurriculum().getKey() + "][Zoom:" + element.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	@Override
	public void forgeLinks(AuthoringEntryRow row) {
		String count = Integer.toString(++counter);
		
		//mark
		FormLink markLink = uifactory.addFormLink("mark_".concat(count), "mark", "", null, null, Link.NONTRANSLATED);
		markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setTitle(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
		//tools
		if(configuration.isTools()) {
			FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}
		if(configuration.isInfos()) {
			FormLink infosLink = uifactory.addFormLink("infos_".concat(count), "infos", "", null, null, Link.NONTRANSLATED);
			infosLink.setIconLeftCSS("o_icon o_icon_info_resource o_icon-fws o_icon-lg");
			infosLink.setUserObject(row);
			infosLink.setTitle(translate("details.info.title"));
			row.setInfosLink(infosLink);
		}
		//references
		if(row.getNumOfReferences() > 0 || row.getNumOfCurriculumElements() > 0) {
			String numOfReferences = Integer.toString(row.getNumOfReferences() + row.getNumOfCurriculumElements());
			FormLink referencesLink = uifactory.addFormLink("refs_".concat(count), "references", numOfReferences, null, null, Link.NONTRANSLATED);
			referencesLink.setUserObject(row);
			referencesLink.setTitle(translate("details.referenceinfo"));
			row.setReferencesLink(referencesLink);
		}
	}

	private void doMigrationSelection(UserRequest ureq, AuthoringEntryRow row) {
		removeAsListenerAndDispose(migrationSelectionCtrl);
		migrationSelectionCtrl = new MigrationSelectionController(ureq, getWindowControl(), row);
		listenTo(migrationSelectionCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), migrationSelectionCtrl.getInitialComponent(),
				true, translate("migration.selection"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doMigrate(UserRequest ureq, String selectedKey, AuthoringEntryRow row) {
		RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
		ICourse course = CourseFactory.loadCourse(entry);
		List<CourseNode> unsupportedCourseNodes = learningPathService.getUnsupportedCourseNodes(course);
		if (!unsupportedCourseNodes.isEmpty()) {
			showUnsupportedMessage(ureq, unsupportedCourseNodes);
			return;
		}

		RepositoryEntry lpEntry = learningPathService.migrate(entry, getIdentity());
		String bPath = "[RepositoryEntry:" + lpEntry.getKey() + "]";
		if (CourseModule.COURSE_TYPE_PROGRESS.equals(selectedKey)) {
			initProgressCourseConfig(lpEntry);
		}
		NewControllerFactory.getInstance().launch(bPath, ureq, getWindowControl());
	}

	private void initProgressCourseConfig(RepositoryEntry repositoryEntry) {
		OLATResourceable courseOres = repositoryEntry.getOlatResource();
		if (CourseFactory.isCourseEditSessionOpen(courseOres.getResourceableId())) {
			log.warn("Not able to set the course node access type: Edit session is already open!");
			return;
		}

		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
		courseConfig.setMenuPathEnabled(false);
		courseConfig.setMenuNodeIconsEnabled(true);

		CourseElementSearchParams searchParams = new CourseElementSearchParams();
		searchParams.setRepositoryEntries(Collections.singletonList(repositoryEntry));
		searchParams.setCourseElementType(Collections.singletonList("st"));
		List<CourseElement> structureElements = courseNodeService.getCourseElements(searchParams);
		Structure runStructure = course.getCourseEnvironment().getRunStructure();

		// set every structureElement Sequence of steps = No sequence
		for (CourseElement structureElement : structureElements) {
			ModuleConfiguration structureNodeConfig = runStructure.getNode(structureElement.getSubIdent()).getModuleConfiguration();
			structureNodeConfig.setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY, STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		}

		CourseEditorTreeNode courseEditorTreeNode = (CourseEditorTreeNode) course.getEditorTreeModel().getRootNode();

		// collect every structure element as structureCourseEditTreeNode
		List<CourseEditorTreeNode> structureCourseEditTreeNodes = new ArrayList<>();
		structureCourseEditTreeNodes.add(courseEditorTreeNode);
		for (int i = 0; i < courseEditorTreeNode.getChildCount(); i++) {
			if (courseEditorTreeNode.getCourseEditorTreeNodeChildAt(i).getCourseNode().getType().equals("st")) {
				structureCourseEditTreeNodes.add(courseEditorTreeNode.getCourseEditorTreeNodeChildAt(i));
			}
		}

		// set every structureCourseEditTreeNode Sequence of steps = No sequence
		for (CourseEditorTreeNode structureCourseEditTreeNode : structureCourseEditTreeNodes) {
			ModuleConfiguration structureCourseEditTreeNodeConfig = structureCourseEditTreeNode.getCourseNode().getModuleConfiguration();
			structureCourseEditTreeNodeConfig.setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY, STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		}

		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.saveCourse(repositoryEntry.getOlatResource().getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
	}
	
	/**
	 * Check if the user can manage the specified row. He needs
	 * to be learn resource manager, administrator or owner of the
	 * resource.
	 * 
	 * @param row
	 * @return true if the user can edit the resource
	 */
	protected boolean canManage(AuthoringEntryRow row) {
		return repositoryService.hasRoleExpanded(getIdentity(), row, OrganisationRoles.learnresourcemanager.name(),
				OrganisationRoles.administrator.name(), GroupRoles.owner.name());
	}
	
	private interface LockedRun {
		
		void run();
		
	}
	
	private class SettingsBulkCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			SettingsContext context = (SettingsContext)runContext.get(SettingsContext.DEFAULT_KEY);
			repositoryBulkService.update(getWindow(), getIdentity(), context);
			return StepsMainRunController.DONE_MODIFIED;
		}
	}
	
	private class FinishedCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
            ModifyOwnersContext context = (ModifyOwnersContext) runContext.get(ModifyOwnersContext.CONTEXT_KEY);
            	
            final List<Identity> removeOwnersList = context.getOwnersToRemove();
            final List<Identity> addOwnersList = context.getOwnersToAdd();
            if(addOwnersList != null) {
            	removeOwnersList.removeAll(addOwnersList);
            	addOwnersList.removeAll(context.getOwnersToRemove());
            }
            
            MailPackage mailing = new MailPackage(context.isSendMail());
            
            for (AuthoringEntryRow resource : context.getAuthoringEntryRows()) {
            	RepositoryEntry repoEntry = repositoryService.loadByKey(resource.getKey());
            	
		        repositoryManager.removeOwners(getIdentity(), removeOwnersList, repoEntry, mailing);
		        
		        if(addOwnersList != null) {
		        	IdentitiesAddEvent addEvent = new IdentitiesAddEvent(addOwnersList);
		        	repositoryManager.addOwners(getIdentity(), addEvent, repoEntry, mailing);
		        }
            }
            // Fire event
            return StepsMainRunController.DONE_MODIFIED;
        }
    }

    private static class CancelCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
            return Step.NOSTEP;
        }
    }
	
	private class ReferencesController extends BasicController {

		@Autowired
		private PortfolioService portfolioService;
		@Autowired
		private ReferenceManager referenceManager;
		@Autowired
		private CurriculumService curriculumService;
		
		public ReferencesController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
			super(ureq, wControl);
			setTranslator(AuthorListController.this.getTranslator());
			VelocityContainer mainVC = createVelocityContainer("references");
			
			List<RepositoryEntry> refs = referenceManager.getRepositoryReferencesTo(entry.getOlatResource());

			List<String> refLinks = new ArrayList<>(refs.size());
			for(RepositoryEntry ref:refs) {
				String name = "ref-" + (++counter);
				Link refLink = LinkFactory.createLink(name, "reference", this.getTranslator(), mainVC, this, Link.NONTRANSLATED);
				refLink.setCustomDisplayText(StringHelper.escapeHtml(ref.getDisplayname()));
				refLink.setUserObject(ref);
				refLink.setIconLeftCSS("o_icon o_icon-fw " + RepositoyUIFactory.getIconCssClass(ref));
				
				String businessPath = "[RepositoryEntry:" + ref.getKey() + "]";
				String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
				refLink.setUrl(url);
				refLinks.add(name);
			}
			mainVC.contextPut("referenceLinks", refLinks);
			
			List<Reference> references = referenceManager.getReferencesTo(entry.getOlatResource(),
					QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME);
			if (!references.isEmpty()) {
				mainVC.contextPut("qualityDataCollections", translate("details.referenceinfo.data.collections",
						String.valueOf(references.size())));
			}

			List<CurriculumElementWithParents> curriculumElements = curriculumService.getOrderedCurriculumElementsTree(entry);
			List<Link> curriculumElementsLinks = new ArrayList<>(curriculumElements.size());
			for(CurriculumElementWithParents curriculumElement:curriculumElements) {
				String name = "curel-" + (++counter);
				Link elementLink = LinkFactory.createLink(name, "curriculum-element", this.getTranslator(), mainVC, this, Link.NONTRANSLATED);
				elementLink.setCustomDisplayText(StringHelper.escapeHtml(curriculumElement.getDisplayName()));
				elementLink.setUserObject(curriculumElement);
				
				String businessPath = "[CurriculumAdmin:0][Curriculum:" + curriculumElement.getCurriculum().getKey() + "][Zoom:" + curriculumElement.getKey() + "]";
				String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
				elementLink.setUrl(url);
				
				curriculumElementsLinks.add(elementLink);
			}
			mainVC.contextPut("curriculumElementsLinks", curriculumElementsLinks);
			
			if(BinderTemplateResource.TYPE_NAME.equals(entry.getOlatResource().getResourceableTypeName())) {
				int usage = portfolioService.getTemplateUsage(entry);
				if(usage > 0) {
					mainVC.contextPut("binderTemplateUsage", translate("details.referenceinfo.binder.template",
							String.valueOf(usage)));
				}
			}
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(source instanceof Link) {
				fireEvent(ureq, Event.DONE_EVENT);
				Link link = (Link)source;
				if("reference".equals(link.getCommand())) {
					RepositoryEntryRef uobject = (RepositoryEntryRef)link.getUserObject();
					launch(ureq, uobject);
				} else if("curriculum-element".equals(link.getCommand())) {
					CurriculumElementWithParents uobject = (CurriculumElementWithParents)link.getUserObject();
					launch(ureq, uobject.getCurriculumElement());
				}
			}
		}
	}
	
	private class ToolsController extends BasicController {

		private final VelocityContainer mainVC;
		
		private final boolean isOwner;
		private final boolean isAuthor;
		private final AuthoringEntryRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, AuthoringEntryRow row, RepositoryEntry entry) {
			super(ureq, wControl);
			setTranslator(AuthorListController.this.getTranslator());
			this.row = row;
			
			boolean isManager = repositoryService.hasRoleExpanded(getIdentity(), entry,
					OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name());
			isOwner = isManager || repositoryService.hasRole(getIdentity(), entry, GroupRoles.owner.name());
			isAuthor = isManager || repositoryService.hasRoleExpanded(getIdentity(), entry, OrganisationRoles.author.name());
			
			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(entry);

			mainVC = createVelocityContainer("tools");
			List<String> links = new ArrayList<>();

			if(isOwner) {
				addLink("tools.edit.description", "description", "o_icon o_icon-fw o_icon_details", "/Settings/0/Info/0", links);
				// Selenium for Firefox need this (cannot click reliably the edit button under the sticky action column)
				EditionSupport editionSupport = handler.supportsEdit(row.getOLATResourceable(), getIdentity(), roles);
				if((editionSupport == EditionSupport.yes || editionSupport == EditionSupport.embedded)
						&& !entry.getEntryStatus().decommissioned()) {
					addLink("edit", "edit", "o_icon o_icon-fw o_icon_edit", "/Editor/0", links);
				}
				
				if(repositoryModule.isCatalogEnabled()) {
					addLink("tools.edit.catalog", "catalog", "o_icon o_icon-fw o_icon_catalog", "/Settings/0/Catalog/0", links);
				}
				addLink("details.members", "members", "o_icon o_icon-fw o_icon_membersmanagement", "/MembersMgmt/0", links);	
			}
			
			boolean copyManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.copy);
			boolean canCopy = (isAuthor || isOwner) && (entry.getCanCopy() || isOwner) && !copyManaged;
			
			boolean canConvertLearningPath = false;
			boolean isLearningPathCourse = false;
			if (canCopy && "CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
				ICourse course = CourseFactory.loadCourse(entry);
				if (course != null) {
					if (!LearningPathNodeAccessProvider.TYPE.equals(course.getCourseConfig().getNodeAccessType().getType())) {
						canConvertLearningPath = true;
					} else {
						isLearningPathCourse = true;
					}
				}
			}
			
			boolean canDownload = entry.getCanDownload() && handler.supportsDownload();
			// disable download for courses if not author or owner
			if (entry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName()) && !(isOwner || isAuthor)) {
				canDownload = false;
			}
			// always enable download for owners
			if (isOwner && handler.supportsDownload()) {
				canDownload = true;
			}
			
			
			if(canCopy || canDownload) {
				links.add("-");
				if (canCopy) {
					addLink("details.copy", "copy", "o_icon o_icon-fw o_icon_copy", "/Infos/0", links);
					if (isLearningPathCourse && canCopy) {
						addLink("details.copy.with.wizard", "copy_with_wizard", "o_icon o_icon-fw o_icon_copy", "/Infos/0", links);
					}
				}
				if (canConvertLearningPath) {
					addLink("details.convert.learning.path", "convertLearningPath", "o_icon o_icon-fw o_icon_learning_path", null, links);
				}
				if(canDownload) {
					addLink("details.download", "download", "o_icon o_icon-fw o_icon_download", null, links);
				}
			}
			
			boolean canClose = OresHelper.isOfType(entry.getOlatResource(), CourseModule.class)
					&& !RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.close)
					&& !entry.getEntryStatus().decommissioned();
			
			if(isOwner) {
				boolean deleteManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.delete);
				if(canClose || !deleteManaged) {
					links.add("-");
				}

				if (oaiPmhModule.isEnabled()) {
					addLink("details.index.metadata", "indexMetadata", "o_icon o_icon-fw o_icon_share", null, links);
					links.add("-");
				}
				
				boolean closed = entry.getEntryStatus() == RepositoryEntryStatusEnum.closed;
				if(closed && "CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
					addLink("details.override.close", "override-close", "o_icon o_icon-fw o_icon_close_resource", null, links);
				} else if(canClose) {
					addLink("details.close.ressoure", "close", "o_icon o_icon-fw o_icon_close_resource", null, links);
				}
				if(!deleteManaged) {
					addLink("details.delete", "delete", "o_icon o_icon-fw o_icon_delete_item", null, links);
				}
			}

			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS, String path, List<String> links) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			if(path != null) {
				String url = row.getUrl().concat(path);
				link.setUrl(url);
			}
			mainVC.put(name, link);
			links.add(name);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link link) {
				String cmd = link.getCommand();
				if("description".equals(cmd)) {
					launchEditDescription(ureq, row);
				} else if("catalog".equals(cmd)) {
					launchCatalog(ureq, row);
				}  else if("members".equals(cmd)) {
					launchMembers(ureq, row);
				} else if("edit".equals(cmd)) {
					launchEditor(ureq, row);
				} else if("copy".equals(cmd)) {
					doCopy(ureq, row);
				} else if("copy_with_wizard".equals(cmd)) {
					doCopyWithWizard(ureq, row);
				} else if("convertLearningPath".equals(cmd)) {
					doMigrationSelection(ureq, row);
				} else if("download".equals(cmd)) {
					doDownload(ureq, row);
				} else if ("indexMetadata".equals(cmd)) {
					doIndexMetadata(row);
				} else if("close".equals(cmd)) {
					doCloseResource(ureq, row);
				} else if("override-close".equals(cmd)) {
					doOverrideCloseResource(ureq, row);
				} else if("delete".equals(cmd)) {
					doDelete(ureq, Collections.singletonList(row));
				}
			}
		}
	}
	
	private class DeletedToolsController extends BasicController {

		private final VelocityContainer mainVC;
		
		private final boolean isOwner;
		private final boolean isAuthor;
		private final AuthoringEntryRow row;
		
		public DeletedToolsController(UserRequest ureq, WindowControl wControl, AuthoringEntryRow row, RepositoryEntry entry) {
			super(ureq, wControl);
			this.row = row;
			setTranslator(AuthorListController.this.getTranslator());
			
			boolean isManager = repositoryService.hasRoleExpanded(getIdentity(), entry,
					OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name());
			isOwner = isManager || repositoryService.hasRole(getIdentity(), entry, GroupRoles.owner.name());
			isAuthor = isManager || repositoryService.hasRoleExpanded(getIdentity(), entry, OrganisationRoles.author.name());

			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(entry);

			mainVC = createVelocityContainer("tools");
			List<String> links = new ArrayList<>();

			boolean copyManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.copy);
			boolean canCopy = (isAuthor || isOwner) && (entry.getCanCopy() || isOwner) && !copyManaged;
			
			boolean canDownload = entry.getCanDownload() && handler.supportsDownload();
			// disable download for courses if not author or owner
			if (entry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName()) && !(isOwner || isAuthor)) {
				canDownload = false;
			}
			// always enable download for owners
			if (isOwner && handler.supportsDownload()) {
				canDownload = true;
			}
			
			if(canCopy || canDownload) {
				if (canCopy) {
					addLink("details.copy", "copy", "o_icon o_icon-fw o_icon_copy", links);
				}
				if(canDownload) {
					addLink("details.download", "download", "o_icon o_icon-fw o_icon_download", links);
				}
			}
			
			if(isOwner) {
				addLink("tools.restore", "restore", "o_icon o_icon-fw o_icon_restore", links);
			}
			
			if(isManager && !RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.delete)) {
				addLink("details.delete.permanently", "delete", "o_icon o_icon-fw o_icon_delete_item", links);
			}

			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS, List<String> links) {
			Link link = LinkFactory.createLink(name, cmd, this.getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			links.add(name);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if("copy".equals(cmd)) {
					doCopy(ureq, row);
				} else if("download".equals(cmd)) {
					doDownload(ureq, row);
				} else if("restore".equals(cmd)) {
					doRestore(ureq, Collections.singletonList(row));
				} else if("delete".equals(cmd)) {
					doDeletePermanently(ureq, Collections.singletonList(row));
				}
			}
		}
	}
}