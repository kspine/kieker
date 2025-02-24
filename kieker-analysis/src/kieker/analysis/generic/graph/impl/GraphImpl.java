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

import java.util.Optional;

import com.google.common.graph.MutableNetwork;

import kieker.analysis.generic.graph.IEdge;
import kieker.analysis.generic.graph.IGraph;
import kieker.analysis.generic.graph.INode;

/**
 * Graph with label.
 *
 * @author Reiner Jung
 * @since 2.0.0
 */
public class GraphImpl implements IGraph {

	private final MutableNetwork<INode, IEdge> graph;
	private String label;

	public GraphImpl(final String label, final MutableNetwork<INode, IEdge> graph) {
		this.label = label;
		this.graph = graph;
	}

	@Override
	public MutableNetwork<INode, IEdge> getGraph() {
		return this.graph;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public Optional<INode> findNode(final String id) {
		return this.graph.nodes().stream().filter(node -> id.equals(node.getId())).findFirst();
	}

	@Override
	public Optional<IEdge> findEdge(final String id) {
		return this.graph.edges().stream().filter(edge -> id.equals(edge.getId())).findFirst();
	}
}
