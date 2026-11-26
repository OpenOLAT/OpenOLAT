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
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Util;
import org.olat.modules.certificationprogram.ui.CertificationProgramSecurityCallback;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumSecurityCallbackFactory;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.LecturesSecurityCallbackFactory;

/**
 * Redesigned "Select CPL Implementation (SingleCourse)" dialog for
 * attaching a curriculum element to a Certification program. Wraps
 * {@link CurriculumComposerController} (in
 * {@link CurriculumComposerConfig#selectSingleCourseImplementation()} mode) as
 * the body of a
 * {@link org.olat.core.gui.control.generic.closablewrapper.FooterModalController},
 * with its own sticky-footer Select/Cancel buttons, mirroring
 * {@link org.olat.repository.ui.RepositoryEntrySelectionController}.
 *
 * Initial date: 2026-11-26<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class SingleCourseImplementationSelectionController extends BasicController {

	private final VelocityContainer footerVC;
	private final Link selectButton;
	private final Link cancelButton;
	private final CurriculumComposerController composerCtrl;

	private CurriculumElement selectedElement;

	public SingleCourseImplementationSelectionController(UserRequest ureq, WindowControl wControl,
			CertificationProgramSecurityCallback certificationSecCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CurriculumComposerController.class, getLocale(), getTranslator()));

		Roles roles = ureq.getUserSession().getRoles();
		boolean admin = roles.isAdministrator() || roles.isCurriculumManager();
		CurriculumSecurityCallback secCallback = CurriculumSecurityCallbackFactory.createCallback(roles, List.of(), List.of());
		LecturesSecurityCallback lecturesSecCallback = LecturesSecurityCallbackFactory.getSecurityCallback(
				admin, roles.isPrincipal(), false, false, List.of(), List.of(),
				admin ? LectureRoles.lecturemanager : LectureRoles.participant);

		CurriculumComposerConfig config = CurriculumComposerConfig.selectSingleCourseImplementation();
		TooledStackedPanel toolbarPanel = new TooledStackedPanel("single-course-impl-selection", getTranslator(), this);
		composerCtrl = new CurriculumComposerController(ureq, wControl, toolbarPanel, null, null, config,
				secCallback, lecturesSecCallback, certificationSecCallback);
		listenTo(composerCtrl);
		List<ContextEntry> relevant = BusinessControlFactory.getInstance().createCEListFromString("[Relevant:0]");
		composerCtrl.activate(ureq, relevant, null);

		footerVC = createVelocityContainer("single_course_implementation_selection_footer");
		selectButton = LinkFactory.createButton("select", footerVC, this);
		selectButton.setElementCssClass("o_sel_cur_impl_selection_select");
		selectButton.setPrimary(true);
		cancelButton = LinkFactory.createButton("cancel", footerVC, this);
		cancelButton.setElementCssClass("o_sel_cur_impl_selection_cancel");

		putInitialPanel(composerCtrl.getInitialComponent());
	}

	/**
	 * The scrollable body for the wrapping {@code FooterModalController}.
	 */
	public Component getBodyComponent() {
		return composerCtrl.getInitialComponent();
	}

	/**
	 * The sticky-footer Select/Cancel row for the wrapping {@code FooterModalController}.
	 */
	public Component getFooterComponent() {
		return footerVC;
	}

	public CurriculumElement getSelectedElement() {
		return selectedElement;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == selectButton) {
			List<CurriculumElementRow> rows = composerCtrl.getMultiSelectedRows();
			if (rows.size() == 1) {
				selectedElement = rows.getFirst().getCurriculumElement();
				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				showWarning("select.element.nothing.selected");
			}
		} else if (source == cancelButton) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
}
