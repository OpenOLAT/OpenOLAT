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
package org.olat.ims.qti;

import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.junit.Test;

/**
 * 
 * Initial date: 02.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportDocxTest {
	
	@Test
	public void testConvertToDocx() throws Exception {
		String html = "<html><head><title>Import me</title></head><body><p>Hello World!</p><table><tr><td>some text</td><td>a</td><td>kjhs kjdfhj kjdfhk kjsdk hjdfhk khjsdfh kjdfkj jhdfh khjdfhk kjhdf kh après il vient quelque-chose d'important et surtout de compréhensible</td></tr><tr><td>some text</td><td>a</td><td>kjhs kjdfhj kjdfhk kjsdk hjdfhk khjsdfh kjdfkj jhdfh khjdfhk kjhdf kh</td></tr></table></body></html>";
    WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

    NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
    wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
    ndp.unmarshalDefaultNumbering();

    wordMLPackage.getMainDocumentPart().getContent().addAll(XHTMLImporter.convert(html, null, wordMLPackage));

    System.out.println(XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true));
    wordMLPackage.save(new java.io.File("/HotCoffee/html_output.docx"));
    System.out.println("done");
	}

}
