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
package org.olat.core.commons.modules.glossary.morphService;

import java.io.File;
import java.util.ArrayList;

import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * Simulates a morphological Service and delivers the reply from a local XML-file.
 * It's used only for testing! This is why some return values are "hard coded".
 * 
 * <P>
 * Initial Date:  7.12.2008 <br>
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class MorphologicalServiceLocalImpl implements MorphologicalService {

	private static final String PATH_TO_MS_REPLY_XML = "/Users/rhaag/olatdata/olat3/bcroot/repository/77733218826329/_glossary_/msreply.xml";
//	private static final String PATH_TO_MS_REPLY_XML = "/Users/rhaag/workspace/olat3/webapp/WEB-INF/test/java/org/olat/core/commons/modules/glossary/msreply.xml";
	private static final String SERVICE_NAME = "Local test Service";
	private static final String SERVICE_IDENTIFIER = "ms-local-test";
	
		
	/**
	 * @see org.olat.core.commons.modules.glossary.morphService.FlexionServiceClient#getFlexions(java.lang.String, java.lang.String)
	 */
	public ArrayList<String> getFlexions(String partOfSpeech, String word) {
		XStream xstream = XStreamHelper.createXStreamInstance();
		File replyFile = new File(PATH_TO_MS_REPLY_XML);
		xstream.alias("msreply",FlexionReply.class);
		xstream.alias("wordform", String.class);
		Object msReply = XStreamHelper.readObject(xstream,replyFile);
		FlexionReply flexionReply = (FlexionReply) msReply;
		ArrayList<String> stemWithWordforms = flexionReply.getStem();
		return stemWithWordforms;
	}

	/**
	 * 
	 * @see org.olat.core.commons.modules.glossary.morphService.FlexionServiceClient#assumePartOfSpeech(java.lang.String)
	 */
	public String assumePartOfSpeech(String glossTerm) {
		return "n";
//		return "a";
//		return "an";
	}

	/**
	 * 
	 * @see org.olat.core.commons.modules.glossary.morphService.FlexionServiceClient#getFlexions(java.lang.String)
	 */
	public ArrayList<String> getFlexions(String word) {
		return getFlexions(assumePartOfSpeech(word), word);
	}

	/**
	 * 
	 * @see org.olat.core.commons.modules.glossary.morphService.FlexionServiceClient#getReplyStatus()
	 */
	public String getReplyStatus() {
		return "known";
//		return "guessed";
//		return "error";
	}
	
	/**
	 * 
	 * @see org.olat.core.commons.modules.glossary.morphService.FlexionServiceManager#getFlexionServiceDescriptor()
	 */
	public String getMorphServiceDescriptor() {
		return SERVICE_NAME;
	}
	
	/**
	 * 
	 * @see org.olat.core.commons.modules.glossary.morphService.FlexionServiceManager#getFlexionServiceIdentifier()
	 */
	public String getMorphServiceIdentifier() {
		return SERVICE_IDENTIFIER;
	}

}
