/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
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

package kieker.tools.opad.record;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import kieker.common.record.AbstractMonitoringRecord;
import kieker.common.record.IMonitoringRecord;
import kieker.common.util.registry.IRegistry;

/**
 * This class contains the data that will be stored in the database after each complete analysis. Therefore, containing the value, the application name, the forecast
 * calculated from the value, the timestamp and the corresponding anomaly score.
 * 
 * @author Tom Frotscher
 * 
 * @since 1.9
 */
public class StorableDetectionResult extends AbstractMonitoringRecord implements IMonitoringRecord.Factory, IMonitoringRecord.BinaryFactory {

	public static final int SIZE = TYPE_SIZE_STRING + TYPE_SIZE_DOUBLE + TYPE_SIZE_LONG + TYPE_SIZE_DOUBLE + TYPE_SIZE_DOUBLE;
	public static final Class<?>[] TYPES = {
		String.class, // applicationName
		double.class, // value
		long.class, // timestamp
		double.class, // forecast
		double.class, // score
	};

	private static final long serialVersionUID = 7325786584057491433L;

	// Attributes
	protected final String applicationName;
	protected final double value;
	protected final long timestamp;
	protected final double forecast;
	protected final double score;

	/**
	 * Creates an instance of this class based on the parameters.
	 * 
	 * @param app
	 *            Application that is the source of the data
	 * @param val
	 *            Produced value
	 * @param timest
	 *            Timestamp
	 * @param fore
	 *            Corresponding forecast
	 * @param sc
	 *            anomaly score
	 */
	public StorableDetectionResult(final String app, final double val, final long timest, final double fore, final double sc) {
		this.applicationName = app;
		this.value = val;
		this.timestamp = timest;
		this.forecast = fore;
		this.score = sc;
	}

	public StorableDetectionResult(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferUnderflowException {
		this.applicationName = stringRegistry.get(buffer.getInt());
		this.value = buffer.getDouble();
		this.timestamp = buffer.getLong();
		this.forecast = buffer.getDouble();
		this.score = buffer.getDouble();
	}

	/**
	 * Creates an Instance of this class based on a single object array.
	 * 
	 * @param values
	 *            Object array containing the application name, value, timestamp, forecast and anomaly score.
	 */
	public StorableDetectionResult(final Object[] values) { // NOPMD (direct store of values)
		AbstractMonitoringRecord.checkArray(values, StorableDetectionResult.TYPES);

		this.applicationName = (String) values[0];
		this.value = (Double) values[1];
		this.timestamp = (Long) values[2];
		this.forecast = (Double) values[3];
		this.score = (Double) values[4];

	}

	/**
	 * {@inheritDoc}
	 */
	public Class<?>[] getValueTypes() {
		return StorableDetectionResult.TYPES.clone();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This record uses the {@link kieker.common.record.IMonitoringRecord.Factory} mechanism. Hence, this method is not implemented.
	 */
	@Deprecated
	public void initFromArray(final Object[] arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] toArray() {
		return new Object[] { this.applicationName, this.value, this.timestamp, this.forecast, this.score };
	}

	/**
	 * Returns the application name.
	 * 
	 * @return
	 *         Apllication name
	 */
	public String getApplication() {
		return this.applicationName;
	}

	/**
	 * Returns the timestamp.
	 * 
	 * @return
	 *         Timestamp
	 */
	public long getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Returns the value.
	 * 
	 * @return
	 *         Value
	 */
	public double getValue() {
		return this.value;
	}

	/**
	 * Returns the forecast.
	 * 
	 * @return
	 *         Forecast
	 */
	public double getForecast() {
		return this.forecast;
	}

	/**
	 * Returns the anomaly score.
	 * 
	 * @return
	 *         Anomaly score
	 */
	public double getScore() {
		return this.score;
	}

	public void writeBytes(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferOverflowException {
		buffer.putInt(stringRegistry.get(this.getApplication()));
		buffer.putDouble(this.getValue());
		buffer.putLong(this.timestamp);
		buffer.putDouble(this.getForecast());
		buffer.putDouble(this.getScore());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This record uses the {@link kieker.common.record.IMonitoringRecord.BinaryFactory} mechanism. Hence, this method is not implemented.
	 */
	@Deprecated
	public void initFromBytes(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferUnderflowException {
		throw new UnsupportedOperationException();
	}

	public int getSize() {
		return SIZE;
	}
}
