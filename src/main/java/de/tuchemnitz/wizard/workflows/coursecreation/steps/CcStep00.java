/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
* Initial code contributed and copyrighted by<br>
* Technische Universitaet Chemnitz Lehrstuhl Technische Informatik<br>
* <br>
* Author Marcel Karras (toka@freebits.de)<br>
* Author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)<br>
* Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
*/

package de.tuchemnitz.wizard.workflows.coursecreation.steps;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;

import de.tuchemnitz.wizard.workflows.coursecreation.CourseCreationHelper;
import de.tuchemnitz.wizard.workflows.coursecreation.model.CourseCreationConfiguration;

/**
 * 
 * Description:<br>
 * First step of the course creation wizard:
 * <br/>- choose course nodes
 * <br/>- do some simple configuration
 * 
 * <P>
 * @author Marcel Karras (toka@freebits.de)
 * @author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)
 * @author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
 * @author skoeber
 */
public class CcStep00 extends BasicStep {

	CourseCreationConfiguration courseConfig;

	private PrevNextFinishConfig prevNextConfig;
	private CloseableModalController cmc;
	private EnrollmentEditForm formEditEnrol;

	/**
	 * First step of the course creation wizard
	 * @param ureq
	 * @param courseConfig
	 * @param repoEntry
	 */
	public CcStep00(UserRequest ureq, CourseCreationConfiguration courseConfig, final RepositoryEntry repoEntry) {
		super(ureq);

		this.courseConfig = courseConfig;
		setI18nTitleAndDescr("coursecreation.choosecourseelements.title", "coursecreation.choosecourseelements.shortDescription");
		setNextStep(new CcStep01(ureq, courseConfig, repoEntry));
		prevNextConfig = PrevNextFinishConfig.NEXT;
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return prevNextConfig;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		StepFormController stepP = new CcStep00Form(ureq, windowControl, form, stepsRunContext, null);
		return stepP;
	}

	class CcStep00Form extends StepFormBasicController {

		private Translator translator;
		private MultipleSelectionElement rightsChooser;
		private FormLayoutContainer fic;
		private FormLink editButtonEnrollment = null;

		public CcStep00Form(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, String customLayoutPageName) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, customLayoutPageName);
			translator = Util.createPackageTranslator(CourseCreationHelper.class, ureq.getLocale());
			super.setTranslator(translator);
			// first set the courseConfig to default
			initWorkflowItem();
			// show gui
			initForm(ureq);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			super.formInnerEvent(ureq, source, event);
			// show edit button if enrollment is activated
			if (isEnrollmentSelected()) {
				if (editButtonEnrollment != null) {
					editButtonEnrollment.setVisible(true);
				}
			} else {
				if (editButtonEnrollment != null) {
					editButtonEnrollment.setVisible(false);
				}
			}

			// overlay with configuration of enrollment
			if (source == editButtonEnrollment) {
				finishWorkflowItem();
				formEditEnrol = new EnrollmentEditForm(ureq, getWindowControl(), courseConfig);
				listenTo(formEditEnrol);
				
				String title = translate("coursecreation.enrollment.title");
				cmc = new CloseableModalController(getWindowControl(), "close", formEditEnrol.getInitialComponent(), true, title);
				listenTo(cmc);
				cmc.activate();
			}
		}
		
		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if(source == formEditEnrol) {
				cmc.deactivate();
			}
		}

		@Override
		protected void formOK(UserRequest ureq) {
			finishWorkflowItem();
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

			fic = FormLayoutContainer.createCustomFormLayout("cc00", this.getTranslator(), this.velocity_root + "/CcStep00_form.html");
			formLayout.add(fic);

			// prepare checkboxes
			String[] keys = new String[] { "sp", "en", "bc", "fo", "co" };
			String[] values = new String[] {
					translator.translate("cce.informationpage"), translator.translate("cce.enrollment"),
					translator.translate("cce.downloadfolder"), translator.translate("cce.forum"),
					translator.translate("cce.contactform")
			};
			// CSS for thumbs
			String[] iconCSS = new String[] {
					"o_icon o_sp_icon", "o_icon o_en_icon",
					"o_icon o_bc_icon", "o_icon o_fo_icon",
					"o_icon o_co_icon"
			};
			// show checkbox
			rightsChooser = FormUIFactory.getInstance().addCheckboxesVertical("cce.label", fic, keys, values, iconCSS, 1);
			rightsChooser.addActionListener(FormEvent.ONCLICK); // Radios/Checkboxes need onclick because of IE bug OLAT-5753
			// create edit button for enrollment and hide it
			editButtonEnrollment = FormUIFactory.getInstance().addFormLink("cce.edit", fic);
			editButtonEnrollment.addActionListener(FormEvent.ONCLICK);
			editButtonEnrollment.setVisible(false);
		}

		public void finishWorkflowItem() {
			// update course config
			courseConfig.setCreateSinglePage(isSinglePageSelected());
			courseConfig.setCreateDownloadFolder(isDownloadSelected());
			courseConfig.setCreateEnrollment(isEnrollmentSelected());
			courseConfig.setCreateForum(isForumSelected());
			courseConfig.setCreateContactForm(isContactSelected());
		}

		public void initWorkflowItem() {
			// reset course config
			courseConfig.setCreateSinglePage(false);
			courseConfig.setCreateDownloadFolder(false);
			courseConfig.setCreateEnrollment(false);
			courseConfig.setCreateForum(false);
			courseConfig.setCreateContactForm(false);
		}

		private final boolean isSinglePageSelected() {
			return rightsChooser.getSelectedKeys().contains("sp");
		}

		private final boolean isEnrollmentSelected() {
			return rightsChooser.getSelectedKeys().contains("en");
		}

		private final boolean isDownloadSelected() {
			return rightsChooser.getSelectedKeys().contains("bc");
		}

		private final boolean isForumSelected() {
			return rightsChooser.getSelectedKeys().contains("fo");
		}

		private final boolean isContactSelected() {
			return rightsChooser.getSelectedKeys().contains("co");
		}
	}
}
