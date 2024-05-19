package fr.radi3nt.udp.actors.subscription.fragment.assembler;

import fr.radi3nt.udp.data.streams.FragmentingPacketStream;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Vector;

public class PacketTerm {

    private final Vector<ByteBuffer> fragments = new Vector<>();
    private final BitSet currentlySet = new BitSet();
    public final long termId;

    private int fragmentAmount;
    private boolean complete;

    public PacketTerm(long termId) {
        this.termId = termId;
    }

    public void add(int termOffset, ByteBuffer message) {
        boolean endBitSet = (termOffset&FragmentingPacketStream.LAST_MESSAGE_HINT)==FragmentingPacketStream.LAST_MESSAGE_HINT;
        int cleanTermOffset = termOffset& 0x3fffffff;
        if (endBitSet)
            fragmentAmount = cleanTermOffset;

        fragments.setSize(Math.max(cleanTermOffset, fragments.size()));
        fragments.set(cleanTermOffset, message);
        currentlySet.set(cleanTermOffset);

        if (fragmentAmount!=0) {
            checkCompleted();
        }

    }

    public ByteBuffer assembledMessage() {
        int messageBytesTotal = getMessageBytesTotal();

        ByteBuffer buffer = ByteBuffer.allocate(messageBytesTotal);
        for (ByteBuffer fragment : fragments) {
            buffer.put(fragment);
        }

        return buffer;
    }

    private int getMessageBytesTotal() {
        int messageBytesTotal = 0;
        for (ByteBuffer fragment : fragments) {
            messageBytesTotal+=fragment.limit();
        }
        return messageBytesTotal;
    }

    private void checkCompleted() {
        complete = fragmentAmount>0 && currentlySet.cardinality()==fragmentAmount;
    }

    public boolean isCompleted() {
        return complete;
    }

    public BitSet receivedFragments() {
        return currentlySet;
    }
}
