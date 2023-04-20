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
package org.olat.course.nodes.jupyterhub;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeGroup;
import org.olat.course.nodes.JupyterHubCourseNode;
import org.olat.modules.jupyterhub.JupyterHubModule;

/**
 * Initial date: 2023-04-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class JupyterHubCourseNodeConfiguration extends AbstractCourseNodeConfiguration {

	@Override
	public String getAlias() {
		return "jupyterHub";
	}

	@Override
	public String getGroup() {
		return CourseNodeGroup.content.name();
	}

	@Override
	public CourseNode getInstance() {
		return new JupyterHubCourseNode();
	}

	@Override
	public String getLinkText(Locale locale) {
		Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		Translator translator = Util.createPackageTranslator(this.getClass(), locale, fallback);
		return translator.translate("link.text");
	}

	@Override
	public String getIconCSSClass() {
		return "o_jupyter_icon";
	}

	@Override
	public boolean isEnabled() {
		JupyterHubModule jupyterHubModule = CoreSpringFactory.getImpl(JupyterHubModule.class);
		return jupyterHubModule.isEnabled() && jupyterHubModule.isEnabledForCourseElement();
	}
}
