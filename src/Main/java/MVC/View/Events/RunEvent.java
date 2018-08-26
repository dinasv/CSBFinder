package MVC.View.Events;

import MVC.Common.CSBFinderRequest;

import java.util.EventObject;

public class RunEvent extends EventObject {

    private CSBFinderRequest request;

    public RunEvent(Object source) {
        super(source);
    }

    public RunEvent(Object source, CSBFinderRequest request) {
        super(source);
        this.request = request;
    }

    public CSBFinderRequest getRequest() {
        return request;
    }

    public void setRequest(CSBFinderRequest request) {
        this.request = request;
    }
}
