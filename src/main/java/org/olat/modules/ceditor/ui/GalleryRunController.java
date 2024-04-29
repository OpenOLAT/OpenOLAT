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
package org.olat.modules.ceditor.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.model.GallerySettings;
import org.olat.modules.ceditor.model.jpa.GalleryPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

/**
 * Initial date: 2024-04-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GalleryRunController extends BasicController implements PageRunElement {

	private final VelocityContainer mainVC;
	private GalleryPart galleryPart;
	private final boolean editable;

	public GalleryRunController(UserRequest ureq, WindowControl wControl, GalleryPart galleryPart, boolean editable) {
		super(ureq, wControl);
		this.galleryPart = galleryPart;
		this.editable = editable;
		mainVC = createVelocityContainer("gallery_run");
		mainVC.setElementCssClass("o_gallery_run_element_css_class");
		setBlockLayoutClass(galleryPart.getSettings());
		putInitialPanel(mainVC);
		initUI();
		updateUI();
	}

	private void setBlockLayoutClass(GallerySettings gallerySettings) {
		mainVC.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(gallerySettings, false));
	}

	private void initUI() {

	}

	private void updateUI() {
		mainVC.contextPut("title", galleryPart.getSettings().getTitle());
		mainVC.put("gallery.images", TextFactory.createTextComponentFromString("gallery.images",
				"image slider placeholder", "o_hint", false, mainVC));
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof ChangePartEvent changePartEvent) {
			if (changePartEvent.getElement() instanceof GalleryPart updatedGalleryPart) {
				galleryPart = updatedGalleryPart;
				setBlockLayoutClass(galleryPart.getSettings());
				updateUI();
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public Component getComponent() {
		return getInitialComponent();
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		return false;
	}
}
