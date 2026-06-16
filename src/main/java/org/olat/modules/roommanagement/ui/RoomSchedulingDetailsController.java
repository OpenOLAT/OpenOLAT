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
package org.olat.modules.roommanagement.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelectionSource;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 15 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomSchedulingDetailsController extends FormBasicController {

	private static final String EVENTS_BUSINESS_PATH = "[CurriculumAdmin:0][Events:0][All:0]";

	private FormLink openInCoursePlannerLink;

	private final RoomSchedulingRow row;

	@Autowired
	private LectureService lectureService;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private RepositoryModule repositoryModule;

	public RoomSchedulingDetailsController(UserRequest ureq, WindowControl wControl, RoomSchedulingRow row, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "room_scheduling_details_view", rootForm);
		setTranslator(Util.createPackageTranslator(LectureListRepositoryController.class, getLocale(), getTranslator()));
		this.row = row;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (!(formLayout instanceof FormLayoutContainer layoutCont)) return;

		LectureBlock lb = row.getBooking().getLectureBlock();
		if (lb == null) return;

		layoutCont.contextPut("title", lb.getTitle());
		layoutCont.contextPut("externalRef", lb.getExternalRef());
		layoutCont.contextPut("warnings", row.getWarnings());
		String statusBadge = LectureBlockStatusCellRenderer.getStatusLabelSolidWithIcon(lb, false, getTranslator());
		layoutCont.contextPut("lectureBlockStatusBadge", statusBadge);

		openInCoursePlannerLink = uifactory.addFormLink("openInCoursePlanner", "openInCoursePlanner",
				"room.scheduling.details.open.in.course.planner", null, formLayout, Link.BUTTON);
		openInCoursePlannerLink.setIconLeftCSS("o_icon o_icon-fw o_icon_external_link");
		openInCoursePlannerLink.setUrl(BusinessControlFactory.getInstance()
				.getRelativeURLFromBusinessPathString(EVENTS_BUSINESS_PATH));

		initSubjects(formLayout, lb);
	}

	private void initSubjects(FormItemContainer formLayout, LectureBlock lb) {
		if (!taxonomyModule.isEnabled() || curriculumModule.getTaxonomyRefs().isEmpty()) return;

		Collection<TaxonomyRef> taxonomyRefs = new HashSet<>(curriculumModule.getTaxonomyRefs());
		taxonomyRefs.addAll(repositoryModule.getTaxonomyRefs());
		if (taxonomyRefs.isEmpty()) return;

		List<TaxonomyLevel> subjects = lectureService.getTaxonomy(lb);
		if (subjects == null || subjects.isEmpty()) return;

		ObjectSelectionElement taxonomyLevelEl = uifactory.addObjectSelectionElement("lecture.subjects",
				"lecture.subjects", formLayout, getWindowControl(), true,
				new TaxonomyLevelSelectionSource(getLocale(), subjects, List::of, null));
		taxonomyLevelEl.setEnabled(false);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == openInCoursePlannerLink) {
			NewControllerFactory.getInstance().launch(EVENTS_BUSINESS_PATH, ureq, getWindowControl());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// read-only
	}
}
