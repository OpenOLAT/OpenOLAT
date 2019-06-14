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
package org.olat.modules.forms;

import org.olat.core.id.OLATResourceable;

/**
 * 
 * Initial date: 12 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface EvaluationFormSurveyIdentifier {
	
	/**
	 * OLATResourceable which is using the survey.
	 *
	 * @return
	 */
	public OLATResourceable getOLATResourceable();
	
	/**
	 * The OLATResourceable may define a subIdent, e.g. the course node.
	 *
	 * @return
	 */
	public String getSubident();
	
	/**
	 * The OLATResourceable may define even a second subIdent.
	 *
	 * @return
	 */
	public String getSubident2();
	
	public static EvaluationFormSurveyIdentifier of(OLATResourceable ores) {
		return of(ores, null);
	}
	
	public static EvaluationFormSurveyIdentifier of(OLATResourceable ores, String resSubident) {
		return of(ores, resSubident, null);
	}
	
	public static EvaluationFormSurveyIdentifier of(OLATResourceable ores, String resSubident, String resSubident2) {
		return new EvaluationFormSurveyIdentifierImpl(ores, resSubident, resSubident2) ;
	}
	
	static class EvaluationFormSurveyIdentifierImpl implements EvaluationFormSurveyIdentifier {
		
		private final OLATResourceable ores;
		private final String subident;
		private final String subident2;
		
		private EvaluationFormSurveyIdentifierImpl(OLATResourceable ores, String resSubident, String resSubident2) {
			this.ores = ores;
			this.subident = resSubident;
			this.subident2 = resSubident2;
		}

		@Override
		public OLATResourceable getOLATResourceable() {
			return ores;
		}

		@Override
		public String getSubident() {
			return subident;
		}

		@Override
		public String getSubident2() {
			return subident2;
		}
		
	}

}
