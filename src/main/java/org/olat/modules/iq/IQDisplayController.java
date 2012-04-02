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

package org.olat.modules.iq;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.container.AssessmentContext;
import org.olat.ims.qti.container.ItemsInput;
import org.olat.ims.qti.container.SectionContext;
import org.olat.ims.qti.navigator.Navigator;
import org.olat.ims.qti.process.AssessmentFactory;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.FilePersister;
import org.olat.ims.qti.process.ImsRepositoryResolver;
import org.olat.ims.qti.process.Persister;
import org.olat.ims.qti.process.Resolver;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * @author Felix Jost
 */
public class IQDisplayController extends DefaultController implements Activateable2 {

	private static final String PACKAGE = Util.getPackageName(IQDisplayController.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(IQDisplayController.class);

	private static Logger log = Logger.getLogger(IQDisplayController.class.getName());

	private VelocityContainer myContent;

	private Translator translator;
	private String repositorySoftkey = null;
	private Resolver resolver = null;
	private Persister persister = null;

	private ProgressBar qtiscoreprogress, qtiquestionprogress;
	private IQComponent qticomp;
	private IQStatus qtistatus;
	private IQManager iqm;
	private IQSecurityCallback iqsec;
	private ModuleConfiguration modConfig;	
	// TODO: make nicer separation
	private long callingResId = 0;
	private String callingResDetail = "";
	private boolean ready;
	private Link closeButton;

	/**
	 * IMS QTI Display Controller used by the course nodes
	 * 
	 * concurrency protection is solved on IQManager.
	 * -> do not make constructor public
	 * -> create controller only via IQManager
	 * 
	 * @param moduleConfiguration
	 * @param secCallback
	 * @param ureq
	 * @param wControl
	 * @param callingResId
	 * @param callingResDetail
	 */
	IQDisplayController(ModuleConfiguration moduleConfiguration, IQSecurityCallback secCallback, UserRequest ureq,
			WindowControl wControl, long callingResId, String callingResDetail) {
		super(wControl);

		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass());

		this.modConfig = moduleConfiguration;
		this.callingResId = callingResId;
		this.callingResDetail = callingResDetail;
		this.repositorySoftkey = (String) moduleConfiguration.get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		
		init(secCallback, ureq);
	}

	/**
	 * IMS QTI Display Controller used by QTI Editor for preview.
	 * 
	 * concurrency protection is solved on IQManager.
	 * -> do not make constructor public
	 * -> create controller only via IQManager
	 * 
	 * @param resolver
	 * @param type
	 * @param secCallback
	 * @param ureq
	 * @param wControl
	 */
	IQDisplayController(Resolver resolver, String type, IQSecurityCallback secCallback, UserRequest ureq, WindowControl wControl) {
		super(wControl);

		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass());

		this.modConfig = new ModuleConfiguration();
		modConfig.set(IQEditController.CONFIG_KEY_ENABLEMENU, Boolean.TRUE);
		modConfig.set(IQEditController.CONFIG_KEY_TYPE, type);
		modConfig.set(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM);
		modConfig.set(IQEditController.CONFIG_KEY_SCOREPROGRESS, Boolean.TRUE);
		modConfig.set(IQEditController.CONFIG_KEY_QUESTIONPROGRESS, Boolean.FALSE);
		modConfig.set(IQEditController.CONFIG_KEY_ENABLECANCEL, Boolean.TRUE);
		modConfig.set(IQEditController.CONFIG_KEY_ENABLESUSPEND, Boolean.FALSE);
		modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED);
		modConfig.set(IQEditController.CONFIG_KEY_RENDERMENUOPTION, Boolean.FALSE);
		this.resolver = resolver;
		this.persister = null;
		init(secCallback, ureq);
	}

	private void init(IQSecurityCallback secCallback, UserRequest ureq) {
		this.iqsec = secCallback;
		this.translator = new PackageTranslator(PACKAGE, ureq.getLocale());
		this.ready = false;

		iqm = IQManager.getInstance();

		myContent = new VelocityContainer("olatmodiqrun", VELOCITY_ROOT + "/qti.html", translator, this);
		
		// Check if fibautocompl.js and fibautocompl.css exists for enhance FIB autocomplete feature
		 Resolver autcompResolver = null;
		if (resolver == null){
			RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftkey, true);
			 autcompResolver = new ImsRepositoryResolver(re.getKey());
		} else {
			autcompResolver = this.resolver;
		}
			
		if (autcompResolver != null && autcompResolver.hasAutocompleteFiles()) {
			// Add Autocomplte JS and CSS file to header
			StringBuilder sb = new StringBuilder();
			// must be like <script type="text/javascript" src="/olat/secstatic/qti/74579818809617/_unzipped_/fibautocompl.js"></script>
			sb.append("<script type=\"text/javascript\" src=\"").append(autcompResolver.getStaticsBaseURI()).append("/").append(ImsRepositoryResolver.QTI_FIB_AUTOCOMPLETE_JS_FILE).append("\"></script>\n");
			// must be like <link rel="StyleSheet" href="/olat/secstatic/qti/74579818809617/_unzipped_/fibautocompl.css" type="text/css" media="screen, print">
			sb.append("<link rel=\"StyleSheet\" href=\"").append(autcompResolver.getStaticsBaseURI()).append("/").append(ImsRepositoryResolver.QTI_FIB_AUTOCOMPLETE_CSS_FILE).append("\" type=\"text/css\" media=\"screen\" >\n");
			JSAndCSSComponent autoCompleteJsCss = new JSAndCSSComponent("auto_complete_js_css", this.getClass(), null, null, true, sb.toString());
			myContent.put("autoCompleteJsCss", autoCompleteJsCss);
		}
		closeButton = LinkFactory.createButton("close", myContent, this);
		
		qtiscoreprogress = new ProgressBar("qtiscoreprogress", 150, 0, 0, "");
		myContent.put("qtiscoreprogress", qtiscoreprogress);
		Boolean displayScoreProgress = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_SCOREPROGRESS);
		if (displayScoreProgress == null) displayScoreProgress = Boolean.TRUE; // migration,
																																						// display
																																						// menu
		if (!displayScoreProgress.booleanValue()) qtiscoreprogress.setVisible(false);
		myContent.contextPut("displayScoreProgress", displayScoreProgress);

		qtiquestionprogress = new ProgressBar("qtiquestionprogress", 150, 0, 0, "");
		myContent.put("qtiquestionprogress", qtiquestionprogress);
		Boolean displayQuestionProgress = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_QUESTIONPROGRESS);
		if (displayQuestionProgress == null) displayQuestionProgress = Boolean.FALSE; // migration,
																																									// don't
																																									// display
																																									// progress
		
		if (!displayQuestionProgress.booleanValue()) qtiquestionprogress.setVisible(false);
		myContent.contextPut("displayQuestionProgress", displayQuestionProgress);

		Boolean displayMenu = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_DISPLAYMENU);
		if (displayMenu == null) displayMenu = Boolean.TRUE; // migration
		myContent.contextPut("displayMenu", displayMenu);

		Boolean enableCancel = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_ENABLECANCEL);
		if (enableCancel == null) {
			if (modConfig.get(IQEditController.CONFIG_KEY_TYPE).equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) enableCancel = Boolean.FALSE; // migration:
																																																																					// disable
																																																																					// cancel
																																																																					// for
																																																																					// assessments
			else enableCancel = Boolean.TRUE; // migration: enable otherwise
		}
		myContent.contextPut("enableCancel", enableCancel);

		Boolean enableSuspend = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_ENABLESUSPEND);
		if (enableSuspend == null) enableSuspend = Boolean.FALSE; // migration
		myContent.contextPut("enableSuspend", enableSuspend);

		qtistatus = new IQStatus(translator);
		qtistatus.setPreview(iqsec.isPreview());
		myContent.contextPut("qtistatus", qtistatus);

		setInitialComponent(myContent);

		// get the assessment
		AssessmentInstance ai = null;
		//
		// IQManagers synchronizes display controller creation with qti editor, please see comment in qti editor
		//
		//synchronized (QTIEditorMainController.IS_SAVING) {
		//QTIEditorMainController.IS_SAVING_RWL.readLock().lock();
		//lock is now checked in the IQManager -> see there
		//try{
			if (repositorySoftkey != null) { // instantiate from repository
				// build path information which will be used to store tempory qti file
				String resourcePathInfo = this.callingResId + File.separator + this.callingResDetail; 
				ai = AssessmentFactory.createAssessmentInstance(ureq.getIdentity(), modConfig, iqsec.isPreview(),resourcePathInfo); 
			} else if (resolver != null) { // instantiate from given resolver
				ai = AssessmentFactory.createAssessmentInstance(resolver, persister, modConfig);
			}
		//}finally{
			//QTIEditorMainController.IS_SAVING_RWL.readLock().unlock();
		//}		
		//}
		// check for null instance or instance with no items
		if (ai == null || ai.getAssessmentContext().getSectionContext(0).getItemContextCount() == 0) throw new AssertException(
				"Assessment Instance was null or no sections/items found.");

		if (!iqsec.isAllowed(ai)) { // security check
			getWindowControl().setError(translator.translate("status.notallowed"));
			return;
		}

		if (iqsec.attemptsLeft(ai) < 1) { // security check
			// note: important: do not check on == 0 since the nr of attempts can be
			// republished for the same test with a smaller number as the latest time.
			getWindowControl().setInfo(translator.translate(ai.isSurvey() ? "status.survey.nomoreattempts" : "status.assess.nomoreattempts"));
			return;
		}

		if (ai.isResuming()) {
			getWindowControl().setInfo(translator.translate(ai.isSurvey() ? "status.survey.resumed" : "status.assess.resumed"));
		}

		ai.setPreview(iqsec.isPreview());

		/*
		 * menu render option: render only section titles or titles and questions.
		 */
		Object tmp = modConfig.get(IQEditController.CONFIG_KEY_RENDERMENUOPTION);
		Boolean renderSectionsOnly;
		if (tmp == null) {
			// migration
			modConfig.set(IQEditController.CONFIG_KEY_RENDERMENUOPTION, Boolean.FALSE);
			renderSectionsOnly = Boolean.FALSE;
		}else {
			renderSectionsOnly = (Boolean)tmp;
		}
		boolean enabledMenu = ((Boolean)modConfig.get(IQEditController.CONFIG_KEY_ENABLEMENU)).booleanValue();
		boolean itemPageSequence = ((String)modConfig.get(IQEditController.CONFIG_KEY_SEQUENCE)).equals(AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM);
		IQMenuDisplayConf mdc = new IQMenuDisplayConf(renderSectionsOnly.booleanValue(), enabledMenu, itemPageSequence);

		tmp = modConfig.get(IQEditController.CONFIG_KEY_MEMO);
		boolean memo = tmp == null ? false : ((Boolean)tmp).booleanValue();
		
		qticomp = new IQComponent("qticomponent", translator, ai, mdc, memo);
		
		qticomp.addListener(this);
		myContent.put("qticomp", qticomp);
		if (!ai.isResuming()) {
			Navigator navigator = ai.getNavigator();
			navigator.startAssessment();
		} else {
			//fxdiff BAKS-7 Resume function
			AssessmentContext act = ai.getAssessmentContext();
			if (act.getCurrentSectionContextPos() >= 0) {
				int sectionPos = act.getCurrentSectionContextPos();
				OLATResourceable sres = OresHelper.createOLATResourceableInstance("gse", new Long(sectionPos));
				WindowControl bwControl = addToHistory(ureq, sres, null, getWindowControl(), false);
				if(!ai.isSectionPage()) {
					SectionContext sct = act.getCurrentSectionContext();
					int itemPos = sct.getCurrentItemContextPos();
					if(itemPos >= 0) {
						OLATResourceable ires = OresHelper.createOLATResourceableInstance("git", new Long(itemPos));
						addToHistory(ureq, ires, null, bwControl, true);
					}
				}
			}
		}

		qtistatus.update(ai);
		if (!qtistatus.isSurvey()) {
			qtiscoreprogress.setMax(ai.getAssessmentContext().getMaxScore());
			qtiscoreprogress.setActual(ai.getAssessmentContext().getScore());
		}
		
		qtiquestionprogress.setMax(Integer.parseInt(qtistatus.getMaxQuestions()));
		updateQuestionProgressDisplay (ai);
		
		ready = true;
	}

	/**
	 * Wether the qti is ready to be launched.
	 * 
	 * @return boolean
	 */
	public boolean isReady() {
		return ready;
	}
	
	public boolean isClosed() {
		if(qticomp == null) return true;
		AssessmentInstance ai = qticomp.getAssessmentInstance();
		return ai.isClosed();
	}	

	private void updateQuestionProgressDisplay (AssessmentInstance ai) {
		
		int answered = ai.getAssessmentContext().getItemsAnsweredCount();
		qtiquestionprogress.setActual(answered);
		qtistatus.setQuestionProgressLabel(
					translator.translate(
								"question.progress.answered", new String[] {
										""+ai.getAssessmentContext().getItemsAnsweredCount(),
										qtistatus.getMaxQuestions()
								}
					)
		);
	}
	
	@Override
	//fxdiff BAKS-7 Resume function
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		
		if (source == myContent || source == qticomp) { // those must be links
			String wfCommand = event.getCommand();
			// process workflow
			AssessmentInstance ai = qticomp.getAssessmentInstance();
			if (qticomp == null || ai == null) { throw new RuntimeException("AssessmentInstance not valid."); }
			
			Navigator navig = ai.getNavigator();
			
			if (wfCommand.equals("mark")) {
				ai.mark(ureq.getParameter("id"), "true".equals(ureq.getParameter("p")));
				ai.persist();
				return;
			}
			
			if (wfCommand.equals("memo")) {
				ai.setMemo(ureq.getParameter("id"), ureq.getParameter("p"));
				ai.persist();
				return;	
			}

			logAudit(ureq);
			
			if (wfCommand.equals("sitse")) { // submitItemorSection
				ItemsInput iInp = iqm.getItemsInput(ureq);
				if(iInp.getItemCount() > 0) {
					navig.submitItems(iInp);
				}
				if (ai.isClosed()) { // do all the finishing stuff
					if(navig.getInfo().isFeedback()) {
						//render the feedback
					} else {
						event(ureq, source, new Event(QTIConstants.QTI_WF_SUBMIT));
						return;
					}
				}
			} else if (wfCommand.equals("sitsec")) { // submit
				if (ai.isClosed()) { // do all the finishing stuff
					if (!qtistatus.isSurvey()) {
						// for test and self-assessment, generate detailed results
						generateDetailsResults(ureq, ai);
					} else {
						// Send also finished event in case of survey
						fireEvent(ureq, new IQSubmittedEvent());
					}
					return;
				}
			} else if (wfCommand.equals("sflash")) { // submit flash answer
				navig.submitItems(iqm.getItemsInput(ureq)); //
				if (ai.isClosed()) { // do all the finishing stuff
					event(ureq, source, new Event(QTIConstants.QTI_WF_SUBMIT));
					return;
				}
			} else if (wfCommand.equals("git")) { // goToItem
				String seid = ureq.getParameter("seid");
				String itid = ureq.getParameter("itid");
				if (seid!=null && seid.length()!=0 && itid!=null && itid.length()!=0) {
					int sectionPos = Integer.parseInt(seid);
					int itemPos = Integer.parseInt(itid);
					navig.goToItem(sectionPos, itemPos);

					//fxdiff BAKS-7 Resume function
					OLATResourceable sres = OresHelper.createOLATResourceableInstance("gse", new Long(sectionPos));
					WindowControl bwControl = addToHistory(ureq, sres, null, getWindowControl(), false);
					OLATResourceable ires = OresHelper.createOLATResourceableInstance("git", new Long(itemPos));
					addToHistory(ureq, ires, null, bwControl, true);
				}
			} else if (wfCommand.equals("gse")) { // goToSection
				String seid = ureq.getParameter("seid");
				if (seid!=null && seid.length()!=0) {
					int sectionPos = Integer.parseInt(seid);
					navig.goToSection(sectionPos);

					//fxdiff BAKS-7 Resume function
					OLATResourceable sres = OresHelper.createOLATResourceableInstance("gse", new Long(sectionPos));
					addToHistory(ureq, sres, null);
				}
			} else if (wfCommand.equals(QTIConstants.QTI_WF_SUBMIT)) { // submit
																																	// Assessment
				navig.submitAssessment();
				postSubmitAssessment(ureq, ai);
			} else if (wfCommand.equals(QTIConstants.QTI_WF_CANCEL)) { // cancel
																																	// assessment
				navig.cancelAssessment();
			} else if (wfCommand.equals(QTIConstants.QTI_WF_SUSPEND)) { // suspend
																																	// assessment
				// just close the controller
				fireEvent(ureq, Event.DONE_EVENT);
				return;
			} else if (wfCommand.equals("close")) {
				qtistatus.update(null);
				// Parent controller need to pop, if they pushed previously
				fireEvent(ureq, Event.DONE_EVENT);
				return;
			}	
			qtistatus.update(ai);
			if (!qtistatus.isSurvey()) qtiscoreprogress.setActual(ai.getAssessmentContext().getScore());
			
			updateQuestionProgressDisplay (ai);
			
		} else if (source == closeButton){ // close component
			qtistatus.update(null);
			// Parent controller need to pop, if they pushed previously
			fireEvent(ureq, Event.DONE_EVENT);
			return;
		}
	}
	
	/**
	 * Persist data in all cases: test, selftest, surveys except previews
	 * In case of survey, data will be anonymized when reading from the
	 * table (using the archiver)
	 */
	protected void postSubmitAssessment(UserRequest ureq, AssessmentInstance ai) {
		if (!qtistatus.isPreview()) {
			iqm.persistResults(ai, callingResId, callingResDetail, ureq);
			getWindowControl().setInfo(translator.translate("status.results.saved"));
		} else {
			getWindowControl().setInfo(translator.translate("status.results.notsaved"));
		}

		if (!qtistatus.isSurvey()) {
			// for test and self-assessment, generate detailed results
			generateDetailsResults(ureq, ai);
		} else {
			// Send also finished event in case of survey
			fireEvent(ureq, new IQSubmittedEvent());
		}
	}
	
	protected void generateDetailsResults(UserRequest ureq, AssessmentInstance ai) {
		Document docResReporting = iqm.getResultsReporting(ai, ureq);
		if (!iqsec.isPreview()) {
			FilePersister.createResultsReporting(docResReporting, ureq.getIdentity(), ai.getFormattedType(), ai.getAssessID());
			// Send score and passed to parent controller. Maybe it is necessary
			// to save some data there
			// Do this now and not later, maybe user will never click on
			// 'close'...
			AssessmentContext ac = ai.getAssessmentContext();
			fireEvent(ureq, new IQSubmittedEvent(ac.getScore(), ac.isPassed(), ai.getAssessID()));
		}
		
		Boolean showResultsOnFinishObj = (Boolean)modConfig.get(IQEditController.CONFIG_KEY_RESULT_ON_FINISH);
		boolean showResultsOnFinish = showResultsOnFinishObj==null || showResultsOnFinishObj!=null && showResultsOnFinishObj.booleanValue();
		if (ai.getSummaryType() == AssessmentInstance.SUMMARY_NONE || !showResultsOnFinish) { 
			// do not display results reporting
			myContent.contextPut("displayreporting", Boolean.FALSE);
		} else { // display results reporting
			String resReporting = iqm.transformResultsReporting(docResReporting, ureq.getLocale(), ai.getSummaryType() );
			myContent.contextPut("resreporting", resReporting);
			myContent.contextPut("displayreporting", Boolean.TRUE);
		} 
		myContent.setPage(VELOCITY_ROOT + "/result.html");
		
		
	}

	/**
	 * @param ureq
	 */
	private void logAudit(UserRequest ureq) {
		Set params = ureq.getParameterSet();
		StringBuilder sb = new StringBuilder();
		for (Iterator iter = params.iterator(); iter.hasNext();) {
			String paramName = (String) iter.next();
			sb.append("|");
			sb.append(paramName);
			sb.append("=");
			sb.append(ureq.getParameter(paramName));
		}
		
		log.info("qti audit logging: hreq=" + ureq.getHttpReq().getRequestURL() + ", params=" + sb.toString());

		String command = ureq.getParameter("cid");
		
		String qtiDetails = LoggingResourceable.restrictStringLength("cid="+command+StringHelper.stripLineBreaks(sb.toString()), LoggingResourceable.MAX_NAME_LEN);
		ThreadLocalUserActivityLogger.log(QTILoggingAction.QTI_AUDIT, getClass(), 
				LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiParams, "", qtiDetails));
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}
}
