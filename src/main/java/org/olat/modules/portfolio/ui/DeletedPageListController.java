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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRenderEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.CategoryToElement;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;

/**
 * 
 * Initial date: 22.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeletedPageListController extends AbstractPageListController {

	private FormLink deleteButton;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteCtrl;
	private RestorePageController restorePageCtrl;
	
	public DeletedPageListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback) {
		super(ureq, wControl, stackPanel, secCallback, BinderConfiguration.createDeletedPagesConfig(), "deleted_pages",
				false, true, true);

		initForm(ureq);
		loadModel(ureq, null);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		model.getTableColumnModel().addFlexiColumnModel(new DefaultFlexiColumnModel("restore.page", translate("restore.page"), "restore"));
		tableEl.setMultiSelect(true);
		
		deleteButton = uifactory.addFormLink("delete.def.page", "delete.def.page", null, formLayout, Link.BUTTON);
		deleteButton.setVisible(tableEl.getRendererType() == FlexiTableRendererType.classic);
		tableEl.setSelectAllEnable(tableEl.getRendererType() == FlexiTableRendererType.classic);
	}

	@Override
	protected String getTimelineSwitchPreferencesName() {
		return "del-timeline-switch";
	}

	@Override
	protected void loadModel(UserRequest ureq, String searchString) {
		Map<Long,Long> numberOfCommentsMap = portfolioService.getNumberOfCommentsOnOwnedPage(getIdentity());
		
		List<CategoryToElement> categorizedElements = portfolioService.getCategorizedOwnedPages(getIdentity());
		Map<OLATResourceable,List<Category>> categorizedElementMap = new HashMap<>();
		for(CategoryToElement categorizedElement:categorizedElements) {
			List<Category> categories = categorizedElementMap.get(categorizedElement.getCategorizedResource());
			if(categories == null) {
				categories = new ArrayList<>();
				categorizedElementMap.put(categorizedElement.getCategorizedResource(), categories);
			}
			categories.add(categorizedElement.getCategory());
		}

		List<Page> pages = portfolioService.searchDeletedPages(getIdentity(), searchString);
		List<PortfolioElementRow> rows = new ArrayList<>(pages.size());
		for (Page page : pages) {
			rows.add(forgePageRow(ureq, page, null, null, categorizedElementMap, numberOfCommentsMap, true));
		}
		
		disposeRows();//clean up the posters
		model.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();

		deleteButton.setVisible(tableEl.getRendererType() == FlexiTableRendererType.classic && model.getRowCount() > 0);
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				@SuppressWarnings("unchecked")
				List<PortfolioElementRow> rows = (List<PortfolioElementRow>)confirmDeleteCtrl.getUserObject();
				doDelete(ureq, rows);
			}
		} else if(restorePageCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(ureq, null);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(restorePageCtrl);
		removeAsListenerAndDispose(cmc);
		restorePageCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableRenderEvent) {
				FlexiTableRenderEvent se = (FlexiTableRenderEvent)event;
				deleteButton.setVisible(se.getRendererType() == FlexiTableRendererType.classic
						&& model.getRowCount() > 0);
				tableEl.setSelectAllEnable(tableEl.getRendererType() == FlexiTableRendererType.classic);
			} else if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select-page".equals(cmd)) {
					PortfolioElementRow row = model.getObject(se.getIndex());
					doOpenRow(ureq, row, false);
				} else if("restore".equals(cmd)) {
					PortfolioElementRow row = model.getObject(se.getIndex());
					if(row.isPage()) {
						doRestorePage(ureq, row);
					}
				}
			}
		} else if(deleteButton == source) {
			doConfirmDelete(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doRestorePage(UserRequest ureq, PortfolioElementRow row) {
		Page reloadedPage = portfolioService.getPageByKey(row.getPage().getKey());
		restorePageCtrl = new RestorePageController(ureq, getWindowControl(), reloadedPage);
		listenTo(restorePageCtrl);
		
		String title = translate("restore.page");
		cmc = new CloseableModalController(getWindowControl(), null, restorePageCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		List<PortfolioElementRow> rows = getSelectedRows();
		if(rows.isEmpty()) {
			showWarning("page.atleastone");
		} else if (rows.size() == 1) {
			String title = translate("delete.def.page.confirm.title");
			String text = translate("delete.def.page.confirm.descr", new String[]{
					StringHelper.escapeHtml(rows.get(0).getPage().getTitle())
				});
			confirmDeleteCtrl = activateYesNoDialog(ureq, title, text, confirmDeleteCtrl);
			confirmDeleteCtrl.setUserObject(rows);
		} else {
			StringBuilder names = new StringBuilder();
			for(PortfolioElementRow row:rows) {
				if(names.length() > 0) names.append(", ");
				names.append(StringHelper.escapeHtml(row.getPage().getTitle()));
			}
			
			String title = translate("delete.def.pages.confirm.title", new String[] { Integer.toString(rows.size()) });
			String text = translate("delete.def.pages.confirm.descr", new String[]{ names.toString() });
			confirmDeleteCtrl = activateYesNoDialog(ureq, title, text, confirmDeleteCtrl);
			confirmDeleteCtrl.setUserObject(rows);
		}
	}
	
	private void doDelete(UserRequest ureq, List<PortfolioElementRow> rows) {
		for(PortfolioElementRow row:rows) {
			portfolioService.deletePage(row.getPage());
		}
		loadModel(ureq, null);
	}
}
