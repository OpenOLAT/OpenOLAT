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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.MediaRow;
import org.olat.modules.portfolio.ui.BindersDataModel.PortfolioCols;
import org.olat.modules.portfolio.ui.event.MediaSelectionEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaCenterController extends FormBasicController implements FlexiTableComponentDelegate, FlexiTableRowCssDelegate {
	
	private MediaDataModel model;
	private FlexiTableElement tableEl;
	
	private final boolean select;
	private final TooledStackedPanel stackPanel;
	
	private MediaDetailsController detailsCtrl;
	
	@Autowired
	private PortfolioService portfolioService;
	 
	public MediaCenterController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "medias");
		this.stackPanel = null;
		this.select = true;
		 
		initForm(ureq);
		loadModel();
	}
	
	public MediaCenterController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, "medias");
		this.stackPanel = stackPanel;
		this.select = false;
		 
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PortfolioCols.key, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PortfolioCols.title, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PortfolioCols.open));
	
		model = new MediaDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_binder_page_listing");
		tableEl.setEmtpyTableMessageKey("table.sEmptyTable");
		tableEl.setPageSize(24);
		VelocityContainer row = createVelocityContainer("media_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setRowCssDelegate(this);
		tableEl.setAndLoadPersistedPreferences(ureq, "media-list");
	}
	
	@Override
	public String getRowCssClass(int pos) {
		return null;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}
	
	private void loadModel() {
		List<Media> medias = portfolioService.searchOwnedMedias(getIdentity());
		List<MediaRow> rows = new ArrayList<>(medias.size());
		for(Media media:medias) {
			MediaRow row = new MediaRow(media);
			rows.add(row);
		}
		model.setObjects(rows);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				MediaRow row = model.getObject(se.getIndex());
				if("select".equals(cmd)) {
					if(select) {
						doSelect(ureq, row.getKey());
					} else {
						doOpenMedia(ureq, row.getKey());
					}
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("open".equals(cmd)) {
				MediaRow row = (MediaRow)link.getUserObject();
				Activateable2 activateable = doOpenMedia(ureq, row.getKey());
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
	

	private void doSelect(UserRequest ureq, Long mediaKey) {
		Media media = portfolioService.getMediaByKey(mediaKey);
		fireEvent(ureq, new MediaSelectionEvent(media));
	}
	
	private Activateable2 doOpenMedia(UserRequest ureq, Long mediaKey) {
		Media media = portfolioService.getMediaByKey(mediaKey);
		detailsCtrl = new MediaDetailsController(ureq, getWindowControl(), media);
		listenTo(detailsCtrl);
		
		String displayName = StringHelper.escapeHtml(media.getTitle());
		stackPanel.pushController(displayName, detailsCtrl);
		return detailsCtrl;
	}
}
