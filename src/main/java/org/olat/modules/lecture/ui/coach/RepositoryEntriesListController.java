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
package org.olat.modules.lecture.ui.coach;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureRepositoryEntryInfos;
import org.olat.modules.lecture.model.LectureRepositoryEntrySearchParameters;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.coach.RepositoryEntriesListTableModel.LectureRepoCols;
import org.olat.modules.lecture.ui.event.SelectLectureRepositoryEntryEvent;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.author.AccessRenderer;
import org.olat.repository.ui.author.TypeRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntriesListController extends FormBasicController {
	
	private FormLink backLink;
	private FlexiTableElement tableEl;
	private RepositoryEntriesListTableModel tableModel;

	private final Identity restrictByTeacher;
	private final LecturesSecurityCallback secCallback;
	
	@Autowired
	private LectureService lectureService;
	
	public RepositoryEntriesListController(UserRequest ureq, WindowControl wControl,
			Identity teacher, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "entries", Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(LectureRepositoryAdminController.class, getLocale(), getTranslator()));
		restrictByTeacher = teacher;
		this.secCallback = secCallback;
		initForm(ureq);
		if(restrictByTeacher != null) {
			loadModel(null);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LectureRepoCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LectureRepoCols.repoEntry, new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LectureRepoCols.displayname, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LectureRepoCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LectureRepoCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LectureRepoCols.lifecycleLabel));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LectureRepoCols.lifecycleSoftKey));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LectureRepoCols.lifecycleStart));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LectureRepoCols.lifecycleEnd));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LectureRepoCols.access, new AccessRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LectureRepoCols.numOfParticipants));

		tableModel = new RepositoryEntriesListTableModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(backLink == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				LectureRepositoryEntryInfos infos = tableModel.getObject(((SelectionEvent)event).getIndex());
				fireEvent(ureq, new SelectLectureRepositoryEntryEvent(infos.getEntry()));
			} else if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent ftse = (FlexiTableSearchEvent)event;
				loadModel(ftse.getSearch());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void loadModel(String searchString) {
		LectureRepositoryEntrySearchParameters searchParams = new LectureRepositoryEntrySearchParameters();
		searchParams.setTeacher(restrictByTeacher);
		searchParams.setSearchString(searchString);
		searchParams.setViewAs(getIdentity(), secCallback.viewAs());
		
		List<LectureRepositoryEntryInfos> rows = lectureService.searchRepositoryEntries(searchParams);
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	

}
