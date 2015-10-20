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

package org.olat.course.nodes.portfolio;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.EPSecurityCallbackImpl;
import org.olat.portfolio.EPTemplateMapResource;
import org.olat.portfolio.EPUIFactory;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPStructureManager;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for PortfolioConfigForm
 * 
 * <P>
 * Initial Date:  6 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioConfigForm extends FormBasicController {
	@Autowired
	private EPFrontendManager ePFMgr;
	@Autowired
	private EPStructureManager eSTMgr;
	private final ModuleConfiguration config;

	private boolean inUse;
	private PortfolioStructureMap map;
	private RepositoryEntry mapEntry;
	
	private ReferencableEntriesSearchController searchController;
	private CloseableModalController cmc;
	
	private FormLink chooseMapLink;
	private FormLink changeMapLink;
	private FormLink editMapLink;
	private FormLink previewMapLink;
	private StaticTextElement mapNameElement;
	
	private Controller previewCtr;
	private Controller columnLayoutCtr;
	private boolean isDirty;
	private final PortfolioCourseNode courseNode;
	private final BreadcrumbPanel stackPanel;
	
	public PortfolioConfigForm(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, PortfolioCourseNode courseNode) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.config = courseNode.getModuleConfiguration();
		this.stackPanel = stackPanel;
		
		mapEntry = courseNode.getReferencedRepositoryEntry();
		if(mapEntry != null) {
			map = (PortfolioStructureMap) ePFMgr.loadPortfolioStructure(mapEntry.getOlatResource());
			Long courseResId = course.getResourceableId();
			OLATResourceable courseOres = OresHelper.createOLATResourceableInstance(CourseModule.class, courseResId);
			if (map != null) {
				inUse = ePFMgr.isTemplateInUse(map, courseOres, courseNode.getIdent(), null);
			}
		}
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pane.tab.portfolio_config.title");

		String name = map == null
				? translate("error.noreference.short", courseNode.getShortTitle())
				: StringHelper.escapeHtml(map.getTitle());
		mapNameElement = uifactory.addStaticTextElement("map-name", "selected.map", name, formLayout);
		mapNameElement.setVisible(map == null);
		
		previewMapLink = uifactory.addFormLink("preview", "selected.map", "selected.map", formLayout, Link.LINK);
		previewMapLink.setCustomEnabledLinkCSS("o_preview");
		previewMapLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		((Link)previewMapLink.getComponent()).setCustomDisplayText(name);
		previewMapLink.setVisible(map != null);
		previewMapLink.setElementCssClass("o_sel_preview_map");
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutContainer = (FormLayoutContainer)formLayout;
		
			FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonGroupLayout.setRootForm(mainForm);
			layoutContainer.add(buttonGroupLayout);
			chooseMapLink = uifactory.addFormLink("select_or_import.map", buttonGroupLayout, Link.BUTTON);
			chooseMapLink.setElementCssClass("o_sel_map_choose_repofile");
			changeMapLink = uifactory.addFormLink("select.map", buttonGroupLayout, Link.BUTTON);
			changeMapLink.setElementCssClass("o_sel_map_change_repofile");
			editMapLink = uifactory.addFormLink("edit.map", buttonGroupLayout, Link.BUTTON);
			editMapLink.setElementCssClass("o_sel_edit_map");
			
			chooseMapLink.setVisible(map == null);
			chooseMapLink.setEnabled(!inUse);
			changeMapLink.setVisible(map != null);
			changeMapLink.setEnabled(!inUse);
			editMapLink.setVisible(map != null);
		}
	}
	
	protected ModuleConfiguration getUpdatedConfig() {
		if(map != null) {
			PortfolioCourseNodeEditController.setReference(mapEntry, map, config);
		}
		return config;
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public void setDirtyFromOtherForm(boolean dirty){
		this.isDirty = dirty;		
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (isDirty) {
			showWarning("form.dirty");
			return;
		}
		if (source == changeMapLink || source == chooseMapLink) {
			removeAsListenerAndDispose(searchController);
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, new String[]{EPTemplateMapResource.TYPE_NAME}, translate("select.map2"),
					false, true, false, false);			
			listenTo(searchController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate("select.map"));
			listenTo(cmc);
			if (isDirty) {
				showWarning("form.dirty");
				return;
			}
			cmc.activate();
		} else if (source == editMapLink) {
			CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, getWindowControl(), courseNode);
		} else if (source == previewMapLink) {
			EPSecurityCallback secCallback = new EPSecurityCallbackImpl(false, true);

			if(previewCtr != null) {
				removeAsListenerAndDispose(previewCtr);
				removeAsListenerAndDispose(columnLayoutCtr);
			}
			previewCtr = EPUIFactory.createPortfolioStructureMapPreviewController(ureq, getWindowControl(), map, secCallback);
			listenTo(previewCtr);
			LayoutMain3ColsController ctr = new LayoutMain3ColsController(ureq, getWindowControl(), previewCtr);
			columnLayoutCtr = ctr;
			stackPanel.pushController(translate("preview.map"), columnLayoutCtr);
			listenTo(columnLayoutCtr);
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == searchController) {
			cmc.deactivate();
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) { 
				// search controller done
				mapEntry = searchController.getSelectedEntry();
				if (mapEntry != null) {
					map = (PortfolioStructureMap)eSTMgr.loadPortfolioStructure(mapEntry.getOlatResource());
					fireEvent(ureq, Event.DONE_EVENT);
				}
				String name = map == null
						? translate("error.noreference.short", courseNode.getShortTitle())
						: StringHelper.escapeHtml(map.getTitle());
				mapNameElement.setValue(name);
				mapNameElement.setVisible(map == null);
				
				previewMapLink.setVisible(map != null);
				((Link)previewMapLink.getComponent()).setCustomDisplayText(name);
				((Link)previewMapLink.getComponent()).setDirty(true);
				
				chooseMapLink.setVisible(map == null);
				changeMapLink.setVisible(map != null);
				editMapLink.setVisible(map != null);
				
				mapNameElement.setVisible(map == null);
				
				flc.setDirty(true);
			}
		}
	}
}
