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
package org.olat.modules.selectus.ui.document;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.AcademicalBackgroundImpl;
import org.olat.modules.selectus.model.AddressImpl;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAttribute;
import org.olat.modules.selectus.model.ApplicationAttributeImpl;
import org.olat.modules.selectus.model.ApplicationImpl;
import org.olat.modules.selectus.model.AttachmentImpl;
import org.olat.modules.selectus.model.AttachmentsImpl;
import org.olat.modules.selectus.model.PersonImpl;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionImpl;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.PositionImpl;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.attributes.SelectConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.model.references.ReferenceImpl;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomWriter;

/**
 * 
 * Initial date: 30 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationXML {
	private static final Logger log = Tracing.createLoggerFor(ApplicationXML.class);

	private static final XStream myXStream = XStreamHelper.createXStreamInstance();

	static {
		myXStream.alias("position", PositionImpl.class);
		myXStream.alias("application", ApplicationImpl.class);
		myXStream.alias("background", AcademicalBackgroundImpl.class);
		myXStream.alias("attachments", AttachmentsImpl.class);
		myXStream.alias("attachment", AttachmentImpl.class);
		myXStream.alias("address", AddressImpl.class);
		myXStream.alias("person", PersonImpl.class);
		myXStream.alias("reference", ReferenceImpl.class);
		myXStream.alias("applicationAttribute", ApplicationAttributeImpl.class);
		myXStream.alias("attributeDefinition", PositionAttributeDefinitionImpl.class);
		
		myXStream.omitField(ApplicationImpl.class, "position");
		myXStream.omitField(ApplicationImpl.class, "attributes");

		myXStream.omitField(ReferenceImpl.class, "application");
		myXStream.omitField(PositionImpl.class, "committeeGroup");
		myXStream.omitField(PositionImpl.class, "committeeHeadGroup");
		myXStream.omitField(PositionImpl.class, "exOfficioGroup");
		myXStream.omitField(PositionImpl.class, "secretaryGroup");
		myXStream.omitField(PositionImpl.class, "expertRecommandationMailTemplate");
		myXStream.omitField(PositionImpl.class, "refereeRecommandationMailTemplate");
		myXStream.omitField(PositionImpl.class, "reviewDefinition");
		myXStream.omitField(PositionImpl.class, "attributes");
		myXStream.omitField(PositionImpl.class, "tabsConfiguration");
		myXStream.omitField(PositionImpl.class, "attributesDefinitions");
		
		myXStream.omitField(PositionAttributeDefinitionImpl.class, "attributeConfiguration");
		myXStream.omitField(PositionAttributeDefinitionImpl.class, "creationDate");
		myXStream.omitField(PositionAttributeDefinitionImpl.class, "lastModified");
		
		myXStream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	private final Position position;
	private final Application application;
	private final Translator translator;
	
	private final RecruitingModule recruitingModule;
	
	public ApplicationXML(Position position, Application application, Translator translator, RecruitingModule recruitingModule) {
		this.position = position;
		this.application = application;
		this.translator = translator;
		this.recruitingModule = recruitingModule;
		
		if(!recruitingModule.isPositionPlannigIdEnabled()) {
			myXStream.omitField(PositionImpl.class, "planingsNumber");
		}
		if(!recruitingModule.isPositionDepartmentEnabled()) {
			myXStream.omitField(PositionImpl.class, "department");
		}
		if(!recruitingModule.isPositionHomepageEnabled()) {
			myXStream.omitField(PositionImpl.class, "homepage");
		}
	}
	
	protected Document appendCoverDocument(Document doc, Element rootEl) {
		rootEl.setAttribute("output-date", getOutputDate());
		enhanceDOM(doc, rootEl);
		return doc;
	}
	
	protected Document appendErrorDocument(Document doc, Element rootEl, String document) {
		rootEl.setAttribute("output-date", getOutputDate());
		if(StringHelper.containsNonWhitespace(document)) {
			rootEl.setAttribute("document", document);
		}
		enhanceDOM(doc, rootEl);
		return doc;
	}
	
	protected Document appendSeparatorDocument(Document doc, Element rootEl, DocumentEnum docEnum) {
		doc.appendChild(rootEl);
		rootEl.setAttribute("output-date", getOutputDate());
		String separatorName = position.getDocumentName(docEnum, translator.getLocale());
		if(!StringHelper.containsNonWhitespace(separatorName)) {
			separatorName = translator.translate(docEnum.i18nKey());
		}
		rootEl.setAttribute("separator-name", separatorName);
		enhanceDOM(doc, rootEl);
		return doc;
	}
	
	protected Document appendReferenceDocument(Document doc, Element rootEl, Reference reference, ReferenceType type, int pos)  {
		rootEl.setAttribute("reference-pos", Integer.toString(pos));
		rootEl.setAttribute("reference-type", type.name());
		doc.appendChild(rootEl);
		rootEl.setAttribute("output-date", getOutputDate());
		enhanceDOM(doc, rootEl);
		myXStream.marshal(reference, new DomWriter(rootEl));
		return doc;
	}
	
	private void enhanceDOM(Document doc, Element rootEl) {
		myXStream.marshal(application, new DomWriter(rootEl));
		myXStream.marshal(position, new DomWriter(rootEl));
		updatePositionTitle(doc);
		updateTitle(doc);
		updateAttachments(doc, rootEl);
		List<Tab> tabs = getEnabledTabs();
		updateAdditionalAttributes(doc, tabs);
	}
	
	private void updateAdditionalAttributes(Document doc, List<Tab> tabs) {
		List<PositionAttributeDefinition> definitions = position.getAttributesDefinitions();
		if(definitions.isEmpty()) return;

		Set<ApplicationAttribute> attributes = application.getAttributes();
		Map<PositionAttributeDefinition,ApplicationAttribute> attributesMap = attributes
				.stream().collect(Collectors.toMap(ApplicationAttribute::getDefinition, attr -> attr));

		for(Node applicationNode=doc.getDocumentElement().getFirstChild(); applicationNode != null; applicationNode=applicationNode.getNextSibling()) {
			//loop to find application
			//loop to find position
			if("application".equals(applicationNode.getNodeName())) {
				Node additionalAttributesNode = applicationNode
						.appendChild(doc.createElement("additionalAttributes"));
				
				for(Tab tab:tabs) {
					Element stepNode = doc.createElement("step");
					stepNode.setAttribute("tab", tab.name());
					boolean someData = false;
					
					for(PositionAttributeDefinition definition:definitions) {
						if(definition.getTabEnum() == tab.attributesTab()) {
							ApplicationAttribute attribute = attributesMap.get(definition);
							PositionAttributeDefinitionTypeEnum type = definition.getTypeEnum();
							if(type == PositionAttributeDefinitionTypeEnum.heading) {
								updateAdditionalHeading(doc, definition, additionalAttributesNode);
							} else if(type == PositionAttributeDefinitionTypeEnum.question
									|| type == PositionAttributeDefinitionTypeEnum.select
									|| type == PositionAttributeDefinitionTypeEnum.number
									|| type == PositionAttributeDefinitionTypeEnum.percentage
									|| type == PositionAttributeDefinitionTypeEnum.date) {
								updateAdditionalAttribute(doc, attribute, definition, stepNode);
								someData = true;
							}
						}
					}
					
					if(someData) {
						additionalAttributesNode.appendChild(stepNode);
					}
				}
			}
		}
	}
	
	private void updateAdditionalHeading(Document doc, PositionAttributeDefinition definition, Node additionalAttributesNode) {
		Element additionalAttributeNode = (Element)additionalAttributesNode
				.appendChild(doc.createElement("additionalAttribute"));
		additionalAttributeNode.setAttribute("type", PositionAttributeDefinitionTypeEnum.heading.name());
		additionalAttributeNode.setAttribute("label", definition.getLabel(Locale.ENGLISH, true));
	}
	
	private void updateAdditionalAttribute(Document doc, ApplicationAttribute attribute, PositionAttributeDefinition definition, Node additionalAttributesNode) {
		if(attribute == null || !StringHelper.containsNonWhitespace(attribute.getValue())) return;
		
		String value = attribute.getValue();
		PositionAttributeDefinitionTypeEnum type = definition.getTypeEnum();

		Element additionalAttributeNode = (Element)additionalAttributesNode
				.appendChild(doc.createElement("additionalAttribute"));
		additionalAttributeNode.setAttribute("tab", definition.getTabEnum().name());
		additionalAttributeNode.setAttribute("type", type.name());
		additionalAttributeNode.setAttribute("label", definition.getLabel(Locale.ENGLISH, true));
		
		if(type == PositionAttributeDefinitionTypeEnum.select) {
			SelectConfiguration selectConfiguration = definition.getConfiguration(SelectConfiguration.class);
			if(selectConfiguration != null) {
				value = ApplicationAttributesDelegate.getLocalizedValues(selectConfiguration, value, Locale.ENGLISH);
			}
		} else if(StringHelper.containsNonWhitespace(value)) {
			if(type == PositionAttributeDefinitionTypeEnum.percentage) {
				value += " %";
			} else if(type == PositionAttributeDefinitionTypeEnum.date) {
				try {
					Date date = Formatter.parseDatetime(value);
					value = DateCellRenderer.format(date);
				} catch (ParseException e) {
					log.error("Cannot parse date: {}", value, e);
				}
			}
		}
		
		if(value.indexOf('\n') >= 0) {
			String[] splitted = value.split("[\\n]");
			for(String split:splitted) {
				additionalAttributeNode
					.appendChild(doc.createElement("multiline"))
					.appendChild(doc.createTextNode(split));
			}
		} else {
			additionalAttributeNode
				.appendChild(doc.createTextNode(value));
		}
	}
	
	private void updatePositionTitle(Document doc) {
		NodeList positionList = doc.getElementsByTagName("position");
		a_a:
		for(int i=0; i<positionList.getLength(); i++) {
			Node positionNode = positionList.item(i);
			//loop to find application
			//loop to find position
			if("position".equals(positionNode.getNodeName())) {
				String positionTitle = position.getMLTitle(translator.getLocale());
				if(!StringHelper.containsNonWhitespace(positionTitle)) {
					positionTitle = position.getPositionTitle();
				}
				if(!StringHelper.containsNonWhitespace(positionTitle)) {
					positionTitle = position.getPositionTitleDe();
				}
				
				for(Node positionTitleNode=positionNode.getFirstChild(); positionTitleNode != null; positionTitleNode=positionTitleNode.getNextSibling()) {
					if("positionTitle".equals(positionTitleNode.getNodeName())) {
						updateTextNode(positionTitleNode, positionTitle);
						break a_a;
					}
				}
				
				Node positionTitleNode = positionNode
						.appendChild(doc.createElement("positionTitle"));
				positionTitleNode
						.appendChild(doc.createTextNode(positionTitle));
			}
		}
	}
	

	private List<Tab> getEnabledTabs() {
		List<Tab> tabs = new ArrayList<>();
		tabs.add(Tab.personalData);
		if(recruitingModule.isPositionAcademicalBackgroundEnabled() && position.isApplicationAcademicalBackground()) {
			tabs.add(Tab.academicalBackground);
		}
		if(recruitingModule.isApplicationProjectEnabled() && position.isApplicationProject()) {
			tabs.add(Tab.project);
		}
		if(recruitingModule.isPositionCustomStepsEnabled()) {
			List<Tab> enabledCustomTabs = position.getCustomEnabledTabsList();
			tabs.addAll(enabledCustomTabs);
		}
		return tabs;
	}
	
	private void updateAttachments(Document doc, Element rootEl) {
		Element documentsEl = (Element)rootEl.appendChild(doc.createElement("documents"));
		for(DocumentOption docOption:recruitingModule.getDocumentOptions()) {
			if(docOption.getDoc().path(application) != null) {
				Element documentEl = (Element)documentsEl.appendChild(doc.createElement("document"));
				String docName = position.getDocumentName(docOption.getDoc(), translator.getLocale());
				if(!StringHelper.containsNonWhitespace(docName)) {
					docName = translator.translate(docOption.getDoc().i18nKey());
				}
				documentEl.setAttribute("name", docName);
			}
		}
	}
	
	private void updateTitle(Document doc) {
		for(Node applicationNode=doc.getDocumentElement().getFirstChild(); applicationNode != null; applicationNode=applicationNode.getNextSibling()) {
			//loop to find application
			//loop to find position
			if("application".equals(applicationNode.getNodeName())) {
				if(application .getPerson() != null
						&& StringHelper.containsNonWhitespace(application.getPerson().getTitle())
						&& !"-".equals(application.getPerson().getTitle())) {
					
					String title = application.getPerson().getTitle();
					String enhancedTitle;
					if("Prof.Dr.".equals(title)) {
						enhancedTitle = "Prof. Dr. ";
					} else {
						enhancedTitle = title + " ";
					}

					Node enhancedTitleNode = applicationNode
							.appendChild(doc.createElement("enhancedTitle"));
					enhancedTitleNode
							.appendChild(doc.createTextNode(enhancedTitle));
				}
			}
		}
	}
	

	
	private void updateTextNode(Node parentNode, String text) {
		for(Node node=parentNode.getFirstChild(); node != null; node=node.getNextSibling()) {
			if(node instanceof Text) {
				((Text)node).setTextContent(text);
			}
		}
	}
	
	private String getOutputDate() {
		return DateCellRenderer.format(new Date());
	}

}
