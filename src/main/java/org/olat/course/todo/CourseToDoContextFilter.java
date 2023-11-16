/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.todo;

import java.util.Locale;

import org.olat.core.util.Util;
import org.olat.course.todo.ui.CourseToDoUIFactory;
import org.olat.modules.todo.ToDoContextFilter;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 7 Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseToDoContextFilter implements ToDoContextFilter {

	private static final String TYPE = "course.context.filter";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getDisplayName(Locale locale) {
		return Util.createPackageTranslator(CourseToDoUIFactory.class, locale).translate("course.context.filter");
	}

	@Override
	public int getFilterSortOrder() {
		return 200;
	}

}
