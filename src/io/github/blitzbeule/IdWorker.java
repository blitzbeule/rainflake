package io.github.blitzbeule;

import java.time.Instant;

public class IdWorker {

    private final int UNUSED_BITS;
    private final int EPOCH_BITS;
    private final int NODE_ID_BITS;
    private final int SEQUENCE_BITS;

    private final int maxNodeId;
    private final int maxSequence;

    private final long epoch;
    private final int nodeId;

    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    public IdWorker(long epoch, int nodeId) {
        this.epoch = epoch;
        this.nodeId = nodeId;

        UNUSED_BITS = 1;
        EPOCH_BITS = 41;
        NODE_ID_BITS = 10;
        SEQUENCE_BITS = 12;

        maxNodeId = (int)(Math.pow(2, NODE_ID_BITS) - 1);
        maxSequence = (int)(Math.pow(2, SEQUENCE_BITS) - 1);

        if(nodeId < 0 || nodeId > maxNodeId) {
            throw new IllegalArgumentException(String.format("NodeId must be between %d and %d", 0, maxNodeId));
        }
    }

    public IdWorker(long epoch, int nodeId, int unused_bits, int epoch_bits, int node_id_bits, int sequence_bits) {
        this.epoch = epoch;
        this.nodeId = nodeId;

        UNUSED_BITS = unused_bits;
        EPOCH_BITS = epoch_bits;
        NODE_ID_BITS = node_id_bits;
        SEQUENCE_BITS = sequence_bits;

        maxNodeId = (int)(Math.pow(2, NODE_ID_BITS) - 1);
        maxSequence = (int)(Math.pow(2, SEQUENCE_BITS) - 1);

        if(nodeId < 0 || nodeId > maxNodeId) {
            throw new IllegalArgumentException(String.format("NodeId must be between %d and %d", 0, maxNodeId));
        }
    }

    public synchronized long nextId() {
        long currentTimestamp = timestamp();

        if(currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("Invalid System Clock!");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if(sequence == 0) {
                // Sequence Exhausted, wait till next millisecond.
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            // reset sequence to start with zero for the next millisecond
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        long id = currentTimestamp << (NODE_ID_BITS + SEQUENCE_BITS);
        id |= (nodeId << SEQUENCE_BITS);
        id |= sequence;
        return id;
    }

    private long timestamp() {
        return Instant.now().toEpochMilli() - this.epoch;
    }

    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = timestamp();
        }
        return currentTimestamp;
    }

}
