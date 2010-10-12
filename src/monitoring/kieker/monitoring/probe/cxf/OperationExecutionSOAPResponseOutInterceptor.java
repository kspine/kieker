package kieker.monitoring.probe.cxf;

import java.util.logging.Level;
import java.util.logging.Logger;

import kieker.common.record.OperationExecutionRecord;
import kieker.monitoring.core.ControlFlowRegistry;
import kieker.monitoring.core.MonitoringController;
import kieker.monitoring.core.SessionRegistry;
import kieker.monitoring.probe.IMonitoringProbe;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.SoapHeaderOutFilterInterceptor;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.interceptor.Fault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * ==================LICENCE=========================
 * Copyright 2006-2009 Kieker Project
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
 * ==================================================
 * 
 * CXF OutInterceptor to set the sessionIdentifier header for an outgoing soap message.
 *   
 * Look here how to add it to your client config: http://cwiki.apache.org/CXF20DOC/interceptors.html
 * 
 * Setting the soap header with jaxb or aegis databinding didn't work yet:
 * http://www.nabble.com/Add-%22out-of-band%22-soap-header-using-simple-frontend-td19380093.html
 */

/**
 * 
 * @author Dennis Kieselhorst, Andre van Hoorn
 */
public class OperationExecutionSOAPResponseOutInterceptor extends SoapHeaderOutFilterInterceptor implements IMonitoringProbe {

    private static final String componentName = OperationExecutionSOAPResponseOutInterceptor.class.getName();
    private static final String opName = "handleMessage(SoapMessage msg)";

    private static final Logger LOG = LogUtils.getL7dLogger(OperationExecutionSOAPResponseOutInterceptor.class);

    private static final MonitoringController ctrlInst = MonitoringController.getInstance();
    protected static final ControlFlowRegistry cfRegistry = ControlFlowRegistry.getInstance();
    protected static final SessionRegistry sessionRegistry = SessionRegistry.getInstance();
    protected static final SOAPTraceRegistry soapRegistry = SOAPTraceRegistry.getInstance();

    protected static final String vmName = OperationExecutionSOAPResponseOutInterceptor.ctrlInst.getHostName();
    
    
    @Override
	public void handleMessage(final SoapMessage msg) throws Fault {
        String sessionID;
        final long traceId = OperationExecutionSOAPResponseOutInterceptor.cfRegistry.recallThreadLocalTraceId();
        long tin = -1, tout = -1;
        boolean isEntryCall = true;
        int eoi = -1;
		final int ess = -1;
		int myEoi = -1, myEss = -1;

        if (traceId == -1) {
            /* Kieker trace Id not registered.
             * Should not happen, since this is a response message! */
            OperationExecutionSOAPResponseOutInterceptor.LOG.log(Level.WARNING,
                        "Kieker traceId not registered. " +
                        "Will unset all threadLocal variables and return.");
            this.unsetKiekerThreadLocalData(); // unset all variables
            return;
        } else { 
            /* thread-local traceId exists: eoi, ess, and sessionID should have
             * been registered before */
            eoi = OperationExecutionSOAPResponseOutInterceptor.cfRegistry.recallThreadLocalEOI();
            sessionID = OperationExecutionSOAPResponseOutInterceptor.sessionRegistry.recallThreadLocalSessionId();
            myEoi = OperationExecutionSOAPResponseOutInterceptor.soapRegistry.recallThreadLocalInRequestEOI();
            myEss = OperationExecutionSOAPResponseOutInterceptor.soapRegistry.recallThreadLocalInRequestESS();
            tin = OperationExecutionSOAPResponseOutInterceptor.soapRegistry.recallThreadLocalInRequestTin();
            tout = MonitoringController.currentTimeNanos();
            isEntryCall = OperationExecutionSOAPResponseOutInterceptor.soapRegistry.recallThreadLocalInRequestIsEntryCall();
        }

        /* The trace is leaving this node, thus we need to clean up the thread-local variables. */
        this.unsetKiekerThreadLocalData();

        /* Log this execution */
        final OperationExecutionRecord rec = new OperationExecutionRecord(OperationExecutionSOAPResponseOutInterceptor.componentName, OperationExecutionSOAPResponseOutInterceptor.opName, sessionID, traceId, tin, tout, OperationExecutionSOAPResponseOutInterceptor.vmName, myEoi, myEss);
        rec.experimentId = OperationExecutionSOAPResponseOutInterceptor.ctrlInst.getExperimentId();
        OperationExecutionSOAPResponseOutInterceptor.ctrlInst.newMonitoringRecord(rec);

        /* We don't put Kieker data into response header if request didn't
         * contain Kieker information*/
        if(isEntryCall){
            return;
        }

        final Document d = DOMUtils.createDocument();
        Element e;
        Header hdr;
        /* 1.) Add sessionId to response header */
        // There's no need to pass the session ID back.

        /* 2.) Add traceId to response header */
        // Actually, there's no need to pass the trace ID back but
        // we do this for consistency checks on the caller side.
        e = d.createElementNS(SOAPHeaderConstants.NAMESPACE_URI, SOAPHeaderConstants.TRACE_QUALIFIED_NAME);
        e.setTextContent(Long.toString(traceId));
        hdr = new Header(SOAPHeaderConstants.TRACE_IDENTIFIER_QNAME, e);
        msg.getHeaders().add(hdr);

        /* 3.) Add eoi to response header */
        e = d.createElementNS(SOAPHeaderConstants.NAMESPACE_URI, SOAPHeaderConstants.EOI_QUALIFIED_NAME);
        e.setTextContent(Integer.toString(eoi));
        hdr = new Header(SOAPHeaderConstants.EOI_IDENTIFIER_QNAME, e);
        msg.getHeaders().add(hdr);

        /* 4.) Add ess to response header */
        // There's no need to pass the ESS back.
    }

    private final void unsetKiekerThreadLocalData() {
        OperationExecutionSOAPResponseOutInterceptor.cfRegistry.unsetThreadLocalTraceId();
        OperationExecutionSOAPResponseOutInterceptor.cfRegistry.unsetThreadLocalEOI();
        OperationExecutionSOAPResponseOutInterceptor.cfRegistry.unsetThreadLocalESS();
        OperationExecutionSOAPResponseOutInterceptor.sessionRegistry.unsetThreadLocalSessionId();
        OperationExecutionSOAPResponseOutInterceptor.soapRegistry.unsetThreadLocalInRequestIsEntryCall();
        OperationExecutionSOAPResponseOutInterceptor.soapRegistry.unsetThreadLocalInRequestTin();
        OperationExecutionSOAPResponseOutInterceptor.soapRegistry.unsetThreadLocalInRequestEOI();
        OperationExecutionSOAPResponseOutInterceptor.soapRegistry.unsetThreadLocalInRequestESS();
    }
}
