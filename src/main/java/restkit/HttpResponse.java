package restkit;

import okhttp3.Response;

import java.util.Map;

public class HttpResponse<T, E> {

    private int code;

    private T data;

    private E error;

    public HttpResponse() {
    }

    public HttpResponse(int code, T data, E error) {
        this.code = code;
        this.data = data;
        this.error = error;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public E getError() {
        return error;
    }

    public void setError(E error) {
        this.error = error;
    }
}
