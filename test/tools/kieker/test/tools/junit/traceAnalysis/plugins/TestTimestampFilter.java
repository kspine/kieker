/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.test.tools.junit.traceAnalysis.plugins;

import java.util.HashMap;

import junit.framework.Assert;
import junit.framework.TestCase;
import kieker.analysis.plugin.AbstractPlugin;
import kieker.analysis.repository.AbstractRepository;
import kieker.common.configuration.Configuration;
import kieker.test.tools.junit.traceAnalysis.util.ExecutionFactory;
import kieker.test.tools.junit.traceAnalysis.util.SimpleSinkPlugin;
import kieker.tools.traceAnalysis.plugins.executionFilter.TimestampFilter;
import kieker.tools.traceAnalysis.systemModel.Execution;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;

import org.junit.Test;

/**
 * 
 * @author Andre van Hoorn
 */
public class TestTimestampFilter extends TestCase { // NOCS

	// private static final Log log = LogFactory.getLog(TestTimestampFilter.class);

	private static final long IGNORE_EXECUTIONS_BEFORE_TIMESTAMP = 50;
	private static final long IGNORE_EXECUTIONS_AFTER_TIMESTAMP = 100;

	private final SystemModelRepository systemEntityFactory = new SystemModelRepository(new Configuration());
	private final ExecutionFactory eFactory = new ExecutionFactory(this.systemEntityFactory);

	/**
	 * Creates a {@link TimestampFilter} with the given properties
	 * using the constructor {@link TimestampFilter#TimestampFilter(kieker.common.configuration.Configuration, java.util.Map)}
	 * 
	 * @param ignoreExecutionsBeforeTimestamp
	 * @param ignoreExecutionsAfterTimestamp
	 * @return
	 */
	private static TimestampFilter createTimestampFilter(final long ignoreExecutionsBeforeTimestamp, final long ignoreExecutionsAfterTimestamp) {
		final Configuration cfg = new Configuration();
		cfg.put(TimestampFilter.CONFIG_IGNORE_EXECUTIONS_BEFORE_TIMESTAMP, Long.toString(ignoreExecutionsBeforeTimestamp));
		cfg.put(TimestampFilter.CONFIG_IGNORE_EXECUTIONS_AFTER_TIMESTAMP, Long.toString(ignoreExecutionsAfterTimestamp));
		return new TimestampFilter(cfg, new HashMap<String, AbstractRepository>());
	}

	/**
	 * Given a TimestampFilter selecting records within an interval <i>[a,b]</i>,
	 * assert that a record <i>r</i> with <i>r.tin &lt; a</i> and <i>r.tout
	 * &gt; a </i>, <i>r.tout &lt; b</i> does not pass the filter.
	 */
	@Test
	public void testRecordTinBeforeToutWithinIgnored() {
		final TimestampFilter filter = TestTimestampFilter.createTimestampFilter(TestTimestampFilter.IGNORE_EXECUTIONS_BEFORE_TIMESTAMP,
				TestTimestampFilter.IGNORE_EXECUTIONS_AFTER_TIMESTAMP);
		final SimpleSinkPlugin sinkPlugin = new SimpleSinkPlugin();
		final Execution exec = this.eFactory.genExecution(77, // traceId (value not important)
				TestTimestampFilter.IGNORE_EXECUTIONS_BEFORE_TIMESTAMP - 1, // tin
				TestTimestampFilter.IGNORE_EXECUTIONS_AFTER_TIMESTAMP - 1, // tout
				0, 0); // eoi, ess

		Assert.assertTrue(sinkPlugin.getList().isEmpty());
		AbstractPlugin.connect(filter, TimestampFilter.OUTPUT_PORT_NAME, sinkPlugin, SimpleSinkPlugin.INPUT_PORT_NAME);
		filter.newExecution(exec);
		Assert.assertTrue("Filter passed execution " + exec + " although tin timestamp before" + TestTimestampFilter.IGNORE_EXECUTIONS_BEFORE_TIMESTAMP
				, sinkPlugin.getList().isEmpty());

	}

	/**
	 * Given a TimestampFilter selecting records within an interval <i>[a,b]</i>,
	 * assert that a record <i>r</i> with <i>r.tin &gt; a</i>, <i>r.tin
	 * &lt; b</i> and <i>r.tout &gt; b </i> does not pass the filter.
	 */
	@Test
	public void testRecordTinWithinToutAfterIgnored() {
		final TimestampFilter filter = TestTimestampFilter.createTimestampFilter(TestTimestampFilter.IGNORE_EXECUTIONS_BEFORE_TIMESTAMP,
				TestTimestampFilter.IGNORE_EXECUTIONS_AFTER_TIMESTAMP);
		final SimpleSinkPlugin sinkPlugin = new SimpleSinkPlugin();
		final Execution exec = this.eFactory.genExecution(15, // traceId (value not important)
				TestTimestampFilter.IGNORE_EXECUTIONS_BEFORE_TIMESTAMP + 1, // tin
				TestTimestampFilter.IGNORE_EXECUTIONS_AFTER_TIMESTAMP + 1, // tout
				0, 0); // eoi, ess

		Assert.assertTrue(sinkPlugin.getList().isEmpty());
		AbstractPlugin.connect(filter, TimestampFilter.OUTPUT_PORT_NAME, sinkPlugin, SimpleSinkPlugin.INPUT_PORT_NAME);

		filter.newExecution(exec);
		Assert.assertTrue("Filter passed execution " + exec + " although tin timestamp before" + TestTimestampFilter.IGNORE_EXECUTIONS_BEFORE_TIMESTAMP
				, sinkPlugin.getList().isEmpty());
	}

	/**
	 * Given a TimestampFilter selecting records within an interval <i>[a,b]</i>,
	 * assert that a record <i>r</i> with <i>r.tin == a</i> and <i>r.tout == b </i>
	 * does pass the filter.
	 */
	@Test
	public void testRecordTinToutOnBordersPassed() {
		final TimestampFilter filter = TestTimestampFilter.createTimestampFilter(TestTimestampFilter.IGNORE_EXECUTIONS_BEFORE_TIMESTAMP,
				TestTimestampFilter.IGNORE_EXECUTIONS_AFTER_TIMESTAMP);
		final SimpleSinkPlugin sinkPlugin = new SimpleSinkPlugin();
		final Execution exec = this.eFactory.genExecution(159, // traceId (value not important)
				TestTimestampFilter.IGNORE_EXECUTIONS_BEFORE_TIMESTAMP, // tin
				TestTimestampFilter.IGNORE_EXECUTIONS_AFTER_TIMESTAMP, // tout
				0, 0); // eoi, ess

		Assert.assertTrue(sinkPlugin.getList().isEmpty());
		AbstractPlugin.connect(filter, TimestampFilter.OUTPUT_PORT_NAME, sinkPlugin, SimpleSinkPlugin.INPUT_PORT_NAME);
		filter.newExecution(exec);

		Assert.assertFalse("Filter didn't pass execution " + exec + " although timestamps within range [" + TestTimestampFilter.IGNORE_EXECUTIONS_BEFORE_TIMESTAMP
				+ "," + TestTimestampFilter.IGNORE_EXECUTIONS_AFTER_TIMESTAMP + "]", sinkPlugin.getList().isEmpty());

		Assert.assertTrue(sinkPlugin.getList().size() == 1);
		Assert.assertSame(sinkPlugin.getList().get(0), exec);
	}

	/**
	 * Given a TimestampFilter selecting records within an interval <i>[a,b]</i>,
	 * assert that a record <i>r</i> with <i>r.tin &gt; a</i>, <i>r.tin &lt; b</i>
	 * and <i>r.tout &lt; b </i>, <i>r.tout &gt; a </i> does pass the filter.
	 */
	@Test
	public void testRecordTinToutWithinRangePassed() {
		final TimestampFilter filter = TestTimestampFilter.createTimestampFilter(TestTimestampFilter.IGNORE_EXECUTIONS_BEFORE_TIMESTAMP,
				TestTimestampFilter.IGNORE_EXECUTIONS_AFTER_TIMESTAMP);
		final SimpleSinkPlugin sinkPlugin = new SimpleSinkPlugin();
		final Execution exec = this.eFactory.genExecution(159, // traceId (value not important)
				TestTimestampFilter.IGNORE_EXECUTIONS_BEFORE_TIMESTAMP + 1, // tin
				TestTimestampFilter.IGNORE_EXECUTIONS_AFTER_TIMESTAMP - 1, // tout
				0, 0); // eoi, ess

		Assert.assertTrue(sinkPlugin.getList().isEmpty());
		AbstractPlugin.connect(filter, TimestampFilter.OUTPUT_PORT_NAME, sinkPlugin, SimpleSinkPlugin.INPUT_PORT_NAME);
		filter.newExecution(exec);
		Assert.assertFalse("Filter didn't pass execution " + exec + " although timestamps within range [" + TestTimestampFilter.IGNORE_EXECUTIONS_BEFORE_TIMESTAMP
				+ "," + TestTimestampFilter.IGNORE_EXECUTIONS_AFTER_TIMESTAMP + "]", sinkPlugin.getList().isEmpty());

		Assert.assertTrue(sinkPlugin.getList().size() == 1);
		Assert.assertSame(sinkPlugin.getList().get(0), exec);
	}
}
