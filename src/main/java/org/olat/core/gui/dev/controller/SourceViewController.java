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
package org.olat.core.gui.dev.controller;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;

/**
 * Description:<br>
 * Displays java and/or velocity source in an new browserwindow
 * 
 * <P>
 * Initial Date:  03.09.2009 <br>
 * @author guido
 */
public class SourceViewController extends BasicController {
	
	private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html; charset=utf-8";
	
	private Link viewJSource;
	private Link viewVelocitySource;
	private final Class<?> clazz;
	private final VelocityContainer vc;
	private final VelocityContainer content;
	private static final String HTML_START = "<html><head><title>Code</title></head><body>";
	private static final String HTML_STOP = "</body></html>";

	public SourceViewController(UserRequest ureq, WindowControl control, Class<?> clazz, VelocityContainer vc) {
		super(ureq, control);
		this.clazz = clazz;
		this.vc = vc;
		
		content = createVelocityContainer("sourcecontrols");
		viewJSource = LinkFactory.createLink("jsource", content, this);
		viewJSource.setTarget("_blank");
		viewVelocitySource = LinkFactory.createLink("vsource", content, this);
		viewVelocitySource.setTarget("_blank");
		
		putInitialPanel(content);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == viewVelocitySource) {
			try {
				String velocityTemplatePath  = WebappHelper.getSourcePath() + "/" + vc.getPage();
				MediaResource mr = showVelocitySource(velocityTemplatePath);
				ureq.getDispatchResult().setResultingMediaResource(mr);
			} catch (IOException e) {
				logError("", e);
			}
		} else if (source == viewJSource) {
			try {
				String className = clazz.getCanonicalName();
				MediaResource mr = showjavaSource(className);
				ureq.getDispatchResult().setResultingMediaResource(mr);
			} catch (IOException e) {
				logError("", e);
			}
		}
	}
	
	public static MediaResource showVelocitySource(String velocityTemplatePath) throws IOException {
		File file = new File(velocityTemplatePath);
		StringWriter writer = new StringWriter(); 
		writer.append(HTML_START);
		if (file.exists()) {
			writer.append(FileUtils.load(file, "UTF-8"));
		} else {
			appendError(writer, velocityTemplatePath);
		}
		writer.append(HTML_STOP);
		
		StringMediaResource mr = new StringMediaResource();
		mr.setContentType(TEXT_HTML_CHARSET_UTF_8);
		mr.setData(writer.toString());
		return mr;
	}
	
	/**
	 * provide a class name with path and you will get an string media resource you can display
	 * @param cl
	 * @return
	 * @throws IOException
	 */
	public static MediaResource showjavaSource(String cl) throws IOException {
		cl = cl.replace('.', '/');
		String javaSourcePath  = WebappHelper.getSourcePath() + "/" + cl + ".java";
		File file = new File(javaSourcePath);
		StringWriter writer = new StringWriter(); 
		writer.append(HTML_START);
		if (file.exists()) {
			JavaSource jsource = new JavaSourceParser().parse(file);
			//Create a converter and write the JavaSource object as Html
			JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
			converter.convert(jsource, JavaSourceConversionOptions.getDefault(), writer);
		} else {
			appendError(writer, javaSourcePath);
		}
		writer.append(HTML_STOP);
		
		StringMediaResource mr = new StringMediaResource();
		mr.setContentType(TEXT_HTML_CHARSET_UTF_8);
		mr.setData(writer.toString());
		return mr;
	}
	
	private static void  appendError(Writer writer, String sourcePath) throws IOException {
		writer.append("<h3>The source file could not be found in the following path:<br>");
		writer.append(sourcePath);
		writer.append("<br>Check if configured source path in olat.local.properties is correct.</h3>");
	}

}
