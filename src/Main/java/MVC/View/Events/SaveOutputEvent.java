package MVC.View.Events;

import Core.OutputType;

import java.util.EventObject;

public class SaveOutputEvent extends EventObject {

    private OutputType outputType;
    private String outputDirectory;
    private String datasetName;
    private int action;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public SaveOutputEvent(Object source) {
        super(source);
    }

    public SaveOutputEvent(Object source, OutputType outputType,  String datasetName, String outputDirectory,
                           int action) {
        super(source);
        this.outputType = outputType;
        this.datasetName = datasetName;
        this.outputDirectory = outputDirectory;
        this.action = action;
    }

    public OutputType getOutputType() {
        return outputType;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputType(OutputType outputType) {
        this.outputType = outputType;
    }

    public int getAction() {
        return action;
    }

    public String getDatasetName() {
        return datasetName;
    }
}
