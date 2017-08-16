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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.PortfolioLoggingAction;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.modules.portfolio.model.BinderRefImpl;
import org.olat.modules.portfolio.model.BinderStatistics;
import org.olat.modules.portfolio.model.SynchedBinder;
import org.olat.modules.portfolio.ui.BindersDataModel.PortfolioCols;
import org.olat.modules.portfolio.ui.event.DeleteBinderEvent;
import org.olat.modules.portfolio.ui.event.NewBinderEvent;
import org.olat.modules.portfolio.ui.model.BinderRow;
import org.olat.modules.portfolio.ui.model.CourseTemplateRow;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.controllers.RepositorySearchController;
import org.olat.repository.controllers.RepositorySearchController.Can;
import org.olat.repository.ui.RepositoryTableModel;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is the list of the binders owned by the user.
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderListController extends FormBasicController
	implements Activateable2, TooledController, FlexiTableComponentDelegate {
	
	private static final Size BACKGROUND_SIZE = new Size(400, 230, false);
	
	private int counter = 1;
	private Link newBinderLink;
	private String mapperThumbnailUrl;
	
	protected FlexiTableElement tableEl;
	protected BindersDataModel model;
	protected final TooledStackedPanel stackPanel;
	private FormLink newBinderDropdown, newBinderFromCourseButton;
	
	protected CloseableModalController cmc;
	protected BinderController binderCtrl;
	private BinderMetadataEditController newBinderCtrl;
	private RepositorySearchController searchTemplateCtrl;
	private CourseTemplateSearchController searchCourseTemplateCtrl;
	
	private NewBinderCalloutController chooseNewBinderTypeCtrl;
	private CloseableCalloutWindowController newBinderCalloutCtrl;
	
	@Autowired
	private PortfolioV2Module portfolioModule;
	@Autowired
	protected PortfolioService portfolioService;
	
	public BinderListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, "binder_list");
		this.stackPanel = stackPanel;
		initForm(ureq);
		loadModel();
	}
	
	public int getNumOfBinders() {
		int count = model.getRowCount();
		if(portfolioModule.isLearnerCanCreateBinders()
				|| portfolioModule.isCanCreateBindersFromTemplate()
				|| portfolioModule.isCanCreateBindersFromCourse()) {
			count--;
		}
		return count;
	}
	
	public BinderRow getFirstBinder() {
		if(model.getRowCount() > 0) {
			return model.getObject(0);
		}
		return null;
	}
	
	protected String getTableId() {
		return "portfolio-list";
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PortfolioCols.key, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PortfolioCols.title, "select"));

		model = new BindersDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setElementCssClass("o_portfolio_listing");
		tableEl.setEmtpyTableMessageKey("table.sEmptyTable");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setPageSize(24);
		VelocityContainer row = createVelocityContainer("binder_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setCssDelegate(new BinderCssDelegate());
		tableEl.setAndLoadPersistedPreferences(ureq, getTableId());
		
		mapperThumbnailUrl = registerCacheableMapper(ureq, "binder-list", new ImageMapper(model));
		row.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
		
		if(!portfolioModule.isLearnerCanCreateBinders() && !portfolioModule.isCanCreateBindersFromTemplate() && portfolioModule.isCanCreateBindersFromCourse()) {
			newBinderFromCourseButton = uifactory.addFormLink("create.binder.from.course", "create.empty.binder.from.course", null, formLayout, Link.BUTTON);
			row.put("createBinderFromCourse", newBinderFromCourseButton.getComponent());
		} else if(portfolioModule.isLearnerCanCreateBinders()
				|| portfolioModule.isCanCreateBindersFromTemplate()
				|| portfolioModule.isCanCreateBindersFromCourse()) {
			newBinderDropdown = uifactory.addFormLink("create.binders", "create.new.binder", null, formLayout, Link.BUTTON);
			
			int count = (portfolioModule.isLearnerCanCreateBinders() ? 1 : 0)
					+ (portfolioModule.isCanCreateBindersFromTemplate() ? 1 : 0)
					+ (portfolioModule.isCanCreateBindersFromCourse() ? 1 :0);
			if(count > 1) {
				newBinderDropdown.setIconRightCSS("o_icon o_icon_caret");
			}
			row.put("createDropdown", newBinderDropdown.getComponent());
		}
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		BinderRow elRow = model.getObject(row);
		List<Component> components = new ArrayList<>(2);
		if(elRow.getOpenLink() != null) {
			components.add(elRow.getOpenLink().getComponent());
		}
		return components;
	}

	@Override
	public void initTools() {
		if(portfolioModule.isLearnerCanCreateBinders()
				|| portfolioModule.isCanCreateBindersFromTemplate()
				|| portfolioModule.isCanCreateBindersFromCourse()) {
			newBinderLink = LinkFactory.createToolLink("create.new.binder", translate("create.new.binder"), this);
			newBinderLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			newBinderLink.setElementCssClass("o_sel_pf_new_binder");
			stackPanel.addTool(newBinderLink, Align.right);
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	protected void loadModel() {
		List<BinderStatistics> binderRows = portfolioService.searchOwnedBinders(getIdentity());
		List<BinderRow> rows = new ArrayList<>(binderRows.size());
		for(BinderStatistics binderRow:binderRows) {
			rows.add(forgePortfolioRow(binderRow));
		}
		if(portfolioModule.isLearnerCanCreateBinders()
				|| portfolioModule.isCanCreateBindersFromTemplate()
				|| portfolioModule.isCanCreateBindersFromCourse()) {
			rows.add(new BinderRow());
		}
		model.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();
	}
	
	protected BinderRow forgePortfolioRow(BinderStatistics binderRow) {
		String openLinkId = "open_" + (++counter);
		FormLink openLink = uifactory.addFormLink(openLinkId, "open", "open", null, flc, Link.LINK);
		openLink.setIconRightCSS("o_icon o_icon_start");
		VFSLeaf image = portfolioService.getPosterImageLeaf(binderRow);
		BinderRow row = new BinderRow(binderRow, image, openLink);
		openLink.setUserObject(row);
		return row;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Binder".equalsIgnoreCase(resName)) {
			Long portfolioKey = entries.get(0).getOLATResourceable().getResourceableId();
			Activateable2 activateable = doOpenBinder(ureq, new BinderRefImpl(portfolioKey));
			if(activateable != null) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				activateable.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(newBinderLink == source) {
			if(portfolioModule.isLearnerCanCreateBinders()
					|| portfolioModule.isCanCreateBindersFromTemplate()
					|| portfolioModule.isCanCreateBindersFromCourse()) {
				doNewBinder(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newBinderCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
				doOpenBinder(ureq, newBinderCtrl.getBinder()).activate(ureq, null, null);
			}
			cmc.deactivate();
			cleanUp();
		} else if(chooseNewBinderTypeCtrl == source) {
			newBinderCalloutCtrl.deactivate();
			cleanUp();
			if(event instanceof NewBinderEvent) {
				String cmd = event.getCommand();
				if(NewBinderEvent.NEW_EMPTY.equals(cmd)) {
					doNewBinder(ureq);
				} else if(NewBinderEvent.NEW_EMPTY_FROM_TEMPLATE.equals(cmd)) {
					doNewBinderFromTemplate(ureq);
				} else if(NewBinderEvent.NEW_EMPTY_FROM_COURSE.equals(cmd)) {
					doNewBinderFromCourse(ureq);
				}
			}
		} else if(searchTemplateCtrl == source) {
			if(RepositoryTableModel.TABLE_ACTION_SELECT_LINK.equals(event.getCommand())) {
				RepositoryEntry repoEntry = searchTemplateCtrl.getSelectedEntry();
				doCreateBinderFromTemplate(ureq, repoEntry);
			}
			cmc.deactivate();
			cleanUp();
		} else if(searchCourseTemplateCtrl == source) {
			if(event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				CourseTemplateRow templateRow = searchCourseTemplateCtrl.getSelectedEntry();
				doCreateBinderFromCourseTemplate(ureq, templateRow);
			}
			cmc.deactivate();
			cleanUp();
		} else if(newBinderCalloutCtrl == source) {
			cleanUp();
		} else if(binderCtrl == source) {
			if(event instanceof DeleteBinderEvent) {
				stackPanel.popUpToController(this);
				loadModel();
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(searchCourseTemplateCtrl);
		removeAsListenerAndDispose(chooseNewBinderTypeCtrl);
		removeAsListenerAndDispose(newBinderCalloutCtrl);
		removeAsListenerAndDispose(searchTemplateCtrl);
		removeAsListenerAndDispose(newBinderCtrl);
		removeAsListenerAndDispose(cmc);
		searchCourseTemplateCtrl = null;
		chooseNewBinderTypeCtrl = null;
		newBinderCalloutCtrl = null;
		searchTemplateCtrl = null;
		newBinderCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(newBinderDropdown == source) {
			doNewBinderCallout(ureq);
		} else if(newBinderFromCourseButton == source) {
			doNewBinderFromCourse(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					BinderRow row = model.getObject(se.getIndex());
					Activateable2 activateable = doOpenBinder(ureq, row);
					if(activateable != null) {
						activateable.activate(ureq, null, null);
					}
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("open".equals(cmd)) {
				BinderRow row = (BinderRow)link.getUserObject();
				Activateable2 activateable = doOpenBinder(ureq, row);
				if(activateable != null) {
					activateable.activate(ureq, null, null);
				}
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	protected List<BinderRow> getSelectedRows() {
		Set<Integer> indexes = tableEl.getMultiSelectedIndex();
		List<BinderRow> selectedRows = new ArrayList<>(indexes.size());
		for(Integer index:indexes) {
			BinderRow row = model.getObject(index.intValue());
			selectedRows.add(row);
		}
		return selectedRows;
	}
	
	protected BinderController doOpenBinder(UserRequest ureq, BinderRef row) {
		SynchedBinder binder = portfolioService.loadAndSyncBinder(row);
		if(binder.isChanged()) {
			showInfo("warning.binder.synched");
		}
		BinderController selectedBinderCtrl = doOpenBinder(ureq, binder.getBinder());
		if(row instanceof BinderRow) {
			VFSLeaf image = portfolioService.getPosterImageLeaf(binder.getBinder());
			((BinderRow)row).setBackgroundImage(image);
		}
		return selectedBinderCtrl;
	}
	
	protected BinderController doOpenBinder(UserRequest ureq, Binder binder) {
		if(binder == null) {
			showWarning("warning.portfolio.not.found");
			return null;
		} else {
			removeAsListenerAndDispose(binderCtrl);
			
			portfolioService.updateBinderUserInformations(binder, getIdentity());
			OLATResourceable binderOres = OresHelper.createOLATResourceableInstance("Binder", binder.getKey());
			WindowControl swControl = addToHistory(ureq, binderOres, null);
			BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForOwnedBinder(binder);
			BinderConfiguration config = BinderConfiguration.createConfig(binder);
			binderCtrl = new BinderController(ureq, swControl, stackPanel, secCallback, binder, config);
			listenTo(binderCtrl);
			stackPanel.pushController(binder.getTitle(), binderCtrl);
			return binderCtrl;
		}
	}
	
	private void doNewBinderCallout(UserRequest ureq) {
		// short cut if only one option is selected
		if(portfolioModule.isLearnerCanCreateBinders() && !portfolioModule.isCanCreateBindersFromTemplate() && !portfolioModule.isCanCreateBindersFromCourse()) {
			doNewBinder(ureq);
		} else if(!portfolioModule.isLearnerCanCreateBinders() && portfolioModule.isCanCreateBindersFromTemplate() && !portfolioModule.isCanCreateBindersFromCourse()) {
			doNewBinderFromTemplate(ureq);
		} else if(!portfolioModule.isLearnerCanCreateBinders() && !portfolioModule.isCanCreateBindersFromTemplate() && portfolioModule.isCanCreateBindersFromCourse()) {
			doNewBinderFromCourse(ureq);
		} else if(chooseNewBinderTypeCtrl == null) {
			chooseNewBinderTypeCtrl = new NewBinderCalloutController(ureq, getWindowControl());
			listenTo(chooseNewBinderTypeCtrl);
	
			newBinderCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					chooseNewBinderTypeCtrl.getInitialComponent(), newBinderDropdown.getFormDispatchId(),
					"", true, "", new CalloutSettings(false));
			listenTo(newBinderCalloutCtrl);
			newBinderCalloutCtrl.activate();
		}
	}

	private void doNewBinder(UserRequest ureq) {
		if(newBinderCtrl != null) return;
		
		newBinderCtrl = new BinderMetadataEditController(ureq, getWindowControl(), null);
		listenTo(newBinderCtrl);
		
		String title = translate("create.new.binder");
		cmc = new CloseableModalController(getWindowControl(), null, newBinderCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doNewBinderFromTemplate(UserRequest ureq) {
		if(searchTemplateCtrl != null) return;

		String title = translate("create.empty.binder.from.template");
		String commandLabel = translate("create.binder.selectTemplate");
		removeAsListenerAndDispose(searchTemplateCtrl);
		searchTemplateCtrl = new RepositorySearchController(commandLabel, ureq, getWindowControl(),
				false, false, new String[]{ BinderTemplateResource.TYPE_NAME }, null);
		searchTemplateCtrl.enableSearchforAllXXAbleInSearchForm(Can.all);
		searchTemplateCtrl.doSearchByTypeLimitAccess(new String[]{ BinderTemplateResource.TYPE_NAME }, ureq);
		listenTo(searchTemplateCtrl);

		cmc = new CloseableModalController(getWindowControl(), title, searchTemplateCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateBinderFromTemplate(UserRequest ureq, RepositoryEntry entry) {
		Binder templateBinder = portfolioService.getBinderByResource(entry.getOlatResource());
		Binder newBinder = portfolioService.assignBinder(getIdentity(), templateBinder, null, null, null);
		DBFactory.getInstance().commit();
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(newBinder);
		newBinder = synchedBinder.getBinder();
		doOpenBinder(ureq, newBinder).activate(ureq, null, null);
	}
	
	private void doNewBinderFromCourse(UserRequest ureq) {
		if(searchCourseTemplateCtrl != null) return;

		removeAsListenerAndDispose(searchCourseTemplateCtrl);
		searchCourseTemplateCtrl = new CourseTemplateSearchController(ureq, getWindowControl());			
		listenTo(searchCourseTemplateCtrl);

		String title = translate("create.empty.binder.from.template");
		cmc = new CloseableModalController(getWindowControl(), title, searchCourseTemplateCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateBinderFromCourseTemplate(UserRequest ureq, CourseTemplateRow row) {
		RepositoryEntry courseEntry = row.getCourseEntry();
		RepositoryEntry templateEntry = row.getTemplateEntry();
		PortfolioCourseNode courseNode = row.getCourseNode();
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());

		Binder copyBinder = portfolioService.getBinder(getIdentity(), templateBinder, courseEntry, courseNode.getIdent());
		if(copyBinder == null) {
			Date deadline = courseNode.getDeadline();
			copyBinder = portfolioService.assignBinder(getIdentity(), templateBinder, courseEntry, courseNode.getIdent(), deadline);
			DBFactory.getInstance().commit();
			SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(copyBinder);
			copyBinder = synchedBinder.getBinder();
			
			if(copyBinder != null) {
				showInfo("map.copied", StringHelper.escapeHtml(templateBinder.getTitle()));
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(copyBinder));
				ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_TASK_STARTED, getClass());
			}
		}
		doOpenBinder(ureq, copyBinder).activate(ureq, null, null);
	}

	public static class ImageMapper implements Mapper {
		
		private final BindersDataModel binderModel;
		
		public ImageMapper(BindersDataModel model) {
			this.binderModel = model;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			String row = relPath;
			if(row.startsWith("/")) {
				row = row.substring(1, row.length());
			}
			int index = row.indexOf("/");
			if(index > 0) {
				row = row.substring(0, index);
				Long key = new Long(row); 
				List<BinderRow> rows = binderModel.getObjects();
				for(BinderRow prow:rows) {
					if(key.equals(prow.getKey())) {
						VFSLeaf image = prow.getBackgroundImage();
						if(image instanceof MetaTagged) {
							MetaInfo info = ((MetaTagged)image).getMetaInfo();
							VFSLeaf thumbnail = info.getThumbnail(BACKGROUND_SIZE.getWidth(), BACKGROUND_SIZE.getHeight(), true);
							if(thumbnail != null) {
								image = thumbnail;
							}
						}
						return new VFSMediaResource(image);
					}
				}
			}
			
			return null;
		}
	}
	
	private static class BinderCssDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_portfolio_entry";
		}
	}
}
