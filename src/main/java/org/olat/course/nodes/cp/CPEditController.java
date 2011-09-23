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
* <p>
*/ 

package org.olat.course.nodes.cp;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.Constants;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.CPCourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.scorm.ScormEditController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.cp.CPUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;

/**
 * Description:<BR/> Edit controller for content packaging course nodes <P/>
 * 
 * Initial Date: Oct 13, 2004
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class CPEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_CPCONFIG = "pane.tab.cpconfig";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	private static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "reporef";
	private static final String VC_CHOSENCP = "chosencp";
  
	// NLS support:	
	private static final String NLS_ERROR_CPREPOENTRYMISSING = "error.cprepoentrymissing";
	private static final String NLS_NO_CP_CHOSEN = "no.cp.chosen";
	private static final String NLS_CONDITION_ACCESSIBILITY_TITLE = "condition.accessibility.title";
	private static final String NLS_COMMAND_CHOOSECP = "command.choosecp";
	private static final String NLS_COMMAND_CREATECP = "command.createcp";
	private static final String NLS_COMMAND_CHANGECP = "command.changecp";
	
	private Panel main;
	private VelocityContainer cpConfigurationVc;
	
	private ModuleConfiguration config;
	private ReferencableEntriesSearchController searchController;
	
	private ConditionEditController accessibilityCondContr;
	private CPCourseNode cpNode;
	private CompMenuForm cpMenuForm;

	private TabbedPane myTabbedPane;

	final static String[] paneKeys = { PANE_TAB_CPCONFIG, PANE_TAB_ACCESSIBILITY };

	private Link previewLink;
	private Link editLink;
	private Link chooseCPButton;
	private Link changeCPButton;

	private LayoutMain3ColsPreviewController previewCtr;
	private CloseableModalController cmc;

	/**
	 * @param cpNode
	 * @param ureq
	 * @param wControl
	 * @param course
	 */
	public CPEditController(CPCourseNode cpNode, UserRequest ureq, WindowControl wControl, ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		this.cpNode = cpNode;
		this.config = cpNode.getModuleConfiguration();		

		main = new Panel("cpmain");		
		
		cpConfigurationVc = this.createVelocityContainer("edit");
		chooseCPButton = LinkFactory.createButtonSmall(NLS_COMMAND_CREATECP, cpConfigurationVc, this);
		changeCPButton = LinkFactory.createButtonSmall(NLS_COMMAND_CHANGECP, cpConfigurationVc, this);
		
		if (config.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null) {
			// fetch repository entry to display the repository entry title of the chosen cp
			RepositoryEntry re = getCPReference(config, false);
			if (re == null) { // we cannot display the entries name, because the
				// repository entry had been deleted between the time when it was chosen here, and now				
				this.showError(NLS_ERROR_CPREPOENTRYMISSING);
				cpConfigurationVc.contextPut("showPreviewButton", Boolean.FALSE);
				cpConfigurationVc.contextPut(VC_CHOSENCP, translate("no.cp.chosen"));
			} else {
				if (isEditable(ureq.getIdentity(), re)) {
					editLink = LinkFactory.createButtonSmall("edit", cpConfigurationVc, this);
				}
				cpConfigurationVc.contextPut("showPreviewButton", Boolean.TRUE);
				previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", re.getDisplayname(), Link.NONTRANSLATED, cpConfigurationVc, this);
				previewLink.setCustomEnabledLinkCSS("b_preview");
				previewLink.setTitle(getTranslator().translate("command.preview"));
			}
		} else {
			// no valid config yet
			cpConfigurationVc.contextPut("showPreviewButton", Boolean.FALSE);
			cpConfigurationVc.contextPut(VC_CHOSENCP, translate(NLS_NO_CP_CHOSEN));
		}
		
		boolean cpMenu = config.getBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU);
		String contentEncoding = (String)config.get(NodeEditController.CONFIG_CONTENT_ENCODING);
		String jsEncoding = (String)config.get(NodeEditController.CONFIG_JS_ENCODING);
		cpMenuForm = new CompMenuForm(ureq, wControl, cpMenu, contentEncoding, jsEncoding);
		listenTo(cpMenuForm);
		
		cpConfigurationVc.put("cpMenuForm", cpMenuForm.getInitialComponent());

		// Accessibility precondition
		Condition accessCondition = cpNode.getPreConditionAccess();
		accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), course.getCourseEnvironment().getCourseGroupManager(),
				accessCondition, "accessabilityConditionForm", AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), cpNode), euce);		
		listenTo(accessibilityCondContr);

		main.setContent(cpConfigurationVc);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
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
				previewCtr = CPUIFactory.getInstance().createMainLayoutPreviewController(ureq, getWindowControl(), new LocalFolderImpl(cpRoot), showMenuB.booleanValue());
				previewCtr.activate();
			}
		} else if (source == editLink) {
			CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, cpNode);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
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
					previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", re.getDisplayname(), Link.NONTRANSLATED, cpConfigurationVc, this);
					previewLink.setCustomEnabledLinkCSS("b_preview");
					previewLink.setTitle(getTranslator().translate("command.preview"));
					// remove existing edit link, add new one if user is allowed to edit this CP
					if (editLink != null) {
						cpConfigurationVc.remove(editLink);
						editLink = null;
					}
					if (isEditable(urequest.getIdentity(), re)) {
						editLink = LinkFactory.createButtonSmall("edit", cpConfigurationVc, this);
					}
					// fire event so the updated config is saved by the editormaincontroller
					fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
				}
			}
			// else cancelled repo search
		} else if (source == accessibilityCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessibilityCondContr.getCondition();
				cpNode.setPreConditionAccess(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == cpMenuForm) {
			if (event == Event.DONE_EVENT) {
				config.setBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU, cpMenuForm.isCpMenu());
				config.set(NodeEditController.CONFIG_CONTENT_ENCODING, cpMenuForm.getContentEncoding());
				config.set(NodeEditController.CONFIG_JS_ENCODING, cpMenuForm.getJSEncoding());
				
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	/**
	 * @param identity
	 * @param repository entry
	 * @return
	 */
	private boolean isEditable(Identity identity, RepositoryEntry re) {
		return (BaseSecurityManager.getInstance().isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN)
				|| RepositoryManager.getInstance().isOwnerOfRepositoryEntry(identity, re) 
				|| RepositoryManager.getInstance().isInstitutionalRessourceManagerFor(re, identity));
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableDefaultController#addTabs(org.olat.core.gui.components.TabbedPane)
	 */
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;

		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate(NLS_CONDITION_ACCESSIBILITY_TITLE)));
		tabbedPane.addTab(translate(PANE_TAB_CPCONFIG), main);
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
		RepositoryEntry entry = rm.lookupRepositoryEntryBySoftkey(repoSoftkey, strict);
		// entry can be null only if !strict
		return entry;
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

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//child controllers registered with listenTo() get disposed in BasicController
		if (previewCtr != null) {
			previewCtr.dispose();
			previewCtr = null;
		}
	}

	public String[] getPaneKeys() {
		return paneKeys;
	}

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

	// NLS support:
	private static final String NLS_DISPLAY_CONFIG_COMPMENU = "display.config.compMenu";

	private SelectionElement cpMenu;
	private SingleSelection encodingContentEl;
	private SingleSelection encodingJSEl;
	
	private boolean compMenuConfig;
	private String contentEncoding;
	private String jsEncoding;
	
	private String[] encodingContentKeys, encodingContentValues;
	private String[] encodingJSKeys, encodingJSValues;
	
	CompMenuForm(UserRequest ureq, WindowControl wControl, Boolean compMenuConfig, String contentEncoding, String jsEncoding) {
		super(ureq, wControl);
		this.compMenuConfig = compMenuConfig == null ? true:compMenuConfig.booleanValue();
		this.contentEncoding = contentEncoding;
		this.jsEncoding = jsEncoding;
		
		Map<String,Charset> charsets = Charset.availableCharsets();
		int numOfCharsets = charsets.size() + 1;
		
		encodingContentKeys = new String[numOfCharsets];
		encodingContentKeys[0] = NodeEditController.CONFIG_CONTENT_ENCODING_AUTO;

		encodingContentValues = new String[numOfCharsets];
		encodingContentValues[0] = translate("encoding.auto");
		
		encodingJSKeys = new String[numOfCharsets];
		encodingJSKeys[0] = NodeEditController.CONFIG_JS_ENCODING_AUTO;

		encodingJSValues = new String[numOfCharsets];
		encodingJSValues[0] =	translate("encoding.same");

		int count = 1;
		Locale locale = ureq.getLocale();
		for(Map.Entry<String, Charset> charset:charsets.entrySet()) {
			encodingContentKeys[count] = charset.getKey();
			encodingContentValues[count] = charset.getValue().displayName(locale);
			encodingJSKeys[count] = charset.getKey();
			encodingJSValues[count] = charset.getValue().displayName(locale);
			count++;
		}
		
		initForm(ureq);
	}
	
	public Object getJSEncoding() {
		return encodingJSEl.getSelectedKey();
	}

	public Object getContentEncoding() {
		return encodingContentEl.getSelectedKey();
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
		cpMenu = uifactory.addCheckboxesVertical("cpMenu", NLS_DISPLAY_CONFIG_COMPMENU, formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		cpMenu.select("xx",compMenuConfig);
		
		encodingContentEl = uifactory.addDropdownSingleselect("encoContent", "encoding.content", formLayout, encodingContentKeys, encodingContentValues, null);
		if (Arrays.asList(encodingContentKeys).contains(contentEncoding)) {
			encodingContentEl.select(contentEncoding, true);
		} else {
			encodingContentEl.select(NodeEditController.CONFIG_CONTENT_ENCODING_AUTO, true);
		}
		
		encodingJSEl = uifactory.addDropdownSingleselect("encoJS", "encoding.js", formLayout, encodingJSKeys, encodingJSValues, null);
		if (Arrays.asList(encodingJSKeys).contains(jsEncoding)) {
			encodingJSEl.select(jsEncoding, true);
		} else {
			encodingJSEl.select(NodeEditController.CONFIG_JS_ENCODING_AUTO, true);
		}
		
		uifactory.addFormSubmitButton("submit", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}
}