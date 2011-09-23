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

package org.olat.course.config.ui;

import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.resource.references.ReferenceImpl;
import org.olat.resource.references.ReferenceManager;

/**
 * Description: <br>
 * User (un)selects one shared folder per course. The softkey of the shared
 * folder repository entry is saved in the course config. Also the reference
 * (course -> repo entry)is saved, that nobody can delete a shared folder which
 * is still referenced from a course.
 * <P>
 * 
 * @version Initial Date: July 11, 2005
 * @author Alexander Schneider
 */
public class CourseSharedFolderController extends DefaultController implements ControllerEventListener {

	private static final String PACKAGE = Util.getPackageName(CourseSharedFolderController.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(PACKAGE);
	private static final String SHAREDFOLDERREF = "sharedfolderref";
	
	//private ICourse course;
	private PackageTranslator translator;
	private VelocityContainer myContent;

	private ReferencableEntriesSearchController searchController;

	private RepositoryManager rm = RepositoryManager.getInstance();

	private boolean hasSF; // has shared folder configured
	private Link changeSFResButton;
	private Link unselectSFResButton;
	private Link selectSFResButton;
	private CloseableModalController cmc;
	private CourseConfig courseConfig;
	private ILoggingAction loggingAction;
	private RepositoryEntry sharedFolderRepositoryEntry;

	/**
	 * @param ureq
	 * @param wControl
	 * @param theCourse
	 */
	public CourseSharedFolderController(UserRequest ureq, WindowControl wControl, CourseConfig courseConfig) {
		super(wControl);

		this.courseConfig = courseConfig;
		translator = new PackageTranslator(PACKAGE, ureq.getLocale());
		
		myContent = new VelocityContainer("courseSharedFolderTab", VELOCITY_ROOT + "/CourseSharedFolder.html", translator, this);
		changeSFResButton = LinkFactory.createButton("sf.changesfresource", myContent, this);
		unselectSFResButton = LinkFactory.createButton("sf.unselectsfresource", myContent, this);
		selectSFResButton = LinkFactory.createButton("sf.selectsfresource", myContent, this);
		
		String softkey = courseConfig.getSharedFolderSoftkey();
		String name;

		if (!courseConfig.hasCustomSharedFolder()) {
			name = translator.translate("sf.notconfigured");
			hasSF = false;
			myContent.contextPut("hasSharedFolder", new Boolean(hasSF));
		} else {
			RepositoryEntry re = rm.lookupRepositoryEntryBySoftkey(softkey, false);
			if (re == null) {
				//log.warning("Removed configured sahred folder from course config, because repo entry does not exist anymore.");
				courseConfig.setSharedFolderSoftkey(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
				name = translator.translate("sf.notconfigured");
				hasSF = false;
				myContent.contextPut("hasSharedFolder", new Boolean(hasSF));
			} else {
				name = re.getDisplayname();
				hasSF = true;
				myContent.contextPut("hasSharedFolder", new Boolean(hasSF));
			}
		}
		myContent.contextPut("resourceTitle", name);

		setInitialComponent(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == selectSFResButton || source == changeSFResButton) { // select or change shared folder
			// let user choose a shared folder
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, 
					SharedFolderFileResource.TYPE_NAME, translator.translate("command.choose"));
			searchController.addControllerListener(this);
			cmc = new CloseableModalController(getWindowControl(), translator.translate("close"), searchController.getInitialComponent());
			cmc.activate();		
		} else if (source == unselectSFResButton) { // unselect shared folder			
			if (courseConfig.hasCustomSharedFolder()) {
				// delete reference from course to sharedfolder
				// get unselected shared folder's softkey used for logging
				String softkeyUsf = courseConfig.getSharedFolderSoftkey();
				RepositoryEntry usfRe = rm.lookupRepositoryEntryBySoftkey(softkeyUsf, true);
				if (usfRe != null) sharedFolderRepositoryEntry = usfRe;
				// set default value to delete configured value in course config
				courseConfig.setSharedFolderSoftkey(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
				//deleteRefTo(course);
				//course.getCourseEnvironment().setCourseConfig(cc);
				String emptyKey = translator.translate(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
				myContent.contextPut("resourceTitle", emptyKey);
				hasSF = false;
				myContent.contextPut("hasSharedFolder", new Boolean(hasSF));
				loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_SHARED_FOLDER_REMOVED;
				this.fireEvent(ureq, Event.CHANGED_EVENT);
				//AuditManager am = course.getCourseEnvironment().getAuditManager();				
				//am.log(LogLevel.ADMIN_ONLY_FINE, ureq.getIdentity(),LOG_SHARED_FOLDER_REMOVED, null, usfRe.getDisplayname());
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == searchController) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				// repository search controller done
				sharedFolderRepositoryEntry = searchController.getSelectedEntry();				
				String softkey = sharedFolderRepositoryEntry.getSoftkey();
				courseConfig.setSharedFolderSoftkey(softkey);
				//updateRefTo(sharedFolderRe, course);
				//course.getCourseEnvironment().setCourseConfig(cc);
				hasSF = true;
				myContent.contextPut("hasSharedFolder", new Boolean(hasSF));

				myContent.contextPut("resourceTitle", sharedFolderRepositoryEntry.getDisplayname());
				
				loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_SHARED_FOLDER_ADDED;
				this.fireEvent(ureq, Event.CHANGED_EVENT);
				/*AuditManager am = course.getCourseEnvironment().getAuditManager();
				am.log(LogLevel.ADMIN_ONLY_FINE, ureq.getIdentity(),LOG_SHARED_FOLDER_ADDED, null, sharedFolderRe.getDisplayname());
				*/
			}
			cmc.deactivate();
		}
	}

	/**
	 * Sets the reference from a course to a shared folder.
	 * 
	 * @param sharedFolderRe
	 * @param course
	 */
	public static void updateRefTo(RepositoryEntry sharedFolderRe, ICourse course) {
		deleteRefTo(course);
		ReferenceManager.getInstance().addReference(course, sharedFolderRe.getOlatResource(), SHAREDFOLDERREF);
	}
	
	/**
	 * Deletes the reference from a course to a shared folder.
	 * 
	 * @param entry - the course that holds a reference to a sharedfolder
	 */
	public static void deleteRefTo(ICourse course) {
		ReferenceManager refM = ReferenceManager.getInstance();
		List repoRefs = refM.getReferences(course);
		for (Iterator iter = repoRefs.iterator(); iter.hasNext();) {
			ReferenceImpl ref = (ReferenceImpl) iter.next();
			if (ref.getUserdata().equals(SHAREDFOLDERREF)) {
				refM.delete(ref);
				return;
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if (searchController != null) {
			searchController.dispose();
			searchController = null;
		}

	}

	/**
	 * 
	 * @return Returns a log message if the course shared folder was added or removed, null otherwise.
	 */
	public ILoggingAction getLoggingAction() {
		return loggingAction;
	}

	public RepositoryEntry getSharedFolderRepositoryEntry() {
		return sharedFolderRepositoryEntry;
	}

}