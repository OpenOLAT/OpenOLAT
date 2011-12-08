package org.olat.commons.info.ui;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;

public class InfoCourseNodeResourceable implements OLATResourceable {
	private final Long resId;
	
	public InfoCourseNodeResourceable(Long resId) {
		this.resId = resId;
	}
	
	@Override
	public String getResourceableTypeName() {
		return OresHelper.calculateTypeName(CourseModule.class);
	}

	@Override
	public Long getResourceableId() {
		return resId;
	}
}
