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
package org.olat.core.gui.render;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.gui.render.velocity.VelocityModule;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.SystemFileFilter;

/**
 * This test parse all velocity templates and check if they
 * are parseable.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VelocityTemplateTest {
	private static final OLog log = Tracing.createLoggerFor(VelocityTemplateTest.class);

	public static final String MAIN_JAVA = "src/main/java";
	
	private VelocityEngine engine;
	private int count = 0;
	
	@Test
	public void testTemplates() {
		engine = getEngine();
		File javaSources = new File(MAIN_JAVA);
		List<Exception> exs = new ArrayList<>();
		testTemplates("", javaSources, exs);
		
		for(Exception ex:exs) {
			log.error(ex.getMessage());
		}
		Assert.assertEquals(0, exs.size());
		log.info("We have " + count + " velocity templates.");
	}
	
	private void testTemplates(String dir, File file, List<Exception> exs) {
		String name = file.getName();
		if("_content".equals(name)) {
			File[] templates = file.listFiles(SystemFileFilter.DIRECTORY_FILES);
			for(File template:templates) {
				String templateName = template.getName();
				if(templateName.endsWith(".html")) {
					try(StringWriter writer = new StringWriter()) {
						String path = dir + templateName;
						Context context = new VelocityContext();
						Template veloTemplate = engine.getTemplate(path);
						veloTemplate.merge(context, writer);
						count++;
					} catch (Exception e) {
						exs.add(e);
					}
				}
			}
		} else if(file.isDirectory()) {
			File[] files = file.listFiles(SystemFileFilter.DIRECTORY_FILES);
			for(File child:files) {
				String subDir = dir + child.getName() + "/";
				testTemplates(subDir, child, exs);
			}
		}
	}
	
	private VelocityEngine getEngine() {
		Properties p = new Properties();
		p.setProperty(RuntimeConstants.INPUT_ENCODING, VelocityModule.getInputEncoding());

		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "file, classpath");					
		p.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
		p.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, MAIN_JAVA);
		p.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "false");								
		p.setProperty("file.resource.loader.modificationCheckInterval", "3");

		p.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		p.setProperty("classpath.resource.loader.cache", "false");
		
		p.setProperty(RuntimeConstants.RESOURCE_MANAGER_LOGWHENFOUND, "false");
		p.setProperty(RuntimeConstants.VM_LIBRARY, "velocity/olat_velocimacros.vm");
		p.setProperty(RuntimeConstants.VM_LIBRARY_AUTORELOAD, "false");

		VelocityEngine ve = new VelocityEngine();
		ve.init(p);
		return ve;
	}
}
