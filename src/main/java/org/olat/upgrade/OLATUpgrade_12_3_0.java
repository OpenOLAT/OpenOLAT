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
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSLeafButSystemFilter;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.course.nodes.dialog.model.DialogElementImpl;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.upgrade.legacy.DialogElement;
import org.olat.upgrade.legacy.DialogPropertyElements;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * 
 * Initial date: 15.11.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_12_3_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_12_3_0.class);

	private static final int BATCH_SIZE = 500;
	
	private static final String VERSION = "OLAT_12.3.0";
	private static final String MIGRATE_QPOOL_TITLE = "MIGRATE QPOOL TITLE";
	private static final String MIGRATE_DIALOG = "MIGRATE DIALOG ELEMENTS";
	private static final String MOVE_DOC_POOL_INFOS_PAGE = "MOVE DOC POOL INFOS PAGE";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private DocumentPoolModule documentPoolModule;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private DialogElementsManager dialogElementsManager;
	
	public OLATUpgrade_12_3_0() {
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
		// Migrate the topics from the database field title to topic.
		// Migrate the title of the question (XML) to the database.
		allOk &= migrateQpoolTopicTitle(upgradeManager, uhd);
		allOk &= migrateDialogElements(upgradeManager, uhd);
		allOk &= moveDocumentPoolInfosPage(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_12_3_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_12_3_0 not finished, try to restart OpenOLAT!");
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
			counter += items.size();
			dbInstance.commitAndCloseSession();
			log.info(counter + " QPool items processed.");
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
			log.warn("Not able to migrate question title of QPool item (no title found). dir: " + item.getDirectory() + ", " + item);
		}
	}

	private String getTitle(QuestionItemImpl item) {
		String title = null;
		switch (item.getFormat()) {
			case QTI21Constants.QTI_21_FORMAT:
				title = getTitleQTI21(item);
				break;
			default:
				log.warn("QPool item has no valid format for title migration: {}, {}", item.getFormat(), item);
		}
		return title;
	}

	private String getTitleQTI21(QuestionItemImpl item) {
		try {
			File resourceDirectory = qpoolService.getRootDirectory(item);
			File resourceFile = qpoolService.getRootFile(item);
			URI assessmentItemUri = resourceFile.toURI();
			
			ResolvedAssessmentItem resolvedAssessmentItem = qtiService
					.loadAndResolveAssessmentItem(assessmentItemUri, resourceDirectory);
			
			return resolvedAssessmentItem.getRootNodeLookup().getRootNodeHolder().getRootNode().getTitle();
		} catch (NullPointerException e) {
			log.warn("Cannot read files from dir: {}", item.getDirectory());
		}
		return null;
	}
	
	private boolean migrateDialogElements(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_DIALOG)) {
			try {
				XStream xstream = XStreamHelper.createXStreamInstance();
				xstream.alias("org.olat.modules.dialog.DialogPropertyElements", DialogPropertyElements.class);
				xstream.alias("org.olat.modules.dialog.DialogElement", DialogElement.class);

				List<Property> properties = getProperties();
				for(Property property:properties) {
					migrateDialogElement(property, xstream);
					dbInstance.commitAndCloseSession();
				}
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_DIALOG, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private boolean migrateDialogElement(Property property, XStream xstream) {
		Long resourceId = property.getResourceTypeId();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("CourseModule", resourceId);
		RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(ores, false);
		if(entry != null) {
			String category = property.getCategory();
			if(category.startsWith("NID:dial::")) {
				category = category.substring("NID:dial::".length(), category.length());
			}
		
			String value = property.getTextValue();
			if(StringHelper.containsNonWhitespace(value)) {
				try {
					DialogPropertyElements propertyElements = (DialogPropertyElements)xstream.fromXML(value);
					List<DialogElement> elements = propertyElements.getDialogPropertyElements();
					for(DialogElement element:elements) {
						createDialogElement(element, entry, category);
					}
				} catch (ConversionException e) {
					log.error("Cannot read following dialog element of course: " + entry.getKey() + " with property: " + property.getKey(), e);
				} catch (Exception e) {
					log.error("Error converting following dialog element of course: " + entry.getKey() + " with property: " + property.getKey(), e);
				}
			}
		}
		return true;
	}
	
	private void createDialogElement(DialogElement element, RepositoryEntry entry, String nodeIdent) {
		try {
			Identity author = null;
			if(StringHelper.isLong(element.getAuthor())) {
				author = securityManager.loadIdentityByKey(Long.valueOf(element.getAuthor()));
			} else if(StringHelper.containsNonWhitespace(element.getAuthor())) {
				author = securityManager.findIdentityByName(element.getAuthor());
			}
			
			Forum forum = forumManager.loadForum(element.getForumKey());
			if(forum == null) {
				log.error("Missing forum");
				return;
			}
			
			Object currentElement = dialogElementsManager.getDialogElementByForum(forum.getKey());
			if(currentElement != null) {
				return;
			}
			
			Date date = element.getDate() == null ? new Date() : element.getDate();
			DialogElementImpl el = new DialogElementImpl();
			el.setCreationDate(date);
			el.setLastModified(date);
			el.setFilename(element.getFilename());
			el.setSize(getFileSize(forum.getKey()));
			el.setEntry(entry);
			el.setSubIdent(nodeIdent);
			el.setAuthor(author);
			el.setForum(forum);
			dbInstance.getCurrentEntityManager().persist(el);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	public Long getFileSize(Long forumKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("/forum/").append(forumKey).append("/");
		VFSContainer forumContainer = VFSManager.olatRootContainer(sb.toString(), null);
		if(forumContainer.exists() && !forumContainer.getItems().isEmpty()) {
			VFSItem vl = forumContainer.getItems().get(0);
			if(vl instanceof VFSLeaf) {
				return ((VFSLeaf)vl).getSize();
			}
		}
		return -1l;
	}
	
	private List<Property> getProperties() {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(Property.class.getName()).append(" as v ")
		  .append(" where v.name=:name and v.resourceTypeName=:resName");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Property.class)
				.setParameter("name", "fileDialog")
				.setParameter("resName", "CourseModule")
				.getResultList();
	}
	
	private boolean moveDocumentPoolInfosPage(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MOVE_DOC_POOL_INFOS_PAGE)) {
			String path = "/" + TaxonomyService.DIRECTORY + "/" + DocumentPoolModule.INFOS_PAGE_DIRECTORY;
			VFSContainer taxonomyContainer =  VFSManager.olatRootContainer(path, null);
			VFSContainer documentPoolContainer = documentPoolModule.getInfoPageContainer();
			if(taxonomyContainer.exists()
					&& documentPoolContainer.getItems(new VFSLeafButSystemFilter()).isEmpty()
					&& !taxonomyContainer.getItems(new VFSLeafButSystemFilter()).isEmpty()) {
				VFSManager.copyContent(taxonomyContainer, documentPoolContainer);
				taxonomyContainer.delete();
			}
			uhd.setBooleanDataValue(MOVE_DOC_POOL_INFOS_PAGE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
}
