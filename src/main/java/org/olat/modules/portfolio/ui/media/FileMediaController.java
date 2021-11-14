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
package org.olat.modules.portfolio.ui.media;

import java.util.Arrays;
import java.util.List;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.CommandFactory;
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
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaRenderingHints;
import org.olat.modules.portfolio.manager.PortfolioFileStorage;
import org.olat.modules.portfolio.ui.MediaMetadataController;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
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

	private final Roles roles;
	private final Media media;
	private final MediaRenderingHints hints;
	private VFSLeaf vfsLeaf;
	private boolean editMode = false;

	@Autowired
	private PortfolioFileStorage fileStorage;

	@Autowired
	private UserManager userManager;
	@Autowired
	private DocEditorService docEditorService;
	
	public FileMediaController(UserRequest ureq, WindowControl wControl, Media media, MediaRenderingHints hints) {
		super(ureq, wControl);
		this.roles = ureq.getUserSession().getRoles();
		this.media = media;
		this.hints = hints;
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale(), getTranslator()));

		mainVC = createVelocityContainer("media_file");
		mainVC.contextPut("filename", media.getContent());
		String desc = media.getDescription();
		mainVC.contextPut("description", StringHelper.containsNonWhitespace(desc) ? desc : null);
		String title = media.getTitle();
		mainVC.contextPut("title", StringHelper.containsNonWhitespace(title) ? title : null);

		mainVC.contextPut("creationdate", media.getCreationDate());
		mainVC.contextPut("author", userManager.getUserDisplayName(media.getAuthor()));

		VFSContainer container = fileStorage.getMediaContainer(media);
		VFSItem item = container.resolve(media.getRootFilename());
		if (item instanceof VFSLeaf) {
			vfsLeaf = (VFSLeaf) item;
			String mapperUri = registerCacheableMapper(ureq,
					"File-Media-" + media.getKey() + "-" + vfsLeaf.getLastModified(), new VFSMediaMapper(vfsLeaf));
			mainVC.contextPut("mapperUri", mapperUri);
			String iconCss = CSSHelper.createFiletypeIconCssClassFor(vfsLeaf.getName());
			mainVC.contextPut("fileIconCss", iconCss);
			mainVC.contextPut("filename", vfsLeaf.getName());
			mainVC.contextPut("size", Formatter.formatBytes(((VFSLeaf) item).getSize()));

			String cssClass = CSSHelper.createFiletypeIconCssClassFor(item.getName());
			if (cssClass == null) {
				cssClass = "o_filetype_file";
			}
			mainVC.contextPut("cssClass", cssClass);

			updateUI();
		}

		if (hints.isExtendedMetadata()) {
			MediaMetadataController metaCtrl = new MediaMetadataController(ureq, wControl, media);
			listenTo(metaCtrl);
			mainVC.put("meta", metaCtrl.getInitialComponent());
		}

		mainVC.setDomReplacementWrapperRequired(false);
		putInitialPanel(mainVC);
	}

	private void updateUI() {
		updateOpenLink();
	}

	private void updateOpenLink() {
		if (editLink != null) mainVC.remove(editLink);
		
		if (vfsLeaf != null && !hints.isToPdf()) {
			Mode mode = getMode();
			if (mode != null) {
				editLink = LinkFactory.createCustomLink("edit", "edit", "", Link.NONTRANSLATED | Link.LINK, mainVC,
						this);
				String editIcon = Mode.EDIT.equals(mode)? "o_icon_edit": "o_icon_preview";
				editLink.setIconLeftCSS("o_icon " + editIcon);
				editLink.setUserObject(mode);
				editLink.setNewWindow(true, true);
			}
		}
	}

	@Override
	public boolean isEditMode() {
		return editMode;
	}

	@Override
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
		updateUI();
	}
	
	private Mode getMode() {
		if (isEditingExcluded()) {
			return null;
		} else if (editMode && docEditorService.hasEditor(getIdentity(), roles, vfsLeaf, Mode.EDIT, true)) {
			return Mode.EDIT;
		} else if (docEditorService.hasEditor(getIdentity(), roles, vfsLeaf, Mode.VIEW, true)) {
			return Mode.VIEW;
		}
		return null;
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

	private void doOpen(UserRequest ureq, Mode mode) {
		VFSContainer container = fileStorage.getMediaContainer(media);
		VFSItem vfsItem = container.resolve(media.getRootFilename());
		if(vfsItem == null || !(vfsItem instanceof VFSLeaf)) {
			showError("error.missing.file");
		} else {
			DocEditorConfigs configs = DocEditorConfigs.builder()
					.withMode(mode)
					.build(vfsLeaf);
			String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), configs);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		}
	}
}
