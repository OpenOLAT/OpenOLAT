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

package org.olat.course.nodes.dialog;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeGroup;
import org.olat.course.nodes.DialogCourseNode;

/**
 * Description:<br>
 * Entry point to get a course node of type "dialog"
 * see olat_extensions.xml and olat_buildingblocks.xml for enabling or disabling this node
 * <P>
 * Initial Date: 02.11.2005 <br>
 * 
 * @author guido
 */
public class DialogCourseNodeConfiguration extends AbstractCourseNodeConfiguration {

	private DialogCourseNodeConfiguration() {
		super();
	}

	@Override
	public String getAlias() {
		return DialogCourseNode.TYPE;
	}

	@Override
	public CourseNode getInstance() {
		return new DialogCourseNode();
	}

	@Override
	public String getLinkText(Locale locale) {
		Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		Translator translator = Util.createPackageTranslator(this.getClass(), locale, fallback);
		return translator.translate("title_" + DialogCourseNode.TYPE);
	}

	@Override
	public String getIconCSSClass() {
		return "o_" + DialogCourseNode.TYPE + "_icon";
	}
	
	@Override
	public String getGroup() {
		return CourseNodeGroup.collaboration.name();
	}
}
