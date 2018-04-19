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
import org.olat.course.certificate.RecertificationTimeUnit;

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
	transient public static final String VALUE_EMPTY_CSS_FILEREF = "form.layout.setsystemcss";
	transient private static final String OLD_VALUE_EMPTY_CSS_FILEREF = "::EmPtY::";
	/**
	 * <code>VALUE_EMPTY_SHAREDFOLDER_SOFTKEY</code> is the <i>softkey</i>
	 */
	transient public static final String VALUE_EMPTY_SHAREDFOLDER_SOFTKEY = "sf.notconfigured";
	/**
	 * current config file version
	 */
	transient private final static int CURRENTVERSION = 13;
	/**
	 * Log levels
	 */
	transient public final static String KEY_LOGLEVEL_ADMIN = "LOGLEVELADMIN";
	/**
	 * Log levels
	 */
	transient public final static String KEY_LOGLEVEL_USER = "LOGLEVELUSER";
	/**
	 * Log levels
	 */
	transient public final static String KEY_LOGLEVEL_STATISTIC = "LOGLEVELSTAT";
	/**
	 * Chat enabled boolean
	 */
	transient public final static String KEY_CHAT_ENABLED = "COURSE_CHAT_ENABLED";
	/**
	 * efficency statement
	 */
	transient public static final String KEY_EFFICENCY_ENABLED = "KEY_EFFICENCY_ENABLED";
	transient public static final String CERTIFICATE_AUTO_ENABLED = "CERTIFICATE_AUTO";
	transient public static final String CERTIFICATE_MANUAL_ENABLED = "CERTIFICATE_MANUAL";
	transient public static final String CERTIFICATE_TEMPLATE = "CERTIFICATE_TEMPLATE";
	transient public static final String RECERTIFICATION_ENABLED = "RECERTIFICATION_ENABLED";
	transient public static final String RECERTIFICATION_TIMELAPSE = "RECERTIFICATION_TIMELAPSE";
	transient public static final String RECERTIFICATION_TIMELAPSE_UNIT = "RECERTIFICATION_TIMELAPSE_UNIT";
	
	/**
	 * The menu is enabled by default
	 */
	transient public static final String MENU_ENABLED = "MENU_ENABLED";
	/**
	 * The toolbar is enabled by default
	 */
	transient public static final String TOOLBAR_ENABLED = "TOOLBAR_ENABLED";
	/**
	 * The bread crumb is enabled by default
	 */
	transient public static final String BREADCRUMB_ENABLED = "BREADCRUMB_ENABLED";
	/**
	 * The course search is enabled by default
	 */
	transient public static final String COURSESEARCH_ENABLED = "COURSESEARCH_ENABLED";
	/**
	 * course calendar
	 */
	transient public static final String KEY_CALENDAR_ENABLED = "KEY_CALENDAR_ENABLED";
	/**
	 * course glossary
	 */
	transient public static final String KEY_GLOSSARY_SOFTKEY = "KEY_GLOSSARY_SOFTKEY";
	/**
	 * 
	 */
	transient public static final String KEY_CSS_FILEREF = "CSS_FILEREF";
	/**
	 * 
	 */
	transient public static final String KEY_SHAREDFOLDER_SOFTKEY = "SHAREDFOLDER_SOFTKEY";
	/**
	 * 
	 */
	transient public static final String KEY_SHAREDFOLDER_READONLY = "SHAREDFOLDER_RO";
	/**
	 * current key set
	 */
	transient public final static String[] KEYS = { KEY_CHAT_ENABLED,	KEY_CSS_FILEREF, KEY_SHAREDFOLDER_SOFTKEY };
	/**
	 * config file version from file
	 */
	private int version = -1;
	/**
	 * holds the configuration
	 */
	private Map<String,Object> configuration = new Hashtable<String,Object>();

	CourseConfig() {
	// empty, for XSTream
	}

	/**
	 * @return version of this loaded/created instance
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * initialize with default values
	 */
	public void initDefaults() {
		// version 1
		//configuration.put(KEY_LOGLEVEL_ADMIN, new Integer(LOGLEVEL_INFO));
		//configuration.put(KEY_LOGLEVEL_STATISTIC, new Integer(LOGLEVEL_INFO));
		//configuration.put(KEY_LOGLEVEL_USER, new Integer(LOGLEVEL_FINE));
		// version 2
		configuration.put(KEY_CHAT_ENABLED, Boolean.TRUE);
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
			// from version 1 -> 2
			if (version == 1) {
				//if (!configuration.containsKey(KEY_LOGLEVEL_ADMIN)) configuration.put(KEY_LOGLEVEL_ADMIN, new Integer(LOGLEVEL_INFO));
				//if (!configuration.containsKey(KEY_LOGLEVEL_STATISTIC)) configuration.put(KEY_LOGLEVEL_STATISTIC, new Integer(LOGLEVEL_INFO));
				//if (!configuration.containsKey(KEY_LOGLEVEL_USER)) configuration.put(KEY_LOGLEVEL_USER, new Integer(LOGLEVEL_FINE));
				this.version = 2;
			}
			// from version 2 -> 3
			if (version == 2) {
				// add the new key
				if (!configuration.containsKey(KEY_CHAT_ENABLED)) configuration.put(KEY_CHAT_ENABLED, Boolean.TRUE);
				this.version = 3;
			}
			// from version 3 -> 4
			if (version == 3) {
				// add the new key
				if (!configuration.containsKey(KEY_CSS_FILEREF)) configuration.put(KEY_CSS_FILEREF, VALUE_EMPTY_CSS_FILEREF);
				this.version = 4;
			}
			// from version 4 -> 5
			if (version == 4) {
				// add the new key
				if (!configuration.containsKey(KEY_SHAREDFOLDER_SOFTKEY)) configuration.put(KEY_SHAREDFOLDER_SOFTKEY,
						VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
				this.version = 5;
			}
			// from version 5 -> 6
			if (version == 5) {
				// add the new key
				if (!configuration.containsKey(KEY_EFFICENCY_ENABLED)) configuration.put(KEY_EFFICENCY_ENABLED, Boolean.FALSE);
				this.version = 6;
			}
			// from version 6 -> 7
			if (version == 6) {
				// add the new key
				if (!configuration.containsKey(KEY_CALENDAR_ENABLED)) configuration.put(KEY_CALENDAR_ENABLED, Boolean.TRUE);
				this.version = 7;
			}
			// from version 7 -> 8
			if (version == 7) {
				// glossary configuration has been added. no default values needed
				this.version = 8;
			}
			// from version 8 -> 9
			if (version == 8) {
				if (configuration.containsKey(KEY_LOGLEVEL_ADMIN)) configuration.remove(KEY_LOGLEVEL_ADMIN);
				if (configuration.containsKey(KEY_LOGLEVEL_USER)) configuration.remove(KEY_LOGLEVEL_USER);
				if (configuration.containsKey(KEY_LOGLEVEL_STATISTIC)) configuration.remove(KEY_LOGLEVEL_STATISTIC);
				this.version = 9;
			}
			
			if (version == 9) {
				if (!configuration.containsKey(CERTIFICATE_AUTO_ENABLED)) configuration.put(CERTIFICATE_AUTO_ENABLED, Boolean.FALSE);
				if (!configuration.containsKey(CERTIFICATE_MANUAL_ENABLED)) configuration.put(CERTIFICATE_MANUAL_ENABLED, Boolean.FALSE);
				if (!configuration.containsKey(CERTIFICATE_TEMPLATE)) configuration.put(CERTIFICATE_TEMPLATE, "");
				if (!configuration.containsKey(RECERTIFICATION_ENABLED)) configuration.put(RECERTIFICATION_ENABLED, Boolean.FALSE);
				if (!configuration.containsKey(RECERTIFICATION_TIMELAPSE)) configuration.put(RECERTIFICATION_TIMELAPSE, new Integer(0));
				this.version = 10;
			}
			
			if (version == 10) {
				if (!configuration.containsKey(MENU_ENABLED)) configuration.put(MENU_ENABLED, Boolean.TRUE);
				if (!configuration.containsKey(TOOLBAR_ENABLED)) configuration.put(TOOLBAR_ENABLED, Boolean.TRUE);
				this.version = 11;
			}
			
			if (version == 11) {
				if (!configuration.containsKey(COURSESEARCH_ENABLED)) configuration.put(COURSESEARCH_ENABLED, Boolean.FALSE);
				this.version = 12;
			}

			if (version == 12) {
				if (!configuration.containsKey(BREADCRUMB_ENABLED)) configuration.put(BREADCRUMB_ENABLED, Boolean.TRUE);
				this.version = 13;
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
		 * ::EmPtY:: now it is form.layout.setsystemcss To have old configuration
		 * files beeing compatible they have to be converted.
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

	/**
	 * @return if chat is enabled
	 */
	public boolean isChatEnabled() {
		Boolean bool = (Boolean) configuration.get(KEY_CHAT_ENABLED);
		return bool.booleanValue();
	}

	/**
	 * @param b
	 */
	public void setChatIsEnabled(boolean b) {
		configuration.put(KEY_CHAT_ENABLED, new Boolean(b));
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

	/**
	 * @return boolean
	 */
	public boolean hasGlossary() {
		return (getGlossarySoftKey() != null);
	}

	/**
	 * @return boolean
	 */
	public boolean hasCustomCourseCSS() {
		return !(VALUE_EMPTY_CSS_FILEREF.equals(getCssLayoutRef()));
	}

	/**
	 * @param softkey
	 */
	public void setSharedFolderSoftkey(String softkey) {
		if(softkey == null) {
			configuration.put(KEY_SHAREDFOLDER_SOFTKEY, VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
		} else {
			configuration.put(KEY_SHAREDFOLDER_SOFTKEY, softkey);
		}
	}

	/**
	 * @return softkey
	 */
	public String getSharedFolderSoftkey() {
		return (String) configuration.get(KEY_SHAREDFOLDER_SOFTKEY);
	}

	/**
	 * @return boolean
	 */
	public boolean hasCustomSharedFolder() {
		return !(VALUE_EMPTY_SHAREDFOLDER_SOFTKEY.equals(getSharedFolderSoftkey()));
	}
	
	public boolean isSharedFolderReadOnlyMount() {
		Object obj = configuration.get(KEY_SHAREDFOLDER_READONLY);
		return (obj == null || !Boolean.FALSE.equals(obj));
	}
	
	public void setSharedFolderReadOnlyMount(boolean mount) {
		configuration.put(KEY_SHAREDFOLDER_READONLY, new Boolean(mount));
	}

	/**
	 * @param b
	 */
	public void setEfficencyStatementIsEnabled(boolean b) {
		configuration.put(KEY_EFFICENCY_ENABLED, new Boolean(b));
	}

	/**
	 * @return true if the efficency statement is enabled
	 */
	public boolean isEfficencyStatementEnabled() {
		Boolean bool = (Boolean) configuration.get(KEY_EFFICENCY_ENABLED);
		return bool.booleanValue();
	}
	
	
	/**
	 * @return true if the efficency statement is enabled
	 */
	public Long getCertificateTemplate() {
		Object templateIdObj = configuration.get(CERTIFICATE_TEMPLATE);
		Long templateId = null;
		if(templateIdObj instanceof Long) {
			templateId = (Long)templateIdObj;
		}
		return templateId;
	}
	
	/**
	 * @param b
	 */
	public void setCertificateTemplate(Long templateId ) {
		if(templateId != null) {
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
		return bool == null ? false : bool.booleanValue();
	}
	
	public void setAutomaticCertificationEnabled(boolean enabled) {
		configuration.put(CERTIFICATE_AUTO_ENABLED, new Boolean(enabled));
	}
	
	public boolean isManualCertificationEnabled() {
		Boolean bool = (Boolean) configuration.get(CERTIFICATE_MANUAL_ENABLED);
		return bool == null ? false : bool.booleanValue();
	}
	
	public void setManualCertificationEnabled(boolean enabled) {
		configuration.put(CERTIFICATE_MANUAL_ENABLED, new Boolean(enabled));
	}

	/**
	 * @return true if the efficency statement is enabled
	 */
	public boolean isRecertificationEnabled() {
		Boolean bool = (Boolean) configuration.get(RECERTIFICATION_ENABLED);
		return bool == null ? false : bool.booleanValue();
	}
	
	/**
	 * @param b
	 */
	public void setRecertificationEnabled(boolean b) {
		configuration.put(RECERTIFICATION_ENABLED, new Boolean(b));
	}
	
	public int getRecertificationTimelapse() {
		Object timelapse = configuration.get(RECERTIFICATION_TIMELAPSE);
		if(timelapse instanceof Integer) {
			return ((Integer)timelapse).intValue();
		}
		return 0;
	}
	
	public void setRecertificationTimelapse(int timelapse) {
		configuration.put(RECERTIFICATION_TIMELAPSE, new Integer(timelapse));
	}
	
	public RecertificationTimeUnit getRecertificationTimelapseUnit() {
		String timelapseUnit = (String)configuration.get(RECERTIFICATION_TIMELAPSE_UNIT);
		RecertificationTimeUnit timeUnit = null;
		if(StringHelper.containsNonWhitespace(timelapseUnit)) {
			timeUnit = RecertificationTimeUnit.valueOf(timelapseUnit);
		}
		return timeUnit;
	}
	
	public void setRecertificationTimelapseUnit(RecertificationTimeUnit timeUnit) {
		if(timeUnit == null) {
			configuration.remove(RECERTIFICATION_TIMELAPSE_UNIT);
		} else {
			configuration.put(RECERTIFICATION_TIMELAPSE_UNIT, timeUnit.name());
		}
	}

	/**
	 * @return true if calendar is enabled
	 */
	public boolean isCalendarEnabled() {
		Boolean bool = (Boolean) configuration.get(KEY_CALENDAR_ENABLED);
		return bool.booleanValue();
	}

	/**
	 * @param b
	 */
	public void setCalendarEnabled(boolean b) {
		configuration.put(KEY_CALENDAR_ENABLED, new Boolean(b));
	}
	
	public boolean isMenuEnabled() {
		Boolean bool = (Boolean) configuration.get(MENU_ENABLED);
		return bool.booleanValue();
	}
	
	public void setMenuEnabled(boolean b) {
		configuration.put(MENU_ENABLED, new Boolean(b));
	}
	
	public boolean isCourseSearchEnabled() {
		Boolean bool = (Boolean) configuration.get(COURSESEARCH_ENABLED);
		return bool.booleanValue();
	}
	
	public void setCourseSearchEnabled(boolean b) {
		configuration.put(COURSESEARCH_ENABLED, new Boolean(b));
	}
	
	public boolean isToolbarEnabled() {
		Boolean bool = (Boolean) configuration.get(TOOLBAR_ENABLED);
		return bool.booleanValue();
	}
	
	public void setToolbarEnabled(boolean b) {
		configuration.put(TOOLBAR_ENABLED, new Boolean(b));
	}

	public boolean isBreadCrumbEnabled() {
		Boolean bool = (Boolean) configuration.get(BREADCRUMB_ENABLED);
		return bool == null ? true : bool.booleanValue();
	}
	
	public void setBreadCrumbEnabled(boolean b) {
		configuration.put(BREADCRUMB_ENABLED, new Boolean(b));
	}

	/**
	 * Creates a deep clone for the current object.
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public CourseConfig clone() {
		CourseConfig clone = new CourseConfig();
		clone.setCalendarEnabled(((Boolean) configuration.get(KEY_CALENDAR_ENABLED)).booleanValue());
		clone.setChatIsEnabled(((Boolean) configuration.get(KEY_CHAT_ENABLED)).booleanValue());
		clone.setCssLayoutRef((String) configuration.get(KEY_CSS_FILEREF));
		clone.setEfficencyStatementIsEnabled(isEfficencyStatementEnabled());
		clone.setGlossarySoftKey(getGlossarySoftKey());
		clone.setSharedFolderSoftkey(getSharedFolderSoftkey());
		clone.setSharedFolderReadOnlyMount(isSharedFolderReadOnlyMount());
		clone.setAutomaticCertificationEnabled(isAutomaticCertificationEnabled());
		clone.setManualCertificationEnabled(isManualCertificationEnabled());
		clone.setCertificateTemplate(getCertificateTemplate());
		clone.setRecertificationEnabled(isRecertificationEnabled());
		clone.setRecertificationTimelapse(getRecertificationTimelapse());
		clone.setRecertificationTimelapseUnit(getRecertificationTimelapseUnit());
		clone.setMenuEnabled(isMenuEnabled());
		clone.setToolbarEnabled(isToolbarEnabled());
		clone.setBreadCrumbEnabled(isBreadCrumbEnabled());
		clone.setCourseSearchEnabled(isCourseSearchEnabled());
		return clone;
	}

	/**
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		try {
			CourseConfig aCourseConfig = (CourseConfig) obj;
			boolean sameCalendarSettings = aCourseConfig.isCalendarEnabled() == isCalendarEnabled();
			boolean sameChatSettings = aCourseConfig.isChatEnabled() == isChatEnabled();
			boolean sameCssLayout = aCourseConfig.getCssLayoutRef().equals(getCssLayoutRef());
			boolean sameEfficiencyStatementSettings = aCourseConfig.isEfficencyStatementEnabled() == isEfficencyStatementEnabled();
			boolean sameSharedFolderSettings = aCourseConfig.getSharedFolderSoftkey().equals(getSharedFolderSoftkey())
					&& aCourseConfig.isSharedFolderReadOnlyMount() == isSharedFolderReadOnlyMount();

			boolean sameGlossarySettings = false;
			if (aCourseConfig.getGlossarySoftKey() != null && this.getGlossarySoftKey() != null) {
				sameGlossarySettings = aCourseConfig.getGlossarySoftKey().equals(this.getGlossarySoftKey());
			} else if (aCourseConfig.getGlossarySoftKey() == null && this.getGlossarySoftKey() == null) {
				sameGlossarySettings = true;
			}

			return sameCalendarSettings && sameChatSettings && sameCssLayout && sameEfficiencyStatementSettings && sameGlossarySettings
					&& sameSharedFolderSettings;

		} catch (RuntimeException e) {
			// nothing to do
		}
		return false;
	}

	/**
	 * @param version The version to set.
	 */
	public void setVersion(int version) {
		this.version = version;
	}

}
