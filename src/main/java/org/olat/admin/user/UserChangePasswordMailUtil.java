package org.olat.admin.user;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class UserChangePasswordMailUtil {

    private String dummyKey;

    final private RegistrationManager registrationManager;
    final private MailManager mailManager;

    private static final Logger log = Tracing.createLoggerFor(UserChangePasswordMailUtil.class);

    @Autowired
    private BaseSecurity securityManager;

    @Autowired
    public UserChangePasswordMailUtil(RegistrationManager registrationManager, MailManager mailManager) {
        this.registrationManager = registrationManager;
        this.mailManager = mailManager;
    }

    private Locale getUserLocale(Identity user) {
        Preferences prefs = user.getUser().getPreferences();
        return I18nManager.getInstance().getLocaleOrDefault(prefs.getLanguage());

    }

    public String generateMailText(Identity user) throws UserHasNoEmailException {
        Locale locale = getUserLocale(user);
        String emailAdress = user.getUser().getProperty(UserConstants.EMAIL, locale);
        if (emailAdress == null) {
            throw new UserHasNoEmailException("No email specified for " + user.getName());
        }

        String serverpath = Settings.getServerContextPathURI();
        Translator userTrans = Util.createPackageTranslator(RegistrationManager.class, locale);
        dummyKey = getDummyKey(emailAdress);

        return userTrans.translate("pwchange.intro", new String[] { user.getName() })
                + userTrans.translate("pwchange.body", new String[] {
                serverpath, dummyKey, I18nModule.getLocaleKey(locale)
        });
    }

    public String getDummyKey(String emailAdress) {
        return Encoder.md5hash(emailAdress);
    }

    public MailerResult sendTokenByMail(UserRequest ureq, Identity user, String text) throws UserChangePasswordException, UserHasNoEmailException {
        Locale sessionLocale = ureq.getUserSession().getLocale();
        Translator sessionTrans = Util.createPackageTranslator(RegistrationManager.class, sessionLocale);
        if (ureq == null) {
            throw new UserChangePasswordException(sessionTrans.translate("error.sendTokenByMail.no.request"));
        } else if (user == null) {
            throw new UserChangePasswordException(sessionTrans.translate("error.sendTokenByMail.no.user"));
        } else if (text == null) {
            throw new UserChangePasswordException(sessionTrans.translate("error.sendTokenByMail.no.text"));
        } else if (registrationManager == null) {
            throw new UserChangePasswordException(sessionTrans.translate("error.sendTokenByMail.no.regmanager"));
        } else if (mailManager == null) {
            throw new UserChangePasswordException(sessionTrans.translate("error.sendTokenByMail.no.mailmanager"));
        }

		// We allow creation of password token when user has no password so far or when he as an OpenOLAT Password.
		// For other cases such as Shibboleth, LDAP, oAuth etc. we don't allow creation of token as this is most
		// likely not a desired action.
	        List<Authentication> authentications = securityManager.getAuthentications(user);
		boolean isOOpwdAllowed = authentications.isEmpty();
		for (Authentication authentication : authentications) {
			if (authentication.getProvider().equals(BaseSecurityModule.getDefaultAuthProviderIdentifier())) {
				isOOpwdAllowed = true;
			}
		}
		if (!isOOpwdAllowed) {
			log.error("sendtoken.wrong.auth");
			throw new UserChangePasswordException(sessionTrans.translate("sendtoken.wrong.auth", new String[] { user.getName() }));
		}

        Locale locale = getUserLocale(user);
        String emailAdress = user.getUser().getProperty(UserConstants.EMAIL, locale);
        if (emailAdress == null) {
            log.error("No email specified for " + user.getName());
            throw new UserHasNoEmailException(sessionTrans.translate("error.sendTokenByMail.no.email", new String[] { user.getName() }));
        }

        // Validate if template corresponds to our expectations (should contain dummy key)
        if (!text.contains(getDummyKey(emailAdress))) {
			log.warn("Can not replace temporary registration token in change pwd mail token dialog, user probably changed temporary token in mai template");
            log.error("Dummy key not found in prepared email");
            throw new UserChangePasswordException(sessionTrans.translate("error.sendTokenByMail.no.dummy.key"));
        }

        String body;
        try {
            TemporaryKey tk = registrationManager.loadTemporaryKeyByEmail(emailAdress);
            if (tk == null) {
                String ip = ureq.getHttpReq().getRemoteAddr();
                tk = registrationManager.createTemporaryKeyByEmail(emailAdress, ip, RegistrationManager.PW_CHANGE);
            }
            body = text.replace(dummyKey, tk.getRegistrationKey());
        } catch (Exception e) {
            throw new UserChangePasswordException(sessionTrans.translate("error.sendTokenByMail.message.not.prepared"));
        }

        MailerResult result;
        try {
            Translator userTrans = Util.createPackageTranslator(RegistrationManager.class, locale) ;
            MailBundle bundle = new MailBundle();
            bundle.setToId(user);
            bundle.setContent(userTrans.translate("pwchange.subject"), body);
            result = mailManager.sendExternMessage(bundle, null, false);
        } catch (Exception e) {
            throw new UserChangePasswordException(sessionTrans.translate("error.sendTokenByMail.message.not.sent"));
        }

        return result;
    }

}

