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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
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
	private VelocityContainer content;
	private Link viewJSource, viewVeloctiySource;
	private Class<?> clazz;
	private VelocityContainer vc, sourceview;
	private CloseableModalController view;
	private final static String HTML_START = "<html><body>";
	private final static String HTML_STOP = "</body></html>";

	public SourceViewController(UserRequest ureq, WindowControl control, Class<?> clazz, VelocityContainer vc) {
		super(ureq, control);
		this.clazz = clazz;
		this.vc = vc;
		sourceview = createVelocityContainer("sourceview");
		content = createVelocityContainer("sourcecontrols");

		viewJSource = LinkFactory.createLink("jsource", content, this);
		viewJSource.setTarget("_blank");
		
		viewVeloctiySource = LinkFactory.createLink("vsource", content, this);
		
		putInitialPanel(content);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == viewVeloctiySource) {
			String velocityTemplatePath  = WebappHelper.getSourcePath()+"/"+vc.getPage();
			String vcContent = FileUtils.load(new File(velocityTemplatePath), "utf-8");
			sourceview.contextPut("content", vcContent);
			sourceview.contextPut("vcname", vc.getPage());
			view = new CloseableModalController(getWindowControl(),"close...", sourceview);
			listenTo(view);
			view.activate();
			
		} else if (source == viewJSource) {
			
			//Parse the raw text to a JavaSource object
			JavaSource jsource = null;
			try {
				String className = clazz.getCanonicalName();
				className = className.replace('.', '/');
				String sourcePath = WebappHelper.getSourcePath()+"/"+className +".java";
				jsource = new JavaSourceParser().parse(new File(sourcePath));
			} catch (IOException e) {
			  showInfo("todo");
			}
	
			//Create a converter and write the JavaSource object as Html
			JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
			StringWriter writer = new StringWriter(); 
			writer.append(HTML_START);
			try {
				 JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
				 options.setShowLineNumbers(true);
			  converter.convert(jsource, options, writer);
			} catch (IOException e) {
				//
			}
			StringMediaResource mr = new StringMediaResource();
			mr.setContentType(TEXT_HTML_CHARSET_UTF_8);
			writer.append(HTML_STOP);
			mr.setData(writer.toString());
			ureq.getDispatchResult().setResultingMediaResource(mr);
		}

	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == view) {
			
		}
	}
	
	/**
	 * provide a class name with path and you will get an string media resource you can display
	 * @param cl
	 * @return
	 * @throws IOException
	 */
	public static MediaResource showjavaSource(String cl) throws IOException {
		JavaSource jsource = null;
		cl = cl.replace('.', '/');
		String javaSourcePath  = WebappHelper.getSourcePath()+"/"+cl+".java";
		File file = new File(javaSourcePath);
		StringWriter writer = new StringWriter(); 
		writer.append(HTML_START);
		if (file.exists()) {
			jsource = new JavaSourceParser().parse(file);
			//Create a converter and write the JavaSource object as Html
			JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
			converter.convert(jsource, JavaSourceConversionOptions.getDefault(), writer);
		} else {
			writer.append("<html><body><h3>The source file could not be found in the following path:<br>"+javaSourcePath+"<br>Check if configured source path in brasatoconfig.xml is correct.</h3></body></html>");
		}
		
		StringMediaResource mr = new StringMediaResource();
		mr.setContentType(TEXT_HTML_CHARSET_UTF_8);
		writer.append(HTML_STOP);
		mr.setData(writer.toString());
		return mr;
		
	}

}
