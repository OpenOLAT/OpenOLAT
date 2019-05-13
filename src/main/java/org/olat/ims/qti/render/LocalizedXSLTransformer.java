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

package org.olat.ims.qti.render;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocument;
import org.dom4j.io.DocumentSource;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.ims.qti.QTI12ResultDetailsController;
import org.olat.ims.resources.IMSEntityResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Mike Stock Comment:
 * Initial Date: 04.06.2003
 */
public class LocalizedXSLTransformer {
	private static ConcurrentHashMap<String, LocalizedXSLTransformer> instanceHash = new ConcurrentHashMap<>(5);
	private static final Logger log = Tracing.createLoggerFor(LocalizedXSLTransformer.class);
	private static EntityResolver er = new IMSEntityResolver();
	private static VelocityEngine velocityEngine;
	
	static {
		// init velocity engine
		Properties p = new Properties();
		try {
			velocityEngine = new VelocityEngine();
			velocityEngine.init(p);
		} catch (Exception e) {
			throw new OLATRuntimeException("config error with velocity properties::" + p, e);
		}		
	}
	
	private Translator pT;
	private Templates templates;
	/**
	 * <code>RESULTS2HTML</code>
	 */
	private static final String XSLFILENAME = "results2html_generic.xsl";

	/**
	 * Private constructor, use getInstance to get an instance of the
	 * LocalizedXSLTransformer
	 * 
	 * @param trans
	 */
	private LocalizedXSLTransformer(Translator trans) {
		pT = trans;
		initTransformer();
	}
	
	/**
	 * Get a localized transformer instance.
	 * 
	 * @param locale The locale for this transformer instance
	 * @return A localized transformer
	 */
	 // cluster_ok only in VM
	public static LocalizedXSLTransformer getInstance(Locale locale) {
		I18nModule i18nModule = CoreSpringFactory.getImpl(I18nModule.class);
		LocalizedXSLTransformer instance = instanceHash.get(i18nModule.getLocaleKey(locale));
		if (instance == null) {
			Translator trans = Util.createPackageTranslator(QTI12ResultDetailsController.class, locale);
			LocalizedXSLTransformer newInstance = new LocalizedXSLTransformer(trans);
			instance = instanceHash.putIfAbsent(i18nModule.getLocaleKey(locale), newInstance); //see javadoc of ConcurrentHashMap
			if(instance == null) { //newInstance was put into the map
				instance = newInstance;
			}
		}
		return instance;
	}

	/**
	 * Render with a localized stylesheet. The localized stylesheet is addressed
	 * by its name with appended locale. E.g. mystyle.xsl in DE locale is
	 * addressed by mystyle_de.xsl
	 * 
	 * @param node The node to render
	 * @param styleSheetName The stylesheet to use.
	 * @return Results of XSL transformation
	 */
	private String render(Element node) {
		try {
			Document doc = node.getDocument();
			if (doc == null) {
				doc = new DOMDocument();
				doc.add(node);
			}
			DocumentSource xmlsource = new DocumentSource(node);
			
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			templates.newTransformer().transform(xmlsource, result);
			return sw.toString();
		} catch (Exception e) {
			throw new OLATRuntimeException(LocalizedXSLTransformer.class, "Error transforming XML.", e);
		}
	}

	/**
	 * Render results processing document
	 * 
	 * @param doc The <results/>document
	 * @return transformation results
	 */
	public String renderResults(Document doc) {
		return render(doc.getRootElement());
	}

	/**
	 * Helper to create XSLT transformer for this instance
	 */
	private void initTransformer() {
		// translate xsl with velocity
		Context vcContext = new VelocityContext();
		vcContext.put("t", pT);
		vcContext.put("staticPath", StaticMediaDispatcher.createStaticURIFor(""));
		String xslAsString = "";
		try(InputStream xslin = getClass().getResourceAsStream("/org/olat/ims/resources/xsl/" + XSLFILENAME)) {
			xslAsString = slurp(xslin);
		} catch (IOException e) {
			log.error("Could not convert xsl to string!", e);
		}
		String replacedOutput = evaluateValue(xslAsString, vcContext);
		TransformerFactory tfactory = newTransformerFactory();
		XMLReader reader;
		try {
			reader = XMLReaderFactory.createXMLReader();
			reader.setEntityResolver(er);
			Source xsltsource = new SAXSource(reader, new InputSource(new StringReader(replacedOutput)));
			templates = tfactory.newTemplates(xsltsource);
		} catch (SAXException e) {
			throw new OLATRuntimeException("Could not initialize transformer!", e);
		} catch (TransformerConfigurationException e) {
			throw new OLATRuntimeException("Could not initialize transformer (wrong config)!", e);
		}
	}
	
	/**
	 * The method try to find the Xalan implementation of the JDK because the styelsheet
	 * was with this one develop and not the Saxon XSLT 2.0 or 3.0 engine which lead to
	 * some compatibility issue with special HTML entity (example: 129).
	 * 
	 * @return Try to get the embedded Xalan implementation which is a pure XSLT 1.0 engine.
	 */
	private TransformerFactory newTransformerFactory() {
		TransformerFactory tfactory = null;
		try {
			tfactory = TransformerFactory.newInstance("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", null);
		} catch (TransformerFactoryConfigurationError e) {
			log.error("", e);
			tfactory = TransformerFactory.newInstance();
		}
		return tfactory;
	}

	/**
	 * Takes String with template and fills values from Translator in Context
	 * 
	 * @param valToEval String with variables to replace
	 * @param vcContext velocity context containing a translator in this case
	 * @return input String where values from context were replaced
	 */
	private String evaluateValue(String valToEval, Context vcContext) {
		StringWriter evaluatedValue = new StringWriter();
		// evaluate inputFieldValue to get a concatenated string
		try {
			velocityEngine.evaluate(vcContext, evaluatedValue, "vcUservalue", valToEval);
		} catch (ParseErrorException e) {
			log.error("parsing of values in xsl-file of LocalizedXSLTransformer not possible!", e);
			return "ERROR";
		} catch (MethodInvocationException e) {
			log.error("evaluating of values in xsl-file of LocalizedXSLTransformer not possible!", e);
			return "ERROR";
		} catch (ResourceNotFoundException e) {
			log.error("xsl-file of LocalizedXSLTransformer not found!", e);
			return "ERROR";
		} catch (Exception e) {
			log.error("could not read xsl-file of LocalizedXSLTransformer!", e);
			return "ERROR";
		}
		return evaluatedValue.toString();
	}
	
	/**
	 * convert xsl InputStream to String
	 * @param in
	 * @return xsl as String
	 * @throws IOException
	 */
	private static String slurp(InputStream in) throws IOException {
	   StringBuilder out = new StringBuilder();
	   byte[] b = new byte[4096];
	   for (int n; (n = in.read(b)) != -1;) {
	       out.append(new String(b, 0, n));
	   }
	   return out.toString();
	}
}