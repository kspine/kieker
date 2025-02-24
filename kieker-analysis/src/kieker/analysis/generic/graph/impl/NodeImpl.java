/***************************************************************************
 * Copyright 2022 Kieker Project (http://kieker-monitoring.net)
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
package kieker.analysis.generic.graph.impl;

import kieker.analysis.generic.graph.GraphFactory;
import kieker.analysis.generic.graph.IGraph;
import kieker.analysis.generic.graph.INode;

public class NodeImpl extends ElementImpl implements INode {

	private IGraph childGraph = null;

	public NodeImpl(final String id) {
		super(id);
	}

	@Override
	public IGraph getChildGraph() {
		return this.childGraph;
	}

	@Override
	public void removeChildGraph() {
		this.childGraph = null;
	}

	@Override
	public boolean hasChildGraph() {
		return this.childGraph != null;
	}

	@Override
	public void createChildGraph() {
		this.childGraph = GraphFactory.createGraph(this.getId());
	}

}
