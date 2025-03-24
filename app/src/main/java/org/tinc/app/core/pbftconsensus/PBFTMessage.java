//package org.tinc.consensus.pbft;
//
//import org.tinc.crypto.Keypair;
//
//public class PBFTMessage {
//    public static final int MESSAGE_TYPE_ID = 1001; // Unique ID for PBFT messages
//
//    private final String type;
//    private final int senderId;
//    private final String digest;
//    private final Keypair.SignatureResult signature;
//    private String content; // Added field for message content
//
//    public PBFTMessage(String type, int senderId, String digest, String signature) {
//        this.type = type;
//        this.senderId = senderId;
//        this.digest = digest;
//        this.signature = signature;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public int getSenderId() {
//        return senderId;
//    }
//
//    public String getDigest() {
//        return digest;
//    }
//
//    public Keypair.SignatureResult getSignature() {
//        return signature;
//    }
//
//    public String getContent() {
//        return content;
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//    }
//
//    @Override
//    public String toString() {
//        return "PBFTMessage{" +
//                "type='" + type + '\'' +
//                ", senderId=" + senderId +
//                ", digest='" + digest + '\'' +
//                ", content='" + content + '\'' +
//                ", signature=" + signature +
//                '}';
//    }
//
//    public String serialize() {
//    }
//}
