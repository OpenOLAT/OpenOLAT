package org.olat.course.nodes;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.course.ICourse;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.StatusDescription;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.repository.RepositoryEntry;

public class PeerReviewCourseNode extends AbstractAccessableCourseNode {
    public static final String TYPE = "peerreview";

    public PeerReviewCourseNode() {
        super(TYPE);
    }
    @Override
    public String getShortTitle() {
        return "Peer Review";
    }

    @Override
    public String getLongTitle() {
        return "Peer Review Assignment";
    }
    
    @Override
    public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
            ICourse course, UserCourseEnvironment euce) {
        org.olat.course.nodes.peerreview.ui.PeerReviewEditController childTabCtrl =
            new org.olat.course.nodes.peerreview.ui.PeerReviewEditController(ureq, wControl, euce, course, this);
        org.olat.course.nodes.CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
        return new org.olat.course.editor.NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCtrl);
    }

    @Override
    public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
            UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd,
            VisibilityFilter visibilityFilter) {
        Controller runCtrl = org.olat.core.gui.control.generic.messages.MessageUIFactory.createInfoMessage(ureq, wControl, null,
            "Peer review feature coming soon");
        return new NodeRunConstructionResult(runCtrl);
    }

    @Override
    public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
        return new StatusDescription[] { StatusDescription.NOERROR };
    }

    @Override
    public StatusDescription isConfigValid() {
        return StatusDescription.NOERROR;
    }

    @Override
    public RepositoryEntry getReferencedRepositoryEntry() {
        return null;
    }

    @Override
    public boolean needsReferenceToARepositoryEntry() {
        return false;
    }

    // Placeholder: add more logic as needed
}
