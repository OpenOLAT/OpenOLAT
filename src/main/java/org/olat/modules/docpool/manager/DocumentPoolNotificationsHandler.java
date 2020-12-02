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
package org.olat.modules.docpool.manager;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.modules.bc.FileInfo;
import org.olat.core.commons.modules.bc.FolderManager;
import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.docpool.ui.DocumentPoolMainController;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyTreeBuilder;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.modules.taxonomy.model.TaxonomyTreeNode;
import org.olat.modules.taxonomy.model.TaxonomyTreeNodeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DocumentPoolNotificationsHandler implements NotificationsHandler {
	
	private static final Logger log = Tracing.createLoggerFor(DocumentPoolNotificationsHandler.class);
	public static final String TYPE_NAME = "DocumentPool";
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private DocumentPoolModule documentPoolModule;
	@Autowired
	private NotificationsManager notificationsManager;

	@Override
	public String getType() {
		return TYPE_NAME;
	}
	
	public PublisherData getTaxonomyDocumentsLibraryPublisherData() {
		String businessPath = "[DocumentPool:0]";
		return new PublisherData(DocumentPoolNotificationsHandler.TYPE_NAME, "", businessPath);
	}
	
	public SubscriptionContext getTaxonomyDocumentsLibrarySubscriptionContext() {
		return new SubscriptionContext(DocumentPoolNotificationsHandler.TYPE_NAME, 0l, DocumentPoolNotificationsHandler.TYPE_NAME);
	}
	
	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();

		try {
			SubscriptionInfo si;
			String taxonomyKey = documentPoolModule.getTaxonomyTreeKey();
			if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews) && StringHelper.isLong(taxonomyKey)) {
				Taxonomy taxonomy = taxonomyService.getTaxonomy(new TaxonomyRefImpl(Long.valueOf(taxonomyKey)));
				if(taxonomy == null) {
					return notificationsManager.getNoSubscriptionInfo();
				}
				
				Identity identity = subscriber.getIdentity();
				Roles roles = securityManager.getRoles(identity);
				boolean isTaxonomyAdmin =  roles.isAdministrator() || roles.isSystemAdmin();
				
				Translator translator = Util.createPackageTranslator(DocumentPoolMainController.class, locale);
				String templates = translator.translate("document.pool.templates");
				TaxonomyTreeBuilder builder = new TaxonomyTreeBuilder(taxonomy, identity, null,
						isTaxonomyAdmin, documentPoolModule.isTemplatesDirectoryEnabled(), templates, locale);
				TreeModel model = builder.buildTreeModel();
				si = new SubscriptionInfo(subscriber.getKey(), p.getType(), getTitleItemForPublisher(), null);
	
				new TreeVisitor(node -> {
					TaxonomyTreeNode tNode = (TaxonomyTreeNode)node;
					if(tNode.getTaxonomyLevel() != null && tNode.isDocumentsLibraryEnabled() && tNode.isCanRead()) {
						VFSContainer container = taxonomyService.getDocumentsLibrary(tNode.getTaxonomyLevel());
						String prefixBusinessPath = "[DocumentPool:" + taxonomy.getKey() + "][TaxonomyLevel:" + tNode.getTaxonomyLevel().getKey() + "][path=";
						createSubscriptionInfo(container, prefixBusinessPath, compareDate, si, p, translator);
					} else if(tNode.getType() == TaxonomyTreeNodeType.templates) {
						VFSContainer container = taxonomyService.getDocumentsLibrary(taxonomy);
						String prefixBusinessPath = "[DocumentPool:" + taxonomy.getKey() + "][Templates:0s][path=";
						createSubscriptionInfo(container, prefixBusinessPath, compareDate, si, p, translator);
					}
				}, model.getRootNode(), false).visitAll();
			} else {
				si = notificationsManager.getNoSubscriptionInfo();
			}
			return si;
		} catch (Exception e) {
			log.error("Cannot create document pool notifications for subscriber: " + subscriber.getKey(), e);
			return notificationsManager.getNoSubscriptionInfo();
		}
	}
	
	private void createSubscriptionInfo(VFSContainer container, String prefixBusinessPath, Date compareDate, SubscriptionInfo si, Publisher p, Translator translator) {
		List<FileInfo> fInfos = FolderManager.getFileInfos(container.getRelPath(), compareDate);
		for (FileInfo infos:fInfos) {
			String title = infos.getRelPath();
			
			// don't show changes in meta-directories. first quick check
			// for any dot files and then compare with our black list of
			// known exclude prefixes
			if (title != null && title.indexOf("/.") != -1 && FileUtils.isMetaFilename(title)) {
				// skip this file, continue with next item in folder
				continue;
			}						
			VFSMetadata metaInfo = infos.getMetaInfo();
			String iconCssClass =  null;
			if (metaInfo != null) {
				if (metaInfo.getTitle() != null) {
					title += " (" + metaInfo.getTitle() + ")";
				}
				iconCssClass = metaInfo.getIconCssClass();
			}
			Long identityKey = infos.getModifiedByIdentityKey();
			Date modDate = infos.getLastModified();

			String desc = identityKey == null
					? translator.translate("notifications.entry.anonymous", new String[] { title })
					: translator.translate("notifications.entry", new String[] { title, NotificationHelper.getFormatedName(identityKey) });
			String urlToSend = null;
			String businessPath = null;
			if(p.getBusinessPath() != null) {
				businessPath = prefixBusinessPath + infos.getRelPath() + "]";
				urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
			}
			si.addSubscriptionListItem(new SubscriptionListItem(desc, urlToSend, businessPath, modDate, iconCssClass));
		}
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		TitleItem title = getTitleItemForPublisher();
		return title.getInfoContent("text/plain");
	}
	
	/**
	 * It returns a TitleItem instance for the given Publisher p if you already
	 * have a reference to the taxonomy, use
	 * <code>getTitleItemForTaxonomy(Taxonomy taxonomy)</code>
	 * 
	 * @param p The publisher
	 * @return The title
	 */
	private TitleItem getTitleItemForPublisher() {
		String taxonomyKey = documentPoolModule.getTaxonomyTreeKey();
		if(StringHelper.isLong(taxonomyKey)) {
			Taxonomy taxonomy = taxonomyService.getTaxonomy(new TaxonomyRefImpl(Long.valueOf(taxonomyKey)));
			if(taxonomy != null) {
				return getTitleItemForTaxonomy(taxonomy);
			}
		}
		return new TitleItem("???", "o_icon_taxonomy");
	}

	/**
	 * It returns a TitleItem instance for the given taxonomy.
	 * 
	 * @param taxonomy The taxonomy
	 * @return The title
	 */
	private TitleItem getTitleItemForTaxonomy(Taxonomy taxonomy) {
		String title = StringHelper.escapeHtml(taxonomy.getDisplayName());
		return new TitleItem(title, "o_icon_taxonomy");
	}
}
