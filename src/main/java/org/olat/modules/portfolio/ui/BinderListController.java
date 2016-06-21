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

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
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
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackImpl;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.BinderRow;
import org.olat.modules.portfolio.ui.BindersDataModel.PortfolioCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
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
	
	private FlexiTableElement tableEl;
	private BindersDataModel model;
	private final TooledStackedPanel stackPanel;
	private DropdownItem createDropdown;
	private FormLink createBinderLink, createBinderFromTemplateLink, createBinderFromCourseLink;
	
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
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setPageSize(24);
		VelocityContainer row = createVelocityContainer("binder_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setCssDelegate(new BinderCssDelegate());
		tableEl.setAndLoadPersistedPreferences(ureq, "portfolio-list");
		
		mapperThumbnailUrl = registerCacheableMapper(ureq, "binder-list", new ImageMapper(model));
		row.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
		
		createDropdown = new DropdownItem("create.binders", "create.new.binder", getTranslator());
		createDropdown.setButton(true);
		createBinderLink = uifactory.addFormLink("create.empty.binder", formLayout);
		createBinderFromTemplateLink = uifactory.addFormLink("create.empty.binder.from.template", formLayout);
		createBinderFromCourseLink = uifactory.addFormLink("create.empty.binder.from.course", formLayout);
		
		createDropdown.addElement(createBinderLink);
		createDropdown.addElement(createBinderFromTemplateLink);
		createDropdown.addElement(createBinderFromCourseLink);
		
		row.put("createDropdown", createDropdown.getComponent());
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
		List<BinderRow> binderRows = portfolioService.searchOwnedBinders(getIdentity());
		List<BinderWrapper> rows = new ArrayList<>(binderRows.size());
		for(BinderRow binderRow:binderRows) {
			BinderWrapper row = forgePortfolioRow(binderRow);
			rows.add(row);
		}
		rows.add(new BinderWrapper());
		model.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();
	}
	
	private BinderWrapper forgePortfolioRow(BinderRow binderRow) {
		String openLinkId = "open_" + (++counter);
		FormLink openLink = uifactory.addFormLink(openLinkId, "open", "open", null, flc, Link.LINK);
		openLink.setIconRightCSS("o_icon o_icon_start");
		VFSLeaf image = portfolioService.getPosterImageLeaf(binderRow);
		BinderWrapper row = new BinderWrapper(binderRow, image, openLink);
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
		if(createBinderLink == source) {
			doNewBinder(ureq);
		} else if(createBinderFromTemplateLink == source) {
			
		} else if(createBinderFromCourseLink == source) {
			
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("open".equals(cmd)) {
				BinderWrapper row = (BinderWrapper)link.getUserObject();
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
				List<BinderWrapper> rows = binderModel.getObjects();
				for(BinderWrapper prow:rows) {
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
	
	public class BinderWrapper {
		
		private final BinderRow binderRow;
		private final VFSLeaf image;
		private final FormLink openLink;
		private final boolean newBinder;
		
		public BinderWrapper() {
			binderRow = null;
			image = null;
			openLink = null;
			newBinder = true;
		}
		
		public BinderWrapper(BinderRow binderRow, VFSLeaf image, FormLink openLink) {
			this.binderRow = binderRow;
			this.image = image;
			this.openLink = openLink;
			newBinder = false;
		}
		
		public boolean isNewBinder() {
			return newBinder;
		}
		
		public Long getKey() {
			return binderRow == null ? null : binderRow.getKey();
		}

		public String getTitle() {
			return binderRow == null ? null : binderRow.getTitle();
		}
		
		public Date getLastUpdate() {
			return binderRow == null ? null : binderRow.getLastModified();
		}
		
		public String[] getNumOfSectionsAndPages() {
			return new String[]{
					Integer.toString(binderRow.getNumOfSections()),
					Integer.toString(binderRow.getNumOfPages())
				};
		}
		
		public String[] getNumOfComments() {
			return new String[]{
					Integer.toString(binderRow.getNumOfComments())
				};
		}

		public FormLink getOpenLink() {
			return openLink;
		}
		
		public boolean isBackground() {
			return image != null;
		}
		
		public VFSLeaf getBackgroundImage() {
			return image;
		}
		
		public String getImageName() {
			return image == null ? null : image.getName();
		}
		
		public String getOpenFormItemName() {
			return openLink == null ? null : openLink.getComponent().getComponentName();
		}
	}
	
	private static class BinderCssDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_portfolio_entry";
		}
	}
}
