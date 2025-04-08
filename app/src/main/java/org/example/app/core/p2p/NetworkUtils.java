package org.example.app.core.p2p;

import java.io.*;
import java.net.*;

/**
 * Utility class for network-related operations.
 */
public class NetworkUtils {
    private static final int BUFFER_SIZE = 8192;  // 8KB buffer
    private static final int SOCKET_TIMEOUT = 30000; // 30 seconds
    private static final int BACKLOG = 50; // Maximum queue length for incoming connections

    /**
     * Creates and returns a socket bound to the specified host and port.
     *
     * @param host The host address.
     * @param port The port number.
     * @return The created ServerSocket.
     * @throws IOException If an error occurs during socket creation.
     */
    public static ServerSocket createSocket(String host, int port) throws IOException {
        validateHostAndPort(host, port);

        ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.setSoTimeout(SOCKET_TIMEOUT);
        serverSocket.bind(new InetSocketAddress(host, port), BACKLOG);
        return serverSocket;
    }

    /**
     * Establishes a connection to a specified host and port.
     *
     * @param host The host address.
     * @param port The port number.
     * @return The connected Socket.
     * @throws IOException If an error occurs while connecting.
     */
    public static Socket connectToAddress(String host, int port) throws IOException {
        validateHostAndPort(host, port);

        Socket socket = new Socket();
        socket.setSoTimeout(SOCKET_TIMEOUT);
        socket.connect(new InetSocketAddress(host, port), SOCKET_TIMEOUT);
        return socket;
    }

    /**
     * Listens for incoming connections on the specified server socket.
     *
     * @param serverSocket The ServerSocket to listen on.
     * @return The accepted client Socket.
     * @throws IOException If an error occurs while accepting a connection.
     */
    public static Socket listenForConnections(ServerSocket serverSocket) throws IOException {
        if (serverSocket == null || serverSocket.isClosed()) {
            throw new IllegalArgumentException("ServerSocket must not be null or closed.");
        }
        Socket clientSocket = serverSocket.accept();
        clientSocket.setSoTimeout(SOCKET_TIMEOUT);
        return clientSocket;
    }

    /**
     * Sends data over the specified socket.
     *
     * @param socket The Socket to send data through.
     * @param data   The data to send.
     * @throws IOException If an error occurs while sending data.
     */
    public static void sendData(Socket socket, byte[] data) throws IOException {
        validateSocketAndData(socket, data);

        try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            // Write the length of the data first
            dos.writeInt(data.length);
            // Write the actual data
            dos.write(data);
            dos.flush();
        }
    }

    /**
     * Receives data from the specified socket.
     *
     * @param socket The Socket to receive data from.
     * @return The received data as a byte array.
     * @throws IOException If an error occurs while receiving data.
     */
    public static byte[] receiveData(Socket socket) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IllegalArgumentException("Socket must not be null or closed.");
        }

        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            // Read the length of the data first
            int length = dis.readInt();
            if (length <= 0) {
                throw new IOException("Invalid data length received: " + length);
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
            return data;
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
                socket.shutdownInput();
                socket.shutdownOutput();
            } catch (IOException ignored) {
                // Ignore shutdown exceptions
            } finally {
                socket.close();
            }
        }
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
            throw new IllegalArgumentException("Host cannot be null or empty.");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535.");
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
            throw new IllegalArgumentException("Socket must not be null or closed.");
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data cannot be null or empty.");
        }
    }
}