package kieker.monitoring.core;

import java.util.concurrent.atomic.AtomicLong;

import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.timer.ITimeSource;
import kieker.monitoring.writer.IMonitoringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * ==================LICENCE=========================
 * Copyright 2006-2011 Kieker Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================================
 */
/**
 * @author Andre van Hoorn, Matthias Rohr, Jan Waller, Robert von Massow
 */
public class WriterController implements IWriterController {
	private static final Log log = LogFactory.getLog(WriterController.class);

	/**
	 * Used to track the total number of monitoring records received while the
	 * controller has been enabled
	 */
	private final AtomicLong numberOfInserts = new AtomicLong(0);
	/** Monitoring Writer */
	private final IMonitoringWriter monitoringWriter;
	/** State of monitoring */
	private volatile boolean writingEnabled = false;

	private final ITimeSource timeSource;

	@Override
	public ITimeSource getTimeSource() {
		return this.timeSource;
	}

	private final IMonitoringControllerState controller;

	private boolean replayMode;

	WriterController(final ITimeSource timeSource,
			final IMonitoringWriter writer, final IMonitoringControllerState controller,
			final boolean replayMode) {
		if ((timeSource == null) || (writer == null) || (controller == null)) {
			final IllegalArgumentException illegalArgumentException =
					new IllegalArgumentException("Arguments must not be null ("
							+
							"TimeSource: " + timeSource +
							", Writer: " + writer +
							", Controller: " + controller);
			throw illegalArgumentException;
		}
		this.timeSource = timeSource;
		this.controller = controller;
		if (controller.isMonitoringTerminated()) {
			this.monitoringWriter = null;
			return;
		} else {
			this.monitoringWriter = writer;
		}
		this.replayMode = replayMode;
		// set Writer
	}

	/**
	 * Permanently terminates monitoring (e.g., due to a failure). Subsequent
	 * tries to enable monitoring via {@link #setMonitoringEnabled(boolean)}
	 * will be refused.
	 */
	@Override
	public boolean terminateMonitoring() {
		// TODO: Logger may be problematic, may already have shutdown!
		WriterController.log.info("Shutting down Writer Controller");
		if (this.monitoringWriter != null) {
			this.monitoringWriter.terminate();
			if (!this.controller.isMonitoringTerminated()) {
				return this.controller.terminateMonitoring();
			}
			return true;
		}
		return false;
	}

	@Override
	public void getState(final StringBuilder sb) {
		sb.append("Time Source: '");
		sb.append(this.timeSource.getClass().getName());
		sb.append("';\nReplay Mode: '");
		sb.append(this.replayMode);
		sb.append("';\nWriting Enabled: '");
		sb.append(this.isWritingEnabled());
		sb.append("'; Number of Inserts: '");
		sb.append(this.getNumberOfInserts());
		sb.append("'\n");
		if (this.monitoringWriter != null) {
			sb.append(this.monitoringWriter.getInfoString());
		} else {
			sb.append("No Monitoring Writer available");
		}
		sb.append(";\n");
	}

	@Override
	public final boolean newMonitoringRecord(final IMonitoringRecord record) {
		try {
			if (this.controller.isMonitoringTerminated()
					|| !this.isWritingEnabled()) { // enabled and not terminated
				return false;
			}
			if (this.isRealtimeMode()) {
				record.setLoggingTimestamp(this.getTimeSource()
						.currentTimeNanos());
			}
			this.numberOfInserts.incrementAndGet();
			final boolean successfulWriting =
					this.monitoringWriter.newMonitoringRecord(record);
			if (!successfulWriting) {
				WriterController.log
						.fatal("Error writing the monitoring data. Will terminate monitoring!");
				this.terminateMonitoring();
				return false;
			}
			return true;
		} catch (final Throwable ex) {
			WriterController.log.fatal(
					"Exception detected. Will terminate monitoring", ex);
			this.terminateMonitoring();
			return false;
		}
	}

	@Override
	public boolean isWritingEnabled() {
		return this.writingEnabled;
	}

	@Override
	public void setWritingEnabled(final boolean enableWriting) {
		this.writingEnabled = enableWriting;
		if (enableWriting && this.controller.isMonitoringTerminated()) {
			WriterController.log
					.warn("Enabled writing but monitoring is terminated");
		}
	}

	@Override
	public final IMonitoringWriter getMonitoringWriter() {
		return this.monitoringWriter;
	}

	@Override
	public final long getNumberOfInserts() {
		return this.numberOfInserts.longValue();
	}

	@Override
	public final void enableRealtimeMode() {
		this.replayMode = false;
	}

	@Override
	public final void enableReplayMode() {
		this.replayMode = true;
	}

	@Override
	public final boolean isRealtimeMode() {
		return !this.replayMode;
	}

	@Override
	public final boolean isReplayMode() {
		return this.replayMode;
	}

	@Override
	public IMonitoringControllerState getControllerConfig() {
		return this.controller;
	}
}
