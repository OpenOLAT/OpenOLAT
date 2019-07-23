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
package org.olat.course.db;

import java.util.List;

import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.id.Identity;
import org.olat.course.ICourse;

/**
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public interface CourseDBManager extends ConfigOnOff {


	public boolean isEnabled();
	
	public Long getCourseId(Long key);
	
	public List<String> getUsedCategories(ICourse course);
	
	public void reset(ICourse course, String category);
	
	public CourseDBEntry getValue(ICourse course, Identity identity, String category, String name);
	
	public CourseDBEntry getValue(Long courseResourceId, Identity identity, String category, String name);
	
	public boolean deleteValue(ICourse course, Identity identity, String category, String name);
	
	public CourseDBEntry setValue(ICourse course, Identity identity, String category, String name, Object value);
	
	public List<CourseDBEntry> getValues(ICourse course, Identity identity, String category, String name);
}
