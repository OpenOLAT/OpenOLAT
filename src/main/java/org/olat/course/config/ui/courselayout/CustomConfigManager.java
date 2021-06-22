/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.config.ui.courselayout;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSItemSuffixFilter;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.config.ui.courselayout.attribs.AbstractLayoutAttribute;
import org.olat.course.config.ui.courselayout.elements.AbstractLayoutElement;
import org.olat.course.run.environment.CourseEnvironment;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Description:<br>
 * used to load and persist the custom config
 * 
 * <P>
 * Initial Date:  04.02.2011 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class CustomConfigManager {
	
	private static final Logger log = Tracing.createLoggerFor(CustomConfigManager.class);
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	
	private static final String IFRAME_CSS = "iframe.css";
	private static final String MAIN_CSS = "main.css";
	private static final String CUSTOM_CONFIG_XML = "config.xml";
	private List<AbstractLayoutElement> availableLayoutElements;
	private List<AbstractLayoutAttribute> availableLayoutAttributes;
	
	
	CustomConfigManager(){
		//
	}	
	
	/**
	 * save the custom css configuration in a reprocessable format (map with inner map)
	 * also generates the two needed css-files (main / iframe)
	 * @param customConfig
	 * @param courseEnvironment
	 */
	public void saveCustomConfigAndCompileCSS(Map<String, Map<String, String>> customConfig, CourseEnvironment courseEnvironment){
		VFSContainer themeBase = null;
		VFSContainer base = null;
		base = (VFSContainer) courseEnvironment.getCourseBaseContainer().resolve(CourseLayoutHelper.LAYOUT_COURSE_SUBFOLDER);
		if (base == null) {
			base = courseEnvironment.getCourseBaseContainer().createChildContainer(CourseLayoutHelper.LAYOUT_COURSE_SUBFOLDER);
		}
		themeBase = (VFSContainer) base.resolve("/" + CourseLayoutHelper.CONFIG_KEY_CUSTOM);
		if (themeBase == null) {
			themeBase = base.createChildContainer(CourseLayoutHelper.CONFIG_KEY_CUSTOM);
		}
		VFSLeaf configTarget = (VFSLeaf) themeBase.resolve(CUSTOM_CONFIG_XML);
		if (configTarget == null) {
			configTarget = themeBase.createChildLeaf(CUSTOM_CONFIG_XML);
		} 
		
		xstream.toXML(customConfig, configTarget.getOutputStream(false));
		
		// compile the css-files
		StringBuilder sbMain = new StringBuilder();
		StringBuilder sbIFrame = new StringBuilder();
		for (Entry<String, Map<String, String>> iterator : customConfig.entrySet()) {
			String type = iterator.getKey();
			Map<String, String> elementConfig = iterator.getValue();			
			AbstractLayoutElement configuredLayEl = createLayoutElementByType(type, elementConfig);
			sbIFrame.append(configuredLayEl.getCSSForIFrame());	
			sbMain.append(configuredLayEl.getCSSForMain());	
		}
		
		// attach line for logo, if there is any to cssForMain:
		appendLogoPart(sbMain, themeBase);
		
		VFSLeaf mainFile = (VFSLeaf) themeBase.resolve(MAIN_CSS);
		if (mainFile == null) mainFile = themeBase.createChildLeaf(MAIN_CSS);
		VFSLeaf iFrameFile = (VFSLeaf) themeBase.resolve(IFRAME_CSS);
		if (iFrameFile == null) iFrameFile = themeBase.createChildLeaf(IFRAME_CSS);
		FileUtils.save(mainFile.getOutputStream(false), sbMain.toString(), "utf-8");
		FileUtils.save(iFrameFile.getOutputStream(false), sbIFrame.toString(), "utf-8");		
	}

	private void appendLogoPart(StringBuilder sb, VFSContainer themeBase) {
		VFSItem vfsItem = getLogoItem(themeBase);
		if (vfsItem != null) {
			sb.append("#o_right_logo {\n\tbackground-image: url(").append(vfsItem.getName()).append("); \n");
			sb.append("\tbackground-position: left top; \n");
			sb.append("\tbackground-repeat: no-repeat; \n");
			LocalFileImpl leaf = (LocalFileImpl) vfsItem;
			int[] size = getImageSize(leaf.getBasefile());
			if(size != null) {
				sb.append("\twidth: ").append(size[0]).append("px; \n")
				  .append("\theight: ").append(size[1]).append("px; \n");
			}
			sb.append("\tfloat: left; \n}\n");
			sb.append("#o_logo { \n\t float: left; \n}");
		}
	}

	public VFSItem getLogoItem(VFSContainer themeBase){
		if (themeBase == null) return null;
		List<VFSItem> images = themeBase.getItems(new VFSItemSuffixFilter(new String[] { "gif", "jpg", "png" }));
		for (VFSItem vfsItem : images) {
			if (vfsItem.getName().indexOf("logo")!=-1) {
				return vfsItem;
			}
		} return null;
	}
	
	/**
	 * calculate the real size of an image
	 * @param image
	 * @return array[width, height]
	 */
	public int[] getImageSize(File image){
		int height = 0;
		int width = 0;
		try {
			BufferedImage imageSrc = ImageIO.read(image);
			if(imageSrc != null) {
				height = imageSrc.getHeight();
				width = imageSrc.getWidth();
			} else {
				return null;
			}
		} catch (IOException e) {
			log.error("Problem reading uploaded image", e);
			return null;
		}
		return new int[] { width, height };
	}
	
	/**
	 * load the persisted customConfig
	 * this is the custom.xml file in bcroot/course/<courseID>/layout/custom
	 * @param courseEnvironment
	 * @return
	 */
	public Map<String, Map<String, String>> getCustomConfig(CourseEnvironment courseEnvironment){
		Map<String, Map<String, String>> defaultConf = new HashMap<>();
		VFSContainer base = (VFSContainer) courseEnvironment.getCourseBaseContainer().resolve(CourseLayoutHelper.LAYOUT_COURSE_SUBFOLDER);
		if (base == null) {
			return defaultConf;
		}
		VFSContainer themeBase = (VFSContainer) base.resolve("/" + CourseLayoutHelper.CONFIG_KEY_CUSTOM);
		if (themeBase == null) {
			return defaultConf;
		}
		VFSLeaf configTarget = (VFSLeaf) themeBase.resolve(CUSTOM_CONFIG_XML);
		if (configTarget == null) {
			return defaultConf;
		}
		try(InputStream in=configTarget.getInputStream()) {
			return fromXML(in);
		} catch(IOException e) {
			log.error("", e);
			return defaultConf;
		}
	}

	@SuppressWarnings("unchecked")
	protected static Map<String, Map<String, String>> fromXML(InputStream in) {
		return (Map<String, Map<String, String>>) xstream.fromXML(in);
	}
	
	/**
	 * get all possible layout elements. the real instances need to be created with the factory method!
	 * @return
	 */
	public List<AbstractLayoutElement> getAllAvailableElements(){
		return availableLayoutElements;
	}
	
	// get a list of configured attributes
	public List<AbstractLayoutAttribute> getAllAvailableAttributes(){
		return availableLayoutAttributes;
	}

	// creates an instance of the given type with the factory-method and sets config for its attributes
	public AbstractLayoutElement createLayoutElementByType(String type, Map<String, String> config){
		List<AbstractLayoutElement> allElements = getAllAvailableElements();
		for (AbstractLayoutElement abstractLayoutElement : allElements) {
			if(abstractLayoutElement.getLayoutElementTypeName().equals(type)) {
				return abstractLayoutElement.createInstance(config);
			}
		}
		return null;
	}
		

	/**
	 * [spring]
	 * @param availableLayoutElements The availableLayoutElements to set.
	 */
	public void setAvailableLayoutElements(List<AbstractLayoutElement> availableLayoutElements) {
		this.availableLayoutElements = availableLayoutElements;
	}

	/**
	 * [spring]
	 * @param availableLayoutAttributes The availableLayoutAttributes to set.
	 */
	public void setAvailableLayoutAttributes(List<AbstractLayoutAttribute> availableLayoutAttributes) {
		this.availableLayoutAttributes = availableLayoutAttributes;
	}
		
}
