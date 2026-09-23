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

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.ratelimit.RateLimitDecision;
import org.olat.core.util.ratelimit.RequestRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Limits the requests of a subject with two guards:
 * <ul>
 *  <li>a fixed window of {@link #WINDOW_SECONDS} seconds per key, stored in the
 *      cache "RequestRateLimiter-window" declared in infinispan-config.xml</li>
 *  <li>a counter of the parallel requests per key</li>
 * </ul>
 * Both counters are per node. On a cluster, the effective limit is the configured
 * value multiplied by the number of nodes.
 * <p>
 * The counter of the window is an {@link AtomicInteger} held by reference in a
 * local simple cache. <code>computeIfAbsent</code> followed by <code>incrementAndGet</code>
 * is atomic without a compare-and-set loop. If the cache is replicated one day, the
 * value must be immutable and the increment needs a merge operation on the
 * {@link CacheWrapper}.
 * <p>
 * The callers prefix the keys with their channel, e.g. "rest:id:123" or "rest:ip:10.0.0.1".
 *
 * Initial date: 23 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
@Service
public class RequestRateLimiterImpl implements RequestRateLimiter {

	public static final int WINDOW_SECONDS = 60;

	private CacheWrapper<String,AtomicInteger> windowCache;
	private LongSupplier clock = () -> Instant.now().getEpochSecond();
	private final ConcurrentHashMap<String,AtomicInteger> inflight = new ConcurrentHashMap<>();

	@Autowired
	public RequestRateLimiterImpl(CoordinatorManager coordinatorManager) {
		windowCache = coordinatorManager.getCoordinator().getCacher()
				.getCache(RequestRateLimiterImpl.class.getSimpleName(), "window");
	}

	/**
	 * For the unit tests.
	 *
	 * @param windowCache The cache of the windows
	 * @param clock The clock in epoch seconds
	 */
	RequestRateLimiterImpl(CacheWrapper<String,AtomicInteger> windowCache, LongSupplier clock) {
		this.windowCache = windowCache;
		this.clock = clock;
	}

	@Override
	public RateLimitDecision check(String key, int limitPerMinute) {
		long now = clock.getAsLong();
		long windowIndex = now / WINDOW_SECONDS;
		long resetEpochSeconds = (windowIndex + 1) * WINDOW_SECONDS;

		AtomicInteger counter = windowCache.computeIfAbsent(key + "#" + windowIndex, k -> new AtomicInteger(0));
		int count = counter.incrementAndGet();
		boolean allowed = count <= limitPerMinute;
		int remaining = Math.max(0, limitPerMinute - count);
		int retryAfterSeconds = allowed ? 0 : (int)Math.max(1l, resetEpochSeconds - now);
		return new RateLimitDecision(allowed, limitPerMinute, remaining, resetEpochSeconds, retryAfterSeconds);
	}

	@Override
	public boolean acquire(String key, int maxParallel) {
		boolean[] acquired = new boolean[] { false };
		inflight.compute(key, (k, current) -> {
			if(current == null) {
				if(maxParallel > 0) {
					acquired[0] = true;
					return new AtomicInteger(1);
				}
				return null;
			}
			if(current.get() < maxParallel) {
				current.incrementAndGet();
				acquired[0] = true;
			}
			return current;
		});
		return acquired[0];
	}

	@Override
	public void release(String key) {
		inflight.computeIfPresent(key, (k, current) -> current.decrementAndGet() <= 0 ? null : current);
	}

	/**
	 * @return The number of keys with requests in flight
	 */
	int inflightSize() {
		return inflight.size();
	}
}
