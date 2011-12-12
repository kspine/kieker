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

package kieker.analysis.plugin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import kieker.analysis.plugin.port.AInputPort;
import kieker.analysis.plugin.port.AOutputPort;
import kieker.analysis.plugin.port.APlugin;
import kieker.analysis.reader.IMonitoringReader;
import kieker.common.configuration.Configuration;
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;

/**
 * <b>Do not</b> inherit directly from this class! Instead inherit from the class {@link AbstractAnalysisPlugin} or {@link AbstractMonitoringReader}.
 * 
 * @author Nils Christian Ehmke
 */
@APlugin(outputPorts = {})
public abstract class AbstractPlugin {

	private static final Log LOG = LogFactory.getLog(AbstractPlugin.class);

	private String name = null;

	protected final Configuration configuration;
	private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Pair<Object, Method>>> registeredMethods;
	private final HashMap<String, AOutputPort> outputPorts;
	private final HashMap<String, AInputPort> inputPorts;

	/**
	 * Each Plugin requires a constructor with a single Configuration object!
	 */
	public AbstractPlugin(final Configuration configuration) {
		try {
			// TODO: somewhat dirty hack...
			final Configuration defaultConfig = this.getDefaultConfiguration(); // NOPMD
			if (defaultConfig != null) {
				configuration.setDefaultConfiguration(defaultConfig);
			}
		} catch (final IllegalAccessException ex) {
			AbstractPlugin.LOG.error("Unable to set plugin default properties");
		}
		this.configuration = configuration;

		/* KEEP IN MIND: Although we use "this" in the following code, it points to the actual class. Not to AbstractPlugin!! */

		/* Get all output ports. */
		this.outputPorts = new HashMap<String, AOutputPort>();
		final APlugin annotation = this.getClass().getAnnotation(APlugin.class);
		for (final AOutputPort outputPort : annotation.outputPorts()) {
			this.outputPorts.put(outputPort.name(), outputPort);
		}
		/* Get all input ports. */
		this.inputPorts = new HashMap<String, AInputPort>();
		final Method allMethods[] = this.getClass().getMethods();
		for (final Method method : allMethods) {
			final AInputPort inputPort = method.getAnnotation(AInputPort.class);
			if (inputPort != null) {
				this.inputPorts.put(method.getName(), inputPort);
			}
		}

		/* Now create a linked queue for every output port of the class, to store the registered methods. */
		this.registeredMethods = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Pair<Object, Method>>>();
		for (final AOutputPort outputPort : annotation.outputPorts()) {
			this.registeredMethods.put(outputPort.name(), new ConcurrentLinkedQueue<Pair<Object, Method>>());
		}
	}

	/**
	 * This method should deliver an instance of {@code Properties} containing the default properties for this class. In other words: Every class inheriting from
	 * {@code AbstractPlugin} should implement this method to deliver an object which can be used for the constructor of this class.
	 * 
	 * @return The default properties.
	 */
	protected abstract Configuration getDefaultConfiguration();

	/**
	 * This method should deliver a {@code Configuration} object containing the current configuration of this instance. In other words: The constructor should be
	 * able to use the given object to initialize a new instance of this class with the same intern properties.
	 * 
	 * @return A complete filled configuration object.
	 */
	public abstract Configuration getCurrentConfiguration();

	/**
	 * This method delivers the current name of this plugin. The name does not have to be unique.
	 * 
	 * @return The name of the plugin.
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * This method sets the current name of this plugin. The name does not have to be unique.
	 * 
	 * @param name
	 *            The new name of the plugin.
	 */
	public final void setName(final String name) {
		this.name = name;
	}

	/**
	 * Delivers the given data to all registered input ports of the given output port.
	 * 
	 * @param outputPortName
	 *            The output port to be used to send the given data.
	 * @param data
	 *            The data to be send.
	 * @return true if and only if the given output port does exist the data is not null and if it suits the port's event types.
	 */
	protected final boolean deliver(final String outputPortName, final Object data) {
		/* First step: Check the data. */
		if (data == null) {
			return false;
		}

		/* Second step: Get the output port. */
		final AOutputPort outputPort = this.outputPorts.get(outputPortName);
		if (outputPort == null) {
			return false;
		}

		/* Third step: Check whether the data fits the event types. */
		for (final Class<?> eventType : outputPort.eventTypes()) {
			if (!eventType.isInstance(data)) {
				return false;
			}
		}

		/* Fourth step: Send everything to the registered ports. */
		final ConcurrentLinkedQueue<Pair<Object, Method>> registeredMethods = this.registeredMethods.get(outputPortName);

		final Iterator<Pair<Object, Method>> methodIterator = registeredMethods.iterator();
		while (methodIterator.hasNext()) {
			final Pair<Object, Method> methodPair = methodIterator.next();
			try {
				System.out.println(methodPair.fst + ", " + methodPair.snd);
				methodPair.snd.invoke(methodPair.fst, data);
			} catch (final Exception e) {
				AbstractPlugin.LOG.warn(String.format("OutputPort %s couldn't send data to InputPort %s\n", outputPort.name(), methodPair.snd.getName()));
			}
		}

		return true;
	}

	/**
	 * This method connects two plugins.
	 * 
	 * @param src
	 *            The source plugin.
	 * @param output
	 *            The output port of the source plugin.
	 * @param dst
	 *            The destination plugin.
	 * @param input
	 *            The input port of the destination port.
	 * @return true if and only if both given plugins are valid, the output and input ports exist and if they are compatible. Furthermore the destination plugin must
	 *         not be a reader.
	 */
	public static final boolean connect(final AbstractPlugin src, final String output, final AbstractPlugin dst, final String input) {
		/* First step: Check whether the plugins are valid. */
		if ((src == null) || (dst == null) || (dst instanceof IMonitoringReader)) {
			return false;
		}

		/* Second step: Check whether the ports exist. */
		final AOutputPort outputPort = src.outputPorts.get(output);
		final AInputPort inputPort = dst.inputPorts.get(input);
		if ((outputPort == null) || (inputPort == null)) {
			return false;
		}

		/* Third step: Make sure the ports are compatible. */
		if (inputPort.eventTypes().length != 0) {
			if (outputPort.eventTypes().length == 0) {
				// Output port can deliver everything
				if (!Arrays.asList(inputPort.eventTypes()).contains(Object.class)) {
					// But the input port cannot get everything.
					return false;
				}
			} else {
				for (final Class<?> srcEventType : outputPort.eventTypes()) {
					boolean compatible = false;
					for (final Class<?> dstEventType : inputPort.eventTypes()) {
						if (dstEventType.isAssignableFrom(srcEventType)) {
							compatible = true;
						}
					}
					if (!compatible) {
						return false;
					}
				}
			}
		}

		/* Connect the ports. */
		try {
			src.registeredMethods.get(output).add(new Pair<Object, Method>(dst, dst.getClass().getMethod(input, Object.class)));
		} catch (final Exception e) {
			AbstractPlugin.LOG.warn(String.format("Couldn't connect OutputPort %s with InputPort %s\n", output, input));
			return false;
		}

		return true;
	}
}

class Pair<T1, T2> {
	public T1 fst;
	public T2 snd;

	public Pair(final T1 fst, final T2 snd) {
		this.fst = fst;
		this.snd = snd;
	}
}
