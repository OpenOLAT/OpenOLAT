/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingMainController;

/**
 * 
 * Initial date: 18.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
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
			} catch (IOException e) {
				log.error("Cannot found stylesheet at: " + path, e);
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
					log.error("", e);
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
