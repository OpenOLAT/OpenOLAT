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
* <p>
*/ 

package org.olat.core.util.radeox;

import java.io.IOException;
import java.io.Writer;

import org.olat.core.defaults.dispatcher.StaticMediaDispatcher;
import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;

/**
 * Description:<BR>
 * Implements the timemarker play wiki tag. still beta
 * 
 * <P>
 * Initial Date:  Dec 22, 2004
 *
 * @author gnaegi 
 */
public class MovieButtonMacro extends BaseMacro {
	private static final String CONTROLLER_MOV = StaticMediaDispatcher.createStaticURIFor("movie/controller.mov");
	
	private static final String NAME = "name";
	private static final String STARTTIME = "start";
	private static final String ENDTIME = "end";
	private static final String ADDRESS = "url";
	private static final String DISPLAYDURATION = "displayduration";

	/**
	 * 
	 */
	public MovieButtonMacro() {
		super();
	}

	/** 
	 * @see org.radeox.macro.Macro#getName()
	 */
	public String getName() {
		return "movieButton";
	}

	/** 
	 * @see org.radeox.macro.Macro#execute(java.io.Writer, org.radeox.macro.parameter.MacroParameter)
	 */
	public void execute(Writer writer, MacroParameter params) throws IllegalArgumentException, IOException {
		
		String name = params.get(NAME);
		String starttime = params.get(STARTTIME);
		String endtime = params.get(ENDTIME);
		String address = params.get(ADDRESS);
		String displayduration = params.get(DISPLAYDURATION);
		String displayname = params.getContent();
		
		StringBuilder movieInfo = new StringBuilder();
		movieInfo.append("<movieinfo>");
		if (address != null) {
			movieInfo.append("<address>");
			movieInfo.append(address);
			movieInfo.append("</address>");
		}		
		if (name != null) {
			movieInfo.append("<name>");
			movieInfo.append(name);
			movieInfo.append("</name>");
		} else {
			// default name
			movieInfo.append("<name>remotelyControlled</name>");
		}
		if (starttime != null) {
			movieInfo.append("<starttime>");
			movieInfo.append(starttime);
			movieInfo.append("</starttime>");
		}
		if (endtime != null) {
			movieInfo.append("<endtime>");
			movieInfo.append(endtime);
			movieInfo.append("</endtime>");
		}
		if (displayname != null) {
			movieInfo.append("<label>");
			movieInfo.append(displayname);
			movieInfo.append("</label>");
		}
		if (displayname != null) {
			movieInfo.append("<displayname>");
			movieInfo.append(displayname);
			movieInfo.append("</displayname>");
		}
		movieInfo.append("</movieinfo>");
		
		
		writer.write("<span class=\"b_wiki_timemarker_play\"><object classid=\"clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B\"  width=\"17\" height=\"16\" codebase=\"http://www.apple.com/qtactivex/qtplugin.cab\">");
		writer.write("<param name=\"SRC\" value=\"" + CONTROLLER_MOV + "\">");
		writer.write("<param name=\"CONTROLLER\" value=\"FALSE\">");
		writer.write("<param name=\"AUTOPLAY\" value=\"FALSE\">");
		writer.write("<param name=\"CACHE\" value=\"FALSE\">");
		writer.write("<param name = \"MovieQTList\" value=");
		writer.write("\"");
		writer.write(movieInfo.toString());
		writer.write("\">");
		writer.write("<embed");
		writer.write(" src=\"" + CONTROLLER_MOV + "\"");
		writer.write(" width=\"17\" height=\"16\"");
		writer.write(" controller=\"FALSE\"");
		writer.write(" autoplay=\"FALSE\"");
		writer.write(" cache=\"FALSE\"");
		writer.write(" MovieQTList=");
		writer.write("\"");
		writer.write(movieInfo.toString());
		writer.write("\"");
		writer.write(" type=\"video/quicktime\"");
		writer.write(" pluginspage=\"http://www.apple.com/quicktime/download/\">");
		writer.write("</embed>");
		writer.write("</object> ");
		if (displayname != null) {
			writer.write(displayname);
			if (displayduration != null) {
				writer.write(": ");
			}
		}
		if (displayduration != null) {
			writer.write(displayduration);
		}			
		writer.write("</span>");
	}

}
