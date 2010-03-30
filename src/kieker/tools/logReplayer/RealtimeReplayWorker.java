package kieker.tools.logReplayer;

import kieker.common.record.IMonitoringRecord;
import kieker.tpan.consumer.IRecordConsumer;
import kieker.tpan.consumer.RecordConsumerExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Runnable to be scheduled via the RealtimeReplayDistributor
 *
 * @author Robert von Massow
 *
 */
public class RealtimeReplayWorker implements Runnable {

    private static final Log log = LogFactory.getLog(RealtimeReplayWorker.class);
    private final IMonitoringRecord monRec;
    private final IRecordConsumer cons;
    private final RealtimeReplayDistributor rd;

    public RealtimeReplayWorker(final IMonitoringRecord monRec, final RealtimeReplayDistributor rd, final IRecordConsumer cons) {
        this.monRec = monRec;
        this.cons = cons;
        this.rd = rd;
    }

    public void run() {
        if (this.monRec != null) {
            try {
                cons.consumeMonitoringRecord(this.monRec);
            } catch (RecordConsumerExecutionException ex) {
                // TODO: check what to do
                log.error("Caught RecordConsumerExecutionException", ex);
            }
            this.rd.decreaseActive();
        }
    }
}
