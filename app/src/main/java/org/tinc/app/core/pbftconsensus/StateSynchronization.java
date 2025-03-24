//package org.tinc.consensus.pbft;
//
//import java.security.MessageDigest;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
///**
// * StateSynchronization ensures replicas synchronize their states correctly after faults or view changes.
// * - Uses Merkle Trees to validate and verify state updates efficiently.
// */
//public class StateSynchronization {
//
//    private final CheckpointManager checkpointManager;   // Manages stable state checkpoints
//    private final PBFTNetwork pbftNetwork;              // Manages communication with other replicas
//    private final Map<Integer, String> localState;       // Local state storage (key-value pairs)
//
//    /**
//     * Constructor to initialize StateSynchronization.
//     *
//     * @param checkpointManager The CheckpointManager to manage stable states.
//     * @param pbftNetwork       The PBFTNetwork for communication.
//     */
//    public StateSynchronization(CheckpointManager checkpointManager, PBFTNetwork pbftNetwork) {
//        this.checkpointManager = checkpointManager;
//        this.pbftNetwork = pbftNetwork;
//        this.localState = new ConcurrentHashMap<>();
//    }
//
//    /**
//     * Synchronizes the replica's state using Merkle Trees to validate state updates.
//     *
//     * @param sequenceNumber The latest checkpoint sequence number.
//     */
//    public void synchronizeState(int sequenceNumber) {
//        System.out.println("Starting state synchronization using Merkle Trees for sequence: " + sequenceNumber);
//
//        // Generate Merkle Root for local state
//        String localMerkleRoot = generateMerkleRoot(new ArrayList<>(localState.values()));
//        System.out.println("Local Merkle Root: " + localMerkleRoot);
//
//        // Broadcast state request with Merkle Root
//        PBFTMessage stateRequest = new PBFTMessage(
//                "STATE-REQUEST", pbftNetwork.getNodeId(),
//                "Requesting state for sequence " + sequenceNumber, null
//        );
//        stateRequest.setContent(localMerkleRoot);
//        pbftNetwork.broadcastPBFTMessage(String.valueOf(stateRequest));
//
//        System.out.println("State request with Merkle Root broadcasted.");
//    }
//
//    /**
//     * Handles an incoming state update and verifies it using Merkle Trees.
//     *
//     * @param stateUpdateMessage The received state update message.
//     * @return True if the state update is valid and successfully integrated, false otherwise.
//     */
//    public boolean handleStateUpdate(PBFTMessage stateUpdateMessage) {
//        System.out.println("Handling state update message: " + stateUpdateMessage);
//
//        // Extract Merkle Root and updated state data
//        String receivedMerkleRoot = stateUpdateMessage.getDigest();
//        List<String> receivedState = Arrays.asList(stateUpdateMessage.getContent().split(","));
//
//        // Verify the integrity of the received state
//        String computedMerkleRoot = generateMerkleRoot(receivedState);
//        if (!computedMerkleRoot.equals(receivedMerkleRoot)) {
//            System.err.println("Merkle Root mismatch. State update is invalid.");
//            return false;
//        }
//
//        // Merge the received state with the local state
//        mergeState(receivedState);
//
//        // Update the checkpoint manager with the valid state
//        checkpointManager.createCheckpoint(
//                Integer.parseInt(stateUpdateMessage.getDigest()), String.join(",", receivedState)
//        );
//        System.out.println("State successfully synchronized.");
//        return true;
//    }
//
//    /**
//     * Generates the Merkle Root for a given list of data items.
//     *
//     * @param dataItems The list of data items to hash.
//     * @return The Merkle Root as a string.
//     */
//    private String generateMerkleRoot(List<String> dataItems) {
//        if (dataItems.isEmpty()) {
//            return "";
//        }
//
//        List<String> hashes = dataItems.stream()
//                .map(this::computeHash)
//                .collect(Collectors.toList());
//
//        while (hashes.size() > 1) {
//            List<String> newLevel = new ArrayList<>();
//            for (int i = 0; i < hashes.size(); i += 2) {
//                if (i + 1 < hashes.size()) {
//                    newLevel.add(computeHash(hashes.get(i) + hashes.get(i + 1)));
//                } else {
//                    newLevel.add(hashes.get(i)); // Handle odd number of nodes
//                }
//            }
//            hashes = newLevel;
//        }
//
//        return hashes.get(0);
//    }
//
//    /**
//     * Merges the received state with the local state.
//     *
//     * @param receivedState The received state as a list of strings.
//     */
//    private void mergeState(List<String> receivedState) {
//        receivedState.forEach(data -> localState.putIfAbsent(data.hashCode(), data));
//        System.out.println("Local state merged with received state.");
//    }
//
//    /**
//     * Computes a cryptographic hash for a given input using SHA-256.
//     *
//     * @param input The input string to hash.
//     * @return The hash as a string.
//     */
//    private String computeHash(String input) {
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hashBytes = digest.digest(input.getBytes());
//            StringBuilder hashBuilder = new StringBuilder();
//            for (byte b : hashBytes) {
//                hashBuilder.append(String.format("%02x", b));
//            }
//            return hashBuilder.toString();
//        } catch (Exception e) {
//            throw new RuntimeException("Error computing hash", e);
//        }
//    }
//
//    /**
//     * Retrieves the current local state as a map.
//     *
//     * @return The local state map.
//     */
//    public Map<Integer, String> getLocalState() {
//        return new HashMap<>(localState);
//    }
//}
