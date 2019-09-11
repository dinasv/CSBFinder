package MVC.View.Events;

public class DontShowSaveMsgEvent implements Event {

    private boolean showSaveMsg;

    public DontShowSaveMsgEvent(boolean showSaveMsg) {
        this.showSaveMsg = showSaveMsg;
    }

    public boolean isShowSaveMsg() {
        return showSaveMsg;
    }
}
