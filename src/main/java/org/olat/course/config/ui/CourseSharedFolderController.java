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

package org.olat.course.config.ui;

import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.resource.references.ReferenceImpl;
import org.olat.resource.references.ReferenceManager;
import org.olat.util.logging.activity.LoggingResourceable;

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
public class CourseSharedFolderController extends BasicController  {

	private static final String SHAREDFOLDERREF = "sharedfolderref";
	
	private VelocityContainer myContent;

	private ReferencableEntriesSearchController searchController;

	private RepositoryManager rm = RepositoryManager.getInstance();

	private boolean hasSF; // has shared folder configured
	private Link changeSFResButton;
	private Link unselectSFResButton;
	private Link selectSFResButton;
	private CloseableModalController cmc;
	private CourseConfig courseConfig;
	private OLATResourceable courseOres;
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param courseOres
	 * @param courseConfig
	 * @param editable
	 */
	public CourseSharedFolderController(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres, CourseConfig courseConfig, boolean editable) {
		super(ureq, wControl);

		this.courseConfig = courseConfig;
		this.courseOres = courseOres;
		
		myContent = createVelocityContainer("CourseSharedFolder");
		if(editable) {
			changeSFResButton = LinkFactory.createButton("sf.changesfresource", myContent, this);
			unselectSFResButton = LinkFactory.createButton("sf.unselectsfresource", myContent, this);
			selectSFResButton = LinkFactory.createButton("sf.selectsfresource", myContent, this);
		}
		
		String softkey = courseConfig.getSharedFolderSoftkey();
		String name;

		if (!courseConfig.hasCustomSharedFolder()) {
			name = translate("sf.notconfigured");
			hasSF = false;
			myContent.contextPut("hasSharedFolder", new Boolean(hasSF));
		} else {
			RepositoryEntry re = rm.lookupRepositoryEntryBySoftkey(softkey, false);
			if (re == null) {
				//log.warning("Removed configured sahred folder from course config, because repo entry does not exist anymore.");
				courseConfig.setSharedFolderSoftkey(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
				name = translate("sf.notconfigured");
				hasSF = false;
				myContent.contextPut("hasSharedFolder", new Boolean(hasSF));
			} else {
				name = re.getDisplayname();
				hasSF = true;
				myContent.contextPut("hasSharedFolder", new Boolean(hasSF));
			}
		}
		myContent.contextPut("resourceTitle", name);
		putInitialPanel(myContent);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == selectSFResButton || source == changeSFResButton) { // select or change shared folder
			// let user choose a shared folder
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, 
					SharedFolderFileResource.TYPE_NAME, translate("command.choose"));
			listenTo(searchController);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent());
			listenTo(cmc);
			cmc.activate();		
		} else if (source == unselectSFResButton) { // unselect shared folder			
			if (courseConfig.hasCustomSharedFolder()) {
				// delete reference from course to sharedfolder
				// get unselected shared folder's softkey used for logging
				// set default value to delete configured value in course config
				courseConfig.setSharedFolderSoftkey(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
				//deleteRefTo(course);
				//course.getCourseEnvironment().setCourseConfig(cc);
				String emptyKey = translate(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
				myContent.contextPut("resourceTitle", emptyKey);
				hasSF = false;
				myContent.contextPut("hasSharedFolder", new Boolean(hasSF));
				saveSharedfolderConfiguration(null);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (searchController == source) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				// repository search controller done
				RepositoryEntry sharedFolder = searchController.getSelectedEntry();				
				hasSF = true;
				myContent.contextPut("hasSharedFolder", new Boolean(hasSF));
				myContent.contextPut("resourceTitle", sharedFolder.getDisplayname());
				saveSharedfolderConfiguration(sharedFolder);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(searchController);
		removeAsListenerAndDispose(cmc);
		searchController = null;
		cmc = null;
	}
	
	private void saveSharedfolderConfiguration(RepositoryEntry sharedFolder) {
		String softKey = sharedFolder == null ?
				CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY : sharedFolder.getSoftkey();
		
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		courseConfig = course.getCourseEnvironment().getCourseConfig();
		courseConfig.setSharedFolderSoftkey(softKey);
		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(course.getResourceableId(),true);
		
		if(sharedFolder != null) {
			CourseSharedFolderController.updateRefTo(sharedFolder, course);
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_SHARED_FOLDER_REMOVED,
					getClass(), LoggingResourceable.wrapBCFile(sharedFolder.getDisplayname()));
		} else {
			CourseSharedFolderController.deleteRefTo(course);
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_SHARED_FOLDER_ADDED,
					getClass(), LoggingResourceable.wrapBCFile(""));
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
		List<ReferenceImpl> repoRefs = refM.getReferences(course);
		for (Iterator<ReferenceImpl> iter = repoRefs.iterator(); iter.hasNext();) {
			ReferenceImpl ref = iter.next();
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
		//
	}
}