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
package org.olat.modules.openbadges.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.openbadges.criteria.BadgeCondition;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2024-08-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CriteriaViewController extends BasicController {

	private final RepositoryEntry entry;
	private final CourseNode courseNode;
	private final BadgeCriteria criteria;

	public CriteriaViewController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, CourseNode courseNode,
								  String criteriaXmlString) {
		super(ureq, wControl);
		this.entry = entry;
		this.courseNode = courseNode;
		this.criteria = BadgeCriteriaXStream.fromXml(criteriaXmlString);
		VelocityContainer mainVC = createVelocityContainer("criteria_view");

		List<BadgeCondition> badgeConditions = criteria.getConditions();
		List<Condition> conditions = new ArrayList<>(badgeConditions.size());
		for (int i = 0; i < badgeConditions.size(); i++) {
			BadgeCondition badgeCondition = badgeConditions.get(i);
			Condition condition = new Condition(badgeCondition, i == 0, getTranslator(), entry);
			conditions.add(condition);
		}
		mainVC.contextPut("conditions", conditions);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	public record Condition(BadgeCondition badgeCondition, boolean first, Translator translator, RepositoryEntry courseEntry) {
		@Override
		public String toString() {
			return badgeCondition.toString(translator, courseEntry);
		}
	}
}
