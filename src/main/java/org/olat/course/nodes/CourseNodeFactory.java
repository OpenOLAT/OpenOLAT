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

package org.olat.course.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.configuration.PreWarm;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.course.CorruptedCourseException;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.handlers.EditionSupport;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * Initial Date: 28.11.2003
 * 
 * @author Mike Stock
 * @author guido
 */
public class CourseNodeFactory implements PreWarm {
	
	private static final Logger log = Tracing.createLoggerFor(CourseNodeFactory.class);

	private static CourseNodeFactory INSTANCE;
	private Map<String, CourseNodeConfiguration> allCourseNodeConfigurations;

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

	@Override
	public void run() {
		getAllCourseNodeConfigurations();
	}


	/**
	 * @return the list of enabled aliases
	 */
	public List<String> getRegisteredCourseNodeAliases() {
		List<CourseNodeConfiguration> configList = new ArrayList<>(getAllCourseNodeConfigurations().values());
		Collections.sort(configList, new OrderComparator());
		List<String> alias = new ArrayList<>(configList.size());
		for(CourseNodeConfiguration config:configList) {
			if(config.isEnabled()) {
				alias.add(config.getAlias());
			}
		}
		return alias;
	}

	private Map<String,CourseNodeConfiguration> getAllCourseNodeConfigurations() {
		if(allCourseNodeConfigurations == null) {
			synchronized(INSTANCE) {
				if(allCourseNodeConfigurations == null) {
					Map<String, CourseNodeConfiguration> configurationMap = new HashMap<>();
					Map<String, CourseNodeConfiguration> courseNodeConfigurationMap = CoreSpringFactory.getBeansOfType(CourseNodeConfiguration.class);
					Collection<CourseNodeConfiguration> courseNodeConfigurationValues = courseNodeConfigurationMap.values();
					for (CourseNodeConfiguration courseNodeConfiguration : courseNodeConfigurationValues) {
						configurationMap.put(courseNodeConfiguration.getAlias(), courseNodeConfiguration);
					}
					allCourseNodeConfigurations = Collections.unmodifiableMap(configurationMap);
				}
			}
		}
		return allCourseNodeConfigurations;
	}
	
	/**
	 * @param alias The node type or alias
	 * @return The instance of the desired type of node if enabled
	 */
	public CourseNodeConfiguration getCourseNodeConfiguration(String alias) {
		CourseNodeConfiguration config = getAllCourseNodeConfigurations().get(alias);
		if(config != null && config.isEnabled()) {
			return config;
		}
		return null;
	}

	/**
	 * @param alias The node type or alias
	 * @return The instance of the desired type of node if enabled or not
	 */
	public CourseNodeConfiguration getCourseNodeConfigurationEvenForDisabledBB(String alias) {
		CourseNodeConfiguration config = getAllCourseNodeConfigurations().get(alias);
		if(config == null) {
			config = new DisabledCourseNodeConfiguration(alias);
		}
		return config;
	}
	
	/**
	 * Launch an editor for the repository entry which is referenced in the given
	 * course node. The editor is launched in a new tab.
	 * 
	 * @param ureq
	 * @param node
	 */
	public boolean launchReferencedRepoEntryEditor(UserRequest ureq, WindowControl wControl, CourseNode node) {
		RepositoryEntry repositoryEntry = node.getReferencedRepositoryEntry();
		if (repositoryEntry == null) {
			// do nothing
			return false;
		}
		
		RepositoryHandler typeToEdit = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
		if (typeToEdit.supportsEdit(repositoryEntry.getOlatResource(), ureq.getIdentity(), ureq.getUserSession().getRoles()) == EditionSupport.no){
			log.error("Trying to edit repository entry which has no associated editor: {}", typeToEdit);
			return false;
		}
		
		try {
			String businessPath = "[RepositoryEntry:" + repositoryEntry.getKey() + "][Editor:0]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, wControl);
			return true;
		} catch (CorruptedCourseException e) {
			log.error("Course corrupted: " + repositoryEntry.getKey() + " (" + repositoryEntry.getOlatResource().getResourceableId() + ")", e);
			return false;
		}
	}
	
	private static class OrderComparator implements Comparator<CourseNodeConfiguration> {
		@Override
		public int compare(CourseNodeConfiguration c1, CourseNodeConfiguration c2) {
			if(c1 == null) return -1;
			if(c2 == null) return 1;
			
			int k1 = c1.getOrder();
			int k2 = c2.getOrder();
			int diff = (k1 < k2 ? -1 : (k1==k2 ? 0 : 1));
			if(diff == 0) {
				String a1 = c1.getAlias();
				String a2 = c2.getAlias();
				if(a1 == null) return -1;
				if(a2 == null) return 1;
				diff = a1.compareTo(a1);
			}
			return diff;
		}
	}
	
	private static class DisabledCourseNodeConfiguration extends AbstractCourseNodeConfiguration {
		
		private final String alias;
		
		public DisabledCourseNodeConfiguration(String alias) {
			this.alias = alias;
		}

		@Override
		public String getAlias() {
			return alias;
		}

		@Override
		public boolean isDeprecated() {
			return true;
		}

		@Override
		public boolean isEnabled() {
			return false;
		}

		@Override
		public String getGroup() {
			return null;
		}

		@Override
		public CourseNode getInstance() {
			return null;
		}

		@Override
		public String getLinkText(Locale locale) {
			return null;
		}

		@Override
		public String getIconCSSClass() {
			return "o_unkown_icon";
		}
	}
}