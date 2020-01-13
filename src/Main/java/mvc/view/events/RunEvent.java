package mvc.view.events;

import mvc.view.requests.Request;

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
}
