/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.olat.user;

import java.util.HashMap;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.home.InviteeHomeMainController;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.olat.user.ProfileAndHomePageEditController;
import org.olat.user.UserManager;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Description:<br>
 * This controller do change the email from a user after he has clicked a link in email. 
 * 
 * <P>
 * Initial Date:  19.05.2009 <br>
 * @author bja
 */
public class ChangeEMailExecuteController extends ChangeEMailController implements SupportsAfterLoginInterceptor {
	
	private static final String PRESENTED_EMAIL_CHANGE_REMINDER = "presentedemailchangereminder";
	

	protected static final String PACKAGE_HOME = ProfileAndHomePageEditController.class.getPackage().getName();
	
	public ChangeEMailExecuteController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		this.userRequest = ureq;
		pT = new PackageTranslator(PACKAGE, userRequest.getLocale());
		pT = UserManager.getInstance().getPropertyHandlerTranslator(pT);
		emKey = userRequest.getHttpReq().getParameter("key");
		if (emKey == null) {
			emKey = userRequest.getIdentity().getUser().getProperty("emchangeKey", null);
		}
		if (emKey != null) {
			// key exist
			// we check if given key is a valid temporary key
			tempKey = rm.loadTemporaryKeyByRegistrationKey(emKey);
		}
	}
	
	@Override
	//fxdiff BAKS-20 instantiate controllers
	public boolean isInterceptionRequired(UserRequest ureq) {
		User user = ureq.getIdentity().getUser();
		if(StringHelper.containsNonWhitespace(user.getProperty("emchangeKey", null))) {
			if (isLinkTimeUp()) {
				deleteRegistrationKey();
				return false;
			} else {
				if (isLinkClicked()) {
					changeEMail(getWindowControl());
				} else {
	    		Boolean alreadySeen = ((Boolean)ureq.getUserSession().getEntry(PRESENTED_EMAIL_CHANGE_REMINDER));
	    		if (alreadySeen == null) {
	    			getWindowControl().setWarning(getPackageTranslator().translate("email.change.reminder"));
	    			ureq.getUserSession().putEntry(PRESENTED_EMAIL_CHANGE_REMINDER, Boolean.TRUE);
	    		}
				}
			}
		} else {
			String value = user.getProperty("emailDisabled", null);
			if (value != null && value.equals("true")) {
				Translator translator = Util.createPackageTranslator(InviteeHomeMainController.class, ureq.getLocale());
				getWindowControl().setWarning(translator.translate("email.disabled"));
			}
		}
		return false;
	}

	/**
	 * change email
	 * @param wControl
	 * @return
	 */
	public boolean changeEMail(WindowControl wControl) {
		XStream xml = new XStream();
		HashMap<String, String> mails = (HashMap<String, String>) xml.fromXML(tempKey.getEmailAddress());
		Identity ident = UserManager.getInstance().findIdentityByEmail(mails.get("currentEMail"));
		if (ident != null) {
			// change mail address
			ident.getUser().setProperty("email", mails.get("changedEMail"));
			// if old mail address closed then set the new mail address
			// unclosed
			String value = ident.getUser().getProperty("emailDisabled", null);
			if (value != null && value.equals("true")) {
				ident.getUser().setProperty("emailDisabled", "false");
			}
			ident.getUser().setProperty("email", mails.get("changedEMail"));
			// success info message
			wControl.setInfo(pT.translate("success.change.email", new String[] { mails.get("currentEMail"), mails.get("changedEMail") }));
			// remove keys
			ident.getUser().setProperty("emchangeKey", null);
			userRequest.getUserSession().removeEntryFromNonClearedStore(ChangeEMailController.CHANGE_EMAIL_ENTRY);
		}
		// delete registration key
		rm.deleteTemporaryKeyWithId(tempKey.getRegistrationKey());
		
		return true;
	}
	
	public boolean isLinkClicked() {
		Object entry = userRequest.getUserSession().getEntry(ChangeEMailController.CHANGE_EMAIL_ENTRY);
		return (entry != null);
	}
	
	public Translator getPackageTranslator() {
		return pT;
	}
}
