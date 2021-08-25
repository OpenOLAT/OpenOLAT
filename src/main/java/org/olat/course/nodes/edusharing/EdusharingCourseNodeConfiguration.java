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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.edusharing;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeGroup;
import org.olat.course.nodes.EdusharingCourseNode;
import org.olat.course.nodes.edusharing.ui.EdusharingEditController;
import org.olat.modules.edusharing.EdusharingModule;

/**
 * Initial date: 19 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingCourseNodeConfiguration extends AbstractCourseNodeConfiguration {
	
	private final String alias;
	
	public EdusharingCourseNodeConfiguration() {
		this("edusharing");
	}
	
	public EdusharingCourseNodeConfiguration(String alias) {
		this.alias = alias;
	}

	@Override
	public String getAlias() {
		return alias;
	}
	
	@Override
	public String getGroup() {
		return CourseNodeGroup.content.name();
	}

	@Override
	public String getIconCSSClass() {
		return EdusharingCourseNode.ICON_CSS;
	}

	@Override
	public CourseNode getInstance() {
		return new EdusharingCourseNode();
	}

	@Override
	public String getLinkText(Locale locale) {
		Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		Translator translator = Util.createPackageTranslator(EdusharingEditController.class, locale, fallback);
		return translator.translate("course.node.title");
	}
	
	@Override
	public boolean isEnabled() {
		return CoreSpringFactory.getImpl(EdusharingModule.class).isCourseNodeEnabled();
	}
}