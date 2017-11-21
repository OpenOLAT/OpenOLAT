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

import java.io.File;
import java.net.URI;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * 
 * Initial date: 15.11.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_12_3_0 extends OLATUpgrade {

	private static final int BATCH_SIZE = 500;
	
	private static final String VERSION = "OLAT_12.3.0";
	private static final String MIGRATE_QPOOL_TITLE = "MIGRATE QPOOL TITLE";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QTI21Service qtiService;
	
	public OLATUpgrade_12_3_0() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
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
		// Migrate the topics from the database field title to topic.
		// Migrate the title of the question (XML) to the database.
		allOk &= migrateQpoolTopicTitle(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_12_3_0 successfully!");
		} else {
			log.audit("OLATUpgrade_12_3_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}

	
	private boolean migrateQpoolTopicTitle(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_QPOOL_TITLE)) {
			try {
				migrateQpoolTopics();
				migrateQpoolTitles();
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_QPOOL_TITLE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private void migrateQpoolTopics() {
		// Migrate only once --> topic is null
		String query = "update questionitem set topic = title where topic is null";
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.executeUpdate();
		log.info("QPool topics successfully migrated.");
	}

	private void migrateQpoolTitles() {
		int counter = 0;
		List<QuestionItemImpl> items;
		
		do {
			items = getQuestionItems(counter, BATCH_SIZE);
			for (QuestionItemImpl item: items) {
				try {
					migrateQPoolTitle(item);
					log.info("QPool item successfully migrated: " + item);
				} catch (Exception e) {
					log.error("Not able to migrate question title: " + item, e);
				}
			}
			log.info(counter + " QPool items processed.");
			counter += items.size();
			dbInstance.commitAndCloseSession();
		} while(items.size() == BATCH_SIZE);
	}

	private List<QuestionItemImpl> getQuestionItems(int firstResults, int maxResult)  {
		String query = "select item from questionitem item order by key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, QuestionItemImpl.class)
				.setFirstResult(firstResults)
				.setMaxResults(maxResult)
				.getResultList();
	}

	private void migrateQPoolTitle(QuestionItemImpl item) {
		String title = getTitle(item);
		if (StringHelper.containsNonWhitespace(title)) {
			item.setTitle(title);
			dbInstance.getCurrentEntityManager().merge(item);
		} else {
			throw new RuntimeException("No title found for QPool item");
		}
	}

	private String getTitle(QuestionItemImpl item) {
		String title = null;
		switch (item.getFormat()) {
			case QTI21Constants.QTI_21_FORMAT:
				title = getTitleQTI21(item);
				break;
			case QTIConstants.QTI_12_FORMAT:
				title = getTitleQTI12(item);
				break;
			default:
				log.error("Not able to migrate question title. QPool item has no valid format: " + item);
		}
		return title;
	}

	private String getTitleQTI21(QuestionItemImpl item) {
		File resourceDirectory = qpoolService.getRootDirectory(item);
		File resourceFile = qpoolService.getRootFile(item);
		URI assessmentItemUri = resourceFile.toURI();
		
		ResolvedAssessmentItem resolvedAssessmentItem = qtiService
				.loadAndResolveAssessmentItem(assessmentItemUri, resourceDirectory);
		
		return resolvedAssessmentItem.getRootNodeLookup().getRootNodeHolder().getRootNode().getTitle();
	}

	private String getTitleQTI12(QuestionItemImpl item) {
		VFSLeaf leaf = qpoolService.getRootLeaf(item);
		Item xmlItem = QTIEditHelper.readItemXml(leaf);
		return xmlItem.getTitle();
	}

}
