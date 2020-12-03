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

package org.olat.collaboration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.CalendarModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.group.BusinessGroup;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.modules.openmeetings.OpenMeetingsModule;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.wiki.WikiModule;

/**
 * Description:<BR>
 * The singleton used for retrieving a collaboration tools suite associated with
 * the supplied OLATResourceable. Supports caching of elements as access to properties can get slow (table can become very large)
 * <P>
 * Initial Date:  2004/10/12 12:45:53
 *
 * @author Felix Jost
 * @author guido
 */
public class CollaborationToolsFactory {
	private static final Logger log = Tracing.createLoggerFor(CollaborationToolsFactory.class);
	private static CollaborationToolsFactory instance;
	private CacheWrapper<String,CollaborationTools> cache;
	private CoordinatorManager coordinatorManager;

	/**
	 * public for group test only, do not use otherwise convenience, helps
	 * iterating possible tools, i.e. in jUnit testCase, also for building up a
	 * tools choice
	 */
	private static String[] TOOLS;
	
	
	/**
	 * [used by spring]
	 */
	private CollaborationToolsFactory(CoordinatorManager coordinatorManager) {
		this.coordinatorManager = coordinatorManager;
		cache = coordinatorManager.getCoordinator().getCacher().getCache(CollaborationToolsFactory.class.getSimpleName(), "tools");
		instance = this;
	}

	/**
	 * Helper method to initialize the list of enabled tools based system wide
	 * configuration.
	 */
	public synchronized void initAvailableTools() {
		ArrayList<String> toolArr = new ArrayList<>();
		toolArr.add(CollaborationTools.TOOL_NEWS);
		toolArr.add(CollaborationTools.TOOL_CONTACT);
		CalendarModule calendarModule = CoreSpringFactory.getImpl(CalendarModule.class);
		if(calendarModule.isEnabled() && calendarModule.isEnableGroupCalendar()) {
			toolArr.add(CollaborationTools.TOOL_CALENDAR);
		}
		toolArr.add(CollaborationTools.TOOL_FOLDER);
		toolArr.add(CollaborationTools.TOOL_FORUM);
		if (CoreSpringFactory.getImpl(InstantMessagingModule.class).isEnabled()) {
			toolArr.add(CollaborationTools.TOOL_CHAT);
		}
		WikiModule wikiModule = CoreSpringFactory.getImpl(WikiModule.class); 
		if (wikiModule.isWikiEnabled()) {
			toolArr.add(CollaborationTools.TOOL_WIKI);			
		}
		PortfolioV2Module portfolioV2Module = CoreSpringFactory.getImpl(PortfolioV2Module.class);
		if (portfolioV2Module.isEnabled()) {
			toolArr.add(CollaborationTools.TOOL_PORTFOLIO);
		}	
		OpenMeetingsModule openMeetingsModule = CoreSpringFactory.getImpl(OpenMeetingsModule.class);
		if(openMeetingsModule.isEnabled()) {
			toolArr.add(CollaborationTools.TOOL_OPENMEETINGS);
		}
		AdobeConnectModule adobeConnectModule = CoreSpringFactory.getImpl(AdobeConnectModule.class);
		if(adobeConnectModule.isEnabled() && adobeConnectModule.isGroupsEnabled()) {
			toolArr.add(CollaborationTools.TOOL_ADOBECONNECT);
		}

		BigBlueButtonModule bigBlueButtonModule = CoreSpringFactory.getImpl(BigBlueButtonModule.class);
		if(bigBlueButtonModule.isEnabled() && bigBlueButtonModule.isGroupsEnabled() && hasBigBlueButtonTemplates()) {
			toolArr.add(CollaborationTools.TOOL_BIGBLUEBUTTON);
		}
		TeamsModule teamsModule = CoreSpringFactory.getImpl(TeamsModule.class);
		if(teamsModule.isEnabled() && teamsModule.isGroupsEnabled()) {
			toolArr.add(CollaborationTools.TOOL_TEAMS);
		}
		
		TOOLS = ArrayHelper.toArray(toolArr);				
	}
	
	private boolean hasBigBlueButtonTemplates() {
		BigBlueButtonManager bigBlueButtonManager = CoreSpringFactory.getImpl(BigBlueButtonManager.class);
		List<BigBlueButtonTemplatePermissions> permissions = Arrays
				.asList(BigBlueButtonTemplatePermissions.group, BigBlueButtonTemplatePermissions.coach);
		List<BigBlueButtonMeetingTemplate> templates = bigBlueButtonManager.getTemplates(permissions);
		return !templates.isEmpty();
	}
	
	/**
	 * Get the array of available (system wide enabled) collaboration tools
	 * @return
	 */
	public String[] getAvailableTools() {
		if (TOOLS == null) {
			initAvailableTools();
		}
		return TOOLS;
	}
	
	/**
	 * it is a singleton.
	 * 
	 * @return CollaborationToolsFactory
	 */
	public static CollaborationToolsFactory getInstance() {
		return instance;
	}

	/**
	 * create a collaborative toolsuite for the specified OLATResourcable
	 * 
	 * @param ores
	 * @return CollaborationTools
	 */
	public CollaborationTools getOrCreateCollaborationTools(final BusinessGroup ores) {
		if (ores == null) throw new AssertException("Null is not allowed here, you have to provide an existing ores here!");
		
		final String cacheKey = Long.toString(ores.getResourceableId().longValue());
		//sync operation cluster wide

		CollaborationTools collabTools = cache.get(cacheKey);
		if (collabTools != null) {		
			log.debug("loading collabTool from cache. Ores: {}", ores.getResourceableId());		
			if (collabTools.isDirty()) {
				log.debug("CollabTools were in cache but dirty. Creating new ones. Ores: {}", ores.getResourceableId());
				CollaborationTools tools = new CollaborationTools(coordinatorManager, ores);
				//update forces clusterwide invalidation of this object
				cache.update(cacheKey, tools);
				collabTools = tools;
			}	
		} else {
			log.debug("collabTool not in cache. Creating new ones. Ores: {}", ores.getResourceableId());
	
			CollaborationTools tools = new CollaborationTools(coordinatorManager, ores);
			CollaborationTools cachedTools = cache.putIfAbsent(cacheKey, tools);
			if(cachedTools != null) {
				collabTools = cachedTools;
			} else {
				collabTools = tools;
			}
		}
		return collabTools;
	}
	
	/**
	 * if you are sure that the cache is populated with the latest version of the collabtools
	 * you can use this method to avoid nested do in sync on the cluster
	 * @param ores
	 * @return
	 */
	public CollaborationTools getCollaborationToolsIfExists(OLATResourceable ores) {
		String cacheKey = Long.toString(ores.getResourceableId().longValue());
		return cache.get(cacheKey);
	}

	
}