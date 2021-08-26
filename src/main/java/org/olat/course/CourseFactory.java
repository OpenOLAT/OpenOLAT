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

package org.olat.course;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.admin.quota.QuotaConstants;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarNotificationManager;
import org.olat.commons.calendar.manager.ImportToCalendarManager;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.ObjectCloner;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigManager;
import org.olat.course.config.ui.courselayout.CourseLayoutHelper;
import org.olat.course.editor.EditorMainController;
import org.olat.course.editor.PublishProcess;
import org.olat.course.editor.PublishSetInformations;
import org.olat.course.editor.StatusDescription;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.PersistingCourseGroupManager;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.properties.PersistingCoursePropertyManager;
import org.olat.course.run.RunMainController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.statistic.AsyncExportManager;
import org.olat.course.style.CourseStyleService;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.course.tree.PublishTreeModel;
import org.olat.group.BusinessGroup;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.manager.ChatLogHelper;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntrySecurityImpl;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.references.Reference;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.core.task.TaskRejectedException;


/**
 * Description: <BR>
 * Use the course factory to create course run and edit controllers or to load a
 * course from disk
 *
 * Initial Date: Oct 12, 2004
 * @author Felix Jost
 * @author guido
 */
public class CourseFactory {

	private static CacheWrapper<Long,PersistingCourseImpl> loadedCourses;
	private static final ConcurrentMap<Long, ModifyCourseEvent> modifyCourseEvents = new ConcurrentHashMap<>();

	public static final String COURSE_EDITOR_LOCK = "courseEditLock";
	/**
	 * This is the lock that must be acquired at course editing, copy course, export course, configure course. It must
	 * be very short life: acquire, do something, release.
	 */
	private static final Map<Long,PersistingCourseImpl> courseEditSessionMap = new ConcurrentHashMap<>();
	private static final Logger log = Tracing.createLoggerFor(CourseFactory.class);
	private static ReferenceManager referenceManager;


	/**
	 * [used by spring]
	 */
	private CourseFactory(CoordinatorManager coordinatorManager, ReferenceManager referenceManager) {
		loadedCourses = coordinatorManager.getCoordinator().getCacher().getCache(CourseFactory.class.getSimpleName(), "courses");
		CourseFactory.referenceManager = referenceManager;
	}

	/**
	 * Create an editor controller for the given course resourceable
	 *
	 * @param ureq
	 * @param wControl
	 * @param courseEntry
	 * @return editor controller for the given course resourceable; if the editor
	 *         is already locked, it returns a controller with a lock message
	 */
	public static EditorMainController createEditorController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel toolbar, RepositoryEntry courseEntry, CourseNode selectedNode) {
		ICourse course = loadCourse(courseEntry);
		EditorMainController emc = new EditorMainController(ureq, wControl, toolbar, course, selectedNode);
		if (emc.getLockEntry() == null) {
			Translator translator = Util.createPackageTranslator(RunMainController.class, ureq.getLocale());
			wControl.setWarning(translator.translate("error.editoralreadylocked", new String[] { "?" }));
			return null;
		} else if(!emc.getLockEntry().isSuccess()) {
			// get i18n from the course runmaincontroller to say that this editor is
			// already locked by another person

			Translator translator = Util.createPackageTranslator(RunMainController.class, ureq.getLocale());
			String lockerName = CoreSpringFactory.getImpl(UserManager.class).getUserDisplayName(emc.getLockEntry().getOwner());
			if(emc.getLockEntry().isDifferentWindows()) {
				wControl.setWarning(translator.translate("error.editoralreadylocked.same.user", new String[] { lockerName }));
			} else {
				wControl.setWarning(translator.translate("error.editoralreadylocked", new String[] { lockerName }));
			}
			return null;
		}
		//set the logger if editor is started
		//since 5.2 groups / areas can be created from the editor -> should be logged.
		emc.addLoggingResourceable(LoggingResourceable.wrap(course));
		return emc;
	}

	/**
	 * Creates an empty course with a single root node. The course is linked to
	 * the resourceable ores. The efficiency statment are enabled per default!
	 * @param shortTitle Short title of root node
	 * @param longTitle Long title of root node
	 * @param ores
	 *
	 * @return An empty course with a single root node.
	 */
	public static ICourse createCourse(RepositoryEntry courseEntry,
			String shortTitle, String longTitle) {
		OLATResource courseResource = courseEntry.getOlatResource();
		PersistingCourseImpl newCourse = new PersistingCourseImpl(courseResource);
		// Put new course in course cache
		loadedCourses.put(newCourse.getResourceableId(), newCourse);

		Structure initialStructure = new Structure();
		CourseNode runRootNode = new STCourseNode();
		runRootNode.setShortTitle(shortTitle);
		runRootNode.setLongTitle(longTitle);
		runRootNode.updateModuleConfigDefaults(true, null, NodeAccessType.of(newCourse));
		initialStructure.setRootNode(runRootNode);
		newCourse.setRunStructure(initialStructure);
		newCourse.saveRunStructure();

		CourseEditorTreeModel editorTreeModel = new CourseEditorTreeModel();
		CourseEditorTreeNode editorRootNode = new CourseEditorTreeNode((CourseNode) ObjectCloner.deepCopy(runRootNode));
		editorTreeModel.setRootNode(editorRootNode);
		newCourse.setEditorTreeModel(editorTreeModel);
		newCourse.saveEditorTreeModel();

		//enable efficiency statement per default
		CourseConfig courseConfig = newCourse.getCourseConfig();
		courseConfig.setEfficencyStatementIsEnabled(true);
		newCourse.setCourseConfig(courseConfig);

		return newCourse;
	}

	/**
	 * Set the type of the course.
	 * 
	 * @param addedEntry The course repository entry
	 * @param type The type of the course
	 * @param identity The user which do the initialization
	 * @return
	 */
	public static ICourse initNodeAccessType(RepositoryEntry addedEntry, NodeAccessType type) {
		OLATResourceable courseOres = addedEntry.getOlatResource();
		if (CourseFactory.isCourseEditSessionOpen(courseOres.getResourceableId())) {
			log.warn("Not able to set the course node access type: Edit session is already open!");
			return loadCourse(addedEntry);
		}
		
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
		String nodeAccessType = type.getType();
		courseConfig.setNodeAccessType(nodeAccessType);
		
		ModuleConfiguration runConfig = course.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration();
		CourseEditorTreeNode courseEditorTreeNode = (CourseEditorTreeNode)course.getEditorTreeModel().getRootNode();
		ModuleConfiguration editorConfig = courseEditorTreeNode.getCourseNode().getModuleConfiguration();
		
		NodeAccessService nodeAccessService = CoreSpringFactory.getImpl(NodeAccessService.class);
		boolean scoreCalculatorSupported = nodeAccessService.isScoreCalculatorSupported(type);
		runConfig.setBooleanEntry(STCourseNode.CONFIG_SCORE_CALCULATOR_SUPPORTED, scoreCalculatorSupported);
		editorConfig.setBooleanEntry(STCourseNode.CONFIG_SCORE_CALCULATOR_SUPPORTED, scoreCalculatorSupported);
		
		if (!scoreCalculatorSupported) {
			runConfig.setStringValue(STCourseNode.CONFIG_SCORE_KEY, STCourseNode.CONFIG_SCORE_VALUE_SUM);
			runConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
			editorConfig.setStringValue(STCourseNode.CONFIG_SCORE_KEY, STCourseNode.CONFIG_SCORE_VALUE_SUM);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		}
		
		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.saveCourse(addedEntry.getOlatResource().getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		return course;
	}


	/**
	 * Gets the course from cache if already there, or loads the course and puts it into cache.
	 * To be called for the "CourseRun" model.
	 * @param resourceableId
	 * @return the course with the given id (the type is always
	 *         CourseModule.class.toString())
	 */
	public static ICourse loadCourse(RepositoryEntry courseEntry) {
		if (courseEntry == null) {
			throw new AssertException("No resourceable ID found.");
		}
		Long resourceableId = courseEntry.getOlatResource().getResourceableId();
		PersistingCourseImpl course = loadedCourses.get(resourceableId);
		if (course == null) {
			// o_clusterOK by:ld - load and put in cache in doInSync block to ensure
			// that no invalidate cache event was missed
			PersistingCourseImpl theCourse = new PersistingCourseImpl(courseEntry);
			theCourse.load();

			PersistingCourseImpl cachedCourse = loadedCourses.putIfAbsent(resourceableId, theCourse);
			if(cachedCourse != null) {
				course = cachedCourse;
				course.updateCourseEntry(courseEntry);
			} else {
				course = theCourse;
			}
		} else {
			course.updateCourseEntry(courseEntry);
		}

		return course;
	}

	public static ICourse loadCourse(final Long resourceableId) {
		if (resourceableId == null) throw new AssertException("No resourceable ID found.");
		PersistingCourseImpl course = loadedCourses.get(resourceableId);
		if (course == null) {
			// o_clusterOK by:ld - load and put in cache in doInSync block to ensure
			// that no invalidate cache event was missed
			OLATResource resource = OLATResourceManager.getInstance().findResourceable(resourceableId, "CourseModule");
			PersistingCourseImpl theCourse = new PersistingCourseImpl(resource);
			theCourse.load();

			PersistingCourseImpl cachedCourse = loadedCourses.putIfAbsent(resourceableId, theCourse);
			if(cachedCourse != null) {
				course = cachedCourse;
			} else {
				course = theCourse;
			}
		}
		return course;
	}

	/**
	 * Load the course for the given course resourceable
	 *
	 * @param olatResource
	 * @return the course for the given course resourceable
	 */
	public static ICourse loadCourse(OLATResourceable olatResource) {
		Long resourceableId = olatResource.getResourceableId();
		return loadCourse(resourceableId);
	}

	/**
	 *
	 * @param resourceableId
	 */
	private static void removeFromCache(Long resourceableId) { //o_clusterOK by: ld
		loadedCourses.remove(resourceableId);
		log.debug("removeFromCache");
	}

	/**
	 * Puts the current course in the local cache and removes it from other caches (other cluster nodes).
	 * @param resourceableId
	 * @param course
	 */
	private static void updateCourseInCache(Long resourceableId, PersistingCourseImpl course) { //o_clusterOK by:ld
		loadedCourses.update(resourceableId, course);
		log.debug("updateCourseInCache");
	}

	/**
	 * Delete a course including its course folder and all references to resources
	 * this course holds.
	 *
	 * @param res
	 */
	public static void deleteCourse(RepositoryEntry entry, OLATResource res) {
		final long start = System.currentTimeMillis();
		log.info("deleteCourse: starting to delete course. res={}", res);

		PersistingCourseImpl course = null;
		try {
			course = (PersistingCourseImpl)loadCourse(res);
		} catch (CorruptedCourseException e) {
			log.error("Try to delete a corrupted course, I make want I can.");
		}

		// call cleanupOnDelete for nodes
		if(course != null) {
			Visitor visitor = new NodeDeletionVisitor(course);
			TreeVisitor tv = new TreeVisitor(visitor, course.getRunStructure().getRootNode(), true);
			tv.visitAll();
		}

		// delete assessment notifications
		OLATResourceable assessmentOres = OresHelper.createOLATResourceableInstance(CourseModule.ORES_COURSE_ASSESSMENT, res.getResourceableId());
		NotificationsManager notificationsManager = CoreSpringFactory.getImpl(NotificationsManager.class);
		notificationsManager.deletePublishersOf(assessmentOres);
		// delete all course notifications
		notificationsManager.deletePublishersOf(res);
		//delete calendar subscription
		clearCalenderSubscriptions(res, course);
		// delete course configuration (not really usefull, the config is in
		// the course folder which is deleted right after)
		if(course != null) {
			CoreSpringFactory.getImpl(CourseConfigManager.class).deleteConfigOf(course);
		}

		CoreSpringFactory.getImpl(TaskExecutorManager.class).delete(res);

		// delete course group- and rightmanagement
		CourseGroupManager courseGroupManager = PersistingCourseGroupManager.getInstance(res);
		courseGroupManager.deleteCourseGroupmanagement();
		// delete all remaining course properties
		CoursePropertyManager propertyManager = PersistingCoursePropertyManager.getInstance(res);
		propertyManager.deleteAllCourseProperties();
		// delete course calendar
		CoreSpringFactory.getImpl(ImportToCalendarManager.class).deleteCourseImportedCalendars(res);
		CoreSpringFactory.getImpl(CalendarManager.class).deleteCourseCalendar(res);
		
		// delete IM messages
		CoreSpringFactory.getImpl(InstantMessagingService.class).deleteMessages(res);
		//delete tasks
		CoreSpringFactory.getImpl(GTAManager.class).deleteAllTaskLists(entry);
		//delete the storage folder of info messages attachments
		CoreSpringFactory.getImpl(InfoMessageFrontendManager.class).deleteStorage(course);

		// cleanup cache
		removeFromCache(res.getResourceableId());

		// Everything is deleted, so we could get rid of course logging
		// with the change in user audit logging - which now all goes into a DB
		// we no longer do this though!

		// delete course directory
		VFSContainer fCourseBasePath = getCourseBaseContainer(res.getResourceableId());
		VFSStatus status = fCourseBasePath.deleteSilently();
		DBFactory.getInstance().commitAndCloseSession();
		boolean deletionSuccessful = (status == VFSConstants.YES || status == VFSConstants.SUCCESS);
		log.info("deleteCourse: finished deletion. res={}, deletion successful: {}, duration: {} ms.",
				res, deletionSuccessful, (System.currentTimeMillis()-start));
	}

	/**
	 * Checks all learning group calendars and the course calendar for publishers (of subscriptions)
	 * and sets their state to "1" which indicates that the ressource is deleted.
	 */
	private static void clearCalenderSubscriptions(OLATResourceable res, ICourse course) {
		//set Publisher state to 1 (= ressource is deleted) for all calendars of the course
		CalendarManager calMan = CoreSpringFactory.getImpl(CalendarManager.class);
		CalendarNotificationManager notificationManager = CoreSpringFactory.getImpl(CalendarNotificationManager.class);
		NotificationsManager nfm = CoreSpringFactory.getImpl(NotificationsManager.class);

		if(course != null) {
			CourseGroupManager courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();
			List<BusinessGroup> learningGroups = courseGroupManager.getAllBusinessGroups();
			//all learning and right group calendars
			for (BusinessGroup bg : learningGroups) {
				KalendarRenderWrapper calRenderWrapper = calMan.getGroupCalendar(bg);
				SubscriptionContext subsContext = notificationManager.getSubscriptionContext(calRenderWrapper);
				Publisher pub = nfm.getPublisher(subsContext);
				if (pub != null) {
					pub.setState(1); //int 0 is OK -> all other is not OK
				}
			}
		}
		//the course calendar
		try {
			KalendarRenderWrapper courseCalendar = calMan.getCalendarForDeletion(res);
			if(courseCalendar != null) {
				SubscriptionContext subContext = notificationManager.getSubscriptionContext(courseCalendar, res);
				OLATResourceable oresToDelete = OresHelper.createOLATResourceableInstance(subContext.getResName(), subContext.getResId());
				nfm.deletePublishersOf(oresToDelete);
			}
		} catch (AssertException e) {
			//if we have a broken course (e.g. canceled import or no repo entry somehow) skip calendar deletion...
		}
	}

	/**
	 * Copies a course. More specifically, the run and editor structures and the
	 * course folder will be copied to create a new course.
	 *
	 * @param sourceRes
	 * @param targetRes
	 * @param author 
	 * @return copy of the course.
	 */
	public static OLATResourceable copyCourse(OLATResourceable sourceRes, OLATResource targetRes, Identity author) {
		PersistingCourseImpl sourceCourse = (PersistingCourseImpl)loadCourse(sourceRes);
		PersistingCourseImpl targetCourse = new PersistingCourseImpl(targetRes);
		LocalFolderImpl fTargetCourseBaseContainer = targetCourse.getCourseBaseContainer();
		File fTargetCourseBasePath = fTargetCourseBaseContainer.getBasefile();

		//close connection before file copy
		DBFactory.getInstance().commitAndCloseSession();

		synchronized (sourceCourse) { // o_clusterNOK - cannot be solved with doInSync since could take too long (leads to error: "Lock wait timeout exceeded")
			// copy configuration
			CourseConfig courseConf = CoreSpringFactory.getImpl(CourseConfigManager.class).copyConfigOf(sourceCourse);
			courseConf.setBlogEnabled(false);
			courseConf.setBlogSoftKey(null);
			targetCourse.setCourseConfig(courseConf);
			// save structures
			targetCourse.setRunStructure((Structure) XStreamHelper.xstreamClone(sourceCourse.getRunStructure()));
			targetCourse.saveRunStructure();
			targetCourse.setEditorTreeModel((CourseEditorTreeModel) XStreamHelper.xstreamClone(sourceCourse.getEditorTreeModel()));
			targetCourse.saveEditorTreeModel();
			
			// copy course style folder
			VFSContainer sourceCourseStyleCont = VFSManager.olatRootContainer(
					sourceCourse.getCourseEnvironment().getCourseBaseContainer().getRelPath() + "/" + CourseStyleService.FOLDER_ROOT);
			if (sourceCourseStyleCont.exists()) {
				VFSContainer targetCourseStyleCont = VFSManager.olatRootContainer(
						targetCourse.getCourseEnvironment().getCourseBaseContainer().getRelPath() + "/" + CourseStyleService.FOLDER_ROOT);
				targetCourseStyleCont.copyContentOf(sourceCourseStyleCont, author);
			}

			// copy course folder
			VFSContainer sourceCourseContainer = sourceCourse.getIsolatedCourseBaseContainer();
			if (sourceCourseContainer.exists()) {
				VFSContainer targetCourseContainer = targetCourse.getIsolatedCourseBaseContainer();
				targetCourseContainer.copyContentOf(sourceCourseContainer, author);
			}

			// copy folder nodes directories
			VFSContainer sourceFoldernodesContainer = VFSManager
					.olatRootContainer(BCCourseNode.getFoldernodesPathRelToFolderBase(sourceCourse.getCourseEnvironment()));
			if (sourceFoldernodesContainer.exists()) {
				VFSContainer targetFoldernodesContainer = VFSManager
						.olatRootContainer(BCCourseNode.getFoldernodesPathRelToFolderBase(targetCourse.getCourseEnvironment()));
				targetFoldernodesContainer.copyContentOf(sourceFoldernodesContainer, author);
			}

			// copy task folder directories
			File fSourceTaskfoldernodesFolder = new File(FolderConfig.getCanonicalRoot()
					+ TACourseNode.getTaskFoldersPathRelToFolderRoot(sourceCourse.getCourseEnvironment()));
			if (fSourceTaskfoldernodesFolder.exists()) FileUtils.copyDirToDir(fSourceTaskfoldernodesFolder, fTargetCourseBasePath, false, "copy task folder directories");

			// update references
			List<Reference> refs = referenceManager.getReferences(sourceCourse);
			int count = 0;
			for (Reference ref: refs) {
				referenceManager.addReference(targetCourse, ref.getTarget(), ref.getUserdata());
				if(count++ % 20 == 0) {
					DBFactory.getInstance().intermediateCommit();
				}
			}

			// set quotas
			Quota sourceQuota = VFSManager.isTopLevelQuotaContainer(sourceCourse.getCourseFolderContainer());
			Quota targetQuota = VFSManager.isTopLevelQuotaContainer(targetCourse.getCourseFolderContainer());
			if (sourceQuota != null && targetQuota != null) {
				QuotaManager qm = CoreSpringFactory.getImpl(QuotaManager.class);
				if (sourceQuota.getQuotaKB() != qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_COURSE).getQuotaKB()) {
					targetQuota = qm.createQuota(targetQuota.getPath(), sourceQuota.getQuotaKB(), sourceQuota.getUlLimitKB());
					qm.setCustomQuotaKB(targetQuota);
				}
			}
		}
		return targetRes;
	}

	/**
	 * Import a course from a ZIP file.
	 *
	 * @param ores
	 * @param zipFile
	 * @return New Course.
	 */
	public static ICourse importCourseFromZip(OLATResource ores, File zipFile) {
		// Generate course with filesystem
		PersistingCourseImpl newCourse = new PersistingCourseImpl(ores);
		
		CourseConfigManager courseConfigMgr = CoreSpringFactory.getImpl(CourseConfigManager.class);
		courseConfigMgr.deleteConfigOf(newCourse);

		// Unzip course structure in new course
		LocalFolderImpl courseBaseContainer = newCourse.getCourseBaseContainer();
		File fCanonicalCourseBasePath = courseBaseContainer.getBasefile();
		if (ZipUtil.unzip(zipFile, fCanonicalCourseBasePath)) {
			// Load course structure now
			try {
				newCourse.load();
				CourseConfig cc = courseConfigMgr.loadConfigFor(newCourse);
				//newCourse is not in cache yet, so we cannot call setCourseConfig()
				newCourse.setCourseConfig(cc);
				loadedCourses.put(newCourse.getResourceableId(), newCourse);
				
				//course folder
				File courseFolderZip = new File(fCanonicalCourseBasePath, "oocoursefolder.zip");
				if(courseFolderZip.exists()) {
					VFSContainer courseFolder = VFSManager.getOrCreateContainer(courseBaseContainer, PersistingCourseImpl.COURSEFOLDER);
					ZipUtil.unzipNonStrict(courseFolderZip, courseFolder, null, false);
					FileUtils.deleteFile(courseFolderZip);
				}
				return newCourse;
			} catch (AssertException ae) {
				// ok failed, cleanup below
				// better logging to search error
				log.error("rollback importCourseFromZip",ae);
			}
		}
		// cleanup if not successfull
		FileUtils.deleteDirsAndFiles(fCanonicalCourseBasePath, true, true);
		return null;
	}

	/**
	 * Publish the course with some standard options
	 * @param course
	 * @param locale
	 * @param identity
	 */
	public static void publishCourse(ICourse course, RepositoryEntryStatusEnum accessStatus, boolean allUsers, boolean guests,
			Identity identity, Locale locale) {
		 CourseEditorTreeModel cetm = course.getEditorTreeModel();
		 PublishProcess publishProcess = PublishProcess.getInstance(course, cetm, locale);
		 PublishTreeModel publishTreeModel = publishProcess.getPublishTreeModel();
		 publishProcess.changeGeneralAccess(identity, accessStatus, allUsers, guests);

		 if (publishTreeModel.hasPublishableChanges()) {
			 List<String>nodeToPublish = new ArrayList<>();
			 visitPublishModel(publishTreeModel.getRootNode(), publishTreeModel, nodeToPublish);
			 
			 //only add selection if changes were possible
			 for(Iterator<String> selectionIt=nodeToPublish.iterator(); selectionIt.hasNext(); ) {
				String ident = selectionIt.next();
				TreeNode node = publishProcess.getPublishTreeModel().getNodeById(ident);
				if(!publishTreeModel.isSelectable(node)) {
					selectionIt.remove();
				}
			 }
			 try {
				 publishProcess.createPublishSetFor(nodeToPublish);

				 course = CourseFactory.openCourseEditSession(course.getResourceableId());
				 PublishSetInformations set = publishProcess.testPublishSet(locale);
				 CourseFactory.closeCourseEditSession(course.getResourceableId(), false);
				 
				 StatusDescription[] status = set.getWarnings();
				 //publish not possible when there are errors
				 for(int i = 0; i < status.length; i++) {
					 if(status[i].isError()) {
						 log.error("Status error by publish: {}", status[i].getLongDescription(locale));
						 return;
					 }
				 }

				 course = CourseFactory.openCourseEditSession(course.getResourceableId());
				 publishProcess.applyPublishSet(identity, locale, false);
			 } catch(Exception e) {
				 log.error("",  e);
			 } finally {
				 closeCourseEditSession(course.getResourceableId(), true);
			 }
		 }
	}

	/**
	 * Create a user locale dependent help-course run controller
	 *
	 * @param ureq The user request
	 * @param wControl The current window controller
	 * @return The help-course run controller
	 */
	public static Controller createHelpCourseLaunchController(UserRequest ureq, WindowControl wControl) {
		// Find repository entry for this course
		String helpCourseSoftKey = CoreSpringFactory.getImpl(HelpModule.class).getCourseSoftkey();
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryService rs = CoreSpringFactory.getImpl(RepositoryService.class);
		RepositoryEntry entry = null;
		if (StringHelper.containsNonWhitespace(helpCourseSoftKey)) {
			entry = rm.lookupRepositoryEntryBySoftkey(helpCourseSoftKey, false);
			if (entry == null) {
				try {
					entry = rm.lookupRepositoryEntry(Long.valueOf(helpCourseSoftKey), false);
				} catch (Exception e) {}
			}
		}
		if (entry == null) {
			Translator translator = Util.createPackageTranslator(CourseFactory.class, ureq.getLocale());
			wControl.setError(translator.translate("error.helpcourse.not.configured"));
			// create empty main controller
			return new LayoutMain3ColsController(ureq, wControl, null, null, null);
		} else {
			// Increment launch counter
			rs.incrementLaunchCounter(entry);
			ICourse course = loadCourse(entry);

			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(entry);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, wControl);
			RepositoryEntrySecurity reSecurity = new RepositoryEntrySecurityImpl(false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false);
			return new RunMainController(ureq, bwControl, null, course, entry, reSecurity, null);
		}
	}
	
	public static boolean isHelpCourseExisting(String helpCourseKey) {
		// Find repository entry for this course
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry entry = null;
		
		if (StringHelper.containsNonWhitespace(helpCourseKey)) {
			entry = rm.lookupRepositoryEntryBySoftkey(helpCourseKey, false);
			if (entry == null) {
				try {
					entry = rm.lookupRepositoryEntry(Long.valueOf(helpCourseKey), false);
				} catch (Exception e) {
					//
				}
			}
		}
		
		return entry != null;
	}

	/**
	 * visit all nodes in the specified course and make them archiving any data
	 * into the identity's export directory.
	 *
	 * @param res
	 * @param charset
	 * @param locale
	 * @param identity
	 */
	public static void archiveCourseToDelete(OLATResourceable res, String charset, Locale locale, Identity identity, Roles roles) {
		RepositoryEntry courseRe = RepositoryManager.getInstance().lookupRepositoryEntry(res, false);
		PersistingCourseImpl course = (PersistingCourseImpl) loadCourse(res);
		File exportDirectory = CourseFactory.getOrCreateDataExportDirectory(identity, course.getCourseTitle());
		
		boolean archiveCourseLog = CoreSpringFactory.getImpl(CourseModule.class)
				.isArchiveLogTableOnDelete();
		
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		boolean isAdministrator = roles.isAdministrator()
				&& repositoryService.hasRoleExpanded(identity, courseRe, OrganisationRoles.administrator.name());
		boolean isOresOwner = repositoryService.hasRole(identity, courseRe, GroupRoles.owner.name());
		boolean isOresInstitutionalManager = roles.isLearnResourceManager()
				&& repositoryService.hasRoleExpanded(identity, courseRe, OrganisationRoles.learnresourcemanager.name());
		archiveCourse(identity, course, charset, locale, exportDirectory, archiveCourseLog, isAdministrator, isOresOwner, isOresInstitutionalManager);
	}

	/**
	 * visit all nodes in the specified course and make them archiving any data
	 * into the identity's export directory.
	 *
	 * @param res
	 * @param charset
	 * @param locale
	 * @param identity
	 */
	private static void archiveCourse(Identity archiveOnBehalfOf, ICourse course, String charset, Locale locale, File exportDirectory,
			boolean archiveCourseLog, boolean isAdministrator, boolean... oresRights) {
		// archive course results overview
		List<Identity> users = ScoreAccountingHelper.loadUsers(course.getCourseEnvironment());
		List<CourseNode> nodes = ScoreAccountingHelper.loadAssessableNodes(course.getCourseEnvironment());
		
		String fileName = ExportUtil.createFileNameWithTimeStamp(course.getCourseTitle(), "zip");
		try(OutputStream out = new FileOutputStream(new File(exportDirectory, fileName));
				ZipOutputStream zout = new ZipOutputStream(out)) {
			ScoreAccountingHelper.createCourseResultsOverview(users, nodes, course, locale, zout);
		} catch(IOException e) {
			log.error("", e);
		}

		// archive all nodes content
		Visitor archiveV = new NodeArchiveVisitor(locale, course, exportDirectory, charset);
		TreeVisitor tv = new TreeVisitor(archiveV, course.getRunStructure().getRootNode(), true);
		tv.visitAll();
		
		// archive all course log files
		if(archiveCourseLog) {
			// administrator gets all log files independent of the visibility configuration
			boolean isOresOwner = (oresRights.length > 0)?oresRights[0]:false;
			boolean isOresInstitutionalManager = (oresRights.length > 1)?oresRights[1]:false;
	
			boolean aLogV = isOresOwner || isOresInstitutionalManager || isAdministrator;
			boolean uLogV = isAdministrator;
			boolean sLogV = isOresOwner || isOresInstitutionalManager || isAdministrator;
	
			// make an intermediate commit here to make sure long running course log export doesn't
			// cause db connection timeout to be triggered
			// rework when backgroundjob infrastructure exists
			DBFactory.getInstance().intermediateCommit();
			try {
				CoreSpringFactory.getImpl(AsyncExportManager.class).asyncArchiveCourseLogFiles(archiveOnBehalfOf,
						course.getResourceableId(), exportDirectory.getPath(), null, null, aLogV, uLogV, sLogV, null, null);
			} catch (TaskRejectedException e) {
				log.error("The course log cannot be archived.", e);
			}
		}

		course.getCourseEnvironment().getCourseGroupManager().archiveCourseGroups(exportDirectory);

		CoreSpringFactory.getImpl(ChatLogHelper.class).archive(course, exportDirectory);
		DBFactory.getInstance().commitAndCloseSession();
	}

	/**
	 * Returns the data export directory. If the directory does not yet exist the
	 * directory will be created
	 *
	 * @param ureq The user request
	 * @param courseName The course name or title. Will be used as directory name
	 * @return The file representing the dat export directory
	 */
	public static File getOrCreateDataExportDirectory(Identity identity, String courseName) {
		String courseFolder = StringHelper.transformDisplayNameToFileSystemName(courseName);
		// folder where exported user data should be put
		File exportFolder = new File(FolderConfig.getCanonicalRoot() + FolderConfig.getUserHome(identity) + "/private/archive/"
						+ courseFolder);
		if (exportFolder.exists()) {
			if (!exportFolder.isDirectory()) { throw new OLATRuntimeException(ExportUtil.class, "File " + exportFolder.getAbsolutePath()
					+ " already exists but it is not a folder!", null); }
		} else {
			exportFolder.mkdirs();
		}
		return exportFolder;
	}


	/**
	 * Returns the data export directory.
	 *
	 * @param ureq The user request
	 * @param courseName The course name or title. Will be used as directory name
	 * @return The file representing the dat export directory
	 */
	public static File getDataExportDirectory(Identity identity, String courseName) {
		File exportFolder = new File( // folder where exported user data should be
				// put
				FolderConfig.getCanonicalRoot() + FolderConfig.getUserHome(identity) + "/private/archive/"
						+ Formatter.makeStringFilesystemSave(courseName));
		return exportFolder;
	}

	/**
	 * Returns the personal folder of the given identity.
	 * <p>
	 * The idea of this method is to match the first part of what
	 * getOrCreateDataExportDirectory() returns.
	 * <p>
	 * @param identity
	 * @return
	 */
	public static File getPersonalDirectory(Identity identity) {
		if (identity==null) {
			return null;
		}
		return new File(FolderConfig.getCanonicalRoot() + FolderConfig.getUserHome(identity));
	}

	/**
	 * Returns the data export directory. If the directory does not yet exist the
	 * directory will be created
	 *
	 * @param ureq The user request
	 * @param courseName The course name or title. Will be used as directory name
	 * @return The file representing the dat export directory
	 */
	public static File getOrCreateStatisticDirectory(Identity identity, String courseName) {
		File exportFolder = new File( // folder where exported user data should be
				// put
				FolderConfig.getCanonicalRoot() + FolderConfig.getUserHome(identity) + "/private/statistics/"
						+ Formatter.makeStringFilesystemSave(courseName));
		if (exportFolder.exists()) {
			if (!exportFolder.isDirectory()) { throw new OLATRuntimeException(ExportUtil.class, "File " + exportFolder.getAbsolutePath()
					+ " already exists but it is not a folder!", null); }
		} else {
			exportFolder.mkdirs();
		}
		return exportFolder;
	}

	/**
	 * Stores the editor tree model AND the run structure (both xml files). Called at publish.
	 * @param resourceableId
	 */
	public static void saveCourse(final Long resourceableId) {
		if (resourceableId == null) throw new AssertException("No resourceable ID found.");

		PersistingCourseImpl theCourse = getCourseEditSession(resourceableId);
		if(theCourse != null) {
			//o_clusterOK by: ld (although the course is locked for editing, we still have to insure that load course is synchronized)
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(theCourse, () -> {
				final PersistingCourseImpl course = getCourseEditSession(resourceableId);
				if(course != null && course.isReadAndWrite()) {
					course.initHasAssessableNodes();
					course.saveRunStructure();
					course.saveEditorTreeModel();

					//clear modifyCourseEvents at publish, since the updateCourseInCache is called anyway
					modifyCourseEvents.remove(resourceableId);
					updateCourseInCache(resourceableId, course);
				} else if(!course.isReadAndWrite()) {
					throw new AssertException("Cannot saveCourse because theCourse is readOnly! You have to open an courseEditSession first!");
				}
			});
		} else {
			throw new AssertException("Cannot saveCourse because theCourse is null! Have you opened a courseEditSession yet?");
		}
	}

	/**
	 * Stores ONLY the editor tree model (e.g. at course tree editing - add/remove/move course nodes).
	 * @param resourceableId
	 */
	public static void saveCourseEditorTreeModel(Long resourceableId) {
		if (resourceableId == null) throw new AssertException("No resourceable ID found.");

		final PersistingCourseImpl course = getCourseEditSession(resourceableId);
		if(course.isReadAndWrite()) {
			synchronized(loadedCourses) { //o_clusterOK by: ld (clusterOK since the course is locked for editing)
				course.saveEditorTreeModel();
				modifyCourseEvents.putIfAbsent(resourceableId, new ModifyCourseEvent(resourceableId));
			}
		} else {
			throw new AssertException("Cannot saveCourse because theCourse is readOnly! You have to open an courseEditSession first!");
		}
	}

	/**
	 * Updates the course cache forcing other cluster nodes to reload this course. <br/>
	 * This is triggered after the course editor is closed. <br/>
	 * It also removes the courseEditSession for this course.
	 *
	 * @param resourceableId
	 */
	public static void fireModifyCourseEvent(Long resourceableId) {
		ModifyCourseEvent modifyCourseEvent = modifyCourseEvents.get(resourceableId);
		if(modifyCourseEvent!=null){
			synchronized(modifyCourseEvents) { //o_clusterOK by: ld
				modifyCourseEvent = modifyCourseEvents.remove(resourceableId);
				if(modifyCourseEvent != null) {
					PersistingCourseImpl course = getCourseEditSession(resourceableId);
					if(course!=null) {
						updateCourseInCache(resourceableId, course);
					}
				}
			}
		}
		//close courseEditSession if not already closed
		closeCourseEditSession(resourceableId, false);
	}

	/**
	 * Create a custom css object for the course layout. This can then be set on a
	 * MainLayoutController to activate the course layout
	 *
	 * @param usess The user session
	 * @param courseEnvironment the course environment
	 * @return The custom course css or NULL if no course css is available
	 */
	public static CustomCSS getCustomCourseCss(UserSession usess, CourseEnvironment courseEnvironment) {
		CustomCSS customCSS = null;
		CourseConfig courseConfig = courseEnvironment.getCourseConfig();
		if (courseConfig.hasCustomCourseCSS()) {
			// Notify the current tab that it should load a custom CSS
			return CourseLayoutHelper.getCustomCSS(usess, courseEnvironment);
		}
		return customCSS;
	}


	/**
	 * the provided resourceableID must belong to a ICourse.getResourceableId(), otherwise you
	 * risk to use a wrong course base container.
	 * @param resourceableId
	 * @return
	 */
	public static VFSContainer getCourseBaseContainer(Long resourceableId) {
		String relPath = "/course/" + resourceableId.longValue();
		LocalFolderImpl courseRootContainer = VFSManager.olatRootContainer(relPath, null);
		File fBasePath = courseRootContainer.getBasefile();
		if (!fBasePath.exists())
			throw new OLATRuntimeException(PersistingCourseImpl.class, "Could not resolve course base path:" + courseRootContainer, null);
		return courseRootContainer;
	}

	/**
	 * Save courseConfig and update cache.
	 * @param resourceableId
	 * @param cc
	 */
	public static void setCourseConfig(final Long resourceableId, final CourseConfig cc) {
		if (resourceableId == null) throw new AssertException("No resourceable ID found.");

		PersistingCourseImpl theCourse = getCourseEditSession(resourceableId);
		if(theCourse!=null) {
			//o_clusterOK by: ld (although the course is locked for editing, we still have to insure that load course is synchronized)
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(theCourse, new SyncerExecutor(){
				@Override
				public void execute() {
					PersistingCourseImpl course = getCourseEditSession(resourceableId);
					if(course!=null) {
						course.setCourseConfig(cc);
						updateCourseInCache(resourceableId, course);
					}
				}
			});
		} else {
			throw new AssertException("Cannot setCourseConfig because theCourse is null! Have you opened a courseEditSession yet?");
		}
	}

	/**
	 * Loads the course or gets it from cache, and adds it to the courseEditSessionMap. <br/>
	 * It guarantees that the returned value is never null. <br/>
	 * The courseEditSession object should live between acquire course lock and release course lock.
	 * 
	 * @param resourceableId The resource id
	 * @return The course
	 */
	public static PersistingCourseImpl openCourseEditSession(Long resourceableId) {
		PersistingCourseImpl course = courseEditSessionMap.get(resourceableId);
		if(course != null) {
			throw new AssertException("There is already an edit session open for this course: " + resourceableId);
		}
		
		course = (PersistingCourseImpl)loadCourse(resourceableId);
		course.setReadAndWrite(true);
		courseEditSessionMap.put(resourceableId, course);
		log.debug("getCourseEditSession - put course in courseEditSessionMap: {}", resourceableId);
		return course;
	}

	public static boolean isCourseEditSessionOpen(Long resourceableId) {
		return courseEditSessionMap.containsKey(resourceableId);
	}

	/**
	 * Provides the currently edited course object with this id. <br/>
	 * It guarantees that the returned value is never null if the openCourseEditSession was called first. <br/>
	 * The CourseEditSession object should live between acquire course lock and release course lock.
	 *
	 * @param resourceableId
	 * @return
	 */
	public static PersistingCourseImpl getCourseEditSession(Long resourceableId) {
		PersistingCourseImpl course = courseEditSessionMap.get(resourceableId);
		if(course == null) {
			throw new AssertException("No edit session open for this course: " + resourceableId + " - Open a session first!");
		}
		return course;
	}

	public static void closeCourseEditSession(Long resourceableId, boolean checkIfAnyAvailable) {
		PersistingCourseImpl course = courseEditSessionMap.get(resourceableId);
		if(course == null && checkIfAnyAvailable) {
			throw new AssertException("No edit session open for this course: " + resourceableId + " - There is nothing to be closed!");
		}
		if(course != null) {
			course.setReadAndWrite(false);
			courseEditSessionMap.remove(resourceableId);
			log.debug("removeCourseEditSession for course: {}", resourceableId);
		}
	}

	private static void visitPublishModel(TreeNode node, PublishTreeModel publishTreeModel, Collection<String> nodeToPublish) {
		int numOfChildren = node.getChildCount();
		for (int i = 0; i < numOfChildren; i++) {
			INode child = node.getChildAt(i);
			if (child instanceof TreeNode && publishTreeModel.isVisible(child)) {
				nodeToPublish.add(child.getIdent());
				visitPublishModel((TreeNode)child, publishTreeModel, nodeToPublish);
			}
		}
	}

	private static class NodeArchiveVisitor implements Visitor {
		private File exportPath;
		private Locale locale;
		private ICourse course;
		private String charset;

		/**
		 * @param locale
		 * @param course
		 * @param exportPath
		 * @param charset
		 */
		public NodeArchiveVisitor(Locale locale, ICourse course, File exportPath, String charset) {
			this.locale = locale;
			this.exportPath = exportPath;
			//o_clusterOk by guido: save to hold reference to course inside editor
			this.course = course;
			this.charset = charset;
		}

		@Override
		public void visit(INode node) {
			CourseNode cn = (CourseNode) node;

			String archiveName = cn.getType() + "_"
					+ StringHelper.transformDisplayNameToFileSystemName(cn.getShortName())
					+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())) + ".zip";

			File exportFile = new File(exportPath, archiveName);
			try(FileOutputStream fileStream = new FileOutputStream(exportFile);
					ZipOutputStream exportStream = new ZipOutputStream(fileStream);) {
				cn.archiveNodeData(locale, course, null, exportStream, "", charset);
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	private static class NodeDeletionVisitor implements Visitor {

		private ICourse course;

		/**
		 * Constructor of the node deletion visitor
		 *
		 * @param course
		 */
		public NodeDeletionVisitor(ICourse course) {
			this.course = course;
		}

		/**
		 * Visitor pattern to delete the course nodes
		 *
		 * @see org.olat.core.util.tree.Visitor#visit(org.olat.core.util.nodes.INode)
		 */
		@Override
		public void visit(INode node) {
			CourseNode cNode = (CourseNode) node;
			cNode.cleanupOnDelete(course);
		}
	}
}

/**
 *
 * Description:<br>
 * Event triggered if a course was edited - namely the course tree model have changed
 * (e.g. nodes added, deleted)
 *
 * <P>
 * Initial Date:  22.07.2008 <br>
 * @author Lavinia Dumitrescu
 */
class ModifyCourseEvent extends MultiUserEvent {
	private static final long serialVersionUID = -2940724437608086461L;
	private final Long courseId;
	/**
	 * @param command
	 */
	public ModifyCourseEvent(Long resourceableId) {
		super("modify_course");
		courseId = resourceableId;
	}

	public Long getCourseId() {
		return courseId;
	}
}