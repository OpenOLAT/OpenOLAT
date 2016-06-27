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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.model.PageRow;
import org.olat.modules.portfolio.ui.PageListDataModel.PageCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractPageListController extends FormBasicController
implements Activateable2, TooledController, FlexiTableComponentDelegate {
	
	protected FlexiTableElement tableEl;
	protected PageListDataModel model;
	protected final TooledStackedPanel stackPanel;
	
	private PageController pageCtrl;
	private CloseableModalController cmc;
	private PageMetadataEditController newPageCtrl;
	
	protected int counter;
	protected final boolean withSections;
	protected final BinderConfiguration config;
	protected final BinderSecurityCallback secCallback;
	
	@Autowired
	protected PortfolioService portfolioService;
	
	public AbstractPageListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, BinderConfiguration config, String vTemplate, boolean withSections) {
		super(ureq, wControl, vTemplate);
		this.config = config;
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		this.withSections = withSections;
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PageCols.key, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.title, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.date, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.open));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PageCols.newEntry, "select"));
	
		model = new PageListDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_binder_page_listing");
		tableEl.setEmtpyTableMessageKey("table.sEmptyTable");
		tableEl.setPageSize(24);
		VelocityContainer row = createVelocityContainer("page_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setCssDelegate(new DefaultFlexiTableCssDelegate());
		tableEl.setAndLoadPersistedPreferences(ureq, "page-list");
	}
	
	@Override
	public void initTools() {
		//
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}
	
	protected abstract void loadModel();
	
	protected PageRow forgeRow(Page page, AssessmentSection assessmentSection, boolean firstOfSection) {
		PageRow row = new PageRow(page, page.getSection(), assessmentSection, firstOfSection, config.isAssessable());
		String openLinkId = "open_" + (++counter);
		FormLink openLink = uifactory.addFormLink(openLinkId, "open.full", "open.full.page", null, flc, Link.BUTTON_SMALL);
		openLink.setIconRightCSS("o_icon o_icon_start");
		openLink.setPrimary(true);
		row.setOpenFormLink(openLink);
		openLink.setUserObject(row);
		return row;
	}
	
	protected PageRow forgeRow(Section section, AssessmentSection assessmentSection, boolean firstOfSection) {
		PageRow row = new PageRow(null, section, assessmentSection, firstOfSection, config.isAssessable());
		String openLinkId = "open_" + (++counter);
		FormLink openLink = uifactory.addFormLink(openLinkId, "open.full", "open.full.page", null, flc, Link.BUTTON_SMALL);
		openLink.setIconRightCSS("o_icon o_icon_start");
		openLink.setPrimary(true);
		row.setOpenFormLink(openLink);
		openLink.setUserObject(row);
		return row;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(pageCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(newPageCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
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
		removeAsListenerAndDispose(newPageCtrl);
		removeAsListenerAndDispose(cmc);
		newPageCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("open.full".equals(cmd)) {
				PageRow row = (PageRow)link.getUserObject();
				doOpenPage(ureq, row.getPage());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenPage(UserRequest ureq, Page page) {
		pageCtrl = new PageController(ureq, getWindowControl(), stackPanel, secCallback, page);
		listenTo(pageCtrl);
		
		String displayName = StringHelper.escapeHtml(page.getTitle());
		stackPanel.pushController(displayName, pageCtrl);
	}
}