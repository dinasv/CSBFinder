package MVC.View.Events;

import MVC.View.Requests.Request;

import java.util.EventObject;

public class RunEvent<T extends Request> extends EventObject {

    private T request;

    public RunEvent(Object source, T request) {
        super(source);
        this.request = request;
    }

    public T getRequest() {
        return request;
    }

    public void setRequest(T request) {
        this.request = request;
    }
}
