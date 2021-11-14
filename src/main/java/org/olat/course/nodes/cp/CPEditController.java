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

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.iframe.DeliveryOptionsConfigurationController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.CPCourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.ui.CPPackageConfig;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.cp.CPUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR/> Edit controller for content packaging course nodes <P/>
 * 
 * Initial Date: Oct 13, 2004
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class CPEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_CPCONFIG = "pane.tab.cpconfig";
	private static final String PANE_TAB_DELIVERYOPTIONS = "pane.tab.deliveryOptions";
	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "reporef";
	private static final String VC_CHOSENCP = "chosencp";
	public static final String CONFIG_DELIVERYOPTIONS = "deliveryOptions";
  
	// NLS support:	
	public static final String NLS_ERROR_CPREPOENTRYMISSING = "error.cprepoentrymissing";
	private static final String NLS_NO_CP_CHOSEN = "no.cp.chosen";
	private static final String NLS_COMMAND_CHOOSECP = "command.choosecp";
	private static final String NLS_COMMAND_CREATECP = "command.createcp";
	private static final String NLS_COMMAND_CHANGECP = "command.changecp";
	
	private Panel main;
	private VelocityContainer cpConfigurationVc;
	
	private ModuleConfiguration config;
	private ReferencableEntriesSearchController searchController;
	private DeliveryOptionsConfigurationController deliveryOptionsCtrl;
	
	private CPCourseNode cpNode;
	private CompMenuForm cpMenuForm;

	private TabbedPane myTabbedPane;

	private static final String[] paneKeys = { PANE_TAB_CPCONFIG };

	private Link previewLink;
	private Link editLink;
	private Link chooseCPButton;
	private Link changeCPButton;

	private Controller previewCtr;
	private CloseableModalController cmc;
	private final BreadcrumbPanel stackPanel;
	
	@Autowired
	private CPManager cpManager;
	@Autowired
	private RepositoryService repositoryService;

	public CPEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, CPCourseNode cpNode) {
		super(ureq, wControl);
		this.cpNode = cpNode;
		this.config = cpNode.getModuleConfiguration();
		this.stackPanel = stackPanel;

		main = new Panel("cpmain");
		
		cpConfigurationVc = createVelocityContainer("edit");
		chooseCPButton = LinkFactory.createButtonSmall(NLS_COMMAND_CREATECP, cpConfigurationVc, this);
		chooseCPButton.setElementCssClass("o_sel_cp_choose_repofile");
		changeCPButton = LinkFactory.createButtonSmall(NLS_COMMAND_CHANGECP, cpConfigurationVc, this);
		changeCPButton.setElementCssClass("o_sel_cp_change_repofile");
		
		DeliveryOptions parentConfig = null;
		if (config.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null) {
			// fetch repository entry to display the repository entry title of the chosen cp
			RepositoryEntry re = getCPReference(config, false);
			if (re == null) { // we cannot display the entries name, because the
				// repository entry had been deleted between the time when it was chosen here, and now				
				showError(NLS_ERROR_CPREPOENTRYMISSING);
				cpConfigurationVc.contextPut("showPreviewButton", Boolean.FALSE);
				cpConfigurationVc.contextPut(VC_CHOSENCP, translate("no.cp.chosen"));
			} else {
				if (canManage(re)) {
					editLink = LinkFactory.createButtonSmall("edit", cpConfigurationVc, this);
				}
				cpConfigurationVc.contextPut("showPreviewButton", Boolean.TRUE);
				String displayname = StringHelper.escapeHtml(re.getDisplayname());
				previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", displayname, Link.NONTRANSLATED, cpConfigurationVc, this);
				previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
				previewLink.setTitle(getTranslator().translate("command.preview"));
				
				CPPackageConfig cpConfig = cpManager.getCPPackageConfig(re.getOlatResource());
				parentConfig = (cpConfig == null ? null : cpConfig.getDeliveryOptions());
			}
		} else {
			// no valid config yet
			cpConfigurationVc.contextPut("showPreviewButton", Boolean.FALSE);
			cpConfigurationVc.contextPut(VC_CHOSENCP, translate(NLS_NO_CP_CHOSEN));
		}
		
		Boolean cpMenu = config.getBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU);
		cpMenuForm = new CompMenuForm(ureq, wControl, cpMenu);
		listenTo(cpMenuForm);
		
		cpConfigurationVc.put("cpMenuForm", cpMenuForm.getInitialComponent());

		DeliveryOptions deliveryOptions = (DeliveryOptions)config.get(CPEditController.CONFIG_DELIVERYOPTIONS);
		deliveryOptionsCtrl = new DeliveryOptionsConfigurationController(ureq, getWindowControl(), deliveryOptions,
				"In Five Steps to Your Content Package#_cp_layout", parentConfig, false);
		listenTo(deliveryOptionsCtrl);

		main.setContent(cpConfigurationVc);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == chooseCPButton || source == changeCPButton) {
			removeAsListenerAndDispose(searchController);
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, 
					ImsCPFileResource.TYPE_NAME, translate(NLS_COMMAND_CHOOSECP));			
			listenTo(searchController);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate(NLS_COMMAND_CREATECP)
			);
			listenTo(cmc);
			cmc.activate();
			
		} else if (source == previewLink){
			// Preview as modal dialogue only if the config is valid
			RepositoryEntry re = getCPReference(config, false);
			if (re == null) { // we cannot preview it, because the repository entry
				// had been deleted between the time when it was chosen here, and now				
				showError(NLS_ERROR_CPREPOENTRYMISSING);
			} else {
				File cpRoot = FileResourceManager.getInstance().unzipFileResource(re.getOlatResource());
				Boolean showMenuB = config.getBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU);
				// pre: showMenuB != null
				removeAsListenerAndDispose(previewCtr);
				
				DeliveryOptions previewOptions = deliveryOptionsCtrl.getOptionsForPreview();
				previewCtr = CPUIFactory.getInstance().createMainLayoutPreviewController_v2(ureq, getWindowControl(), new LocalFolderImpl(cpRoot),
						showMenuB.booleanValue(), previewOptions);
				stackPanel.pushController(translate("preview.cp"), previewCtr);
			}
		} else if (source == editLink) {
			CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, getWindowControl(), cpNode);
		}
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == searchController) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) { 
				// search controller done
				// -> close closeable modal controller
				cmc.deactivate();
				RepositoryEntry re = searchController.getSelectedEntry();
				if (re != null) {
					setCPReference(re, config);
					cpConfigurationVc.contextPut("showPreviewButton", Boolean.TRUE);
					String displayname = StringHelper.escapeHtml(re.getDisplayname());
					previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", displayname, Link.NONTRANSLATED, cpConfigurationVc, this);
					previewLink.setCustomEnabledLinkCSS("o_preview");
					previewLink.setTitle(getTranslator().translate("command.preview"));
					// remove existing edit link, add new one if user is allowed to edit this CP
					if (editLink != null) {
						cpConfigurationVc.remove(editLink);
						editLink = null;
					}
					if (canManage(re)) {
						editLink = LinkFactory.createButtonSmall("edit", cpConfigurationVc, this);
					}
					// fire event so the updated config is saved by the editormaincontroller
					fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
					
					CPPackageConfig cpConfig = cpManager.getCPPackageConfig(re.getOlatResource());
					if(cpConfig != null && cpConfig.getDeliveryOptions() != null) {
						deliveryOptionsCtrl.setParentDeliveryOptions(cpConfig.getDeliveryOptions());
					}
				}
			}
			// else cancelled repo search
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

	private boolean canManage(RepositoryEntry re) {
		return repositoryService.hasRoleExpanded(getIdentity(), re,
				OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name(),
				GroupRoles.owner.name());
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_CPCONFIG), main);
		tabbedPane.addTab(translate(PANE_TAB_DELIVERYOPTIONS), deliveryOptionsCtrl.getInitialComponent());
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
		if (previewCtr != null) {
			previewCtr.dispose();
			previewCtr = null;
		}
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