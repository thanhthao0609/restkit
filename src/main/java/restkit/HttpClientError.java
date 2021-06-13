package restkit;

public enum HttpClientError {
    URL_INVALID("url invalid"),
    METHOD_INVALID("http method invalid");

    HttpClientError(String message) {
        this.message = message;
    }

    private String message;

    public String getMessage() {
        return message;
    }
}
