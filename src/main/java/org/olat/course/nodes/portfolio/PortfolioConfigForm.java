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
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.modules.portfolio.ui.BinderController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  6 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioConfigForm extends FormBasicController {

	private static final String[] BINDER_RESOURCE = new String[]{ BinderTemplateResource.TYPE_NAME};

	private final ModuleConfiguration config;

	private boolean inUse;
	private Binder binder;
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
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PortfolioConfigForm(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, PortfolioCourseNode courseNode) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.config = courseNode.getModuleConfiguration();
		this.stackPanel = stackPanel;
		
		mapEntry = courseNode.getReferencedRepositoryEntry();
		if(mapEntry != null) {
			if(BinderTemplateResource.TYPE_NAME.equals(mapEntry.getOlatResource().getResourceableTypeName())) {
				binder = portfolioService.getBinderByResource(mapEntry.getOlatResource());
				RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				if (binder != null) {
					inUse = portfolioService.isTemplateInUse(binder, courseEntry, courseNode.getIdent());
				}
			}
		}
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("manual_user/portfolio/Creating_Portfolio_Tasks/");
		setFormTitle("pane.tab.portfolio_config.title");

		String name = getName(mapEntry);
		mapNameElement = uifactory.addStaticTextElement("map-name", "selected.map", name, formLayout);
		mapNameElement.setVisible(binder == null);
		
		previewMapLink = uifactory.addFormLink("preview", "selected.map", "selected.map", formLayout, Link.LINK);
		previewMapLink.setCustomEnabledLinkCSS("o_preview");
		previewMapLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		previewMapLink.getComponent().setCustomDisplayText(name);
		previewMapLink.setVisible(binder != null);
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
			
			chooseMapLink.setVisible(binder == null);
			chooseMapLink.setEnabled(!inUse);
			chooseMapLink.setTextReasonForDisabling(translate("select.map.disabled.msg"));
			changeMapLink.setVisible(binder != null);
			changeMapLink.setEnabled(!inUse);
			changeMapLink.setTextReasonForDisabling(translate("select.map.disabled.msg"));
			editMapLink.setVisible(binder != null);
		}
	}
	
	protected ModuleConfiguration getUpdatedConfig() {
		if(binder != null) {
			PortfolioCourseNodeEditController.setReference(mapEntry, config);
		}
		return config;
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
			doChangeTemplate(ureq);
		} else if (source == editMapLink) {
			CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, getWindowControl(), courseNode);
		} else if (source == previewMapLink) {
			doPreview(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == searchController) {
			cmc.deactivate();
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) { 
				// search controller done
				RepositoryEntry entry = searchController.getSelectedEntry();
				doSelectTemplate(entry);
				fireEvent(ureq, Event.DONE_EVENT);
			}
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(searchController);
		removeAsListenerAndDispose(cmc);
		searchController = null;
		cmc = null;
	}
	
	private void doChangeTemplate(UserRequest ureq) {
		if(guardModalController(searchController)) return;
		if (isDirty) {
			showWarning("form.dirty");
			return;
		}
		
		removeAsListenerAndDispose(searchController);
		removeAsListenerAndDispose(cmc);
		
		searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, BINDER_RESOURCE,
				translate("select.map2"), false, true, false, false, false, false);
		listenTo(searchController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate("select.map"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doPreview(UserRequest ureq) {
		removeAsListenerAndDispose(previewCtr);
		removeAsListenerAndDispose(columnLayoutCtr);
		
		if(binder != null && stackPanel instanceof TooledStackedPanel) {
			BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getReadOnlyCallback();
			BinderConfiguration bConfig = BinderConfiguration.createTemplateConfig(false);
			previewCtr = new BinderController(ureq, getWindowControl(), (TooledStackedPanel)stackPanel, secCallback, binder, bConfig);
		} else {
			return;
		}
		
		listenTo(previewCtr);
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), previewCtr);
		stackPanel.pushController(translate("preview.map"), columnLayoutCtr);
		listenTo(columnLayoutCtr);
	}
	
	private void doSelectTemplate(RepositoryEntry entry) {
		this.mapEntry = entry;
		
		if (mapEntry != null) {
			if(BinderTemplateResource.TYPE_NAME.equals(mapEntry.getOlatResource().getResourceableTypeName())) {
				binder = portfolioService.getBinderByResource(mapEntry.getOlatResource());
			}
		}
		String name = getName(mapEntry);
		mapNameElement.setValue(name);
		mapNameElement.setVisible(binder == null);
		
		previewMapLink.setVisible(binder != null);
		previewMapLink.getComponent().setCustomDisplayText(name);
		previewMapLink.getComponent().setDirty(true);
		
		chooseMapLink.setVisible(binder == null);
		changeMapLink.setVisible(binder != null);
		editMapLink.setVisible(binder != null);
		
		mapNameElement.setVisible(binder == null);
		flc.setDirty(true);
	}
	
	private String getName(RepositoryEntry mapEntry) {
		String name = translate("error.noreference.short", courseNode.getShortTitle());
		if(mapEntry != null) {
			name = StringHelper.escapeHtml(mapEntry.getDisplayname());
		}
		return name;
	}
}
