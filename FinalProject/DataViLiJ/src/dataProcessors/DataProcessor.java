package dataProcessors;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;

import java.util.HashMap;
import java.util.HashSet;

public interface DataProcessor {
    default void toChartData(Data InputData, XYChart<Number, Number> chart){
        HashMap<String, String> dataLabels = InputData.getDataLabels();
        HashMap<String, Point2D> dataPoints =InputData.getDataPoints();
        HashSet<String> labels =InputData.getLabels();
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);

            dataLabels.entrySet().stream().filter(entry -> entry.getValue()!=null&& entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY(),entry.getKey()));
            });
            chart.getData().add(series);
            for(XYChart.Data<Number,Number> data : series.getData()){
                Tooltip.install(data.getNode(),new Tooltip(data.getExtraValue().toString()));
                data.getNode().setCursor(Cursor.CROSSHAIR);
            }
        }
        XYChart.Series<Number,Number> series = new XYChart.Series<>();
        dataLabels.entrySet().stream().filter(entry-> entry.getValue()==null).forEach(entry -> {
            Point2D point = dataPoints.get(entry.getKey());
            series.getData().add(new XYChart.Data<>(point.getX(), point.getY(),entry.getKey()));
        });
        chart.getData().add(series);
        series.setName(null);

        for(XYChart.Data<Number,Number> data : series.getData()){
            Tooltip.install(data.getNode(),new Tooltip(data.getExtraValue().toString()));
            data.getNode().setCursor(Cursor.CROSSHAIR);
        }
    }
}
