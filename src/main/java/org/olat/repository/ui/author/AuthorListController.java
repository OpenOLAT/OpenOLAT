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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.NewControllerFactory;
import org.olat.admin.help.ui.HelpAdminController;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.help.HelpLinkSPI;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.ui.LicenseRenderer;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
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
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
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
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.ui.UnsupportedCourseNodesController;
import org.olat.course.nodes.CourseNode;
import org.olat.login.LoginModule;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.handlers.RepositoryHandlerFactory.OrderedRepositoryHandler;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.OrderBy;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.ResourceUsage;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.ui.author.AuthoringEntryDataModel.Cols;
import org.olat.repository.wizard.RepositoryWizardProvider;
import org.olat.resource.references.Reference;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.comx
 *
 */
public class AuthorListController extends FormBasicController implements Activateable2, AuthoringEntryDataSourceUIFactory, FlexiTableCssDelegate {

	private static final String[] statusKeys = new String[]{ "all", "active", "closed" };
	
	private SingleSelection closedEl;
	protected FlexiTableElement tableEl;
	private final TooledStackedPanel stackPanel;
	
	private final String i18nName;
	private final boolean withSearch;
	private final boolean withClosedfilter;
	
	private AuthoringEntryDataModel model;
	private AuthoringEntryDataSource dataSource;
	private final SearchAuthorRepositoryEntryViewParams searchParams;

	private ToolsController toolsCtrl;
	protected CloseableModalController cmc;
	private SendMailController sendMailCtrl;
	private StepsMainRunController wizardCtrl;
	private StepsMainRunController modifyOwnersWizardCtrl;
	private AuthorSearchController searchCtrl;
	private UserSearchController userSearchCtr;
	private DialogBoxController copyDialogCtrl;
	private ReferencesController referencesCtrl;
	private CopyRepositoryEntryController copyCtrl;
	private ConfirmCloseController closeCtrl;
	private ConfirmDeleteSoftlyController confirmDeleteCtrl;
	private ImportRepositoryEntryController importCtrl;
	private ImportURLRepositoryEntryController importUrlCtrl;
	private CreateEntryController createCtrl;
	private UnsupportedCourseNodesController unsupportedCourseNodesCtrl;
	protected CloseableCalloutWindowController toolsCalloutCtrl;
	
	protected boolean hasAuthorRight;
	protected boolean hasAdministratorRight;
	
	private Link importLink;
	private Link importUrlLink;
	private FormLink copyButton;
	private FormLink deleteButton;
	private FormLink sendMailButton;
	private FormLink modifyOwnersButton;

	private LockResult lockResult;
	private final boolean taxonomyEnabled;
	private final AtomicInteger counter = new AtomicInteger();
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
	protected RepositoryModule repositoryModule;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected RepositoryManager repositoryManager;
	@Autowired
	protected RepositoryHandlerFactory repositoryHandlerFactory;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;
	@Autowired
	private LearningPathService learningPathService;
	@Autowired
	private HelpModule helpModule;
	@Autowired
	private TaxonomyModule taxonomyModule;

	public AuthorListController(UserRequest ureq, WindowControl wControl, String i18nName,
			SearchAuthorRepositoryEntryViewParams searchParams, boolean withSearch, boolean withClosedfilter) {
		super(ureq, wControl, "entries");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));

		this.i18nName = i18nName;
		this.withSearch = withSearch;
		this.searchParams = searchParams;
		this.withClosedfilter = withClosedfilter;

		OLATResourceable ores = OresHelper.createOLATResourceableType("RepositorySite");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		
		Roles roles = ureq.getUserSession().getRoles();
		hasAdministratorRight = roles.isAdministrator() || roles.isLearnResourceManager();
		hasAuthorRight =  hasAdministratorRight || roles.isAuthor();
		
		taxonomyEnabled = taxonomyModule.isEnabled() && StringHelper.isLong(repositoryModule.getTaxonomyTreeKey());
		
		dataSource = new AuthoringEntryDataSource(searchParams, this, !withSearch, taxonomyEnabled);
		initForm(ureq);

		stackPanel = new TooledStackedPanel(i18nName, getTranslator(), this);
		stackPanel.pushController(translate(i18nName), this);
		initTools(ureq);
	}
	
	protected void initTools(UserRequest ureq) {
		if(!withSearch && hasAuthorRight) {
			importLink = LinkFactory.createLink("cmd.import.ressource", getTranslator(), this);
			importLink.setDomReplacementWrapperRequired(false);
			importLink.setIconLeftCSS("o_icon o_icon_import");
			importLink.setElementCssClass("o_sel_author_import");
			stackPanel.addTool(importLink, Align.left);
			
			importUrlLink = LinkFactory.createLink("cmd.import.url.ressource", getTranslator(), this);
			importUrlLink.setDomReplacementWrapperRequired(false);
			importUrlLink.setIconLeftCSS("o_icon o_icon_import");
			importUrlLink.setElementCssClass("o_sel_author_url_import");
			stackPanel.addTool(importUrlLink, Align.left);
			
			List<OrderedRepositoryHandler> handlers = repositoryHandlerFactory.getOrderRepositoryHandlers();
			Dropdown createDropdown = new Dropdown("cmd.create.ressource", "cmd.create.ressource", false, getTranslator());
			createDropdown.setElementCssClass("o_sel_author_create");
			createDropdown.setIconCSS("o_icon o_icon_add");
			int lastGroup = 0;
			for(OrderedRepositoryHandler orderedHandler:handlers) {
				RepositoryHandler handler = orderedHandler.getHandler();
				
				if(handler != null && handler.supportCreate(getIdentity(), ureq.getUserSession().getRoles())) {
					// for each 10-group, create a separator
					int group = orderedHandler.getOrder() / 10;
					if (group > lastGroup) {
						createDropdown.addComponent(new Spacer("spacer" + orderedHandler.getOrder()));
						lastGroup = group;
					}
					addCreateLink(handler, createDropdown);
				}
			}
			stackPanel.addTool(createDropdown, Align.left);
			
			// Help module
			if (helpModule.isHelpEnabled()) {
				VelocityContainer helpVC = createVelocityContainer("help");
				helpVC.setTranslator(Util.createPackageTranslator(HelpAdminController.class, getLocale()));
				List<HelpLinkSPI> helpLinks = helpModule.getAuthorSiteHelpPlugins();
				
				if (helpLinks.size() == 1) {
					stackPanel.addTool(helpLinks.get(0).getHelpUserTool(getWindowControl()).getMenuComponent(ureq, helpVC), Align.right);
				} else if (helpLinks.size() > 1) {
					Dropdown helpDropdown = new Dropdown("help.list", "help.authoring", false, Util.createPackageTranslator(HelpAdminController.class, getLocale()));
					helpDropdown.setIconCSS("o_icon o_icon_help");
					helpDropdown.setOrientation(DropdownOrientation.right);
					
					for (HelpLinkSPI helpLinkSPI : helpLinks) {
						helpDropdown.addComponent(helpLinkSPI.getHelpUserTool(getWindowControl()).getMenuComponent(ureq, helpVC));
					}
					
					stackPanel.addTool(helpDropdown, Align.right);
				}
			}
		}
	}
	
	public String getI18nName() {
		return i18nName;
	}
	
	@Override
	public Translator getTranslator() {
		return super.getTranslator();
	}

	private void addCreateLink(RepositoryHandler handler, Dropdown dropdown) {
		Link createLink = LinkFactory.createLink(handler.getSupportedType(), getTranslator(), this);
		createLink.setIconLeftCSS("o_icon o_icon-fw " + RepositoyUIFactory.getIconCssClass(handler.getSupportedType()));
		createLink.setElementCssClass("o_sel_author_create-" + handler.getSupportedType());
		createLink.setUserObject(handler);
		dropdown.addComponent(createLink);
	}
	
	public boolean isEmpty() {
		return dataSource.getRowCount() == 0;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//search form
		if(withSearch) {
			setFormDescription("table.search.author.desc");
			searchCtrl = new AuthorSearchController(ureq, getWindowControl(), true, mainForm);
			searchCtrl.setEnabled(false);
			listenTo(searchCtrl);
		}
		
		if(withClosedfilter) {
			String[] statusValues = new String[] {
					translate("cif.resources.status.all"),
					translate("cif.resources.status.active"),
					translate("cif.resources.status.closed")
				};
			closedEl = uifactory.addRadiosHorizontal("cif_status", "cif.resources.status", formLayout, statusKeys, statusValues);
			closedEl.addActionListener(FormEvent.ONCHANGE);
			closedEl.setDomReplacementWrapperRequired(false);
			closedEl.select(statusKeys[1], true);
			searchParams.setClosed(Boolean.FALSE);
		}
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18nKey(), Cols.key.ordinal(), true, OrderBy.key.name()));
		DefaultFlexiColumnModel markColumn = new DefaultFlexiColumnModel(true, Cols.mark.i18nKey(), Cols.mark.ordinal(), true, OrderBy.favorit.name());
		markColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(markColumn);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.type.i18nKey(), Cols.type.ordinal(), true, OrderBy.type.name(),
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lifecycleLabel.i18nKey(), Cols.lifecycleLabel.ordinal(),
				true, OrderBy.lifecycleLabel.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lifecycleSoftkey.i18nKey(), Cols.lifecycleSoftkey.ordinal(),
				true, OrderBy.lifecycleSoftkey.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lifecycleStart.i18nKey(), Cols.lifecycleStart.ordinal(),
				true, OrderBy.lifecycleStart.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lifecycleEnd.i18nKey(), Cols.lifecycleEnd.ordinal(),
				true, OrderBy.lifecycleEnd.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		if (taxonomyEnabled) {
			DefaultFlexiColumnModel taxonomyLevelColumnModel = new DefaultFlexiColumnModel(true, Cols.taxonomyLevels.i18nKey(),
					Cols.taxonomyLevels.ordinal(), false, null);
			taxonomyLevelColumnModel.setCellRenderer(new TaxonomyLevelRenderer());
			columnsModel.addFlexiColumnModel(taxonomyLevelColumnModel);
			DefaultFlexiColumnModel taxonomyLevelPathColumnModel = new DefaultFlexiColumnModel(true, Cols.taxonomyPaths.i18nKey(),
					Cols.taxonomyPaths.ordinal(), false, null);
			taxonomyLevelPathColumnModel.setCellRenderer(new TaxonomyPathsRenderer());
			columnsModel.addFlexiColumnModel(taxonomyLevelPathColumnModel);
		}
		DefaultFlexiColumnModel educationalTypeColumnModel = new DefaultFlexiColumnModel(false, Cols.educationalType.i18nKey(),
				Cols.educationalType.ordinal(), false, null);
		educationalTypeColumnModel.setCellRenderer(new EducationalTypeRenderer());
		columnsModel.addFlexiColumnModel(educationalTypeColumnModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.author.i18nKey(), Cols.author.ordinal(),
				true, OrderBy.author.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.access.i18nKey(), Cols.access.ordinal(),
				true, OrderBy.access.name(), FlexiColumnModel.ALIGNMENT_LEFT, new AccessRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.ac.i18nKey(), Cols.ac.ordinal(),
				true, OrderBy.ac.name(), FlexiColumnModel.ALIGNMENT_LEFT, new ACRenderer()));
		if(loginModule.isGuestLoginEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.guests.i18nKey(), Cols.guests.ordinal(),
					true, OrderBy.guests.name(), FlexiColumnModel.ALIGNMENT_LEFT, new GuestAccessRenderer(getLocale())));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.creationDate.i18nKey(), Cols.creationDate.ordinal(),
				true, OrderBy.creationDate.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastUsage.i18nKey(), Cols.lastUsage.ordinal(),
				true, OrderBy.lastUsage.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.references.i18nKey(), Cols.references.ordinal(),
				true, OrderBy.references.name()));
		
		if(lectureModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lectureInfos.i18nKey(), Cols.lectureInfos.ordinal(),
				true, OrderBy.lectureEnabled.name(), FlexiColumnModel.ALIGNMENT_LEFT, new LectureInfosRenderer(getTranslator())));
		}
		if (licenseModule.isEnabled(licenseHandler)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, false, Cols.license.i18nKey(), null, Cols.license.ordinal(), "license", false, null, FlexiColumnModel.ALIGNMENT_LEFT,
					 new StaticFlexiCellRenderer("license", new LicenseRenderer(getLocale()))));
		}
		
		initActionsColumns(columnsModel);
		
		model = new AuthoringEntryDataModel(dataSource, columnsModel, getIdentity(), ureq.getUserSession().getRoles());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(withSearch);
		tableEl.setCssDelegate(this);
		tableEl.setExportEnabled(true);
		tableEl.setExtendedSearch(searchCtrl);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_coursetable");
		tableEl.setShowAllRowsEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setSortSettings(new FlexiTableSortOptions(true, new SortKey(OrderBy.displayname.name(), true)));
		tableEl.setAndLoadPersistedPreferences(ureq, "authors-list-" + i18nName);
		if (withSearch) {
			tableEl.setEmptyTableSettings("author.search.empty", "author.search.empty.hint", "o_CourseModule_icon");
		} else {
			tableEl.setEmptyTableSettings("author.list.empty", "author.list.empty.hint", "o_CourseModule_icon");
			tableEl.reloadData();
			tableEl.setFilters(null, getFilters(), false);			
		}
		initBatchButtons(formLayout);
	}
	
	protected void initActionsColumns(FlexiTableColumnModel columnsModel) {
		DefaultFlexiColumnModel detailsColumn = new DefaultFlexiColumnModel(Cols.detailsSupported.i18nKey(), Cols.detailsSupported.ordinal(), "details",
				new StaticFlexiCellRenderer("", "details", "o_icon o_icon-lg o_icon_details", translate("details")));
		detailsColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(detailsColumn);
		if(hasAuthorRight) {
			DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel(Cols.editionSupported.i18nKey(), Cols.editionSupported.ordinal(), "edit",
				new BooleanCellRenderer(new StaticFlexiCellRenderer("", "edit", "o_icon o_icon-lg o_icon_edit", translate("edit")), null));
			editColumn.setExportable(false);
			columnsModel.addFlexiColumnModel(editColumn);
			DefaultFlexiColumnModel toolsColumn = new DefaultFlexiColumnModel(Cols.tools.i18nKey(), Cols.tools.ordinal());
			toolsColumn.setExportable(false);
			columnsModel.addFlexiColumnModel(toolsColumn);
		}
	}
	
	protected void initBatchButtons(FormItemContainer formLayout) {
		if(hasAuthorRight) {			
			sendMailButton = uifactory.addFormLink("tools.send.mail", formLayout, Link.BUTTON);
			tableEl.addBatchButton(sendMailButton);
			modifyOwnersButton = uifactory.addFormLink("tools.modify.owners", formLayout, Link.BUTTON);
			tableEl.addBatchButton(modifyOwnersButton);
			copyButton = uifactory.addFormLink("details.copy", formLayout, Link.BUTTON);
			tableEl.addBatchButton(copyButton);
			deleteButton = uifactory.addFormLink("details.delete", formLayout, Link.BUTTON);
			tableEl.addBatchButton(deleteButton);
		}
	}
	
	private List<FlexiTableFilter> getFilters() {
		List<OrderedRepositoryHandler> supportedHandlers = repositoryHandlerFactory.getOrderRepositoryHandlers();
		List<FlexiTableFilter> resources = new ArrayList<>(supportedHandlers.size() + 1);
		int lastGroup = 0;
		for(OrderedRepositoryHandler handler:supportedHandlers) {
			// for each 10-group, crate a separator
			int group = handler.getOrder() / 10;
			if (group > lastGroup) {
				resources.add(FlexiTableFilter.SPACER);
				lastGroup = group;
			}
			String type = handler.getHandler().getSupportedType();
			String inconLeftCss = RepositoyUIFactory.getIconCssClass(type);
			resources.add(new FlexiTableFilter(translate(type), type, inconLeftCss));
		}
		return resources;
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

	public TooledStackedPanel getStackPanel() {
		return stackPanel;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(state instanceof AuthorListState) {
			AuthorListState se = (AuthorListState)state;
			if(se.getTableState() != null) {
				tableEl.setStateEntry(ureq, se.getTableState());
			}
			if(se.getSearchEvent() != null) {
				searchCtrl.update(se.getSearchEvent());
				doExtendedSearch(ureq, se.getSearchEvent());
			}
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(importLink == source) {
			doImport(ureq);
		} else if(importUrlLink == source) {
			doImportUrl(ureq);
		} else if(source instanceof Link && ((Link)source).getUserObject() instanceof RepositoryHandler) {
			RepositoryHandler handler = (RepositoryHandler)((Link)source).getUserObject();
			if(handler != null) {
				doCreate(ureq, handler);
			}
		}
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
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				reloadRows();
				launchEditDescription(ureq, copyCtrl.getCopiedEntry());
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
		} else if(modifyOwnersWizardCtrl == source) {
			if (event.equals(Event.CHANGED_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
				getWindowControl().pop();
				
				reloadRows();
				cleanUp();
			}
		} else if(searchCtrl == source) {
			if(event instanceof SearchEvent) {
				SearchEvent se = (SearchEvent)event;
				doExtendedSearch(ureq, se);
			} else if(event == Event.CANCELLED_EVENT) {
				doResetExtendedSearch(ureq);
			}
		} else if(userSearchCtr == source) {
			@SuppressWarnings("unchecked")
			List<AuthoringEntryRow> rows = (List<AuthoringEntryRow>)userSearchCtr.getUserObject();
			if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent mice = (MultiIdentityChosenEvent) event; 
				doAddOwners(mice.getChosenIdentities(), rows);
			} else if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent  sice = (SingleIdentityChosenEvent) event;
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
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		} else if(referencesCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
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
		} else if(copyDialogCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				@SuppressWarnings("unchecked")
				List<AuthoringEntryRow> rows = (List<AuthoringEntryRow>)copyDialogCtrl.getUserObject();
				doCompleteCopy(rows);
				reloadRows();
			}
		}
		super.event(ureq, source, event);
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(modifyOwnersWizardCtrl);
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(userSearchCtr);
		removeAsListenerAndDispose(importUrlCtrl);
		removeAsListenerAndDispose(sendMailCtrl);
		removeAsListenerAndDispose(createCtrl);
		removeAsListenerAndDispose(importCtrl);
		removeAsListenerAndDispose(wizardCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(closeCtrl);
		removeAsListenerAndDispose(cmc);
		modifyOwnersWizardCtrl = null;
		confirmDeleteCtrl = null;
		toolsCalloutCtrl = null;
		userSearchCtr = null;
		importUrlCtrl = null;
		sendMailCtrl = null;
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
		if(modifyOwnersButton == source) {
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
		} else if(deleteButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows();
			if(!rows.isEmpty()) {
				doDelete(ureq, rows);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if(closedEl == source) {
			doSetClosedFilter();
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("mark".equals(cmd)) {
				AuthoringEntryRow row = (AuthoringEntryRow)link.getUserObject();
				boolean marked = doMark(ureq, row);
				link.setIconLeftCSS(marked ? "o_icon o_icon_bookmark o_icon-lg" : "o_icon o_icon_bookmark_add o_icon-lg");
				link.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
				link.getComponent().setDirty(true);
				row.setMarked(marked);
			} else if("tools".equals(cmd)) {
				AuthoringEntryRow row = (AuthoringEntryRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			} else if("references".equals(cmd)) {
				AuthoringEntryRow row = (AuthoringEntryRow)link.getUserObject();
				doOpenReferences(ureq, row, link);
			}
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AuthoringEntryRow row = model.getObject(se.getIndex());
				if("details".equals(cmd)) {
					launchDetails(ureq, row);
				} else if("edit".equals(cmd)) {
					launchEditor(ureq, row);
				} else if("select".equals(cmd)) {
					launch(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				AuthorListState stateEntry = new AuthorListState();
				stateEntry.setTableState(tableEl.getStateEntry());
				addToHistory(ureq, stateEntry);
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

	private void doOpenTools(UserRequest ureq, AuthoringEntryRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
		if(entry == null) {
			tableEl.reloadData();
			showWarning("repositoryentry.not.existing");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row, entry);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doOpenReferences(UserRequest ureq, AuthoringEntryRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
		if(entry == null) {
			tableEl.reloadData();
			showWarning("repositoryentry.not.existing");
		} else {
			referencesCtrl = new ReferencesController(ureq, getWindowControl(), entry);
			listenTo(referencesCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					referencesCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
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
		
		String title = translate("cmd.import.ressource");
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
			EntryChangedEvent e = new EntryChangedEvent(row, getIdentity(), Change.removeBookmark, i18nName);
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
			return false;
		}
		
		String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
		markManager.setMark(item, getIdentity(), null, businessPath);
		
		dbInstance.commit();//before sending, save the changes
		EntryChangedEvent e = new EntryChangedEvent(row, getIdentity(), Change.addBookmark, i18nName);
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
	
	private void doResetExtendedSearch(UserRequest ureq) {
		searchParams.setResourceTypes(null);
		searchParams.setEducationalTypeKeys(null);
		searchParams.setIdAndRefs(null);
		searchParams.setAuthor(null);
		searchParams.setOwnedResourcesOnly(false);
		searchParams.setResourceUsage(ResourceUsage.all);
		searchParams.setClosed(null);
		searchParams.setDisplayname(null);
		searchParams.setDescription(null);
		searchParams.setLicenseTypeKeys(null);
		searchParams.setEntryOrganisations(null);
		searchParams.setTaxonomyLevels(null);
		
		tableEl.resetSearch(ureq);
		flc.setDirty(true);
	}
	
	private void doExtendedSearch(UserRequest ureq, SearchEvent se) {
		if(se.getTypes() != null && !se.getTypes().isEmpty()) {
			searchParams.setResourceTypes(new ArrayList<>(se.getTypes()));
		} else {
			searchParams.setResourceTypes(null);
		}
		searchParams.setTechncialTypes(se.getTechnicalTypes());
		searchParams.setEducationalTypeKeys(se.getEducationalTypeKeys());

		searchParams.setIdAndRefs(se.getId());
		searchParams.setAuthor(se.getAuthor());
		searchParams.setOwnedResourcesOnly(se.isOwnedResourcesOnly());
		searchParams.setResourceUsage(se.getResourceUsage());
		searchParams.setClosed(se.getClosed());
		searchParams.setDisplayname(se.getDisplayname());
		searchParams.setDescription(se.getDescription());
		searchParams.setLicenseTypeKeys(se.getLicenseTypeKeys());
		searchParams.setEntryOrganisations(se.getEntryOrganisations());
		searchParams.setTaxonomyLevels(se.getTaxonomyLevels());
		tableEl.reset(true, true, true);
		flc.setDirty(true);

		AuthorListState stateEntry = new AuthorListState();
		stateEntry.setSearchEvent(se);
		stateEntry.setTableState(tableEl.getStateEntry());
		addToHistory(ureq, stateEntry);
	}
	
	protected List<AuthoringEntryRow> getMultiSelectedRows() {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<AuthoringEntryRow> rows = new ArrayList<>(selections.size());
		if(!selections.isEmpty()) {
			for(Integer i:selections) {
				AuthoringEntryRow row = model.getObject(i.intValue());
				if(row != null) {
					rows.add(row);
				}
			}
		}
		return rows;
	}
	
	private void doSetClosedFilter() {
		searchParams.setClosed(null);
		if(closedEl.isOneSelected()) {
			int selected = closedEl.getSelected();
			if(selected == 1) {
				searchParams.setClosed(Boolean.FALSE);
			} else if(selected == 2) {
				searchParams.setClosed(Boolean.TRUE);
			}
		}
		tableEl.reset(true, true, true);
	}
	
	private void doSendMail(UserRequest ureq, List<AuthoringEntryRow> rows) {
		if(guardModalController(sendMailCtrl)) return;

		removeAsListenerAndDispose(userSearchCtr);
		sendMailCtrl = new SendMailController(ureq, getWindowControl(), rows);
		listenTo(sendMailCtrl);
		
		String title = translate("tools.send.mail");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), sendMailCtrl.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
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
			
			
			
			
//			removeAsListenerAndDispose(userSearchCtr);
//			userSearchCtr = new UserSearchController(ureq, getWindowControl(), false, true, UserSearchController.ACTION_KEY_CHOOSE_FINISH);
//			userSearchCtr.setUserObject(manageableRows);
//			listenTo(userSearchCtr);
//			
//			String title = translate("tools.add.owners");
//			cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtr.getInitialComponent(),
//					true, title);
//			listenTo(cmc);
//			cmc.activate();
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
			repositoryService.copy(sourceEntry, getIdentity(), displayname);
		}
		
		showInfo("details.copy.success", new String[]{ Integer.toString(rows.size()) });
	}
	
	private void doCloseResource(UserRequest ureq, AuthoringEntryRow row) {
		removeAsListenerAndDispose(closeCtrl);
		
		RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
		
		List<RepositoryEntry> entryToClose = Collections.singletonList(entry);
		closeCtrl = new ConfirmCloseController(ureq, getWindowControl(), entryToClose);
		listenTo(closeCtrl);
		
		String title = translate("read.only.header", entry.getDisplayname());
		cmc = new CloseableModalController(getWindowControl(), "close", closeCtrl.getInitialComponent(), true, title);
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
	
	private void doConvertToLearningPath(UserRequest ureq, AuthoringEntryRow row) {
		RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
		ICourse course = CourseFactory.loadCourse(entry);
		List<CourseNode> unsupportedCourseNodes = learningPathService.getUnsupportedCourseNodes(course);
		if (!unsupportedCourseNodes.isEmpty()) {
			showUnsupportedMessage(ureq, unsupportedCourseNodes);
			return;
		}
		
		RepositoryEntry lpEntry = learningPathService.migrate(entry, getIdentity());
		String bPath = "[RepositoryEntry:" + lpEntry.getKey() + "]";
		NewControllerFactory.getInstance().launch(bPath, ureq, getWindowControl());
	}

	private void showUnsupportedMessage(UserRequest ureq, List<CourseNode> unsupportedCourseNodes) {
		unsupportedCourseNodesCtrl = new UnsupportedCourseNodesController(ureq, getWindowControl(), unsupportedCourseNodes);
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
			MediaResource mr = typeToDownload.getAsMediaResource(ores);
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
				String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
				if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
					tableEl.reloadData();
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

	@Override
	public void forgeLinks(AuthoringEntryRow row) {
		//mark
		FormLink markLink = uifactory.addFormLink("mark_" + counter.incrementAndGet(), "mark", "", null, null, Link.NONTRANSLATED);
		markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setTitle(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
		//tools
		FormLink toolsLink = uifactory.addFormLink("tools_" + counter.incrementAndGet(), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
		//references
		if(row.getNumOfReferences() > 0) {
			String numOfReferences = Integer.toString(row.getNumOfReferences());
			FormLink referencesLink = uifactory.addFormLink("tools_" + counter.incrementAndGet(), "references", numOfReferences, null, null, Link.NONTRANSLATED);
			referencesLink.setUserObject(row);
			row.setReferencesLink(referencesLink);
		}
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
	
	private class FinishedCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
            ModifyOwnersContext context = (ModifyOwnersContext) runContext.get(ModifyOwnersContext.CONTEXT_KEY);
            	
            List<Identity> removeOwnersList = context.getOwnersToRemove();
            removeOwnersList.removeAll(context.getOwnersToAdd());
            
            List<Identity> addOwnersList = context.getOwnersToAdd();
            addOwnersList.removeAll(context.getOwnersToRemove());
            
            MailPackage mailing = new MailPackage(context.isSendMail());
            
            for (AuthoringEntryRow resource : context.getAuthoringEntryRows()) {
            	RepositoryEntry repoEntry = repositoryService.loadByKey(resource.getKey());
            	
		        repositoryManager.removeOwners(getIdentity(), removeOwnersList, repoEntry, mailing);
		        
		        IdentitiesAddEvent addEvent = new IdentitiesAddEvent(addOwnersList);
		        repositoryManager.addOwners(getIdentity(), addEvent, repoEntry, mailing);
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
		
		public ReferencesController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
			super(ureq, wControl);
			setTranslator(AuthorListController.this.getTranslator());
			VelocityContainer mainVC = createVelocityContainer("references");
			
			List<RepositoryEntry> refs = referenceManager.getRepositoryReferencesTo(entry.getOlatResource());

			List<String> refLinks = new ArrayList<>(refs.size());
			for(RepositoryEntry ref:refs) {
				String name = "ref-" + counter.incrementAndGet();
				Link refLink = LinkFactory.createLink(name, "reference", getTranslator(), mainVC, this, Link.NONTRANSLATED);
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
						new String[] { String.valueOf(references.size()) }));
			}
			
			if(BinderTemplateResource.TYPE_NAME.equals(entry.getOlatResource().getResourceableTypeName())) {
				int usage = portfolioService.getTemplateUsage(entry);
				if(usage > 0) {
					mainVC.contextPut("binderTemplateUsage", translate("details.referenceinfo.binder.template",
							new String[] { String.valueOf(usage) }));
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
				}
			}
		}

		@Override
		protected void doDispose() {
			//
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
				if(repositoryModule.isCatalogEnabled()) {
					addLink("tools.edit.catalog", "catalog", "o_icon o_icon-fw o_icon_catalog", "/Settings/0/Catalog/0", links);
				}
				addLink("details.members", "members", "o_icon o_icon-fw o_icon_membersmanagement", "/MembersMgmt/0", links);
			}
			
			boolean copyManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.copy);
			boolean canCopy = (isAuthor || isOwner) && (entry.getCanCopy() || isOwner) && !copyManaged;
			
			boolean canConvertLearningPath = false;
			if (canCopy && "CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
				ICourse course = CourseFactory.loadCourse(entry);
				if (course != null) {
					if (!LearningPathNodeAccessProvider.TYPE.equals(course.getCourseConfig().getNodeAccessType().getType())) {
						canConvertLearningPath = true;
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
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if("description".equals(cmd)) {
					launchEditDescription(ureq, row);
				} else if("catalog".equals(cmd)) {
					launchCatalog(ureq, row);
				}  else if("members".equals(cmd)) {
					launchMembers(ureq, row);
				} else if("copy".equals(cmd)) {
					doCopy(ureq, row);
				} else if("convertLearningPath".equals(cmd)) {
					doConvertToLearningPath(ureq, row);
				} else if("download".equals(cmd)) {
					doDownload(ureq, row);
				} else if("close".equals(cmd)) {
					doCloseResource(ureq, row);
				} else if("override-close".equals(cmd)) {
					doOverrideCloseResource(ureq, row);
				} else if("delete".equals(cmd)) {
					doDelete(ureq, Collections.singletonList(row));
				}
			}
		}

		@Override
		protected void doDispose() {
			//
		}
	}
}