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

package org.olat.course.nodes.scorm;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
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
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.scorm.ScormAPIandDisplayController;
import org.olat.modules.scorm.ScormConstants;
import org.olat.modules.scorm.ScormMainManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<BR/> Edit controller for content packaging course nodes <P/>
 * 
 * Initial Date: Oct 13, 2004
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class ScormEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_CPCONFIG = "pane.tab.cpconfig";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";

	private static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "reporef";
	public static final String CONFIG_SHOWMENU = "showmenu";
	public static final String CONFIG_SHOWNAVBUTTONS = "shownavbuttons";
	public static final String CONFIG_ISASSESSABLE = "isassessable";
	public static final String CONFIG_CUTVALUE = "cutvalue";
	public static final String CONFIG_HEIGHT = "height";	
	public final static String CONFIG_HEIGHT_AUTO = "auto";

	private static final String VC_CHOSENCP = "chosencp";

	private static final String[] paneKeys = { PANE_TAB_CPCONFIG, PANE_TAB_ACCESSIBILITY };

	// NLS support:
	
	private static final String NLS_ERROR_CPREPOENTRYMISSING = "error.cprepoentrymissing";
	private static final String NLS_NO_CP_CHOSEN = "no.cp.chosen";
	private static final String NLS_CONDITION_ACCESSIBILITY_TITLE = "condition.accessibility.title";
	
	private Panel main;
	private VelocityContainer cpConfigurationVc;

	private ModuleConfiguration config;
	private ReferencableEntriesSearchController searchController;
	private CloseableModalController cmc;
	
	private ConditionEditController accessibilityCondContr;
	private ScormCourseNode scormNode;

	private LayoutMain3ColsPreviewController previewLayoutCtr;

	private TabbedPane myTabbedPane;

	private ICourse course;

	private VarForm scorevarform;

	private Link previewLink;
	private Link chooseCPButton;
	private Link changeCPButton;

	/**
	 * @param cpNode CourseNode
	 * @param ureq
	 * @param wControl
	 * @param course Course Interface
	 * @param euce User course environment
	 */
	public ScormEditController(ScormCourseNode scormNode, UserRequest ureq, WindowControl wControl, ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		//o_clusterOk by guido: save to hold reference to course inside editor
		this.course = course;
		this.scormNode = scormNode;
		this.config = scormNode.getModuleConfiguration();
		main = new Panel("cpmain");				
		cpConfigurationVc = this.createVelocityContainer("edit");
		
		chooseCPButton = LinkFactory.createButtonSmall("command.importcp", cpConfigurationVc, this);
		changeCPButton = LinkFactory.createButtonSmall("command.changecp", cpConfigurationVc, this);
		
		if (config.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null) {
			// fetch repository entry to display the repository entry title of the
			// chosen cp
			RepositoryEntry re = getScormCPReference(config, false);
			if (re == null) { // we cannot display the entries name, because the repository entry had been deleted 
												// between the time when it was chosen here, and now				
				this.showError(NLS_ERROR_CPREPOENTRYMISSING);
				cpConfigurationVc.contextPut("showPreviewButton", Boolean.FALSE);
				cpConfigurationVc.contextPut(VC_CHOSENCP, translate(NLS_NO_CP_CHOSEN));
			} else {
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
		
		// add the form for choosing the score variable
		boolean showMenu = config.getBooleanSafe(CONFIG_SHOWMENU, true);
		boolean showNavButtons = config.getBooleanSafe(CONFIG_SHOWNAVBUTTONS, true);
		boolean assessable = config.getBooleanSafe(CONFIG_ISASSESSABLE);
		int cutvalue = config.getIntegerSafe(CONFIG_CUTVALUE, 0);
		String height = (String) config.get(CONFIG_HEIGHT);
		String encContent = (String) config.get(NodeEditController.CONFIG_CONTENT_ENCODING);
		String encJS = (String) config.get(NodeEditController.CONFIG_JS_ENCODING);
		
		//= conf.get(CONFIG_CUTVALUE);
		scorevarform = new VarForm(ureq, wControl, showMenu, showNavButtons, height, encContent, encJS, assessable, cutvalue);
		listenTo(scorevarform);
		cpConfigurationVc.put("scorevarform", scorevarform.getInitialComponent());

		// Accessibility precondition
		Condition accessCondition = scormNode.getPreConditionAccess();
		accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), course.getCourseEnvironment().getCourseGroupManager(), 
				accessCondition, "accessabilityConditionForm",
				AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), scormNode), euce);		
		this.listenTo(accessibilityCondContr);

		main.setContent(cpConfigurationVc);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == chooseCPButton || source == changeCPButton) { // those must be links
			removeAsListenerAndDispose(searchController);
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq,
					ScormCPFileResource.TYPE_NAME, translate("command.choosecp"));			
			listenTo(searchController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate("command.importcp"));
			listenTo(cmc);
			cmc.activate();
		} else if (source == previewLink){
			// Preview as modal dialogue
			// only if the config is valid
			RepositoryEntry re = getScormCPReference(config, false);
			if (re == null) { // we cannot preview it, because the repository entry
												// had been deleted between the time when it was
												// chosen here, and now				
				this.showError("error.cprepoentrymissing");
			} else {
				File cpRoot = FileResourceManager.getInstance().unzipFileResource(re.getOlatResource());
				boolean showMenu = config.getBooleanSafe(CONFIG_SHOWMENU, true);
				
				if (previewLayoutCtr != null) previewLayoutCtr.dispose();
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapScormRepositoryEntry(re));
				ScormAPIandDisplayController previewController = ScormMainManager.getInstance().createScormAPIandDisplayController(ureq, getWindowControl(), showMenu, null, cpRoot, null, course.getResourceableId().toString(),
						ScormConstants.SCORM_MODE_BROWSE, ScormConstants.SCORM_MODE_NOCREDIT, true, true);				
				// configure some display options
				boolean showNavButtons = config.getBooleanSafe(ScormEditController.CONFIG_SHOWNAVBUTTONS, true);
				previewController.showNavButtons(showNavButtons);
				String height = (String) config.get(ScormEditController.CONFIG_HEIGHT);
				if ( ! height.equals(ScormEditController.CONFIG_HEIGHT_AUTO)) {
					previewController.setHeightPX(Integer.parseInt(height));
				}
				String contentEncoding = (String) config.get(NodeEditController.CONFIG_CONTENT_ENCODING);
				if ( ! contentEncoding.equals(NodeEditController.CONFIG_CONTENT_ENCODING_AUTO)) {
					previewController.setContentEncoding(contentEncoding);
				}
				String jsEncoding = (String) config.get(NodeEditController.CONFIG_JS_ENCODING);
				if ( ! jsEncoding.equals(NodeEditController.CONFIG_JS_ENCODING_AUTO)) {
					previewController.setJSEncoding(jsEncoding);
				}
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == searchController) {			
			cmc.deactivate();
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) { 
				// search controller done
				RepositoryEntry re = searchController.getSelectedEntry();
				if (re != null) {
					setScormCPReference(re, config);
					cpConfigurationVc.contextPut("showPreviewButton", Boolean.TRUE);
					previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", re.getDisplayname(), Link.NONTRANSLATED, cpConfigurationVc, this);
					previewLink.setCustomEnabledLinkCSS("b_preview");
					previewLink.setTitle(getTranslator().translate("command.preview"));
					// fire event so the updated config is saved by the
					// editormaincontroller
					fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
				}
				// else cancelled repo search
			}
		} else if (source == accessibilityCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessibilityCondContr.getCondition();
				scormNode.setPreConditionAccess(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == scorevarform) {
			if (event == Event.DONE_EVENT) {
				boolean showmenu = scorevarform.isShowMenu();
				config.setBooleanEntry(CONFIG_SHOWMENU, showmenu);
				boolean showNavButtons = scorevarform.isShowNavButtons();
				config.setBooleanEntry(CONFIG_SHOWNAVBUTTONS, showNavButtons);
				boolean assessable = scorevarform.isAssessable();
				config.setBooleanEntry(CONFIG_ISASSESSABLE, assessable);
				int cutvalue = scorevarform.getCutValue();
				config.setIntValue(CONFIG_CUTVALUE, cutvalue);
				String height = scorevarform.getHeightValue();
				config.set(CONFIG_HEIGHT, height);
				String encContent = scorevarform.getEncodingContentValue();
				config.set(NodeEditController.CONFIG_CONTENT_ENCODING, encContent);
				String encJS = scorevarform.getEncodingJSValue();
				config.set(NodeEditController.CONFIG_JS_ENCODING, encJS);
				// fire event so the updated config is saved by the
				// editormaincontroller
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableDefaultController#addTabs(org.olat.core.gui.components.TabbedPane)
	 */
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate(NLS_CONDITION_ACCESSIBILITY_TITLE)));
		tabbedPane.addTab(translate(PANE_TAB_CPCONFIG), main); // the choose learning content tab
	}

	/**
	 * @param config the moduleConfig
	 * @param strict an assertion exception is thrown if no entry is found when
	 *          strict is set to true, otherwise, null is returned
	 * @return the repositoryentry or null if not in strict mode and no entry
	 *         found
	 * @throws AssertException when in strict mode and no entry is found
	 */
	public static RepositoryEntry getScormCPReference(ModuleConfiguration config, boolean strict) {
		if (config == null) throw new AssertException("missing config in CP");
		String repoSoftkey = (String) config.get(ScormEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (repoSoftkey == null) throw new AssertException("invalid config when being asked for references");
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
	public static void removeScormCPReference(ModuleConfiguration moduleConfiguration) {
		moduleConfiguration.remove(ScormEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
	}

	/**
	 * Set the referenced repository entry.
	 * 
	 * @param re
	 * @param moduleConfiguration
	 */
	public static void setScormCPReference(RepositoryEntry re, ModuleConfiguration moduleConfiguration) {
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
		if (previewLayoutCtr != null) {
			previewLayoutCtr.dispose();
			previewLayoutCtr = null;
		}
	}

	public String[] getPaneKeys() {
		return paneKeys;
	}

	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}
}

class VarForm extends FormBasicController {
	private SelectionElement showMenuEl;
	private SelectionElement showNavButtonsEl;
	private SelectionElement isAssessableEl;
	private IntegerElement cutValueEl;
	private SingleSelection heightEl;
	private SingleSelection encodingContentEl;
	private SingleSelection encodingJSEl;
	
	private boolean showMenu, showNavButtons, isAssessable;
	private String height;
	private String encodingContent;
	private String encodingJS;
	private int cutValue;
	private String[] keys, values;
	private String[] encodingContentKeys, encodingContentValues;
	private String[] encodingJSKeys, encodingJSValues;
	
	/**
	 * 
	 * @param name  Name of the form
	 */
	public VarForm(UserRequest ureq, WindowControl wControl, boolean showMenu, boolean showNavButtons, String height,
			String encodingContent, String encodingJS, boolean isAssessable, int cutValue) {
		super(ureq, wControl);
		this.showMenu = showMenu;
		this.showNavButtons = showNavButtons;
		this.isAssessable = isAssessable;
		this.cutValue = cutValue;
		this.height = height;
		this.encodingContent = encodingContent;
		this.encodingJS = encodingJS;
		
		keys = new String[]{ ScormEditController.CONFIG_HEIGHT_AUTO, "460", "480", 
				"500", "520", "540", "560", "580",
				"600", "620", "640", "660", "680",
				"700", "720", "730", "760", "780",
				"800", "820", "840", "860", "880",
				"900", "920", "940", "960", "980",
				"1000", "1020", "1040", "1060", "1080",
				"1100", "1120", "1140", "1160", "1180",
				"1200", "1220", "1240", "1260", "1280",
				"1300", "1320", "1340", "1360", "1380"
		};
		
		values = new String[]{ translate("height.auto"), "460px", "480px", 
				"500px", "520px", "540px", "560px", "580px",
				"600px", "620px", "640px", "660px", "680px",
				"700px", "720px", "730px", "760px", "780px",
				"800px", "820px", "840px", "860px", "880px",
				"900px", "920px", "940px", "960px", "980px",
				"1000px", "1020px", "1040px", "1060px", "1080px",
				"1100px", "1120px", "1140px", "1160px", "1180px",
				"1200px", "1220px", "1240px", "1260px", "1280px",
				"1300px", "1320px", "1340px", "1360px", "1380px"
		};
		
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
		
		initForm (ureq);
	}
	

	/**
	 * @return
	 */
	public int getCutValue() {
		return cutValueEl.getIntValue();
	}

	public boolean isShowMenu() {
		return showMenuEl.isSelected(0);
	}

	public boolean isShowNavButtons() {
		return showNavButtonsEl.isSelected(0);
	}

	public boolean isAssessable() {
		return isAssessableEl.isSelected(0);
	}
	
	public String getHeightValue() {
		return heightEl.getSelectedKey();
	}
	
	public String getEncodingContentValue() {
		return encodingContentEl.getSelectedKey();
	}
	
	public String getEncodingJSValue() {
		return encodingJSEl.getSelectedKey();
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("headerform");
		setFormContextHelp("org.olat.course.nodes.scorm","ced-scorm-settings.html","help.hover.scorm-settings-filename");
		
		showMenuEl = uifactory.addCheckboxesVertical("showmenu", "showmenu.label", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		showMenuEl.select("xx", showMenu);
		
		showNavButtonsEl = uifactory.addCheckboxesVertical("shownavbuttons", "shownavbuttons.label", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		showNavButtonsEl.select("xx", showNavButtons);
		
		heightEl = uifactory.addDropdownSingleselect("height", "height.label", formLayout, keys, values, null);
		if (Arrays.asList(keys).contains(height)) {
			heightEl.select(height, true);
		} else {
			heightEl.select(ScormEditController.CONFIG_HEIGHT_AUTO, true);
		}

		encodingContentEl = uifactory.addDropdownSingleselect("encoContent", "encoding.content", formLayout, encodingContentKeys, encodingContentValues, null);
		if (Arrays.asList(encodingContentKeys).contains(encodingContent)) {
			encodingContentEl.select(encodingContent, true);
		} else {
			encodingContentEl.select(NodeEditController.CONFIG_CONTENT_ENCODING_AUTO, true);
		}
		
		encodingJSEl = uifactory.addDropdownSingleselect("encoJS", "encoding.js", formLayout, encodingJSKeys, encodingJSValues, null);
		if (Arrays.asList(encodingJSKeys).contains(encodingJS)) {
			encodingJSEl.select(encodingJS, true);
		} else {
			encodingJSEl.select(NodeEditController.CONFIG_JS_ENCODING_AUTO, true);
		}
		
		isAssessableEl = uifactory.addCheckboxesVertical("isassessable", "assessable.label", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		isAssessableEl.select("xx", isAssessable);
		
		cutValueEl = uifactory.addIntegerElement("cutvalue", "cutvalue.label", 0, formLayout);
		cutValueEl.setIntValue(cutValue);
		cutValueEl.setDisplaySize(3);
		uifactory.addFormSubmitButton("save", formLayout);
	}


	@Override
	protected void doDispose() {
		//
	}
}