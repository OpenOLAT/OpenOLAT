package org.olat.course.nodes;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

public class PeerReviewCourseNodeConfiguration extends AbstractCourseNodeConfiguration {

    public PeerReviewCourseNodeConfiguration() {
        super();
    }

    @Override
    public CourseNode getInstance() {
        return new PeerReviewCourseNode();
    }

    @Override
    public String getLinkText(Locale locale) {
        Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
        Translator translator = Util.createPackageTranslator(this.getClass(), locale, fallback);
        return translator.translate("title_peerreview", "Peer Review");
    }

    @Override
    public String getIconCSSClass() {
        return "o_icon_peer_review";
    }

    @Override
    public String getAlias() {
        return "peerreview";
    }

    @Override
    public String getGroup() {
        return CourseNodeGroup.assessment.name();
    }
}
