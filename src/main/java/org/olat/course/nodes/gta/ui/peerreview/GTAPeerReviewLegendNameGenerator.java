/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.peerreview;

import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.ui.LegendNameGenerator;

/**
 * 
 * Initial date: 2 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAPeerReviewLegendNameGenerator implements LegendNameGenerator {

	private final Map<EvaluationFormParticipation,String> userMap;
	
	private GTAPeerReviewLegendNameGenerator(Map<EvaluationFormParticipation,String> userMap) {
		this.userMap = userMap;
	}
	
	public String getName(EvaluationFormParticipation participation) {
		if(participation == null) {
			return "ERROR";
		}
		return userMap.getOrDefault(participation, "ERROR");
	}
	
	@Override
	public String getName(EvaluationFormSession session, Identity identity) {
		return getName(session.getParticipation());
	}

	public static GTAPeerReviewLegendNameGenerator mapSubRows(CoachPeerReviewRow parentRow) {
		Map<EvaluationFormParticipation,String> map =  parentRow.getChildrenRows().stream()
			.filter(r -> r.getParticipation() != null)
			.collect(Collectors.toMap(CoachPeerReviewRow::getParticipation, CoachPeerReviewRow::getFullName, (u, v) -> u));
		return new GTAPeerReviewLegendNameGenerator(map);
	}
	
	public static GTAPeerReviewLegendNameGenerator valueOf(Map<EvaluationFormParticipation,String> map) {
		return new GTAPeerReviewLegendNameGenerator(map);
	}
}
