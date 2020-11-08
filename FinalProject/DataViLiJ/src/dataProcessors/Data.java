package dataProcessors;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Data {
    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character. ";

        public InvalidDataNameException(String name, int lineNum) {
            super(String.format("Invalid name '%s' at line %d. " + NAME_ERROR_MSG,name,lineNum));
        }
    }
    public static  class RepeatingIDException extends Exception{
        private  static final String REPEATING_NAME_ERROR_MSG ="All data instance names must be use only once. ";

        public RepeatingIDException(String name,int lineNum){
            super(String.format("Instance name '%s' cannot be use at line %d because it already exist. "+ REPEATING_NAME_ERROR_MSG,name,lineNum));
        }
    }

    private HashMap<String, String>     dataLabels;
    private HashMap<String, Point2D>    dataPoints;
    private ArrayList<String>           dataOrder;
    private AtomicInteger               lineNum;
    private HashSet<String>             labels;
    private static final String UNIVERSAL_ERROR_MESSAGE = "Invalid data format ar line %d.";
    private static final String NEW_LINE_CHAR ="\n";
    private static final String TAB_CHAR="\t";
    public Data(){
        dataLabels= new HashMap<>();
        dataOrder = new ArrayList<>();
        dataPoints = new HashMap<>();
        lineNum= new AtomicInteger();
        labels = new HashSet<>();
    }

    public HashMap<String, Point2D> getDataPoints() {
        return dataPoints;
    }

    public HashSet<String> getLabels() {
        return labels;
    }

    public HashMap<String, String> getDataLabels() {
        return dataLabels;
    }

    public int getLabelNumber(){return labels.size();}
    @Override
    public String toString() {
        StringBuilder data = new StringBuilder();
        for(int i =0;i<dataOrder.size();i++){
            String temp= dataOrder.get(i);
            data.append(temp)
                    .append(TAB_CHAR)
                    .append(dataLabels.get(temp))
                    .append(TAB_CHAR)
                    .append(dataPoints.get(temp).getX())
                    .append(",")
                    .append(dataPoints.get(temp).getY())
                    .append(NEW_LINE_CHAR);
        }
        return data.toString();
    }

    public String getFirstTenLines(){
        StringBuilder firstTenLines= new StringBuilder();
        for(int i=0;i<10;i++){
            if(i<dataOrder.size()) {
                String temp = dataOrder.get(i);
                firstTenLines.append(temp)
                        .append(TAB_CHAR)
                        .append(dataLabels.get(temp))
                        .append(TAB_CHAR)
                        .append(dataPoints.get(temp).getX())
                        .append(",")
                        .append(dataPoints.get(temp).getY())
                        .append(NEW_LINE_CHAR);
            }
        }
        return firstTenLines.toString();
    }

    public String getDataInfo(String string){
        int instanceNumber = dataOrder.size();
        int labelNumber = labels.size();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(string,instanceNumber,labelNumber));
        for (String label : labels) {
            stringBuilder.append(NEW_LINE_CHAR).append("-"+label);
        }
        return stringBuilder.toString();
    }

    public String getDataInfo(String string,String string2, String filename, String fileAbsolutePath){
        int instanceNumber = dataOrder.size();
        int labelNumber = labels.size();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(string,instanceNumber,labelNumber,filename));
        for (String label : labels) {
            stringBuilder.append(NEW_LINE_CHAR).append("-"+label);
        }
        stringBuilder.append(NEW_LINE_CHAR).append(string2).append(NEW_LINE_CHAR).append(fileAbsolutePath);
        return stringBuilder.toString();
    }
    public void setData(String data) throws Exception{
        clear();
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        lineNum.set(0);
        Stream.of(data.split(NEW_LINE_CHAR))
                .map(line -> Arrays.asList(line.split(TAB_CHAR)))
                .forEach(list -> {
                    if(!hadAnError.get()){
                        try {
                            lineNum.getAndIncrement();
                            String   name  = checkName(list.get(0));
                            dataOrder.add(name);
                            checkInstanceNameRepetition(name);
                            String   label = list.get(1);
                            boolean isNull=label.equalsIgnoreCase("null");
                            if(!isNull)
                                labels.add(label);
                            String[] pair  = list.get(2).split(",");
                            Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                            if(list.size()>3)
                                throw new Exception();
                            if(isNull)
                                dataLabels.put(name,null);
                            else
                                dataLabels.put(name, label);
                            dataPoints.put(name, point);
                        } catch (InvalidDataNameException|RepeatingIDException e) {
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

    private void checkInstanceNameRepetition(String name) throws  RepeatingIDException{
        if(dataLabels.containsKey(name))
            throw new RepeatingIDException(name,lineNum.intValue());
    }
    private String checkName(String name) throws InvalidDataNameException{
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name,lineNum.intValue());
        return name;
    }

    public void clear(){
        dataOrder.clear();
        dataLabels.clear();
        dataPoints.clear();
    }




}
