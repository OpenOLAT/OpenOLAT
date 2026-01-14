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
package org.olat.modules.coach.ui.dashboard;

import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.ui.LectureBlocksWidgetController;
import org.olat.modules.lecture.ui.LectureBlocksWidgetRow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 8, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CoachLectureBlocksWidgetController extends LectureBlocksWidgetController {

	@Autowired
	private LectureService lectureService;

	public CoachLectureBlocksWidgetController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
		
		showAllLink.setUrl(null);
		setUrl(showAllLink, "[CoachSite:0][Events:0][Teacher:0][Teacher:0]");
	}

	@Override
	protected List<LectureBlock> loadLectureBlocks(Date fromDate, Date toDate) {
		// see LectureListRepositoryController
		
		LecturesBlockSearchParameters searchParams = getSearchParams();
		searchParams.setStartDate(fromDate);
		searchParams.setEndDate(toDate);
		return lectureService.getLectureBlocks(searchParams, -1, null);
	}

	@Override
	protected LectureBlockRef loadNextScheduledBlock() {
		LecturesBlockSearchParameters nextParams = getSearchParams();
		nextParams.setStartDate(new Date());
		return lectureService.getNextScheduledLectureBlock(nextParams);
	}

	@Override
	protected Date getPrevLectureBlock(Date date) {
		LecturesBlockSearchParameters searchParams = getSearchParams();
		searchParams.setStartDateBefore(date);
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(searchParams, 1, Boolean.FALSE);
		return !lectureBlocks.isEmpty()? lectureBlocks.get(0).getStartDate(): null;
	}

	@Override
	protected Date getNextLectureBlock(Date date) {
		LecturesBlockSearchParameters searchParams = getSearchParams();
		searchParams.setStartDate(date);
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(searchParams, 1, Boolean.TRUE);
		return !lectureBlocks.isEmpty()? lectureBlocks.get(0).getStartDate(): null;
	}
	
	private LecturesBlockSearchParameters getSearchParams() {
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setTeacher(getIdentity());
		searchParams.setInSomeCurriculum(false);
		searchParams.setLectureConfiguredRepositoryEntry(true);
		return searchParams;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent se) {
				doOpenLectureBlock(ureq, se.getIndex());
			}
		} else if (source == showAllLink) {
			if (showAllLink.getUserObject() instanceof String businessPath) {
				doOpen(ureq, businessPath);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void setUrl(FormLink link, String businessPath) {
		link.setUserObject(businessPath);
		String url = BusinessControlFactory.getInstance().getRelativeURLFromBusinessPathString(businessPath);
		link.setUrl(url);
	}
	
	private void doOpenLectureBlock(UserRequest ureq, int index) {
		LectureBlocksWidgetRow row = dataModel.getObject(index);
		doOpen(ureq, "[CoachSite:0][Events:0][Teacher:0][Teacher:0][Lecture:" + row.getKey() + "]");
	}
	
	private void doOpen(UserRequest ureq, String businessPath) {
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

}
