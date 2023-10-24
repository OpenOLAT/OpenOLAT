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
package org.olat.course.nodes.gta.ui;

import static org.olat.course.nodes.gta.ui.GTAUIFactory.officeHtml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.vfs.manager.VFSTranscodingDoneEvent;
import org.olat.core.commons.services.video.ui.VideoAudioPlayerController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.model.Solution;
import org.olat.course.nodes.gta.ui.SolutionTableModel.SolCols;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class GTASampleSolutionsEditController extends FormBasicController implements Activateable2, GenericEventListener {

	private FormLink addSolutionLink;
	private FormLink createSolutionLink;
	private FormLink recordVideoLink;
	private FormLink recordAudioLink;
	private SolutionTableModel solutionModel;
	private FlexiTableElement solutionTable;
	private DropdownItem createSolutionDropdown;
	
	private CloseableModalController cmc;
	private EditSolutionController addSolutionCtrl;
	private EditSolutionController editSolutionCtrl;
	private NewSolutionController newSolutionCtrl;
	private AVSampleSolutionController avSampleSolutionController;
	private CloseableCalloutWindowController ccwc;
	private AVConvertingMenuController avConvertingMenuCtrl;
	private VideoAudioPlayerController videoAudioPlayerController;
	private Controller docEditorCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;

	private final File solutionDir;
	private final boolean readOnly;
	private final GTACourseNode gtaNode;
	private final CourseEnvironment courseEnv;
	private final VFSContainer solutionContainer;
	private final Long courseRepoKey;
	
	private int linkCounter = 0;
	private Roles roles;

	@Autowired
	private UserManager userManager;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private AVModule avModule;
	@Autowired
	private VFSTranscodingService transcodingService;
	
	public GTASampleSolutionsEditController(UserRequest ureq, WindowControl wControl, GTACourseNode gtaNode,
			CourseEnvironment courseEnv, boolean readOnly) {
		super(ureq, wControl, "edit_solution_list", Util.createPackageTranslator(DocEditorController.class, ureq.getLocale()));
		this.gtaNode = gtaNode;
		this.readOnly = readOnly;
		this.courseEnv = courseEnv;
		this.courseRepoKey = courseEnv.getCourseGroupManager().getCourseEntry().getKey();
		solutionDir = gtaManager.getSolutionsDirectory(courseEnv, gtaNode);
		solutionContainer = gtaManager.getSolutionsContainer(courseEnv, gtaNode);
		initForm(ureq);

		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, VFSTranscodingService.ores);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addSolutionLink = uifactory.addFormLink("add.solution", formLayout, Link.BUTTON);
		addSolutionLink.setElementCssClass("o_sel_course_gta_add_solution");
		addSolutionLink.setIconLeftCSS("o_icon o_icon_upload");
		addSolutionLink.setVisible(!readOnly);
		createSolutionLink = uifactory.addFormLink("create.solution", formLayout, Link.BUTTON);
		createSolutionLink.setElementCssClass("o_sel_course_gta_create_solution");
		createSolutionLink.setIconLeftCSS("o_icon o_icon_edit");
		createSolutionLink.setVisible(!readOnly);

		createSolutionDropdown = uifactory.addDropdownMenu("create.solution.dropdown", null, null, formLayout, getTranslator());
		createSolutionDropdown.setOrientation(DropdownOrientation.right);
		createSolutionDropdown.setElementCssClass("o_sel_add_more");
		createSolutionDropdown.setEmbbeded(true);
		createSolutionDropdown.setButton(true);
		createSolutionDropdown.setVisible(false);

		recordVideoLink = uifactory.addFormLink("av.record.video", formLayout, Link.LINK);
		recordVideoLink.setElementCssClass("o_sel_course_gta_record_video");
		recordVideoLink.setIconLeftCSS("o_icon o_icon_video_record");
		recordVideoLink.setVisible(!readOnly && avModule.isVideoRecordingEnabled());
		createSolutionDropdown.addElement(recordVideoLink);
		recordAudioLink = uifactory.addFormLink("av.record.audio", formLayout, Link.LINK);
		recordAudioLink.setElementCssClass("o_sel_course_gta_record_audio");
		recordAudioLink.setIconLeftCSS("o_icon o_icon_audio_record");
		recordAudioLink.setVisible(!readOnly && avModule.isAudioRecordingEnabled());
		createSolutionDropdown.addElement(recordAudioLink);
		if (recordVideoLink.isVisible() || recordAudioLink.isVisible()) {
			createSolutionDropdown.setVisible(true);
		}

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SolCols.title.i18nKey(), SolCols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SolCols.file.i18nKey(), SolCols.file.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SolCols.author.i18nKey(), SolCols.author.ordinal()));
		
		if(!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SolCols.edit.i18nKey(), SolCols.edit.ordinal()));
			DefaultFlexiColumnModel toolsFlexiColumnModel = new DefaultFlexiColumnModel(SolCols.toolsLink.i18nKey(), SolCols.toolsLink.ordinal());
			toolsFlexiColumnModel.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(toolsFlexiColumnModel);
		}

		solutionModel = new SolutionTableModel(columnsModel);
		solutionTable = uifactory.addTableElement(getWindowControl(), "table", solutionModel, getTranslator(), formLayout);
		solutionTable.setExportEnabled(true);
		updateModel(ureq);
	}
	
	private void updateModel(UserRequest ureq) {
		if (ureq != null) {
			roles = ureq.getUserSession().getRoles();
		}
		List<Solution> solutionList = gtaManager.getSolutions(courseEnv, gtaNode);
		List<SolutionRow> rows = new ArrayList<>(solutionList.size());
		for(Solution solution:solutionList) {
			String filename = solution.getFilename();
			String author = null;
			
			VFSItem item = solutionContainer.resolve(filename);
			solution.setInTranscoding(false);
			
			DownloadLink downloadLink = null;
			FormLink openLink = null;
			FormLink toolsLink = null;
			FormLink editLink = null;
			FormLink documentLink = null;
			if(item instanceof VFSLeaf vfsLeaf && item.canMeta() == VFSConstants.YES) {
				VFSMetadata metaInfo = item.getMetaInfo();
				if (metaInfo.getFileInitializedBy() != null) {
					author = userManager.getUserDisplayName(metaInfo.getFileInitializedBy());
				}
				solution.setInTranscoding(metaInfo.isInTranscoding());

				String iconFilename = "";
				if (solution.isInTranscoding()) {
					openLink = uifactory.addFormLink("transcoding_" + (++linkCounter), "transcoding", "av.converting", null, flc, Link.LINK);
					openLink.setUserObject(solution);
					documentLink = uifactory.addFormLink("transcoding_" + (++linkCounter), "transcoding", "av.converting", null, flc, Link.LINK);
					documentLink.setUserObject(solution);
				} else {
					downloadLink = uifactory.addDownloadLink("file_" + (++linkCounter), filename, null, vfsLeaf, solutionTable);
					downloadLink.setUserObject(vfsLeaf);
					
					DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), roles, vfsLeaf,
							metaInfo, true, DocEditorService.modesEditView(!readOnly));
					if (editorInfo.isEditorAvailable()) {
						iconFilename = "<i class=\"o_icon o_icon-fw " + CSSHelper.createFiletypeIconCssClassFor(filename) + "\"></i> " + filename;
						documentLink = uifactory.addFormLink("open_" + (++linkCounter), "open", iconFilename, null, flc, Link.LINK | Link.NONTRANSLATED);
						openLink = uifactory.addFormLink("open_" + (++linkCounter), "open", "", null, flc, Link.LINK | Link.NONTRANSLATED);
						openLink.setGhost(true);
						openLink.setI18nKey(editorInfo.getModeButtonLabel(getTranslator()));
						openLink.setIconLeftCSS("o_icon o_icon-fw " + editorInfo.getModeIcon());
						if (editorInfo.isNewWindow()) {
							openLink.setNewWindow(true, true, false);
							documentLink.setNewWindow(true, true, false);
						}
						openLink.setUserObject(solution);
						documentLink.setUserObject(solution);
					}
				}
				if(!readOnly) {
					editLink = uifactory.addFormLink("edit_" + (++linkCounter), "editEntry", translate("table.header.metadata"), "", null, Link.NONTRANSLATED);
					editLink.setTooltip(translate("edit"));
					editLink.setUserObject(solution);
				}
				toolsLink = uifactory.addFormLink("tools_" + (++linkCounter), "tools", translate("table.header.action"), null, null, Link.NONTRANSLATED);
			}

			rows.add(new SolutionRow(solution, author, downloadLink, openLink, documentLink, editLink, toolsLink));
		}
		solutionModel.setObjects(rows);
		solutionTable.reset();
	}

	private void doOpenTools(UserRequest ureq, FormLink link) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), (SolutionRow) link.getUserObject());
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	@Override
	public void event(Event event) {
		if (event instanceof VFSTranscodingDoneEvent doneEvent) {
			if (solutionModel.getObjects().stream().anyMatch(s -> doneEvent.getFileName().equals(s.solution().getFilename()))) {
				updateModel(null);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addSolutionCtrl == source) {
			if(event == Event.DONE_EVENT) {
				Solution newSolution = addSolutionCtrl.getSolution();
				gtaManager.addSolution(newSolution, courseEnv, gtaNode);
				fireEvent(ureq, Event.DONE_EVENT);
				updateModel(ureq);
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editSolutionCtrl == source) {
			if(event == Event.DONE_EVENT) {
				gtaManager.updateSolution(editSolutionCtrl.getFilenameToReplace(), editSolutionCtrl.getSolution(), courseEnv, gtaNode);
				fireEvent(ureq, Event.DONE_EVENT);
				updateModel(ureq);
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
		} else if(newSolutionCtrl == source) {
			Solution newSolution = newSolutionCtrl.getSolution();
			cmc.deactivate();
			cleanUp();
			
			if(event == Event.DONE_EVENT) {
				gtaManager.addSolution(newSolution, courseEnv, gtaNode);
				fireEvent(ureq, Event.DONE_EVENT);
				updateModel(ureq);
				gtaManager.markNews(courseEnv, gtaNode);
			}
		} else if(docEditorCtrl == source) {
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if (avSampleSolutionController == source) {
			if (event == Event.DONE_EVENT) {
				gtaManager.addSolution(avSampleSolutionController.getSolution(), courseEnv, gtaNode);
				fireEvent(ureq, Event.DONE_EVENT);
				updateModel(ureq);
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
		} else if (ccwc == source) {
			cleanUp();
		} else if (avConvertingMenuCtrl == source) {
			if (event == AVConvertingMenuController.PLAY_MASTER_EVENT) {
				Solution solution = (Solution) avConvertingMenuCtrl.getUserObject();
				ccwc.deactivate();
				cleanUp();
				doPlayMaster(ureq, solution);
			}
		} else if (videoAudioPlayerController == source) {
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editSolutionCtrl);
		removeAsListenerAndDispose(addSolutionCtrl);
		removeAsListenerAndDispose(avSampleSolutionController);
		removeAsListenerAndDispose(avConvertingMenuCtrl);
		removeAsListenerAndDispose(videoAudioPlayerController);
		removeAsListenerAndDispose(docEditorCtrl);
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		editSolutionCtrl = null;
		addSolutionCtrl = null;
		avSampleSolutionController = null;
		avConvertingMenuCtrl = null;
		videoAudioPlayerController = null;
		docEditorCtrl = null;
		cmc = null;
		ccwc = null;
		toolsCtrl = null;
		toolsCalloutCtrl = null;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			cleanUp();
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addSolutionLink == source) {
			doAddSolution(ureq);
		} else if(createSolutionLink == source) {
			doCreateSolution(ureq);
		} else if (recordVideoLink == source) {
			doRecordVideo(ureq);
		} else if (recordAudioLink == source) {
			doRecordAudio(ureq);
		} else if (source instanceof FormLink link) {
			if (link.getUserObject() instanceof Solution solution) {
				if ("open".equalsIgnoreCase(link.getCmd())) {
					doOpenMedia(ureq, solution);
				} else if ("transcoding".equalsIgnoreCase(link.getCmd())) {
					doOpenTranscoding(ureq, link, solution);
				}
				if("editEntry".equalsIgnoreCase(link.getCmd())) {
					doEditMetadata(ureq, solution);
				}
			}
			if ("tools".equalsIgnoreCase(link.getCmd())) {
				doOpenTools(ureq, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenTranscoding(UserRequest ureq, FormLink link, Solution solution) {
		if (guardModalController(avConvertingMenuCtrl)) return;
		
		avConvertingMenuCtrl = new AVConvertingMenuController(ureq, getWindowControl(), solution);
		listenTo(avConvertingMenuCtrl);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(),
				avConvertingMenuCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}
	
	private void doPlayMaster(UserRequest ureq, Solution solution) {
		doOpenMedia(ureq, solution);
	}

	private void doOpenMedia(UserRequest ureq, Solution solution) {
		VFSItem vfsItem = solutionContainer.resolve(solution.getFilename());
		if(vfsItem instanceof VFSLeaf vfsLeaf) {
			gtaManager.markNews(courseEnv, gtaNode);
			DocEditorConfigs configs = GTAUIFactory.getEditorConfig(solutionContainer, vfsLeaf, solution.getFilename(), Mode.EDIT, courseRepoKey);
			docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.modesEditView(!readOnly)).getController();
			listenTo(docEditorCtrl);
		} else {
			showError("error.missing.file");
		}
	}

	private void doAddSolution(UserRequest ureq) {
		addSolutionCtrl = new EditSolutionController(ureq, getWindowControl(), solutionDir, solutionContainer);
		listenTo(addSolutionCtrl);

		String title = translate("add.solution");
		cmc = new CloseableModalController(getWindowControl(), null, addSolutionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditMetadata(UserRequest ureq, Solution solution) {
		editSolutionCtrl = new EditSolutionController(ureq, getWindowControl(), solution, solutionDir, solutionContainer);
		listenTo(editSolutionCtrl);

		String title = translate("add.solution");
		cmc = new CloseableModalController(getWindowControl(), null, editSolutionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateSolution(UserRequest ureq) {
		newSolutionCtrl = new NewSolutionController(ureq, getWindowControl(), solutionContainer,
				officeHtml(getIdentity(), ureq.getUserSession().getRoles(), getLocale()));
		listenTo(newSolutionCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), newSolutionCtrl.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(UserRequest ureq, SolutionRow solution) {
		String documentName = solution.solution().getFilename();
		VFSItem item = solutionContainer.resolve(documentName);
		if(item != null) {
			transcodingService.deleteMasterFile(item);
			item.delete();
		}
		gtaManager.removeSolution(solution.solution(), courseEnv, gtaNode);
		fireEvent(ureq, Event.DONE_EVENT);
		updateModel(ureq);
	}

	private void doRecordAudio(UserRequest ureq) {
		avSampleSolutionController = new AVSampleSolutionController(ureq, getWindowControl(), solutionContainer, true);
		listenTo(avSampleSolutionController);

		String title = translate("av.record.audio");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), avSampleSolutionController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doRecordVideo(UserRequest ureq) {
		avSampleSolutionController = new AVSampleSolutionController(ureq, getWindowControl(), solutionContainer, false);
		listenTo(avSampleSolutionController);

		String title = translate("av.record.video");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), avSampleSolutionController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, VFSTranscodingService.ores);
		super.doDispose();
	}

	private class ToolsController extends BasicController {

		private final VelocityContainer mainVC;
		private final Link deleteLink;
		private final Link editLink;
		private final Link openLink;
		private final Link downloadLink;
		private final SolutionRow solutionRow;

		public ToolsController(UserRequest ureq, WindowControl wControl, SolutionRow solutionRow) {
			super(ureq, wControl);
			this.solutionRow = solutionRow;
			setTranslator(getTranslator());

			mainVC = createVelocityContainer("submit_docs_tools");

			List<String> links = new ArrayList<>(2);

			editLink = addLink("edit", "o_icon_edit", links);
			links.add("-");

			String iconLeftCSS = solutionRow.openLink() != null ? solutionRow.openLink().getComponent().getIconLeftCSS() : "";
			String i18nKey = "";
			if (iconLeftCSS.contains("preview")) {
				i18nKey = "open.file";
			} else if (iconLeftCSS.contains("edit")) {
				i18nKey = "edit.file";
				iconLeftCSS = "o_icon-file-pen";
			} else if (iconLeftCSS.contains("_play")) {
				i18nKey = "play.file";
			}
			openLink = addLink(i18nKey, iconLeftCSS, links);
			if (i18nKey.equalsIgnoreCase("edit.file")) {
				openLink.setNewWindow(true, true);
			}

			if (readOnly) {
				editLink.setVisible(false);
				openLink.setVisible(false);
			}
			downloadLink = addLink("download.file", "o_icon_download", links);
			links.add("-");
			deleteLink = addLink("delete", "o_icon_delete_item", links);

			mainVC.contextPut("links", links);

			putInitialPanel(mainVC);
		}

		private Link addLink(String name, String iconCss, List<String> links) {
			int presentation = Link.LINK;
			if (solutionRow.openLink() != null && solutionRow.openLink().getI18nKey().equals(name)) {
				presentation = Link.NONTRANSLATED;
			}
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, presentation);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon o_icon-fw " + iconCss);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (source == deleteLink) {
				close();
				doDelete(ureq, solutionRow);
			} else if (source == editLink) {
				close();
				doEditMetadata(ureq, solutionRow.solution());
			} else if (source == openLink) {
				close();
				if (solutionRow.openLink().getCmd().equalsIgnoreCase("open")) {
					doOpenMedia(ureq,solutionRow.solution());
				} else if (solutionRow.openLink().getCmd().equalsIgnoreCase("transcoding")) {
					doOpenTranscoding(ureq, solutionRow.openLink(), solutionRow.solution());
				}
			} else if (source == downloadLink) {
				VFSMediaResource vdr = new VFSMediaResource((VFSLeaf) solutionRow.downloadLink().getUserObject());
				vdr.setDownloadable(true);
				ureq.getDispatchResult().setResultingMediaResource(vdr);
			}
		}

		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
