/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.curriculum.ui.widgets;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.ui.LectureBlocksWidgetController;
import org.olat.modules.lecture.ui.LectureBlocksWidgetRow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 21, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumLectureBlocksWidgetController extends LectureBlocksWidgetController {
	
	private Curriculum curriculum;
	private CurriculumElement curriculumElement;
	
	@Autowired
	private LectureService lectureService;

	public CurriculumLectureBlocksWidgetController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
		setUrl(showAllLink, "[CurriculumAdmin:0][Events:0][All:0]");
	}
	
	public CurriculumLectureBlocksWidgetController(UserRequest ureq, WindowControl wControl, Curriculum curriculum) {
		super(ureq, wControl);
		this.curriculum = curriculum;
		
		initForm(ureq);
		setUrl(showAllLink, "[CurriculumAdmin:0][Curriculums:0][Curriculum:" + curriculum.getKey() + "][Lectures:0][All:0]");
	}
	
	public CurriculumLectureBlocksWidgetController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement) {
		super(ureq, wControl);
		this.curriculumElement = curriculumElement;
		
		initForm(ureq);
		setUrl(showAllLink, "[CurriculumAdmin:0][Implementations:0][CurriculumElement:" + curriculumElement.getKey() + "][Lectures:0][All:0]");
	}

	@Override
	protected List<LectureBlock> loadLectureBlocks(Date fromDate, Date toDate) {
		LecturesBlockSearchParameters searchParams = getSearchParameters();
		searchParams.setStartDate(fromDate);
		searchParams.setEndDate(toDate);
		searchParams.setManager(getIdentity());
		
		return lectureService.getLectureBlocks(searchParams, -1, Boolean.TRUE);
	}

	@Override
	protected LectureBlockRef loadNextScheduledBlock() {
		LecturesBlockSearchParameters nextParams = getSearchParameters();
		nextParams.setStartDate(new Date());
		return lectureService.getNextScheduledLectureBlock(nextParams);
	}

	@Override
	protected Date getPrevLectureBlock(Date date) {
		LecturesBlockSearchParameters searchParams = getSearchParameters();
		searchParams.setStartDateBefore(date);
		searchParams.setManager(getIdentity());
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(searchParams, 1, Boolean.FALSE);
		return !lectureBlocks.isEmpty()? lectureBlocks.get(0).getStartDate(): null;
	}

	@Override
	protected Date getNextLectureBlock(Date date) {
		LecturesBlockSearchParameters searchParams = getSearchParameters();
		searchParams.setStartDate(date);
		searchParams.setManager(getIdentity());
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(searchParams, 1, Boolean.TRUE);
		return !lectureBlocks.isEmpty()? lectureBlocks.get(0).getStartDate(): null;
	}
	
	private LecturesBlockSearchParameters getSearchParameters() {
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setLectureConfiguredRepositoryEntry(false);
		if(curriculum != null) {
			searchParams.setCurriculums(List.of(curriculum));
		} else if(curriculumElement != null) {
			searchParams.setCurriculumElementPath(curriculumElement.getMaterializedPathKeys());
		} else {
			searchParams.setInSomeCurriculum(true);
		}
		return searchParams;
	}
	
	private void setUrl(FormLink link, String businessPath) {
		link.setUserObject(businessPath);
		String url = BusinessControlFactory.getInstance().getRelativeURLFromBusinessPathString(businessPath);
		link.setUrl(url);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent se) {
				doOpenLectureBlock(ureq, se.getIndex());
			}
		} else if (source == showAllLink) {
			doOpen(ureq, "[Lectures:0][All:0]");
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doOpenLectureBlock(UserRequest ureq, int index) {
		LectureBlocksWidgetRow row = dataModel.getObject(index);
		StringBuilder lecturesPath = new StringBuilder();
		lecturesPath.append("[Lectures:0][All:0]");
		lecturesPath.append("[Lecture:").append(row.getKey()).append("]");
		doOpen(ureq, lecturesPath.toString());
	}

	private void doOpen(UserRequest ureq, String businessPath) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance()
				.createCEListFromString(businessPath);
		fireEvent(ureq, new ActivateEvent(entries));
	}

}
