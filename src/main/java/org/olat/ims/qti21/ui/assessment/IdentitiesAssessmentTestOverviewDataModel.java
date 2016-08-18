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
package org.olat.ims.qti21.ui.assessment;

import java.math.BigDecimal;
import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 17.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentitiesAssessmentTestOverviewDataModel extends DefaultFlexiTableDataModel<AssessmentTestSession> {

	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final AssessmentTestCorrection assessmentTestCorrection;
	
	public IdentitiesAssessmentTestOverviewDataModel(FlexiTableColumnModel columnModel,
			AssessmentTestCorrection assessmentTestCorrection, List<UserPropertyHandler> userPropertyHandlers,
			Translator translator) {
		super(columnModel);
		this.translator = translator;
		this.userPropertyHandlers = userPropertyHandlers;
		this.assessmentTestCorrection = assessmentTestCorrection;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AssessmentTestSession correction = getObject(row);
		if(col >= 0 && col < IACols.values().length) {
			switch(IACols.values()[col]) {
				case username: return correction.getIdentity().getName();
				case numOfItemSessions: {
					List<AssessmentItemCorrection> itemCorrections = assessmentTestCorrection.getCorrections(correction.getIdentity());
					return itemCorrections.size();
				}
				case responded: {
					List<AssessmentItemCorrection> itemCorrections = assessmentTestCorrection.getCorrections(correction.getIdentity());
					int responded = 0;
					for(AssessmentItemCorrection itemCorrection:itemCorrections) {
						if(itemCorrection.getItemSessionState().isResponded()) {
							responded++;
						}
					}
					return responded;
				}
				case corrected: {
					List<AssessmentItemCorrection> itemCorrections = assessmentTestCorrection.getCorrections(correction.getIdentity());
					int corrected = 0;
					for(AssessmentItemCorrection itemCorrection:itemCorrections) {
						if(itemCorrection.getItemSession() != null && itemCorrection.getItemSession().getManualScore() != null) {
							corrected++;
						}
					}
					return corrected;
				}
				case score: return correction.getScore();
				case manualScore: return correction.getManualScore();
				case finalScore: {
					BigDecimal finalScore = correction.getScore();
					BigDecimal manualScore = correction.getManualScore();
					if(finalScore == null) {
						finalScore = manualScore;
					} else if(manualScore != null) {
						finalScore = finalScore.add(manualScore);
					}
					return finalScore;
				}
			}
		}
		
		int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
		UserPropertyHandler handler = userPropertyHandlers.get(propPos);
		return handler.getUserProperty(correction.getIdentity().getUser(), translator.getLocale());
	}

	@Override
	public DefaultFlexiTableDataModel<AssessmentTestSession> createCopyWithEmptyList() {
		return new IdentitiesAssessmentTestOverviewDataModel(getTableColumnModel(), assessmentTestCorrection, userPropertyHandlers, translator);
	}
	
	public enum IACols implements FlexiColumnDef {
		username("table.header.username"),
		numOfItemSessions("table.header.itemSessions"),
		responded("table.header.responded"),
		corrected("table.header.corrected"),
		score("table.header.score"),
		manualScore("table.header.manualScore"),
		finalScore("table.header.finalScore");
		
		private final String i18nHeaderKey;
		
		private IACols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
