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

package org.olat.course.run.glossary;

import java.util.Properties;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.glossary.GlossaryItemManager;
import org.olat.core.commons.modules.glossary.GlossaryMainController;
import org.olat.core.commons.modules.glossary.GlossarySecurityCallback;
import org.olat.core.commons.modules.glossary.GlossarySecurityCallbackImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.textmarker.GlossaryMarkupItemController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.config.CourseConfig;
import org.olat.modules.glossary.GlossaryManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;

/**
 * 
 * Description: <br>
 * Factory methods to create the glossary wrapper, the run and edit controller
 * <p>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 * 
 */
public class CourseGlossaryFactory {

	/**
	 * The glossary wrapper enables the glossary in the given component. Meaning,
	 * within the component the glossary terms are highlighted. The controller hides
	 * itself, the user won't see anything besides the glossary terms.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param tmComponent the component to which the glossary should be applied
	 * @param courseConfig use the glossary configuration from the given course
	 *          configuration
	 */
	public static GlossaryMarkupItemController createGlossaryMarkupWrapper(UserRequest ureq, WindowControl wControl, Component tmComponent, CourseConfig courseConfig){
		if (courseConfig.hasGlossary() && courseConfig.isGlossaryEnabled()) {
			RepositoryEntry repoEntry = CoreSpringFactory.getImpl(RepositoryManager.class).lookupRepositoryEntryBySoftkey(courseConfig.getGlossarySoftKey(),	false);
			if (repoEntry == null) {
				// seems to be removed
				return null;
			}
			VFSContainer glossaryFolder = CoreSpringFactory.getImpl(GlossaryManager.class).getGlossaryRootFolder(repoEntry.getOlatResource());
			String glossaryId = repoEntry.getOlatResource().getResourceableId().toString();
			return new GlossaryMarkupItemController(ureq, wControl, tmComponent, glossaryFolder, glossaryId);
		}
		return null;
	}
		
	/**
	 * Creates the key for the GUI preferences where the users glossary display
	 * settings are stored
	 * 
	 * @param course
	 * @return
	 */
	public static String createGuiPrefsKey(OLATResourceable course) {
		return "glossary.enabled.course." + course.getResourceableId();
	}

	
	/**
	 * The glossarymaincontroller allows browsing in the glossary. A flag enables
	 * the edit mode.
	 * 
	 * @param windowControl
	 * @param ureq
	 * @param courseConfig use the glossary configuration from the given course
	 *          configuration
	 * @param hasGlossaryEditRights
	 * @return
	 */
	public static GlossaryMainController createCourseGlossaryMainRunController(WindowControl lwControl, UserRequest lureq, CourseConfig cc,
			boolean hasGlossaryRights) {
		if (cc.hasGlossary()) {
			RepositoryEntry repoEntry = CoreSpringFactory.getImpl(RepositoryManager.class)
					.lookupRepositoryEntryBySoftkey(cc.getGlossarySoftKey(),
					false);
			if (repoEntry == null) {
				// seems to be removed
				return null;
			}

			boolean owner = CoreSpringFactory.getImpl(RepositoryService.class)
					.hasRole(lureq.getIdentity(), repoEntry, GroupRoles.owner.name());
			VFSContainer glossaryFolder = CoreSpringFactory.getImpl(GlossaryManager.class)
					.getGlossaryRootFolder(repoEntry.getOlatResource());
			Properties glossProps = CoreSpringFactory.getImpl(GlossaryItemManager.class)
					.getGlossaryConfig(glossaryFolder);
			boolean editUsersEnabled =  "true".equals(glossProps.getProperty(GlossaryItemManager.EDIT_USERS));
			GlossarySecurityCallback secCallback;
			if (lureq.getUserSession().getRoles().isGuestOnly()) {
				secCallback = new GlossarySecurityCallbackImpl();				
			} else {
				secCallback = new GlossarySecurityCallbackImpl(hasGlossaryRights, owner, editUsersEnabled, lureq.getIdentity().getKey());				
			}
			return new GlossaryMainController(lwControl, lureq, glossaryFolder, repoEntry.getOlatResource(), secCallback, true);
		}
		return null;
	}
	
}
