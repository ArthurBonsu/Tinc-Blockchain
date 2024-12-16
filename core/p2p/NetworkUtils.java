package org.tinc.p2p;

import java.io.*;
import java.net.*;

/**
 * Utility class for network-related operations.
 */
public class NetworkUtils {

    /**
     * Creates and returns a socket bound to the specified host and port.
     *
     * @param host The host address.
     * @param port The port number.
     * @return The created ServerSocket.
     * @throws IOException If an error occurs during socket creation.
     */
    public static ServerSocket createSocket(String host, int port) throws IOException {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty.");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535.");
        }

        ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(host, port));
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
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty.");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535.");
        }

        return new Socket(host, port);
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
        return serverSocket.accept();
    }

    /**
     * Sends data over the specified socket.
     *
     * @param socket The Socket to send data through.
     * @param data   The data to send.
     * @throws IOException If an error occurs while sending data.
     */
    public static void sendData(Socket socket, byte[] data) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IllegalArgumentException("Socket must not be null or closed.");
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data cannot be null or empty.");
        }

        try (OutputStream outputStream = socket.getOutputStream()) {
            outputStream.write(data);
            outputStream.flush();
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

        try (InputStream inputStream = socket.getInputStream();
             ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteStream.write(buffer, 0, bytesRead);
                if (bytesRead < buffer.length) break; // Stop if the message is fully read
            }
            return byteStream.toByteArray();
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
            socket.close();
        }
    }
}
