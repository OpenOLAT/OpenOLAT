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
package org.olat.course.config.ui.courselayout.elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.util.StringHelper;
import org.olat.course.config.ui.courselayout.attribs.AbstractLayoutAttribute;
import org.olat.course.config.ui.courselayout.attribs.PreviewLA;

/**
 * Description:<br>
 * does all common stuff for a css-element. 
 * extend this type; to get a concrete element implement createInstance!
 * there needs to be set three things (by setters):
 * - iFrameRelativeChildren	-> all possible child elements with relative position to choosen value, for iframe.css
 * - mainRelativeChilds			-> same as above, but for main-css
 * - availableAttributes		-> all possible attributes, that can be used for this element
 * 
 * using getCSSForMain / getCSSForIFrame returns all attributes as compiled String
 * <P>
 * Initial Date:  03.02.2011 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public abstract class AbstractLayoutElement {
	
	private List<AbstractLayoutAttribute> availableAttributes;
	private Map<String, String> config;
	private HashMap<String, Integer> iFrameRelativeChildren;
	private HashMap<String, Integer> mainRelativeChilds;
	
	protected AbstractLayoutElement(){
		//
	}
	
	protected AbstractLayoutElement(Map<String, String> config) {
		this.config = config;
	}

	public String getCSSForMain(){
		return loopChildren(mainRelativeChilds);
	}

	public String getCSSForIFrame(){
		return loopChildren(iFrameRelativeChildren);
	}
	/**
	 * @return
	 */
	private String loopChildren(HashMap<String, Integer> relativeChilds) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Integer> relChild : relativeChilds.entrySet()) {
			sb.append(relChild.getKey()).append(" { \n" );
			int rel = relChild.getValue();
			prepareAttributePart(rel, sb);			
			sb.append("}\n");
		}
		return sb.toString();
	}
		
	protected void prepareAttributePart(int rel, StringBuilder sb){
		for (AbstractLayoutAttribute attrib : availableAttributes) {
			if (StringHelper.containsNonWhitespace(attrib.getAttributeValue())){
				sb.append("\t");
				sb.append(attrib.getRelativeCompiledAttribute(rel));
			}
		}
	}
	
	protected void setIframeRelativeChildren(HashMap<String, Integer> iFrameRelativeChildren) {
		this.iFrameRelativeChildren = iFrameRelativeChildren;		
	}
	
	protected void setMainRelativeChildren(HashMap<String, Integer> mainRelativeChilds) {
		this.mainRelativeChilds = mainRelativeChilds;		
	}

	public void setAvailableAttributes(List<AbstractLayoutAttribute> availableAttributes) {
		this.availableAttributes = availableAttributes;
	}

	/**
	 * @return Returns the availableAttributes.
	 */
	public List<AbstractLayoutAttribute> getAvailableAttributes() {
		return availableAttributes;
	}
	
	public abstract String getLayoutElementTypeName();
	
	// factory method must be implemented to create a new instance of given type
	public abstract AbstractLayoutElement createInstance(Map<String, String> elConfig);
	
	protected void initAttributeConfig() {
		if (getConfig() != null) {
			List<AbstractLayoutAttribute> avAttribs = getAvailableAttributes();
			for (AbstractLayoutAttribute abstractLayoutAttribute : avAttribs) {
				String type = abstractLayoutAttribute.getLayoutAttributeTypeName();
				if (type.equals(PreviewLA.IDENTIFIER)){
					StringBuilder sbPreviewStyle = new StringBuilder();
					prepareAttributePart(0, sbPreviewStyle);
					abstractLayoutAttribute.setAttributeValue(sbPreviewStyle.toString());
				} else {
					abstractLayoutAttribute.setAttributeValue(getConfig().get(type));
				}
			}
		}
	}

	/**
	 * @return Returns the config.
	 */
	public Map<String, String> getConfig() {
		return config;
	}
	
	
}
