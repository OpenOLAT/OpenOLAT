/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.peerreview;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorOpenInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.nodes.gta.ui.GTAParticipantController;
import org.olat.course.nodes.gta.ui.peerreview.GTADocumentsTableModel.DocumentCols;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.user.UserManager;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitSize;
import org.olat.user.UsersPortraitsComponent.PortraitUser;
import org.olat.user.UsersPortraitsFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTADocumentsController extends FormBasicController implements FlexiTableComponentDelegate, FlexiTableCssDelegate {
	
	private static final String CMD_SELECT = "select";
	
	private FlexiTableElement tableEl;
	private GTADocumentsTableModel tableModel;
	
	private int count = 0;
	private final Roles roles;
	private final boolean anonym;
	private final String placeholderName;
	private final VFSContainer container;
	private final MapperKey avatarMapperKey;
	
	private Controller docEditorCtrl;

	@Autowired
	private AVModule avModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private FolderModule folderModule;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public GTADocumentsController(UserRequest ureq, WindowControl wControl, MapperKey avatarMapperKey,
			VFSContainer container, String placeholderName, boolean anonym) {
		super(ureq, wControl, "documents", Util.createPackageTranslator(GTAParticipantController.class, ureq.getLocale()));
		this.anonym = anonym;
		this.container = container;
		this.placeholderName = placeholderName;
		this.avatarMapperKey = avatarMapperKey;
		roles = ureq.getUserSession().getRoles();
		
		initForm(ureq);
		loadModel(ureq);
		
		if(tableModel.getRowCount() >= 1 && tableModel.getRowCount() <= 5) {
			tableEl.setRendererType(FlexiTableRendererType.custom);
		} else {
			tableEl.setRendererType(FlexiTableRendererType.classic);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentCols.portraits));
		
		tableModel = new GTADocumentsTableModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "docs", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setElementCssClass("o_gta_document_list");
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("document_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		tableEl.setCssDelegate(this);
	}

	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return "o_table_wrapper o_table_flexi o_gta_file_list";
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return FlexiTableRendererType.custom == type
				? "o_gta_file_rows o_block_top o_gta_cards"
				: "o_gta_file_rows o_block_top";
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		return "o_gta_file_row";
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmpList = new ArrayList<>();
		GTADocumentRow documentRow = tableModel.getObject(row);
		if(documentRow.getUserPortraits() != null) {
			cmpList.add(documentRow.getUserPortraits());
		}
		if(documentRow.getSelectLink() != null) {
			cmpList.add(documentRow.getSelectLink().getComponent());
		}
		if(documentRow.getSelectClassicLink() != null) {
			cmpList.add(documentRow.getSelectClassicLink().getComponent());
		}
		return cmpList;
	}

	private void loadModel(UserRequest ureq) {
		List<VFSItem> items = container.getItems(new VFSSystemItemFilter());
		List<GTADocumentRow> rows = new ArrayList<>(items.size());
		
		for(VFSItem item:items) {
			if(item instanceof VFSLeaf leaf) {
				VFSMetadata metadata = leaf.getMetaInfo();
				
				String id = "doc-" + (++count);
				GTADocumentRow row = new GTADocumentRow(id, leaf, metadata, anonym);
				if(anonym) {
					row.setAuthorName(placeholderName);
					TransientIdentity anonymId = new TransientIdentity();
					forgeUsersPortraits(ureq, row, anonymId);
				} else {
					Identity initializedBy = null;
					if(metadata != null) {
						initializedBy = metadata.getFileInitializedBy();
					}
					row.setAuthorName(userManager.getUserDisplayName(initializedBy));
					forgeUsersPortraits(ureq, row, initializedBy);
				}
				forgeSelectLink(row, leaf, metadata, roles);
				
				if (isThumbnailAvailable(leaf, metadata)) {
					VFSLeaf thumbnail = getThumbnail(leaf);
					if (thumbnail != null) {
						row.setThumbnailAvailable(true);
						VFSMediaMapper thumbnailMapper = new VFSMediaMapper(thumbnail);
						String thumbnailUrl = registerCacheableMapper(ureq, null, thumbnailMapper);
						row.setThumbnailUrl(thumbnailUrl);
					}
				}
				
				rows.add(row);
			}
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void forgeSelectLink(GTADocumentRow row, VFSLeaf vfsLeaf, VFSMetadata vfsMetadata, Roles roles) {
		String id = Integer.toString(++count);
		FormLink link = uifactory.addFormLink("select_" + id, CMD_SELECT, "", null, flc, Link.LINK + Link.NONTRANSLATED);
		FormLink classicLink = uifactory.addFormLink("selectc_" + id, CMD_SELECT, "", null, flc, Link.LINK + Link.NONTRANSLATED);
		
		link.setElementCssClass("o_link_plain");
		
		link.setI18nKey(StringHelper.escapeHtml(row.getDisplayName()));
		classicLink.setI18nKey(StringHelper.escapeHtml(row.getDisplayName()));
		
		String iconCSS = CSSHelper.createFiletypeIconCssClassFor(vfsMetadata.getFilename());
		link.setIconLeftCSS("o_icon " + iconCSS);
		
		DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), roles, vfsLeaf,
				vfsMetadata, true, DocEditorService.MODES_VIEW);
		if (editorInfo.isNewWindow() && !folderModule.isForceDownload(vfsLeaf)) {
			link.setNewWindow(true, true, false);
			classicLink.setNewWindow(true, true, false);
			row.setOpenInNewWindow(true);
		}

		link.setUserObject(row);
		classicLink.setUserObject(row);
		row.setSelectLink(link);
		row.setSelectClassicLink(classicLink);
	}
	
	private void forgeUsersPortraits(UserRequest ureq, GTADocumentRow row, Identity initializedBy) {
		List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(List.of(initializedBy));
		UsersPortraitsComponent usersPortraitCmp = UsersPortraitsFactory.create(ureq, "users_" + (++count), flc.getFormItemComponent(), null, avatarMapperKey);
		usersPortraitCmp.setAriaLabel(translate("member.list.aria"));
		usersPortraitCmp.setSize(PortraitSize.small);
		usersPortraitCmp.setMaxUsersVisible(5);
		usersPortraitCmp.setUsers(portraitUsers);
		row.setUserPortraits(usersPortraitCmp);
	}
	
	private boolean isThumbnailAvailable(VFSLeaf vfsLeaf, VFSMetadata vfsMetadata) {
		if (isAudio(vfsLeaf)) {
			return true;
		}
		if (vfsLeaf.getSize() == 0) {
			return false;
		}
		return vfsRepositoryService.isThumbnailAvailable(vfsLeaf, vfsMetadata);
	}

	private boolean isAudio(VFSLeaf vfsLeaf) {
		if ("m4a".equalsIgnoreCase(FileUtils.getFileSuffix(vfsLeaf.getRelPath()))) {
			return true;
		}
		return false;
	}

	private VFSLeaf getThumbnail(VFSLeaf vfsLeaf) {
		if (isAudio(vfsLeaf)) {
			return vfsRepositoryService.getLeafFor(avModule.getAudioWaveformUrl());
		}
		return vfsRepositoryService.getThumbnail(vfsLeaf, 650, 1000, false);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(docEditorCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(docEditorCtrl);
		docEditorCtrl = null;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String fileId = ureq.getParameter("select_file");
			if(StringHelper.containsNonWhitespace(fileId)) {
				GTADocumentRow row = tableModel.getObjectById(fileId);
				if(row != null) {
					doOpenOrDownload(ureq, row);
					return;
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink link) {
			if (CMD_SELECT.equals(link.getCmd()) && link.getUserObject() instanceof GTADocumentRow row) {
				doOpenOrDownload(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenOrDownload(UserRequest ureq, GTADocumentRow row) {
		VFSLeaf vfsLeaf = row.getDocument();
		if(folderModule.isForceDownload(vfsLeaf)) {
			doDownload(ureq, vfsLeaf);
		} else {
			VFSMetadata vfsMetadata = row.getDocumentMetadata();
			DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(),
					ureq.getUserSession().getRoles(), vfsLeaf, vfsMetadata,
					true, DocEditorService.MODES_VIEW);
			if (editorInfo.isEditorAvailable()) {
				doOpenFile(ureq, vfsLeaf);
			} else {
				doDownload(ureq, vfsLeaf);
			}
		}
	}
	
	private void doDownload(UserRequest ureq, VFSLeaf vfsLeaf) {
		VFSMediaResource resource = new VFSMediaResource(vfsLeaf);
		resource.setDownloadable(true);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doOpenFile(UserRequest ureq, VFSLeaf vfsLeaf) {
		HTMLEditorConfig htmlEditorConfig = HTMLEditorConfig.builder(container, vfsLeaf.getName())
				.withAllowCustomMediaFactory(false)
				.withDisableMedia(true)
				.build();
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withFireSavedEvent(true)
				.addConfig(htmlEditorConfig)
				.build(vfsLeaf);
		DocEditorOpenInfo docEditorOpenInfo = docEditorService.openDocument(ureq, getWindowControl(), configs,
				DocEditorService.MODES_VIEW);
		docEditorCtrl = listenTo(docEditorOpenInfo.getController());
	}
}
