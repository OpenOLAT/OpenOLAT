/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
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
package org.olat.core.commons.services.ai.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Modal consent dialog shown at the start of a quiz that corrects free-text
 * answers with AI, as long as the person has made no choice yet. It tells the
 * person what happens, names the system default of the institution and offers
 * three answers.
 * <p>
 * The controller stores nothing. It fires {@link #ALLOW_ONCE_EVENT},
 * {@link #ALLOW_ALWAYS_EVENT} or {@link #DENY_EVENT} and leaves the decision to
 * the calling controller.
 *
 * Initial date: Sep 13, 2026<br>
 * @author Florian Gnägi, https://www.frentix.com
 *
 */
public class AiCorrectionConsentController extends BasicController {

	/** The AI correction runs for this run only, nothing is stored. */
	public static final Event ALLOW_ONCE_EVENT = new Event("ai-consent-allow-once");
	/** The AI correction runs and the choice is stored as ON. */
	public static final Event ALLOW_ALWAYS_EVENT = new Event("ai-consent-allow-always");
	/** The AI correction does not run for this run, nothing is stored. */
	public static final Event DENY_EVENT = new Event("ai-consent-deny");

	private final Link allowOnceLink;
	private final Link allowAlwaysLink;
	private final Link denyLink;

	/**
	 * @param ureq the user request
	 * @param wControl the window control
	 * @param userDefaultOn the system default of the administrator. It selects
	 *        the sentence about the institution and the primary button, it does
	 *        not decide whether the dialog appears.
	 */
	public AiCorrectionConsentController(UserRequest ureq, WindowControl wControl, boolean userDefaultOn) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("ai_correction_consent");
		mainVC.contextPut("userDefaultOn", Boolean.valueOf(userDefaultOn));

		allowOnceLink = LinkFactory.createButton("ai.consent.allow.once", mainVC, this);
		allowAlwaysLink = LinkFactory.createButton("ai.consent.allow.always", mainVC, this);
		denyLink = LinkFactory.createButton("ai.consent.deny", mainVC, this);
		if (userDefaultOn) {
			allowOnceLink.setPrimary(true);
		} else {
			denyLink.setPrimary(true);
		}

		putInitialPanel(mainVC);
	}

	/**
	 * @return the title of the modal window, translated with the bundle of this
	 *         package
	 */
	public String getModalTitle() {
		return translate("ai.consent.title");
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == allowOnceLink) {
			fireEvent(ureq, ALLOW_ONCE_EVENT);
		} else if (source == allowAlwaysLink) {
			fireEvent(ureq, ALLOW_ALWAYS_EVENT);
		} else if (source == denyLink) {
			fireEvent(ureq, DENY_EVENT);
		}
	}

}
