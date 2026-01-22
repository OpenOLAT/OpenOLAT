package org.olat.course.nodes.peerreview.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.nodes.PeerReviewCourseNode;
import org.olat.course.nodes.peerreview.ui.PeerReviewEditFormController;

public class PeerReviewEditController extends ActivateableTabbableDefaultController {

    public static final String PANE_TAB_CONFIG = "pane.tab.config";
    private static final String[] paneKeys = { PANE_TAB_CONFIG };

    private PeerReviewEditFormController formCtrl;

    public PeerReviewEditController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
            ICourse course, PeerReviewCourseNode node) {
        super(ureq, wControl);
        formCtrl = new PeerReviewEditFormController(ureq, wControl, course, node);
        listenTo(formCtrl);
    }

    @Override
    public void addTabs(TabbedPane tabbedPane) {
        tabbedPane.addTab(translate(PANE_TAB_CONFIG), formCtrl.getInitialComponent());
    }

    @Override
    public String[] getPaneKeys() {
        return paneKeys;
    }

    @Override
    public TabbedPane getTabbedPane() {
        return null;
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        // no-op
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == formCtrl) {
            if (event == org.olat.course.editor.NodeEditController.NODECONFIG_CHANGED_EVENT) {
                fireEvent(ureq, org.olat.course.editor.NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        }
    }
}
