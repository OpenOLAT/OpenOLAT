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
package org.olat.restapi.repository.course.config;

import org.olat.repository.RepositoryEntry;
import org.olat.restapi.repository.course.AbstractCourseNodeWebService.CustomConfigDelegate;

public class CustomConfigFactory {
	
	static ICustomConfigCreator creator = null;
	
	public CustomConfigFactory(ICustomConfigCreator creator) {
		CustomConfigFactory.creator = creator;
	}
	
	public static CustomConfigDelegate getTestCustomConfig(RepositoryEntry repoEntry) {
		return CustomConfigFactory.creator.getTestCustomConfig(repoEntry);
	}
	
	public static CustomConfigDelegate getSurveyCustomConfig(RepositoryEntry repoEntry) {
		return CustomConfigFactory.creator.getSurveyCustomConfig(repoEntry);
	}
	
	public interface ICustomConfigCreator {
		public CustomConfigDelegate getTestCustomConfig(RepositoryEntry repoEntry);
		public CustomConfigDelegate getSurveyCustomConfig(RepositoryEntry repoEntry);
	}
}