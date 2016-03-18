package org.olat.course.nodes.video;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeGroup;
import org.olat.course.nodes.VideoCourseNode;
import org.olat.modules.video.VideoModule;

public class VideoCourseNodeConfiguration  extends AbstractCourseNodeConfiguration {

	private VideoCourseNodeConfiguration() {
		super();
	}

	@Override
	public CourseNode getInstance() {
		return new VideoCourseNode();
	}

	@Override
	public String getLinkText(Locale locale) {
		Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		Translator translator = Util.createPackageTranslator(this.getClass(), locale, fallback);
		return translator.translate("title_video");
	}

	@Override
	public String getIconCSSClass() {
		return "o_FileResource-MOVIE_icon";
	}

	@Override
	public String getAlias() {
		return "video";
	}

	@Override
	public String getGroup() {
		return CourseNodeGroup.content.name();
	}

	@Override
	public boolean isEnabled() {
		return CoreSpringFactory.getImpl(VideoModule.class).isCoursenodeEnabled();
	}

}
