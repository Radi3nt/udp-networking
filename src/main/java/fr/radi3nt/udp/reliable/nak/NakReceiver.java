package fr.radi3nt.udp.reliable.nak;

import fr.radi3nt.udp.actors.subscription.fragment.assembler.MissingFragments;
import fr.radi3nt.udp.data.streams.FragmentingPacketStream;
import fr.radi3nt.udp.headers.FrameDataHeader;
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
            for (ResendingFragment fragment : entry.getValue().fragments) {
                if (fragment.sent)
                    continue;

                FrameDataHeader header = new FrameDataHeader(entry.getKey(), fragment.termId, 0);
                PacketFrame[] frames = packetStreamMap.get(entry.getKey()).getFrames(entry.getKey(), fragment.termId);
                for (int missingFragment : fragment.missingFragments) {
                    FrameDataHeader child = header.child();
                    child.termOffset = missingFragment;
                    toResend.add(frames[missingFragment]);
                }
                toResend.addAll(Arrays.asList(frames).subList(fragment.lastReceivedOffset + 1, frames.length));
                fragment.sent = true;
            }

            for (PacketFrame packetFrame : toResend) {
                sender.addFrame(packetFrame);
            }
        }
    }

    public void receive(ByteBuffer frame) {
        long streamId = frame.getLong();
        long arrayLength = frame.getInt();

        Collection<ResendingFragment> missingFragments = new ArrayList<>();

        for (int i = 0; i < arrayLength; i++) {
            long termId = frame.getLong();
            int lastReceivedOffset = frame.getInt();
            int missingFragmentsLength = frame.getInt();
            int[] missingFragmentsOffsets = new int[missingFragmentsLength];
            for (int j = 0; j < missingFragmentsLength; j++) {
                int missingFragment = frame.getInt();
                missingFragmentsOffsets[j] = missingFragment;
            }
            missingFragments.add(new ResendingFragment(termId, missingFragmentsOffsets, lastReceivedOffset));
        }
        long minTermId = frame.getLong();
        StreamMissed streamMissed = missedStreams.computeIfAbsent(streamId, aLong -> new StreamMissed());
        streamMissed.fragments.removeAll(missingFragments);
        streamMissed.fragments.addAll(missingFragments);
        streamMissed.lastSuccessfulTerm = minTermId;
    }

    public static class StreamMissed {

        public final Collection<ResendingFragment> fragments = new HashSet<>();
        public long lastSuccessfulTerm;

    }


    public static class ResendingFragment extends MissingFragments {

        public boolean sent;

        public ResendingFragment(long termId, int[] missingFragments, int lastReceivedOffset) {
            super(termId, missingFragments, lastReceivedOffset);
        }


    }

}
