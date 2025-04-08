//package org.example.app.core.p2p;
//
//import java.io.*;
//import java.net.*;
//
///**
// * Utility class for network-related operations.
// */
//public class NetworkUtils {
//    private static final int BUFFER_SIZE = 8192;  // 8KB buffer
//    private static final int SOCKET_TIMEOUT = 30000; // 30 seconds
//    private static final int BACKLOG = 50; // Maximum queue length for incoming connections
//
//    /**
//     * Creates and returns a socket bound to the specified host and port.
//     *
//     * @param host The host address.
//     * @param port The port number.
//     * @return The created ServerSocket.
//     * @throws IOException If an error occurs during socket creation.
//     */
//    public static ServerSocket createSocket(String host, int port) throws IOException {
//        validateHostAndPort(host, port);
//
//        ServerSocket serverSocket = new ServerSocket();
//        serverSocket.setReuseAddress(true);
//        serverSocket.setSoTimeout(SOCKET_TIMEOUT);
//        serverSocket.bind(new InetSocketAddress(host, port), BACKLOG);
//        return serverSocket;
//    }
//
//    /**
//     * Establishes a connection to a specified host and port.
//     *
//     * @param host The host address.
//     * @param port The port number.
//     * @return The connected Socket.
//     * @throws IOException If an error occurs while connecting.
//     */
//    public static Socket connectToAddress(String host, int port) throws IOException {
//        validateHostAndPort(host, port);
//
//        Socket socket = new Socket();
//        socket.setSoTimeout(SOCKET_TIMEOUT);
//        socket.connect(new InetSocketAddress(host, port), SOCKET_TIMEOUT);
//        return socket;
//    }
//
//    /**
//     * Listens for incoming connections on the specified server socket.
//     *
//     * @param serverSocket The ServerSocket to listen on.
//     * @return The accepted client Socket.
//     * @throws IOException If an error occurs while accepting a connection.
//     */
//    public static Socket listenForConnections(ServerSocket serverSocket) throws IOException {
//        if (serverSocket == null || serverSocket.isClosed()) {
//            throw new IllegalArgumentException("ServerSocket must not be null or closed.");
//        }
//        Socket clientSocket = serverSocket.accept();
//        clientSocket.setSoTimeout(SOCKET_TIMEOUT);
//        return clientSocket;
//    }
//
//    /**
//     * Sends data over the specified socket.
//     *
//     * @param socket The Socket to send data through.
//     * @param data   The data to send.
//     * @throws IOException If an error occurs while sending data.
//     */
//    public static void sendData(Socket socket, byte[] data) throws IOException {
//        validateSocketAndData(socket, data);
//
//        try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
//            // Write the length of the data first
//            dos.writeInt(data.length);
//            // Write the actual data
//            dos.write(data);
//            dos.flush();
//        }
//    }
//
//    /**
//     * Receives data from the specified socket.
//     *
//     * @param socket The Socket to receive data from.
//     * @return The received data as a byte array.
//     * @throws IOException If an error occurs while receiving data.
//     */
//    public static byte[] receiveData(Socket socket) throws IOException {
//        if (socket == null || socket.isClosed()) {
//            throw new IllegalArgumentException("Socket must not be null or closed.");
//        }
//
//        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
//            // Read the length of the data first
//            int length = dis.readInt();
//            if (length <= 0) {
//                throw new IOException("Invalid data length received: " + length);
//            }
//
//            byte[] data = new byte[length];
//            int totalBytesRead = 0;
//            while (totalBytesRead < length) {
//                int bytesRemaining = length - totalBytesRead;
//                int bytesRead = dis.read(data, totalBytesRead, bytesRemaining);
//                if (bytesRead == -1) {
//                    throw new IOException("Connection closed prematurely");
//                }
//                totalBytesRead += bytesRead;
//            }
//            return data;
//        }
//    }
//
//    /**
//     * Closes the specified socket.
//     *
//     * @param socket The Socket to close.
//     * @throws IOException If an error occurs while closing the socket.
//     */
//    public static void closeSocket(Socket socket) throws IOException {
//        if (socket != null && !socket.isClosed()) {
//            try {
//                socket.shutdownInput();
//                socket.shutdownOutput();
//            } catch (IOException ignored) {
//                // Ignore shutdown exceptions
//            } finally {
//                socket.close();
//            }
//        }
//    }
//
//    /**
//     * Validates host and port parameters.
//     *
//     * @param host The host address to validate.
//     * @param port The port number to validate.
//     * @throws IllegalArgumentException If parameters are invalid.
//     */
//    private static void validateHostAndPort(String host, int port) {
//        if (host == null || host.trim().isEmpty()) {
//            throw new IllegalArgumentException("Host cannot be null or empty.");
//        }
//        if (port <= 0 || port > 65535) {
//            throw new IllegalArgumentException("Port must be between 1 and 65535.");
//        }
//    }
//
//    /**
//     * Validates socket and data parameters.
//     *
//     * @param socket The socket to validate.
//     * @param data The data to validate.
//     * @throws IllegalArgumentException If parameters are invalid.
//     */
//    private static void validateSocketAndData(Socket socket, byte[] data) {
//        if (socket == null || socket.isClosed()) {
//            throw new IllegalArgumentException("Socket must not be null or closed.");
//        }
//        if (data == null || data.length == 0) {
//            throw new IllegalArgumentException("Data cannot be null or empty.");
//        }
//    }
//}





package org.example.app.core.p2p;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for network-related operations.
 */
public class NetworkUtils {
    private static final Logger logger = Logger.getLogger(NetworkUtils.class.getName());

    private static final int BUFFER_SIZE = 8192;  // 8KB buffer
    private static final int SOCKET_TIMEOUT = 30000; // 30 seconds
    private static final int BACKLOG = 50; // Maximum queue length for incoming connections

    // Private constructor to prevent instantiation
    private NetworkUtils() {
        throw new AssertionError("NetworkUtils is a utility class and should not be instantiated");
    }

    /**
     * Creates and returns a socket bound to the specified host and port.
     *
     * @param host The host address.
     * @param port The port number.
     * @return The created ServerSocket.
     * @throws IOException If an error occurs during socket creation.
     * @throws IllegalArgumentException If parameters are invalid.
     */
    public static ServerSocket createSocket(String host, int port) throws IOException {
        validateHostAndPort(host, port);

        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(SOCKET_TIMEOUT);
            serverSocket.bind(new InetSocketAddress(host, port), BACKLOG);

            logger.fine("Created server socket on " + host + ":" + port);
            return serverSocket;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create server socket on " + host + ":" + port, e);
            throw e;
        }
    }

    /**
     * Establishes a connection to a specified host and port.
     *
     * @param host The host address.
     * @param port The port number.
     * @return The connected Socket.
     * @throws IOException If an error occurs while connecting.
     * @throws IllegalArgumentException If parameters are invalid.
     */
    public static Socket connectToAddress(String host, int port) throws IOException {
        validateHostAndPort(host, port);

        try {
            Socket socket = new Socket();
            socket.setSoTimeout(SOCKET_TIMEOUT);
            socket.connect(new InetSocketAddress(host, port), SOCKET_TIMEOUT);

            logger.fine("Connected to " + host + ":" + port);
            return socket;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to connect to " + host + ":" + port, e);
            throw e;
        }
    }

    /**
     * Listens for incoming connections on the specified server socket.
     *
     * @param serverSocket The ServerSocket to listen on.
     * @return The accepted client Socket.
     * @throws IOException If an error occurs while accepting a connection.
     * @throws IllegalArgumentException If serverSocket is invalid.
     */
    public static Socket listenForConnections(ServerSocket serverSocket) throws IOException {
        if (serverSocket == null || serverSocket.isClosed()) {
            throw new IllegalArgumentException("ServerSocket must not be null or closed");
        }

        try {
            Socket clientSocket = serverSocket.accept();
            clientSocket.setSoTimeout(SOCKET_TIMEOUT);

            logger.fine("Accepted connection from " + clientSocket.getInetAddress().getHostAddress() +
                    ":" + clientSocket.getPort());
            return clientSocket;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to accept connection", e);
            throw e;
        }
    }

    /**
     * Sends data over the specified socket.
     *
     * @param socket The Socket to send data through.
     * @param data   The data to send.
     * @throws IOException If an error occurs while sending data.
     * @throws IllegalArgumentException If parameters are invalid.
     */
    public static void sendData(Socket socket, byte[] data) throws IOException {
        validateSocketAndData(socket, data);

        try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            // Write the length of the data first
            dos.writeInt(data.length);
            // Write the actual data
            dos.write(data);
            dos.flush();

            logger.fine("Sent " + data.length + " bytes to " +
                    socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to send data", e);
            throw e;
        }
    }

    /**
     * Receives data from the specified socket.
     *
     * @param socket The Socket to receive data from.
     * @return The received data as a byte array.
     * @throws IOException If an error occurs while receiving data.
     * @throws IllegalArgumentException If socket is invalid.
     */
    public static byte[] receiveData(Socket socket) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IllegalArgumentException("Socket must not be null or closed");
        }

        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            // Read the length of the data first
            int length = dis.readInt();
            if (length <= 0) {
                throw new IOException("Invalid data length received: " + length);
            }

            // For security, limit maximum data size to prevent memory attacks
            if (length > 10 * 1024 * 1024) { // 10MB limit
                throw new IOException("Data length exceeds maximum allowed: " + length);
            }

            byte[] data = new byte[length];
            int totalBytesRead = 0;
            while (totalBytesRead < length) {
                int bytesRemaining = length - totalBytesRead;
                int bytesRead = dis.read(data, totalBytesRead, bytesRemaining);
                if (bytesRead == -1) {
                    throw new IOException("Connection closed prematurely");
                }
                totalBytesRead += bytesRead;
            }

            logger.fine("Received " + totalBytesRead + " bytes from " +
                    socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
            return data;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to receive data", e);
            throw e;
        }
    }

    /**
     * Closes the specified socket.
     *
     * @param socket The Socket to close.
     * @throws IOException If an error occurs while closing the socket.
     */
    public static void closeSocket(Socket socket) throws IOException {
        if (socket != null && !socket.isClosed()) {
            try {
                // Shutdown input/output first
                try {
                    socket.shutdownInput();
                } catch (IOException ignored) {
                    // Ignore this exception
                }

                try {
                    socket.shutdownOutput();
                } catch (IOException ignored) {
                    // Ignore this exception
                }

                // Close the socket
                socket.close();
                logger.fine("Closed socket to " +
                        socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing socket", e);
                throw e;
            }
        }
    }

    /**
     * Gets the local IP address of this machine.
     *
     * @return The local IP address as a string.
     * @throws SocketException If an error occurs while getting the network interface.
     */
    public static String getLocalIpAddress() throws SocketException {
        try {
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                String ip = socket.getLocalAddress().getHostAddress();
                logger.fine("Local IP address: " + ip);
                return ip;
            }
        } catch (UnknownHostException | SocketException e) {
            logger.log(Level.WARNING, "Failed to determine local IP address", e);

            // Fallback: try to get the first non-loopback address
            for (NetworkInterface networkInterface : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    for (InetAddress address : java.util.Collections.list(networkInterface.getInetAddresses())) {
                        if (address instanceof Inet4Address) {
                            logger.fine("Local IP address (fallback): " + address.getHostAddress());
                            return address.getHostAddress();
                        }
                    }
                }
            }

            // If all else fails, return localhost
            logger.warning("Using localhost as fallback IP address");
            return "127.0.0.1";
        }
    }

    /**
     * Checks if a port is available for use.
     *
     * @param port The port to check.
     * @return true if the port is available, false otherwise.
     */
    public static boolean isPortAvailable(int port) {
        if (port < 0 || port > 65535) {
            return false;
        }

        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(false);
            serverSocket.bind(new InetSocketAddress(port));
            return true;
        } catch (IOException e) {
            // Port is in use or unavailable
            return false;
        }
    }

    /**
     * Finds an available port within a specified range.
     *
     * @param startPort The starting port to check.
     * @param endPort The ending port to check.
     * @return An available port, or -1 if no ports are available.
     * @throws IllegalArgumentException If the port range is invalid.
     */
    public static int findAvailablePort(int startPort, int endPort) {
        if (startPort < 0 || startPort > 65535 || endPort < 0 || endPort > 65535 || startPort > endPort) {
            throw new IllegalArgumentException("Invalid port range: " + startPort + "-" + endPort);
        }

        for (int port = startPort; port <= endPort; port++) {
            if (isPortAvailable(port)) {
                logger.fine("Found available port: " + port);
                return port;
            }
        }

        logger.warning("No available ports found in range: " + startPort + "-" + endPort);
        return -1;
    }

    /**
     * Validates host and port parameters.
     *
     * @param host The host address to validate.
     * @param port The port number to validate.
     * @throws IllegalArgumentException If parameters are invalid.
     */
    private static void validateHostAndPort(String host, int port) {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
    }

    /**
     * Validates socket and data parameters.
     *
     * @param socket The socket to validate.
     * @param data The data to validate.
     * @throws IllegalArgumentException If parameters are invalid.
     */
    private static void validateSocketAndData(Socket socket, byte[] data) {
        if (socket == null || socket.isClosed()) {
            throw new IllegalArgumentException("Socket must not be null or closed");
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data cannot be null or empty");
        }
    }
}