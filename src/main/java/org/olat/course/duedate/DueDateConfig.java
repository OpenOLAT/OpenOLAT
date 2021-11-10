/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.duedate;

import java.util.Date;

import org.olat.core.util.StringHelper;
import org.olat.course.duedate.model.DueDateConfigImpl;
import org.olat.course.duedate.model.ModulDueDateConfig;
import org.olat.course.duedate.model.NoDueDateConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 4 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface DueDateConfig extends RelativeDueDateConfig, AbsoluteDueDateConfig {
	
	public static DueDateConfig noDueDateConfig() {
		return NoDueDateConfig.NO_DUE_DATE_CONFIG;
	}
	
	public static DueDateConfig absolute(Date absoluteDate) {
		if (absoluteDate == null) return noDueDateConfig();
		
		DueDateConfigImpl config = new DueDateConfigImpl();
		config.setAbsolutDate(absoluteDate);
		return config;
	}
	
	public static DueDateConfig relative(int numOfDays, String relativeToType) {
		DueDateConfigImpl config = new DueDateConfigImpl();
		config.setNumOfDays(numOfDays);
		config.setRelativeToType(relativeToType);
		return config;
	}
	
	public static DueDateConfig ofCourseNode(CourseNode courseNode, String relativeKey, String absoluteDateKey,
			String numOfDaysKey, String raltiveToTypeKey) {
		return new ModulDueDateConfig(courseNode.getModuleConfiguration(), relativeKey, absoluteDateKey,
				numOfDaysKey, raltiveToTypeKey);
	}
	
	public static DueDateConfig ofModuleConfiguration(ModuleConfiguration config, String relativeKey,
			String absoluteDateKey, String numOfDaysKey, String raltiveToTypeKey) {
		return new ModulDueDateConfig(config, relativeKey, absoluteDateKey, numOfDaysKey, raltiveToTypeKey);
	}
	
	public static boolean isDueDate(DueDateConfig config) {
		return isAbsolute(config) || isRelative(config);
	}
	
	public static boolean isRelative(RelativeDueDateConfig config) {
		return config != null && config.getNumOfDays() >= 0 && StringHelper.containsNonWhitespace(config.getRelativeToType());
	}
	
	public static boolean isAbsolute(AbsoluteDueDateConfig config) {
		return config != null && config.getAbsoluteDate() != null;
	}
	
}
