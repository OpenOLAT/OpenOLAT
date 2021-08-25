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

package org.olat.course.nodes.info;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeGroup;
import org.olat.course.nodes.InfoCourseNode;

/**
 * 
 * Description:<br>
 * The configuration of the info message course node
 * 
 * <P>
 * Initial Date:  3 aug. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoCourseNodeConfiguration extends AbstractCourseNodeConfiguration {

	public static final String CONFIG_DURATION_DEPRECATED = "duration";
	public static final String CONFIG_DURATION = "infos.duration";
	public static final String CONFIG_LENGTH = "length";
	public static final String CONFIG_AUTOSUBSCRIBE = "autosubscribe";
	
	@Override
	public String getAlias() {
		return "info";
	}
	
	@Override
	public String getGroup() {
		return CourseNodeGroup.management.name();
	}

	@Override
	public CourseNode getInstance() {
		return new InfoCourseNode();
	}

	@Override
	public String getLinkText(Locale locale) {
		Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		Translator translator = Util.createPackageTranslator(this.getClass(), locale, fallback);
		return translator.translate("title_info");
	}

	@Override
	public String getIconCSSClass() {
		return "o_infomsg_icon";
	}
}
