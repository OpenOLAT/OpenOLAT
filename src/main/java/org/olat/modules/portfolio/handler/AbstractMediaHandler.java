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
package org.olat.modules.portfolio.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.model.StoredData;
import org.olat.modules.ceditor.ui.PageRunControllerElement;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.MediaInformations;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.MediaRenderingHints;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.model.StandardMediaRenderingHints;
import org.olat.modules.portfolio.ui.MediaCenterController;
import org.olat.modules.portfolio.ui.MediaMetadataController;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractMediaHandler implements MediaHandler, PageElementHandler {

	private static final Logger log = Tracing.createLoggerFor(AbstractMediaHandler.class);
	
	protected static final PortfolioStorage dataStorage = new PortfolioStorage();
	
	private final String type;
	
	public AbstractMediaHandler(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
	}
	
	@Override
	public String getIconCssClass(MediaLight media) {
		return getIconCssClass();
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, PageElementRenderingHints options) {
		if(element instanceof Media) {
			return new PageRunControllerElement(getMediaController(ureq, wControl, (Media)element, new RenderingHints(options)));
		}
		if(element instanceof MediaPart) {
			MediaPart mediaPart = (MediaPart)element;
			return new PageRunControllerElement(getMediaController(ureq, wControl, mediaPart.getMedia(), new RenderingHints(options)));
		}
		return null;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof Media) {
			return getMediaController(ureq, wControl, (Media)element, new StandardMediaRenderingHints());
		}
		if(element instanceof MediaPart) {
			MediaPart mediaPart = (MediaPart)element;
			return getMediaController(ureq, wControl, mediaPart.getMedia(), new StandardMediaRenderingHints());
		}
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
				component.contextPut("content", media.getContent());
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
	
	public static class RenderingHints implements MediaRenderingHints, PageElementRenderingHints {
		
		private final PageElementRenderingHints options;
		
		public RenderingHints(PageElementRenderingHints options) {
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
	}
	
	public static class PortfolioStorage implements DataStorage {

		@Override
		public File getFile(StoredData storedData) {
			File mediaDir = new File(FolderConfig.getCanonicalRoot(), storedData.getStoragePath());
			return new File(mediaDir, storedData.getRootFilename());
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
