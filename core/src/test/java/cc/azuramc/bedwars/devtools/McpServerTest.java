package cc.azuramc.bedwars.devtools;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpServerTest {

    private static final String TOKEN = "0123456789abcdef0123456789abcdef";

    private final List<String> audit = new ArrayList<>();
    private final List<McpTool> tools = new ArrayList<>();
    private boolean writesBlocked;
    private McpServer server;
    private String url;

    @BeforeEach
    void setUp() throws IOException {
        McpServer.Host host = new McpServer.Host() {
            @Override
            public void checkWriteAllowed() {
                if (writesBlocked) {
                    throw new ToolError("blocked");
                }
            }

            @Override
            public void audit(String tool, JsonObject arguments, String outcome) {
                audit.add(tool + " " + outcome);
            }
        };
        tools.add(McpTool.read("echo", "Echo").string("text", "Text", true)
                .handle(args -> new JsonPrimitive(args.string("text"))));
        tools.add(McpTool.write("touch", "Write something").handle(args -> new JsonPrimitive("done")));
        server = new McpServer(host, new McpContent(null, null), () -> tools, TOKEN, "test");
        server.start("127.0.0.1", 0);
        url = "http://127.0.0.1:" + server.getAddress().getPort() + McpServer.PATH;
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void negotiatesProtocolVersionAndReturnsSessionId() throws IOException {
        Response response = post(request(1, "initialize", "{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{}}"), "Bearer " + TOKEN);
        assertEquals(200, response.status);
        JsonObject result = response.json.getAsJsonObject().getAsJsonObject("result");
        assertEquals("2025-06-18", result.get("protocolVersion").getAsString());
        assertEquals("AzuraBedWars", result.getAsJsonObject("serverInfo").get("name").getAsString());
        assertTrue(response.sessionId != null && !response.sessionId.isEmpty());

        Response unknownVersion = post(request(2, "initialize", "{\"protocolVersion\":\"1999-01-01\"}"), "Bearer " + TOKEN);
        assertEquals("2025-11-25", unknownVersion.json.getAsJsonObject().getAsJsonObject("result").get("protocolVersion").getAsString());
    }

    @Test
    void listsAndCallsTools() throws IOException {
        JsonArray tools = call("tools/list", "{}").getAsJsonArray("tools");
        assertEquals(2, tools.size());
        assertTrue(tools.get(0).getAsJsonObject().getAsJsonObject("annotations").get("readOnlyHint").getAsBoolean());
        assertEquals("text", tools.get(0).getAsJsonObject().getAsJsonObject("inputSchema").getAsJsonArray("required").get(0).getAsString());

        JsonObject result = call("tools/call", "{\"name\":\"echo\",\"arguments\":{\"text\":\"hi\"}}");
        assertFalse(result.get("isError").getAsBoolean());
        assertEquals("hi", result.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString());

        JsonObject missing = call("tools/call", "{\"name\":\"echo\",\"arguments\":{}}");
        assertTrue(missing.get("isError").getAsBoolean());
        assertEquals("Missing argument text", missing.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString());
        assertEquals(Arrays.asList("echo ok", "echo error: Missing argument text"), audit);
    }

    @Test
    void picksUpToolsAddedAfterStart() throws IOException {
        tools.add(McpTool.external("late_tool", "Late", new JsonObject(), true, args -> new JsonPrimitive("late")));
        assertEquals(3, call("tools/list", "{}").getAsJsonArray("tools").size());
        JsonObject result = call("tools/call", "{\"name\":\"late_tool\"}");
        assertEquals("late", result.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString());
    }

    @Test
    void refusesWritesWhenTheHostBlocksThem() throws IOException {
        writesBlocked = true;
        JsonObject blocked = call("tools/call", "{\"name\":\"touch\"}");
        assertTrue(blocked.get("isError").getAsBoolean());
        assertEquals("blocked", blocked.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString());
        assertFalse(call("tools/call", "{\"name\":\"echo\",\"arguments\":{\"text\":\"read\"}}").get("isError").getAsBoolean());
    }

    @Test
    void readsGuidesAndPrompts() throws IOException {
        JsonArray resources = call("resources/list", "{}").getAsJsonArray("resources");
        String uri = resources.get(0).getAsJsonObject().get("uri").getAsString();
        String text = call("resources/read", "{\"uri\":\"" + uri + "\"}").getAsJsonArray("contents")
                .get(0).getAsJsonObject().get("text").getAsString();
        assertTrue(text.startsWith("# AzuraBedWars concepts"));
        assertEquals(2, call("resources/templates/list", "{}").getAsJsonArray("resourceTemplates").size());

        JsonObject prompt = call("prompts/get", "{\"name\":\"setup-map\",\"arguments\":{\"map\":\"islands\"}}");
        assertTrue(prompt.getAsJsonArray("messages").get(0).getAsJsonObject().getAsJsonObject("content")
                .get("text").getAsString().contains("\"islands\""));
    }

    @Test
    void rejectsWrongTokenForeignOriginAndGet() throws IOException {
        assertEquals(401, post(request(1, "ping", "{}"), "Bearer wrong").status);
        assertEquals(401, post(request(1, "ping", "{}"), null).status);
        assertEquals(403, rawStatus("https://evil.example"));
        assertEquals(200, rawStatus("http://localhost:6274"));

        HttpURLConnection get = (HttpURLConnection) new URL(url).openConnection();
        get.setRequestProperty("Authorization", "Bearer " + TOKEN);
        assertEquals(405, get.getResponseCode());
    }

    @Test
    void answersNotificationsBatchesAndErrors() throws IOException {
        assertEquals(202, post("{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}", "Bearer " + TOKEN).status);

        Response batch = post("[" + request(1, "ping", "{}") + "," + request(2, "nope", "{}") + "]", "Bearer " + TOKEN);
        JsonArray replies = batch.json.getAsJsonArray();
        assertEquals(2, replies.size());
        assertEquals(-32601, replies.get(1).getAsJsonObject().getAsJsonObject("error").get("code").getAsInt());

        assertEquals(400, post("{not json", "Bearer " + TOKEN).status);
        Response unknownTool = post(request(3, "tools/call", "{\"name\":\"missing\"}"), "Bearer " + TOKEN);
        assertEquals(-32602, unknownTool.json.getAsJsonObject().getAsJsonObject("error").get("code").getAsInt());
    }

    @Test
    void allowsOnlyLoopbackOrigins() {
        assertTrue(McpServer.isAllowedOrigin(null));
        assertTrue(McpServer.isAllowedOrigin("http://127.0.0.1:3000"));
        assertTrue(McpServer.isAllowedOrigin("http://[::1]:3000"));
        assertFalse(McpServer.isAllowedOrigin("http://localhost.evil.example"));
        assertFalse(McpServer.isAllowedOrigin("null"));
    }

    /** HttpURLConnection drops the Origin header */
    private int rawStatus(String origin) throws IOException {
        String body = request(1, "ping", "{}");
        try (Socket socket = new Socket("127.0.0.1", server.getAddress().getPort())) {
            OutputStream output = socket.getOutputStream();
            output.write(("POST " + McpServer.PATH + " HTTP/1.1\r\nHost: 127.0.0.1\r\nAuthorization: Bearer " + TOKEN
                    + "\r\nOrigin: " + origin + "\r\nContent-Type: application/json\r\nContent-Length: " + body.length()
                    + "\r\nConnection: close\r\n\r\n" + body).getBytes(StandardCharsets.UTF_8));
            output.flush();
            String statusLine = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)).readLine();
            return Integer.parseInt(statusLine.split(" ")[1]);
        }
    }

    private JsonObject call(String method, String params) throws IOException {
        Response response = post(request(7, method, params), "Bearer " + TOKEN);
        assertEquals(200, response.status);
        return response.json.getAsJsonObject().getAsJsonObject("result");
    }

    private static String request(int id, String method, String params) {
        return "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"method\":\"" + method + "\",\"params\":" + params + "}";
    }

    @SuppressWarnings("deprecation")
    private Response post(String body, String authorization) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json, text/event-stream");
        if (authorization != null) {
            connection.setRequestProperty("Authorization", authorization);
        }
        try (OutputStream output = connection.getOutputStream()) {
            output.write(body.getBytes(StandardCharsets.UTF_8));
        }
        Response response = new Response();
        response.status = connection.getResponseCode();
        response.sessionId = connection.getHeaderField("Mcp-Session-Id");
        InputStream input = response.status < 400 ? connection.getInputStream() : connection.getErrorStream();
        if (input != null) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                bytes.write(buffer, 0, read);
            }
            String text = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
            response.json = text.isEmpty() ? null : new JsonParser().parse(text);
        }
        return response;
    }

    private static final class Response {
        int status;
        String sessionId;
        JsonElement json;
    }
}
