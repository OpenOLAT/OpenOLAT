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
package org.olat.modules.fo.portfolio;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaInformations;
import org.olat.modules.cemedia.MediaLoggingAction;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.handler.AbstractMediaHandler;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.ui.medias.StandardEditMediaController;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.MessageLight;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.user.manager.ManifestBuilder;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ForumMediaHandler extends AbstractMediaHandler {
	
	public static final String FORUM_HANDLER = OresHelper.calculateTypeName(Forum.class);
	
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	
	public ForumMediaHandler() {
		super(FORUM_HANDLER);
	}

	@Override
	public String getIconCssClass() {
		return "o_fo_icon";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.content;
	}
	
	@Override
	public boolean acceptMimeType(String mimeType) {
		return false;
	}

	@Override
	public VFSLeaf getThumbnail(MediaVersion media, Size size) {
		return null;
	}
	
	@Override
	public MediaInformations getInformations(Object mediaObject) {
		String title = null;
		if(mediaObject instanceof MessageLight messageLight) {
			title = messageLight.getTitle();
		}
		return new Informations(title, null);
	}

	@Override
	public Media createMedia(String title, String description, String altText, Object mediaObject, String businessPath, Identity author) {
		Message message = null;
		if(mediaObject instanceof Message msg) {
			message = forumManager.loadMessage(msg.getKey());
		} else if(mediaObject instanceof MessageLight messageLight) {
			message = forumManager.loadMessage(messageLight.getKey());
		}
		
		Media media = mediaDao.createMedia(title, description, altText, FORUM_HANDLER, businessPath, null, 70, author);
		ThreadLocalUserActivityLogger.log(MediaLoggingAction.CE_MEDIA_ADDED, getClass(),
				LoggingResourceable.wrap(media));
		
		String storagePath = null;
		String content = message.getBody();
		
		File messageDir = forumManager.getMessageDirectory(message.getForum().getKey(), message.getKey(), false);
		if (messageDir != null && messageDir.exists()) {
			File[] attachments = messageDir.listFiles();
			if (attachments.length > 0) {
				File mediaDir = fileStorage.generateMediaSubDirectory(media);
				for(File attachment:attachments) {
					FileUtils.copyFileToDir(attachment, mediaDir, "Forum media");
				}
				storagePath = fileStorage.getRelativePath(mediaDir);
			}
		}

		media = mediaDao.createVersion(media, new Date(), content, storagePath, null);
		return media;
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, MediaVersion version, RenderingHints hints) {
		return new ForumMessageMediaController(ureq, wControl, version, hints);
	}

	@Override
	public Controller getEditMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		return new StandardEditMediaController(ureq, wControl, media);
	}
	
	@Override
	public Controller getEditMetadataController(UserRequest ureq, WindowControl wControl, Media media) {
		return new StandardEditMediaController(ureq, wControl, media);
	}

	@Override
	public void export(Media media, ManifestBuilder manifest, File mediaArchiveDirectory, Locale locale) {
		List<MediaVersion> versions = media.getVersions();
		List<File> attachments = new ArrayList<>();
		if(!versions.isEmpty() && StringHelper.containsNonWhitespace(versions.get(0).getStoragePath())) {
			File mediaDir = fileStorage.getMediaDirectory(versions.get(0));
			if(mediaDir != null && mediaDir.exists()) {
				File[] attachmentArr = mediaDir.listFiles(SystemFileFilter.FILES_ONLY);
				attachments = Arrays.asList(attachmentArr);
			}
		}
		super.exportContent(media, null, attachments, mediaArchiveDirectory, locale);
	}
}
