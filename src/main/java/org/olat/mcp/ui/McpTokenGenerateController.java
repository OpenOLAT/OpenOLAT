/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.mcp.ui;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpService;
import org.olat.mcp.McpTokenCreation;
import org.olat.mcp.McpTokenService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Asks for the label of a new token and generates it. The checks of the
 * personal panel run again here: the module, the self-service switch and the
 * grants of the user. In the user administration a manager generates the
 * token for another user; the check is then the right to manage the MCP
 * access of this user and the creation is logged with the manager. Fires
 * {@link Event#DONE_EVENT} with the creation available through
 * {@link #getCreation()}.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpTokenGenerateController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(McpTokenGenerateController.class);

	private TextElement labelEl;

	private McpTokenCreation creation;
	private final Identity owner;
	private final boolean manager;
	private final McpUserTokenTableModel existingTokens;

	@Autowired
	private McpModule mcpModule;
	@Autowired
	private McpService mcpService;
	@Autowired
	private McpTokenService tokenService;

	/**
	 * Generates a token for the user of the session (self-service).
	 */
	public McpTokenGenerateController(UserRequest ureq, WindowControl wControl, McpUserTokenTableModel existingTokens) {
		this(ureq, wControl, null, existingTokens);
	}

	/**
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param owner The owner of the new token, null for the user of the session.
	 * 		Another owner requires that the user of the session may manage the MCP
	 * 		access of the owner (user administration).
	 * @param existingTokens The tokens of the owner, to check the label
	 */
	public McpTokenGenerateController(UserRequest ureq, WindowControl wControl, Identity owner,
			McpUserTokenTableModel existingTokens) {
		super(ureq, wControl, LAYOUT_DEFAULT);
		this.owner = owner == null ? getIdentity() : owner;
		this.manager = !this.owner.getKey().equals(getIdentity().getKey());
		this.existingTokens = existingTokens;
		initForm(ureq);
	}

	/**
	 * @return The creation with the plaintext, null before a successful generation
	 */
	public McpTokenCreation getCreation() {
		return creation;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo(manager ? "user.admin.generate.intro" : "user.generate.intro");

		labelEl = uifactory.addTextElement("token.label", McpTokenService.LABEL_MAX_LENGTH, "", formLayout);
		labelEl.setMandatory(true);
		labelEl.setExampleKey("user.generate.label.hint", null);
		labelEl.setElementCssClass("o_sel_mcp_token_label");
		labelEl.setFocus(true);

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("user.generate.button", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		labelEl.clearError();
		String label = labelEl.getValue();
		if(!StringHelper.containsNonWhitespace(label)) {
			labelEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(label.trim().length() > McpTokenService.LABEL_MAX_LENGTH) {
			labelEl.setErrorKey("input.toolong", Integer.toString(McpTokenService.LABEL_MAX_LENGTH));
			allOk &= false;
		} else if(existingTokens != null && existingTokens.hasLabel(label, getLocale())) {
			labelEl.setErrorKey(manager ? "user.admin.generate.label.taken" : "user.generate.label.taken");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		if(!isAllowed(roles)) {
			showWarning(manager ? "user.admin.generate.not.allowed" : "user.generate.not.allowed");
			fireEvent(ureq, Event.CANCELLED_EVENT);
			return;
		}

		try {
			creation = tokenService.generateToken(owner, labelEl.getValue().trim());
			if(manager) {
				log.info(Tracing.M_AUDIT, "MCP token {} created for identity {} by the manager {}",
						creation.token().tokenId(), owner.getKey(), getIdentity().getKey());
			}
			fireEvent(ureq, Event.DONE_EVENT);
		} catch (IllegalStateException e) {
			labelEl.setErrorKey(manager ? "user.admin.generate.max.reached" : "user.generate.max.reached",
					Integer.toString(mcpModule.getUserTokenMax()));
		} catch (IllegalArgumentException e) {
			// the label is used by a token created in another window
			labelEl.setErrorKey(manager ? "user.admin.generate.label.taken" : "user.generate.label.taken");
		}
	}

	/**
	 * The self-service needs the switch of the module and at least one grant,
	 * a manager needs the right to manage the MCP access of the owner.
	 */
	private boolean isAllowed(Roles roles) {
		if(!mcpModule.isEnabled() || roles == null) {
			return false;
		}
		if(manager) {
			return mcpService.canManageMembers(roles, owner);
		}
		return mcpModule.isUserTokenGenerationEnabled() && !mcpService.resolveGrants(getIdentity()).isEmpty();
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
