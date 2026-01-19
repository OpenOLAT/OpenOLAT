package org.olat.course.nodes.peerreview.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.nodes.PeerReviewCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * Simple configuration form for Peer Review course node.
 */
public class PeerReviewEditFormController extends FormBasicController {

    private MultipleSelectionElement enabledEl;
    private IntegerElement numReviewersEl;
    private MultipleSelectionElement anonymousEl;

    private final PeerReviewCourseNode node;
    private final ModuleConfiguration modConfig;

    public static final String CONFIG_ENABLED = "peer.review.enabled";
    public static final String CONFIG_NUM_REVIEWERS = "peer.review.num.reviewers";
    public static final String CONFIG_ANONYMOUS = "peer.review.anonymous";

    public PeerReviewEditFormController(UserRequest ureq, WindowControl wControl, ICourse course, PeerReviewCourseNode node) {
        super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
        setTranslator(Util.createPackageTranslator(PeerReviewEditFormController.class, getLocale(), getTranslator()));
        this.node = node;
        this.modConfig = node.getModuleConfiguration();
        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        enabledEl = uifactory.addCheckboxesHorizontal("peer.enabled", formLayout, new String[] { "on" }, new String[] { translate("peer.enabled") });
        enabledEl.addActionListener(FormEvent.ONCLICK);

        int initNum = modConfig.getIntegerSafe(CONFIG_NUM_REVIEWERS, 1);
        numReviewersEl = uifactory.addIntegerElement("peer.num", "peer.num", initNum, formLayout);
        numReviewersEl.setDisplaySize(4);

        anonymousEl = uifactory.addCheckboxesHorizontal("peer.anon", formLayout, new String[] { "on" }, new String[] { translate("peer.anon") });

        FormLayoutContainer buttons = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
        formLayout.add(buttons);
        uifactory.addFormSubmitButton("save", buttons);

        applyModuleConfig();
    }

    private void applyModuleConfig() {
        Boolean enabled = modConfig.getBooleanEntry(CONFIG_ENABLED);
        enabledEl.select("on", enabled != null && enabled.booleanValue());
        int num = modConfig.getIntegerSafe(CONFIG_NUM_REVIEWERS, 1);
        numReviewersEl.setIntValue(num);
        Boolean anon = modConfig.getBooleanEntry(CONFIG_ANONYMOUS);
        anonymousEl.select("on", anon != null && anon.booleanValue());
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean ok = super.validateFormLogic(ureq);
        if (numReviewersEl.getIntValue() < 1) {
            numReviewersEl.setErrorKey("form.error.nointeger", null);
            ok = false;
        }
        return ok;
    }

    @Override
    protected void formOK(UserRequest ureq) {
        modConfig.setBooleanEntry(CONFIG_ENABLED, enabledEl.isSelected(0));
        modConfig.setIntValue(CONFIG_NUM_REVIEWERS, numReviewersEl.getIntValue());
        modConfig.setBooleanEntry(CONFIG_ANONYMOUS, anonymousEl.isSelected(0));
        fireEvent(ureq, org.olat.course.editor.NodeEditController.NODECONFIG_CHANGED_EVENT);
    }

    @Override
    protected void doDispose() {
        // nothing
    }
}
