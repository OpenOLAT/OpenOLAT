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
package org.olat.modules.taxonomy.manager;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.modules.bc.FileInfo;
import org.olat.core.commons.modules.bc.FolderManager;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
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
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.OlatRelPathImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.modules.taxonomy.model.TaxonomyTreeNode;
import org.olat.modules.taxonomy.ui.TaxonomyMainController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyDocumentsLibraryNotificationsHandler implements NotificationsHandler {
	
	private static final OLog log = Tracing.createLoggerFor(TaxonomyDocumentsLibraryNotificationsHandler.class);
	public static final String TYPE_NAME = "TaxonomyLibrary";
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private NotificationsManager notificationsManager;

	@Override
	public String getType() {
		return TYPE_NAME;
	}
	
	public PublisherData getTaxonomyDocumentsLibraryPublisherData(TaxonomyRef taxonomy) {
		String businessPath = "[Taxonomy:" + taxonomy.getKey() + "]";
		return new PublisherData(TaxonomyDocumentsLibraryNotificationsHandler.TYPE_NAME, String.valueOf(taxonomy.getKey()), businessPath);
	}
	
	public SubscriptionContext getTaxonomyDocumentsLibrarySubscriptionContext(TaxonomyRef taxonomy) {
		return new SubscriptionContext(TaxonomyDocumentsLibraryNotificationsHandler.TYPE_NAME, taxonomy.getKey(), TaxonomyDocumentsLibraryNotificationsHandler.TYPE_NAME);
	}
	
	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();

		try {
			SubscriptionInfo si;
			if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
				Taxonomy taxonomy = taxonomyService.getTaxonomy(new TaxonomyRefImpl(p.getResId()));
				if(taxonomy == null) {
					return notificationsManager.getNoSubscriptionInfo();
				}
				
				Identity identity = subscriber.getIdentity();
				Roles roles = securityManager.getRoles(identity);
				boolean isTaxonomyAdmin = roles.isOLATAdmin();
				
				TaxonomyTreeBuilder builder = new TaxonomyTreeBuilder(taxonomy, identity, null, isTaxonomyAdmin);
				TreeModel model = builder.buildTreeModel();
				Translator translator = Util.createPackageTranslator(TaxonomyMainController.class, locale);
				si = new SubscriptionInfo(subscriber.getKey(), p.getType(), getTitleItemForPublisher(p), null);
	
				new TreeVisitor(new Visitor() {
					@Override
					public void visit(INode node) {
						TaxonomyTreeNode tNode = (TaxonomyTreeNode)node;
						if(tNode.getTaxonomyLevel() != null && tNode.isDocumentsLibraryEnabled() && tNode.isCanRead()) {
							VFSContainer container = taxonomyService.getDocumentsLibrary(tNode.getTaxonomyLevel());
							List<FileInfo> fInfos = FolderManager.getFileInfos(((OlatRelPathImpl)container).getRelPath(), compareDate);
							
							String prefixBusinessPath = "[Taxonomy:" + taxonomy.getKey() + "][TaxonomyLevel:" + tNode.getTaxonomyLevel().getKey() + "][path=";
							for (FileInfo infos:fInfos) {
								String title = infos.getRelPath();
								
								// don't show changes in meta-directories. first quick check
								// for any dot files and then compare with our black list of
								// known exclude prefixes
								if (title != null && title.indexOf("/.") != -1 && FileUtils.isMetaFilename(title)) {
									// skip this file, continue with next item in folder
									continue;
								}						
								MetaInfo metaInfo = infos.getMetaInfo();
								String iconCssClass =  null;
								if (metaInfo != null) {
									if (metaInfo.getTitle() != null) {
										title += " (" + metaInfo.getTitle() + ")";
									}
									iconCssClass = metaInfo.getIconCssClass();
								}
								Identity ident = infos.getAuthor();
								Date modDate = infos.getLastModified();

								String desc = translator.translate("notifications.document.entry", new String[] { title, NotificationHelper.getFormatedName(ident) });
								String urlToSend = null;
								String businessPath = null;
								if(p.getBusinessPath() != null) {
									businessPath = prefixBusinessPath + infos.getRelPath() + "]";
									urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
								}
								si.addSubscriptionListItem(new SubscriptionListItem(desc, urlToSend, businessPath, modDate, iconCssClass));
							}
						}
					}
				}, model.getRootNode(), false).visitAll();
			} else {
				si = NotificationsManager.getInstance().getNoSubscriptionInfo();
			}
			return si;
		} catch (Exception e) {
			log.error("Error creating task notifications for subscriber: " + subscriber.getKey(), e);
			return notificationsManager.getNoSubscriptionInfo();
		}
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		TitleItem title = getTitleItemForPublisher(subscriber.getPublisher());
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
	private TitleItem getTitleItemForPublisher(Publisher p) {
		Taxonomy taxonomy = taxonomyService.getTaxonomy(new TaxonomyRefImpl(p.getResId()));
		return getTitleItemForTaxonomy(taxonomy);
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
