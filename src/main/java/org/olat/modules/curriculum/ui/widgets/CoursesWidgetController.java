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
package org.olat.modules.curriculum.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl.SelectionMode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.EmptyPanelItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.ConfirmInstantiateTemplateController;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.CurriculumListManagerController;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.curriculum.ui.widgets.CoursesWidgetDataModel.EntriesCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.author.AccessRenderer;
import org.olat.repository.ui.author.AuthorListConfiguration;
import org.olat.repository.ui.author.AuthorListController;
import org.olat.repository.ui.author.AuthoringEntryRowSelectionEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoursesWidgetController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private static final String CMD_OPEN = "open";
	private static final String CMD_INSTANTIATE = "instantiate";
	
	private FormLink coursesLink;
	private FormLink addMenuButton;
	private FormLink addTemplateButton;
	private FormLink addResourceButton;
	private EmptyPanelItem emptyList;
	private FlexiTableElement entriesTableEl;
	private CoursesWidgetDataModel entriesTableModel;

	private final boolean resourcesManaged;
	private final MapperKey mapperThumbnailKey;
	private final CurriculumElement curriculumElement;
	private final CurriculumSecurityCallback secCallback;
	private final CurriculumElementType curriculumElementType;
	
	private CloseableModalController cmc;
	private AddMenuController addMenuCtrl;
	private AuthorListController repoSearchCtr;
	private AuthorListController templateSearchCtr;
	private CloseableCalloutWindowController menuCalloutCtrl;
	private ConfirmInstantiateTemplateController confirmInstantiateCtrl;

	@Autowired
	private MapperService mapperService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	
	public CoursesWidgetController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "courses_widget", Util.createPackageTranslator(CurriculumComposerController.class, ureq.getLocale()));
		mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper(900, 600));
		
		this.secCallback = secCallback;
		this.curriculumElement = curriculumElement;
		this.curriculumElementType = curriculumElement.getType();
		resourcesManaged = CurriculumElementManagedFlag.isManaged(curriculumElement, CurriculumElementManagedFlag.resources);
		
		initForm(ureq);
		loadModel();
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		if(rowObject instanceof CourseWidgetRow entryRow) {
			List<Component> links = new ArrayList<>(2);
			if(entryRow.getOpenLink() != null) {
				links.add(entryRow.getOpenLink().getComponent());
			}
			if(entryRow.getInstantiateTemplateLink() != null) {
				links.add(entryRow.getInstantiateTemplateLink().getComponent());
			}
			return links;
		}
		return List.of();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		coursesLink = uifactory.addFormLink("curriculum.courses", formLayout);
		coursesLink.setIconRightCSS("o_icon o_icon-fw o_icon_course_next");
		
		if(!resourcesManaged && secCallback.canManagerCurriculumElementResources(curriculumElement)) {
			if(curriculumElementType == null || curriculumElementType.getMaxRepositoryEntryRelations() != 0) {
				addResourceButton = uifactory.addFormLink("add.resource", "", null, formLayout, Link.LINK | Link.NONTRANSLATED);
				addResourceButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
				addResourceButton.setTitle("add.resource");
			}

			if(curriculumElementType != null && curriculumElementType.getMaxRepositoryEntryRelations() == 1) {
				addMenuButton = uifactory.addFormLink("add.menu", "", null, formLayout, Link.LINK | Link.NONTRANSLATED);
				addMenuButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
				addMenuButton.setTitle("add.menu");
				
				addTemplateButton = uifactory.addFormLink("add.template", "add.template", null, formLayout, Link.LINK | Link.NONTRANSLATED);
				addTemplateButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
				addTemplateButton.setTitle("add.template");
			}
		}

		emptyList = uifactory.addEmptyPanel("course.empty", null, formLayout);
		emptyList.setTitle(translate("curriculum.no.course.assigned.title"));
		emptyList.setIconCssClass("o_icon o_icon-lg o_CourseModule_icon");

		initFormTable(formLayout);
	}
	
	private void initFormTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, EntriesCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntriesCols.displayName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntriesCols.externalRef));
		
		entriesTableModel = new CoursesWidgetDataModel(columnsModel);
		entriesTableEl = uifactory.addTableElement(getWindowControl(), "entriesTable", entriesTableModel, 25, false, getTranslator(), formLayout);
		entriesTableEl.setCustomizeColumns(false);
		entriesTableEl.setNumOfRowsEnabled(false);
		entriesTableEl.setSelection(true, false, false);
		entriesTableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		entriesTableEl.setRendererType(FlexiTableRendererType.custom);
		entriesTableEl.setNumOfRowsEnabled(false);
		entriesTableEl.setCssDelegate(new EntriesDelegate());
		
		VelocityContainer row = new VelocityContainer(null, "vc_row1", velocity_root + "/entry_1.html",
				getTranslator(), this);
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		entriesTableEl.setRowRenderer(row, this);
	}
	
	public void loadModel() {
		List<RepositoryEntry> repositoryEntries = curriculumService.getRepositoryEntries(curriculumElement);
		List<RepositoryEntry> repositoryTemplates = curriculumService.getRepositoryTemplates(curriculumElement);
		final int numOfEntries = repositoryEntries.size();
		
		AccessRenderer renderer = new AccessRenderer(getLocale());
		List<CourseWidgetRow> rows = new ArrayList<>();
		for(RepositoryEntry entry:repositoryEntries) {
			rows.add(forgeRow(entry, false, numOfEntries, renderer));
		}
		for(RepositoryEntry template:repositoryTemplates) {
			rows.add(forgeRow(template, true, numOfEntries, renderer));
		}
		entriesTableModel.setObjects(rows);
		entriesTableEl.reset(true, true, true);
		
		boolean empty = rows.isEmpty();
		entriesTableEl.setVisible(!empty);
		emptyList.setVisible(empty);
		
		int maxRelations = curriculumElementType == null ? -1 : curriculumElementType.getMaxRepositoryEntryRelations();
		if(addResourceButton != null) {
			addResourceButton.setVisible(maxRelations == -1 || maxRelations > repositoryEntries.size());
		}
		
		if(addTemplateButton != null) {
			addTemplateButton.setVisible(maxRelations == 1
					&& repositoryEntries.isEmpty() && repositoryTemplates.isEmpty());
		}
		
		if(addMenuButton != null) {
			addMenuButton.setVisible(addResourceButton != null && addResourceButton.isVisible()
					&& addTemplateButton != null && addTemplateButton.isVisible());
		}
	}
	
	private CourseWidgetRow forgeRow(RepositoryEntry entry, boolean template, int numOfEntries, AccessRenderer renderer) {
		String displayName = StringHelper.escapeHtml(entry.getDisplayname());
		FormLink openLink = uifactory.addFormLink("open_" + entry.getKey(), "open", displayName, null, flc, Link.NONTRANSLATED);
		final String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString("[RepositoryEntry:" + entry.getKey() + "]");
		openLink.setUrl(url);
		
		FormLink instantiateLink = null;
		if(template && numOfEntries == 0) {
			instantiateLink = uifactory.addFormLink("instantiate_" + entry.getKey(), "instantiate", "instantiate.template", null, flc, Link.BUTTON);
			instantiateLink.setElementCssClass("btn btn-primary");
			instantiateLink.setUserObject(entry);
		}
		
		VFSLeaf image = repositoryManager.getImage(entry.getKey(), entry.getOlatResource());
		String thumbnailUrl = null;
		if(image != null) {
			thumbnailUrl = RepositoryEntryImageMapper.getImageUrl(mapperThumbnailKey.getUrl(), image);
		}
		String status = renderer.renderEntryStatus(entry);
		CourseWidgetRow row = new CourseWidgetRow(entry, template, openLink, instantiateLink, url, thumbnailUrl, status);
		openLink.setUserObject(row);
		return row;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(repoSearchCtr == source) {
			if(event instanceof AuthoringEntryRowSelectionEvent se) {
				doAddRepositoryEntry(se.getRow());
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(templateSearchCtr == source) {
			if(event instanceof AuthoringEntryRowSelectionEvent se) {
				doAddTemplate(se.getRow());
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmInstantiateCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
			
		} else if(addMenuCtrl == source) {
			if(event == Event.CLOSE_EVENT) {
				menuCalloutCtrl.deactivate();
			}
			cleanUp();
		} else if(cmc == source || menuCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(menuCalloutCtrl);
		removeAsListenerAndDispose(repoSearchCtr);
		removeAsListenerAndDispose(addMenuCtrl);
		removeAsListenerAndDispose(cmc);
		menuCalloutCtrl = null;
		repoSearchCtr = null;
		addMenuCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(coursesLink == source) {
			List<ContextEntry> entries = BusinessControlFactory.getInstance()
					.createCEListFromResourceType(CurriculumListManagerController.CONTEXT_RESOURCES);
			fireEvent(ureq, new ActivateEvent(entries));
		} else if(addResourceButton == source) {
			doChooseResources(ureq);
		} else if(addTemplateButton == source) {
			doChooseTemplate(ureq);
		} else if(addMenuButton == source) {
			doOpenMenu(ureq, addMenuButton);
		} else if(source instanceof FormLink link) {
			if(CMD_OPEN.equals(link.getCmd())
					&& link.getUserObject() instanceof CourseWidgetRow row) {
				doOpen(ureq, row);
			} else if(CMD_INSTANTIATE.equals(link.getCmd())
					&& link.getUserObject() instanceof RepositoryEntry template) {
				doInstantiateTemplate(ureq, template);
			}
		} else if(source instanceof FormLayoutContainer) {
			String entryKey = ureq.getParameter("select_entry");
			if(StringHelper.isLong(entryKey)) {
				doOpen(ureq, Long.valueOf(entryKey));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpen(UserRequest ureq, CourseWidgetRow row) {
		doOpen(ureq, row.getKey());
	}
	
	private void doOpen(UserRequest ureq, Long entryKey) {
		String businessPath = "[RepositoryEntry:" + entryKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doChooseResources(UserRequest ureq) {
		if(guardModalController(repoSearchCtr)) return;
		
		Roles roles = ureq.getUserSession().getRoles();
		AuthorListConfiguration tableConfig = AuthorListConfiguration.selectRessource("curriculum-course-v1", "CourseModule");
		tableConfig.setSelectRepositoryEntry(SelectionMode.single);
		tableConfig.setBatchSelect(true);
		tableConfig.setImportRessources(false);
		tableConfig.setCreateRessources(false);
		
		SearchAuthorRepositoryEntryViewParams searchParams = new SearchAuthorRepositoryEntryViewParams(getIdentity(), roles);
		searchParams.addResourceTypes("CourseModule");
		repoSearchCtr = new AuthorListController(ureq, getWindowControl(), searchParams, tableConfig);
		listenTo(repoSearchCtr);
		repoSearchCtr.selectFilterTab(ureq, repoSearchCtr.getMyCoursesTab());
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), repoSearchCtr.getInitialComponent(),
				true, translate("add.resource"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddRepositoryEntry(RepositoryEntryRef entryRef) {
		RepositoryEntry entry = repositoryService.loadBy(entryRef);
		if(entry != null) {
			boolean hasRepositoryEntries = curriculumService.hasRepositoryEntries(curriculumElement);
			boolean moveLectureBlocks = !hasRepositoryEntries;
			curriculumService.addRepositoryEntry(curriculumElement, entry, moveLectureBlocks);
			showInfo("info.repositoryentry.added");
		}
	}
	
	private void doChooseTemplate(UserRequest ureq) {
		if(guardModalController(templateSearchCtr)) return;
		
		Roles roles = ureq.getUserSession().getRoles();
		AuthorListConfiguration tableConfig = AuthorListConfiguration.selectRessource("curriculum-template-v1", "CourseModule");
		tableConfig.setSelectRepositoryEntry(SelectionMode.single);
		tableConfig.setBatchSelect(true);
		tableConfig.setImportRessources(false);
		tableConfig.setCreateRessources(false);
		tableConfig.setAllowedRuntimeTypes(List.of(RepositoryEntryRuntimeType.template));
		
		SearchAuthorRepositoryEntryViewParams searchParams = new SearchAuthorRepositoryEntryViewParams(getIdentity(), roles);
		searchParams.addResourceTypes("CourseModule");
		searchParams.setRuntimeType(RepositoryEntryRuntimeType.template);
		templateSearchCtr = new AuthorListController(ureq, getWindowControl(), searchParams, tableConfig);
		listenTo(templateSearchCtr);
		templateSearchCtr.selectFilterTab(ureq, templateSearchCtr.getMyCoursesTab());
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), templateSearchCtr.getInitialComponent(),
				true, translate("add.template"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddTemplate(RepositoryEntryRef entryRef) {
		RepositoryEntry entry = repositoryService.loadBy(entryRef);
		if(entry != null && entry.getRuntimeType() == RepositoryEntryRuntimeType.template) {
			curriculumService.addRepositoryTemplate(curriculumElement, entry);
			showInfo("info.repositorytemplate.added");
		}
	}
	
	private void doInstantiateTemplate(UserRequest ureq, RepositoryEntry template) {
		confirmInstantiateCtrl = new ConfirmInstantiateTemplateController(ureq, getWindowControl(),
				curriculumElement, template);
		listenTo(confirmInstantiateCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmInstantiateCtrl.getInitialComponent(),
				true, translate("instantiate.template"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenMenu(UserRequest ureq, FormLink link) {
		removeAsListenerAndDispose(addMenuCtrl);
		removeAsListenerAndDispose(menuCalloutCtrl);

		addMenuCtrl = new AddMenuController(ureq, getWindowControl());
		listenTo(addMenuCtrl);

		menuCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				addMenuCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(menuCalloutCtrl);
		menuCalloutCtrl.activate();
	}
	
	private static class EntriesDelegate implements FlexiTableCssDelegate {

		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return null;
		}

		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return "o_cards";
		}

		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return null;
		}
	}
	
	private class AddMenuController extends BasicController {
		
		private Link addResourceLink;
		private Link addTemplateLink;

		private final VelocityContainer mainVC;
		
		public AddMenuController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, Util.createPackageTranslator(CurriculumComposerController.class, ureq.getLocale()));
			
			mainVC = createVelocityContainer("tools");
			
			addResourceLink = LinkFactory.createLink("add.resource", "add.resource", getTranslator(), mainVC, this, Link.LINK);
			addResourceLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
			mainVC.put("add.resource", addResourceLink);
			
			addTemplateLink = LinkFactory.createLink("add.template", "add.template", getTranslator(), mainVC, this, Link.LINK);
			addTemplateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
			mainVC.put("add.template", addTemplateLink);
			
			mainVC.contextPut("links", List.of("add.resource", "add.template"));

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(addResourceLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doChooseResources(ureq);
			} else if(addTemplateLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doChooseTemplate(ureq);
			}
		}
	}
}
