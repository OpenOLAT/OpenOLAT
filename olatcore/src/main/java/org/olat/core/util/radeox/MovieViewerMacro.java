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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.util.radeox;

import java.io.IOException;
import java.io.Writer;

import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;

/**
 * Description:<BR>
 * Implements timemarker movie viewer makro, still beta
 * <P>
 * Initial Date:  Dec 22, 2004
 *
 * @author gnaegi 
 */
public class MovieViewerMacro extends BaseMacro {
	// allowed macro attributes
	private static final String NAME = "name";
	private static final String ADDRESS = "url";
	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
	private static final String DISPLAYDURATION = "displayduration";
	
	// default height and width of player window
	private static final int DEFAULT_WIDTH = 320;
	private static final int DEFAULT_HEIGHT = 240;
	// additional pixels used by the video controller for sliding etc
	private static final int CONTROLLER_HEIGHT = 16;
	// offset to have enouth space
	// height+60 (some browsers need this space) + 20 (to show playButton tag)
	// width+40 (some browsers need this space)
	private static final int HEIGHT_OFFSET = 80;
	private static final int WIDTH_OFFSET = 40;

	/**
	 * 
	 */
	public MovieViewerMacro() {
		super();
	}

	/** 
	 * @see org.radeox.macro.Macro#getName()
	 */
	public String getName() {
		return "movieViewer";
	}

	/** 
	 * @see org.radeox.macro.Macro#execute(java.io.Writer, org.radeox.macro.parameter.MacroParameter)
	 */
	public void execute(Writer writer, MacroParameter params) throws IllegalArgumentException, IOException {

		String name = params.get(NAME);
		String address = params.get(ADDRESS);
		String height = params.get(HEIGHT);
		String width = params.get(WIDTH);
		String displayduration = params.get(DISPLAYDURATION);
		String displayname = params.getContent();

		if (address == null) {
			writer.write("<p>movieViewer: illegal format: missing attribute " + ADDRESS + "</p>");
			return;
		}
		// default values for unset optional parameter
		if (name == null) name = "remotelyControlled";
		if (width == null) width = Integer.toString(DEFAULT_WIDTH);
		if (height == null) height = Integer.toString(DEFAULT_HEIGHT);
		
		try {
			int iHeigt = Integer.parseInt(height);
			height = Integer.toString(iHeigt + CONTROLLER_HEIGHT );
		} catch (NumberFormatException e) {
			// using default values
			width = Integer.toString(DEFAULT_WIDTH);
			height = Integer.toString(DEFAULT_HEIGHT + CONTROLLER_HEIGHT);		
		}

		writer.write("<span class=\"b_wiki_timemarker_viewer\">");
		// show popup link
		writer.write("<a href=\"javascript:void(");
		// open window a bit bitter than the movie size itself
		String args = "width=" + (Integer.parseInt(width) + WIDTH_OFFSET) + ",height=" + (Integer.parseInt(height) + HEIGHT_OFFSET) + ",resizable=yes,scrollbars=yes";
		writer.write("window.open(o_info.o_baseURI + '/movie/popup.html?maddress=' + encodeURIComponent('" + address + "') + '&mname=' + encodeURIComponent('" + name + 
				"') + '&displayname=' + encodeURIComponent('" + displayname + "') + '&displayduration=' + encodeURIComponent('" + displayduration + "') + '&width=" + width + "&height=" + height + "', '" + name +"', '" + args + "')");
		writer.write(")\">Open movie player");
		if (displayname != null) {
			writer.write(": ");
			writer.write(displayname);
			if (displayduration != null) {
				writer.write(": ");
			}
		}
		if (displayduration != null) {
			writer.write(displayduration);
		}			
		writer.write("</a>");
		writer.write("</span>");
	}

}
