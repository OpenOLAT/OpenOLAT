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

package org.olat.course.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.olat.OlatBeanTypes;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.RepositoryDetailsController;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * Initial Date: 28.11.2003
 * 
 * @author Mike Stock
 * @author guido
 */
public class CourseNodeFactory {
	
	private static OLog log = Tracing.createLoggerFor(CourseNodeFactory.class);
	private static CourseNodeFactory INSTANCE;
	private static List<String> courseNodeConfigurationsAliases;
	private static Map<String, CourseNodeConfiguration> courseNodeConfigurations;
	private Object lockObject = new Object();

	/**
	 * [used by spring]
	 */
	private CourseNodeFactory() {
		INSTANCE = this;
	}


	/**
	 * @return an instance of the course node factory.
	 */
	public static CourseNodeFactory getInstance() {
		return INSTANCE;
	}

	public List<String> getRegisteredCourseNodeAliases() {
		if (courseNodeConfigurationsAliases == null) {
			initCourseNodeConfigurationList();
		}
		return courseNodeConfigurationsAliases;
	}

	private void initCourseNodeConfigurationList() {
		courseNodeConfigurationsAliases = new ArrayList<String>();
		courseNodeConfigurations = new HashMap<String, CourseNodeConfiguration>();
		Map sortedMap = new TreeMap(); 
		Map<String, Object> courseNodeConfigurationMap = CoreSpringFactory.getBeansOfType(OlatBeanTypes.courseNodeConfiguration);
		Collection<Object> courseNodeConfigurationValues = courseNodeConfigurationMap.values();
		for (Object object : courseNodeConfigurationValues) {
			CourseNodeConfiguration courseNodeConfiguration = (CourseNodeConfiguration) object;
			if (courseNodeConfiguration.isEnabled()) {
				int key = courseNodeConfiguration.getOrder();
				while (sortedMap.containsKey(key) ) {
					// a key with this value already exist => add 1000 because offset must be outside of other values.
					key += 1000;
				}
				if ( key != courseNodeConfiguration.getOrder() ) {
					log.warn("CourseNodeConfiguration Problem: Dublicate order-value for node=" + courseNodeConfiguration.getAlias() + ", append course node at the end");
				}
				sortedMap.put(key, courseNodeConfiguration);
			} else {
				log.debug("Disabled courseNodeConfiguration=" + courseNodeConfiguration);
			}
		}
		
		for (Object key : sortedMap.keySet()) {
			CourseNodeConfiguration courseNodeConfiguration = (CourseNodeConfiguration)sortedMap.get(key);
			courseNodeConfigurationsAliases.add(courseNodeConfiguration.getAlias());
			courseNodeConfigurations.put(courseNodeConfiguration.getAlias(), courseNodeConfiguration);
		}
	}
	
	
	/**
	 * @param type The node type
	 * @return a new instance of the desired type of node
	 */
	public CourseNodeConfiguration getCourseNodeConfiguration(String alias) {
		if (courseNodeConfigurations == null) {
			synchronized(lockObject) {
				if (courseNodeConfigurations == null) { // check again in synchronized-block, only one may create list		
					initCourseNodeConfigurationList();
				}
			}
		}
		return courseNodeConfigurations.get(alias);
	}

	/**
	 * Launch an editor for the repository entry which is referenced in the given
	 * course node. The editor is launched in a new tab.
	 * 
	 * @param ureq
	 * @param node
	 */
	public void launchReferencedRepoEntryEditor(UserRequest ureq, CourseNode node) {
		RepositoryEntry repositoryEntry = node.getReferencedRepositoryEntry();
		if (repositoryEntry == null) {
			// do nothing
			return;
		}
		RepositoryHandler typeToEdit = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
		if (!typeToEdit.supportsEdit(repositoryEntry)){
			throw new AssertException("Trying to edit repository entry which has no assoiciated editor: "+ typeToEdit);
		}					
		// Open editor in new tab
		OLATResourceable ores = repositoryEntry.getOlatResource();
		DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
		DTab dt = dts.getDTab(ores);
		if (dt == null) {
			// does not yet exist -> create and add
			dt = dts.createDTab(ores, repositoryEntry.getDisplayname());
			if (dt == null){
				//null means DTabs are full -> warning is shown
				return;
			}
			//user activity logger is set by course factory
			Controller editorController = typeToEdit.createEditorController(ores, ureq, dt.getWindowControl());
			if(editorController == null){
				//editor could not be created -> warning is shown
				return;
			}
			dt.setController(editorController);
			dts.addDTab(dt);
		}
		dts.activate(ureq, dt, RepositoryDetailsController.ACTIVATE_EDITOR);
	}

}
