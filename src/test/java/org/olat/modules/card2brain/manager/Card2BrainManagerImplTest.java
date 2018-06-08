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
package org.olat.modules.card2brain.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.olat.ims.lti.LTIManager;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 09.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class Card2BrainManagerImplTest {
	
    @InjectMocks
    private Card2BrainManagerImpl sut;
    
    @Mock
    LTIManager ltiManagerMock;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    HttpResponse httpResponseMock;
    
    @Mock
    ObjectMapper objectMapperMock;

	@Test
    public void isSetOfFlashcardExisting() throws UnsupportedEncodingException {
    	// prepare
		when(httpResponseMock.getStatusLine().getStatusCode()).thenReturn(200);
		when(httpResponseMock.getEntity()).thenReturn(new StringEntity("content"));
    	
    	// test
		assertThat(sut.isSetOfFlashcardExisting(httpResponseMock)).isTrue();
    }
	
	@Test
    public void isSetOfFlashcardExisting_Status400() throws UnsupportedEncodingException {
		// prepare
		when(httpResponseMock.getStatusLine().getStatusCode()).thenReturn(400);
		when(httpResponseMock.getEntity()).thenReturn(new StringEntity("content"));
	
		// test
		assertThat(sut.isSetOfFlashcardExisting(httpResponseMock)).isFalse();
	}
	
	@Test
    public void isSetOfFlashcardExisting_content0() throws UnsupportedEncodingException {
		// prepare
		when(httpResponseMock.getStatusLine().getStatusCode()).thenReturn(200);
		when(httpResponseMock.getEntity()).thenReturn(new StringEntity(""));
    	
    	// test
		assertThat(sut.isSetOfFlashcardExisting(httpResponseMock)).isFalse();
    }
	
	@Test
    public void isSetOfFlashcardExisting_Exception() throws IllegalStateException, IOException {
		// prepare
		when(httpResponseMock.getStatusLine().getStatusCode()).thenReturn(200);
		when(httpResponseMock.getEntity().getContent()).thenThrow(new IllegalStateException());
    	
    	// test
		assertThat(sut.isSetOfFlashcardExisting(httpResponseMock)).isFalse();
    }
	
	@Test
	public void checkEnterpriseLogin() throws JsonParseException, JsonMappingException, IOException {
		// run
		sut.checkEnterpriseLogin("", "", "");

		// test
		verify(ltiManagerMock).sign(isNull(), anyString(), anyString(), anyString());
		verify(ltiManagerMock).post(anyMap(), anyString());
		verify(objectMapperMock).readValue((String) isNull(), eq(Card2BrainVerificationResult.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void checkEnterpriseLogin_Exception() throws JsonParseException, JsonMappingException, IOException {
		// prepare
		when(objectMapperMock.readValue(anyString(), any(Class.class)))
				.thenThrow(mock(JsonParseException.class));

		// test
		assertThat(sut.checkEnterpriseLogin("", "", "")).isNull();
	}
	
	@Test
	public void parseAliasShouldRemoveUrlStart() {
		String alias = "myAlias";
		String url = "https://card2brain.ch/box/" + alias;
		
		String parsedAlias = sut.parseAlias(url);
		
		assertThat(parsedAlias).isEqualTo(alias);
	}
	
	@Test
	public void parseAliasShouldRemoveUrlEditor() {
		String alias = "myAlias";
		String url = "https://card2brain.ch/box/" + alias + "/editor";
		
		String parsedAlias = sut.parseAlias(url);
		
		assertThat(parsedAlias).isEqualTo(alias);
	}		
	
	@Test
	public void parseAliasShouldReplaceBlanks() {
		String aliasWithBlanks = "bread and butter";
		String aliasWithoutBlanks = "bread_and_butter";
		
		String parsedAlias = sut.parseAlias(aliasWithBlanks);
		
		assertThat(parsedAlias).isEqualTo(aliasWithoutBlanks);
	}
}
