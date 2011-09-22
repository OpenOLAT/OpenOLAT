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
* <p>
* 
* Technische Universitaet Chemnitz Lehrstuhl Technische Informatik
* 
* Author Marcel Karras (toka@freebits.de)
* Author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)
* Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
* Author Stefan Köber (stefan.koeber@bps-system.de)
*/

package org.olat.repository.controllers;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br>
 * Choose between
 * - start course creation wizard
 * - open course editor
 * - go to detail page
 * 
 * <P>
 * Initial Date:  22.06.2010 <br>
 * @author skoeber
 */
public class RepositoryAddChooseStepsController extends BasicController {
	
	public static class ChooseStepsForm extends FormBasicController {
		private SingleSelection chooser;
		private String[] keys, labels;

		public final static String KEY_START_WIZARD = "sw";
		public final static String KEY_COURSE_EDIT = "ce";
		public final static String KEY_DETAILS_VIEW = "dv";

		public ChooseStepsForm(UserRequest ureq, WindowControl wControl, Translator translator) {
			super(ureq, wControl);
			this.setTranslator(translator);
			initForm(ureq);
		}
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			this.keys = new String[] { KEY_START_WIZARD, KEY_COURSE_EDIT, KEY_DETAILS_VIEW };
			this.labels = new String[] { translate("csc.startwizard"), translate("csc.courseedit"), translate("csc.detailsview") };
			chooser = uifactory.addRadiosVertical("csc.label", formLayout, keys, labels);
			chooser.select(this.keys[0], true);
			uifactory.addFormSubmitButton("cmd.forward", formLayout);
		}
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {	return chooser.isOneSelected(); }
		@Override
		protected void formOK(UserRequest ureq) {
			String selection = chooser.getSelectedKey();
			if(selection.equals(KEY_COURSE_EDIT)) fireEvent(ureq, COURSE_EDIT);
			else if(selection.equals(KEY_DETAILS_VIEW)) fireEvent(ureq, DETAILS_VIEW);
			else if(selection.equals(KEY_START_WIZARD)) fireEvent(ureq, CREATION_WIZARD);
		}
		@Override
		protected void doDispose() { /*nothing to dispose*/ }
	}

	
	/** Step to start the wizard has been chosen. */
	public static Event CREATION_WIZARD = new Event(ChooseStepsForm.KEY_START_WIZARD);
	/** Step to open the details view has been chosen. */
	public static Event DETAILS_VIEW = new Event(ChooseStepsForm.KEY_DETAILS_VIEW);
	/** Step to start the raw course editor has been chosen. */
	public static Event COURSE_EDIT = new Event(ChooseStepsForm.KEY_COURSE_EDIT);

	// package name
	private static final String PACKAGE = RepositoryEntry.class.getPackage().getName();
	// translator object
	private Translator translator;
	// velocity template
	private VelocityContainer vc;
	// course elements chooser
	private FormBasicController chooser;
	// keep track of the current course repository entry
	private final RepositoryEntry addedEntry;

	/**
	 * Event fired when the close icon is pressed.
	 */
	public static final Event EVENT_CLOSEICON = new Event("ci");

	public RepositoryAddChooseStepsController(final UserRequest ureq, final WindowControl control, final RepositoryEntry addedEntry) {
		super(ureq, control);
		this.addedEntry = addedEntry;
		// translator for this class
		translator = new PackageTranslator(PACKAGE, ureq.getLocale());
		// velocity template
		vc = new VelocityContainer("addSteps", RepositoryManager.class, "addSteps", translator, this);
		// chooser form
		chooser = new ChooseStepsForm(ureq, control, translator);
		listenTo(chooser);
		vc.put("chooser", chooser.getInitialComponent());
		
		putInitialPanel(vc);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doDispose() {
		// nothing to dispose here
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == this.chooser) {
			fireEvent(ureq, event);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	/**
	 * Get the currently active course entry.
	 */
	public final RepositoryEntry getCourseRepositoryEntry() {
		return this.addedEntry;
	}

}
