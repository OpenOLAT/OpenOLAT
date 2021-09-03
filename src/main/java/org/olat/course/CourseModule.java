/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course;

import java.util.HashMap;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.style.ImageSourceType;
import org.olat.course.style.TeaserImageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial Date: 02.09.2005 <br>
 * 
 * @author Mike Stock
 * @author guido
 * @author Florian Gnägi
 * @author fkiefer
 */
@Service
public class CourseModule extends AbstractSpringModule {

	private static final String COURSE_DISPLAY_CHANGELOG = "course.display.changelog";
	private static final String COURSE_DISPLAY_INFOBOX = "course.display.infobox";
	private static final String COURSE_DISCLAIMER_ENABLED = "course.disclaimer.enabled";
	private static final String COURSE_TYPE_DEFAULT = "course.type.default";
	private static final String COURSE_STYLE_TEASER_IMAGE_SOURCE_TYPE = "course.style.teaser.image.source.type";
	private static final String COURSE_STYLE_TEASER_IMAGE_FILENAME = "course.style.teaser.image.filename";
	private static final String COURSE_STYLE_TEASER_IMAGE_STYLE = "course.style.teaser.image.style";
	
	@Value("${course.display.participants.count}")
	private boolean displayParticipantsCount;
	@Value("${help.course.softkey}")
	private String helpCourseSoftkey;
	@Autowired @Qualifier("logVisibilityForCourseAuthor")
	private HashMap<String, String> logVisibilities;
	@Value("${course.display.infobox}")
	private boolean displayInfoBox;
	@Value("${course.display.changelog}")
	private boolean displayChangeLog;
	@Value("${course.disclaimer.enabled:true}")
	private boolean disclaimerEnabled;
	@Value("${course.type.default}")
	private String courseTypeDefault;
	@Value("${course.archive.log.table.on.delete:true}")
	private String archiveLogTableOnDelete;
	@Value("${course.info.details.enabled:false}")
	private boolean infoDetailsEnabled;
	@Value("${course.style.teaser.image.type}")
	private String teaserImageSourceTypeName;
	private ImageSourceType teaserImageSourceType;
	@Value("${course.style.teaser.image.filename}")
	private String teaserImageFilename;
	@Value("${course.style.teaser.image.style}")
	private String teaserImageStyleName;
	private TeaserImageStyle teaserImageStyle;
	
	// Repository types
	public static final String ORES_TYPE_COURSE = OresHelper.calculateTypeName(CourseModule.class);
	public static final OLATResourceable ORESOURCEABLE_TYPE_COURSE = OresHelper.lookupType(CourseModule.class);
	public static final String ORES_COURSE_ASSESSMENT = OresHelper.calculateTypeName(AssessmentManager.class);
	
	private static CoordinatorManager coordinatorManager;

	@Autowired
	public CourseModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
		CourseModule.coordinatorManager = coordinatorManager;
	}
	
	@Override
	protected void initFromChangedProperties() {
		//set properties
		String userAllowed = getStringPropertyValue(COURSE_DISPLAY_INFOBOX, true);
		if(StringHelper.containsNonWhitespace(userAllowed)) {
			displayInfoBox = "true".equals(userAllowed);
		}
		String authorAllowed = getStringPropertyValue(COURSE_DISPLAY_CHANGELOG, true);
		if(StringHelper.containsNonWhitespace(authorAllowed)) {
			displayChangeLog = "true".equals(authorAllowed);
		}
		
		String disclaimerEnabledObj = getStringPropertyValue(COURSE_DISCLAIMER_ENABLED, false);
		if (StringHelper.containsNonWhitespace(disclaimerEnabledObj)) {
			disclaimerEnabled = "true".equals(disclaimerEnabledObj);
		}
		
		String courseTypeDefaultObj = getStringPropertyValue(COURSE_TYPE_DEFAULT, true);
		if (StringHelper.containsNonWhitespace(courseTypeDefaultObj)) {
			courseTypeDefault = courseTypeDefaultObj;
		}
		
		String teaserImageSourceTypeObj = getStringPropertyValue(COURSE_STYLE_TEASER_IMAGE_SOURCE_TYPE, true);
		if (StringHelper.containsNonWhitespace(teaserImageSourceTypeObj)) {
			teaserImageSourceTypeName = teaserImageSourceTypeObj;
			teaserImageSourceType = ImageSourceType.valueOf(teaserImageSourceTypeName);
		}
		
		String teaserImageFilenameObj = getStringPropertyValue(COURSE_STYLE_TEASER_IMAGE_FILENAME, true);
		if (StringHelper.containsNonWhitespace(teaserImageFilenameObj)) {
			teaserImageFilename = teaserImageFilenameObj;
		}
		
		String teaserImageStyleObj = getStringPropertyValue(COURSE_STYLE_TEASER_IMAGE_STYLE, true);
		if (StringHelper.containsNonWhitespace(teaserImageStyleObj)) {
			teaserImageStyleName = teaserImageStyleObj;
			teaserImageStyle = TeaserImageStyle.valueOf(teaserImageStyleName);
		}
	}

	@Override
	public void init() {
		initFromChangedProperties();
	}
	
	/**
	 * @return true if the course author can see/download/modify the admin log
	 */
	public boolean isAdminLogVisibleForMigrationOnly() {
		return logVisibilities.get("AdminLog").equals("VISIBLE");
	}

	/**
	 * @return true if the course author can see/download/modify the user log
	 */
	public boolean isUserLogVisibleForMigrationOnly() {
		return logVisibilities.get("UserLog").equals("VISIBLE");
	}

	/**
	 * @return true if the course author can see/download/modify the statistic log
	 */
	public boolean isStatisticLogVisibleForMigrationOnly() {
		return logVisibilities.get("StatisticLog").equals("VISIBLE");
	}


	/**
	 * 
	 * @return The filename of the zipped help course
	 */
	public String getHelpCourseSoftKey() {
		return helpCourseSoftkey;
	}
	
	/**
	 * @return type name
	 */
	public static String getCourseTypeName() {
		return ORES_TYPE_COURSE;
	}

	/**
	 * @param ce
	 * @param cn
	 * @return the generated SubscriptionContext
	 */
	public static SubscriptionContext createSubscriptionContext(CourseEnvironment ce, CourseNode cn) {
		return new SubscriptionContext(getCourseTypeName(), ce.getCourseResourceableId(), cn.getIdent());
	}

	/**
	 * @param ce
	 * @param cn
	 * @return a subscriptioncontext with no translations for the user, but only
	 *         to be able to cleanup/obtain
	 */
	public static SubscriptionContext createTechnicalSubscriptionContext(CourseEnvironment ce, CourseNode cn) {
		return new SubscriptionContext(getCourseTypeName(), ce.getCourseResourceableId(), cn.getIdent());
	}

	/**
	 * Creates subscription context which points to an element e.g. that is a sub
	 * element of a node (subsubId). E.g. inside the course node dialog elements
	 * where a course node can have several forums.
	 * 
	 * @param ce
	 * @param cn
	 * @param subsubId
	 * @return
	 */
	public static SubscriptionContext createSubscriptionContext(CourseEnvironment ce, CourseNode cn, String subsubId) {
		return new SubscriptionContext(getCourseTypeName(), ce.getCourseResourceableId(), cn.getIdent() + ":" + subsubId);
	}
	
	public static void registerForCourseType(GenericEventListener gel, Identity identity) {
		coordinatorManager.getCoordinator().getEventBus().registerFor(gel, identity, ORESOURCEABLE_TYPE_COURSE);
	}

	public static void deregisterForCourseType(GenericEventListener gel) {
		coordinatorManager.getCoordinator().getEventBus().deregisterFor(gel, ORESOURCEABLE_TYPE_COURSE);
	}

	/**
	 * max number of course nodes
	 * @return
	 */
	public static int getCourseNodeLimit() {
		return 499;
	}

	public boolean displayParticipantsCount() {
		return displayParticipantsCount;
	}

	public boolean isDisplayInfoBox() {
		return displayInfoBox;
	}

	public void setDisplayInfoBox(boolean enabled) {
		this.displayInfoBox = enabled;
		setStringProperty(COURSE_DISPLAY_INFOBOX, Boolean.toString(enabled), true);
	}

	public boolean isDisplayChangeLog() {
		return displayChangeLog;
	}

	public void setDisplayChangeLog(boolean enabled) {
		this.displayChangeLog = enabled;
		setStringProperty(COURSE_DISPLAY_CHANGELOG, Boolean.toString(enabled), true);
	}
	
	public void setDisclaimerEnabled(boolean enabled) {
		disclaimerEnabled = enabled;
		setStringProperty(COURSE_DISCLAIMER_ENABLED, Boolean.toString(enabled), true);
	}
	
	public boolean isDisclaimerEnabled() {
		return disclaimerEnabled;
	}
	
	public String getCourseTypeDefault() {
		return courseTypeDefault;
	}
	
	public void setCourseTypeDefault(String courseTypeDefault) {
		this.courseTypeDefault = courseTypeDefault;
		setStringProperty(COURSE_TYPE_DEFAULT, courseTypeDefault, true);
	}

	public boolean isArchiveLogTableOnDelete() {
		return "true".equals(archiveLogTableOnDelete);
	}

	public void setArchiveLogTableOnDelete(String archiveLogTableOnDelete) {
		this.archiveLogTableOnDelete = archiveLogTableOnDelete;
	}

	public boolean isInfoDetailsEnabled() {
		return infoDetailsEnabled;
	}

	public ImageSourceType getTeaserImageSourceType() {
		return teaserImageSourceType;
	}

	public void setTeaserImageSourceType(ImageSourceType imageSourceType) {
		this.teaserImageSourceType = imageSourceType;
		this.teaserImageSourceTypeName = imageSourceType.name();
		setStringProperty(COURSE_STYLE_TEASER_IMAGE_SOURCE_TYPE, teaserImageSourceTypeName, true);
	}

	public String getTeaserImageFilename() {
		return teaserImageFilename;
	}
	
	public void setTeaserImageFilename(String filename) {
		this.teaserImageFilename = filename;
		setStringProperty(COURSE_STYLE_TEASER_IMAGE_FILENAME, filename, true);
	}

	public TeaserImageStyle getTeaserImageStyle() {
		return teaserImageStyle;
	}

	public void setTeaserImageStyle(TeaserImageStyle teaserImageStyle) {
		this.teaserImageStyle = teaserImageStyle;
		this.teaserImageStyleName = teaserImageStyle.name();
		setStringProperty(COURSE_STYLE_TEASER_IMAGE_STYLE, teaserImageStyleName, true);
	}
	
}
