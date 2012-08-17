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
package org.olat.core.commons.service.usercomments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.UserRatingsManager;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * Description:<br>
 * Test class for user comments package
 * 
 * <P>
 * Initial Date: 23.11.2009 <br>
 * 
 * @author gnaegi
 */
public class UserRatingsTest extends OlatTestCase {
	
	@Test
	public void should_service_present() {
		CommentAndRatingService service = CoreSpringFactory.getImpl(CommentAndRatingService.class);
		Assert.assertNotNull(service);
	}
	
	@Test
	public void testCRUDRating() {
		//init
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsUser("ucar-crud-1-" + UUID.randomUUID().toString());
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsUser("ucar-crud-2-" + UUID.randomUUID().toString());
		Identity ident3 = JunitTestHelper.createAndPersistIdentityAsUser("ucar-crud-3-" + UUID.randomUUID().toString());
		CommentAndRatingService service = CoreSpringFactory.getImpl(CommentAndRatingService.class);			
		service.init(ident1, ores, null, true, false);
		CommentAndRatingService serviceWithSubPath = CoreSpringFactory.getImpl(CommentAndRatingService.class);
		serviceWithSubPath.init(ident1, ores, "blubli", true, false);				

		// add comments
		UserRatingsManager urm = service.getUserRatingsManager();
		UserRatingsManager urm2 = serviceWithSubPath.getUserRatingsManager();
		
		assertEquals(0, urm.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(0, urm2.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(0l, urm.countRatings().longValue());
		assertEquals(0l, urm2.countRatings().longValue());
		
		UserRating r1 = urm.createRating(ident1, 2);
		Assert.assertNotNull(r1);
		UserRating r2 = urm2.createRating(ident1, 2);
		Assert.assertNotNull(r2);
		assertEquals(2, urm.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(2, urm2.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(1l, urm.countRatings().longValue());
		assertEquals(1l, urm2.countRatings().longValue());
		//
		UserRating r3 = urm.createRating(ident2, 4);
		Assert.assertNotNull(r3);
		UserRating r4 = urm2.createRating(ident2, 4);
		Assert.assertNotNull(r4);
		assertEquals(3, urm.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(3, urm2.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(2l, urm.countRatings().longValue());
		assertEquals(2l, urm2.countRatings().longValue());
		// 
		UserRating r5 = urm.createRating(ident3, 1);
		Assert.assertNotNull(r5);
		UserRating r6 = urm2.createRating(ident3, 1);
		Assert.assertNotNull(r6);
		assertEquals(2.33f, urm.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(2.33f, urm2.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(3l, urm.countRatings().longValue());
		assertEquals(3l, urm2.countRatings().longValue());
		//
		assertNotNull(urm.getRating(ident1));
		assertNotNull(urm.getRating(ident2));
		assertNotNull(urm.getRating(ident3));
		// can !!not!! create two ratings per person
		r1 = urm.createRating(ident1, 2);
		r2 = urm2.createRating(ident1, 2);
		assertEquals(2.25f, urm.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(2.25f, urm2.calculateRatingAverage().floatValue(), 0.01);
		//can create 2 ratings
		assertEquals(4l, urm.countRatings().longValue());
		assertEquals(4l, urm2.countRatings().longValue());
		
		// Delete ratings without subpath
		urm.deleteAllRatings();
		assertEquals(0f, urm.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(2.25f, urm2.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(0l, urm.countRatings().longValue());
		assertEquals(4l, urm2.countRatings().longValue());

		// Recreate and delete ignoring subpath
		r1 = urm.createRating(ident1, 2);
		r2 = urm2.createRating(ident1, 2);
		assertEquals(2f, urm.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(2.2f, urm2.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(1, urm.countRatings().longValue());
		assertEquals(5l, urm2.countRatings().longValue());
		urm.deleteAllRatingsIgnoringSubPath();
		assertEquals(0f, urm.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(0f, urm2.calculateRatingAverage().floatValue(), 0.01);
		assertEquals(0l, urm.countRatings().longValue());
		assertEquals(0l, urm2.countRatings().longValue());
	}
	
}
