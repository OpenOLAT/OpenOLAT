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
* <p>
*/ 

package org.olat.course.nodes.sp;

import java.util.List;
import java.util.Locale;

import org.olat.core.extensions.ExtensionResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.SPCourseNode;
/**
 * 
 * Description:<br>
 * TODO: guido Class Description for SPCourseNodeConfiguration
 */
public class SPCourseNodeConfiguration extends AbstractCourseNodeConfiguration implements CourseNodeConfiguration {
	
	private SPCourseNodeConfiguration() {
		super();
	}

	public CourseNode getInstance() {
		return new SPCourseNode();
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getLinkText(java.util.Locale)
	 */
	public String getLinkText(Locale locale) {
		Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		Translator translator = Util.createPackageTranslator(this.getClass(), locale, fallback);
		return translator.translate("title_sp");
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getCSSClass()
	 */
	public String getIconCSSClass() {
		return "o_sp_icon";
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getLinkCSSClass()
	 */
	public String getLinkCSSClass() {
		return null;
	}

	public String getAlias() {
		return "sp";
	}

	//
	// OLATExtension interface implementations.
	//

	public String getName() {
		return getAlias();
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#getExtensionResources()
	 */
	public List getExtensionResources() {
		// no ressources, part of main css
		return null;
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#getExtensionCSS()
	 */
	public ExtensionResource getExtensionCSS() {
		// no ressources, part of main css
		return null;
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#setURLBuilder(org.olat.core.gui.render.URLBuilder)
	 */
	public void setExtensionResourcesBaseURI(String ubi) {
	// no need for the URLBuilder
	}

}
