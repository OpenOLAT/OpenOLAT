package org.olat.user;

import org.olat.admin.user.SystemRolesAndRightsController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Initial date: 2018-04-27<br />
 * @author Christian Meier (christian.meier@zi.uzh.ch)
 */
public class FilesPrefsForm extends FormBasicController {

    private Identity tobeChangedIdentity;
    private SingleSelection charset;
    private MultipleSelectionElement checkboxShowHiddenFiles;


    /**
     * Constructor for the user preferences form
     *
     * @param ureq
     * @param wControl
     * @param tobeChangedIdentity the Identity which preferences are displayed and
     *          edited. Not necessarily the same as ureq.getIdentity()
     */
    public FilesPrefsForm(UserRequest ureq, WindowControl wControl, Identity tobeChangedIdentity) {
        super(ureq, wControl, Util.createPackageTranslator(SystemRolesAndRightsController.class, ureq.getLocale()));
        this.tobeChangedIdentity = tobeChangedIdentity;
        initForm(ureq);
    }

    /**
     * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
     */
    protected void formOK(UserRequest ureq) {
        UserManager um = UserManager.getInstance();
        BaseSecurity secMgr = BaseSecurityManager.getInstance();
        // Refresh user from DB to prevent stale object issues
        tobeChangedIdentity = secMgr.loadIdentityByKey(tobeChangedIdentity.getKey());
        Preferences prefs = tobeChangedIdentity.getUser().getPreferences();

        um.setUserCharset(tobeChangedIdentity, charset.getSelectedKey());

        boolean on = !checkboxShowHiddenFiles.getSelectedKeys().isEmpty();
        um.setShowHiddenFiles(tobeChangedIdentity, on);

        fireEvent(ureq, Event.DONE_EVENT);
    }

    /**
     * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formCancelled(org.olat.core.gui.UserRequest)
     */
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    /**
     * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
     *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
     */
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        setFormTitle("title.prefs.files");
        //setFormContextHelp("Configuration#_einstellungen"); // TODO point to correct Help article whenever it gets written

        // load preferences
        Preferences prefs = tobeChangedIdentity.getUser().getPreferences();

        // Text encoding
        Map<String, Charset> charsets = Charset.availableCharsets();
        String currentCharset = UserManager.getInstance().getUserCharset(tobeChangedIdentity);
        String[] csKeys = StringHelper.getMapKeysAsStringArray(charsets);
        charset = uifactory.addDropdownSingleselect("form.charset", formLayout, csKeys, csKeys, null);
        charset.setElementCssClass("o_sel_home_settings_charset");
        if(currentCharset != null) {
            for(String csKey:csKeys) {
                if(csKey.equals(currentCharset)) {
                    charset.select(currentCharset, true);
                }
            }
        }
        if(!charset.isOneSelected() && charsets.containsKey("UTF-8")) {
            charset.select("UTF-8", true);
        }

        // Show hidden files in folders - LMSUZH-616
        String[] keys = { "on" };
        String[] values = new String[] { translate("form.hiddenfiles.on") };
        boolean currentShowHiddenFiles = UserManager.getInstance().getShowHiddenFiles(tobeChangedIdentity);
        checkboxShowHiddenFiles = uifactory.addCheckboxesHorizontal("form.hiddenfiles", formLayout, keys, values);
        checkboxShowHiddenFiles.select("on", currentShowHiddenFiles);

        // Submit and cancel buttons
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
        formLayout.add(buttonLayout);
        buttonLayout.setElementCssClass("o_sel_home_settings_prefs_buttons");
        uifactory.addFormSubmitButton("submit", buttonLayout);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
    }

    @Override
    protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {

    }

    /**
     * @see org.olat.core.gui.control.DefaultController#doDispose()
     */
    protected void doDispose() {
        // nothing to do
    }

}
