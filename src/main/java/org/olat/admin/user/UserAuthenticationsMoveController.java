package org.olat.admin.user;

import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class UserAuthenticationsMoveController extends FormBasicController {

	private Authentication auth;
	private TextElement textUsername;
	private TextElement textIdentityId;
	private FormLink btnSearch;
	private FormSubmit submit;
	private FormLink btnMove;

	private Identity sourceIdentity;
	private Identity targetIdentity;

	private StaticTextElement targetUsername;
	private StaticTextElement targetFirstName;
	private StaticTextElement targetLastName;

	private String errorMessage = "";

	@Autowired
	private BaseSecurity securityManager;

	public UserAuthenticationsMoveController(UserRequest ureq, WindowControl wControl, Identity sourceIdentity) {
		super(ureq, wControl, "moveauthentication");
		this.sourceIdentity = sourceIdentity;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, @UnknownInitialization Controller listener, UserRequest ureq) {
		if (uifactory != null) {
			FormLayoutContainer searchFlc = FormLayoutContainer.createDefaultFormLayout("flc_search", getTranslator());
			formLayout.add(searchFlc);
			textUsername = uifactory.addTextElement("search.username", "moveidentity.search.username", 100, "", searchFlc);
			textIdentityId = uifactory.addTextElement("search.identityid", "moveidentity.search.identityid", 100, "", searchFlc);
			btnSearch = uifactory.addFormLink("search.search", searchFlc, "btn btn-default");
			btnSearch.setI18nKey("moveidentity.search.lookup");
			flc.contextPut("identityFound", false);
			submit = uifactory.addFormSubmitButton("submit.save", formLayout);
			submit.setEnabled(false);

			FormLayoutContainer moveFlc = FormLayoutContainer.createDefaultFormLayout("flc_move", getTranslator());
			formLayout.add(moveFlc);
			uifactory.addStaticTextElement("move.dummy", translate("move.explanation"), moveFlc);
			targetUsername = uifactory.addStaticTextElement("move.username", "", moveFlc);
			targetFirstName = uifactory.addStaticTextElement("move.firstname", "", moveFlc);
			targetLastName = uifactory.addStaticTextElement("move.lastname", "", moveFlc);
			btnMove = uifactory.addFormLink("move.move", moveFlc, "btn btn-default");
			btnMove.setEnabled(false);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink fl = (FormLink) source;
			if (fl.getCmd().equals("search.search")) {
				String username = textUsername.getValue();
				String identityKey = textIdentityId.getValue();
				String errorCode = "";
				targetIdentity = null;
				if (!textIdentityId.getValue().equals("")) {
					try {
						targetIdentity = BaseSecurityManager.getInstance().loadIdentityByKey(Long.parseLong(identityKey));
					} catch (NumberFormatException nfe) {
						errorCode = "error.move.identity.not.number";
					}
				} else if (!textUsername.getValue().equals("")) {
					targetIdentity = BaseSecurityManager.getInstance().findIdentityByName(username);
				} else {
					errorCode = "error.move.empty.search.fields";
				}

				// detect targetIdentity to match sourceIdentity
				if (targetIdentity != null && targetIdentity.getName().equals(sourceIdentity.getName())) {
					targetIdentity = null;
					errorCode = "error.move.same.identity";
				}

				if (targetIdentity != null) {
					flc.contextPut("identityFound", true);
					flc.contextPut("errorMessage", "");
					targetLastName.setValue(targetIdentity.getUser().getLastName());
					targetFirstName.setValue(targetIdentity.getUser().getFirstName());
					targetUsername.setValue(targetIdentity.getName());
					btnMove.setEnabled(true);
				} else {
					if ("".equals(errorCode)) {
						errorCode = "error.no.user.found";
					}
					flc.contextPut("hasError", true);
					flc.contextPut("identityFound", false);
					flc.contextPut("errorMessage", translate(errorCode));
					btnMove.setEnabled(false);
				}
			} else if(fl.getCmd().equals("move.move")) {
				String provider = auth.getProvider();

				// make sure that targetIdentity does not have an identity of the same provider as auth
				boolean alreadyHasTheSameProvider = false;
				List<Authentication> targetAuthentications = securityManager.getAuthentications(targetIdentity);
				for (Authentication authentication : targetAuthentications) {
					if (authentication.getProvider().equals(provider)) {
						alreadyHasTheSameProvider = true;
						break;
					}
				}

				if (alreadyHasTheSameProvider) {
					flc.contextPut("hasError", true);
					flc.contextPut("errorMessage", translate("error.move.same.provider.exists", new String[]{targetUsername.getValue(), targetFirstName.getValue(), targetLastName.getValue(), auth.getProvider()}));
					flc.contextPut("identityFound", false);
					btnMove.setEnabled(false);
				} else {
					flc.contextPut("hasError", false);
					auth.setIdentity(targetIdentity);
					auth = securityManager.updateAuthentication(auth);
					fireEvent(ureq, Event.DONE_EVENT);
				}
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Do not react on ENTER, only react on pressing one or another button
	}

	@Override
	public void event(UserRequest userRequest, Component source, Event event) {
		if (event instanceof FormEvent) {
			FormEvent fe = (FormEvent) event;
			formInnerEvent(userRequest, fe.getFormItemSource(), fe);
		} else if (event.getCommand().equals("validation ok")) {
			formOK(userRequest);
		}
	}

	public Authentication getAuth() {
		return auth;
	}

	public void setAuth(Authentication auth) {
		this.auth = auth;
	}

	@Override
	protected void doDispose() {

	}
}
