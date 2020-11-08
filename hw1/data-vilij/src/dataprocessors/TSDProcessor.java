package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character. ";

        public InvalidDataNameException(String name, int lineNum) {
            super(String.format("Invalid name '%s' at line %d. " + NAME_ERROR_MSG,name,lineNum));
        }
    }
    public static  class RepeatingDataNameException extends Exception{
        private  static final String REPEATING_NAME_ERROR_MSG ="All data instance names must be use only once. ";

        public RepeatingDataNameException(String name,int lineNum){
            super(String.format("Instance name '%s' cannot be use at line %d because it already exist. "+ REPEATING_NAME_ERROR_MSG,name,lineNum));
        }
    }

    private Map<String, String>  dataLabels;
    private Map<String, Point2D> dataPoints;
    private Map<Integer ,String>  nameOrder;
    private AtomicInteger lineNum=new AtomicInteger();
    private int TSDLinePointer;
    private static final String UNIVERSAL_ERROR_MESSAGE = "Invalid data format ar line %d.";
    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
        nameOrder = new HashMap<>();
    }
    public int getTotalLine(){return lineNum.get();}

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        lineNum.set(0);
        Stream.of(tsdString.split("\n"))
              .map(line -> Arrays.asList(line.split("\t")))
              .forEach(list -> {
                  if(!hadAnError.get()){
                    try {
                        lineNum.getAndIncrement();
                        String   name  = checkedname(list.get(0));
                        nameOrder.put(lineNum.intValue(),name);
                        checkInstanceNameRepetition(name);
                        String   label = list.get(1);
                        String[] pair  = list.get(2).split(",");
                        Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                        if(list.size()>3)
                            throw new Exception();
                        dataLabels.put(name, label);
                        dataPoints.put(name, point);
                    } catch (InvalidDataNameException|RepeatingDataNameException e) {
                        errorMessage.setLength(0);
                        errorMessage.append(e.getMessage());
                        hadAnError.set(true);
                    }catch(Exception e){
                        errorMessage.setLength(0);
                        errorMessage.append(String.format(UNIVERSAL_ERROR_MESSAGE,lineNum.intValue()));
                        hadAnError.set(true);
                    }
                  }
              });
        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());
    }
    public String getInitialFirstTenLines(){
        int lineCount=0;
        List<String> IndividualLine =new LinkedList<>();
        List<String> firstTenLines =new LinkedList<>();
        for (Integer index:nameOrder.keySet()){
            lineCount++;
            IndividualLine.add(nameOrder.get(index));
            IndividualLine.add(dataLabels.get(nameOrder.get(index)));
            IndividualLine.add(dataPoints.get(nameOrder.get(index)).getX()+
                    ","+dataPoints.get(nameOrder.get(index)).getY());
            firstTenLines.add(String.join("\t",IndividualLine));
            IndividualLine.clear();
            if(lineCount>=10) {
                TSDLinePointer=10;
                break;
            }else{TSDLinePointer=index;}
        }
        return String.join("\n",firstTenLines);
    }
    String addMissingLinesToTextArea(int NumberOfLinesNeed){
        List<String> IndividualLine = new LinkedList<>();
        List<String> setOfLines = new LinkedList<>();
        for(int i=1;i<=NumberOfLinesNeed;i++){
            if(TSDLinePointer+1<nameOrder.size()){
                IndividualLine.add(nameOrder.get(TSDLinePointer + 1));
                IndividualLine.add(dataLabels.get(nameOrder.get(TSDLinePointer + 1)));
                IndividualLine.add(dataPoints.get(nameOrder.get(TSDLinePointer + 1)).getX() +
                        "," + dataPoints.get(nameOrder.get(TSDLinePointer + 1)).getY());
                setOfLines.add(String.join("\t", IndividualLine));
                IndividualLine.clear();
                TSDLinePointer++;
            }
        }
        return String.join("\n",setOfLines);
    }
    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart, String averageLineString,String AverageLineCSSID) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);

            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY(),entry.getKey()));
            });
            chart.getData().add(series);
            for(XYChart.Data<Number,Number> data : series.getData()){
                Tooltip.install(data.getNode(),new Tooltip(data.getExtraValue().toString()));
                data.getNode().setCursor(Cursor.CROSSHAIR);
            }
        }
        setAverageYLine(chart,averageLineString,AverageLineCSSID);

    }
    private void setAverageYLine(XYChart<Number,Number> chart, String averageLineString,String AverageLineCSSID){
        XYChart.Series<Number,Number> AverageYSeries = new XYChart.Series<>();
        AverageYSeries.setName(averageLineString);
        Double yAverage= dataPoints.values().stream().mapToDouble(Point2D::getY).reduce(0.0,(a,b)->a+b)
                /dataPoints.size();
        Double XMax_values = dataPoints.values().stream().mapToDouble(Point2D::getX).max().orElse(0);
        Double XMin_values = dataPoints.values().stream().mapToDouble(Point2D::getX).min().orElse(0);
        if(!XMax_values.equals(XMin_values)) {
            AverageYSeries.getData().add(new XYChart.Data<>(XMax_values,yAverage));
            AverageYSeries.getData().add(new XYChart.Data<>(XMin_values,yAverage));
        }else{
            AverageYSeries.getData().add(new XYChart.Data<>(XMax_values+10,yAverage));
            AverageYSeries.getData().add(new XYChart.Data<>(XMax_values-10,yAverage));
        }
        chart.getData().add(AverageYSeries);
        AverageYSeries.getNode().setId(AverageLineCSSID);
        for (XYChart.Data<Number,Number> data: AverageYSeries.getData()) {
            data.getNode().setVisible(false);
        }
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
        nameOrder.clear();
    }
    private void checkInstanceNameRepetition(String name) throws RepeatingDataNameException{
        if(dataLabels.containsKey(name))
            throw new RepeatingDataNameException(name,lineNum.intValue());
    }
    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name,lineNum.intValue());
        return name;
    }
}
