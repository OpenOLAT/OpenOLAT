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
package org.olat.ims.qti21.model.xml;

import java.io.File;

import org.junit.Test;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiModelBuildingError;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlInterpretationException;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.validation.ItemValidationResult;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * Only to play with
 * 
 * Initial date: 25.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ValidationTest {
	
	@Test
	public void validateItem() {
		
		File itemFile = new File("/Users/srosse/Desktop/QTI/fxtest_Onyx_3_1_1/Task_1597435347.xml");
		
		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(itemFile.toPath());
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);

        ItemValidationResult itemResult = assessmentObjectXmlLoader.loadResolveAndValidateItem(itemFile.toURI());
        System.out.println("Has errors: " + (itemResult.getModelValidationErrors().size() > 0));

        BadResourceException e = itemResult.getResolvedAssessmentItem().getItemLookup().getBadResourceException();
        if(e instanceof QtiXmlInterpretationException) {
        	QtiXmlInterpretationException qe = (QtiXmlInterpretationException)e;
        	for(QtiModelBuildingError error :qe.getQtiModelBuildingErrors()) {
        		String localName = error.getElementLocalName();
        		String msg = error.getException().getMessage();
        		int lineNumber = error.getElementLocation().getLineNumber();
        		System.out.println(lineNumber + " :: " + localName + " :: " + msg);
        	}
        }
        //System.out.println("Actual validation result: " + ObjectDumper.dumpObject(itemResult, DumpMode.DEEP));
		
	}

}
