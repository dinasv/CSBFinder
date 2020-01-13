package mvc.view.events;

import model.OutputType;


public class ExportEvent implements Event {

    private OutputType outputType;
    private String outputDirectory;
    private String datasetName;
    private int action;


    public ExportEvent(OutputType outputType, String datasetName, String outputDirectory,
                       int action) {
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
