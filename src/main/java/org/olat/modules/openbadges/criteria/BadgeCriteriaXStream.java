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
package org.olat.modules.openbadges.criteria;

import java.io.StringWriter;

import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * Initial date: 2023-06-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeCriteriaXStream {

	private static final XStream xstream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		Class<?>[] types = new Class[] {
				BadgeCriteria.class, BadgeCondition.class, CoursePassedCondition.class, CourseScoreCondition.class,
				OtherBadgeEarnedCondition.class, CourseElementPassedCondition.class, CourseElementScoreCondition.class,
				LearningPathProgressCondition.class, CoursesPassedCondition.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.ignoreUnknownElements();

		xstream.alias("criteria", BadgeCriteria.class);
		xstream.alias(CoursePassedCondition.KEY, CoursePassedCondition.class);
		xstream.alias(CourseScoreCondition.KEY, CourseScoreCondition.class);
		xstream.alias(OtherBadgeEarnedCondition.KEY, OtherBadgeEarnedCondition.class);
		xstream.alias(CourseElementPassedCondition.KEY, CourseElementPassedCondition.class);
		xstream.alias(CourseElementScoreCondition.KEY, CourseElementScoreCondition.class);
		xstream.alias(LearningPathProgressCondition.KEY, LearningPathProgressCondition.class);
		xstream.alias(CoursesPassedCondition.KEY, CoursesPassedCondition.class);
	}

	public static String toXml(BadgeCriteria badgeCriteria) {
		StringWriter stringWriter = new StringWriter();
		xstream.marshal(badgeCriteria, new CompactWriter(stringWriter));
		return stringWriter.toString();
	}

	public static BadgeCriteria fromXml(String xmlString) {
		if (XStreamHelper.readObject(xstream, xmlString) instanceof BadgeCriteria badgeCriteria) {
			return badgeCriteria;
		}
		return null;
	}
}
