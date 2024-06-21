package fr.radi3nt.udp.reliable.nak;

import fr.radi3nt.udp.actors.subscription.fragment.assembler.IncompleteFragments;
import fr.radi3nt.udp.data.streams.FragmentingPacketStream;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.senders.PacketFrameSender;

import java.nio.ByteBuffer;
import java.util.*;

public class NakReceiver {

    private static final int MAX_RESEND = 100;
    private final Vector<StreamMissed> missedStreams = new Vector<>();
    private final PacketFrameSender sender;

    private final int activeTimeout;
    private final int inactiveTimeout;

    public NakReceiver(Vector<FragmentingPacketStream> packetStreamMap, PacketFrameSender sender, int activeTimeout, int inactiveTimeout) {
        this.sender = sender;
        this.activeTimeout = activeTimeout;
        this.inactiveTimeout = inactiveTimeout;

        missedStreams.setSize(packetStreamMap.size());
        for (int i = 0; i < packetStreamMap.size(); i++) {
            missedStreams.set(i, new StreamMissed(packetStreamMap.get(i)));
        }
    }

    public void resend() {
        for (int streamId = 0, size = missedStreams.size(); streamId < size; streamId++) {
            StreamMissed missedStream = missedStreams.get(streamId);
            if (missedStream==null)
                continue;

            long lastSuccessfulTerm = missedStream.lastSuccessfulTerm;
            missedStream.stream.clearHistory(lastSuccessfulTerm);

            if (missedStream.fragments.isEmpty())
                continue;

            Collection<PacketFrame> toResend = new HashSet<>();
            for (Iterator<ResendingFragment> iterator = missedStream.fragments.values().iterator(); iterator.hasNext(); ) {
                ResendingFragment fragment = iterator.next();
                if (fragment.termId<=lastSuccessfulTerm) {
                    iterator.remove();
                    continue;
                }
                if (fragment.resendingNotNeeded(activeTimeout, inactiveTimeout))
                    continue;

                PacketFrame[] frames = missedStream.stream.getFrames(streamId, fragment.termId);
                if (frames == null) {
                    iterator.remove();
                    System.err.println("Trying to resend already cleared term " + fragment.termId);
                    continue;
                }

                int pos = fragment.startSure;

                while ((pos = fragment.receivedFragmentsBits.nextClearBit(pos)) < Math.min(fragment.endsSure, frames.length)) {
                    toResend.add(frames[pos]);
                    if (toResend.size()>MAX_RESEND)
                        break;
                    pos++;
                }
                fragment.sent();
            }

            if (toResend.isEmpty())
                continue;
            sender.addMissingFrames(toResend);
        }
    }

    public void receive(ByteBuffer frame) {
        long streamId = frame.getLong();
        int arrayLength = frame.getInt();

        Collection<ResendingFragment> missingFragments = new ArrayList<>();

        for (int i = 0; i < arrayLength; i++) {
            long termId = frame.getLong();
            int rawMissingFragmentsLength = frame.getInt();

            int starts = frame.getInt();
            int ends = frame.getInt();

            byte[] missingFragmentsOffsets = new byte[(rawMissingFragmentsLength)];
            frame.get(missingFragmentsOffsets, 0, missingFragmentsOffsets.length);

            BitSet set = BitSet.valueOf(missingFragmentsOffsets);

            BitSet toApplySet = set;
            if (starts!=0) {
                toApplySet = new BitSet();
                int pos = 0;
                while ((pos = set.nextSetBit(pos)) != -1) {
                    toApplySet.set(pos + starts);
                    pos++;
                }
            }

            missingFragments.add(new ResendingFragment(termId, toApplySet, starts, ends));
        }

        long minTermId = frame.getLong();
        StreamMissed streamMissed = missedStreams.get((int) streamId);

        streamMissed.lastSuccessfulTerm = Math.max(streamMissed.lastSuccessfulTerm, minTermId);
        streamMissed.fragments.values().removeIf(value -> value.termId <= streamMissed.lastSuccessfulTerm);

        for (ResendingFragment missingFragment : missingFragments) {
            if (missingFragment.termId<=streamMissed.lastSuccessfulTerm)
                continue;

            ResendingFragment resendingFragment = streamMissed.fragments.get(missingFragment.termId);
            if (resendingFragment==null)
                streamMissed.fragments.put(missingFragment.termId, missingFragment);
            else {
                BitSet oldSet = (BitSet) resendingFragment.receivedFragmentsBits.clone();
                resendingFragment.receivedFragmentsBits.or(missingFragment.receivedFragmentsBits);
                resendingFragment.startSure = Math.min(resendingFragment.startSure, missingFragment.startSure);
                resendingFragment.endsSure = Math.max(resendingFragment.endsSure, missingFragment.endsSure);

                if (!oldSet.equals(resendingFragment.receivedFragmentsBits))
                    resendingFragment.refreshed();
            }
        }
    }

    public static class ResendingFragment extends IncompleteFragments {

        private int startSure;
        private int endsSure;


        public ResendingFragment(long termId, BitSet receivedFragmentsBits, int startSure, int endsSure) {
            super(termId, receivedFragmentsBits);
            this.startSure = startSure;
            this.endsSure = endsSure;
        }
    }

    public static class StreamMissed {

        public final Map<Long, ResendingFragment> fragments = new HashMap<>();
        public final FragmentingPacketStream stream;
        public long lastSuccessfulTerm = -1;

        public StreamMissed(FragmentingPacketStream stream) {
            this.stream = stream;
        }
    }

}
