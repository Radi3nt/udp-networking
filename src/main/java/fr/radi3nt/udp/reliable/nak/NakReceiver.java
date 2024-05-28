package fr.radi3nt.udp.reliable.nak;

import fr.radi3nt.udp.actors.subscription.fragment.assembler.IncompleteFragments;
import fr.radi3nt.udp.data.streams.FragmentingPacketStream;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.senders.PacketFrameSender;

import java.nio.ByteBuffer;
import java.util.*;

public class NakReceiver {

    private final Map<Long, StreamMissed> missedStreams = new HashMap<>();
    private final Map<Long, FragmentingPacketStream> packetStreamMap;
    private final PacketFrameSender sender;

    private final int activeTimeout;
    private final int inactiveTimeout;

    public NakReceiver(Map<Long, FragmentingPacketStream> packetStreamMap, PacketFrameSender sender, int activeTimeout, int inactiveTimeout) {
        this.packetStreamMap = packetStreamMap;
        this.sender = sender;
        this.activeTimeout = activeTimeout;
        this.inactiveTimeout = inactiveTimeout;
    }

    public void resend() {
        for (Map.Entry<Long, StreamMissed> entry : missedStreams.entrySet()) {
            long lastSuccessfulTerm = entry.getValue().lastSuccessfulTerm;

            packetStreamMap.get(entry.getKey()).clearHistory(lastSuccessfulTerm);

            Collection<PacketFrame> toResend = new HashSet<>();
            for (Iterator<IncompleteFragments> iterator = entry.getValue().fragments.values().iterator(); iterator.hasNext(); ) {
                IncompleteFragments fragment = iterator.next();
                if (fragment.termId<=lastSuccessfulTerm) {
                    iterator.remove();
                    continue;
                }
                if (fragment.resendingNotNeeded(activeTimeout, inactiveTimeout))
                    continue;

                PacketFrame[] frames = packetStreamMap.get(entry.getKey()).getFrames(entry.getKey(), fragment.termId);
                if (frames == null) {
                    iterator.remove();
                    System.err.println("Trying to resend already cleared term " + fragment.termId);
                    continue;
                }

                int pos = 0;

                while ((pos = fragment.receivedFragmentsBits.nextClearBit(pos)) < frames.length) {
                    toResend.add(frames[pos]);
                    pos++;
                }
                fragment.sent();
            }

            sender.addMissingFrames(toResend);
        }
    }

    public void receive(ByteBuffer frame) {
        long streamId = frame.getLong();
        int arrayLength = frame.getInt();

        Collection<IncompleteFragments> missingFragments = new ArrayList<>();

        for (int i = 0; i < arrayLength; i++) {
            long termId = frame.getLong();
            int rawMissingFragmentsLength = frame.getInt();
            int missingFragmentsLength = (rawMissingFragmentsLength& 0x3fffffff);
            boolean arraySent = missingFragmentsLength!=rawMissingFragmentsLength;

            byte[] missingFragmentsOffsets = new byte[missingFragmentsLength];
            frame.get(missingFragmentsOffsets, 0, missingFragmentsOffsets.length);

            BitSet set;
            if (arraySent) {
                ByteBuffer missingFragmentsBuffer = ByteBuffer.wrap(missingFragmentsOffsets);
                int bitSetSize = missingFragmentsBuffer.getInt();
                set = new BitSet(bitSetSize);
                set.set(0, bitSetSize);
                while (missingFragmentsBuffer.remaining()>0) {
                    set.clear(missingFragmentsBuffer.getInt());
                }
            } else {
                set = BitSet.valueOf(missingFragmentsOffsets);
            }
            missingFragments.add(new IncompleteFragments(termId, set));
        }

        long minTermId = frame.getLong();
        StreamMissed streamMissed = missedStreams.computeIfAbsent(streamId, aLong -> new StreamMissed());

        streamMissed.lastSuccessfulTerm = Math.max(streamMissed.lastSuccessfulTerm, minTermId);
        streamMissed.fragments.values().removeIf(value -> value.termId <= streamMissed.lastSuccessfulTerm);

        for (IncompleteFragments missingFragment : missingFragments) {
            if (missingFragment.termId<=streamMissed.lastSuccessfulTerm)
                continue;

            IncompleteFragments resendingFragment = streamMissed.fragments.get(missingFragment.termId);
            if (resendingFragment==null)
                streamMissed.fragments.put(missingFragment.termId, missingFragment);
            else {
                BitSet oldSet = (BitSet) resendingFragment.receivedFragmentsBits.clone();
                resendingFragment.receivedFragmentsBits.or(missingFragment.receivedFragmentsBits);
                if (!oldSet.equals(resendingFragment.receivedFragmentsBits))
                    resendingFragment.refreshed();
            }
        }



    }

    public static class StreamMissed {

        public final Map<Long, IncompleteFragments> fragments = new HashMap<>();
        public long lastSuccessfulTerm = -1;

    }

}
