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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRowCssDelegate;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackImpl;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.ui.BindersDataModel.PortfolioCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderListController extends FormBasicController
	implements Activateable2, TooledController, FlexiTableComponentDelegate, FlexiTableRowCssDelegate {
	
	private int counter = 1;
	private Link newBinderLink;
	
	private FlexiTableElement tableEl;
	private BindersDataModel model;
	private final TooledStackedPanel stackPanel;
	
	private CloseableModalController cmc;
	private BinderController binderCtrl;
	private BinderMetadataEditController newBinderCtrl;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public BinderListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, "binder_list");
		this.stackPanel = stackPanel;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PortfolioCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PortfolioCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PortfolioCols.open));

		model = new BindersDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setElementCssClass("o_portfolio_listing");
		tableEl.setEmtpyTableMessageKey("table.sEmptyTable");
		tableEl.setPageSize(24);
		VelocityContainer row = createVelocityContainer("binder_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setRowCssDelegate(this);
		tableEl.setAndLoadPersistedPreferences(ureq, "portfolio-list");
	}

	@Override
	public String getRowCssClass(int pos) {
		return "o_portfolio_entry";
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}

	@Override
	public void initTools() {
		newBinderLink = LinkFactory.createToolLink("create.new.binder", translate("create.new.binder"), this);
		newBinderLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
		stackPanel.addTool(newBinderLink, Align.right);
		stackPanel.setToolbarEnabled(true);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		List<Binder> portfolios = portfolioService.searchOwnedBinders(getIdentity());
		List<PortfolioRow> rows = new ArrayList<>(portfolios.size());
		for(Binder portfolio:portfolios) {
			PortfolioRow row = forgePortfolioRow(portfolio);
			rows.add(row);
		}
		model.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();
	}
	
	private PortfolioRow forgePortfolioRow(Binder portfolio) {
		String openLinkId = "open_" + (++counter);
		FormLink openLink = uifactory.addFormLink(openLinkId, "open", "open", null, flc, Link.LINK);
		PortfolioRow row = new PortfolioRow(portfolio, openLink);
		openLink.setUserObject(row);
		return row;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Binder".equalsIgnoreCase(resName)) {
			Long portfolioKey = entries.get(0).getOLATResourceable().getResourceableId();
			Activateable2 activateable = doOpenBinder(ureq, portfolioKey);
			if(activateable != null) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				activateable.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(newBinderLink == source) {
			doNewBinder(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newBinderCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(newBinderCtrl);
		removeAsListenerAndDispose(cmc);
		newBinderCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("open".equals(cmd)) {
				PortfolioRow row = (PortfolioRow)link.getUserObject();
				Activateable2 activateable = doOpenBinder(ureq, row.getKey());
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
	
	private BinderController doOpenBinder(UserRequest ureq, Long binderKey) {
		Binder binder = portfolioService.getBinderByKey(binderKey);
		if(binder == null) {
			showWarning("warning.portfolio.not.found");
			return null;
		} else {
			removeAsListenerAndDispose(binderCtrl);
			
			OLATResourceable binderOres = OresHelper.createOLATResourceableInstance("Binder", binderKey);
			WindowControl swControl = addToHistory(ureq, binderOres, null);
			BinderSecurityCallback secCallback = new BinderSecurityCallbackImpl(true, binder.getTemplate() == null);
			binderCtrl = new BinderController(ureq, swControl, stackPanel, secCallback, binder);
			String displayName = StringHelper.escapeHtml(binder.getTitle());
			stackPanel.pushController(displayName, binderCtrl);
			return binderCtrl;
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
	
	public static class PortfolioRow {
		
		private final Long key;
		private final String title;
		private final FormLink openLink;
		
		public PortfolioRow(Binder binder, FormLink openLink) {
			key = binder.getKey();
			title = binder.getTitle();
			this.openLink = openLink;
		}
		
		public Long getKey() {
			return key;
		}

		public String getTitle() {
			return title;
		}


		public FormLink getOpenLink() {
			return openLink;
		}
		
		public String getOpenFormItemName() {
			return openLink == null ? null : openLink.getComponent().getComponentName();
		}
	}
}
