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
*/

package org.olat.core.gui.control.generic.closablewrapper;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ModalController;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.util.ZIndexWrapper;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * A modal dialog wrapper like {@link CloseableModalController}, but with an
 * additional sticky footer region (e.g. for a "Select"/"Cancel"
 * button row).
 *
 * Copied from {@link CloseableModalController} rather than extending it 
 * to keep the original untouched.
 *
 * Initial date: 2026-10-26<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FooterModalController extends DefaultController implements ModalController {

	public static final Event CLOSE_MODAL_EVENT = CloseableModalController.CLOSE_MODAL_EVENT;
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(FooterModalController.class);

	private final Link closeIcon;
	private final VelocityContainer myContent;

	public FooterModalController(WindowControl wControl, String closeButtonText, Component modalContent,
			Component footerContent, String title) {
		super(wControl);
		final Panel guiMsgPlace = new Panel("guimessage_place");
		myContent = new VelocityContainer("footerModalWrapper", VELOCITY_ROOT + "/index_footer.html", null, this) {
			@Override
			public void validate(UserRequest ureq, ValidationResult vr) {
				super.validate(ureq, vr);
				WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
				List<ZIndexWrapper> zindexed = wbo.getGuiMessages();
				zindexed.add(new ZIndexWrapper(guiMsgPlace, 20));
			}
		};
		myContent.put("guimessage", guiMsgPlace);

		closeIcon = LinkFactory.createIconClose(closeButtonText, myContent, this);
		closeIcon.setDomReplacementWrapperRequired(false);
		closeIcon.setSuppressDirtyFormWarning(true);
		closeIcon.setTitle(closeButtonText == null ? "close" : closeButtonText);

		if (title != null) {
			myContent.contextPut("title", StringHelper.escapeHtml(title));
		}
		myContent.put("modalContent", modalContent);
		myContent.put("footerContent", footerContent);

		setInitialComponent(myContent);
	}

	@Override
	public boolean isCloseable() {
		return true;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == closeIcon) {
			deactivate();
			fireEvent(ureq, CLOSE_MODAL_EVENT);
		}
	}

	@Override
	public Component getInitialComponent() {
		throw new RuntimeException("please use activate() instead");
	}

	public void activate() {
		getWindowControl().pushAsModalDialog(myContent);
	}

	public void deactivate() {
		getWindowControl().pop();
	}

	@Override
	protected void doDispose() {
		getWindowControl().removeModalDialog(myContent);
		super.doDispose();
	}
}
