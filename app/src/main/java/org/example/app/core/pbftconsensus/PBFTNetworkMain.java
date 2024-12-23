//package org.tinc.consensus.pbft;
//
//import org.tinc.crypto.Keypair;
//import org.tinc.p2p.RobustP2PManager;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class PBFTNetworkMain {
//
//    public static void main(String[] args) {
//        try {
//            // Initialize RobustP2PManager
//            RobustP2PManager p2pManager = new RobustP2PManager();
//            p2pManager.initializeNetwork("peers.xml", "224.0.0.1");
//
//            // Set up initial replicas with mock Keypairs
//            Map<Integer, Keypair> initialReplicas = new HashMap<>();
//            for (int i = 0; i < 4; i++) {
//                initialReplicas.put(i, Keypair.generate());
//            }
//
//            // Initialize PBFT components
//            PrimaryElection primaryElection = new PrimaryElection(0);
//            CheckpointManager checkpointManager = new CheckpointManager();
//            PBFTMessageHandler messageHandler = new PBFTMessageHandler();
//            TimeoutManager timeoutManager = new TimeoutManager();
//            PBFT pbft = new PBFT(p2pManager, 0, initialReplicas.size());
//            PBFTHandler pbftHandler = new PBFTHandler(pbft, p2pManager, messageHandler);
//            PBFTNetwork pbftNetwork = new PBFTNetwork(p2pManager, pbftHandler, messageHandler);
//
//            // Initialize supporting modules
//            ClientRequestManager clientRequestManager = new ClientRequestManager(p2pManager, 3, 5000);
//            DynamicMembership dynamicMembership = new DynamicMembership(initialReplicas, initialReplicas.size(), p2pManager);
//            FaultDetector faultDetector = new FaultDetector(5000, p2pManager);
//            FaultRecovery faultRecovery = new FaultRecovery(primaryElection, checkpointManager, timeoutManager, p2pManager);
//            StateSynchronization stateSynchronization = new StateSynchronization(checkpointManager, pbftNetwork);
//            ViewChangeHandler viewChangeHandler = new ViewChangeHandler(primaryElection, pbftNetwork, 0, initialReplicas.size());
//
//            // Start PBFT network server
//            pbftNetwork.startServer(9000);
//
//            // Connect to mock peers (adjust IP and ports as needed for testing)
//            for (int i = 1; i < 4; i++) {
//                pbftNetwork.connectToPeer("127.0.0." + i, 9000 + i);
//            }
//
//            // Simulate a client request
//            String clientRequest = "Test Request";
//            int requestId = clientRequestManager.receiveRequest(clientRequest);
//
//            // Simulate a fault in the primary
//            int failedPrimary = 0;
//            System.out.println("Simulating primary failure for replica: " + failedPrimary);
//            faultDetector.updateReplicaTimestamp(failedPrimary); // Mark it as non-responsive
//            faultRecovery.recoverFromPrimaryFailure(failedPrimary, initialReplicas.size());
//
//            // Simulate state synchronization
//            stateSynchronization.synchronizeState(1);
//
//            // Test dynamic membership
//            int newNodeId = 5;
//            Keypair newNodeKeypair = Keypair.generate();
//            dynamicMembership.addNode(newNodeId, newNodeKeypair);
//
//            // Simulate view change
//            viewChangeHandler.initiateViewChange(failedPrimary);
//
//            // Shutdown the network
//            pbftNetwork.shutdown();
//            System.out.println("PBFT network test completed.");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
