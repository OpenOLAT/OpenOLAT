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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.course.config.ui.courselayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.helpers.Settings;
import org.olat.core.util.IImageHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.course.config.CourseConfig;
import org.olat.course.run.environment.CourseEnvironment;

/**
 * Description:<br>
 * some static helpers for the course-layout-generator
 * 
 * <P>
 * Initial Date: 01.02.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class CourseLayoutHelper {

	public static final String COURSEFOLDER_CSS_BASE = "/courseCSS";
	private static int logoMaxHeight;
	private static int logoMaxWidth;
	private static IImageHelper imageHelperToUse;
	
	public static final String CONFIG_KEY_LEGACY = "legacy";
	public static final String CONFIG_KEY_DEFAULT = CourseConfig.VALUE_EMPTY_CSS_FILEREF;
	public static final String CONFIG_KEY_TEMPLATE = "template::";
	public static final String CONFIG_KEY_PREDEFINED = "predefined";
	public static final String CONFIG_KEY_CUSTOM = "custom";

	public static final String LAYOUT_COURSE_SUBFOLDER = "/layout";
	private static final String IFRAME_CSS = "/iframe.css";
	private static final String MAIN_CSS = "/main.css";

	/**
	 * Array holding the folder-names of the blacklisted course-layouts
	 * (set by spring)
	 * 
	 */
	private static List<String> layoutBlacklist;
	
	CourseLayoutHelper(){
		// 
	}
	
/**
 * get configured path for this courseEnvironment
 * @param courseEnvironment
 * @return
 */
	public static VFSContainer getCSSBaseFolder(CourseEnvironment courseEnvironment) {
		CourseConfig courseConfig = courseEnvironment.getCourseConfig();
		String cssSet = courseConfig.getCssLayoutRef();
		return getThemeBaseFolder(courseEnvironment, cssSet); 
	}

 /**
  * get path according to type of theme
  * @param courseEnvironment
  * @param cssSet
  * @return
  */
	public static VFSContainer getThemeBaseFolder(CourseEnvironment courseEnvironment, String cssSet) {
		VFSContainer courseBase = courseEnvironment.getCourseBaseContainer();
		
		// the hidden-dir CSS in "/coursecss/xy.css"
		if (StringHelper.containsNonWhitespace(cssSet) && cssSet.contains(COURSEFOLDER_CSS_BASE) && cssSet.startsWith("/") && cssSet.lastIndexOf("/") > 0){
			if (courseEnvironment.getCourseFolderContainer().resolve(COURSEFOLDER_CSS_BASE) != null) {
				return courseEnvironment.getCourseFolderContainer();
			} else return null;
		}	else if (!StringHelper.containsNonWhitespace(cssSet) || cssSet.startsWith("/")) {
			// the old legacy format "/blibla.css"
			return (VFSContainer) courseBase.resolve("coursefolder");
		} else if (cssSet.equals(CONFIG_KEY_CUSTOM)) {
			return (VFSContainer) courseBase.resolve(LAYOUT_COURSE_SUBFOLDER + "/custom");
		} else if (CONFIG_KEY_PREDEFINED.equals(cssSet)) {
			return (VFSContainer) courseBase.resolve(LAYOUT_COURSE_SUBFOLDER + "/predefined");
		} else if (cssSet.startsWith(CONFIG_KEY_TEMPLATE)) {
			String selTheme = cssSet.substring(CONFIG_KEY_TEMPLATE.length());

			// 1. check if it's a layout from the OLAT-theme
			VFSContainer themeContainer = getOLATThemeCourseLayoutFolder();
			if(themeContainer!=null){
				themeContainer = (VFSContainer) themeContainer.resolve("/"+selTheme);
				return themeContainer;
			}
				
			// 2. check if it's system-default 
			String staticAbsPath = WebappHelper.getContextRoot() + "/static/coursethemes/";
			File themesFile = new File(staticAbsPath + selTheme);
			if(themesFile.exists()  && themesFile.isDirectory())
				return new LocalFolderImpl(themesFile);
		} 
		return null; // default was set
	}

	/**
	 * get CustomCSS preconfigured according to choosen course theme
	 * @param usess
	 * @param courseEnvironment
	 * @return
	 */
	public static CustomCSS getCustomCSS(UserSession usess, CourseEnvironment courseEnvironment) {
		VFSContainer courseContainer = getCSSBaseFolder(courseEnvironment);
		CustomCSS customCSS = null;
		// check for existing main.css and iframe.css
		if (isCourseThemeFolderValid(courseContainer)) {
			customCSS = new CustomCSS(courseContainer, MAIN_CSS, IFRAME_CSS, usess);
		} else if (courseContainer!=null && courseContainer.resolve(courseEnvironment.getCourseConfig().getCssLayoutRef()) != null){ // legacy fallback
			customCSS = new CustomCSS(courseContainer, courseEnvironment.getCourseConfig().getCssLayoutRef(), usess);
		}
		return customCSS;
	}
	
	/**
	 * get a list of system wide course theme templates
	 * they need to be in webapp/static/coursethemes/XY
	 * @return
	 */
	public static List<VFSItem> getCourseThemeTemplates(){  
		List<VFSItem> courseThemes = new ArrayList<VFSItem>();

		// 1. add the system-defaults
		String staticAbsPath = WebappHelper.getContextRoot() + "/static";
		File themesFile = new File(staticAbsPath);
		VFSContainer cThemeCont = new LocalFolderImpl(themesFile);
		cThemeCont = (VFSContainer) cThemeCont.resolve("/coursethemes");
		if (cThemeCont != null) {
			courseThemes = cThemeCont.getItems(new VFSItemFilter() {
				@Override
				public boolean accept(VFSItem it) {
					if (!(it instanceof VFSContainer)) return false;
					if (it.getName().equals("CVS") || it.getName().equals("cvs")) return false;
					return !(layoutBlacklist.contains(it.getName()));
				}
			});
		}

		// 2. now add the additional Templates from the current Theme
		VFSContainer addThCont = CourseLayoutHelper.getOLATThemeCourseLayoutFolder();
		if (addThCont != null) {
			List<VFSItem> additionalThemes = addThCont.getItems(new VFSItemFilter() {
				@Override
				public boolean accept(VFSItem it) {
					if (!(it instanceof VFSContainer)) return false;
					return (!it.getName().equals("CVS") && !it.getName().equals("cvs"));
				}
			});
			courseThemes.addAll(additionalThemes);
		}
		return courseThemes;
	}
	
	/**
	 * returns the Folder with the additional courselayouts from the current
	 * OLAT-Theme this is e.g. : /static/themes/frentix/courselayouts/<br />
	 * If no courselayouts exist in the current OLAT-Theme, null is returned!
	 * 
	 * @return the courselayouts-folder or null
	 */
	public static VFSContainer getOLATThemeCourseLayoutFolder() {
		String staticThemesPath = WebappHelper.getContextRoot() + "/static/themes/";
		File tmpDir = new File(staticThemesPath);
		VFSContainer addThCont = new LocalFolderImpl(tmpDir);
		return (VFSContainer) addThCont.resolve("/" + Settings.getGuiThemeIdentifyer() + "/courselayouts");
	}
	
	/**
	 * checks if the given theme base contains the needed css-files
	 * - main.css
	 * - iframe.css
	 * @param courseThemeBase
	 * @return
	 */
	public static boolean isCourseThemeFolderValid(VFSContainer courseThemeBase){
		if (courseThemeBase==null) return false;
		return courseThemeBase.resolve(MAIN_CSS) != null && courseThemeBase.resolve(IFRAME_CSS) != null;
	}

	/**
	 * [spring]
	 * @param logoMaxHeight The logoMaxHeight to set.
	 */
	public void setLogoMaxHeight(int logoMaxHeight) {
		CourseLayoutHelper.logoMaxHeight = logoMaxHeight;
	}
	
	/**
	 * [spring]
	 * 
	 * @param layouts comma-separated list of folder-names (e.g.
	 *          purple,green,blue)
	 */
	public void setLayoutBlacklist(String layouts){
		layoutBlacklist = Arrays.asList(layouts.split(","));
	}

	/**
	 * @return Returns the logoMaxHeight.
	 */
	public static int getLogoMaxHeight() {
		return logoMaxHeight;
	}

	/**
	 * [spring]
	 * @param logoMaxWidth The logoMaxWidth to set.
	 */
	public void setLogoMaxWidth(int logoMaxWidth) {
		CourseLayoutHelper.logoMaxWidth = logoMaxWidth;
	}

	/**
	 * @return Returns the logoMaxWidth.
	 */
	public static int getLogoMaxWidth() {
		return logoMaxWidth;
	}

	/**
	 * @return Returns the imageHelperToUse.
	 */
	public static IImageHelper getImageHelperToUse() {
		return imageHelperToUse;
	}

	/**
	 * allows to exchange the java implementation of Imagehelper.
	 * to use i.e. an imageMagick-instance to get better image-scaling (transparency, etc.)
	 * [spring]
	 * @param imageHelperToUse The imageHelperToUse to set.
	 */
	public void setImageHelperToUse(IImageHelper imageHelperToUse) {
		CourseLayoutHelper.imageHelperToUse = imageHelperToUse;
	}
	
	

}
