/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.Logger;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.xml.XMLFactories;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class TemplatesCache {
	private static final Logger log = Tracing.createLoggerFor(TemplatesCache.class);
	private static final Map<String, Templates> templates = new ConcurrentHashMap<>();

	public Transformer getTransformer(String templateName)
	throws TransformerConfigurationException, TransformerFactoryConfigurationError {
		
		Templates template;
		if (Settings.isDebuging()) {			
			File path = new File(WebappHelper.getSourcePath().replace("/java", "/resources"),"org/olat/modules/selectus/ui");
			try(InputStream inXslt = new FileInputStream(new File(path, templateName));	) {
				StreamSource sourceXslt = new StreamSource(inXslt);
				TransformerFactory factory = XMLFactories.newTransformerFactory();
				factory.setURIResolver(new ClasspathResourceURIResolver());
				template = factory.newTemplates(sourceXslt);
			} catch (Exception e) {
				log.error("Cannot found stylesheet at: {} / {}", path, templateName, e);
				return null;
			}
		} else {
			template = templates.computeIfAbsent(templateName, name -> {
				try(InputStream inXslt = RecruitingMainController.class.getResourceAsStream(name)) {
					StreamSource sourceXslt = new StreamSource(inXslt);
					TransformerFactory factory = XMLFactories.newTransformerFactory();
					factory.setURIResolver(new ClasspathResourceURIResolver());
					return factory.newTemplates(sourceXslt);
				} catch(Exception e) {
					log.error("Cannot read stylesheet: {}", name, e);
					return null;
				}
			});
		}
		return template.newTransformer();
	}
	
	private static class ClasspathResourceURIResolver implements URIResolver {
		@Override
		public Source resolve(String href, String base) throws TransformerException {
			return new StreamSource(PositionController.class.getResourceAsStream(href));
		}
	}
}
