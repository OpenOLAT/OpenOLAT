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
package org.olat.core.util.ratelimit.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.ratelimit.RateLimitDecision;

/**
 * Unit test of the limiter with a map as cache and a controlled clock.
 *
 * Initial date: 23 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class RequestRateLimiterTest {

	private long now;
	private RequestRateLimiterImpl limiter;

	@Before
	public void setUp() {
		now = 1800000000l; // multiple of 60, start of a window
		limiter = new RequestRateLimiterImpl(new MapCacheWrapper<>(), () -> now);
	}

	@Test
	public void checkAllowsUpToLimit() {
		for(int i=1; i<=5; i++) {
			RateLimitDecision decision = limiter.check("rest:id:1", 5);
			Assert.assertTrue("Request " + i, decision.allowed());
			Assert.assertEquals(5, decision.limit());
			Assert.assertEquals(5 - i, decision.remaining());
			Assert.assertEquals(0, decision.retryAfterSeconds());
		}
	}

	@Test
	public void checkDeniesOverLimit() {
		for(int i=0; i<5; i++) {
			limiter.check("rest:id:2", 5);
		}
		now += 10; // 10 seconds in the window
		RateLimitDecision decision = limiter.check("rest:id:2", 5);
		Assert.assertFalse(decision.allowed());
		Assert.assertEquals(0, decision.remaining());
		Assert.assertEquals(1800000060l, decision.resetEpochSeconds());
		Assert.assertEquals(50, decision.retryAfterSeconds());
	}

	@Test
	public void checkRetryAfterAtLeastOneSecond() {
		limiter.check("rest:id:3", 1);
		now += 59; // last second of the window
		RateLimitDecision decision = limiter.check("rest:id:3", 1);
		Assert.assertFalse(decision.allowed());
		Assert.assertEquals(1, decision.retryAfterSeconds());
	}

	@Test
	public void checkResetsWithNextWindow() {
		for(int i=0; i<3; i++) {
			limiter.check("rest:id:4", 2);
		}
		Assert.assertFalse(limiter.check("rest:id:4", 2).allowed());

		now += 60;
		RateLimitDecision decision = limiter.check("rest:id:4", 2);
		Assert.assertTrue(decision.allowed());
		Assert.assertEquals(1, decision.remaining());
	}

	@Test
	public void checkIndependentKeys() {
		for(int i=0; i<3; i++) {
			limiter.check("rest:id:5", 2);
		}
		Assert.assertFalse(limiter.check("rest:id:5", 2).allowed());
		Assert.assertTrue(limiter.check("rest:id:6", 2).allowed());
		Assert.assertTrue(limiter.check("rest:ip:10.0.0.1", 2).allowed());
	}

	@Test
	public void checkFirstRejection() {
		for(int i=0; i<3; i++) {
			Assert.assertFalse(limiter.check("rest:id:12", 3).firstRejection());
		}
		Assert.assertTrue(limiter.check("rest:id:12", 3).firstRejection());
		for(int i=0; i<5; i++) {
			RateLimitDecision decision = limiter.check("rest:id:12", 3);
			Assert.assertFalse(decision.allowed());
			Assert.assertFalse(decision.firstRejection());
		}

		// again in the next window
		now += 60;
		for(int i=0; i<3; i++) {
			limiter.check("rest:id:12", 3);
		}
		Assert.assertTrue(limiter.check("rest:id:12", 3).firstRejection());
	}

	@Test
	public void checkFirstRejectionParallel() throws Exception {
		AtomicInteger firstRejections = new AtomicInteger(0);
		runInParallel(40, () -> {
			if(limiter.check("rest:id:13", 10).firstRejection()) {
				firstRejections.incrementAndGet();
			}
		});
		Assert.assertEquals(1, firstRejections.get());
	}

	@Test
	public void markParallelRejection() {
		Assert.assertTrue(limiter.markParallelRejection("rest:id:14"));
		Assert.assertFalse(limiter.markParallelRejection("rest:id:14"));
		Assert.assertFalse(limiter.markParallelRejection("rest:id:14"));
		// other key
		Assert.assertTrue(limiter.markParallelRejection("rest:id:15"));
		// the marker doesn't count as request
		Assert.assertTrue(limiter.check("rest:id:14", 1).allowed());

		now += 60;
		Assert.assertTrue(limiter.markParallelRejection("rest:id:14"));
	}

	@Test
	public void acquireAndRelease() {
		Assert.assertTrue(limiter.acquire("rest:id:7", 2));
		Assert.assertTrue(limiter.acquire("rest:id:7", 2));
		Assert.assertFalse(limiter.acquire("rest:id:7", 2));
		limiter.release("rest:id:7");
		Assert.assertTrue(limiter.acquire("rest:id:7", 2));
		// other key
		Assert.assertTrue(limiter.acquire("rest:id:8", 2));
	}

	@Test
	public void releaseRemovesKey() {
		limiter.acquire("rest:id:9", 4);
		limiter.acquire("rest:id:9", 4);
		limiter.release("rest:id:9");
		Assert.assertEquals(1, limiter.inflightSize());
		limiter.release("rest:id:9");
		Assert.assertEquals(0, limiter.inflightSize());
		// one release too much doesn't fail
		limiter.release("rest:id:9");
		Assert.assertEquals(0, limiter.inflightSize());
	}

	@Test
	public void acquireParallel() throws Exception {
		AtomicInteger granted = new AtomicInteger(0);
		runInParallel(50, () -> {
			if(limiter.acquire("rest:id:10", 4)) {
				granted.incrementAndGet();
			}
		});
		Assert.assertEquals(4, granted.get());
	}

	@Test
	public void checkParallel() throws Exception {
		AtomicInteger allowed = new AtomicInteger(0);
		runInParallel(40, () -> {
			if(limiter.check("rest:id:11", 25).allowed()) {
				allowed.incrementAndGet();
			}
		});
		Assert.assertEquals(25, allowed.get());
	}

	private void runInParallel(int threads, Runnable task) throws InterruptedException {
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(threads);
		for(int i=0; i<threads; i++) {
			new Thread(() -> {
				try {
					start.await();
					task.run();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					done.countDown();
				}
			}).start();
		}
		start.countDown();
		Assert.assertTrue(done.await(10, TimeUnit.SECONDS));
	}

	private static class MapCacheWrapper<U, V> implements CacheWrapper<U, V> {

		private final ConcurrentHashMap<U, V> map = new ConcurrentHashMap<>();

		@Override
		public boolean containsKey(U key) {
			return map.containsKey(key);
		}

		@Override
		public V get(U key) {
			return map.get(key);
		}

		@Override
		public V update(U key, V value) {
			return map.put(key, value);
		}

		@Override
		public V put(U key, V value) {
			return map.put(key, value);
		}

		@Override
		public V put(U key, V value, int lifespan, int maxIdleTime) {
			return map.put(key, value);
		}

		@Override
		public V putIfAbsent(U key, V value) {
			return map.putIfAbsent(key, value);
		}

		@Override
		public V replace(U key, V value) {
			return map.replace(key, value);
		}

		@Override
		public V computeIfAbsent(U key, Function<? super U, ? extends V> mappingFunction) {
			return map.computeIfAbsent(key, mappingFunction);
		}

		@Override
		public List<U> getKeys() {
			return new ArrayList<>(map.keySet());
		}

		@Override
		public V remove(U key) {
			return map.remove(key);
		}

		@Override
		public int size() {
			return map.size();
		}

		@Override
		public long maxCount() {
			return -1l;
		}

		@Override
		public Iterator<U> iterateKeys() {
			return map.keySet().iterator();
		}

		@Override
		public void clear() {
			map.clear();
		}

		@Override
		public void addListener(Object obj) {
			//
		}
	}
}
