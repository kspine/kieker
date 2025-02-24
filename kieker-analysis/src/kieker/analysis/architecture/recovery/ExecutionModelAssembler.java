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

package kieker.analysis.architecture.recovery;

import kieker.analysis.architecture.recovery.events.OperationCallDurationEvent;
import kieker.model.analysismodel.execution.ExecutionFactory;
import kieker.model.analysismodel.execution.ExecutionModel;
import kieker.model.analysismodel.execution.Invocation;
import kieker.model.analysismodel.source.SourceModel;

/**
 * Assemble execution model based on operation call tuples.
 *
 * @author Sören Henning
 *
 * @since 1.14
 */
public class ExecutionModelAssembler extends AbstractSourceModelAssembler implements IExecutionModelAssembler {

	private final ExecutionFactory factory = ExecutionFactory.eINSTANCE;

	private final ExecutionModel executionModel;

	public ExecutionModelAssembler(final ExecutionModel executionModel, final SourceModel sourceModel, final String sourceLabel) {
		super(sourceModel, sourceLabel);
		this.executionModel = executionModel;
	}

	@Override
	public void addOperationCall(final OperationCallDurationEvent operationCall) {
		if (!this.executionModel.getAggregatedInvocations().containsKey(operationCall.getOperationCall())) {
			final Invocation invocation = this.factory.createInvocation();
			invocation.setCaller(operationCall.getOperationCall().getFirst());
			invocation.setCallee(operationCall.getOperationCall().getSecond());

			this.updateSourceModel(invocation);

			this.executionModel.getAggregatedInvocations().put(operationCall.getOperationCall(), invocation);
		}
	}

}
