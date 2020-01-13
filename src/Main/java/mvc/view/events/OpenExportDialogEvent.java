package mvc.view.events;

import model.OutputType;

/**
 */
public class OpenExportDialogEvent extends OpenDialogEvent {

    private OutputType outputType;

    public OpenExportDialogEvent(OutputType outputType){

        this.outputType = outputType;
    }

    public OutputType getOutputType() {
        return outputType;
    }
}
