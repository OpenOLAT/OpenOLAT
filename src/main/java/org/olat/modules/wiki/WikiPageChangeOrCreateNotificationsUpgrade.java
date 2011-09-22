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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.modules.wiki;

import org.jfree.util.Log;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.NotificationsUpgrade;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.nodes.wiki.WikiEditController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Description:<br>
 * update the publisher to business path
 * 
 * <P>
 * Initial Date:  6 jan. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class WikiPageChangeOrCreateNotificationsUpgrade extends LogDelegator implements NotificationsUpgrade {

	@Override
	public Publisher ugrade(Publisher publisher) {
		String businessPath = publisher.getBusinessPath();
		if(businessPath != null && businessPath.startsWith("[")) return null;
		
		Long resId = publisher.getResId();
		if (publisher.getResName().equals( CourseModule.getCourseTypeName() ) ) {
			// resId = CourseResourceableId           p.getSubidentifier() = wikiCourseNode.getIdent()
			CourseNode courseNode = null;
			try {
				ICourse course = CourseFactory.loadCourse(resId);
				CourseEnvironment cenv = course.getCourseEnvironment();
				courseNode = cenv.getRunStructure().getNode(publisher.getSubidentifier());
			} catch (Exception e) {
				Log.warn("Could not load course with resid: "+resId, e);
				return null;
			}
			if(courseNode == null){
				logInfo("deleting publisher with key; "+publisher.getKey(), null);
				//NotificationsManager.getInstance().delete(publisher);
				// return nothing available
				return null;
			}
			ModuleConfiguration config = ((WikiCourseNode)courseNode).getModuleConfiguration();
			RepositoryEntry re = WikiEditController.getWikiRepoReference(config, true);
			resId = re.getOlatResource().getResourceableId();
			businessPath = "[RepositoryEntry:" + re.getKey().toString() + "]" 
				+ "[CourseNode:" + publisher.getSubidentifier() + "]";
		} else {
			// resName = 'BusinessGroup' or 'FileResource.WIKI'
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(publisher.getResName(), resId);
		  BusinessGroup bGroup = BusinessGroupManagerImpl.getInstance().loadBusinessGroup(resId, false);
		  if (bGroup==null) {
			  // Wiki as Repo-Ressource
		  	RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(ores,false);
		  	if (re != null) {
		  		businessPath = "[RepositoryEntry:" + re.getKey().toString() + "]" 
		  		+ "["+re.getOlatResource().getResourceableTypeName()+":" + re.getResourceableId() + "]";
		  	} else {
		  		//repo entry not found, delete publisher
		  		logInfo("deleting publisher with key; "+publisher.getKey(), null);
		  		//NotificationsManager.getInstance().delete(publisher);
		  		return null;
		  	}
		  } else {
		  	businessPath = "[BusinessGroup:" + bGroup.getKey().toString() + "][wiki:0]";			  
		  }
		}
		publisher.setBusinessPath(businessPath);
		return publisher;
	}

	@Override
	public String getType() {
		return "WikiPage";
	}
}