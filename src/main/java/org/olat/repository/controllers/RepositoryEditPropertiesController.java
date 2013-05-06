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
* <p>
*/
package org.olat.repository.controllers;

import java.util.List;

import org.olat.basesecurity.SecurityGroup;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.ui.events.KalendarModifiedEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.assessment.EfficiencyStatementManager;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.course.config.ui.CourseCalendarConfigController;
import org.olat.course.config.ui.CourseChatSettingController;
import org.olat.course.config.ui.CourseConfigGlossaryController;
import org.olat.course.config.ui.CourseEfficencyStatementController;
import org.olat.course.config.ui.CourseSharedFolderController;
import org.olat.course.config.ui.courselayout.CourseLayoutGeneratorController;
import org.olat.course.run.RunMainController;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.fileresource.types.GlossaryResource;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.modules.glossary.GlossaryEditSettingsController;
import org.olat.modules.glossary.GlossaryManager;
import org.olat.modules.glossary.GlossaryRegisterSettingsController;
import org.olat.repository.PropPupForm;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;
import org.olat.resource.references.ReferenceImpl;
import org.olat.resource.references.ReferenceManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * If the resource is a course it tries to aquire the lock for editing the properties of this course.
 * 
 * @author Ingmar Kroll
 */
public class RepositoryEditPropertiesController extends BasicController implements Activateable2 {
	
	private static final String ACTION_FORWARD ="forw";
	private static final String ACTION_BACKWARD ="bckw";
	
	public static final Event BACKWARD_EVENT = new Event("backward");
	public static final Event FORWARD_EVENT = new Event("forward");
	
	private VelocityContainer bgVC;
	private VelocityContainer editproptabpubVC;
	private PropPupForm propPupForm;
	private CourseChatSettingController ccc;
	private CourseSharedFolderController csfC;
	private CourseLayoutGeneratorController clayoutC;
	private CourseEfficencyStatementController ceffC;
	private CourseCalendarConfigController calCfgCtr;
	private CourseConfigGlossaryController cglosCtr;
	//fxdiff VCRP-1,2: access control of resources
	private AccessConfigurationController acCtr;
	private TabbedPane tabbedPane;
	private RepositoryEntry repositoryEntry;
	
  private LockResult courseLockEntry;
  
  private CourseConfig initialCourseConfig; //deep clone of the courseConfig
  private CourseConfig changedCourseConfig; //deep clone of the courseConfig
  private DialogBoxController yesNoCommitConfigsCtr;
  private boolean repositoryEntryChanged; //false per default
	private boolean courseConfigChanged;
	
	private int efficiencyConfigPos;
	
	private OLog log = Tracing.createLoggerFor(this.getClass());
	private final static String RELEASE_LOCK_AT_CATCH_EXCEPTION = "Must release course lock since an exception occured in " + RepositoryEditPropertiesController.class;
	
	/**
	 * Create a repository add controller that adds the given resourceable.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param sourceEntry
	 */
	public RepositoryEditPropertiesController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean usedInWizard) {
		super(ureq, wControl);
		//sets velocity root and translator to RepositoryManager package 
		setBasePackage(RepositoryManager.class);
		
		addLoggingResourceable(LoggingResourceable.wrap(entry, OlatResourceableType.genRepoEntry));
		
		this.repositoryEntry = entry;
				
		SecurityGroup secGroup = repositoryEntry.getOwnerGroup();
		// needed b/c of lazy initialized set
		//DBFactory.getInstance().reputInHibernateSessionCache(secGroup);
		//o_clusterREVIEW
		secGroup = (SecurityGroup) DBFactory.getInstance().loadObject(secGroup);
		
		bgVC = createVelocityContainer("bgrep");
		bgVC.contextPut("title", entry.getDisplayname());
		tabbedPane = new TabbedPane("descTB", ureq.getLocale());
		
		editproptabpubVC = createVelocityContainer("editproptabpub");
		tabbedPane.addTab(translate("tab.public"), editproptabpubVC);
		propPupForm = new PropPupForm(ureq, wControl, entry);
		listenTo(propPupForm);
		editproptabpubVC.put("proppupform", propPupForm.getInitialComponent());
		
		//fxdiff VCRP-1,2: access control of resources
	  acCtr = new AccessConfigurationController(ureq, getWindowControl(), repositoryEntry.getOlatResource(), repositoryEntry.getDisplayname(), true);
	  int access = propPupForm.getAccess();
	  if(access == RepositoryEntry.ACC_USERS || access == RepositoryEntry.ACC_USERS_GUESTS) {
	  	editproptabpubVC.put("accesscontrol", acCtr.getInitialComponent());
	  	editproptabpubVC.contextPut("isGuestAccess", Boolean.valueOf(access == RepositoryEntry.ACC_USERS_GUESTS));
	  }	  
		
		tabbedPane.addListener(this);
		try {
		if (repositoryEntry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
			// FIXME: This is duplicate code!!!! See CourseConfigMainController.
			// it is a course
			ICourse course = CourseFactory.loadCourse(repositoryEntry.getOlatResource());
			this.changedCourseConfig = course.getCourseEnvironment().getCourseConfig().clone();
			this.initialCourseConfig = course.getCourseEnvironment().getCourseConfig().clone();
						
			boolean isAlreadyLocked = CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(repositoryEntry.getOlatResource(), CourseFactory.COURSE_EDITOR_LOCK);
			 //try to acquire edit lock for this course and show dialog box on failure..
			courseLockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(repositoryEntry.getOlatResource(), ureq.getIdentity(), CourseFactory.COURSE_EDITOR_LOCK);
			if (!courseLockEntry.isSuccess()){				
				this.showWarning("error.course.alreadylocked", courseLockEntry.getOwner().getName());
				//beware: the controller is not properly initialized - the initial component is null
				return;
			} else if(courseLockEntry.isSuccess() && isAlreadyLocked) {
				this.showWarning("warning.course.alreadylocked.bySameUser");
				//beware: the controller is not properly initialized - the initial component is null
				courseLockEntry = null; //invalid lock
				return;
			} else {			
			  // editproptabinfVC.put(CourseFactory.getDetailsComponent(repositoryEntry.getOlatResource(),ureq));
			  // enable course chat settings, if instant messenger module is available
			  // and course chat is enabled.
				InstantMessagingModule imModule = CoreSpringFactory.getImpl(InstantMessagingModule.class);
			  if (imModule.isEnabled() && imModule.isCourseEnabled() && CourseModule.isCourseChatEnabled()) {
				  ccc = new CourseChatSettingController(ureq, getWindowControl(), changedCourseConfig);
				  listenTo(ccc);
				  // push on controller stack and register <this> as controllerlistener
				  tabbedPane.addTab(translate("tab.chat"), ccc.getInitialComponent());
			  }
			  clayoutC = new CourseLayoutGeneratorController(ureq, getWindowControl(), changedCourseConfig, course.getCourseEnvironment());
			  listenTo(clayoutC);
			  tabbedPane.addTab(translate("tab.layout"), clayoutC.getInitialComponent());

			  csfC = new CourseSharedFolderController(ureq, getWindowControl(), changedCourseConfig);
			  this.listenTo(csfC);
			  tabbedPane.addTab(translate("tab.sharedfolder"), csfC.getInitialComponent());

			  ceffC = new CourseEfficencyStatementController(ureq, getWindowControl(), changedCourseConfig);
			  this.listenTo(ceffC);
			  efficiencyConfigPos = tabbedPane.addTab(translate("tab.efficencystatement"), ceffC.getInitialComponent());
			
			  calCfgCtr = new CourseCalendarConfigController(ureq, getWindowControl(), changedCourseConfig);
			  this.listenTo(calCfgCtr);
			  tabbedPane.addTab(translate("tab.calendar"), calCfgCtr.getInitialComponent());

			  cglosCtr = new CourseConfigGlossaryController(ureq, getWindowControl(), changedCourseConfig, course.getResourceableId());
			  this.listenTo(cglosCtr);
			  tabbedPane.addTab(translate("tab.glossary"), cglosCtr.getInitialComponent());		
			}     
		} else if (repositoryEntry.getOlatResource().getResourceableTypeName().equals(GlossaryResource.TYPE_NAME)){
			GlossaryRegisterSettingsController glossRegisterSetCtr = new GlossaryRegisterSettingsController(ureq, getWindowControl(), repositoryEntry.getOlatResource());
			tabbedPane.addTab(translate("tab.glossary.register"), glossRegisterSetCtr.getInitialComponent());
			
			GlossaryEditSettingsController glossEditCtr = new GlossaryEditSettingsController(ureq, getWindowControl(), repositoryEntry.getOlatResource());
			tabbedPane.addTab(translate("tab.glossary.edit"), glossEditCtr.getInitialComponent());
		
		} else if (ImsCPFileResource.TYPE_NAME.equals(repositoryEntry.getOlatResource().getResourceableTypeName())) {
			QuotaManager qm = QuotaManager.getInstance();
			if (qm.hasQuotaEditRights(ureq.getIdentity())) {
				OlatRootFolderImpl cpRoot = FileResourceManager.getInstance().unzipContainerResource(repositoryEntry.getOlatResource());
				Controller quotaCtrl = qm.getQuotaEditorInstance(ureq, wControl, cpRoot.getRelPath(), false);
				tabbedPane.addTab(translate("tab.quota.edit"), quotaCtrl.getInitialComponent());
			}
		} else if (BlogFileResource.TYPE_NAME.equals(repositoryEntry.getOlatResource().getResourceableTypeName())
				|| PodcastFileResource.TYPE_NAME.equals(repositoryEntry.getOlatResource().getResourceableTypeName())) {
			QuotaManager qm = QuotaManager.getInstance();
			if (qm.hasQuotaEditRights(ureq.getIdentity())) {
				OlatRootFolderImpl feedRoot = FileResourceManager.getInstance().getFileResourceRootImpl(repositoryEntry.getOlatResource());
				Controller quotaCtrl = qm.getQuotaEditorInstance(ureq, wControl, feedRoot.getRelPath(), false);
				tabbedPane.addTab(translate("tab.quota.edit"), quotaCtrl.getInitialComponent());
			}
		}

		bgVC.put("descTB", tabbedPane);
		bgVC.contextPut("wizardfinish",new Boolean(usedInWizard));
		
		putInitialPanel(bgVC);
		} catch (RuntimeException e) {
			log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION);			
			this.dispose();
			throw e;
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if("tabs".equals(type)) {
			tabbedPane.activate(ureq, entries, state);
		} else if ("EfficiencyStatementConfig".equals(type)) {
			tabbedPane.setSelectedPane(efficiencyConfigPos);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
	  try {
		if(source==this.bgVC){
			if(event.getCommand().equals(ACTION_BACKWARD)){
				fireEvent(ureq,BACKWARD_EVENT);
			}else if(event.getCommand().equals(ACTION_FORWARD)){
				fireEvent(ureq, FORWARD_EVENT);
			}			
		}
	  } catch (RuntimeException e) {
			log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION);			
			this.dispose();
			throw e;
		}
	}
		

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
	  try {
		if (source == ccc
				|| source == clayoutC
				|| source == csfC
				|| source == ceffC
				|| source == calCfgCtr
				|| source == cglosCtr) { 
			
			if(!initialCourseConfig.equals(changedCourseConfig)) {
				courseConfigChanged = true;	
			}
		} else if(source == yesNoCommitConfigsCtr ) {			
			if(repositoryEntryChanged) {
				if (DialogBoxUIFactory.isYesEvent(event)) {
					RepositoryManager.getInstance().setProperties(repositoryEntry, propPupForm.canCopy(), propPupForm.canReference(), propPupForm.canLaunch(), propPupForm.canDownload() );		
					//fxdiff VCRP-1,2: access control of resources
					RepositoryManager.getInstance().setAccess(repositoryEntry, propPupForm.getAccess(), propPupForm.isMembersOnly());
					repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(repositoryEntry.getKey());
					repositoryEntryChanged = false;
					
					MultiUserEvent modifiedEvent = new EntryChangedEvent(repositoryEntry, EntryChangedEvent.MODIFIED);
					CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, repositoryEntry);
					
					// do logging
					ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES, getClass());
					
					fireEvent(ureq, new Event("courseChanged"));
				} else {			
					// yesNoCommitConfigsCtr => NO => do not change repository, reset changed flag
					repositoryEntryChanged = false;
				}
			}
			if(courseConfigChanged && !initialCourseConfig.equals(changedCourseConfig) && DialogBoxUIFactory.isYesEvent(event)) {				
				//ICourse course = CourseFactory.loadCourse(repositoryEntry.getOlatResource());
				ICourse course = CourseFactory.openCourseEditSession(repositoryEntry.getOlatResource().getResourceableId());
				//change course config	
				CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
				courseConfig.setCalendarEnabled(changedCourseConfig.isCalendarEnabled());
				courseConfig.setChatIsEnabled(changedCourseConfig.isChatEnabled());
				courseConfig.setCssLayoutRef(changedCourseConfig.getCssLayoutRef());
				courseConfig.setEfficencyStatementIsEnabled(changedCourseConfig.isEfficencyStatementEnabled());
				courseConfig.setGlossarySoftKey(changedCourseConfig.getGlossarySoftKey());
				courseConfig.setSharedFolderSoftkey(changedCourseConfig.getSharedFolderSoftkey());
				CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
				CourseFactory.closeCourseEditSession(course.getResourceableId(),true);
				
				//CourseChatSettingController 
				if(ccc!=null) {
				  if(changedCourseConfig.isChatEnabled() != initialCourseConfig.isChatEnabled()) {
            		//log instant messaging enabled disabled settings
				  	if (changedCourseConfig.isChatEnabled()) {
				  		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_IM_ENABLED, getClass());
				  	} else {
				  		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_IM_DISABLED, getClass());
				  	}
				  }
				}
				
        		// CourseLayoutController 
//				if(!changedCourseConfig.getCssLayoutRef().equals(initialCourseConfig.getCssLayoutRef()) && clayoutC.getLoggingAction()!=null) {					
//					// log removing custom course layout
//					ThreadLocalUserActivityLogger.log(clayoutC.getLoggingAction(), getClass());					
//				}
        		// CourseSharedFolderController 
				if(!changedCourseConfig.getSharedFolderSoftkey().equals(initialCourseConfig.getSharedFolderSoftkey()) && csfC.getLoggingAction()!=null) {
					String logDetail = csfC.getSharedFolderRepositoryEntry()!=null ? csfC.getSharedFolderRepositoryEntry().getDisplayname() : null; 
					ThreadLocalUserActivityLogger.log(csfC.getLoggingAction(), getClass(), LoggingResourceable.wrapBCFile(logDetail));
					if(!changedCourseConfig.getSharedFolderSoftkey().equals(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY)) {
						CourseSharedFolderController.updateRefTo(csfC.getSharedFolderRepositoryEntry(), course);
					} else {
						CourseSharedFolderController.deleteRefTo(course);
					}
				}
        // CourseEfficencyStatementController 
				if((changedCourseConfig.isEfficencyStatementEnabled() != initialCourseConfig.isEfficencyStatementEnabled() && ceffC.getLoggingAction()!=null)) {
					if(changedCourseConfig.isEfficencyStatementEnabled()) {
            // first create the efficiencies, send event to agency (all courses add link)
						List<Identity> identitiesWithData = course.getCourseEnvironment().getCoursePropertyManager().getAllIdentitiesWithCourseAssessmentData(null);
						EfficiencyStatementManager.getInstance().updateEfficiencyStatements(course, identitiesWithData);							
					} else {
            // delete really the efficiencies of the users.
						RepositoryEntry courseRepoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
						EfficiencyStatementManager.getInstance().deleteEfficiencyStatementsFromCourse(courseRepoEntry.getKey());						
					}
          //inform everybody else		
					EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
					CourseConfigEvent courseConfigEvent = new CourseConfigEvent(CourseConfigEvent.EFFICIENCY_STATEMENT_TYPE, course.getResourceableId());
					eventBus.fireEventToListenersOf(courseConfigEvent, course);
					ThreadLocalUserActivityLogger.log(ceffC.getLoggingAction(), getClass());
				}
				// CourseCalendarConfigController 
				if(changedCourseConfig.isCalendarEnabled()!= initialCourseConfig.isCalendarEnabled() && calCfgCtr.getLoggingAction()!=null) {
					ThreadLocalUserActivityLogger.log(calCfgCtr.getLoggingAction(), getClass());
          // notify calendar components to refresh their calendars
					CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new KalendarModifiedEvent(), OresHelper.lookupType(CalendarManager.class));
				}
				// CourseConfigGlossaryController 
				if((changedCourseConfig.getGlossarySoftKey()==null && initialCourseConfig.getGlossarySoftKey()!=null)
						|| (changedCourseConfig.getGlossarySoftKey()!=null && initialCourseConfig.getGlossarySoftKey()==null)
						&& cglosCtr.getLoggingAction()!=null) {
					
					final String glossarySoftKey = changedCourseConfig.getGlossarySoftKey();
					LoggingResourceable lri = null;
					if (glossarySoftKey!=null) {
						lri = LoggingResourceable.wrapNonOlatResource(StringResourceableType.glossarySoftKey, glossarySoftKey, glossarySoftKey);
					} else {
						final String deleteGlossarySoftKey = initialCourseConfig.getGlossarySoftKey();
						if (deleteGlossarySoftKey != null) {
							lri = LoggingResourceable.wrapNonOlatResource(StringResourceableType.glossarySoftKey, deleteGlossarySoftKey, deleteGlossarySoftKey);
						}
					}
					if (lri != null) ThreadLocalUserActivityLogger.log(cglosCtr.getLoggingAction(), getClass(), lri);
					if(changedCourseConfig.getGlossarySoftKey()==null) {
            // update references
					  ReferenceManager refM = ReferenceManager.getInstance();
						List<ReferenceImpl> repoRefs = refM.getReferences(course);
						for (ReferenceImpl ref:repoRefs) {
							if (ref.getUserdata().equals(GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER)) {
								refM.delete(ref);
								continue;
							}
						}
					} else if(changedCourseConfig.getGlossarySoftKey()!=null) {
            // update references
						RepositoryManager rm = RepositoryManager.getInstance();
						RepositoryEntry repoEntry = rm.lookupRepositoryEntryBySoftkey(changedCourseConfig.getGlossarySoftKey(), false);
						ReferenceManager.getInstance().addReference(course, repoEntry.getOlatResource(), GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER); 
					}
				}
				//course config transaction fihished
				initialCourseConfig = course.getCourseEnvironment().getCourseConfig().clone();				
				
        //fire CourseConfigEvent for this course channel
				EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
				CourseConfigEvent courseConfigEvent = new CourseConfigEvent(CourseConfigEvent.CALENDAR_TYPE, course.getResourceableId());
				eventBus.fireEventToListenersOf(courseConfigEvent, course);
				
				fireEvent(ureq, Event.DONE_EVENT);
			} else if(!DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {				
				fireEvent(ureq, Event.DONE_EVENT);
			} 
		} else if (source == this.propPupForm) { // process details form events
			if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} else if (event == Event.DONE_EVENT) {
				repositoryEntryChanged = true;				
				// inform user about inconsistent configuration: doesn't make sense to set a repositoryEntry canReference=true if it is only accessible to owners
				if (!repositoryEntry.getCanReference() && propPupForm.canReference() && (propPupForm.getAccess() < RepositoryEntry.ACC_OWNERS_AUTHORS && !propPupForm.isMembersOnly())) {					
					showError("warn.config.reference.no.access");
				}	
				//if not a course, update the repositoryEntry NOW!
				if(!repositoryEntry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
					RepositoryManager.getInstance().setProperties(repositoryEntry, propPupForm.canCopy(), propPupForm.canReference(), propPupForm.canLaunch(), propPupForm.canDownload() );		
					//fxdiff VCRP-1,2: access control of resources
					RepositoryManager.getInstance().setAccess(repositoryEntry, propPupForm.getAccess(), propPupForm.isMembersOnly());		
					// inform anybody interrested about this change
					MultiUserEvent modifiedEvent = new EntryChangedEvent(repositoryEntry, EntryChangedEvent.MODIFIED);
					CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, repositoryEntry);			
					fireEvent(ureq, Event.CHANGED_EVENT);
				}
				
				int access = propPupForm.getAccess();
				if(access == RepositoryEntry.ACC_USERS || access == RepositoryEntry.ACC_USERS_GUESTS) {
			  	editproptabpubVC.put("accesscontrol", acCtr.getInitialComponent());
			  	editproptabpubVC.contextPut("isGuestAccess", Boolean.valueOf(access == RepositoryEntry.ACC_USERS_GUESTS));
				} else {
			  	editproptabpubVC.remove(acCtr.getInitialComponent());
				}
				
				return;
			}
		}
	  } catch (RuntimeException e) {
			log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION);			
			this.dispose();
			throw e;
		}
	}

	/**
	 * Releases the course lock, the child controlers are disposed on the superclass.
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		releaseCourseLock();		
	}
	
	/**
	 * Must always release the lock upon dispose!
	 * Releases course lock and closes the CourseEditSession, if any open.
	 *
	 */
	private void releaseCourseLock() {
		if(courseLockEntry!=null && courseLockEntry.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(courseLockEntry);
			//cleanup course edit session, in case some error occurred during property editing.
			CourseFactory.closeCourseEditSession(repositoryEntry.getOlatResource().getResourceableId(),false);
			courseLockEntry = null; //invalidate lock
		}
	}

	/**
	 * @return Returns the repositoryEntry.
	 */
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}
	
	/**
	 * 
	 * @param ureq
	 * @return Return false if nothing changed, else true and activateYesNoDialog for save confirmation.
	 */
	public boolean checkIfCourseConfigChanged(UserRequest ureq) {		
		if(repositoryEntry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName()) && (repositoryEntryChanged || courseConfigChanged)) {
			OLATResourceable courseRunOres = OresHelper.createOLATResourceableInstance(RunMainController.ORES_TYPE_COURSE_RUN, repositoryEntry.getOlatResource().getResourceableId());
			int cnt = CoordinatorManager.getInstance().getCoordinator().getEventBus().getListeningIdentityCntFor(courseRunOres) -1; // -1: Remove myself from list;
			if (cnt < 0 ) {
				cnt = 0; // do not show any negative value
			}
		  yesNoCommitConfigsCtr = this.activateYesNoDialog(ureq, translate("course.config.changed.title"), translate("course.config.changed.text", String.valueOf(cnt)), yesNoCommitConfigsCtr);
		  return true;
		}
		return false;
	}
}