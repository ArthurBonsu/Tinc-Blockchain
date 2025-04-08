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






package org.example.app.core.pbftconsensus;
import org.example.app.core.crypto.Keypair;
import org.example.app.core.crypto.PublicKeyManager;
import org.example.app.core.p2p.Peer;
import org.example.app.core.p2p.RobustP2PManager;
import org.example.app.core.p2p.UDPManager;
import org.example.app.core.p2p.XMLPeerDiscovery;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * PBFTNetworkMain provides a runnable example of a PBFT network.
 * This class demonstrates how to initialize and run a PBFT network with multiple components.
 */
public class PBFTNetworkMain {
    private static final Logger logger = Logger.getLogger(PBFTNetworkMain.class.getName());

    public static void main(String[] args) {
        try {
            // Initialize RobustP2PManager
            RobustP2PManager p2pManager = new RobustP2PManager();
            // For demo purposes, using multicast address 224.0.0.1
            p2pManager.initializeNetwork("peers.xml", "224.0.0.1");

            // Set up initial replicas with Keypairs
            Map<Integer, Keypair> initialReplicas = new HashMap<>();
            for (int i = 0; i < 4; i++) {
                initialReplicas.put(i, Keypair.generate());
            }

            // Initialize PublicKeyManager for handling replica keypairs
            PublicKeyManager keyManager = new PublicKeyManager();
            for (Map.Entry<Integer, Keypair> entry : initialReplicas.entrySet()) {
                keyManager.storeKeypair(entry.getValue(), String.valueOf(entry.getKey()));
            }

            // Set the local node ID
            int localNodeId = 0;
            PBFTNetwork.setNodeId(localNodeId);

            // Initialize PBFT components
            PBFTMessageHandler messageHandler = new PBFTMessageHandler();
            TimeoutManager timeoutManager = new TimeoutManager();
            PrimaryElection primaryElection = new PrimaryElection(0);
            CheckpointManager checkpointManager = new CheckpointManager();

            // Initialize network and handlers
            ViewChangeHandler viewChangeHandler = new ViewChangeHandler(
                    primaryElection, null, 0, initialReplicas.size(), messageHandler);

            StateSynchronization stateSynchronization = new StateSynchronization(
                    checkpointManager, null, messageHandler);

            // Initialize main PBFT component
            PBFT pbft = new PBFT(p2pManager, localNodeId, initialReplicas.size(),
                    viewChangeHandler, stateSynchronization, keyManager);

            // Initialize the PBFT handler
            PBFTHandler pbftHandler = new PBFTHandler(pbft, p2pManager, messageHandler);

            // Create PBFTNetwork with circular references resolved
            PBFTNetwork pbftNetwork = new PBFTNetwork(p2pManager, pbftHandler, localNodeId);

            // Update network reference in components that need it
            Field viewChangeHandlerPbftNetworkField = ViewChangeHandler.class.getDeclaredField("pbftNetwork");
            viewChangeHandlerPbftNetworkField.setAccessible(true);
            viewChangeHandlerPbftNetworkField.set(viewChangeHandler, pbftNetwork);

            Field stateSyncPbftNetworkField = StateSynchronization.class.getDeclaredField("pbftNetwork");
            stateSyncPbftNetworkField.setAccessible(true);
            stateSyncPbftNetworkField.set(stateSynchronization, pbftNetwork);

            // Initialize supporting modules
            ClientRequestManager clientRequestManager = new ClientRequestManager(
                    p2pManager, 3, 5000, messageHandler);

            DynamicMembership dynamicMembership = new DynamicMembership(
                    initialReplicas, initialReplicas.size(), p2pManager, messageHandler);

            FaultDetector faultDetector = new FaultDetector(
                    5000, p2pManager, pbftNetwork, messageHandler);

            FaultRecovery faultRecovery = new FaultRecovery(
                    primaryElection, checkpointManager, timeoutManager,
                    p2pManager, pbftNetwork, messageHandler);

            PerformanceMetrics performanceMetrics = new PerformanceMetrics();

            LoadBalancer loadBalancer = new LoadBalancer(
                    new ArrayList<>(initialReplicas.keySet()), 10, performanceMetrics);

            // Start PBFT network server
            pbftNetwork.startServer(9000);
            logger.info("PBFT Network started on port 9000");

            // Connect to other replicas (adjust IP and ports as needed for testing)
            for (int i = 1; i < 4; i++) {
                pbftNetwork.connectToPeer("127.0.0." + i, 9000 + i);
            }

            // Simulate a client request
            String clientRequest = "Test Request";
            int requestId = clientRequestManager.receiveRequest(clientRequest);
            logger.info("Client request sent with ID: " + requestId);

            // Wait for the request to be processed
            TimeUnit.SECONDS.sleep(2);

            // Simulate a fault in the primary
            int failedPrimary = 0;
            logger.info("Simulating primary failure for replica: " + failedPrimary);
            faultDetector.updateReplicaTimestamp(failedPrimary); // Mark it as non-responsive

            // Detect and recover from the fault
            List<Integer> faults = faultDetector.detectFaults();
            if (!faults.isEmpty()) {
                faultRecovery.recoverFromPrimaryFailure(failedPrimary, initialReplicas.size());
            }

            // Simulate state synchronization
            stateSynchronization.synchronizeState(1);

            // Test dynamic membership
            int newNodeId = 5;
            Keypair newNodeKeypair = Keypair.generate();
            dynamicMembership.addNode(newNodeId, newNodeKeypair);

            // Simulate view change
            viewChangeHandler.initiateViewChange(failedPrimary);

            // Keep the example running for a while
            logger.info("PBFT network running. Press Ctrl+C to exit.");
            TimeUnit.SECONDS.sleep(30);

            // Shutdown components
            timeoutManager.shutdown();
            faultDetector.shutdown();
            clientRequestManager.shutdown();
            pbftNetwork.shutdown();

            logger.info("PBFT network test completed.");
        } catch (Exception e) {
            logger.severe("Error in PBFT network: " + e.getMessage());
            e.printStackTrace();
        }
    }
}



