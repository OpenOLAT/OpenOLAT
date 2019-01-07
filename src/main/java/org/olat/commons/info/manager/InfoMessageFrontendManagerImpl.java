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

package org.olat.commons.info.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.commons.info.InfoMessageManager;
import org.olat.commons.info.InfoSubscriptionManager;
import org.olat.commons.info.model.InfoMessageImpl;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  28 juil. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class InfoMessageFrontendManagerImpl implements InfoMessageFrontendManager {

	private final DateFormat formater = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	private static final OLog log = Tracing.createLoggerFor(InfoMessageFrontendManagerImpl.class);
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private InfoMessageManager infoMessageManager;
	@Autowired
	private InfoSubscriptionManager infoSubscriptionManager;

	@Override
	public InfoMessage loadInfoMessage(Long key) {
		return infoMessageManager.loadInfoMessageByKey(key);
	}

	@Override
	public InfoMessage createInfoMessage(OLATResourceable ores, String subPath, String businessPath, Identity author) {
		return infoMessageManager.createInfoMessage(ores, subPath, businessPath, author);
	}
	
	@Override
	public void saveInfoMessage(InfoMessage infoMessage) {
		infoMessageManager.saveInfoMessage(infoMessage);
	}
	
	@Override
	public VFSLeaf getAttachment(InfoMessage msg) {
		VFSLeaf attachment = null;
		if(StringHelper.containsNonWhitespace(msg.getAttachmentPath())) {
			VFSItem item = getStoragePath().resolve(msg.getAttachmentPath());
			if(item instanceof VFSLeaf) {
				attachment = (VFSLeaf)item;
			}
		}
		return attachment;
	}
	
	@Override
	public void deleteAttachments(Collection<String> paths) {
		if(paths == null || paths.isEmpty()) return;
		
		VFSContainer ressourceContainer = getStoragePath();
		for(String path:paths) {
			VFSItem item = ressourceContainer.resolve(path);
			if(item instanceof VFSLeaf) {
				((VFSLeaf)item).deleteSilently();
			}
		}
	}

	@Override
	public String storeAttachment(File file, String filename, OLATResourceable ores, String subPath) {
		try {
			File ressourceDir = getResourceDir(ores);

			String datePart;
			synchronized(formater) {
				datePart = formater.format(new Date());
			}
			if(filename == null) {
				filename = file.getName();
			}
			filename = datePart + "_" + filename;
			File attachment = new File(ressourceDir, filename);
			Files.copy(file.toPath(), attachment.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
			File root = getStoragePath().getBasefile();
			Path relativePath = root.toPath().relativize(attachment.toPath());
			return relativePath.toString();
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	private File getResourceDir(OLATResourceable ores) {
		File root = getStoragePath().getBasefile();
		String type = ores.getResourceableTypeName().toLowerCase();
		File typePath = new File(root, type);
		String id = ores.getResourceableId().toString();
		File resourceFile = new File(typePath, id);
		if(!resourceFile.exists()) {
			resourceFile.mkdirs();
		}
		return resourceFile;
	}
	
	private VFSContainer getResourceContainer(OLATResourceable ores) {
		VFSContainer root = getStoragePath();
		String type = ores.getResourceableTypeName().toLowerCase();
		VFSItem typePath = root.resolve(type);
		if(typePath == null) {
			typePath = root.createChildContainer(type);
		}
		String id = ores.getResourceableId().toString();
		if(typePath instanceof VFSContainer) {
			VFSContainer typeContainer = (VFSContainer)typePath;
			VFSItem resourceItem = typeContainer.resolve(id);
			if(resourceItem == null) {
				resourceItem = typeContainer.createChildContainer(id);
			}
			
			if(resourceItem instanceof VFSContainer) {
				return (VFSContainer)resourceItem;
			}
		}
		return null;
	}
	
    private OlatRootFolderImpl getStoragePath() {
    	return new OlatRootFolderImpl("/infomessages/", null);
	}

	@Override
	public boolean sendInfoMessage(InfoMessage infoMessage, MailFormatter mailFormatter, Locale locale, Identity from, List<Identity> tos) {
		infoMessageManager.saveInfoMessage(infoMessage);
		
		boolean send = false;
		if(tos != null && !tos.isEmpty()) {
			Set<Long> identityKeySet = new HashSet<>();
			ContactList contactList = new ContactList("Infos");
			for(Identity to:tos) {
				if(identityKeySet.contains(to.getKey())) continue;
				contactList.add(to);
				identityKeySet.add(to.getKey());
			}
			
			try {
				String subject = null;
				String body = null;
				if(mailFormatter != null) {
					subject = mailFormatter.getSubject(infoMessage);
					body = mailFormatter.getBody(infoMessage);
				}
				if(!StringHelper.containsNonWhitespace(subject)) {
					subject = infoMessage.getTitle();
				}
				if(!StringHelper.containsNonWhitespace(body)) {
					body = infoMessage.getMessage();
				}
				File attachment = null;
				if(StringHelper.containsNonWhitespace(infoMessage.getAttachmentPath())) {
					File root = getStoragePath().getBasefile();
					attachment = new File(root, infoMessage.getAttachmentPath());
				}
				
				MailContext context = new MailContextImpl(mailFormatter.getBusinessPath());
				MailBundle bundle = new MailBundle();
				bundle.setContext(context);
				bundle.setFromId(from);
				bundle.setContactList(contactList);
				if(attachment != null) {
					bundle.setContent(subject, body, attachment);
				} else {
					bundle.setContent(subject, body);
				}
				
				MailerResult result = mailManager.sendMessage(bundle);
				send = result.isSuccessful();
			} catch (Exception e) {
				log.error("Cannot send info messages", e);
			}
		}

		infoSubscriptionManager.markPublisherNews(infoMessage.getOLATResourceable(), infoMessage.getResSubPath());
		MultiUserEvent mue = new MultiUserEvent("new_info_message");
		coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(mue, oresFrontend);
		return send;
	}
	
	@Override
	public void deleteInfoMessage(InfoMessage infoMessage) {
		if(StringHelper.containsNonWhitespace(infoMessage.getAttachmentPath())) {
			deleteAttachments(Collections.singletonList(infoMessage.getAttachmentPath()));
		}
		infoMessageManager.deleteInfoMessage(infoMessage);
		infoSubscriptionManager.markPublisherNews(infoMessage.getOLATResourceable(), infoMessage.getResSubPath());
	}
	
	@Override
	public void updateInfoMessagesOfIdentity(BusinessGroupRef businessGroup, IdentityRef identity) {
		List<InfoMessage> infoMessages = infoMessageManager.loadInfoMessagesOfIdentity(businessGroup, identity);
		for (InfoMessage infoMessage : infoMessages) {
			Identity author = infoMessage.getAuthor();
			if (author != null && author.getKey().equals(identity.getKey())) {
				((InfoMessageImpl)infoMessage).setAuthor(null);
			} 
			Identity modifier = infoMessage.getModifier();
			if (modifier != null && modifier.getKey().equals(identity.getKey())) {
				infoMessage.setModifier(null);
			}
			infoMessageManager.saveInfoMessage(infoMessage);
		}		
	}
	
	@Override
	public void removeInfoMessagesAndSubscriptionContext(BusinessGroup group) {
		List<InfoMessage> messages = infoMessageManager.loadInfoMessageByResource(group,
				InfoMessageFrontendManager.businessGroupResSubPath, null, null, null, 0, 0);
		List<String> pathToDelete = new ArrayList<>();
		for (InfoMessage im : messages) {
			infoMessageManager.deleteInfoMessage(im);
			if(StringHelper.containsNonWhitespace(im.getAttachmentPath())) {
				pathToDelete.add(im.getAttachmentPath());
			}
		}			
		String resName = group.getResourceableTypeName();
		Long resId = group.getResourceableId();
		SubscriptionContext subscriptionContext = new SubscriptionContext(resName, resId, "");
		infoSubscriptionManager.deleteSubscriptionContext(subscriptionContext);
		deleteAttachments(pathToDelete);
		// make sure all meta and version informations are deleted and the main directory
		deleteStorage(group);
	}

	@Override
	public void deleteStorage(OLATResourceable ores) {
		VFSContainer resourceContainer = getResourceContainer(ores);
		if(resourceContainer != null) {
			resourceContainer.deleteSilently();
		}
	}

	@Override
	public List<InfoMessage> loadInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before, int firstResult, int maxReturn) {
		return infoMessageManager.loadInfoMessageByResource(ores, subPath, businessPath, after, before, firstResult, maxReturn);
	}
	
	@Override
	public int countInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before) {
		return infoMessageManager.countInfoMessageByResource(ores, subPath, businessPath, after, before);
	}

	@Override
	public List<Identity> getInfoSubscribers(OLATResourceable resource, String subPath) {
		return infoSubscriptionManager.getInfoSubscribers(resource, subPath);
	}
}
