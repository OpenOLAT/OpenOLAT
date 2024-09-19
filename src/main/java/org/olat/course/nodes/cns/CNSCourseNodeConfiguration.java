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
 * 12.10.2011 by frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.cns;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CNSCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeGroup;

/**
 * 
 * Initial date: 10 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSCourseNodeConfiguration extends AbstractCourseNodeConfiguration {
	
	private final String alias;
	
	public CNSCourseNodeConfiguration() {
		this(CNSCourseNode.TYPE);
	}
	
	public CNSCourseNodeConfiguration(String alias) {
		this.alias = alias;
	}

	@Override
	public String getAlias() {
		return alias;
	}
	
	@Override
	public String getGroup() {
		return CourseNodeGroup.other.name();
	}

	@Override
	public String getIconCSSClass() {
		return CNSCourseNode.ICON_CSS;
	}

	@Override
	public CourseNode getInstance() {
		return new CNSCourseNode();
	}

	@Override
	public String getLinkText(Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		return translator.translate("title_cns");
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
}