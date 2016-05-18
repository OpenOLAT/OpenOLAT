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
package org.olat.ims.qti21.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.MultipartFileInfos;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

import uk.ac.ed.ph.jqtiplus.exception.QtiParseException;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;

/**
 * 
 * Initial date: 24.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractQtiWorksController extends FormBasicController {
		
	public AbstractQtiWorksController(UserRequest ureq, WindowControl wControl, String pageName) {
		super(ureq, wControl, pageName);
	}

	@Override
	protected void doDispose() {
		//
	}

	protected String extractComment() {
        if (mainForm.getRequestParameter("qtiworks_comment_presented") == null) {
            /* No comment box given to candidate */
            return null;
        }
        String comment = mainForm.getRequestParameter("qtiworks_comment");
        return StringHelper.containsNonWhitespace(comment) ? comment : null;
    }
	
	protected abstract Identifier getResponseIdentifierFromUniqueId(String uniqueId);
	
	protected void processResponse(UserRequest ureq, FormItem source) {
		Map<Identifier, StringResponseData> stringResponseMap = extractStringResponseData();
		Map<Identifier, MultipartFileInfos> fileResponseMap;
		if(mainForm.isMultipartEnabled()) {
			fileResponseMap = extractFileResponseData();
		} else {
			fileResponseMap = Collections.emptyMap();
		}
		
		if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			String cmd = button.getCmd();
			if(cmd != null && cmd.startsWith("qtiworks_response_")) {
				String responseIdentifierString = cmd.substring("qtiworks_response_".length());
				String presentedFlag = "qtiworks_presented_".concat(responseIdentifierString);
				if(mainForm.getRequestParameterSet().contains(presentedFlag)) {
					Identifier responseIdentifier;
					try {
						responseIdentifier = getResponseIdentifierFromUniqueId(responseIdentifierString);
						//Identifier.parseString(responseIdentifierString);
					} catch (final QtiParseException e) {
						throw new RuntimeException("Bad response identifier encoded in parameter " + cmd, e);
					}
					
					String[] responseValues;
					if(button.getUserObject() instanceof EndAttemptInteraction) {
						responseValues = new String[]{ ((EndAttemptInteraction)button.getUserObject()).getTitle() };
					} else {
						responseValues = new String[]{ "submit" };
					}
			        StringResponseData stringResponseData = new StringResponseData(responseValues);
					stringResponseMap.put(responseIdentifier, stringResponseData);
				}
			}
		}
		
		String candidateComment = extractComment();
		fireResponse(ureq, source, stringResponseMap, fileResponseMap, candidateComment);
	}
	
	protected abstract void fireResponse(UserRequest ureq, FormItem source,
			Map<Identifier, StringResponseData> stringResponseMap, Map<Identifier, MultipartFileInfos> fileResponseMap,
			String comment);
		
	protected Map<Identifier, StringResponseData> extractStringResponseData() {
        final Map<Identifier, StringResponseData> responseMap = new HashMap<Identifier, StringResponseData>();

        final Set<String> parameterNames = mainForm.getRequestParameterSet();
        for (final String name : parameterNames) {
            if (name.startsWith("qtiworks_presented_")) {
                final String responseIdentifierString = name.substring("qtiworks_presented_".length());
                final Identifier responseIdentifier;
                try {
                	responseIdentifier = getResponseIdentifierFromUniqueId(responseIdentifierString);
                   // responseIdentifier = Identifier.parseString(responseIdentifierString);
                }
                catch (final QtiParseException e) {
                    //throw new BadResponseWebPayloadException("Bad response identifier encoded in parameter  " + name, e);
                	throw new RuntimeException("Bad response identifier encoded in parameter  " + name, e);
                }
                
                final String[] responseValues = mainForm.getRequestParameterValues("qtiworks_response_" + responseIdentifierString);
                final StringResponseData stringResponseData = new StringResponseData(responseValues);
                responseMap.put(responseIdentifier, stringResponseData);
            }
        }
        return responseMap;
    }
	
	protected Map<Identifier, MultipartFileInfos> extractFileResponseData() {
		Map<Identifier, MultipartFileInfos> fileResponseMap = new HashMap<Identifier, MultipartFileInfos>();

		Set<String> parameterNames = new HashSet<>(mainForm.getRequestMultipartFilesSet());
		parameterNames.addAll(mainForm.getRequestParameterSet());
		for (String name : parameterNames) {
			if (name.startsWith("qtiworks_uploadpresented_")) {
				String responseIdentifierString = name.substring("qtiworks_uploadpresented_".length());
				Identifier responseIdentifier;
				try {
					responseIdentifier = getResponseIdentifierFromUniqueId(responseIdentifierString);
					//responseIdentifier = Identifier.parseString(responseIdentifierString);
				} catch (final QtiParseException e) {
					throw new RuntimeException("Bad response identifier encoded in parameter " + name, e);
				}
				String multipartName = "qtiworks_uploadresponse_" + responseIdentifierString;
				MultipartFileInfos multipartFile = mainForm.getRequestMultipartFileInfos(multipartName);
				if (multipartFile == null) {
					throw new RuntimeException("Expected to find multipart file with name " + multipartName);
				}
				fileResponseMap.put(responseIdentifier, multipartFile);
			}
		}
		return fileResponseMap;
	}
}
