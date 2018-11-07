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
package org.olat.course.config.ui.courselayout.attribs;


import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;

/**
 * 
 * Description:<br>
 * attribute: color
 * needs some dummy classes in olat.css
 * colored-dropdowns are not supported by all browsers!
 * 
 * <P>
 * Initial Date:  07.02.2011 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class ColorLA extends AbstractLayoutAttribute {

	private static final String IDENTIFIER = "color";

	public ColorLA() {
		setAttributeKey("color");

		String[] availKeys = new String[] { "Black", "Navy", "DarkBlue", "MediumBlue", "Blue", "DarkGreen", "Green", "Teal", "DarkCyan",
				"DeepSkyBlue", "DarkTurquoise", "MediumSpringGreen", "Lime", "SpringGreen", "Aqua", "Cyan", "MidnightBlue", "DodgerBlue",
				"LightSeaGreen", "ForestGreen", "SeaGreen", "DarkSlateGray", "DarkSlateGrey", "LimeGreen", "MediumSeaGreen", "Turquoise",
				"RoyalBlue", "SteelBlue", "DarkSlateBlue", "MediumTurquoise", "Indigo ", "DarkOliveGreen", "CadetBlue", "CornflowerBlue",
				"MediumAquaMarine", "DimGray", "DimGrey", "SlateBlue", "OliveDrab", "SlateGray", "SlateGrey", "LightSlateGray", "LightSlateGrey",
				"MediumSlateBlue", "LawnGreen", "Chartreuse", "Aquamarine", "Maroon", "Purple", "Olive", "Gray", "Grey", "SkyBlue", "LightSkyBlue",
				"BlueViolet", "DarkRed", "DarkMagenta", "SaddleBrown", "DarkSeaGreen", "LightGreen", "MediumPurple", "DarkViolet", "PaleGreen",
				"DarkOrchid", "YellowGreen", "Sienna", "Brown", "DarkGray", "DarkGrey", "LightBlue", "GreenYellow", "PaleTurquoise",
				"LightSteelBlue", "PowderBlue", "FireBrick", "DarkGoldenRod", "MediumOrchid", "RosyBrown", "DarkKhaki", "Silver",
				"MediumVioletRed", "IndianRed ", "Peru", "Chocolate", "Tan", "LightGray", "LightGrey", "PaleVioletRed", "Thistle", "Orchid",
				"GoldenRod", "Crimson", "Gainsboro", "Plum", "BurlyWood", "LightCyan", "Lavender", "DarkSalmon", "Violet", "PaleGoldenRod",
				"LightCoral", "Khaki", "AliceBlue", "HoneyDew", "Azure", "SandyBrown", "Wheat", "Beige", "WhiteSmoke", "MintCream", "GhostWhite",
				"Salmon", "AntiqueWhite", "Linen", "LightGoldenRodYellow", "OldLace", "Red", "Fuchsia", "Magenta", "DeepPink", "OrangeRed",
				"Tomato", "HotPink", "Coral", "Darkorange", "LightSalmon", "Orange", "LightPink", "Pink", "Gold", "PeachPuff", "NavajoWhite",
				"Moccasin", "Bisque", "MistyRose", "BlanchedAlmond", "PapayaWhip", "LavenderBlush", "SeaShell", "Cornsilk", "LemonChiffon",
				"FloralWhite", "Snow", "Yellow", "LightYellow", "Ivory", "White" };
		setAvailKeys(availKeys);
		String[] availValues = new String[] { "Black", "Navy", "DarkBlue", "MediumBlue", "Blue", "DarkGreen", "Green", "Teal", "DarkCyan",
				"DeepSkyBlue", "DarkTurquoise", "MediumSpringGreen", "Lime", "SpringGreen", "Aqua", "Cyan", "MidnightBlue", "DodgerBlue",
				"LightSeaGreen", "ForestGreen", "SeaGreen", "DarkSlateGray", "DarkSlateGrey", "LimeGreen", "MediumSeaGreen", "Turquoise",
				"RoyalBlue", "SteelBlue", "DarkSlateBlue", "MediumTurquoise", "Indigo ", "DarkOliveGreen", "CadetBlue", "CornflowerBlue",
				"MediumAquaMarine", "DimGray", "DimGrey", "SlateBlue", "OliveDrab", "SlateGray", "SlateGrey", "LightSlateGray", "LightSlateGrey",
				"MediumSlateBlue", "LawnGreen", "Chartreuse", "Aquamarine", "Maroon", "Purple", "Olive", "Gray", "Grey", "SkyBlue", "LightSkyBlue",
				"BlueViolet", "DarkRed", "DarkMagenta", "SaddleBrown", "DarkSeaGreen", "LightGreen", "MediumPurple", "DarkViolet", "PaleGreen",
				"DarkOrchid", "YellowGreen", "Sienna", "Brown", "DarkGray", "DarkGrey", "LightBlue", "GreenYellow", "PaleTurquoise",
				"LightSteelBlue", "PowderBlue", "FireBrick", "DarkGoldenRod", "MediumOrchid", "RosyBrown", "DarkKhaki", "Silver",
				"MediumVioletRed", "IndianRed ", "Peru", "Chocolate", "Tan", "LightGray", "LightGrey", "PaleVioletRed", "Thistle", "Orchid",
				"GoldenRod", "Crimson", "Gainsboro", "Plum", "BurlyWood", "LightCyan", "Lavender", "DarkSalmon", "Violet", "PaleGoldenRod",
				"LightCoral", "Khaki", "AliceBlue", "HoneyDew", "Azure", "SandyBrown", "Wheat", "Beige", "WhiteSmoke", "MintCream", "GhostWhite",
				"Salmon", "AntiqueWhite", "Linen", "LightGoldenRodYellow", "OldLace", "Red", "Fuchsia", "Magenta", "DeepPink", "OrangeRed",
				"Tomato", "HotPink", "Coral", "Darkorange", "LightSalmon", "Orange", "LightPink", "Pink", "Gold", "PeachPuff", "NavajoWhite",
				"Moccasin", "Bisque", "MistyRose", "BlanchedAlmond", "PapayaWhip", "LavenderBlush", "SeaShell", "Cornsilk", "LemonChiffon",
				"FloralWhite", "Snow", "Yellow", "LightYellow", "Ivory", "White" };
		setAvailValues(availValues);
		String[] availCSS = new String[] { "Black", "Navy", "DarkBlue", "MediumBlue", "Blue", "DarkGreen", "Green", "Teal", "DarkCyan",
				"DeepSkyBlue", "DarkTurquoise", "MediumSpringGreen", "Lime", "SpringGreen", "Aqua", "Cyan", "MidnightBlue", "DodgerBlue",
				"LightSeaGreen", "ForestGreen", "SeaGreen", "DarkSlateGray", "DarkSlateGrey", "LimeGreen", "MediumSeaGreen", "Turquoise",
				"RoyalBlue", "SteelBlue", "DarkSlateBlue", "MediumTurquoise", "Indigo ", "DarkOliveGreen", "CadetBlue", "CornflowerBlue",
				"MediumAquaMarine", "DimGray", "DimGrey", "SlateBlue", "OliveDrab", "SlateGray", "SlateGrey", "LightSlateGray", "LightSlateGrey",
				"MediumSlateBlue", "LawnGreen", "Chartreuse", "Aquamarine", "Maroon", "Purple", "Olive", "Gray", "Grey", "SkyBlue", "LightSkyBlue",
				"BlueViolet", "DarkRed", "DarkMagenta", "SaddleBrown", "DarkSeaGreen", "LightGreen", "MediumPurple", "DarkViolet", "PaleGreen",
				"DarkOrchid", "YellowGreen", "Sienna", "Brown", "DarkGray", "DarkGrey", "LightBlue", "GreenYellow", "PaleTurquoise",
				"LightSteelBlue", "PowderBlue", "FireBrick", "DarkGoldenRod", "MediumOrchid", "RosyBrown", "DarkKhaki", "Silver",
				"MediumVioletRed", "IndianRed ", "Peru", "Chocolate", "Tan", "LightGray", "LightGrey", "PaleVioletRed", "Thistle", "Orchid",
				"GoldenRod", "Crimson", "Gainsboro", "Plum", "BurlyWood", "LightCyan", "Lavender", "DarkSalmon", "Violet", "PaleGoldenRod",
				"LightCoral", "Khaki", "AliceBlue", "HoneyDew", "Azure", "SandyBrown", "Wheat", "Beige", "WhiteSmoke", "MintCream", "GhostWhite",
				"Salmon", "AntiqueWhite", "Linen", "LightGoldenRodYellow", "OldLace", "Red", "Fuchsia", "Magenta", "DeepPink", "OrangeRed",
				"Tomato", "HotPink", "Coral", "Darkorange", "LightSalmon", "Orange", "LightPink", "Pink", "Gold", "PeachPuff", "NavajoWhite",
				"Moccasin", "Bisque", "MistyRose", "BlanchedAlmond", "PapayaWhip", "LavenderBlush", "SeaShell", "Cornsilk", "LemonChiffon",
				"FloralWhite", "Snow", "Yellow", "LightYellow", "Ivory", "White" };
		setAvailCSS(availCSS);
	}
	
	@Override
	public String getLayoutAttributeTypeName() {
		return IDENTIFIER;
	}



	/**
	 * @see org.olat.course.config.ui.courselayout.attribs.AbstractLayoutAttribute#getFormItem(java.lang.String, org.olat.core.gui.components.form.flexible.FormItemContainer)
	 * get a dropdown and an input field wrapped in a FormLayoutContainer
	 */
	@Override
	public FormItem getFormItem(String compName, FormItemContainer formLayout) {
		FormUIFactory uifact = FormUIFactory.getInstance();
		FormLayoutContainer colorFLC = FormLayoutContainer.createVerticalFormLayout(compName, formLayout.getTranslator());
		formLayout.add(compName, colorFLC);	
		FormItem dropDown = super.getFormItem(compName + "sel", formLayout);
		dropDown.addActionListener(FormEvent.ONCHANGE);
		colorFLC.add(dropDown);
		
		String inputValue = "";
		if (getAttributeValue()!=null && !((SingleSelection)dropDown).isOneSelected()){
			inputValue = getAttributeValue();
		}
		
		TextElement inputEl = uifact.addTextElement(compName + "value", null, 7, inputValue, colorFLC);
		inputEl.setDisplaySize(7);
		colorFLC.setUserObject(new ColorSpecialHandler(colorFLC));		
		return colorFLC;
	}
	
}

