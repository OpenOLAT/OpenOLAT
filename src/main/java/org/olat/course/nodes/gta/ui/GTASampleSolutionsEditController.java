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
package org.olat.course.nodes.gta.ui;

import static org.olat.course.nodes.gta.ui.GTAUIFactory.getOpenMode;
import static org.olat.course.nodes.gta.ui.GTAUIFactory.htmlOffice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallbackBuilder;
import org.olat.core.commons.services.doceditor.ui.DocEditorFullscreenController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.model.Solution;
import org.olat.course.nodes.gta.ui.SolutionTableModel.SolCols;
import org.olat.course.nodes.gta.ui.component.ModeCellRenderer;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTASampleSolutionsEditController extends FormBasicController implements Activateable2 {
	
	private FormLink addSolutionLink;
	private FormLink createSolutionLink;
	private SolutionTableModel solutionModel;
	private FlexiTableElement solutionTable;
	
	private CloseableModalController cmc;
	private EditSolutionController addSolutionCtrl;
	private EditSolutionController editSolutionCtrl;
	private NewSolutionController newSolutionCtrl;
	private DocEditorFullscreenController docEditorCtrl;
	
	private final File solutionDir;
	private final boolean readOnly;
	private final GTACourseNode gtaNode;
	private final CourseEnvironment courseEnv;
	private final VFSContainer solutionContainer;
	private final Long courseRepoKey;
	
	private int linkCounter = 0;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private GTAManager gtaManager;
	
	public GTASampleSolutionsEditController(UserRequest ureq, WindowControl wControl, GTACourseNode gtaNode,
			CourseEnvironment courseEnv, boolean readOnly) {
		super(ureq, wControl, "edit_solution_list");
		this.gtaNode = gtaNode;
		this.readOnly = readOnly;
		this.courseEnv = courseEnv;
		this.courseRepoKey = courseEnv.getCourseGroupManager().getCourseEntry().getKey();
		solutionDir = gtaManager.getSolutionsDirectory(courseEnv, gtaNode);
		solutionContainer = gtaManager.getSolutionsContainer(courseEnv, gtaNode);
		initForm(ureq);
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

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SolCols.title.i18nKey(), SolCols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SolCols.file.i18nKey(), SolCols.file.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SolCols.author.i18nKey(), SolCols.author.ordinal()));
		
		String openI18n = readOnly? "table.header.view": "table.header.edit";
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(openI18n, SolCols.mode.ordinal(), "open", new ModeCellRenderer("open")));
		if(!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.metadata", translate("table.header.metadata"), "metadata"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.delete", translate("table.header.delete"), "delete"));
		}

		solutionModel = new SolutionTableModel(columnsModel);
		solutionTable = uifactory.addTableElement(getWindowControl(), "table", solutionModel, getTranslator(), formLayout);
		solutionTable.setExportEnabled(true);
		updateModel(ureq);
	}
	
	private void updateModel(UserRequest ureq) {
		List<Solution> solutionList = gtaManager.getSolutions(courseEnv, gtaNode);
		List<SolutionRow> rows = new ArrayList<>(solutionList.size());
		for(Solution solution:solutionList) {
			String filename = solution.getFilename();
			String author = null;
			Mode openMode = null;
			
			VFSItem item = solutionContainer.resolve(filename);
			if(item.canMeta() == VFSConstants.YES) {
				VFSMetadata metaInfo = item.getMetaInfo();
				if(metaInfo.getAuthor() != null) {
					author = userManager.getUserDisplayName(metaInfo.getAuthor());
				}
			}
			
			DownloadLink downloadLink = null;
			if(item instanceof VFSLeaf) {
				VFSLeaf vfsLeaf = (VFSLeaf)item;
				downloadLink = uifactory
					.addDownloadLink("file_" + (++linkCounter), filename, null, vfsLeaf, solutionTable);
				openMode = getOpenMode(getIdentity(), ureq.getUserSession().getRoles(), vfsLeaf, readOnly);
			}

			rows.add(new SolutionRow(solution, author, downloadLink, openMode));
		}
		solutionModel.setObjects(rows);
		solutionTable.reset();
	}
	
	@Override
	protected void doDispose() {
		//
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
				doOpen(ureq, newSolution, Mode.EDIT);
				updateModel(ureq);
				gtaManager.markNews(courseEnv, gtaNode);
			}
		} else if (source == docEditorCtrl) {
			if(event == Event.DONE_EVENT) {
				gtaManager.markNews(courseEnv, gtaNode);
				updateModel(ureq);
				cleanUp();
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(docEditorCtrl);
		removeAsListenerAndDispose(editSolutionCtrl);
		removeAsListenerAndDispose(addSolutionCtrl);
		removeAsListenerAndDispose(cmc);
		docEditorCtrl = null;
		editSolutionCtrl = null;
		addSolutionCtrl = null;
		cmc = null;
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
		} else if(solutionTable == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				SolutionRow row = solutionModel.getObject(se.getIndex());
				if("open".equals(se.getCommand())) {
					doOpen(ureq, row.getSolution(), row.getMode());
				} else if("metadata".equals(se.getCommand())) {
					doEditmetadata(ureq, row.getSolution());
				} else if("delete".equals(se.getCommand())) {
					doDelete(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpen(UserRequest ureq, Solution solution, Mode mode) {
		VFSItem vfsItem = solutionContainer.resolve(solution.getFilename());
		if(!(vfsItem instanceof VFSLeaf)) {
			showError("error.missing.file");
		} else {
			DocEditorSecurityCallback secCallback = DocEditorSecurityCallbackBuilder.builder()
					.withMode(mode)
					.build();
			DocEditorConfigs configs = GTAUIFactory.getEditorConfig(solutionContainer, solution.getFilename(), courseRepoKey);
			WindowControl swb = addToHistory(ureq, OresHelper.createOLATResourceableType("DocEditor"), null);
			docEditorCtrl = new DocEditorFullscreenController(ureq, swb, (VFSLeaf)vfsItem, secCallback, configs);
			listenTo(docEditorCtrl);
		}
	}

	private void doAddSolution(UserRequest ureq) {
		addSolutionCtrl = new EditSolutionController(ureq, getWindowControl(), solutionDir, solutionContainer);
		listenTo(addSolutionCtrl);

		String title = translate("add.solution");
		cmc = new CloseableModalController(getWindowControl(), null, addSolutionCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditmetadata(UserRequest ureq, Solution solution) {
		editSolutionCtrl = new EditSolutionController(ureq, getWindowControl(), solution, solutionDir, solutionContainer);
		listenTo(editSolutionCtrl);

		String title = translate("add.solution");
		cmc = new CloseableModalController(getWindowControl(), null, editSolutionCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateSolution(UserRequest ureq) {
		newSolutionCtrl = new NewSolutionController(ureq, getWindowControl(), solutionContainer,
				htmlOffice(getIdentity(), ureq.getUserSession().getRoles(), getLocale()));
		listenTo(newSolutionCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", newSolutionCtrl.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(UserRequest ureq, SolutionRow solution) {
		String documentName = solution.getSolution().getFilename();
		VFSItem item = solutionContainer.resolve(documentName);
		if(item != null) {
			item.delete();
		}
		gtaManager.removeSolution(solution.getSolution(), courseEnv, gtaNode);
		fireEvent(ureq, Event.DONE_EVENT);
		updateModel(ureq);
	}
}
