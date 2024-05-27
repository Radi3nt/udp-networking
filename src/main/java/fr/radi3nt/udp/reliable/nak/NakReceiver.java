package fr.radi3nt.udp.reliable.nak;

import fr.radi3nt.udp.actors.subscription.fragment.assembler.MissingFragments;
import fr.radi3nt.udp.data.streams.FragmentingPacketStream;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.senders.PacketFrameSender;

import java.nio.ByteBuffer;
import java.util.*;

public class NakReceiver {

    private final Map<Long, StreamMissed> missedStreams = new HashMap<>();
    private final Map<Long, FragmentingPacketStream> packetStreamMap;
    private final PacketFrameSender sender;

    public NakReceiver(Map<Long, FragmentingPacketStream> packetStreamMap, PacketFrameSender sender) {
        this.packetStreamMap = packetStreamMap;
        this.sender = sender;
    }

    public void resend() {
        for (Map.Entry<Long, StreamMissed> entry : missedStreams.entrySet()) {
            packetStreamMap.get(entry.getKey()).clearHistory(entry.getValue().lastSuccessfulTerm);
            Collection<PacketFrame> toResend = new HashSet<>();
            for (Iterator<ResendingFragment> iterator = entry.getValue().fragments.values().iterator(); iterator.hasNext(); ) {
                ResendingFragment fragment = iterator.next();
                if (fragment.sent)
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
                fragment.sent = true;
            }

            sender.addFrames(toResend);
        }
    }

    public void receive(ByteBuffer frame) {
        long streamId = frame.getLong();
        long arrayLength = frame.getInt();

        Collection<ResendingFragment> missingFragments = new ArrayList<>();

        for (int i = 0; i < arrayLength; i++) {
            long termId = frame.getLong();
            int missingFragmentsLength = frame.getInt();
            byte[] missingFragmentsOffsets = new byte[missingFragmentsLength];
            frame.get(missingFragmentsOffsets, 0, missingFragmentsOffsets.length);
            BitSet set = BitSet.valueOf(missingFragmentsOffsets);

            missingFragments.add(new ResendingFragment(termId, set));
        }

        long minTermId = frame.getLong();
        StreamMissed streamMissed = missedStreams.computeIfAbsent(streamId, aLong -> new StreamMissed());
        for (ResendingFragment missingFragment : missingFragments) {
            ResendingFragment resendingFragment = streamMissed.fragments.get(missingFragment.termId);
            if (resendingFragment==null)
                streamMissed.fragments.put(missingFragment.termId, missingFragment);
            else {
                resendingFragment.receivedFragmentsBits.or(missingFragment.receivedFragmentsBits);
                resendingFragment.sent = false;
            }
        }
        streamMissed.lastSuccessfulTerm = minTermId;
    }

    public static class StreamMissed {

        public final Map<Long, ResendingFragment> fragments = new HashMap<>();
        public long lastSuccessfulTerm;

    }


    public static class ResendingFragment extends MissingFragments {

        public boolean sent;

        public ResendingFragment(long termId, BitSet receivedFragmentsBits) {
            super(termId, receivedFragmentsBits);
        }
    }

}
