/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.upgrade;

import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.area.BGAreaManagerImpl;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManager;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.modules.fo.ForumManager;
import org.olat.modules.fo.Message;
import org.olat.note.Note;
import org.olat.note.NoteManager;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.HomePageConfig;
import org.olat.user.HomePageConfigManager;
import org.olat.user.HomePageConfigManagerImpl;

/**
 * Description:<br>
 * Upgrade to OLAT 6.2:
 * - Migration of old wiki-fields to flexiform 
 * 
 * Code is already here for every update. 
 * Method calls will be commented out step by step when corresponding new controllers are ready.
 * As long as there will be other things to migrate Upgrade won't be set to DONE!
 * 
 * <P>
 * Initial Date: 20.06.09 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_6_2_0 extends OLATUpgrade {
	private static final String VERSION = "OLAT_6.2";
	private static final String TASK_MIGRATE_WIKICODE_NOTES = "Migrate Wiki-field NOTE to new syntax";
	private static final String TASK_MIGRATE_WIKICODE_FORUM = "Migrate Wiki-field FORUM to new syntax";
	private static final String TASK_MIGRATE_WIKICODE_GROUPNEWS = "Migrate Wiki-field GROUP-INFORMATION/NEWS to new syntax";
	private static final String TASK_MIGRATE_WIKICODE_BGCONTEXT = "Migrate Wiki-field BUSINESSGROUPCONTEXT to new syntax";
	private static final String TASK_MIGRATE_WIKICODE_BGAREA = "Migrate Wiki-field BUSINESSGROUPAREA to new syntax";
	private static final String TASK_MIGRATE_WIKICODE_BG_DESC = "Migrate Wiki-field BUSINESSGROUPDESC to new syntax";
	private static final String TASK_MIGRATE_WIKICODE_HOMEPAGE = "Migrate Wiki-field in HOMEPAGE/BIO to new syntax";
	private static final String TASK_MIGRATE_WIKICODE_REPOENTRY = "Migrate Wiki-field in REPOSITORY ENTRY to new syntax";
	
	
	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPreSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
			return false;
	}

	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPostSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) return false;
		}

		long startTime = 0 ;
		if (log.isDebug()) startTime = System.currentTimeMillis();
		
//		migrationTest();
		
	//migrate old wiki htmlareas to new tinymce fields -> persist with html code instead wiki-syntax
	    //userNotes
	    migrateNotes(upgradeManager, uhd);
	    if (log.isDebug()) {
	    	log.debug("OLATUpgrade_6_2_0: migrateNotes takes " + (System.currentTimeMillis() - startTime) + "ms");
	    	startTime = System.currentTimeMillis();
	    }
	    
	    //forum posts
	    migrateForum(upgradeManager, uhd);
	    if (log.isDebug()) {
	    	log.debug("OLATUpgrade_6_2_0: migrateForum takes " + (System.currentTimeMillis() - startTime) + "ms");
	    	startTime = System.currentTimeMillis();
	    }	
	    
	    //newsform (information in groups)
	    migrateGroupNews(upgradeManager, uhd);
	    if (log.isDebug()) {
	    	log.debug("OLATUpgrade_6_2_0: migrateGroupNews takes " + (System.currentTimeMillis() - startTime) + "ms");
	    	startTime = System.currentTimeMillis();
	    }	

	    // business group description
	    migrateGroupDescription(upgradeManager, uhd);
	    if (log.isDebug()) {
	    	log.debug("OLATUpgrade_6_2_0: migrateGroupDescription takes " + (System.currentTimeMillis() - startTime) + "ms");
	    	startTime = System.currentTimeMillis();
	    }		    
	    
	    //BG Context Groups
	    migrateBGContext(upgradeManager, uhd);
	    if (log.isDebug()) {
	    	log.debug("OLATUpgrade_6_2_0: migrateBGContext takes " + (System.currentTimeMillis() - startTime) + "ms");
	    	startTime = System.currentTimeMillis();
	    }		    

	    //BG Areas
	    migrateBGArea(upgradeManager, uhd);
	    if (log.isDebug()) {
	    	log.debug("OLATUpgrade_6_2_0: migrateBGArea takes " + (System.currentTimeMillis() - startTime) + "ms");
	    	startTime = System.currentTimeMillis();
	    }		    
	    
	    //Repository Entry
	    migrateRepoEntry(upgradeManager, uhd);
	    if (log.isDebug()) {
	    	log.debug("OLATUpgrade_6_2_0: migrateRepoEntry takes " + (System.currentTimeMillis() - startTime) + "ms");
	    	startTime = System.currentTimeMillis();
	    }		    

	    //homepage-bio / visitcard
	    migrateHomepageBio(upgradeManager, uhd);
	    if (log.isDebug()) {
	    	log.debug("OLATUpgrade_6_2_0: migrateHomepageBio takes " + (System.currentTimeMillis() - startTime) + "ms");
	    	startTime = System.currentTimeMillis();
	    }		    
	    
	    // tests for xss-filter, needs to be done during upgrade (startup) as jUnit has other database without real data.
//	    testXSSFilter();
//	    if (log.isDebug()) {
//	    	log.debug("OLATUpgrade_6_2_0: testing the XSS Filter takes " + (System.currentTimeMillis() - startTime) + "ms");
//	    	startTime = System.currentTimeMillis();
//	    }		    
   
    // now pre and post code was ok, finish installation
		uhd.setInstallationComplete(true);
		// persist infos
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		return true;
	}


	public String getVersion() {
		return VERSION;
	}
	
	private void migrateNotes(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_NOTES)) {
			log.audit("+---------------------------------------------------------------+");
			log.audit("+... " + TASK_MIGRATE_WIKICODE_NOTES + "   ...+");
			log.audit("+---------------------------------------------------------------+");
			BaseSecurity secMgr = BaseSecurityManager.getInstance();
			NoteManager noteMgr = NoteManager.getInstance();
			List<Identity> identitiesList = secMgr.getIdentitiesByPowerSearch(null, null, true, null, null, null,
					null, null, null, null, null);
			DBFactory.getInstance().intermediateCommit();
			int counter = 0;
			int usercounter = 0;
			if (log.isDebug()) log.info("Migrating notes for " + identitiesList.size() + " Identities.");
			for (Iterator<Identity> iterator = identitiesList.iterator(); iterator.hasNext();) {
				Identity identity = iterator.next();
				try{
					List<Note> allIdentityNotes = noteMgr.listUserNotes(identity);
					if (log.isDebug()) log.info("Migrate " + allIdentityNotes.size() + " Notes for Identity: " + identity.getName());
					if (!allIdentityNotes.isEmpty()){
						usercounter++;
						for (Iterator<Note> iterator2 = allIdentityNotes.iterator(); iterator2.hasNext();) {
							try{
								Note note = iterator2.next();
								String parsedText = note.getNoteText();
								parsedText = migrateStringSavely(parsedText);
								note.setNoteText(parsedText);
								noteMgr.saveNote(note);
								counter ++;
								DBFactory.getInstance().intermediateCommit();
							} catch (Exception e) {
								log.error("Error during Migration: "+e, e);
								DBFactory.getInstance().rollback();
							}
								
							if (counter > 0 && counter % 150 == 0){
								if (log.isDebug()) log.audit("Another 150 items done");
							}
						}
					}
					DBFactory.getInstance().intermediateCommit();
				} catch (Exception e) {
					log.error("Error during Migration: "+e, e);
					DBFactory.getInstance().rollback();
				}
			}
			DBFactory.getInstance().intermediateCommit();
			log.audit("Migrated total " + counter + " notes of " + usercounter + " users with notes");
			uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_NOTES, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);			
		}		
	}

	
	private void migrateForum(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_FORUM)) {
			log.audit("+---------------------------------------------------------------+");
			log.audit("+...     " + TASK_MIGRATE_WIKICODE_FORUM + "     ...+");
			log.audit("+---------------------------------------------------------------+");
			ForumManager fMgr = ForumManager.getInstance();
			List<Long> allForumKeys = fMgr.getAllForumKeys();
			int fCounter = 0;
			int totMCounter = 0;
			if (log.isDebug()) log.info("Migrating " + allForumKeys.size() + " forums.");
			for (Iterator<Long> iterator = allForumKeys.iterator(); iterator.hasNext();) {
				try{
					Long forumKey = iterator.next();
	//				Long forumKey = new Long(338493441);
					log.audit("  Found forum with key: " + forumKey.toString() + " containing " + fMgr.countMessagesByForumID(forumKey) + " messages to migrate.");
					List<Message> allMessages = fMgr.getMessagesByForumID(forumKey);
					fCounter++;
					int mCounter = 0;
					for (Iterator<Message> iterator2 = allMessages.iterator(); iterator2.hasNext();) {
						try{
							Message message = iterator2.next();
							if (log.isDebug()){
								log.audit("    - Message inside: " + message.getTitle() + " key: " + message.getKey());
							}
							String oldValue = message.getBody();
							String newMsgBody = migrateStringSavely(oldValue);
							message.setBody(newMsgBody);
							// Update message without ForumManager to prevent resetting the lastModifiedTime
							DBFactory.getInstance().updateObject(message);
							mCounter ++;
							DBFactory.getInstance().intermediateCommit();
						} catch (Exception e) {
							log.error("Error during Migration: "+e, e);
							DBFactory.getInstance().rollback();
						}
						if (mCounter > 0 && mCounter % 150 == 0) {
							if (log.isDebug()) log.audit("Another 150 items done");
						}
					}
					totMCounter += mCounter;
					//commit for each forum
					DBFactory.getInstance().intermediateCommit();
				} catch (Exception e) {
					log.error("Error during Migration: "+e, e);
					DBFactory.getInstance().rollback();
				}
			}
			log.audit("**** Migrated " + fCounter + " forums with a total of " + totMCounter + " messages inside. ****");
			uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_FORUM, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);		
		}
	}
	
	private void migrateGroupNews(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_GROUPNEWS)) {
			log.audit("+---------------------------------------------------------------+");
			log.audit("+...     " + TASK_MIGRATE_WIKICODE_GROUPNEWS + "     ...+");
			log.audit("+---------------------------------------------------------------+");
			PropertyManager pm = PropertyManager.getInstance();
			
			// hardcoded query since manager does not provide appropriate method
			StringBuilder query = new StringBuilder();
			query.append("from v in class org.olat.properties.Property where ");
			query.append("v.category = '").append(CollaborationTools.PROP_CAT_BG_COLLABTOOLS).append("'");
			query.append(" and ");
			query.append("v.name = 'news'");			
			List<Property> props = DBFactory.getInstance().find(query.toString());
			if (log.isDebug()) log.info("Found " + props.size() + " groupnews to migrate.");
			
			int counter = 0;
			for (Property property : props) {
				try{
					String oldVal = property.getTextValue();
					String newVal = migrateStringSavely(oldVal);
					property.setTextValue(newVal);
					pm.updateProperty(property);
					counter++;
					DBFactory.getInstance().intermediateCommit();
				} catch (Exception e) {
					log.error("Error during Migration: "+e, e);
					DBFactory.getInstance().rollback();
				}
					
				if (counter > 0 && counter % 150 == 0) {
					if (log.isDebug()) log.audit("Another 150 items done");
				}
			}
			DBFactory.getInstance().intermediateCommit();
			log.audit("**** Migrated " + counter + " group news. ****");
			uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_GROUPNEWS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	
	
	
	private void migrateRepoEntry(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_REPOENTRY)) {
			log.audit("+---------------------------------------------------------------+");
			log.audit("+...     " + TASK_MIGRATE_WIKICODE_REPOENTRY + "     ...+");
			log.audit("+---------------------------------------------------------------+");

			DB db = DBFactory.getInstance();
			StringBuilder q = new StringBuilder();
			q.append(" select repoEntry from org.olat.repository.RepositoryEntry as repoEntry");
			DBQuery query = db.createQuery(q.toString());
			List<RepositoryEntry> entries = (List<RepositoryEntry>) query.list();
			RepositoryManager repoManager = RepositoryManager.getInstance();
			if (log.isDebug()) log.info("Migrating " + entries.size() + " Repository Entires.");
			int counter = 0;
			for (RepositoryEntry entry : entries) {
				try{
					String oldDesc = entry.getDescription();
					if (StringHelper.containsNonWhitespace(oldDesc)) {
						String newDesc = migrateStringSavely(oldDesc);
						entry.setDescription(newDesc);
						repoManager.updateRepositoryEntry(entry);
						counter++;
					}
					DBFactory.getInstance().intermediateCommit();
				} catch (Exception e) {
					log.error("Error during Migration: "+e, e);
					DBFactory.getInstance().rollback();
				}
				if (counter > 0 && counter % 150 == 0) {
					if (log.isDebug()) log.audit("Another 150 items done");
				}
			}
			DBFactory.getInstance().intermediateCommit();
			log.audit("**** Migrated " + counter + " repository entries. ****");
			uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_REPOENTRY, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}


	private void migrateBGContext(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_BGCONTEXT)) {
			log.audit("+---------------------------------------------------------------+");
			log.audit("+...     " + TASK_MIGRATE_WIKICODE_BGCONTEXT + "     ...+");
			log.audit("+---------------------------------------------------------------+");

			DB db = DBFactory.getInstance();
			StringBuilder q = new StringBuilder();
			q.append(" select context from org.olat.group.context.BGContextImpl as context");
			DBQuery query = db.createQuery(q.toString());

			List<BGContext> contexts = (List<BGContext>) query.list();
			if (log.isDebug()) log.info("Migrating " + contexts.size() + " BG Contexts.");
			BGContextManager contextManager = BGContextManagerImpl.getInstance();
			int bgcounter = 0;
			for (BGContext context : contexts) {
				try{
					String oldDesc = context.getDescription();
					if (StringHelper.containsNonWhitespace(oldDesc)) {
						String newDesc = migrateStringSavely(oldDesc);
						context.setDescription(newDesc);
						contextManager.updateBGContext(context);
						bgcounter++;
					}
					DBFactory.getInstance().intermediateCommit();
				} catch (Exception e) {
					log.error("Error during Migration: "+e, e);
					DBFactory.getInstance().rollback();
				}
				if (bgcounter > 0 && bgcounter % 150 == 0) {
					if (log.isDebug()) log.audit("Another 150 items done");
				}
			}

			DBFactory.getInstance().intermediateCommit();
			log.audit("**** Migrated " + bgcounter + " BGContext. ****");

			uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_BGCONTEXT, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	

	private void migrateBGArea(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_BGAREA)) {
			log.audit("+---------------------------------------------------------------+");
			log.audit("+...     " + TASK_MIGRATE_WIKICODE_BGAREA + "     ...+");
			log.audit("+---------------------------------------------------------------+");

			DB db = DBFactory.getInstance();
			String q = "select area from org.olat.group.area.BGAreaImpl area ";
			DBQuery query = db.createQuery(q);
			List<BGArea> areas = query.list();
			if (log.isDebug()) log.info("Migrating " + areas.size() + " BG areas.");
			BGAreaManager bgM = BGAreaManagerImpl.getInstance();
			int bgcounter = 0;

			for (BGArea area : areas) {
				try{
					String oldDesc = area.getDescription();
					if (StringHelper.containsNonWhitespace(oldDesc)) {
						String newDesc = migrateStringSavely(oldDesc);
						area.setDescription(newDesc);
						bgM.updateBGArea(area);
						bgcounter++;
					}
					DBFactory.getInstance().intermediateCommit();
				} catch (Exception e) {
					log.error("Error during Migration: "+e, e);
					DBFactory.getInstance().rollback();
				}
				if (bgcounter > 0 && bgcounter % 150 == 0) {
					if (log.isDebug()) log.audit("Another 150 items done");
				}

			}
			DBFactory.getInstance().intermediateCommit();
			log.audit("**** Migrated " + bgcounter + " BGAreas. ****");
			uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_BGAREA, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}

	private void migrateGroupDescription(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_BG_DESC)) {
			log.audit("+---------------------------------------------------------------+");
			log.audit("+...     " + TASK_MIGRATE_WIKICODE_BG_DESC + "     ...+");
			log.audit("+---------------------------------------------------------------+");

			int bgcounter = 0;			
			BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
			List<BusinessGroup> allGroups = bgm.getAllBusinessGroups();
			if (log.isDebug()) log.info("Migrating " + allGroups.size() + " BusinessGroups.");

			if (allGroups != null && allGroups.size() != 0) {
				for (BusinessGroup group : allGroups) {
					try{
						String oldDesc = group.getDescription();
						if (StringHelper.containsNonWhitespace(oldDesc)) {
							String newDesc = migrateStringSavely(oldDesc);
							group.setDescription(newDesc);
							bgm.updateBusinessGroup(group);
							bgcounter++;
						}
						DBFactory.getInstance().intermediateCommit();
					} catch (Exception e) {
						log.error("Error during Migration: "+e, e);
						DBFactory.getInstance().rollback();
					}
					if (bgcounter > 0 && bgcounter % 150 == 0) {
						if (log.isDebug()) log.audit("Another 150 items done");
					}
					
				}
				DBFactory.getInstance().intermediateCommit();
				log.audit("**** Migrated " + bgcounter + " BusinessGroups. ****");

				uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_BG_DESC, true);
				upgradeManager.setUpgradesHistory(uhd, VERSION);
			}
		}
	}


	private void migrateHomepageBio(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_HOMEPAGE)) {
			log.audit("+---------------------------------------------------------------+");
			log.audit("+...     " + TASK_MIGRATE_WIKICODE_HOMEPAGE + "     ...+");
			log.audit("+---------------------------------------------------------------+");
			
			BaseSecurity secMgr = BaseSecurityManager.getInstance();
			List<Identity> identitiesList = secMgr.getIdentitiesByPowerSearch(null, null, true, null, null, null,
					null, null, null, null, null);
			HomePageConfigManager hpcm = HomePageConfigManagerImpl.getInstance();
			int counter = 0;
			if (log.isDebug()) log.info("Migrating homepage-bio for " + identitiesList.size() + " identities.");

			for (Identity identity : identitiesList) {
				try{
					HomePageConfig hpcfg = hpcm.loadConfigFor(identity.getName());
					String oldBio = hpcfg.getTextAboutMe();
					if (StringHelper.containsNonWhitespace(oldBio)){
						String newBio = migrateStringSavely(oldBio);
						hpcfg.setTextAboutMe(newBio);
						hpcm.saveConfigTo(identity.getName(), hpcfg);
						counter++;
					}
					DBFactory.getInstance().intermediateCommit();
				} catch (Exception e) {
					log.error("Error during Migration: "+e, e);
					DBFactory.getInstance().rollback();
				}
				if (counter % 150 == 0) {
					if (log.isDebug()) log.audit("Another 150 items done");
				}				
			}
			
			DBFactory.getInstance().intermediateCommit();
			log.audit("**** Migrated total " + counter + " Homepage-biography fields to new syntax. ****");
			 uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_HOMEPAGE, true);
			 upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	
	@SuppressWarnings("deprecation") // as it has to be used here (was before deprecation)!
	private String migrateStringSavely(String oldValue){
		if (log.isDebug()) log.audit("Old String before migration:", oldValue);
		String newValue = "";

		//newValue = Formatter.formatWikiMarkup(oldValue);
		log.warn("no wikiMarkupUpdate is done, OpenOLAT 8 does not support the legacy wiki markup forms!");
		
		if (log.isDebug()) log.audit("New String after migration:", newValue);
		return newValue;
	}

	private void migrationTest() {
		String testString = "Von support@olat.unizh.ch bin {$} oder {} oder [$] <> <$> ich auf diese Diskussion verwiesen worden. Mein ursprünglicher Beitrag von dem ein Teil aber hier schon besprochen wurde ist:> In Foren-Einträgen kann man kein <pre> verwenden, dafür aber viel tolle> Smileys.Wenn man Programmcode zeigen möchte, erhält man dann ein> unleserliches Durcheinander. Eckige Klammern der Form \"[]\" oder {} werden aus dem Beitrag gleich ganz gefiltert.> Es gibt auch keine Syntax um URLsabzugrenzen. Wenn eine URL zum> Beispiel am Ende eines Satzes steht, muss man den Punkt am Satzende> weglassen, damit er nicht als Teil der URL angesehen wird. BBCode oder> mehr von der Mediawiki-Syntax> (<http://de.wikipedia.org/wiki/Hilfe:Textgestaltung>) wäre sehr praktisch.Das Zitat illustriert auch schon die angesprochenen Probleme: Eckige Klammern und Backslash gefiltert, eine spitze schließende Klammer \">\" + eine runde schließende Klammer \")\", die zu einem zwinkernden Smiley wird, aber die spitze schließende Klammer stehen lässt (was nach dokumentierter Syntax nicht einmal korrekt ist) und die URL dessen abgrenzendes \">\" in die URL mitaufgenommen wird und deshalb auf eine nicht-existente Seite verweist. Man kann auch nicht korrekt auf URLs verweisen die ein \"&\" oder \"öffnende Eckige Klammer\" oder \"schließende eckige Klammer\" enthalten:Beispiel:http://www.srgdeutschschweiz.ch/omb_beanstandung.0.html?&tx_ttnews[pS]=1189036060&tx_ttnews[tt_news]=494&tx_ttnews[backPid]=186Die korrekte URL ist mittels http://tinyurl.com/2jq5wd ersichtlich.http://www.google.com/search?hl=en&q=search&btnG=Google+SearchDas korrekte Ergebnis wäre eine Google-Suche nach \"search\".Aus http://www.ietf.org/rfc/rfc2396.txt S. 33 \"Recommendations for Delimiting URI in Context\": > Using <> angle brackets around each URI is especially recommended as> a delimiting style for URI that contain whitespace.> (...)> For robustness, software that accepts user-typed URI should attempt> to recognize and strip both delimiters and embedded whitespace.Zu den Smileys: Wenn man schon autmatisch bestimmte Zeichenkombinationen in grafische Smileys umwandelt, muss man dem Benutzer auch die Möglichkeit geben dieses Verhalten per Checkbox vor dem Post eines Beitrages zu deaktivieren. Bilder mit zwinkernden Smileys in seinem Text zu haben, der zufällig eine der Zeichenketten enthält ist unschön. Die Syntax für ein Zitat sollte den Anfang und das Ende des Zitats kennzeichen und nicht wie derzeit vor jede zitierte Zeile hinzugefügt werden müssen und einen Parameter enthalten, der die Quelle des Zitats bzw. den Autor des Zitats angibt.Wenn ihr das jetzt also komplett überarbeitet, dann wäre es schon, wenn die angesprochenen Punkte vorher getestet würden. Es gibt ja genug funktionierende Webforen an denen man sehen kann, welche Funktionen sinnvoll sind und wie man diese mittels praktischer Syntax implementieren kann. Es sollte dann auch die Möglichkeit geben, Zeichen, die zur Syntax gehören, aber zum Beispiel auch in URLs vorkommen können, zu escapen. Ein solcher Fall wäre zum Beispiel die URL mit den eckigen Klammern in Verbindung mit BBCode. Eine Funktion wie <nowiki>...</nowiki> um über Syntax schreiben zu können, wäre auch praktisch. Mit BackslashGeschweifteKlammerAufCodeGeschweifteKlammerZu kann man zwar \\{code} erzeugen, aber man kann anscheinend einem anderen nicht sagen wie er das auch machen könnte, es sei denn man bedient sich einer umständlichen, ungenauen Umschreibung für die einzelnen Zeichen. Zweimal \"Backslash\"Code führt zu \\\\{code} .\n";
		String newValue = migrateStringSavely(testString);
	}
	

	//testing the xss filter infrastructure by filtering forum messages and comparing to original value
	private void testXSSFilter() {
		log.audit("+---------------------------------------------------------------+");
		log.audit("                    Testing the XSS-Filter ");
		log.audit("+---------------------------------------------------------------+");
		DBFactory.getInstance().intermediateCommit();
		ForumManager fMgr = ForumManager.getInstance();
		List<Long> allForumKeys = fMgr.getAllForumKeys();
		int fCounter = 0;
		int totMCounter = 0;
		int sucCounter = 0;
		OWASPAntiSamyXSSFilter xssFilter = new OWASPAntiSamyXSSFilter(-1, false);
		for (Iterator<Long> iterator = allForumKeys.iterator(); iterator.hasNext();) {
			Long forumKey = iterator.next();
			List<Message> allMessages = fMgr.getMessagesByForumID(forumKey);
			fCounter++;
			int mCounter = 0;
			for (Iterator<Message> iterator2 = allMessages.iterator(); iterator2.hasNext();) {
				try {
					Message message = iterator2.next();
					if (log.isDebug()) {
						log.audit("    - Message inside: " + message.getTitle() + " key: " + message.getKey());
					}
					String msgBody = message.getBody();
					String filteredVal = xssFilter.filter(msgBody);
					if (msgBody.equals(filteredVal)){
						sucCounter++;
					} else {
						String errMsg = xssFilter.getOrPrintErrorMessages();
						if (errMsg.equals("")){
							sucCounter++;
						}
					}		
					mCounter++;
					if (mCounter > 0 && mCounter % 150 == 0){
						DBFactory.getInstance().rollback();
						DBFactory.getInstance().closeSession();
					}
				} catch (Exception e) {
					log.error("Error during XSS test: ", e);
				}
			}
			totMCounter += mCounter;
			DBFactory.getInstance().rollback();
			DBFactory.getInstance().closeSession();
		}
		double percent = ((double) sucCounter / totMCounter * 100);
		log.audit("**** Tested XSS Filter with " + fCounter + " forums with a total of " + totMCounter + " messages inside. ****");
		log.audit("Successful on " + sucCounter + " messages. This is " + percent + "% correct.");
		log.audit("Please send log to Roman, to fine-tune the XSSFilter");
	}
	
	
}
