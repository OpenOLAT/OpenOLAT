/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
*/
package org.olat.test.util.setup.context;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.olat.test.util.setup.SetupType;

public class SeleniumSuite extends Suite {
	
	protected SeleniumSuite(Class<?> klass, Class<?>[] suiteClasses)
			throws InitializationError {
		super(klass, suiteClasses);
	}
	

	public SeleniumSuite(Class<?> klass, List<Runner> runners)
			throws InitializationError {
		super(klass, runners);
	}

	public SeleniumSuite(Class<?> klass, RunnerBuilder builder)
			throws InitializationError {
		super(klass, builder);
	}

	public SeleniumSuite(RunnerBuilder builder, Class<?> klass,
			Class<?>[] suiteClasses) throws InitializationError {
		super(builder, klass, suiteClasses);
	}

	public SeleniumSuite(RunnerBuilder builder, Class<?>[] classes)
			throws InitializationError {
		super(builder, classes);
	}


	private static Context context;

	public void startSeleniumserver() throws Exception {
		//context.restartSeleniumServer();
	}
	
	public void stopSeleniumServer() throws Exception {
		//todo: implement this!!!
	}
	
	@BeforeClass
	public static void setupOnce() {
		context = Context.setupContext(SeleniumSuite.class.getName(), SetupType.TWO_NODE_CLUSTER);
	}
}
