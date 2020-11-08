package dataprocessors;

import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.*;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;
    private  final String new_Line_Char ="\n";

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        StringBuilder data = new StringBuilder();
        Dialog errorDialog = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        if(dataFilePath.toString().endsWith(applicationTemplate.manager
                .getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name()).substring(1))){
            try {
                String temp;
                BufferedReader fileReader = new BufferedReader(new FileReader(dataFilePath.toFile()));
                while((temp=fileReader.readLine())!=null){
                    data.append(temp);
                    data.append(new_Line_Char);
                }
                fileReader.close();
                checkDataFormatInTSDFile(data.toString());
                loadData(data.toString());
                ((AppUI)applicationTemplate.getUIComponent()).setTextFild(processor.getInitialFirstTenLines());
                Dialog information = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                if(processor.getTotalLine()>10) {
                    information.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Loaded_Data_Info.name()),
                            String.format(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Loaded_Data_Info_Message_Version1.name()),processor.getTotalLine()));
                }else{
                        information.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Loaded_Data_Info.name()),
                                String.format(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Loaded_Data_Info_Message_Version2.name()),processor.getTotalLine()));
                }
            }catch (IOException e){
                errorDialog.show(applicationTemplate.manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name()),
                        applicationTemplate.manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name())+dataFilePath.toFile().getName());
            }catch (Exception e){
                errorDialog.show(applicationTemplate.manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name()),
                        e.getMessage()
                                +applicationTemplate.manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name())
                                +dataFilePath.toFile().getName());
            }
        }else{
            errorDialog.show(applicationTemplate.manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name()),
                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Load_Error_Message.name()));
        }
    }
    private String deleteEmptyLines (String string){
        List<String> lines = new LinkedList<>();
        Stream.of(string.split(new_Line_Char)).forEach(line->{
            if(line.length()>0)
                lines.add(line);
        });
        return String.join(new_Line_Char,lines);
    }
    public void setTextAreaAtTenLines(){
        AppUI appUI =((AppUI)applicationTemplate.getUIComponent());
        int NumberOfLinesNeed = 10-countTextAreaLine(appUI.getTextFieldContent());
        if(NumberOfLinesNeed>0){
            String missingLines =new_Line_Char+processor.addMissingLinesToTextArea(NumberOfLinesNeed);
            if(missingLines.length()>2) {
                appUI.addToExistingText(missingLines);
                appUI.setTextFild(deleteEmptyLines(appUI.getTextFieldContent()));
            }
        }
    }
    public int countTextAreaLine(String TextAreaContent){
        AtomicInteger TextAreaLine=new AtomicInteger(0);
        Stream.of(TextAreaContent.split(new_Line_Char)).forEach(string ->{
            if(string.length()>0)
                TextAreaLine.getAndIncrement();
        });
        return TextAreaLine.get();
    }
    public void loadData(String dataString){
        Dialog errorDialog = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        try {
            clear();
            applicationTemplate.getUIComponent().clear();
            processor.processString(dataString);
            displayData();
            ((AppUI)applicationTemplate.getUIComponent()).diableScrnshotButton(false);
        } catch(Exception e){
            errorDialog.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Display_Error_Title.name()),
                    e.getMessage());
            ((AppUI)applicationTemplate.getUIComponent()).diableScrnshotButton(true);
        }
    }
    //helper method to check dataformat that is implemented in TSDProcessor class
    public void checkDataFormatInTextField() throws Exception{
        String data=((AppUI)applicationTemplate.getUIComponent()).getTextFieldContent();
        processor.clear();
        processor.processString(data);
    }
    private void checkDataFormatInTSDFile(String data) throws Exception{
        processor.clear();
        processor.processString(data);
    }

    @Override
    public void saveData(Path dataFilePath){
        try {
            File file = dataFilePath.toFile();
            if (file != null) {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(((AppUI) applicationTemplate.getUIComponent()).getTextFieldContent());
                fileWriter.close();
            }
        }catch (IOException e){
            Dialog errorDialog = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            errorDialog.show(applicationTemplate.manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),
                    applicationTemplate.manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name())+dataFilePath);
        }
    }

    @Override
    public void clear() {
        processor.clear();
    }

    private void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart(),
                applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Average_Label.name()),
                applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Average_Line_CSS_ID.name()));
    }
}
