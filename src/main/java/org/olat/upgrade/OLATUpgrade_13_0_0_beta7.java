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
package org.olat.upgrade;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.modules.forms.model.jpa.EvaluationFormSurveyImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_13_0_0_beta7 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_13_0_0_beta7.class);
	
	private static final String VERSION = "OLAT_13.0.0.beta7";
	private static final String MIGRATE_SURVEY_MATH_PATH = "MIGRATE SURVEY SERIES";
	
	@Autowired
	private DB dbInstance;
	
	public OLATUpgrade_13_0_0_beta7() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}
		
		boolean allOk = true;
		allOk &= migrateSurveySeries(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_13_0_0_beta7 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_13_0_0_beta7 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}

	private boolean migrateSurveySeries(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_SURVEY_MATH_PATH)) {
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("select survey from evaluationformsurvey as survey left join fetch survey.seriesPrevious as previous");
				List<EvaluationFormSurveyImpl> surveys = dbInstance.getCurrentEntityManager()
						.createQuery(sb.toString(), EvaluationFormSurveyImpl.class)
						.getResultList();
				for (int i=0; i<surveys.size(); i++) {
					EvaluationFormSurveyImpl survey = surveys.get(i);
					updateSeries(survey);
					if (i % 50 == 0) {
						log.info("Migration survey series: " + i + " / " + surveys.size());
					}
				}
				log.info("Migration survey series: " + surveys.size());
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_SURVEY_MATH_PATH, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private EvaluationFormSurveyImpl updateSeries(EvaluationFormSurveyImpl survey) {
		EvaluationFormSurveyImpl previous = (EvaluationFormSurveyImpl)survey.getSeriesPrevious();
		if (previous != null) {
			previous = updateSeries(previous);
		}
		Long seriesKey = previous != null? previous.getSeriesKey(): survey.getKey();
		survey.setSeriesKey(seriesKey);
		Integer seriesIndex = previous != null? previous.getSeriesIndex() + 1: 1;
		survey.setSeriesIndex(seriesIndex);
		survey = dbInstance.getCurrentEntityManager().merge(survey);
		return survey;
	}
	
}
