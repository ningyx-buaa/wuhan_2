package act.ring.cncert.data.es;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import act.ring.cncert.data.bean.Element;

public class Logstash<T extends Element> {

    private ObjectMapper objectMapper;

    private GenericUrl endpoint;
    private HttpTransport transport;
    private HttpRequestFactory requestFactory;

    public Logstash(String endpoint) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        this.endpoint = new GenericUrl(endpoint);
        this.transport = new NetHttpTransport();
        this.requestFactory = transport.createRequestFactory();
    }

    /**
     * @return Http status code.
     */
    public int store(T value) throws IOException {
        ObjectNode valueNode = objectMapper.valueToTree(value);
        valueNode
            .putObject("message");  // patch an empty "message" field for logstash's json codec.
        ByteArrayContent
            content =
            ByteArrayContent.fromString("application/json", valueNode.toString());
        HttpRequest request = requestFactory.buildPostRequest(this.endpoint, content);
        return request.execute().getStatusCode();
    }
}
