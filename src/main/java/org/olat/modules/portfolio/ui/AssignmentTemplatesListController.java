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

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.manager.PortfolioFileStorage;
import org.olat.modules.portfolio.ui.AssignmentTemplatesDataModel.TemplateCols;
import org.olat.modules.portfolio.ui.component.AssignmentTypeCellRenderer;
import org.olat.modules.portfolio.ui.event.OpenPageEvent;
import org.olat.modules.portfolio.ui.model.AssignmentTemplateRow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 Nov 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentTemplatesListController extends FormBasicController implements TooledController {
	
	private Link newEntryLink;
	private FlexiTableElement tableEl;
	private AssignmentTemplatesDataModel tableModel;
	private TooledStackedPanel stackPanel;

	private Binder binder;
	private int counter = 0;
	private BinderSecurityCallback secCallback;
	
	private CloseableModalController cmc;
	private PageMetadataEditController newPageCtrl; 
	
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private PortfolioFileStorage portfolioFileStorage;
	
	public AssignmentTemplatesListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Binder binder) {
		super(ureq, wControl, "templates_run");
		this.binder = binder;
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		initForm(ureq);
		loadModel();
	}

	@Override
	public void initTools() {
		if(secCallback.canInstantianteBinderAssignment()) {
			newEntryLink = LinkFactory.createToolLink("new.page", translate("create.new.page"), this);
			newEntryLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			newEntryLink.setElementCssClass("o_sel_pf_new_entry");
			stackPanel.addTool(newEntryLink, Align.right);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TemplateCols.type, new AssignmentTypeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TemplateCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TemplateCols.creationDate, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TemplateCols.action));

		tableModel = new AssignmentTemplatesDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
	}
	
	private void loadModel() {
		List<Assignment> assignments = portfolioService.getBindersAssignmentsTemplates(binder);
		List<AssignmentTemplateRow> rows = assignments.stream()
				.map(a -> forgeRow(a)).collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private AssignmentTemplateRow forgeRow(Assignment assignment) {
		AssignmentTemplateRow row = new AssignmentTemplateRow(assignment);
		if(assignment.getAssignmentType() == AssignmentType.document) {
			File directory = portfolioFileStorage.getAssignmentDirectory(assignment);
			File[] files = directory.listFiles(new SystemFilenameFilter(true, false));
			if(files != null && files.length >= 1) {
				DownloadLink downloadLink = uifactory
						.addDownloadLink("download_" + counter++, assignment.getTitle(), null, files[0], tableEl);
				row.setActionLink(downloadLink);
			}
		} else if(assignment.getAssignmentType() == AssignmentType.form) {
			FormLink newLink = uifactory.addFormLink("new_" + (++counter), "new.entry", "create.new.page", null, flc, Link.LINK);
			newLink.setUserObject(row);
			row.setActionLink(newLink);
		}
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newPageCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, new OpenPageEvent(newPageCtrl.getPage()));
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
	public void event(UserRequest ureq, Component source, Event event) {
		if(newEntryLink == source) {
			doCreateNewPage(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("new.entry".equals(link.getCmd())) {
				AssignmentTemplateRow row = (AssignmentTemplateRow)link.getUserObject();
				doCreateNewPage(ureq, row.getAssignment());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doCreateNewPage(UserRequest ureq) {
		if(guardModalController(newPageCtrl)) return;
		
		newPageCtrl = new PageMetadataEditController(ureq, getWindowControl(), secCallback,
				binder, false, (Section)null, true, null);
		listenTo(newPageCtrl);
		
		String title = translate("create.new.page");
		cmc = new CloseableModalController(getWindowControl(), null, newPageCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateNewPage(UserRequest ureq, Assignment assignment) {
		if(guardModalController(newPageCtrl)) return;
		
		newPageCtrl = new PageMetadataEditController(ureq, getWindowControl(), secCallback, binder, false, assignment, true);
		listenTo(newPageCtrl);
		
		String title = translate("create.new.page");
		cmc = new CloseableModalController(getWindowControl(), null, newPageCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
