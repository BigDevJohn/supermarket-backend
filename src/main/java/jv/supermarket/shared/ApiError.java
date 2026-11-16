package jv.supermarket.shared;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response for API operations")
public class ApiError implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Error timestamp", example = "2024-12-16T14:55:50Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    private Instant timestamp;

    @Schema(description = "HTTP error status")
    private Integer status;

    @Schema(description = "Error title")
    private String error;

    @Schema(description = "URI of the request where the error occurred")
    private String path;

    @Schema(description = "Additional error messages")
    private List<String> messages;

    public ApiError() {
    }

    public ApiError(Instant timestamp, Integer status, String error, String path, List<String> messages) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.path = path;
        this.messages = messages;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
