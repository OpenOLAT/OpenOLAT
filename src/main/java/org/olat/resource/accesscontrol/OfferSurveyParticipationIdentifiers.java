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
package org.olat.resource.accesscontrol;

import java.util.UUID;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.resource.OLATResource;

/**
 * Builds and parses the EvaluationFormSurveyIdentifier and the
 * EvaluationFormParticipationIdentifier used for booking order forms.
 *
 * All booking order form surveys of one implementation share the same
 * survey OLATResourceable, type name AC_OFFER_SURVEY_RES_TYPE_NAME, resId =
 * resource key. Real navigation goes through the fk_survey foreign key of
 * OfferToSurvey, so the identifier only needs to satisfy the mandatory
 * parameter of EvaluationFormManager.createSurvey and to support bulk
 * lookups.
 *
 * The participation identifier type is "order", the key is
 * "&lt;orderKey&gt;-&lt;uuid&gt;". The uuid has its dashes stripped, so the
 * first dash in the key always separates the order key from the uuid.
 *
 * Initial date: 1 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyParticipationIdentifiers {

	public static final String SURVEY_ORES_TYPE_NAME = "AcOfferSurvey";
	public static final String PARTICIPATION_TYPE = "order";

	public static OLATResourceable getSurveyOLATResourceable(OLATResource resource) {
		return OresHelper.createOLATResourceableInstance(SURVEY_ORES_TYPE_NAME, resource.getKey());
	}

	public static EvaluationFormParticipationIdentifier of(Order order) {
		String key = order.getKey() + "-" + UUID.randomUUID().toString().replace("-", "");
		return new EvaluationFormParticipationIdentifier(PARTICIPATION_TYPE, key);
	}

	public static boolean isOfferSurveyParticipation(EvaluationFormParticipationIdentifier identifier) {
		return PARTICIPATION_TYPE.equals(identifier.getType());
	}

	public static Long getOrderKey(EvaluationFormParticipationIdentifier identifier) {
		return getOrderKey(identifier.getKey());
	}

	public static Long getOrderKey(String identifierKey) {
		int separatorIndex = identifierKey.indexOf('-');
		String orderKey = separatorIndex < 0 ? identifierKey : identifierKey.substring(0, separatorIndex);
		return Long.valueOf(orderKey);
	}

}
