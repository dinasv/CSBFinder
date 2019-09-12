package MVC.View.Events;

import Model.OutputType;

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
