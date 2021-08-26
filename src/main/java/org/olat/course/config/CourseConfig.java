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

package org.olat.course.config;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.certificate.RecertificationTimeUnit;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.ImageSource;
import org.olat.course.style.TeaserImageStyle;

/**
 * Description: <br>
 * The CourseConfig class represents a configuration for a course. It is
 * persisted as an xml file in the course folder. Loading and saving of
 * configuration files are in conjunction with an ICourse and managed through
 * the CourseConfigManagerImpl.
 * <p>
 * A CourseConfig has a class wide <code>CURRENTVERSION</code>, an integer
 * denoting the most actual code version of the CourseConfig. The attribute
 * <code>version</code> is the version number of an instance.
 * <p>
 * <b><code>CURRENTVERSION vs. version</code> </b>
 * <ul>
 * <li><code>version == CURRENTVERSION</code><br>
 * an up to date CourseConfig</li>
 * <li><code>version &lt; CURRENTVERSION</code><br>
 * demands for resolving version differences, as the loaded CourseConfig may
 * contain outdated configuration information</li>
 * </ul>
 * <P>
 * 
 * Initial Date: Jun 3, 2005 <br>
 * 
 * @author patrick
 */
public class CourseConfig implements Serializable, Cloneable {

	private static final long serialVersionUID = -1158707796830204185L;
	/**
	 * <code>VALUE_EMPTY_CSS_FILEREF</code> is the <i>filename </i>
	 */
	public static final transient String VALUE_EMPTY_CSS_FILEREF = "form.layout.setsystemcss";
	private static final transient String OLD_VALUE_EMPTY_CSS_FILEREF = "::EmPtY::";
	/**
	 * <code>VALUE_EMPTY_SHAREDFOLDER_SOFTKEY</code> is the <i>softkey</i>
	 */
	public static final transient String VALUE_EMPTY_SHAREDFOLDER_SOFTKEY = "sf.notconfigured";
	/**
	 * current config file version
	 */
	private static final transient int CURRENTVERSION = 20;

	public static final transient String KEY_LOGLEVEL_ADMIN = "LOGLEVELADMIN";
	public static final transient String KEY_LOGLEVEL_USER = "LOGLEVELUSER";
	public static final transient String KEY_LOGLEVEL_STATISTIC = "LOGLEVELSTAT";

	public static final transient String NODE_ACCESS_TYPE = "NODE_ACCESS_TYPE";
	public static final transient String NODE_ACCESS_TYPE_DEFAULT = LearningPathNodeAccessProvider.TYPE;

	public static final transient String KEY_EFFICENCY_ENABLED = "KEY_EFFICENCY_ENABLED";
	public static final transient String CERTIFICATE_AUTO_ENABLED = "CERTIFICATE_AUTO";
	public static final transient String CERTIFICATE_MANUAL_ENABLED = "CERTIFICATE_MANUAL";
	public static final transient String CERTIFICATE_TEMPLATE = "CERTIFICATE_TEMPLATE";
	public static final transient String CERTIFICATE_CUSTOM1 = "CERTIFICATE_CUSTOM1";
	public static final transient String CERTIFICATE_CUSTOM2 = "CERTIFICATE_CUSTOM2";
	public static final transient String CERTIFICATE_CUSTOM3 = "CERTIFICATE_CUSTOM3";
	public static final transient String RECERTIFICATION_ENABLED = "RECERTIFICATION_ENABLED";
	public static final transient String RECERTIFICATION_TIMELAPSE = "RECERTIFICATION_TIMELAPSE";
	public static final transient String RECERTIFICATION_TIMELAPSE_UNIT = "RECERTIFICATION_TIMELAPSE_UNIT";

	public static final transient String MENU_ENABLED = "MENU_ENABLED";
	public static final transient String TOOLBAR_ENABLED = "TOOLBAR_ENABLED";
	public static final transient String BREADCRUMB_ENABLED = "BREADCRUMB_ENABLED";
	public static final transient String NODE_TEASER_IMAGE_SOURCE = "NODE_TEASER_IMAGE_SOURCE";
	public static final transient String NODE_TEASER_IMAGE_STYLE = "NODE_TEASER_IMAGE_STYLE";
	public static final transient String NODE_COLOR_CATEGORY_IDENITFIER = "NODE_COLOR_CATEGORY_IDENITFIER";

	public static final transient String COURSESEARCH_ENABLED = "COURSESEARCH_ENABLED";
	public static final transient String KEY_CHAT_ENABLED = "COURSE_CHAT_ENABLED";
	public static final transient String PARTICIPANT_LIST_ENABLED = "PARTICIPANT_LIST_ENABLED";
	public static final transient String PARTICIPANT_INFO_ENABLED = "PARTICIPANT_INFO_ENABLED";
	public static final transient String EMAIL_ENABLED = "EMAIL_ENABLED";
	public static final transient String BLOG_ENABLED = "BLOG_ENABLED";
	public static final transient String BLOG_SOFTKEY = "BLOG_SOFTKEY";
	public static final transient String FORUM_ENABLED = "FORUM_ENABLED";
	public static final transient String WIKI_ENABLED = "WIKI_ENABLED";
	public static final transient String WIKI_SOFTKEY = "WIKI_SOFTKEY";
	public static final transient String DOCUMENTS_ENABLED = "DOCUMENTS_ENABLED";
	public static final transient String DOCUMENTS_PATH = "DOCUMENTS_PATH";
	public static final transient String KEY_CALENDAR_ENABLED = "KEY_CALENDAR_ENABLED";
	public static final transient String TEAMS_ENABLED = "TEAMS_ENABLED";
	public static final transient String BIGBLUEBUTTON_ENABLED = "BIGBLUEBUTTON_ENABLED";
	public static final transient String BIGBLUEBUTTON_MODERATOR_STARTS_MEETING = "BIGBLUEBUTTON_MODERATOR_STARTS_MEETING";

	public static final transient String KEY_GLOSSARY_ENABLED = "KEY_GLOSSARY_ENABLED";
	public static final transient String KEY_GLOSSARY_SOFTKEY = "KEY_GLOSSARY_SOFTKEY";
	public static final transient String KEY_CSS_FILEREF = "CSS_FILEREF";

	public static final transient String KEY_SHAREDFOLDER_SOFTKEY = "SHAREDFOLDER_SOFTKEY";
	public static final transient String KEY_SHAREDFOLDER_READONLY = "SHAREDFOLDER_RO";

	private static final transient String KEY_COMPLETION_TYPE = "COMPLETION_TYPE";

	private static final transient String DISCLAIMER_1_ENABLED = "DISCLAIMER_1_ENABLED";
	private static final transient String DISCLAIMER_1_TITLE = "DISCLAIMER_1_TITLE";
	private static final transient String DISCLAIMER_1_TERMS = "DISCLAIMER_1_TERMS";
	private static final transient String DISCLAIMER_1_LABEL_1 = "DISCLAIMER_1_LABEL_1";
	private static final transient String DISCLAIMER_1_LABEL_2 = "DISCLAIMER_1_LABEL_2";

	private static final transient String DISCLAIMER_2_ENABLED = "DISCLAIMER_2_ENABLED";
	private static final transient String DISCLAIMER_2_TITLE = "DISCLAIMER_2_TITLE";
	private static final transient String DISCLAIMER_2_TERMS = "DISCLAIMER_2_TERMS";
	private static final transient String DISCLAIMER_2_LABEL_1 = "DISCLAIMER_2_LABEL_1";
	private static final transient String DISCLAIMER_2_LABEL_2 = "DISCLAIMER_2_LABEL_2";


	/**
	 * current key set
	 */
	private static final transient String[] KEYS = { KEY_CHAT_ENABLED, KEY_CSS_FILEREF, KEY_SHAREDFOLDER_SOFTKEY };
	/**
	 * config file version from file
	 */
	private int version = -1;
	/**
	 * holds the configuration
	 */
	private Map<String, Object> configuration = new Hashtable<>();

	public CourseConfig() {
		// empty, for XSTream
	}

	/**
	 * @return version of this loaded/created instance
	 */
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * initialize with default values
	 */
	public void initDefaults() {
		// version 1
		// version 2
		configuration.put(KEY_CHAT_ENABLED, Boolean.FALSE);
		// version 3
		configuration.put(KEY_CSS_FILEREF, VALUE_EMPTY_CSS_FILEREF);
		// version 4
		configuration.put(KEY_SHAREDFOLDER_SOFTKEY, VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
		// version 6
		configuration.put(KEY_EFFICENCY_ENABLED, Boolean.FALSE);
		// version 7
		configuration.put(KEY_CALENDAR_ENABLED, Boolean.FALSE);
		// version 8
		// added glossary configuration. no default configuration needed
		// version 9
		configuration.remove(KEY_LOGLEVEL_ADMIN);
		configuration.remove(KEY_LOGLEVEL_USER);
		configuration.remove(KEY_LOGLEVEL_STATISTIC);

		configuration.put(MENU_ENABLED, Boolean.TRUE);
		configuration.put(TOOLBAR_ENABLED, Boolean.TRUE);

		configuration.put(COURSESEARCH_ENABLED, Boolean.TRUE);
		configuration.put(PARTICIPANT_LIST_ENABLED, Boolean.FALSE);
		configuration.put(PARTICIPANT_INFO_ENABLED, Boolean.FALSE);
		configuration.put(EMAIL_ENABLED, Boolean.FALSE);
		configuration.put(BLOG_ENABLED, Boolean.FALSE);
		configuration.put(FORUM_ENABLED, Boolean.FALSE);
		configuration.put(WIKI_ENABLED, Boolean.FALSE);
		configuration.put(DOCUMENTS_ENABLED, Boolean.FALSE);
		configuration.put(TEAMS_ENABLED, Boolean.FALSE);
		configuration.put(BIGBLUEBUTTON_ENABLED, Boolean.FALSE);
		configuration.put(BIGBLUEBUTTON_MODERATOR_STARTS_MEETING, Boolean.TRUE);
		
		configuration.put(NODE_ACCESS_TYPE, NODE_ACCESS_TYPE_DEFAULT);
		configuration.put(KEY_COMPLETION_TYPE, CompletionType.numberOfNodes.name());

		// Version 20 
		configuration.put(DISCLAIMER_1_ENABLED, Boolean.FALSE);
		configuration.put(DISCLAIMER_1_TITLE, "");
		configuration.put(DISCLAIMER_1_TERMS, "");
		configuration.put(DISCLAIMER_1_LABEL_1, "");
		configuration.put(DISCLAIMER_1_LABEL_2, "");

		configuration.put(DISCLAIMER_2_ENABLED, Boolean.FALSE);
		configuration.put(DISCLAIMER_2_TITLE, "");
		configuration.put(DISCLAIMER_2_TERMS, "");
		configuration.put(DISCLAIMER_2_LABEL_1, "");
		configuration.put(DISCLAIMER_2_LABEL_2, "");
		
		configuration.put(NODE_TEASER_IMAGE_STYLE, TeaserImageStyle.DEFAULT_COURSE);
		configuration.put(NODE_COLOR_CATEGORY_IDENITFIER, ColorCategory.IDENTIFIER_DEFAULT_COURSE);

		this.version = CURRENTVERSION;
	}

	/**
	 * resolve issues of changed configuration version. Such as:
	 * <ul>
	 * <li>add new default values <br>
	 * <b>&gt;&gt;add the them </b></li>
	 * <li>no longer exisiting key value pairs <br>
	 * <b>&gt;&gt;remove from configuration </b></li>
	 * <li>changing of value meanings/types <br>
	 * <b>&gt;&gt;convert the existing entries </b></li>
	 * </ul>
	 */
	public boolean resolveVersionIssues() {
		boolean versionChanged = false;
		if (version < CURRENTVERSION) {
			if (version == 1) {
				this.version = 2;
			}

			if (version == 2) {
				if (!configuration.containsKey(KEY_CHAT_ENABLED))
					configuration.put(KEY_CHAT_ENABLED, Boolean.TRUE);
				this.version = 3;
			}

			if (version == 3) {
				if (!configuration.containsKey(KEY_CSS_FILEREF))
					configuration.put(KEY_CSS_FILEREF, VALUE_EMPTY_CSS_FILEREF);
				this.version = 4;
			}

			if (version == 4) {
				if (!configuration.containsKey(KEY_SHAREDFOLDER_SOFTKEY))
					configuration.put(KEY_SHAREDFOLDER_SOFTKEY, VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
				this.version = 5;
			}

			if (version == 5) {
				if (!configuration.containsKey(KEY_EFFICENCY_ENABLED))
					configuration.put(KEY_EFFICENCY_ENABLED, Boolean.FALSE);
				this.version = 6;
			}

			if (version == 6) {
				if (!configuration.containsKey(KEY_CALENDAR_ENABLED))
					configuration.put(KEY_CALENDAR_ENABLED, Boolean.TRUE);
				this.version = 7;
			}

			if (version == 7) {
				// glossary configuration has been added. no default values needed
				this.version = 8;
			}

			if (version == 8) {
				if (configuration.containsKey(KEY_LOGLEVEL_ADMIN))
					configuration.remove(KEY_LOGLEVEL_ADMIN);
				if (configuration.containsKey(KEY_LOGLEVEL_USER))
					configuration.remove(KEY_LOGLEVEL_USER);
				if (configuration.containsKey(KEY_LOGLEVEL_STATISTIC))
					configuration.remove(KEY_LOGLEVEL_STATISTIC);
				this.version = 9;
			}

			if (version == 9) {
				if (!configuration.containsKey(CERTIFICATE_AUTO_ENABLED))
					configuration.put(CERTIFICATE_AUTO_ENABLED, Boolean.FALSE);
				if (!configuration.containsKey(CERTIFICATE_MANUAL_ENABLED))
					configuration.put(CERTIFICATE_MANUAL_ENABLED, Boolean.FALSE);
				if (!configuration.containsKey(CERTIFICATE_TEMPLATE))
					configuration.put(CERTIFICATE_TEMPLATE, "");
				if (!configuration.containsKey(RECERTIFICATION_ENABLED))
					configuration.put(RECERTIFICATION_ENABLED, Boolean.FALSE);
				if (!configuration.containsKey(RECERTIFICATION_TIMELAPSE))
					configuration.put(RECERTIFICATION_TIMELAPSE, Integer.valueOf(0));
				this.version = 10;
			}

			if (version == 10) {
				if (!configuration.containsKey(MENU_ENABLED))
					configuration.put(MENU_ENABLED, Boolean.TRUE);
				if (!configuration.containsKey(TOOLBAR_ENABLED))
					configuration.put(TOOLBAR_ENABLED, Boolean.TRUE);
				this.version = 11;
			}

			if (version == 11) {
				if (!configuration.containsKey(COURSESEARCH_ENABLED))
					configuration.put(COURSESEARCH_ENABLED, Boolean.FALSE);
				this.version = 12;
			}

			if (version == 12) {
				if (!configuration.containsKey(BREADCRUMB_ENABLED))
					configuration.put(BREADCRUMB_ENABLED, Boolean.TRUE);
				this.version = 13;
			}

			if (version == 13) {
				if (!configuration.containsKey(CERTIFICATE_CUSTOM1))
					configuration.put(CERTIFICATE_CUSTOM1, "");
				if (!configuration.containsKey(CERTIFICATE_CUSTOM2))
					configuration.put(CERTIFICATE_CUSTOM2, "");
				if (!configuration.containsKey(CERTIFICATE_CUSTOM3))
					configuration.put(CERTIFICATE_CUSTOM3, "");

				this.version = 14;
			}

			if (version == 14) {
				if (!configuration.containsKey(PARTICIPANT_LIST_ENABLED))
					configuration.put(PARTICIPANT_LIST_ENABLED, Boolean.FALSE);
				if (!configuration.containsKey(PARTICIPANT_INFO_ENABLED))
					configuration.put(PARTICIPANT_INFO_ENABLED, Boolean.FALSE);
				if (!configuration.containsKey(EMAIL_ENABLED))
					configuration.put(EMAIL_ENABLED, Boolean.FALSE);

				this.version = 15;
			}

			if (version == 15) {
				if (!configuration.containsKey(NODE_ACCESS_TYPE))
					configuration.put(NODE_ACCESS_TYPE, ConditionNodeAccessProvider.TYPE);

				this.version = 16;
			}

			if (version == 16) {
				if (!configuration.containsKey(FORUM_ENABLED))
					configuration.put(FORUM_ENABLED, Boolean.FALSE);
				if (!configuration.containsKey(DOCUMENTS_ENABLED))
					configuration.put(DOCUMENTS_ENABLED, Boolean.FALSE);

				this.version = 17;
			}

			if (version == 17) {
				if (!configuration.containsKey(KEY_COMPLETION_TYPE)) {
					configuration.put(KEY_COMPLETION_TYPE, CompletionType.numberOfNodes.name());
				}

				this.version = 18;
			}

			if (version == 18) {
				if (!configuration.containsKey(BLOG_ENABLED))
					configuration.put(BLOG_ENABLED, Boolean.FALSE);
				if (!configuration.containsKey(WIKI_ENABLED))
					configuration.put(WIKI_ENABLED, Boolean.FALSE);

				this.version = 19;
			}

			if (version == 19) {
				if (!configuration.containsKey(DISCLAIMER_1_ENABLED)) {
					configuration.put(DISCLAIMER_1_ENABLED, Boolean.FALSE);
				}
				if (!configuration.containsKey(DISCLAIMER_1_TITLE)) {
					configuration.put(DISCLAIMER_1_TITLE, "");
				}
				if (!configuration.containsKey(DISCLAIMER_1_TERMS)) {
					configuration.put(DISCLAIMER_1_TERMS, "");
				}
				if (!configuration.containsKey(DISCLAIMER_1_LABEL_1)) {
					configuration.put(DISCLAIMER_1_LABEL_1, "");
				}
				if (!configuration.containsKey(DISCLAIMER_1_LABEL_2)) {
					configuration.put(DISCLAIMER_1_LABEL_2, "");
				} 
				
				if (!configuration.containsKey(DISCLAIMER_2_ENABLED)) {
					configuration.put(DISCLAIMER_2_ENABLED, Boolean.FALSE);
				}
				if (!configuration.containsKey(DISCLAIMER_2_TITLE)) {
					configuration.put(DISCLAIMER_2_TITLE, "");
				}
				if (!configuration.containsKey(DISCLAIMER_2_TERMS)) {
					configuration.put(DISCLAIMER_2_TERMS, "");
				}
				if (!configuration.containsKey(DISCLAIMER_2_LABEL_1)) {
					configuration.put(DISCLAIMER_2_LABEL_1, "");
				}
				if (!configuration.containsKey(DISCLAIMER_2_LABEL_2)) {
					configuration.put(DISCLAIMER_2_LABEL_2, "");
				}

				this.version = 20;
			}
			
			if(version == 20) {
				if (!configuration.containsKey(TEAMS_ENABLED)) {
					configuration.put(TEAMS_ENABLED, Boolean.FALSE);
				}
				if (!configuration.containsKey(BIGBLUEBUTTON_ENABLED)) {
					configuration.put(BIGBLUEBUTTON_ENABLED, Boolean.FALSE);
				}
				if (!configuration.containsKey(BIGBLUEBUTTON_MODERATOR_STARTS_MEETING)) {
					configuration.put(BIGBLUEBUTTON_MODERATOR_STARTS_MEETING, Boolean.TRUE);
				}
				if (!configuration.containsKey(NODE_TEASER_IMAGE_STYLE)) {
					configuration.put(NODE_TEASER_IMAGE_STYLE, TeaserImageStyle.DEFAULT_COURSE);
				}
				if (!configuration.containsKey(NODE_COLOR_CATEGORY_IDENITFIER)) {
					configuration.put(NODE_COLOR_CATEGORY_IDENITFIER, ColorCategory.IDENTIFIER_DEFAULT_COURSE);
				}
			}

			/*
			 * after resolving the issues, the version number is merged to the
			 * CURRENTVERSION !! leave this!
			 */

			this.version = CURRENTVERSION;
			versionChanged = true;
		} else if (version > CURRENTVERSION) {
			// this is an error
		}
		/*
		 * otherwise the version == CURRENTVERSION no version issues to resolve! but
		 * maybe other stuff to resolve!!!
		 */
		/*
		 * resolve issue of changing defaultvalue: before the default entry was
		 * ::EmPtY:: now it is form.layout.setsystemcss To have old configuration files
		 * beeing compatible they have to be converted.
		 */
		if (configuration.containsKey(KEY_CSS_FILEREF)) {
			String keyCss = (String) configuration.get(KEY_CSS_FILEREF);
			if (keyCss.equals(OLD_VALUE_EMPTY_CSS_FILEREF)) {
				configuration.put(KEY_CSS_FILEREF, VALUE_EMPTY_CSS_FILEREF);
				versionChanged = true;
			}
		}
		return versionChanged;
	}

	public void setNodeAccessType(String nodeAccessType) {
		configuration.put(NODE_ACCESS_TYPE, nodeAccessType);
	}

	public NodeAccessType getNodeAccessType() {
		return NodeAccessType.of((String) configuration.get(NODE_ACCESS_TYPE));
	}

	public CompletionType getCompletionType() {
		String completionEvaluationStr = (String) configuration.get(KEY_COMPLETION_TYPE);
		return CompletionType.valueOf(completionEvaluationStr);
	}

	public void setCompletionType(CompletionType completionType) {
		String completionTypeStr = completionType != null ? completionType.name() : CompletionType.none.name();
		configuration.put(KEY_COMPLETION_TYPE, completionTypeStr);
	}

	public boolean isChatEnabled() {
		Boolean bool = (Boolean) configuration.get(KEY_CHAT_ENABLED);
		return bool != null && bool.booleanValue();
	}

	public void setChatIsEnabled(boolean b) {
		configuration.put(KEY_CHAT_ENABLED, Boolean.valueOf(b));
	}

	public boolean isGlossaryEnabled() {
		Boolean bool = (Boolean) configuration.get(KEY_GLOSSARY_ENABLED);
		return bool != null && bool.booleanValue();
	}

	public void setGlossaryIsEnabled(boolean b) {
		configuration.put(KEY_GLOSSARY_ENABLED, Boolean.valueOf(b));
	}

	/**
	 * set the course layout by adding a reference to a css file, or disabling
	 * custom layout by adding the empty css fileref
	 * 
	 * @param cssLayoutRef
	 * @see CourseConfig#VALUE_EMPTY_CSS_FILEREF
	 */
	public void setCssLayoutRef(String cssLayoutRef) {
		configuration.put(KEY_CSS_FILEREF, cssLayoutRef);
	}

	/**
	 * @return reference to a css file in the course folder
	 */
	public String getCssLayoutRef() {
		return (String) configuration.get(KEY_CSS_FILEREF);
	}

	/**
	 * set the glossary softkey for this course or null if not used. A NULL value
	 * will remove the glossary configuration
	 * 
	 * @param glossarySoftkey
	 */
	public void setGlossarySoftKey(String glossarySoftkey) {
		if (glossarySoftkey == null) {
			if (configuration.containsKey(KEY_GLOSSARY_SOFTKEY)) {
				configuration.remove(KEY_GLOSSARY_SOFTKEY);
			}
		} else {
			configuration.put(KEY_GLOSSARY_SOFTKEY, glossarySoftkey);
		}
	}

	/**
	 * @return softkey of the course glossary resource. Can be NULL
	 */
	public String getGlossarySoftKey() {
		return (String) configuration.get(KEY_GLOSSARY_SOFTKEY);
	}

	public boolean hasGlossary() {
		return (getGlossarySoftKey() != null);
	}

	public boolean hasCustomCourseCSS() {
		return !(VALUE_EMPTY_CSS_FILEREF.equals(getCssLayoutRef()));
	}

	public void setSharedFolderSoftkey(String softkey) {
		if (softkey == null) {
			configuration.put(KEY_SHAREDFOLDER_SOFTKEY, VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
		} else {
			configuration.put(KEY_SHAREDFOLDER_SOFTKEY, softkey);
		}
	}

	public String getSharedFolderSoftkey() {
		return (String) configuration.get(KEY_SHAREDFOLDER_SOFTKEY);
	}

	public boolean hasCustomSharedFolder() {
		return !(VALUE_EMPTY_SHAREDFOLDER_SOFTKEY.equals(getSharedFolderSoftkey()));
	}

	public boolean isSharedFolderReadOnlyMount() {
		Object obj = configuration.get(KEY_SHAREDFOLDER_READONLY);
		return (obj == null || !Boolean.FALSE.equals(obj));
	}

	public void setSharedFolderReadOnlyMount(boolean mount) {
		configuration.put(KEY_SHAREDFOLDER_READONLY, Boolean.valueOf(mount));
	}

	public void setEfficencyStatementIsEnabled(boolean b) {
		configuration.put(KEY_EFFICENCY_ENABLED, Boolean.valueOf(b));
	}

	public boolean isEfficencyStatementEnabled() {
		Boolean bool = (Boolean) configuration.get(KEY_EFFICENCY_ENABLED);
		return bool.booleanValue();
	}

	public Long getCertificateTemplate() {
		Object templateIdObj = configuration.get(CERTIFICATE_TEMPLATE);
		Long templateId = null;
		if (templateIdObj instanceof Long) {
			templateId = (Long) templateIdObj;
		}
		return templateId;
	}

	public String getCertificateCustom1() {
		return (String) configuration.get(CERTIFICATE_CUSTOM1);
	}

	public void setCertificateCustom1(String custom1) {
		if (custom1 != null) {
			configuration.put(CERTIFICATE_CUSTOM1, custom1);
		} else {
			configuration.remove(CERTIFICATE_CUSTOM1);
		}
	}

	public String getCertificateCustom2() {
		return (String) configuration.get(CERTIFICATE_CUSTOM2);
	}

	public void setCertificateCustom2(String custom2) {
		if (custom2 != null) {
			configuration.put(CERTIFICATE_CUSTOM2, custom2);
		} else {
			configuration.remove(CERTIFICATE_CUSTOM2);
		}
	}

	public String getCertificateCustom3() {
		return (String) configuration.get(CERTIFICATE_CUSTOM3);
	}

	public void setCertificateCustom3(String custom3) {
		if (custom3 != null) {
			configuration.put(CERTIFICATE_CUSTOM3, custom3);
		} else {
			configuration.remove(CERTIFICATE_CUSTOM3);
		}
	}

	public void setCertificateTemplate(Long templateId) {
		if (templateId != null) {
			configuration.put(CERTIFICATE_TEMPLATE, templateId);
		} else {
			configuration.remove(CERTIFICATE_TEMPLATE);
		}
	}

	public boolean isCertificateEnabled() {
		return isAutomaticCertificationEnabled() || isManualCertificationEnabled();
	}

	public boolean isAutomaticCertificationEnabled() {
		Boolean bool = (Boolean) configuration.get(CERTIFICATE_AUTO_ENABLED);
		return bool != null && bool.booleanValue();
	}

	public void setAutomaticCertificationEnabled(boolean enabled) {
		configuration.put(CERTIFICATE_AUTO_ENABLED, Boolean.valueOf(enabled));
	}

	public boolean isManualCertificationEnabled() {
		Boolean bool = (Boolean) configuration.get(CERTIFICATE_MANUAL_ENABLED);
		return bool != null && bool.booleanValue();
	}

	public void setManualCertificationEnabled(boolean enabled) {
		configuration.put(CERTIFICATE_MANUAL_ENABLED, Boolean.valueOf(enabled));
	}

	public boolean isRecertificationEnabled() {
		Boolean bool = (Boolean) configuration.get(RECERTIFICATION_ENABLED);
		return bool != null && bool.booleanValue();
	}

	public void setRecertificationEnabled(boolean b) {
		configuration.put(RECERTIFICATION_ENABLED, Boolean.valueOf(b));
	}

	public int getRecertificationTimelapse() {
		Object timelapse = configuration.get(RECERTIFICATION_TIMELAPSE);
		if (timelapse instanceof Integer) {
			return ((Integer) timelapse).intValue();
		}
		return 0;
	}

	public void setRecertificationTimelapse(int timelapse) {
		configuration.put(RECERTIFICATION_TIMELAPSE, Integer.valueOf(timelapse));
	}

	public RecertificationTimeUnit getRecertificationTimelapseUnit() {
		String timelapseUnit = (String) configuration.get(RECERTIFICATION_TIMELAPSE_UNIT);
		RecertificationTimeUnit timeUnit = null;
		if (StringHelper.containsNonWhitespace(timelapseUnit)) {
			timeUnit = RecertificationTimeUnit.valueOf(timelapseUnit);
		}
		return timeUnit;
	}

	public void setRecertificationTimelapseUnit(RecertificationTimeUnit timeUnit) {
		if (timeUnit == null) {
			configuration.remove(RECERTIFICATION_TIMELAPSE_UNIT);
		} else {
			configuration.put(RECERTIFICATION_TIMELAPSE_UNIT, timeUnit.name());
		}
	}

	public boolean isCalendarEnabled() {
		Boolean bool = (Boolean) configuration.get(KEY_CALENDAR_ENABLED);
		return bool != null && bool.booleanValue();
	}

	public void setCalendarEnabled(boolean b) {
		configuration.put(KEY_CALENDAR_ENABLED, Boolean.valueOf(b));
	}

	public boolean isMenuEnabled() {
		Boolean bool = (Boolean) configuration.get(MENU_ENABLED);
		return bool.booleanValue();
	}

	public void setMenuEnabled(boolean b) {
		configuration.put(MENU_ENABLED, Boolean.valueOf(b));
	}

	public boolean isCourseSearchEnabled() {
		Boolean bool = (Boolean) configuration.get(COURSESEARCH_ENABLED);
		return bool.booleanValue();
	}

	public void setCourseSearchEnabled(boolean b) {
		configuration.put(COURSESEARCH_ENABLED, Boolean.valueOf(b));
	}

	public boolean isParticipantListEnabled() {
		Boolean bool = (Boolean) configuration.get(PARTICIPANT_LIST_ENABLED);
		return bool.booleanValue();
	}

	public void setParticipantListEnabled(boolean b) {
		configuration.put(PARTICIPANT_LIST_ENABLED, Boolean.valueOf(b));
	}

	public boolean isParticipantInfoEnabled() {
		Boolean bool = (Boolean) configuration.get(PARTICIPANT_INFO_ENABLED);
		return bool.booleanValue();
	}

	public void setParticipantInfoEnabled(boolean b) {
		configuration.put(PARTICIPANT_INFO_ENABLED, Boolean.valueOf(b));
	}

	public boolean isEmailEnabled() {
		Boolean bool = (Boolean) configuration.get(EMAIL_ENABLED);
		return bool.booleanValue();
	}

	public void setEmailEnabled(boolean b) {
		configuration.put(EMAIL_ENABLED, Boolean.valueOf(b));
	}
	
	public boolean isTeamsEnabled() {
		Boolean bool = (Boolean) configuration.get(TEAMS_ENABLED);
		return bool != null && bool.booleanValue();
	}

	public void setTeamsEnabled(boolean b) {
		configuration.put(TEAMS_ENABLED, Boolean.valueOf(b));
	}
	
	public boolean isBigBlueButtonEnabled() {
		Boolean bool = (Boolean) configuration.get(BIGBLUEBUTTON_ENABLED);
		return bool != null && bool.booleanValue();
	}

	public void setBigBlueButtonEnabled(boolean b) {
		configuration.put(BIGBLUEBUTTON_ENABLED, Boolean.valueOf(b));
	}
	
	public boolean isBigBlueButtonModeratorStartsMeeting() {
		Boolean bool = (Boolean) configuration.get(BIGBLUEBUTTON_MODERATOR_STARTS_MEETING);
		return bool == null || bool.booleanValue();
	}

	public void setBigBlueButtonModeratorStartsMeeting(boolean b) {
		configuration.put(BIGBLUEBUTTON_MODERATOR_STARTS_MEETING, Boolean.valueOf(b));
	}

	public boolean isBlogEnabled() {
		Boolean bool = (Boolean) configuration.get(BLOG_ENABLED);
		return bool != null && bool.booleanValue();
	}

	public void setBlogEnabled(boolean b) {
		configuration.put(BLOG_ENABLED, Boolean.valueOf(b));
	}

	public String getBlogSoftKey() {
		Object softKey = configuration.get(BLOG_SOFTKEY);
		return softKey != null? (String) softKey: null;
	}

	public void setBlogSoftKey(String blogSoftKey) {
		if (blogSoftKey != null) {
			configuration.put(BLOG_SOFTKEY, blogSoftKey);
		} else {
			configuration.remove(BLOG_SOFTKEY);
		}
	}

	public boolean isForumEnabled() {
		Boolean bool = (Boolean) configuration.get(FORUM_ENABLED);
		return bool.booleanValue();
	}

	public void setForumEnabled(boolean b) {
		configuration.put(FORUM_ENABLED, Boolean.valueOf(b));
	}
	public boolean isWikiEnabled() {
		Boolean bool = (Boolean) configuration.get(WIKI_ENABLED);
		return bool.booleanValue();
	}

	public void setWikiEnabled(boolean b) {
		configuration.put(WIKI_ENABLED, Boolean.valueOf(b));
	}

	public String getWikiSoftKey() {
		Object softKey = configuration.get(WIKI_SOFTKEY);
		return softKey != null? (String) softKey: null;
	}

	public void setWikiSoftKey(String wikiSoftKey) {
		if (wikiSoftKey != null) {
			configuration.put(WIKI_SOFTKEY, wikiSoftKey);
		} else {
			configuration.remove(WIKI_SOFTKEY);
		}
	}

	public boolean isDocumentsEnabled() {
		Boolean bool = (Boolean) configuration.get(DOCUMENTS_ENABLED);
		return bool.booleanValue();
	}

	public void setDocumentsEnabled(boolean b) {
		configuration.put(DOCUMENTS_ENABLED, Boolean.valueOf(b));
	}

	public String getDocumentsPath() {
		Object path = configuration.get(DOCUMENTS_PATH);
		return path != null ? (String) path : null;
	}

	public void setDocumentPath(String documentPath) {
		if (documentPath != null) {
			configuration.put(DOCUMENTS_PATH, documentPath);
		} else {
			configuration.remove(DOCUMENTS_PATH);
		}
	}

	public boolean isToolbarEnabled() {
		Boolean bool = (Boolean) configuration.get(TOOLBAR_ENABLED);
		return bool.booleanValue();
	}

	public void setToolbarEnabled(boolean b) {
		configuration.put(TOOLBAR_ENABLED, Boolean.valueOf(b));
	}

	public boolean isBreadCrumbEnabled() {
		Boolean bool = (Boolean) configuration.get(BREADCRUMB_ENABLED);
		return bool == null || bool.booleanValue();
	}

	public void setBreadCrumbEnabled(boolean b) {
		configuration.put(BREADCRUMB_ENABLED, Boolean.valueOf(b));
	}
	
	public void setDisclaimerEnabled(int disclaimer, boolean enabled) {
		if (disclaimer == 1) {
			configuration.put(DISCLAIMER_1_ENABLED, enabled);
		} else if (disclaimer == 2) {
			configuration.put(DISCLAIMER_2_ENABLED, enabled);
		}
	}
	
	public void setDisclaimerTitle(int disclaimer, String title) {
		if (disclaimer == 1) {
			configuration.put(DISCLAIMER_1_TITLE, title);
		} else if (disclaimer == 2) {
			configuration.put(DISCLAIMER_2_TITLE, title);
		}
	}
	
	public void setDisclaimerTerms(int disclaimer, String terms) {
		if (disclaimer == 1) {
			configuration.put(DISCLAIMER_1_TERMS, terms);
		} else if (disclaimer == 2) {
			configuration.put(DISCLAIMER_2_TERMS, terms);
		}
	}
	
	public void setDisclaimerLabel(int disclaimer, int label, String labelText) {
		if (disclaimer == 1) {
			if (label == 1) {
				configuration.put(DISCLAIMER_1_LABEL_1, labelText);
			} else if (label == 2) {
				configuration.put(DISCLAIMER_1_LABEL_2, labelText);
			}
			
		} else if (disclaimer == 2) {
			if (label == 1) {
				configuration.put(DISCLAIMER_2_LABEL_1, labelText);
			} else if (label == 2) {
				configuration.put(DISCLAIMER_2_LABEL_2, labelText);
			}
		}
	}
	
	public boolean isDisclaimerEnabled(int disclaimer) {
		if (disclaimer == 1) {
			return (boolean) configuration.getOrDefault(DISCLAIMER_1_ENABLED, false);
		} else if (disclaimer == 2) {
			return (boolean) configuration.getOrDefault(DISCLAIMER_2_ENABLED, false);
		}
		
		return false;
	}
	
	public boolean isDisclaimerEnabled() {
		return isDisclaimerEnabled(1) || isDisclaimerEnabled(2);
	}
	
	public String getDisclaimerTitel(int disclaimer) {
		if (disclaimer == 1) {
			return (String) configuration.getOrDefault(DISCLAIMER_1_TITLE, "");
		} else if (disclaimer == 2) {
			return (String) configuration.getOrDefault(DISCLAIMER_2_TITLE, "");
		}
		
		return null;
	}
	
	public String getDisclaimerTerms(int disclaimer) {
		if (disclaimer == 1) {
			return (String) configuration.getOrDefault(DISCLAIMER_1_TERMS, "");
		} else if (disclaimer == 2) {
			return (String) configuration.getOrDefault(DISCLAIMER_2_TERMS, "");
		}
		
		return null;
	}
	
	public String getDisclaimerLabel(int disclaimer, int label) {
		if (disclaimer == 1) {
			if (label == 1) {
				return (String) configuration.getOrDefault(DISCLAIMER_1_LABEL_1, "");
			} else if (label == 2) {
				return (String) configuration.getOrDefault(DISCLAIMER_1_LABEL_2, "");
			}
		} else if (disclaimer == 2) {
			if (label == 1) {
				return (String) configuration.getOrDefault(DISCLAIMER_2_LABEL_1, "");
			} else if (label == 2) {
				return (String) configuration.getOrDefault(DISCLAIMER_2_LABEL_2, "");
			}
		}
		
		return null;
	}
	
	public ImageSource getTeaserImageSource() {
		Object imageSource = configuration.get(NODE_TEASER_IMAGE_SOURCE);
		return imageSource != null ? (ImageSource) imageSource : null;
	}
	
	public void setTeaserImageSource(ImageSource imageSource) {
		if (imageSource != null) {
			configuration.put(NODE_TEASER_IMAGE_SOURCE, imageSource);
		} else {
			configuration.remove(NODE_TEASER_IMAGE_SOURCE);
		}
	}
	
	public TeaserImageStyle getTeaserImageStyle() {
		Object teaserImageStyle = configuration.get(NODE_TEASER_IMAGE_STYLE);
		return teaserImageStyle != null 
				? (TeaserImageStyle) teaserImageStyle
				: TeaserImageStyle.DEFAULT_COURSE;
	}
	
	public void setTeaserImageStyle(TeaserImageStyle teaserImageStyle) {
		if (teaserImageStyle != null) {
			configuration.put(NODE_TEASER_IMAGE_STYLE, teaserImageStyle);
		} else {
			configuration.put(NODE_TEASER_IMAGE_STYLE, TeaserImageStyle.DEFAULT_COURSE);
		}
	}
	
	public void setColorCategoryIdentifier(String colorCategoryIdentifier) {
		String identifier = colorCategoryIdentifier != null
				? colorCategoryIdentifier
				: ColorCategory.IDENTIFIER_DEFAULT_COURSE;
		configuration.put(NODE_COLOR_CATEGORY_IDENITFIER, identifier);
	}

	public String getColorCategoryIdentifier() {
		Object colorCategoryIdentifier = configuration.get(NODE_COLOR_CATEGORY_IDENITFIER);
		return colorCategoryIdentifier != null
				? (String) colorCategoryIdentifier
				: ColorCategory.IDENTIFIER_DEFAULT_COURSE;
	}

	@Override
	public CourseConfig clone() {
		CourseConfig clone = new CourseConfig();
		clone.setCalendarEnabled(((Boolean) configuration.get(KEY_CALENDAR_ENABLED)).booleanValue());
		clone.setChatIsEnabled(((Boolean) configuration.get(KEY_CHAT_ENABLED)).booleanValue());
		clone.setCssLayoutRef((String) configuration.get(KEY_CSS_FILEREF));
		clone.setEfficencyStatementIsEnabled(isEfficencyStatementEnabled());
		clone.setGlossarySoftKey(getGlossarySoftKey());
		clone.setGlossaryIsEnabled(isGlossaryEnabled());
		clone.setSharedFolderSoftkey(getSharedFolderSoftkey());
		clone.setSharedFolderReadOnlyMount(isSharedFolderReadOnlyMount());
		clone.setAutomaticCertificationEnabled(isAutomaticCertificationEnabled());
		clone.setManualCertificationEnabled(isManualCertificationEnabled());
		clone.setCertificateTemplate(getCertificateTemplate());
		clone.setCertificateCustom1(getCertificateCustom1());
		clone.setCertificateCustom2(getCertificateCustom2());
		clone.setCertificateCustom3(getCertificateCustom3());
		clone.setRecertificationEnabled(isRecertificationEnabled());
		clone.setRecertificationTimelapse(getRecertificationTimelapse());
		clone.setRecertificationTimelapseUnit(getRecertificationTimelapseUnit());
		clone.setMenuEnabled(isMenuEnabled());
		clone.setToolbarEnabled(isToolbarEnabled());
		clone.setBreadCrumbEnabled(isBreadCrumbEnabled());
		clone.setCourseSearchEnabled(isCourseSearchEnabled());
		clone.setParticipantListEnabled(isParticipantListEnabled());
		clone.setParticipantInfoEnabled(isParticipantInfoEnabled());
		clone.setEmailEnabled(isEmailEnabled());
		clone.setTeamsEnabled(isTeamsEnabled());
		clone.setBigBlueButtonEnabled(isBigBlueButtonEnabled());
		clone.setBigBlueButtonModeratorStartsMeeting(isBigBlueButtonModeratorStartsMeeting());
		clone.setBlogEnabled(isBlogEnabled());
		clone.setBlogSoftKey(getBlogSoftKey());
		clone.setForumEnabled(isForumEnabled());
		clone.setWikiEnabled(isWikiEnabled());
		clone.setWikiSoftKey(getWikiSoftKey());
		clone.setDocumentsEnabled(isDocumentsEnabled());
		clone.setDocumentPath(getDocumentsPath());
		clone.setCompletionType(getCompletionType());
		clone.setNodeAccessType(getNodeAccessType().getType());
		clone.setDisclaimerEnabled(1, isDisclaimerEnabled(1));
		clone.setDisclaimerEnabled(2, isDisclaimerEnabled(2));
		clone.setDisclaimerTitle(1, getDisclaimerTitel(1));
		clone.setDisclaimerTitle(2, getDisclaimerTitel(2));
		clone.setDisclaimerTerms(1, getDisclaimerTerms(1));
		clone.setDisclaimerTerms(2, getDisclaimerTerms(2));
		clone.setDisclaimerLabel(1, 1, getDisclaimerLabel(1, 1));
		clone.setDisclaimerLabel(1, 2, getDisclaimerLabel(1, 2));
		clone.setDisclaimerLabel(2, 1, getDisclaimerLabel(2, 1));
		clone.setDisclaimerLabel(2, 2, getDisclaimerLabel(2, 2));
		ImageSource teaserImageSource = getTeaserImageSource();
		if (teaserImageSource != null) {
			ImageSource clonedTeaserImageSource = (ImageSource)XStreamHelper.xstreamClone(teaserImageSource);
			clone.setTeaserImageSource(clonedTeaserImageSource);
		}
		clone.setTeaserImageStyle(getTeaserImageStyle());
		clone.setColorCategoryIdentifier(getColorCategoryIdentifier());
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof CourseConfig) {
			try {
				CourseConfig aCourseConfig = (CourseConfig) obj;
				boolean sameCalendarSettings = aCourseConfig.isCalendarEnabled() == isCalendarEnabled();
				boolean sameChatSettings = aCourseConfig.isChatEnabled() == isChatEnabled();
				boolean sameCssLayout = aCourseConfig.getCssLayoutRef().equals(getCssLayoutRef());
				boolean sameEfficiencyStatementSettings = aCourseConfig
						.isEfficencyStatementEnabled() == isEfficencyStatementEnabled();
				boolean sameSharedFolderSettings = aCourseConfig.getSharedFolderSoftkey()
						.equals(getSharedFolderSoftkey())
						&& aCourseConfig.isSharedFolderReadOnlyMount() == isSharedFolderReadOnlyMount();

				boolean sameGlossarySettings = false;
				if (aCourseConfig.getGlossarySoftKey() != null && this.getGlossarySoftKey() != null) {
					sameGlossarySettings = aCourseConfig.getGlossarySoftKey().equals(this.getGlossarySoftKey());
				} else if (aCourseConfig.getGlossarySoftKey() == null && this.getGlossarySoftKey() == null) {
					sameGlossarySettings = true;
				}
				
				boolean sameDisclaimerSettings = false;
				if (isDisclaimerEnabled(1) == aCourseConfig.isDisclaimerEnabled(1) &&
					isDisclaimerEnabled(2) == aCourseConfig.isDisclaimerEnabled(2) &&
					getDisclaimerTitel(1).equals(aCourseConfig.getDisclaimerTitel(1)) &&
					getDisclaimerTitel(2).equals(aCourseConfig.getDisclaimerTitel(2)) &&
					getDisclaimerTerms(1).equals(aCourseConfig.getDisclaimerTerms(1)) &&
					getDisclaimerTerms(2).equals(aCourseConfig.getDisclaimerTerms(2)) &&
					getDisclaimerLabel(1, 1).equals(aCourseConfig.getDisclaimerLabel(1, 1)) &&
					getDisclaimerLabel(1, 2).equals(aCourseConfig.getDisclaimerLabel(1, 2)) &&
					getDisclaimerLabel(2, 1).equals(aCourseConfig.getDisclaimerLabel(2, 1)) &&
					getDisclaimerLabel(2, 2).equals(aCourseConfig.getDisclaimerLabel(2, 2))) {
					sameDisclaimerSettings = true;
				}

				return sameCalendarSettings && sameChatSettings && sameCssLayout && sameEfficiencyStatementSettings
						&& sameGlossarySettings && sameSharedFolderSettings && sameDisclaimerSettings;

			} catch (RuntimeException e) {
				// nothing to do
			}
		}
		return false;
	}

}
