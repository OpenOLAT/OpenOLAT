/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.cp;

import java.io.File;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.iframe.DeliveryOptionsConfigurationController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.id.Organisation;
import org.olat.core.logging.AssertException;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.course.ICourse;
import org.olat.course.editor.CourseNodeReferenceProvider;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.CPCourseNode;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.ui.CPPackageConfig;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.cp.CPUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.EmptyScreenData;
import org.olat.repository.ui.RepositoryEntryReferenceController;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.ReferenceContentProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR/> Edit controller for content packaging course nodes <P/>
 * 
 * Initial Date: Oct 13, 2004
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class CPEditController extends ActivateableTabbableDefaultController implements ReferenceContentProvider {

	public static final String PANE_TAB_CPCONFIG = "pane.tab.cpconfig";
	private static final String PANE_TAB_DELIVERYOPTIONS = "pane.tab.deliveryOptions";
	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "reporef";
	public static final String CONFIG_DELIVERYOPTIONS = "deliveryOptions";
  
	// NLS support:	
	public static final String NLS_ERROR_CPREPOENTRYMISSING = "error.cprepoentrymissing";
	
	private Panel main;
	private VelocityContainer cpConfigurationVc;
	
	private ModuleConfiguration config;
	private final IconPanelLabelTextContent iconPanelContent;
	private final RepositoryEntryReferenceController referenceCtrl;
	private DeliveryOptionsConfigurationController deliveryOptionsCtrl;
	
	private CPCourseNode cpNode;
	private final RepositoryEntry courseEntry;
	private CompMenuForm cpMenuForm;

	private TabbedPane myTabbedPane;

	private static final String[] paneKeys = { PANE_TAB_CPCONFIG };

	private Controller previewCtr;
	private final BreadcrumbPanel stackPanel;
	
	@Autowired
	private CPManager cpManager;
	@Autowired
	private RepositoryService repositoryService;

	public CPEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, CPCourseNode cpNode, ICourse course) {
		super(ureq, wControl);
		this.cpNode = cpNode;
		this.config = cpNode.getModuleConfiguration();
		this.stackPanel = stackPanel;
		courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();

		main = new Panel("cpmain");
		
		cpConfigurationVc = createVelocityContainer("edit");
		
		iconPanelContent = new IconPanelLabelTextContent("content");

		RepositoryEntry cpEntry = cpNode.getReferencedRepositoryEntry();
		DeliveryOptions parentConfig = null;
		if (cpEntry != null) {
			CPPackageConfig cpConfig = cpManager.getCPPackageConfig(cpEntry.getOlatResource());
			parentConfig = (cpConfig == null ? null : cpConfig.getDeliveryOptions());
		} else if (config.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null) {
			// we cannot display the entry, because the repository entry had been deleted
			// between the time when it was chosen here, and now
			showError(NLS_ERROR_CPREPOENTRYMISSING);
		}

		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withMessageTranslated(translate("no.cp.resource.selected"))
				.withHintTranslated(translate("no.cp.resource.selected.text"))
				.withIconCss("o_icon o_FileResource-IMSCP_icon")
				.withPrimaryButton("o_icon_search o_sel_cp_choose_repofile", "select", null)
				.withSecondaryButton("o_icon_add", "create", null, "create")
				.withSecondaryButton("o_icon_import", "import", null, "import")
				.build();
		List<Organisation> defaultOrganisations = repositoryService.getOrganisations(courseEntry);
		CPCourseNodeReferenceProvider referenceProvider = new CPCourseNodeReferenceProvider(repositoryService,
				List.of(ImsCPFileResource.TYPE_NAME), defaultOrganisations, emptyStateConfig, translate("command.choosecp"), this);
		referenceCtrl = new RepositoryEntryReferenceController(ureq, wControl, cpEntry, referenceProvider);
		listenTo(referenceCtrl);
		cpConfigurationVc.put("reference", referenceCtrl.getInitialComponent());

		Boolean cpMenu = config.getBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU);
		cpMenuForm = new CompMenuForm(ureq, wControl, cpMenu);
		listenTo(cpMenuForm);
		
		cpConfigurationVc.put("cpMenuForm", cpMenuForm.getInitialComponent());

		DeliveryOptions deliveryOptions = (DeliveryOptions)config.get(CPEditController.CONFIG_DELIVERYOPTIONS);
		deliveryOptionsCtrl = new DeliveryOptionsConfigurationController(ureq, getWindowControl(), deliveryOptions,
				"manual_user/learningresources/Course_Element_CP_Learning_Content/", parentConfig, false);
		listenTo(deliveryOptionsCtrl);

		main.setContent(cpConfigurationVc);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == referenceCtrl) {
			if (event == RepositoryEntryReferenceController.SELECTION_EVENT) {
				doChangeCP(urequest, referenceCtrl.getRepositoryEntry());
			} else if (event == RepositoryEntryReferenceController.PREVIEW_EVENT) {
				doPreview(urequest);
			}
		} else if (source == cpMenuForm) {
			if (event == Event.DONE_EVENT) {
				config.setBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU, cpMenuForm.isCpMenu());
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == deliveryOptionsCtrl) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				config.set(CPEditController.CONFIG_DELIVERYOPTIONS, deliveryOptionsCtrl.getDeliveryOptions());
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	private void doChangeCP(UserRequest ureq, RepositoryEntry re) {
		if (re == null) {
			return;
		}
		setCPReference(re, config);
		// fire event so the updated config is saved by the editormaincontroller
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);

		CPPackageConfig cpConfig = cpManager.getCPPackageConfig(re.getOlatResource());
		if (cpConfig != null && cpConfig.getDeliveryOptions() != null) {
			deliveryOptionsCtrl.setParentDeliveryOptions(cpConfig.getDeliveryOptions());
		}
	}

	private void doPreview(UserRequest ureq) {
		RepositoryEntry re = getCPReference(config, false);
		if (re == null) { // we cannot preview it, because the repository entry
			// had been deleted between the time when it was chosen here, and now
			showError(NLS_ERROR_CPREPOENTRYMISSING);
			return;
		}

		File cpRoot = FileResourceManager.getInstance().unzipFileResource(re.getOlatResource());
		Boolean showMenuB = config.getBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU);
		// pre: showMenuB != null
		removeAsListenerAndDispose(previewCtr);

		DeliveryOptions previewOptions = deliveryOptionsCtrl.getOptionsForPreview();
		previewCtr = CPUIFactory.getInstance().createMainLayoutPreviewController_v2(ureq, getWindowControl(), new LocalFolderImpl(cpRoot),
				showMenuB.booleanValue(), previewOptions);
		listenTo(previewCtr);
		stackPanel.pushController(translate("preview.cp"), previewCtr);
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_CPCONFIG), "o_sel_repo_entry", main);
		tabbedPane.addTab(translate(PANE_TAB_DELIVERYOPTIONS), "o_sel_cp_layout_options", deliveryOptionsCtrl.getInitialComponent());
	}

	/**
	 * @param config the moduleConfig
	 * @param strict an assertion exception is thrown if no entry is found when
	 *          strict is set to true, otherwise, null is returned
	 * @return the repositoryentry or null if not in strict mode and no entry
	 *         found
	 * @throws AssertException when in strict mode and no entry is found
	 */
	public static RepositoryEntry getCPReference(ModuleConfiguration config, boolean strict) {
		if (config == null) {
			if (strict) throw new AssertException("missing config in CP");
			else return null;
		}
		String repoSoftkey = (String) config.get(CPEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (repoSoftkey == null) {
			if (strict) throw new AssertException("invalid config when being asked for references");
			else return null;
		}
		RepositoryManager rm = RepositoryManager.getInstance();
		return rm.lookupRepositoryEntryBySoftkey(repoSoftkey, strict);
	}

	/**
	 * Remove the reference to the repository entry.
	 * 
	 * @param moduleConfiguration
	 */
	public static void removeCPReference(ModuleConfiguration moduleConfiguration) {
		moduleConfiguration.remove(CPEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
	}

	/**
	 * Set the referenced repository entry.
	 * 
	 * @param re
	 * @param moduleConfiguration
	 */
	public static void setCPReference(RepositoryEntry re, ModuleConfiguration moduleConfiguration) {
		moduleConfiguration.set(CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());
	}

	/**
	 * @param moduleConfiguration
	 * @return boolean
	 */
	public static boolean isModuleConfigValid(ModuleConfiguration moduleConfiguration) {
		return (moduleConfiguration.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null);
	}

	@Override
	protected void doDispose() {
		//child controllers registered with listenTo() get disposed in BasicController
        super.doDispose();
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

	@Override
	public Component getContent(RepositoryEntry repositoryEntry) {
		return iconPanelContent;
	}

	@Override
	public void refresh(Component cmp, RepositoryEntry repositoryEntry) {
		// Refresh is handled on change event.
	}

	/**
	 * A CP learning content can be selected, created or imported for a course element,
	 * and replace is unconditional (matches the previous behaviour).
	 */
	public class CPCourseNodeReferenceProvider extends CourseNodeReferenceProvider {

		public CPCourseNodeReferenceProvider(RepositoryService repositoryService, List<String> resourceTypes,
				List<Organisation> defaultOrganisations, EmptyStateConfig emptyStateConfig, String selectionTitle,
				ReferenceContentProvider referenceContentProvider) {
			super(repositoryService, resourceTypes, defaultOrganisations, emptyStateConfig, selectionTitle, referenceContentProvider);
		}

		@Override
		public boolean useNewSelectionDialog() {
			return true;
		}

		@Override
		public String getMyTabLabel() {
			return translate("my.cps");
		}

		@Override
		public EmptyScreenData getEmptyScreenDataForNoEntriesYet() {
			return new EmptyScreenData("o_FileResource-IMSCP_icon",
					translate("select.no.cp.yet"),
					translate("select.no.cp.yet.hint"));
		}

		@Override
		public EmptyScreenData getEmptyScreenDataForNoEntriesInView() {
			return new EmptyScreenData("o_FileResource-IMSCP_icon",
					translate("select.no.cps.in.view"),
					translate("select.no.cps.in.view.hint"));
		}

		@Override
		public String getEnterSearchTermMessage() {
			return translate("select.enter.search.term");
		}
	}

}

class CompMenuForm extends FormBasicController {
	
	/**
	 * Simple form for asking whether component menu should be shown or not.
	 * 
	 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
	 */
	private SelectionElement cpMenu;

	private boolean compMenuConfig;

	CompMenuForm(UserRequest ureq, WindowControl wControl, Boolean compMenuConfig) {
		super(ureq, wControl);
		this.compMenuConfig = compMenuConfig == null ? true:compMenuConfig.booleanValue();
		initForm(ureq);
	}

	public boolean isCpMenu() {
		return cpMenu.isSelected(0);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {	
		cpMenu = uifactory.addCheckboxesHorizontal("cpMenu", "display.config.compMenu", formLayout, new String[]{"xx"}, new String[]{null});
		cpMenu.select("xx",compMenuConfig);

		uifactory.addFormSubmitButton("submit", formLayout);
	}
}