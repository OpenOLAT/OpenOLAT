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
package org.olat.modules.fo.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.DefaultGlobalSettings;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.EmptyURLBuilder;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.velocity.VelocityRenderDecorator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumModule;
import org.olat.modules.fo.Message;
import org.olat.modules.wiki.WikiManager;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserDataExportable;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ForumUserDataManager implements UserDataExportable {
	
	private static final OLog log = Tracing.createLoggerFor(ForumUserDataManager.class);

	public static final String FORUM_KEY = "forumKey";// course and group
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	@Override
	public String getExporterID() {
		return "forums";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		List<Message> messages = forumManager.getMessageByCreator(identity);
		dbInstance.commitAndCloseSession();
		if(messages.isEmpty()) return;


		File forumArchive = new File(archiveDirectory, "Forums");
		forumArchive.mkdir();
		Map<Long,Property> properties = loadMappingProperties();
		Map<Long, Location> locations = new HashMap<>();
		Translator translator = Util.createPackageTranslator(ForumModule.class, locale);
		int count = 1;
		for(Message message:messages) {
			String name = count++ + "_" + StringHelper.transformDisplayNameToFileSystemName(message.getTitle());

			Forum forum = message.getForum();
			Location location = locations.computeIfAbsent(forum.getKey(), forumKey -> resolveLocation(forumKey, properties));
			
			List<File> attachments;
			File msgContainer = forumManager.getMessageDirectory(forum.getKey(), message.getKey(), false);
			if(msgContainer != null && msgContainer.exists()) {
				File[] attachmentArr = msgContainer.listFiles(SystemFileFilter.FILES_ONLY);
				attachments = Arrays.asList(attachmentArr);
				if(!attachments.isEmpty()) {
					File messageAttachementDir = new File(forumArchive, name);
					messageAttachementDir.mkdir();
					for(File attachment:attachments) {
						FileUtils.copyFileToDir(attachment, messageAttachementDir, "Copy forum message attachments");
					}
				}
			} else {
				attachments = Collections.emptyList();
			}
			
			File messageFile = new File(forumArchive, name + ".html");
			try(OutputStream out = new FileOutputStream(messageFile)) {
				String content = renderForumMessage(location, message, attachments, translator);
				out.write(content.getBytes("UTF-8"));
				out.flush();
			} catch(IOException e) {
				log.error("", e);
			}
		}

	}
	
	private Location resolveLocation(Long forumKey, Map<Long,Property> properties) {
		Property property = properties.get(forumKey);
		String name = null;
		String businessPath = null;
		if(property != null) {
			if("CourseModule".equals(property.getResourceTypeName())) {
				OLATResourceable resourceable = OresHelper.createOLATResourceableInstance(property.getResourceTypeName(), property.getResourceTypeId());
				RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(resourceable, false);
				if(entry != null) {
					name = entry.getDisplayname();
					businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
					String nodeId = extractNodeId(property);
					if(nodeId != null) {
						businessPath += "[CourseNode:" + nodeId + "]";
					}
				}	
			} else if("BusinessGroup".equals(property.getResourceTypeName())) {
				BusinessGroup businessGroup = businessGroupService.loadBusinessGroup(property.getResourceTypeId());
				if(businessGroup != null) {
					name = businessGroup.getName();
					businessPath += "[BusinessGroup:" + businessGroup.getKey() + "]";
				}
			}
		}
		
		String url = businessPath == null ? null : BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
		return new Location(name, url);
	}
	
	private String extractNodeId(Property property) {
		if(property.getCategory() != null) {
			String category = property.getCategory();
			int index = category.indexOf("::");
			if(index > 0 && index + 2 < category.length()) {
				return category.substring(index + 2, category.length());
			}
		}	
		return null;
	}
	
	private Map<Long,Property> loadMappingProperties() {
		Map<Long,Property> propertyMap = new HashMap<>();
		List<Property> properties = propertyManager.listProperties(null, null, null, null, null, FORUM_KEY);
		for(Property property:properties) {
			propertyMap.put(property.getLongValue(), property);
		}
		
		List<Property> wikiProperties = propertyManager.listProperties(null, null, null, null, null, WikiManager.FORUM_KEY);
		for(Property property:wikiProperties) {
			propertyMap.put(property.getLongValue(), property);
		}
		return propertyMap;
	}

	
	private String renderForumMessage(Location location, Message message, List<File> attachments, Translator translator) {
		StringOutput sb = new StringOutput(10000);
		String pagePath = Util.getPackageVelocityRoot(ForumUserDataManager.class) + "/export_message.html";
		VelocityContainer component = new VelocityContainer("html", pagePath, translator, null);
		component.contextPut("message", message);
		component.contextPut("attachments", attachments);
		component.contextPut("location", location);
		
		Renderer renderer = Renderer.getInstance(component, translator, new EmptyURLBuilder(), new RenderResult(), new DefaultGlobalSettings());
		VelocityRenderDecorator vrdec = new VelocityRenderDecorator(renderer, component, sb);
		component.contextPut("r", vrdec);
		renderer.render(sb, component, null);
		return sb.toString();
	}
	
	public static class Location {
		private final String name;
		private final String url;
		
		public Location(String name, String url) {
			this.name = name;
			this.url = url;
		}

		public String getName() {
			return name;
		}

		public String getUrl() {
			return url;
		}
	}
}
