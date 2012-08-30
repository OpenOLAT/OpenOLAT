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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.rules.RulesFactory;
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
import org.olat.core.util.StringHelper;
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
	public static final String CONFIG_SKIPLAUNCHPAGE = "skiplaunchpage";
	public static final String CONFIG_SHOWNAVBUTTONS = "shownavbuttons";
	public static final String CONFIG_ISASSESSABLE = "isassessable";
	public static final String CONFIG_CUTVALUE = "cutvalue";
	public static final String CONFIG_RAW_CONTENT = "rawcontent";
	public static final String CONFIG_HEIGHT = "height";	
	public final static String CONFIG_HEIGHT_AUTO = "auto";
	//fxdiff FXOLAT-116: SCORM improvements
	public final static String CONFIG_FULLWINDOW = "fullwindow";
	public final static String CONFIG_CLOSE_ON_FINISH = "CLOSEONFINISH";
	
	// <OLATCE-289>
	public static final String CONFIG_MAXATTEMPTS = "attempts";
	public static final String CONFIG_ADVANCESCORE = "advancescore";
	public static final String CONFIG_ATTEMPTSDEPENDONSCORE = "scoreattampts";
	// </OLATCE-289>

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
		boolean skipLaunchPage = config.getBooleanSafe(CONFIG_SKIPLAUNCHPAGE,false);
		
		// <OLATCE-289>
		boolean assessable = config.getBooleanSafe(CONFIG_ISASSESSABLE, true);
		boolean attemptsDependOnScore = config.getBooleanSafe(CONFIG_ATTEMPTSDEPENDONSCORE, true);
		int maxAttempts = config.getIntegerSafe(CONFIG_MAXATTEMPTS, 0);
		boolean advanceScore = config.getBooleanSafe(CONFIG_ADVANCESCORE, true);
		// </OLATCE-289>
		int cutvalue = config.getIntegerSafe(CONFIG_CUTVALUE, 0);
		boolean rawContent = config.getBooleanSafe(CONFIG_RAW_CONTENT, false);
		String height = (String) config.get(CONFIG_HEIGHT);
		String encContent = (String) config.get(NodeEditController.CONFIG_CONTENT_ENCODING);
		String encJS = (String) config.get(NodeEditController.CONFIG_JS_ENCODING);
		//fxdiff FXOLAT-116: SCORM improvements
		boolean fullWindow = config.getBooleanSafe(CONFIG_FULLWINDOW, true);
		boolean closeOnFinish = config.getBooleanSafe(CONFIG_CLOSE_ON_FINISH, false);
		
		//= conf.get(CONFIG_CUTVALUE);
		scorevarform = new VarForm(ureq, wControl, showMenu, skipLaunchPage, showNavButtons,
				rawContent, height, encContent, encJS, assessable, cutvalue, fullWindow,
				closeOnFinish, maxAttempts, advanceScore, attemptsDependOnScore);
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
				boolean fullWindow = config.getBooleanSafe(CONFIG_FULLWINDOW, true);
				
				if (previewLayoutCtr != null) previewLayoutCtr.dispose();
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapScormRepositoryEntry(re));
				ScormAPIandDisplayController previewController = ScormMainManager.getInstance().createScormAPIandDisplayController(ureq, getWindowControl(), showMenu, null, cpRoot, null, course.getResourceableId().toString(),
						ScormConstants.SCORM_MODE_BROWSE, ScormConstants.SCORM_MODE_NOCREDIT, true, true, fullWindow);				
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
				previewController.activate();
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
				//save form-values to config
				
				config.setBooleanEntry(CONFIG_SHOWMENU, scorevarform.isShowMenu());
				//fxdiff FXOLAT-322
				config.setBooleanEntry(CONFIG_SKIPLAUNCHPAGE, scorevarform.isSkipLaunchPage());
				config.setBooleanEntry(CONFIG_SHOWNAVBUTTONS, scorevarform.isShowNavButtons());
				config.setBooleanEntry(CONFIG_ISASSESSABLE, scorevarform.isAssessable());
				config.setIntValue(CONFIG_CUTVALUE, scorevarform.getCutValue());
				//fxdiff FXOLAT-116: SCORM improvements
				config.setBooleanEntry(CONFIG_FULLWINDOW, scorevarform.isFullWindow());
				config.setBooleanEntry(CONFIG_CLOSE_ON_FINISH, scorevarform.isCloseOnFinish());
				// <OLATCE-289>
				config.setIntValue(CONFIG_MAXATTEMPTS, scorevarform.getAttemptsValue());
				config.setBooleanEntry(CONFIG_ADVANCESCORE, scorevarform.isAdvanceScore());
				config.setBooleanEntry(CONFIG_ATTEMPTSDEPENDONSCORE, scorevarform.getAttemptsDependOnScore());
				// </OLATCE-289>
				config.setBooleanEntry(CONFIG_RAW_CONTENT, scorevarform.isRawContent());
				config.set(CONFIG_HEIGHT, scorevarform.getHeightValue());
				config.set(NodeEditController.CONFIG_CONTENT_ENCODING, scorevarform.getEncodingContentValue());
				config.set(NodeEditController.CONFIG_JS_ENCODING, scorevarform.getEncodingJSValue());
				
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
	private SelectionElement fullWindowEl;//fxdiff FXOLAT-116: SCORM improvements
	private SelectionElement closeOnFinishEl;//fxdiff FXOLAT-116: SCORM improvements
	private SelectionElement isAssessableEl;
	private SelectionElement skipLaunchPageEl; //fxdiff FXOLAT-322 : skip start-page / auto-launch
	private SelectionElement rawContentEl;
	private IntegerElement cutValueEl;
	private SingleSelection heightEl;
	private SingleSelection encodingContentEl;
	private SingleSelection encodingJSEl;
	
	
	private boolean showMenu, showNavButtons, isAssessable, skipLaunchPage, rawContent;
	private String height;
	private String encodingContent;
	private String encodingJS;
	private int cutValue;
	private boolean fullWindow;//fxdiff FXOLAT-116: SCORM improvements
	private boolean closeOnFinish;//fxdiff FXOLAT-116: SCORM improvements
	private String[] keys, values;
	private String[] encodingContentKeys, encodingContentValues;
	private String[] encodingJSKeys, encodingJSValues;

	// <OLATCE-289>
	private SingleSelection attemptsEl;
	private MultipleSelectionElement advanceScoreEl;
	private MultipleSelectionElement scoreAttemptsEl;
	
	private boolean advanceScore;
	private boolean scoreAttempts;
	private int maxattempts;
	// </OLATCE-289>
	
	/**
	 * 
	 * @param name  Name of the form
	 */
	public VarForm(UserRequest ureq, WindowControl wControl, boolean showMenu, boolean skipLaunchPage, boolean showNavButtons, 
			boolean rawContent, String height, String encodingContent, String encodingJS, 
			boolean isAssessable, int cutValue, boolean fullWindow, boolean closeOnFinish,
			// <OLATCE-289>
			int maxattempts, boolean advanceScore, boolean attemptsDependOnScore
			// </OLATCE-289>
			) {
		super(ureq, wControl);
		this.showMenu = showMenu;
		this.skipLaunchPage = skipLaunchPage;
		this.showNavButtons = showNavButtons;
		this.isAssessable = isAssessable;
		this.cutValue = cutValue;
		//fxdiff FXOLAT-116: SCORM improvements
		this.fullWindow = fullWindow;
		this.closeOnFinish = closeOnFinish;
		this.rawContent = rawContent;
		this.height = height;
		this.encodingContent = encodingContent;
		this.encodingJS = encodingJS;
		
		// <OLATCE-289>
		this.advanceScore = advanceScore;
		this.scoreAttempts = attemptsDependOnScore;
		this.maxattempts = maxattempts;
		// </OLATCE-289>
		
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
	//fxdiff FXOLAT-116: SCORM improvements
	public boolean isFullWindow() {
		return fullWindowEl.isMultiselect() && fullWindowEl.isSelected(0);
	}
	//fxdiff FXOLAT-116: SCORM improvements
	public boolean isCloseOnFinish() {
		return closeOnFinishEl.isMultiselect() && closeOnFinishEl.isSelected(0);
	}

	public boolean isShowMenu() {
		return showMenuEl.isSelected(0);
	}
	
	public boolean isSkipLaunchPage() {
		return skipLaunchPageEl.isSelected(0);
	}

	public boolean isShowNavButtons() {
		return showNavButtonsEl.isSelected(0);
	}

	public boolean isAssessable() {
		return isAssessableEl.isSelected(0);
	}
	
	public boolean isRawContent() {
		return rawContentEl.isSelected(0);
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
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		heightEl.clearError();
		if(isRawContent()) {
			String height = getHeightValue();
			if(!StringHelper.containsNonWhitespace(height) || ScormEditController.CONFIG_HEIGHT_AUTO.equals(height)) {
				allOk &= false;
				heightEl.setErrorKey("rawcontent.height.error", null);
			}	
		}
		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("headerform");
		setFormContextHelp("org.olat.course.nodes.scorm","ced-scorm-settings.html","help.hover.scorm-settings-filename");
		
		showMenuEl = uifactory.addCheckboxesVertical("showmenu", "showmenu.label", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		showMenuEl.select("xx", showMenu);
		
		skipLaunchPageEl = uifactory.addCheckboxesVertical("skiplaunchpage", "skiplaunchpage.label", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		skipLaunchPageEl.select("xx", skipLaunchPage);
		
		showNavButtonsEl = uifactory.addCheckboxesVertical("shownavbuttons", "shownavbuttons.label", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		showNavButtonsEl.select("xx", showNavButtons);
		//fxdiff FXOLAT-116: SCORM improvements
		fullWindowEl = uifactory.addCheckboxesVertical("fullwindow", "fullwindow.label", formLayout, new String[]{"fullwindow"}, new String[]{null}, null, 1);
		fullWindowEl.select("fullwindow", fullWindow);
		
		closeOnFinishEl = uifactory.addCheckboxesVertical("closeonfinish", "closeonfinish.label", formLayout, new String[]{"closeonfinish"}, new String[]{null}, null, 1);
		closeOnFinishEl.select("closeonfinish", closeOnFinish);

		rawContentEl = uifactory.addCheckboxesVertical("rawcontent", "rawcontent.label", formLayout, new String[]{"rawcontent"}, new String[]{null}, null, 1);
		rawContentEl.select("rawcontent", rawContent);
		
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
		
		isAssessableEl = uifactory.addCheckboxesVertical("isassessable", "assessable.label", formLayout, new String[]{"ison"}, new String[]{null}, null, 1);
		isAssessableEl.select("ison", isAssessable);
		
		cutValueEl = uifactory.addIntegerElement("cutvalue", "cutvalue.label", 0, formLayout);
		cutValueEl.setIntValue(cutValue);
		cutValueEl.setDisplaySize(3);
		
		// <OLATCE-289>
		isAssessableEl.addActionListener(this, FormEvent.ONCHANGE);
		advanceScoreEl = uifactory.addCheckboxesVertical("advanceScore", "advance.score.label", formLayout, new String[]{"ison"}, new String[]{null}, null, 1);
		advanceScoreEl.select("ison", advanceScore);
		advanceScoreEl.addActionListener(this, FormEvent.ONCHANGE);
		
		RulesFactory.createShowRule(isAssessableEl, "ison", advanceScoreEl, formLayout);
		RulesFactory.createHideRule(isAssessableEl, null, advanceScoreEl, formLayout);
		
		scoreAttemptsEl = uifactory.addCheckboxesVertical("scoreAttempts", "attempts.depends.label", formLayout, new String[]{"ison"}, new String[]{null}, null, 1);
		scoreAttemptsEl.select("ison", scoreAttempts);
		scoreAttemptsEl.addActionListener(this, FormEvent.ONCHANGE);
		
		RulesFactory.createShowRule(advanceScoreEl, "ison",scoreAttemptsEl, formLayout);
		RulesFactory.createHideRule(advanceScoreEl, null, scoreAttemptsEl, formLayout);
		
		// <BPS-252> BPS-252_1
		int maxNumber = 21;
		String[] attemptsKeys = new String[maxNumber];
		attemptsKeys[0] = "0"; // position 0 means no restriction
		for (int i = 1; i < maxNumber; i++) {
            attemptsKeys[i] = (String.valueOf(i));
        }
		String[] attemptsValues = new String[maxNumber];
		attemptsValues[0] = translate("attempts.noLimit");
	    for (int i = 1; i < maxNumber; i++) {
	            attemptsValues[i] = (String.valueOf(i) + " x");
	        }
		if (maxattempts >= maxNumber) {
			maxattempts = 0;
		}

		attemptsEl = uifactory.addDropdownSingleselect("attempts.label", formLayout, attemptsKeys, attemptsValues, null);
		attemptsEl.select("" + maxattempts, true);
		// </OLATCE-289>
		
		uifactory.addFormSubmitButton("save", formLayout);
	}
	
	// <OLATCE-289>
	public int getAttemptsValue() {
		return Integer.valueOf(attemptsEl.getSelectedKey()); 
	}
	public boolean isAdvanceScore() {
		return advanceScoreEl.isSelected(0);
	}
	
	public boolean getAttemptsDependOnScore() {
		return scoreAttemptsEl.isSelected(0);
	}

	@Override
	protected void doDispose() {
		//
	}
}