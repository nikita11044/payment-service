import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpServer {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started at http://localhost:" + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            handleRequest(socket);
        }
    }

    private static void handleRequest(Socket socket) {
        try (
                Socket s = socket;
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8)
                );
                BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream())
        ) {
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isBlank()) return;

            String headerLine;
            while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            }

            String[] parts = requestLine.split("\\s+");
            if (parts.length < 2 || !parts[0].equals("GET")) {
                createNotFoundResponse(out);
                return;
            }

            String rawPath = parts[1].split("\\?", 2)[0];
            String decodedPath = URLDecoder.decode(rawPath, StandardCharsets.UTF_8);

            String fileName = getFilename(decodedPath);
            if (fileName.isEmpty()) fileName = "index.html";

            Path baseDir = Paths.get("static").toAbsolutePath().normalize();
            Path resolved = baseDir.resolve(fileName).normalize();

            if (!resolved.startsWith(baseDir)) {
                createNotFoundResponse(out);
                return;
            }

            String resourcePath = "static/" + fileName;

            InputStream is = HttpServer.class.getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                createNotFoundResponse(out);
                return;
            }

            byte[] body;
            try (InputStream input = is) {
                body = input.readAllBytes();
            }

            String contentType = getContentType(fileName);

            out.write(("HTTP/1.1 200 OK\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Type: " + contentType + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Length: " + body.length + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(("Connection: close\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(("\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(body);
            out.flush();

        } catch (IOException ignored) {
        }
    }

    private static String getFilename(String path) {
        if (path == null || path.equals("/")) return "";
        String p = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int idx = p.lastIndexOf('/');
        return idx >= 0 ? p.substring(idx + 1) : p;
    }

    private static String getContentType(String name) {
        String n = name.toLowerCase();
        if (n.endsWith(".html") || n.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (n.endsWith(".css")) return "text/css; charset=UTF-8";
        if (n.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".gif")) return "image/gif";
        if (n.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    private static void createNotFoundResponse(BufferedOutputStream out) throws IOException {
        byte[] body = "<h1>404 Not Found</h1>".getBytes(StandardCharsets.UTF_8);
        out.write(("HTTP/1.1 404 Not Found\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: text/html; charset=UTF-8\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Length: " + body.length + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Connection: close\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }
}
