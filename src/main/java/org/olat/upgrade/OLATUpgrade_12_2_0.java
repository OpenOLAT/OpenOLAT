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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.MailModule;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.upgrade.model.UpgradeQuestionItem;
import org.olat.upgrade.model.UpgradeTaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.10.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_12_2_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_12_2_0.class);

	private static final int BATCH_SIZE = 500;
	
	private static final String VERSION = "OLAT_12.2.0";
	private static final String KEEP_INTERNAL_MAIL_DISABLED = "KEEP INTERNAL MAIL DISABLED";
	private static final String MAIL_CONFIG_SPLITTING = "MAIL CONFIG SPLITTING";
	private static final String MIGRATE_QPOOL_TAXONOMY = "MIGRATE QPOOL TAXONOMY";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MailModule mailModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	
	public OLATUpgrade_12_2_0() {
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
		// The internal mail is now enabled per default. This new behavior should
		// only be disposed in new installations. Keep existing configurations
		// and do not enable the internal inbox automatically when upgrading a system.
		allOk &= keepInternalMailSettings(upgradeManager, uhd);
		// The config of the visibility of email recipient name and address is
		// split in inbox and outbox specific configs. Get the old values and
		// transfer them to the new values.
		allOk &= splitMailConfigToInboxAndOutbox(upgradeManager, uhd);
		// migrate the question pool taxonomy
		allOk &= migrateTaxonomy(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_12_2_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_12_2_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}

	private boolean keepInternalMailSettings(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(KEEP_INTERNAL_MAIL_DISABLED)) {
			String userDataDirectory = WebappHelper.getUserDataRoot();
			Path configurationPropertiesFile = Paths.get(userDataDirectory, "system", "configuration", "org.olat.core.util.mail.MailModule.properties");
			if (Files.notExists(configurationPropertiesFile)) {
				mailModule.setInterSystem(false);
			}
			
			uhd.setBooleanDataValue(KEEP_INTERNAL_MAIL_DISABLED, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private boolean splitMailConfigToInboxAndOutbox(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MAIL_CONFIG_SPLITTING)) {
			boolean showRecipientsNames = mailModule.isShowOutboxRecipientNames();
			mailModule.setShowInboxRecipientNames(showRecipientsNames);
			log.info("Migrated email config 'show inbox recipient name' to: " + showRecipientsNames);
			boolean showMailAddresses = mailModule.isShowOutboxMailAddresses();
			mailModule.setShowInboxMailAddresses(showMailAddresses);
			log.info("Migrated email config 'show inbox mail adresses' to: " + showMailAddresses);
			
			uhd.setBooleanDataValue(MAIL_CONFIG_SPLITTING, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean migrateTaxonomy(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_QPOOL_TAXONOMY)) {
			Taxonomy taxonomy = getQPoolTaxonomy();
			
			try {
				migrateTaxonomyLevels(taxonomy, null, null);
				migrateQuestions(taxonomy);
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}
			
			uhd.setBooleanDataValue(MIGRATE_QPOOL_TAXONOMY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateQuestions(TaxonomyRef taxonomy) {
		int counter = 0;
		List<UpgradeQuestionItem> questions;
		do {
			questions = getQuestionItems(counter, BATCH_SIZE);
			for(UpgradeQuestionItem question:questions) {
				processQuestionTaxonomyLevel(taxonomy, question);
			}
			counter += questions.size();
			log.info(Tracing.M_AUDIT, "Taxonomy level migration processed: " + questions.size() + ", total questions processed (" + counter + ")");
			dbInstance.commitAndCloseSession();
		} while(questions.size() == BATCH_SIZE);
	}
	
	private List<UpgradeQuestionItem> getQuestionItems(int firstResults, int maxResult) {
		String q = "select item from upgradequestionitem item order by item.key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, UpgradeQuestionItem.class)
				.setFirstResult(firstResults)
				.setMaxResults(maxResult)
				.getResultList();
	}
	
	private void processQuestionTaxonomyLevel(TaxonomyRef taxonomy, UpgradeQuestionItem question) {
		if(question == null || question.getOldTaxonomyLevel() == null || question.getNewTaxonomyLevel() != null) return;
		Long oldKey = question.getOldTaxonomyLevel().getKey();
		if(oldKey == null) return;
		
		List<TaxonomyLevel> levels = taxonomyLevelDao.getLevelsByExternalId(taxonomy, oldKey.toString());
		if(levels.size() > 0) {
			question.setNewTaxonomyLevel(levels.get(0));
			question = dbInstance.getCurrentEntityManager().merge(question);
		}
	}
	
	private void migrateTaxonomyLevels(Taxonomy taxonomy, TaxonomyLevel parentLevel, UpgradeTaxonomyLevel parentUpgradeLevel) {
		List<UpgradeTaxonomyLevel> upgradeLevels;
		if(parentUpgradeLevel == null) {
			upgradeLevels = getRootUpgradeTaxonomyLevels();
		} else {
			upgradeLevels = getTaxonomyLevels(parentUpgradeLevel);
		}

		for(UpgradeTaxonomyLevel upgradeLevel:upgradeLevels) {
			List<TaxonomyLevel> levels = taxonomyLevelDao.getLevelsByExternalId(taxonomy, upgradeLevel.getKey().toString());
			
			TaxonomyLevel newLevel;
			if(levels.isEmpty()) {
				newLevel = copyTaxonomyLevel(taxonomy, parentLevel, upgradeLevel);
			} else {
				newLevel = levels.get(0);
			}
			migrateTaxonomyLevels(taxonomy, newLevel, upgradeLevel);
		}
	}
	
	private TaxonomyLevel copyTaxonomyLevel(Taxonomy taxonomy, TaxonomyLevel parent, UpgradeTaxonomyLevel upgradeLevel) {
		String id = upgradeLevel.getKey().toString();
		String displayName = upgradeLevel.getField();
		return taxonomyLevelDao.createTaxonomyLevel(displayName, null, displayName, "", id, null, parent, null, taxonomy);
	}
	
	private Taxonomy getQPoolTaxonomy() {
		List<Taxonomy> taxonomyList = taxonomyService.getTaxonomyList();
		for(Taxonomy taxonomy:taxonomyList) {
			if(QuestionPoolModule.DEFAULT_TAXONOMY_QPOOL_IDENTIFIER.equals(taxonomy.getIdentifier())) {
				return taxonomy;
			}
		}
		
		Taxonomy taxonomy = taxonomyService.createTaxonomy("QPOOL", "Question pool", "Taxonomy for the question pool", "QPOOL");
		return taxonomy;
	}
	
	private List<UpgradeTaxonomyLevel> getRootUpgradeTaxonomyLevels() {
		String q = "select level from upgradetaxonomylevel level where level.parentField is null";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, UpgradeTaxonomyLevel.class)
				.getResultList();
	}
	
	private List<UpgradeTaxonomyLevel> getTaxonomyLevels(UpgradeTaxonomyLevel parent) {
		String q = "select level from upgradetaxonomylevel level where level.parentField.key=:parentKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, UpgradeTaxonomyLevel.class)
				.setParameter("parentKey", parent.getKey())
				.getResultList();
	}
	

}
