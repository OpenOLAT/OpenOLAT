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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.BinderStatistics;
import org.olat.modules.portfolio.ui.BindersDataModel.PortfolioCols;
import org.olat.modules.portfolio.ui.event.OpenBinderEvent;
import org.olat.modules.portfolio.ui.event.OpenMyBindersEvent;
import org.olat.modules.portfolio.ui.model.BinderRow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LastBinderListController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private FormLink allBindersLink;
	private BindersDataModel model;
	private FlexiTableElement tableEl;
	
	private int counter = 0;
	private final int numOfBinders;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public LastBinderListController(UserRequest ureq, WindowControl wControl, int numOfBinders) {
		super(ureq, wControl, "last_binders");
		this.numOfBinders = numOfBinders;

		initForm(ureq);
		loadModel();
	}
	
	public boolean hasBinders() {
		return model.getRowCount() > 0;
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
		tableEl.setElementCssClass("o_portfolio_last_binders");
		tableEl.setEmptyTableMessageKey("table.sEmptyTable");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setPageSize(24);
		VelocityContainer row = createVelocityContainer("binder_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setCssDelegate(new BinderCssDelegate());
		
		String mapperThumbnailUrl = registerCacheableMapper(ureq, "last-binder-list", new ImageMapper(model));
		row.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
		
		allBindersLink = uifactory.addFormLink("all.binders", "all.binders", "all.binders", null, formLayout, Link.LINK | Link.NONTRANSLATED);
		allBindersLink.setIconRightCSS("o_icon o_icon_start");
	}

	@Override
	protected void doDispose() {
		//
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
	
	protected void loadModel() {
		List<BinderStatistics> binderRows = portfolioService.searchOwnedLastBinders(getIdentity(), numOfBinders);
		List<BinderRow> rows = new ArrayList<>(binderRows.size());
		for(BinderStatistics binderRow:binderRows) {
			rows.add(forgePortfolioRow(binderRow));
		}
		model.setObjects(rows);
		tableEl.reset(true, true, true);
		
		int numOfOwnedBinders = portfolioService.countOwnedBinders(getIdentity());
		String allBinderKey = (numOfOwnedBinders <= 1 ?  "all.binder" : "all.binders");
		allBindersLink.setI18nKey(translate(allBinderKey, new String[]{ Integer.toString(numOfOwnedBinders) }));
	}
	
	protected BinderRow forgePortfolioRow(BinderStatistics binderRow) {
		String openLinkId = "open_" + (++counter);
		FormLink openLink = uifactory.addFormLink(openLinkId, "open", "open", null, flc, Link.LINK);
		openLink.setIconRightCSS("o_icon o_icon_start");

		VFSLeaf image = portfolioService.getPosterImageLeaf(binderRow);
		BinderRow row = new BinderRow(binderRow, image, openLink, null);
		openLink.setUserObject(row);
		return row;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(allBindersLink == source) {
			fireEvent(ureq, new OpenMyBindersEvent());
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("open".equals(cmd)) {
				BinderRow row = (BinderRow)link.getUserObject();
				fireEvent(ureq, new OpenBinderEvent(row));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private static class BinderCssDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_portfolio_entry";
		}
	}
}
