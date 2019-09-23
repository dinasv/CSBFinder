package MVC.View.Events;

public class showSaveMsgEvent implements Event {

    private boolean showSaveMsg;

    public showSaveMsgEvent(boolean showSaveMsg) {
        this.showSaveMsg = showSaveMsg;
    }

    public boolean isShowSaveMsg() {
        return showSaveMsg;
    }
}
