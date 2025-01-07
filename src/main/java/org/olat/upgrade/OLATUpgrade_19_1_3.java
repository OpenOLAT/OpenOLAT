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
package org.olat.upgrade;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * 
 * Initial date: 14 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_19_1_3 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_19_1_3.class);

	private static final int BATCH_SIZE = 1000;
	
	private static final String VERSION = "OLAT_19.1.3";

	private static final String QPOOL_MAX_SCORE = "QUESTION POOL MAX SCORE";

	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QuestionItemDAO questionItemDao;

	public OLATUpgrade_19_1_3() {
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
		allOk &= updateQuestionPoolMaxScore(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_19_1_3 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_19_1_3 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean updateQuestionPoolMaxScore(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(QPOOL_MAX_SCORE)) {
			try {
				log.info("Start updating max. score of questions in question pool.");
				
				int counter = 0;
				List<QuestionItemFull> items;
				do {
					items = getQuestionItems(counter, BATCH_SIZE);
					for(int i=0; i<items.size(); i++) {
						QuestionItemFull item = items.get(i);
						if(item.getMaxScore() == null) {
							updateQuestionItemMaxScore(item);
						}

						if(i % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					counter += items.size();
					log.info(Tracing.M_AUDIT, "Update questions: {} total processed ({})", items.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (items.size() == BATCH_SIZE);

				log.info("Updating max. score of questions in question pool finished.");
				
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(QPOOL_MAX_SCORE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		
		return allOk;
	}
	
	private void updateQuestionItemMaxScore(QuestionItemFull questionItem) {
		try {
			QuestionItemImpl item = loadQuestionById(questionItem.getKey());
			if(item == null || !QTI21Constants.QTI_21_FORMAT.equals(item.getFormat())) {
				return;
			}
			
			File resourceDirectory = qpoolService.getRootDirectory(questionItem);
			File resourceFile = qpoolService.getRootFile(questionItem);
			if(resourceFile == null) {
				return;
			}
			
			URI assessmentItemUri = resourceFile.toURI();
			ResolvedAssessmentItem resolvedAssessmentItem = qtiService
					.loadAndResolveAssessmentItem(assessmentItemUri, resourceDirectory);
			if(resolvedAssessmentItem != null) {
				AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
				if(assessmentItem != null) {
					Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
					if(maxScore != null) {
						item.setMaxScore(BigDecimal.valueOf(maxScore.doubleValue()));
						questionItemDao.merge(item);
						dbInstance.commit();
					}
				}
			}
		} catch (Exception e) {
			log.error("Item cannot be updated: {}", questionItem.getKey());
			dbInstance.rollbackAndCloseSession();
		}
	}
	
	private List<QuestionItemFull> getQuestionItems(int firstResult, int maxResults) {
		String sb = "select item from questionitem item order by item.key asc";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, QuestionItemFull.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private QuestionItemImpl loadQuestionById(Long key) {
		String sb = "select item from questionitem item where item.key=:key";
		List<QuestionItemImpl> items = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItemImpl.class)
				.setParameter("key", key)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		if(items.isEmpty()) {
			return null;
		}
		return items.get(0);
	}
}
