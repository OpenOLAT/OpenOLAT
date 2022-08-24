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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSFormItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
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
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.BinderStatus;
import org.olat.modules.portfolio.PortfolioLoggingAction;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.modules.portfolio.model.BinderRefImpl;
import org.olat.modules.portfolio.model.BinderStatistics;
import org.olat.modules.portfolio.model.SynchedBinder;
import org.olat.modules.portfolio.ui.BindersDataModel.PortfolioCols;
import org.olat.modules.portfolio.ui.event.DeleteBinderEvent;
import org.olat.modules.portfolio.ui.event.NewBinderEvent;
import org.olat.modules.portfolio.ui.event.RestoreBinderEvent;
import org.olat.modules.portfolio.ui.export.ExportBinderAsCPResource;
import org.olat.modules.portfolio.ui.model.BinderListSettings;
import org.olat.modules.portfolio.ui.model.BinderRow;
import org.olat.modules.portfolio.ui.model.CourseTemplateRow;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;
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
	
	
	private int counter = 1;
	private Link newBinderLink;
	
	protected FlexiTableElement tableEl;
	protected BindersDataModel model;
	protected final TooledStackedPanel stackPanel;
	private FormLink newBinderDropdown;
	private FormLink newBinderFromCourseButton;
	
	private ToolsController toolsCtrl;
	protected BinderController binderCtrl;
	protected CloseableModalController cmc;
	private BinderMetadataEditController newBinderCtrl;
	private RepositorySearchController searchTemplateCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CourseTemplateSearchController searchCourseTemplateCtrl;
	private NewBinderCalloutController chooseNewBinderTypeCtrl;
	private CloseableCalloutWindowController newBinderCalloutCtrl;
	private BinderMetadataEditController binderMetadataCtrl;
	private ConfirmDeleteBinderController deleteBinderCtrl;
	private ConfirmMoveBinderToTrashController moveBinderToTrashCtrl;
	private DialogBoxController confirmRestoreBinderCtrl;
	private StepsMainRunController wizardCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PortfolioV2Module portfolioModule;
	@Autowired
	protected PortfolioService portfolioService;
	
	public BinderListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, "binder_list");
		this.stackPanel = stackPanel;
		initForm(ureq);
		loadModel(ureq, true);
		initSettings(ureq);//fix the order
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
		int numOfRows = model.getRowCount();
		for(int i=0; i<numOfRows; i++) {
			BinderRow row = model.getObject(i);
			if(row.getKey() != null) {
				return row;
			}
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
		tableEl.setEmptyTableMessageKey("table.sEmptyTable");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setPageSize(24);
		VelocityContainer row = createVelocityContainer("binder_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setCssDelegate(new BinderCssDelegate());
		tableEl.setAndLoadPersistedPreferences(ureq, getTableId());
		
		String mapperThumbnailUrl = registerCacheableMapper(ureq, "binder-list", new ImageMapper(model));
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
		
		JSAndCSSFormItem js = new JSAndCSSFormItem("js", new String[] { "js/dragula/dragula.js" });
		formLayout.add(js);
		((FormLayoutContainer)formLayout).getFormItemComponent().addListener(this);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		BinderRow elRow = model.getObject(row);
		List<Component> components = new ArrayList<>(3);
		if(elRow.getOpenLink() != null) {
			components.add(elRow.getOpenLink().getComponent());
		}
		if(elRow.getToolsLink() != null) {
			components.add(elRow.getToolsLink().getComponent());
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
	
	protected void loadModel(UserRequest ureq, boolean reset) {
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
		rows = reorderRowsBysettings(ureq, rows);
		model.setObjects(rows);
		if(reset) {
			tableEl.reset();
		}
		tableEl.reloadData();
	}
	
	private List<BinderRow> reorderRowsBysettings(UserRequest ureq, List<BinderRow> rows) {
		BinderListSettings settings = getSettings(ureq);
		if(settings.getOrderedBinderKeys() != null && !settings.getOrderedBinderKeys().isEmpty()) {
			Map<Long,BinderRow> rowMap = rows.stream().collect(Collectors.toMap(BinderRow::getKey, r -> r, (u,v) -> u));
			
			List<BinderRow> orderRows = new ArrayList<>(rows.size());
			for(Long orderKey:settings.getOrderedBinderKeys()) {
				BinderRow row = rowMap.get(orderKey);
				if(row != null) {
					orderRows.add(row);
					rows.remove(row);
				}
			}
			
			for(BinderRow row:rows) {
				orderRows.add(row);
			}
			return orderRows;
		}
		return rows;
	}
	
	protected BinderRow forgePortfolioRow(BinderStatistics binderRow) {
		String openLinkId = "open_" + (++counter);
		FormLink openLink = uifactory.addFormLink(openLinkId, "open", "open", null, flc, Link.LINK);
		openLink.setIconRightCSS("o_icon o_icon_start");
		
		String toolsLinkId = "tools_" + (++counter);
		FormLink toolsLink = uifactory.addFormLink(toolsLinkId, "tools", null, null, flc, Link.LINK | Link.NONTRANSLATED);
		toolsLink.getComponent().setCustomDisplayText("");
		toolsLink.setIconRightCSS("o_icon o_icon_settings");
		
		VFSLeaf image = portfolioService.getPosterImageLeaf(binderRow);
		BinderRow row = new BinderRow(binderRow, image, openLink, toolsLink);
		openLink.setUserObject(row);
		toolsLink.setUserObject(row);
		return row;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Binder".equalsIgnoreCase(resName)) {
			Long portfolioKey = entries.get(0).getOLATResourceable().getResourceableId();
			BinderRow row = model.getObjectByKey(portfolioKey);
			if(row != null) {
				Activateable2 activateable = doOpenBinder(ureq, new BinderRefImpl(portfolioKey));
				if(activateable != null) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					activateable.activate(ureq, subEntries, entries.get(0).getTransientState());
				}
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
		} else if("drop-binder".equals(event.getCommand())) {
			String binderKey = ureq.getParameter("dragged");
			String siblingKey = ureq.getParameter("sibling");
			doDragAndDrop(ureq, binderKey, siblingKey);
			flc.setDirty(true);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newBinderCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(ureq, true);
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
				} else if(NewBinderEvent.NEW_FROM_ENTRIES.equals(cmd)) {
					doNewBinderFromEntries(ureq);
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
		} else if(binderMetadataCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadModel(ureq, false);
			}
			cmc.deactivate();
			cleanUp();
		} else if(moveBinderToTrashCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doMoveBinderToTrash(moveBinderToTrashCtrl.getUserObject());
				loadModel(ureq, true);
				fireEvent(ureq, new DeleteBinderEvent());
			}
			cmc.deactivate();
			cleanUp();
		} else if(deleteBinderCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doDeleteBinder(deleteBinderCtrl.getUserObject());
				loadModel(ureq, true);
				fireEvent(ureq, new DeleteBinderEvent());
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmRestoreBinderCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doRestore((BinderRef)confirmRestoreBinderCtrl.getUserObject());
				loadModel(ureq, true);
				fireEvent(ureq, new RestoreBinderEvent());
			}	
		} else if(binderCtrl == source) {
			if(event instanceof DeleteBinderEvent) {
				stackPanel.popUpToController(this);
				loadModel(ureq, true);
			}
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		} else if (wizardCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
                // Close the dialog
                getWindowControl().pop();

                // Remove steps controller
                cleanUp();
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
		removeAsListenerAndDispose(moveBinderToTrashCtrl);
		removeAsListenerAndDispose(searchTemplateCtrl);
		removeAsListenerAndDispose(binderMetadataCtrl);
		removeAsListenerAndDispose(deleteBinderCtrl);
		removeAsListenerAndDispose(newBinderCtrl);
		removeAsListenerAndDispose(wizardCtrl);
		removeAsListenerAndDispose(cmc);
		searchCourseTemplateCtrl = null;
		chooseNewBinderTypeCtrl = null;
		moveBinderToTrashCtrl = null;
		newBinderCalloutCtrl = null;
		binderMetadataCtrl = null;
		searchTemplateCtrl = null;
		deleteBinderCtrl = null;
		newBinderCtrl = null;
		wizardCtrl = null;
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
			} else if("tools".equals(cmd)) {
				BinderRow row = (BinderRow)link.getUserObject();
				doOpenTools(ureq, link, row);
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
		if(guardModalController(newBinderCtrl)) return;
		
		newBinderCtrl = new BinderMetadataEditController(ureq, getWindowControl(), null);
		listenTo(newBinderCtrl);
		
		String title = translate("create.new.binder");
		cmc = new CloseableModalController(getWindowControl(), null, newBinderCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doNewBinderFromTemplate(UserRequest ureq) {
		if(guardModalController(searchTemplateCtrl)) return;

		String title = translate("create.empty.binder.from.template");
		String commandLabel = translate("create.binder.selectTemplate");
		removeAsListenerAndDispose(searchTemplateCtrl);
		searchTemplateCtrl = new RepositorySearchController(commandLabel, ureq, getWindowControl(),
				false, false, new String[]{ BinderTemplateResource.TYPE_NAME }, false, null, null);
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
		if(guardModalController(searchCourseTemplateCtrl)) return;

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
	
	private void doNewBinderFromEntries(UserRequest ureq) {
		PortfolioImportEntriesContext context = new PortfolioImportEntriesContext();
		context.setBinderSecurityCallback(BinderSecurityCallbackFactory.getCallbackForImportPages());
		FinishCallback finish = new FinishCallback();
		CancelCallback cancel = new CancelCallback();
		CreateNewBinderStep createNewBinderStep = new CreateNewBinderStep(ureq, context);
		
		wizardCtrl = new StepsMainRunController(ureq, getWindowControl(), createNewBinderStep, finish, cancel, translate("create.binder.from.entries"), null);
		listenTo(wizardCtrl);
		getWindowControl().pushAsModalDialog(wizardCtrl.getInitialComponent());
	}
	
	private void doOpenTools(UserRequest ureq, FormLink link, BinderRow row) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		
		Binder binder = portfolioService.getBinderByKey(row.getKey());
		BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForOwnedBinder(binder);
		toolsCtrl = new ToolsController(ureq, getWindowControl(), secCallback, binder, row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doDragAndDrop(UserRequest ureq, String binderKeyStr, String siblingKeyStr) {
		if(!StringHelper.isLong(binderKeyStr) || !StringHelper.isLong(siblingKeyStr)) return;
		
		BinderRow binder = model.getObjectByKey(Long.valueOf(binderKeyStr));
		BinderRow sibling = model.getObjectByKey(Long.valueOf(siblingKeyStr));
		if(binder != null && sibling != null && !binder.equals(sibling)) {
			List<BinderRow> rows = model.getObjects();
			rows.remove(binder);
			
			int index = rows.indexOf(sibling);
			rows.add(index, binder);
			
			doSaveBinderListOrder(ureq, rows);
			model.setObjects(rows);
			tableEl.reset(false, false, true);
		}
	}
	
	private void doMoveUp(UserRequest ureq, BinderRow row) {
		List<BinderRow> rows = model.getObjects();
		int index = rows.indexOf(row);
		if(index > 0 && index < rows.size()) {
			rows.remove(index);
			rows.add(index-1, row);
			doSaveBinderListOrder(ureq, rows);
			model.setObjects(rows);
			tableEl.reset(false, false, true);
		}
	}
	
	private void doMoveDown(UserRequest ureq, BinderRow row) {
		List<BinderRow> rows = model.getObjects();
		int index = rows.indexOf(row);
		if(index >= 0 && index + 1 < rows.size()) {
			rows.remove(index);
			rows.add(index+1, row);
			doSaveBinderListOrder(ureq, rows);
			model.setObjects(rows);
			tableEl.reset(false, false, true);
		}
	}
	
	private void initSettings(UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		BinderListSettings settings  = (BinderListSettings)guiPrefs.get(BinderListController.class, "binder-list-settings");
		if(settings == null) {
			List<BinderRow> rows = model.getObjects();
			doSaveBinderListOrder(ureq, rows);
		}
	}
	
	private BinderListSettings getSettings(UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		BinderListSettings settings  = (BinderListSettings)guiPrefs.get(BinderListController.class, "binder-list-settings");
		if(settings == null) {
			settings = new BinderListSettings();
		}
		return settings;
	}

	private void doSaveBinderListOrder(UserRequest ureq, List<BinderRow> orderRows) {
		BinderListSettings settings = getSettings(ureq);
		List<Long> keys = orderRows.stream().map(BinderRow::getKey).collect(Collectors.toList());
		settings.setOrderedBinderKeys(keys);
		doSaveSettings(ureq, settings);
	}
	
	private void doSaveSettings(UserRequest ureq, BinderListSettings settings) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(BinderListController.class, "binder-list-settings", settings);
		}
	}
	
	private void doEditBinderMetadata(UserRequest ureq, BinderRow row) {
		if(guardModalController(binderMetadataCtrl)) return;
		
		Binder reloadedBinder = portfolioService.getBinderByKey(row.getKey());
		binderMetadataCtrl = new BinderMetadataEditController(ureq, getWindowControl(), reloadedBinder);
		listenTo(binderMetadataCtrl);
		
		String title = translate("edit.binder.metadata");
		cmc = new CloseableModalController(getWindowControl(), null, binderMetadataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmMoveToTrashBinder(UserRequest ureq, BinderRow row) {
		if(guardModalController(moveBinderToTrashCtrl)) return;
		
		BinderStatistics stats = portfolioService.getBinderStatistics(row);
		moveBinderToTrashCtrl = new ConfirmMoveBinderToTrashController(ureq, getWindowControl(), stats);
		moveBinderToTrashCtrl.setUserObject(row);
		listenTo(moveBinderToTrashCtrl);
		
		String title = translate("delete.binder");
		cmc = new CloseableModalController(getWindowControl(), null, moveBinderToTrashCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doMoveBinderToTrash(BinderRow row) {
		Binder binder = portfolioService.getBinderByKey(row.getKey());
		binder.setBinderStatus(BinderStatus.deleted);
		portfolioService.updateBinder(binder);
		dbInstance.commit();
		showInfo("delete.binder.success");
	}
	
	private void doConfirmDeleteBinder(UserRequest ureq, BinderRow row) {
		if(guardModalController(moveBinderToTrashCtrl)) return;
		
		BinderStatistics stats = portfolioService.getBinderStatistics(row);
		deleteBinderCtrl = new ConfirmDeleteBinderController(ureq, getWindowControl(), stats);
		deleteBinderCtrl.setUserObject(row);
		listenTo(deleteBinderCtrl);
		
		String title = translate("delete.binder");
		cmc = new CloseableModalController(getWindowControl(), null, deleteBinderCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteBinder(BinderRef binder) {
		portfolioService.deleteBinder(binder);
		dbInstance.commit();
		showInfo("delete.binder.success");
	}
	
	private void doConfirmRestore(UserRequest ureq, BinderRow row) {
		String title = translate("restore.binder.confirm.title");
		String text = translate("restore.binder.confirm.descr", new String[]{ StringHelper.escapeHtml(row.getTitle()) });
		confirmRestoreBinderCtrl = activateYesNoDialog(ureq, title, text, confirmRestoreBinderCtrl);
		confirmRestoreBinderCtrl.setUserObject(row);
	}
	
	private void doRestore(BinderRef row) {
		Binder binder = portfolioService.getBinderByKey(row.getKey());
		binder.setBinderStatus(BinderStatus.open);
		portfolioService.updateBinder(binder);
		showInfo("restore.binder.success");
	}
	
	private void doExportBinderAsCP(UserRequest ureq, BinderRow row) {
		MediaResource resource = new ExportBinderAsCPResource(row, ureq, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private static class BinderCssDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_portfolio_entry o_dragable";
		}
	}
	
	private class FinishCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			// Load context
			PortfolioImportEntriesContext context = (PortfolioImportEntriesContext) runContext.get(PortfolioImportEntriesContext.CONTEXT_KEY);
			
			// Create binder
			String imagePath = null;
			if (context.getNewBinderImage() != null) {
				imagePath = portfolioService.addPosterImageForBinder(context.getNewBinderImage(), context.getNewBinderImageName());
			}
			Binder newBinder = portfolioService.createNewBinder(context.getNewBinderTitle(), context.getNewBinderDescription(), imagePath, getIdentity());
			
			// Create section
			SectionRef newSectionRef = portfolioService.appendNewSection(context.getNewSectionTitle(), context.getNewSectionDescription(), null, null, newBinder);
			
			// Import pages
			for (PortfolioElementRow page : context.getSelectedPortfolioEntries()) {
				portfolioService.appendNewPage(getIdentity(), page.getTitle(), page.getSummary(), page.getImageUrl(), page.getPage().getImageAlignment(), newSectionRef, page.getPage());
			}
			
			// Reload data and open new binder
			loadModel(ureq, true);
			doOpenBinder(ureq, newBinder).activate(ureq, null, null);
			
			// Fire event
            return StepsMainRunController.DONE_MODIFIED;
		}
	}
	
	private class CancelCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			// Cancel the wizard
			return StepsMainRunController.DONE_UNCHANGED;
		}
	}
	
	private class ToolsController extends BasicController {
		
		private final BinderRow row;
		
		private Link moveUpLink;
		private Link moveDownLink;
		private Link deleteBinderLink;
		private Link restoreBinderLink;
		private Link exportBinderAsCpLink;
		private Link editBinderMetadataLink;
		private Link moveToTrashBinderLink;
		private final VelocityContainer mainVC;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, BinderSecurityCallback secCallback, Binder binder, BinderRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			List<String> links = new ArrayList<>();
			
			
			//metadata
			if(secCallback.canEditMetadataBinder()) {
				editBinderMetadataLink = addLink("edit.binder.metadata", "edit.metadata", "o_icon o_icon_new_portfolio", links);
			}

			moveUpLink = addLink("move.left", "move-up", "o_icon o_icon_left", links);
			moveDownLink = addLink("move.right", "move-down", "o_icon o_icon_right", links);
			
			if(secCallback.canExportBinder()) {
				exportBinderAsCpLink = addLink("export.binder.cp", "export-cp", "o_icon o_icon_download", links);
			}
			if(secCallback.canMoveToTrashBinder(binder)) {
				moveToTrashBinderLink = addLink("delete.binder", "trash-binder", "o_icon o_icon_delete_item", links);
			}
			
			if(secCallback.canDeleteBinder(binder)) {
				deleteBinderLink = addLink("delete.binder", "delete.binder", "o_icon o_icon_delete_item", links);
				restoreBinderLink = addLink("restore.binder", "restore.binder", "o_icon o_icon_restore", links);
			}

			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String cmd, String iconCSS, List<String> links) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			links.add(name);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(moveUpLink == source) {
				doMoveUp(ureq, row);
			} else if(moveDownLink == source) {
				doMoveDown(ureq, row);
			} else if(editBinderMetadataLink == source) {
				doEditBinderMetadata(ureq, row);
			} else if(moveToTrashBinderLink == source) {
				doConfirmMoveToTrashBinder(ureq, row);
			} else if(exportBinderAsCpLink == source) {
				doExportBinderAsCP(ureq, row);
			} else if(deleteBinderLink == source) {
				doConfirmDeleteBinder(ureq, row);
			} else if(restoreBinderLink == source) {
				doConfirmRestore(ureq, row);
			}
		}
	}
}
