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
*/ 

package org.olat.ims.qti.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableDefaultController;
import org.olat.ims.qti.editor.beecom.objects.Duration;
import org.olat.ims.qti.editor.beecom.objects.Section;

/**
 * Initial Date: Oct 21, 2004 <br>
 * 
 * @author mike
 */
public class SectionController extends TabbableDefaultController implements ControllerEventListener {
	
	private VelocityContainer main;	
	private Section section;
	private QTIEditorPackage qtiPackage;
	private boolean restrictedEdit;

	/**
	 * @param section
	 * @param qtiPackage
	 * @param locale
	 * @param wControl
	 */
	public SectionController(Section section, QTIEditorPackage qtiPackage, UserRequest ureq, WindowControl wControl, boolean restrictedEdit) {
		super(ureq, wControl);

		this.restrictedEdit = restrictedEdit;
		this.section = section;
		this.qtiPackage = qtiPackage;
		
		main = this.createVelocityContainer("tab_section");
		main.contextPut("section", section);
		main.contextPut("order_type", section.getSelection_ordering().getOrderType());
		main.contextPut("selection_number", String.valueOf(section.getSelection_ordering().getSelectionNumber()));
		main.contextPut("mediaBaseURL", qtiPackage.getMediaBaseURL());
		main.contextPut("isRestrictedEdit", restrictedEdit ? Boolean.TRUE : Boolean.FALSE);

		boolean isSurvey = qtiPackage.getQTIDocument().isSurvey();
		main.contextPut("isSurveyMode", isSurvey ? "true" : "false");
		if (!isSurvey) {
			if (section.getDuration() != null) main.contextPut("duration", section.getDuration());
		}		
		this.putInitialPanel(main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == main) {
			if (event.getCommand().equals("ssec")) {
				String newTitle = ureq.getParameter("title");
				if (newTitle.trim().isEmpty()) { // Remove empty title to fix OLAT-2296
					newTitle = "";
				}
				String oldTitle = section.getTitle();
				boolean hasTitleChange = newTitle != null && !newTitle.equals(oldTitle);
				String newObjectives = ureq.getParameter("objectives");
				String oldObjectives = section.getObjectives();
				boolean hasObjectivesChange = newObjectives != null && !newObjectives.equals(oldObjectives);
				NodeBeforeChangeEvent nce = new NodeBeforeChangeEvent();
				if (hasTitleChange) {
					nce.setNewTitle(newTitle);
				}
				if (hasObjectivesChange) {
					nce.setNewObjectives(newObjectives);
				}
				if (hasTitleChange || hasObjectivesChange) {
					// create a memento first
					nce.setSectionIdent(section.getIdent());
					fireEvent(ureq, nce);
					// then apply changes
					section.setTitle(newTitle);
					section.setObjectives(newObjectives);
				}
				//
				if (!restrictedEdit) {
					section.getSelection_ordering().setOrderType(ureq.getParameter("order_type"));
					section.getSelection_ordering().setSelectionNumber(ureq.getParameter("selection_number"));
					main.contextPut("order_type", section.getSelection_ordering().getOrderType());
					main.contextPut("selection_number", String.valueOf(section.getSelection_ordering().getSelectionNumber()));

					String duration = ureq.getParameter("duration");
					if (duration != null && duration.equals("Yes")) {
						String durationMin = ureq.getParameter("duration_min");
						String durationSec = ureq.getParameter("duration_sec");
						try {
							Integer.parseInt(durationMin);
							int sec = Integer.parseInt(durationSec);
							if (sec > 60) throw new NumberFormatException();
						} catch (NumberFormatException nfe) {
							durationMin = "0";
							durationSec = "0";							
							this.showWarning("error.duration");
						}
						Duration d = new Duration(durationMin, durationSec);
						section.setDuration(d);
						main.contextPut("duration", d);
					} else {
						section.setDuration(null);
						main.contextRemove("duration");
					}
				}
				qtiPackage.serializeQTIDocument();

				//refresh for removing dirty marking of button even if nothing changed
				main.setDirty(true);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		main = null;		
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableController#addTabs(org.olat.core.gui.components.TabbedPane)
	 */
	public void addTabs(TabbedPane tabbedPane) {
		tabbedPane.addTab(translate("tab.section"), main);
	}

}