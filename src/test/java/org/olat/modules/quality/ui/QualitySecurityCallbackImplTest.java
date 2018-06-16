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
package org.olat.modules.quality.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.model.QualityDataCollectionImpl;

/**
 * 
 * Initial date: 14.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualitySecurityCallbackImplTest {
	
	private QualitySecurityCallbackImpl sut = new QualitySecurityCallbackImpl();
	
	@Test
	public void shouldAllowUpdateStartOfDataCollectionIfNotStarted() {
		QualityDataCollectionLight dataCollection = createDataCollectionWithStartInFuture();
		
		boolean canUpdateStart = sut.canUpdateStart(dataCollection);
		
		assertThat(canUpdateStart).isTrue();
	}
	
	@Test
	public void shouldNotAllowUpdateStartOfDataCollectionIfStarted() {
		QualityDataCollectionLight dataCollection = createDataCollectionWithStartInPast();
		
		boolean canUpdateStart = sut.canUpdateStart(dataCollection);
		
		assertThat(canUpdateStart).isFalse();
	}
	
	@Test
	public void shouldAllowUpdateDeadlineOfDataCollectionIfDeadlineIsInFuture() {
		QualityDataCollectionLight dataCollection = createDataCollectionWithDeadlineInFuture();
		
		boolean canUpdateDeadline = sut.canUpdateDeadline(dataCollection);
		
		assertThat(canUpdateDeadline).isTrue();
	}
	
	@Test
	public void shouldNotAllowUpdateDeadlineOfDataCollectionIfDeadlineInPast() {
		QualityDataCollectionLight dataCollection = createDataCollectionWithDeadlineInPast();
		
		boolean canUpdateDeadline = sut.canUpdateDeadline(dataCollection);
		
		assertThat(canUpdateDeadline).isFalse();
	}
	
	@Test
	public void shouldAllowDataCollectionIfNotStarted() {
		QualityDataCollectionLight dataCollection = createDataCollectionWithStartInFuture();
		
		boolean canDeleteDataCollection = sut.canDeleteDataCollection(dataCollection);
		
		assertThat(canDeleteDataCollection).isTrue();
	}
	
	@Test
	public void shouldNotAllowDeleteDataCollectionIfStarted() {
		QualityDataCollectionLight dataCollection = createDataCollectionWithDeadlineInPast();
		
		boolean canUpdateDeadline = sut.canUpdateDeadline(dataCollection);
		
		assertThat(canUpdateDeadline).isFalse();
	}
	
	@Test
	public void shouldAllowRemoveParticipantsIfNotStarted() {
		QualityDataCollectionLight dataCollection = createDataCollectionWithStartInFuture();
		
		boolean canRevomeParticipation = sut.canRevomeParticipation(dataCollection);
		
		assertThat(canRevomeParticipation).isTrue();
	}
	
	@Test
	public void shouldNotAllowRemoveParticipantsIfStarted() {
		QualityDataCollectionLight dataCollection = createDataCollectionWithStartInPast();
		
		boolean canRevomeParticipation = sut.canRevomeParticipation(dataCollection);
		
		assertThat(canRevomeParticipation).isFalse();
	}
	
	private static QualityDataCollectionLight createDataCollectionWithStartInFuture() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date dateInFuture = calendar.getTime();
		
		QualityDataCollection dataCollection = new QualityDataCollectionImpl();
		dataCollection.setStart(dateInFuture);
		return dataCollection;
	}
	
	private static QualityDataCollectionLight createDataCollectionWithStartInPast() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		Date dateInPast = calendar.getTime();
		
		QualityDataCollection dataCollection = new QualityDataCollectionImpl();
		dataCollection.setStart(dateInPast);
		return dataCollection;
	}
	
	private static QualityDataCollectionLight createDataCollectionWithDeadlineInFuture() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date dateInFuture = calendar.getTime();
		
		QualityDataCollection dataCollection = new QualityDataCollectionImpl();
		dataCollection.setDeadline(dateInFuture);
		return dataCollection;
	}
	
	private static QualityDataCollectionLight createDataCollectionWithDeadlineInPast() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		Date dateInPast = calendar.getTime();
		
		QualityDataCollection dataCollection = new QualityDataCollectionImpl();
		dataCollection.setDeadline(dateInPast);
		return dataCollection;
	}

}
