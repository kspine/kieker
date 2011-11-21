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

package kieker.analysis.plugin.configuration;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author Andre van Hoorn
 */
public final class OutputPort extends AbstractPort implements IOutputPort {

	private final Collection<IInputPort> subscribers = new CopyOnWriteArrayList<IInputPort>();

	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param description
	 *            A human-readable string explaining what this port can
	 *            be used for. This string will probably be used later for a
	 *            GUI.
	 * 
	 * @param eventTypes
	 *            A list containing the classes which are transfered by this
	 *            port. If a component tries to use the port to send an object
	 *            which is not from a (sub)class within this list, the request
	 *            will be ignored.
	 */
	public OutputPort(final String description, final Collection<Class<?>> eventTypes) {
		/* Call the inherited constructor to delegate the initializing. */
		super(description, eventTypes);
	}

	/**
	 * This method can be used to deliver a given event of any type to the
	 * subscribers of this port. Keep in mind that not registered classes are
	 * not treated.
	 * 
	 * @param event
	 *            The event to be delivered to the subscribers.
	 * 
	 * @return true iff the event has been treated by this instance.
	 */
	public boolean deliver(final Object event) {
		/* Check whether the class of the given event is registered. */
		boolean isRegistered = false;
		if (eventTypes != null) {
			for (Class<?> c : eventTypes) {
				if (c.isInstance(event)) {
					isRegistered = true;
					break;
				}
			}
			if (!isRegistered)
				return false;
		}
		/* Seems like it's okay. Deliver it to the subscribers. */
		for (final IInputPort l : this.subscribers) {
			l.newEvent(event);
		}
		return true;
	}

	@Override
	public final void subscribe(final IInputPort subscriber) {
		this.subscribers.add(subscriber);
	}

	@Override
	public final void unsubscribe(final IInputPort subscriber) {
		this.subscribers.remove(subscriber);
	}
}
