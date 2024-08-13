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
package org.olat.modules.cemedia.ui.medias;

import java.util.Arrays;
import java.util.List;

import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Roles;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.BlockLayoutClassFactory;
import org.olat.modules.ceditor.ui.ModalInspectorController;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ChangeVersionPartEvent;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.MediaMetadataController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.06.2016<br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FileMediaController extends BasicController implements PageElementEditorController {
	
	// Editing is excluded because we do not wand to use the internal editor at that place.
	private static final List<String> EDIT_EXCLUDED_SUFFIX = Arrays.asList("html", "htm", "txt");

	private VelocityContainer mainVC;
	private Link editLink;

	private Controller docEditorCtrl;

	private final Roles roles;
	private Media media;
	private MediaVersion version;
	private VFSMetadata metadata;
	private final RenderingHints hints;
	private VFSLeaf vfsLeaf;

	@Autowired
	private UserManager userManager;
	@Autowired
	private FolderModule folderModule;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private ContentEditorFileStorage fileStorage;

	public FileMediaController(UserRequest ureq, WindowControl wControl, PageElement pageElement, MediaVersion version, RenderingHints hints) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MediaCenterController.class, getLocale(), getTranslator()));
		this.roles = ureq.getUserSession().getRoles();
		this.media = version == null ? null : version.getMedia();
		this.metadata = version == null ? null : version.getMetadata();
		this.version = version;
		this.hints = hints;

		mainVC = createVelocityContainer("media_file");
		setBlockLayoutClass(pageElement);
		if(media != null) {
			String desc = media.getDescription();
			mainVC.contextPut("description", StringHelper.containsNonWhitespace(desc) ? desc : null);
			String title = media.getTitle();
			mainVC.contextPut("title", StringHelper.containsNonWhitespace(title) ? title : null);
			mainVC.contextPut("author", userManager.getUserDisplayName(media.getAuthor()));
	
			updateVersion(ureq);
	
			if (hints.isExtendedMetadata()) {
				MediaMetadataController metaCtrl = new MediaMetadataController(ureq, wControl, media);
				listenTo(metaCtrl);
				mainVC.put("meta", metaCtrl.getInitialComponent());
			}
		}

		mainVC.setDomReplacementWrapperRequired(false);
		putInitialPanel(mainVC);
	}

	private void setBlockLayoutClass(PageElement pageElement) {
		mainVC.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(pageElement, false));
	}

	private void updateVersion(UserRequest ureq) {
		mainVC.contextPut("filename", version.getContent());
		mainVC.contextPut("creationdate", version.getCollectionDate());
		
		VFSContainer container = fileStorage.getMediaContainer(version);
		VFSItem item = container.resolve(version.getRootFilename());
		if(metadata != null) {
			String filename = metadata.getFilename();
			
			mainVC.contextPut("filename", filename);
			mainVC.contextPut("size", Formatter.formatBytes(metadata.getFileSize()));
			
			String iconCss = CSSHelper.createFiletypeIconCssClassFor(filename);
			if (iconCss == null) {
				iconCss = "o_filetype_file";
			}
			mainVC.contextPut("fileIconCss", iconCss);
			mainVC.contextPut("cssClass", iconCss);
		}
		
		if (item instanceof VFSLeaf leaf) {
			vfsLeaf = leaf;
			VFSMediaMapper mapper = new VFSMediaMapper(vfsLeaf);
			// Force download if config o
			boolean forceDownload = folderModule.isForceDownload() || isEditingExcluded();
			mapper.setForceDownloadHtml(forceDownload);
			String mapperUri = registerCacheableMapper(ureq,
					"File-Media-" + media.getKey() + "-" + vfsLeaf.getLastModified(), mapper);
			mainVC.contextPut("mapperUri", mapperUri);
		} else {
			mainVC.contextRemove("mapperUri");
		}
		updateOpenLink();
	}

	private void updateOpenLink() {
		if (editLink != null) mainVC.remove(editLink);
		
		if (vfsLeaf != null && !hints.isToPdf() && !hints.isOnePage()) {
			DocEditorDisplayInfo editorInfo = getEditorDisplayInfo() ;
			if (editorInfo.isEditorAvailable()) {
				createEditLink(editorInfo, Mode.EDIT);
			} else {
				DocEditorDisplayInfo viewOnly = getEditorDisplayInfoViewOnly();
				if (viewOnly.isEditorAvailable()) {
					createEditLink(viewOnly, Mode.VIEW);
				}
				
			}
		}
	}
	
	private void createEditLink(DocEditorDisplayInfo editorInfo, Mode mode) {
		editLink = LinkFactory.createCustomLink("edit", "edit", "", Link.NONTRANSLATED | Link.BUTTON_XSMALL, mainVC, this);
		Translator buttonTranslator = Util.createPackageTranslator(DocEditorController.class, getLocale());
		editLink.setIconLeftCSS("o_icon o_icon-fw " + editorInfo.getModeIcon());
		editLink.setGhost(true);
		editLink.setCustomDisplayText(editorInfo.getModeButtonLabel(buttonTranslator));
		editLink.setUserObject(mode);
		if (editorInfo.isNewWindow()) {
			editLink.setNewWindow(true, true);
		}
	}
	
	private DocEditorDisplayInfo getEditorDisplayInfo() {
		if (hints.isEditable() && !isEditingExcluded() && mediaService.isMediaEditable(getIdentity(), media)) {
			return docEditorService.getEditorInfo(getIdentity(), roles, vfsLeaf, vfsLeaf.getMetaInfo(), true, DocEditorService.MODES_EDIT_VIEW);
		}
		return DocEditorDisplayInfo.noEditorAvailable();
	}
	
	private DocEditorDisplayInfo getEditorDisplayInfoViewOnly() {
		return docEditorService.getEditorInfo(getIdentity(), roles, vfsLeaf, vfsLeaf.getMetaInfo(), true, DocEditorService.MODES_VIEW);
	}

	private boolean isEditingExcluded() {
		String suffix = FileUtils.getFileSuffix(vfsLeaf.getName());
		return EDIT_EXCLUDED_SUFFIX.contains(suffix);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == editLink) {
			Mode mode = (Mode)editLink.getUserObject();
			doOpen(ureq, mode);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof ModalInspectorController && event instanceof ChangeVersionPartEvent cvpe) {
			PageElement element = cvpe.getElement();
			if (element instanceof MediaPart mediaPart) {
				media = mediaPart.getMedia();
				version = mediaPart.getMediaVersion();
				metadata = version == null ? null : version.getMetadata();
				updateVersion(ureq);
			}
		} else if (source instanceof ModalInspectorController && event instanceof ChangePartEvent changePartEvent) {
			setBlockLayoutClass(changePartEvent.getElement());
		} else if (source == docEditorCtrl) {
			removeAsListenerAndDispose(docEditorCtrl);
			docEditorCtrl = null;
		}
		super.event(ureq, source, event);
	}

	private void doOpen(UserRequest ureq, Mode mode) {
		VFSContainer container = fileStorage.getMediaContainer(version);
		VFSItem vfsItem = container.resolve(version.getRootFilename());
		if(vfsItem instanceof VFSLeaf docLeaf) {
			vfsLeaf = docLeaf;
			
			HTMLEditorConfig htmlEditorConfig = HTMLEditorConfig.builder(container, vfsItem.getName())
				.withAllowCustomMediaFactory(false)
				.withDisableMedia(true)
				.build();
			
			DocEditorConfigs configs = DocEditorConfigs.builder()
					.withMode(mode)
					.withFireSavedEvent(true)
					.addConfig(htmlEditorConfig)
					.build(vfsLeaf);
			// Force view only mode if needed
			List<Mode> modesAndFallback = mode == Mode.VIEW ? DocEditorService.MODES_VIEW : DocEditorService.MODES_EDIT_VIEW;
			docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, modesAndFallback).getController();
			listenTo(docEditorCtrl);
		} else {
			showError("error.missing.file");
		}
	}
}
