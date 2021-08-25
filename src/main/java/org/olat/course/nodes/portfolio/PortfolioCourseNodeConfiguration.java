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

package org.olat.course.nodes.portfolio;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeGroup;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.modules.portfolio.PortfolioV2Module;

/**
 * Initial Date:  6 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioCourseNodeConfiguration extends AbstractCourseNodeConfiguration {

	public static final String MAP_KEY = "map-key";
	public static final String REPO_SOFT_KEY = "repo-soft-key";
	public static final String NODE_TEXT = "node_text";
	public static final String DEADLINE_MONTH = "deadline_month";
	public static final String DEADLINE_WEEK = "deadline_week";
	public static final String DEADLINE_DAY = "deadline_day";
	public static final String DEADLINE_DATE = "deadline_date";
	public static final String DEADLINE_TYPE = "deadline_type";
	
	public enum DeadlineType {
		none, absolut, relative
	}
	
	private PortfolioCourseNodeConfiguration() {
		super();
	}
	
	@Override
	public String getAlias() {
		return "ep";
	}
	
	@Override
	public String getGroup() {
		return CourseNodeGroup.assessment.name();
	}

	@Override
	public CourseNode getInstance() {
		return new PortfolioCourseNode();
	}

	@Override
	public String getLinkText(Locale locale) {
		Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		Translator translator = Util.createPackageTranslator(this.getClass(), locale, fallback);
		return translator.translate("title_info");
	}

	@Override
	public String getIconCSSClass() {
		return "o_ep_icon";
	}

	@Override
	public boolean isEnabled() {
		return CoreSpringFactory.getImpl(PortfolioV2Module.class).isEnabled();
	}
}
