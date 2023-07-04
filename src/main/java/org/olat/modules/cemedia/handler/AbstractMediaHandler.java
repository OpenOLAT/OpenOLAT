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
package org.olat.modules.cemedia.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.DefaultGlobalSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.EmptyURLBuilder;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.velocity.VelocityRenderDecorator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.gui.util.WindowControlMocker;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.model.StoredData;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.PageRunControllerElement;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaHandlerVersion;
import org.olat.modules.cemedia.MediaInformations;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.MediaMetadataController;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractMediaHandler implements MediaHandler, PageElementHandler {

	private static final Logger log = Tracing.createLoggerFor(AbstractMediaHandler.class);
	
	protected static final Storage dataStorage = new Storage();
	
	private final String type;
	
	public AbstractMediaHandler(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
	}
	
	@Override
	public String getIconCssClass(MediaVersion mediaVersion) {
		return getIconCssClass();
	}

	@Override
	public MediaHandlerVersion hasVersion() {
		return new MediaHandlerVersion(false, false, null, false, null);
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, RenderingHints options) {
		if(element instanceof MediaPart mediaPart) {
			return new PageRunControllerElement(getMediaController(ureq, wControl, mediaPart.getMediaVersion(), options));
		}
		return null;
	}

	@Override
	public final Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		return null;
	}
	
	@Override
	public Controller getNewVersionController(UserRequest ureq, WindowControl wControl, Media media, CreateVersion createVersion) {
		return null;
	}

	/**
	 * A utility method to export media with content or a content component. The
	 * attached files are added to the output as a list of links.
	 * 
	 * 
	 * @param media The media to export
	 * @param contentCmp Optional, if not present with simply print the content of the media
	 * @param attachments Optional, a list of document to be attached to the media
	 * @param mediaArchiveDirectory The directory where all the medias are exported
	 * @param locale The language
	 */
	protected void exportContent(Media media, Component contentCmp, List<File> attachments, File mediaArchiveDirectory, Locale locale) {
		String title = StringHelper.transformDisplayNameToFileSystemName(media.getTitle());
		File mediaFile = new File(mediaArchiveDirectory, media.getKey() + "_" + title + ".html");
		try(OutputStream out = new FileOutputStream(mediaFile)) {
			String content = exportContent(media, contentCmp, attachments, locale);
			out.write(content.getBytes("UTF-8"));
			out.flush();
		} catch(IOException e) {
			log.error("", e);
		}
		
		if(attachments != null && !attachments.isEmpty()) {
			File attachmentsDir = new File(mediaArchiveDirectory, media.getKey().toString());
			attachmentsDir.mkdir();
			for(File attachment:attachments) {
				FileUtils.copyFileToDir(attachment, attachmentsDir, false, "Copy media artfacts");
			}
		}
	}

	private String exportContent(Media media, Component contentCmp, List<File> attachments, Locale locale) {
		try(StringOutput sb = new StringOutput(10000)) {
			Translator translator = Util.createPackageTranslator(MediaCenterController.class, locale);
			String pagePath = Util.getPackageVelocityRoot(AbstractMediaHandler.class) + "/export_content.html";
			VelocityContainer component = new VelocityContainer("html", pagePath, translator, null);
			component.contextPut("media", media);
			if(contentCmp != null) {
				component.put("contentCmp", contentCmp);
			} else {
				List<String> contents = new ArrayList<>();
				List<MediaVersion> versions = media.getVersions();
				for(MediaVersion version:versions) {
					if(version != null && StringHelper.containsNonWhitespace(version.getContent())) {
						contents.add(version.getContent());
					}
				}
				component.contextPut("contents", contents);
			}
			component.contextPut("attachments", attachments);
			
			SyntheticUserRequest ureq = new SyntheticUserRequest(null, locale);
			MediaMetadataController metadata = new MediaMetadataController(ureq, new WindowControlMocker(), media);
			component.put("metadata", metadata.getInitialComponent());
			render(sb, component, translator);
			return sb.toString();
		} catch(IOException e) {
			log.error("", e);
			return "ERROR";
		}
	}
	
	private void render(StringOutput sb, VelocityContainer component, Translator translator) {
		Renderer renderer = Renderer.getInstance(component, translator, new EmptyURLBuilder(), new RenderResult(), new DefaultGlobalSettings(), "-");
		try(VelocityRenderDecorator vrdec = new VelocityRenderDecorator(renderer, component, sb)) {
			component.contextPut("r", vrdec);
			renderer.render(sb, component, null);
		} catch(IOException e) {
			log.error("", e);
		}
	}

	public final class Informations implements MediaInformations {
		
		private final String title;
		private final String description;
		
		public Informations(String title, String description) {
			this.title = title;
			this.description = description;
		}
		
		@Override
		public String getType() {
			return AbstractMediaHandler.this.getType();
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getDescription() {
			return description;
		}
	}
	
	public static class MediaRenderingHints implements RenderingHints {
		
		private final RenderingHints options;
		
		public MediaRenderingHints(RenderingHints options) {
			this.options = options;
		}	

		@Override
		public boolean isToPdf() {
			return options.isToPdf();
		}

		@Override
		public boolean isOnePage() {
			return options.isOnePage();
		}

		@Override
		public boolean isExtendedMetadata() {
			return options.isExtendedMetadata();
		}

		@Override
		public boolean isEditable() {
			return options.isEditable();
		}
	}
	
	public static class Storage implements DataStorage {

		@Override
		public File getFile(StoredData storedData) {
			if(StringHelper.containsNonWhitespace(storedData.getStoragePath()) && StringHelper.containsNonWhitespace(storedData.getRootFilename())) {
				File mediaDir = new File(FolderConfig.getCanonicalRoot(), storedData.getStoragePath());
				return new File(mediaDir, storedData.getRootFilename());
			}
			return null;
		}

		@Override
		public StoredData save(String filename, File file, StoredData metadata) throws IOException {
			throw new OLATRuntimeException("Not implemented");
		}

		@Override
		public StoredData copy(StoredData original, StoredData copy) throws IOException {
			throw new OLATRuntimeException("Not implemented");
		}
	}
}
