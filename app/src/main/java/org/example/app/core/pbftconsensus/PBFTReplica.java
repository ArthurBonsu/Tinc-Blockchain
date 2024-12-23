//package org.tinc.consensus.pbft;
//
//import org.tinc.crypto.Keypair;
//import org.tinc.crypto.Keypair.SignatureResult;
//
//import java.util.Base64;
//import java.util.HashSet;
//import java.util.Set;
//
//public class PBFTReplica {
//
//    private final int replicaId;
//    private final Keypair keypair;
//    private final Set<String> processedMessages;
//
//    public PBFTReplica(int replicaId) {
//        this.replicaId = replicaId;
//        this.keypair = Keypair.generate();
//        this.processedMessages = new HashSet<>();
//    }
//
//    public PBFTMessage createMessage(String type, byte[] data) {
//        String digest = generateDigest(data);
//        if (isDuplicate(digest)) return null;
//
//        SignatureResult signature = signMessage(data);
//        markAsProcessed(digest);
//        return new PBFTMessage(type, replicaId, digest, signature.toString());
//    }
//
//    public boolean verifyMessage(PBFTMessage message, Keypair senderKeypair) {
//        byte[] dataToVerify = (message.getType() + message.getSenderId() + message.getDigest()).getBytes();
//        return senderKeypair.verify(message.getSignature(), dataToVerify);
//    }
//
//    public void processMessage(PBFTMessage message) {
//        if (isDuplicate(message.getDigest())) {
//            System.out.println("Duplicate message detected: " + message);
//            return;
//        }
//        markAsProcessed(message.getDigest());
//        System.out.println("Replica " + replicaId + " processed: " + message);
//    }
//
//    private String generateDigest(byte[] data) {
//        return Base64.getEncoder().encodeToString(data);
//    }
//
//    private boolean isDuplicate(String digest) {
//        return processedMessages.contains(digest);
//    }
//
//    private void markAsProcessed(String digest) {
//        processedMessages.add(digest);
//    }
//
//    public SignatureResult signMessage(byte[] data) {
//        return keypair.sign(data);
//    }
//}
