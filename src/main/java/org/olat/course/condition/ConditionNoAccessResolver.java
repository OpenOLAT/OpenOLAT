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
package org.olat.course.condition;

import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.nodeaccess.NoAccessResolver;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 12 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConditionNoAccessResolver implements NoAccessResolver {
	
	public static final ConditionNoAccessResolver INSTANCE = new ConditionNoAccessResolver();
	
	private ConditionNoAccessResolver() {
		//
	}

	@Override
	public NoAccess getNoAccessMessage(CourseNode courseNode) {
		String explanation = courseNode.getNoAccessExplanation();
		explanation = StringHelper.containsNonWhitespace(explanation)? Formatter.formatLatexFormulas(explanation): "";
		return NoAccessResolver.condition(explanation);
	}

}
