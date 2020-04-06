package act.ring.cncert.task.nlp;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DuplicateRemoval {

    private final static Logger LOG = LoggerFactory.getLogger(DuplicateRemoval.class);

    private static final int HTTP_SERVER_PORT = 30001;

    private HttpServer server;
    private HashMap<String, TextDuplicateHandler> handlers;

    public DuplicateRemoval() throws IOException {
        handlers = new HashMap<>();
        handlers.put("dupNews", new TextDuplicateHandler("dupNews"));
        handlers.put("dupWeiboEvents", new TextDuplicateHandler("dupWeiboEvents"));
        handlers.put("dupForeignMedia", new TextDuplicateHandler("dupForeignMedia"));
        handlers.put("dupOpinion", new TextDuplicateHandler("dupOpinion"));

        server = HttpServer.create(new InetSocketAddress(HTTP_SERVER_PORT), 16);
        for (Map.Entry<String, TextDuplicateHandler> kv : handlers.entrySet()) {
            server.createContext("/" + kv.getKey(), kv.getValue());
        }

        // dump
        server.createContext("/dump", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                JsonNode request = readJson(httpExchange);
                String name = request.get("name").asText();
                String filename = request.get("filename").asText();
                ObjectMapper objectMapper = new ObjectMapper();
                if (handlers.containsKey(name)) {
                    ObjectNode corpus = objectMapper.valueToTree(handlers.get(name).corpus.asMap());
                    FileUtils
                        .writeStringToFile(new File(filename), corpus.toString(), Charsets.UTF_8);
                }
                writeJson(httpExchange, objectMapper.createObjectNode());
            }
        });

        // loading
        server.createContext("/load", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                JsonNode request = readJson(httpExchange);
                String name = request.get("name").asText();
                String filename = request.get("filename").asText();
                ObjectMapper objectMapper = new ObjectMapper();
                if (handlers.containsKey(name)) {
                    Cache<String, String> cache = handlers.get(name).corpus;
                    JsonNode
                        corpus =
                        objectMapper.readTree(
                            FileUtils.readFileToString(new File(filename), Charsets.UTF_8));
                    Iterator<Map.Entry<String, JsonNode>> iter = corpus.fields();
                    while (iter.hasNext()) {
                        Map.Entry<String, JsonNode> value = iter.next();
                        cache.put(value.getKey(), value.getValue().asText());
                    }
                    FileUtils
                        .writeStringToFile(new File(filename), corpus.toString(), Charsets.UTF_8);
                }
                writeJson(httpExchange, objectMapper.createObjectNode());
            }
        });

        // update sim threshold
        server.createContext("/setsim", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                JsonNode request = readJson(httpExchange);
                String name = request.get("name").asText();
                double sim = request.get("sim").asDouble(0.4f);
                if (handlers.containsKey(name)) {
                    handlers.get(name).setSimThreshold(sim);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                writeJson(httpExchange, objectMapper.createObjectNode());
            }
        });

        server.setExecutor(Executors.newCachedThreadPool());
    }

    public static void main(String[] args) throws IOException {
        DuplicateRemoval worker = new DuplicateRemoval();
        worker.start();
    }

    public void start() {
        LOG.info("start duplicate removal server, port: {}", HTTP_SERVER_PORT);
        this.server.start();
    }

    private JsonNode readJson(HttpExchange httpExchange) {
        JsonNode value = null;
        try {
            value = new ObjectMapper().readTree(httpExchange.getRequestBody());
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("failed to read request");
            httpExchange.close();
        } finally {
            return value;
        }
    }

    private void writeJson(HttpExchange httpExchange, ObjectNode value) {
        byte[] result = value.toString().getBytes();
        try {
            httpExchange.sendResponseHeaders(200, result.length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(result);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to send response content.");
        } finally {
            httpExchange.close();
        }
    }

    class TextDuplicateHandler implements HttpHandler {

        private double SIM_THRESHOLD = 0.5;

        private String handlerName;
        private Cache<String, String> corpus;
        private ObjectMapper objectMapper;

        public TextDuplicateHandler(String name) {
            this.handlerName = name;
            this.corpus = CacheBuilder.newBuilder()
                .expireAfterWrite(3L, TimeUnit.DAYS)
                .build();
            this.objectMapper = new ObjectMapper();
        }

        public void setSimThreshold(double sim) {
            this.SIM_THRESHOLD = sim;
        }

        /**
         * @return String similarity based on the Levenshtein distance.
         */
        private double stringSimilarity(String dst, String src) {
            int edit = StringUtils.getLevenshteinDistance(dst, src);
            return 1.0f - (edit / (double) Math.max(dst.length(), src.length()));
        }

        /**
         * @return id of duplicate item.
         */
        public String findDup(String id, String text) {
            String targetId = "";
            double targetSim = 0.0;
            ConcurrentMap<String, String> cache = this.corpus.asMap();
            if (cache.containsKey(id)) {
                return id;
            }
            for (Map.Entry<String, String> kv : cache.entrySet()) {
                double sim = this.stringSimilarity(kv.getValue(), text);
                if (sim > SIM_THRESHOLD && !id.equals(kv.getKey()) && sim >= targetSim) {
                    targetSim = sim;
                    targetId = kv.getKey();
                }
            }
            return targetId;
        }

        @Override
        public void handle(HttpExchange httpExchange) {
            JsonNode value = readJson(httpExchange);
            String id = value.get("id").asText();
            String text = value.get("text").asText();
            String dup = this.findDup(id, text);
            if (dup.isEmpty()) {
                this.corpus.put(id, text);
            }
            ObjectNode result = objectMapper.createObjectNode();
            result.put("dup", !dup.isEmpty());
            result.put("dup_text_id", dup);
            writeJson(httpExchange, result);
        }
    }
}