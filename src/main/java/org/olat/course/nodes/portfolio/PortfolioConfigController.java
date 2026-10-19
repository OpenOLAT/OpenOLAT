/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */

package org.olat.course.nodes.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Organisation;
import org.olat.course.ICourse;
import org.olat.course.editor.CourseNodeReferenceProvider;
import org.olat.course.editor.NodeEditController;
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
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryReferenceController;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.ReferenceContentProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial Date:  6 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class PortfolioConfigController extends BasicController implements ReferenceContentProvider {

	private final VelocityContainer mainVC;
	private final IconPanelLabelTextContent iconPanelContent;
	private final RepositoryEntryReferenceController referenceCtrl;

	private Controller previewCtr;
	private Controller columnLayoutCtr;

	private final BreadcrumbPanel stackPanel;
	private final PortfolioCourseNode courseNode;
	private final ModuleConfiguration config;
	private final RepositoryEntry courseEntry;

	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private RepositoryService repositoryService;

	public PortfolioConfigController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, PortfolioCourseNode courseNode) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.courseNode = courseNode;
		this.config = courseNode.getModuleConfiguration();
		this.courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();

		mainVC = createVelocityContainer("portfolio_config");

		iconPanelContent = new IconPanelLabelTextContent("content");

		RepositoryEntry mapEntry = courseNode.getReferencedRepositoryEntry();
		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withMessageTranslated(translate("no.portfolio.resource.selected"))
				.withDescTranslated(translate("no.portfolio.resource.selected.text"))
				.withIconCss("o_icon o_BinderTemplate_icon")
				.withPrimaryButton("o_icon_search", "select", null)
				.withSecondaryButton("o_icon_add", "create", null, "create")
				.build();
		List<Organisation> defaultOrganisations = repositoryService.getOrganisations(courseEntry);
		PortfolioCourseNodeReferenceProvider referenceProvider = new PortfolioCourseNodeReferenceProvider(repositoryService,
				List.of(BinderTemplateResource.TYPE_NAME), defaultOrganisations, emptyStateConfig, translate("select.map2"), this);
		referenceCtrl = new RepositoryEntryReferenceController(ureq, wControl, mapEntry, referenceProvider);
		listenTo(referenceCtrl);
		mainVC.put("reference", referenceCtrl.getInitialComponent());

		if (mapEntry != null) {
			updateReferenceContentUI(mapEntry);
		}

		putInitialPanel(mainVC);
	}

	/**
	 * A portfolio template cannot be imported for a course element (only selected or created),
	 * and it cannot be replaced any more once a participant has started collecting it.
	 */
	public class PortfolioCourseNodeReferenceProvider extends CourseNodeReferenceProvider {

		public PortfolioCourseNodeReferenceProvider(RepositoryService repositoryService, List<String> resourceTypes,
				List<Organisation> defaultOrganisations, EmptyStateConfig emptyStateConfig, String selectionTitle,
				ReferenceContentProvider referenceContentProvider) {
			super(repositoryService, resourceTypes, defaultOrganisations, emptyStateConfig, selectionTitle, referenceContentProvider);
		}

		@Override
		public boolean canImport() {
			return false;
		}

		@Override
		public Confirm confirmCanReplace(UserRequest ureq) {
			if (isTemplateInUse(getPortfolioReference())) {
				return new Confirm(false, translate("select.map.disabled.msg"));
			}
			return new Confirm(true, null);
		}
	}

	private RepositoryEntry getPortfolioReference() {
		return courseNode.getReferencedRepositoryEntry();
	}

	private boolean isTemplateInUse(RepositoryEntry mapEntry) {
		if (mapEntry == null || !BinderTemplateResource.TYPE_NAME.equals(mapEntry.getOlatResource().getResourceableTypeName())) {
			return false;
		}
		Binder binder = portfolioService.getBinderByResource(mapEntry.getOlatResource());
		return binder != null && portfolioService.isTemplateInUse(binder, courseEntry, courseNode.getIdent());
	}

	@Override
	public Component getContent(RepositoryEntry repositoryEntry) {
		return iconPanelContent;
	}

	@Override
	public void refresh(Component cmp, RepositoryEntry repositoryEntry) {
		// Refresh is handled on change event.
	}

	private void updateReferenceContentUI(RepositoryEntry mapEntry) {
		int usage = portfolioService.getTemplateUsage(mapEntry);

		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(1);
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("num.collected"), String.valueOf(usage)));
		iconPanelContent.setLabelTexts(labelTexts);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == referenceCtrl) {
			if (event == RepositoryEntryReferenceController.SELECTION_EVENT) {
				doChangeResource(ureq, referenceCtrl.getRepositoryEntry());
			} else if (event == RepositoryEntryReferenceController.PREVIEW_EVENT) {
				doPreview(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	private void doChangeResource(UserRequest ureq, RepositoryEntry mapEntry) {
		if (mapEntry == null) {
			return;
		}
		PortfolioCourseNodeEditController.setReference(mapEntry, config);
		updateReferenceContentUI(mapEntry);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doPreview(UserRequest ureq) {
		RepositoryEntry mapEntry = referenceCtrl.getRepositoryEntry();
		if (mapEntry == null || !(stackPanel instanceof TooledStackedPanel)) {
			return;
		}
		Binder binder = portfolioService.getBinderByResource(mapEntry.getOlatResource());
		if (binder == null) {
			return;
		}

		removeAsListenerAndDispose(previewCtr);
		removeAsListenerAndDispose(columnLayoutCtr);

		BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getReadOnlyCallback();
		BinderConfiguration bConfig = BinderConfiguration.createTemplateConfig(false);
		previewCtr = new BinderController(ureq, getWindowControl(), (TooledStackedPanel)stackPanel, secCallback, binder, bConfig);
		listenTo(previewCtr);

		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), previewCtr);
		stackPanel.pushController(translate("preview.map"), columnLayoutCtr);
		listenTo(columnLayoutCtr);
	}
}
