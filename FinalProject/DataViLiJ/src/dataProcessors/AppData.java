package dataProcessors;

import Algorithm.AlgorithmType;
import Algorithm.ClassificationAlgorithm;
import Algorithm.ClusteringAlgorithm;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.io.*;
import java.nio.file.Path;

public class AppData implements DataComponent {

    private static final String NEW_LINE_CHAR ="\n";
    private ApplicationTemplate applicationTemplate;
    private DataProcessor processor;
    private Data originalData;
    private Data modifiedData;
    private String initialSaveText;

    public AppData(ApplicationTemplate applicationTemplate){
        this.applicationTemplate=applicationTemplate;
    }

    public Data getOriginalData() {
        return originalData;
    }

    @Override
    public void loadData(Path dataFilePath){
        StringBuilder stringbuilder = new StringBuilder();
        PropertyManager manager = applicationTemplate.manager;
        AppUI ui= (AppUI)applicationTemplate.getUIComponent();
        Dialog error = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        if(dataFilePath.toString().endsWith(manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name())
                .substring(1))){
            try{
                String temp;
                BufferedReader fileReader = new BufferedReader(new FileReader(dataFilePath.toFile()));
                while((temp=fileReader.readLine())!=null){
                    stringbuilder.append(temp);
                    stringbuilder.append(NEW_LINE_CHAR);
                }
                fileReader.close();
                CheckDataValidity(stringbuilder.toString());
                ui.getTextArea().setText(originalData.getFirstTenLines());
                ui.showDataInformation(
                        originalData.getDataInfo(manager.getPropertyValue(AppPropertyTypes.LOADED_DATA_INFO_TEXT.name()),
                                manager.getPropertyValue(AppPropertyTypes.LOADED_FILE_LOCATION_TEXT.name()),
                                dataFilePath.getFileName().toString(),dataFilePath.toAbsolutePath().toString()));
                ui.getTextArea().setDisable(true);
                if(!ui.getLeftTopPane().isVisible())
                    ui.getLeftTopPane().setVisible(true);
                ui.showAlgorithmTypeSelection(originalData);
                ui.disableSaveButton(true);
            }catch (IOException io){
                error.show(manager.getPropertyValue(AppPropertyTypes.LOAD_ERROR_TITLE.name()),
                        manager.getPropertyValue(AppPropertyTypes.LOAD_IO_ERROR_MESSAGE.name()));
            }catch (Exception e){
                error.show(manager.getPropertyValue(AppPropertyTypes.LOAD_ERROR_TITLE.name()),
                        e.getMessage());
            }
        }else{
            //incorrect data format error
            String filePath = dataFilePath.toString();
            String fileExtension = filePath.substring(filePath.lastIndexOf('.')+1);
            error.show(manager.getPropertyValue(AppPropertyTypes.LOAD_ERROR_TITLE.name()),
                    String.format(manager.getPropertyValue(AppPropertyTypes.LOAD_WRONG_FORMAT_MESSAGE.name())
                            , fileExtension));
        }
    }
    public void loadDataToChart(AlgorithmType algorithmType){
        if(algorithmType.getClass().getSuperclass().equals(ClusteringAlgorithm.class)){
            processor= new ClusteringProcessor();
        }else if(algorithmType.getClass().getSuperclass().equals(ClassificationAlgorithm.class)){
            processor=new ClassificationProcessor();
        }
        processor.toChartData(originalData,((AppUI)applicationTemplate.getUIComponent()).getChart());
        ((AppUI)applicationTemplate.getUIComponent()).disableScrnShotButton(false);
    }
    @Override
    public void saveData(Path dataFilePath){
        Dialog error = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager= applicationTemplate.manager;
        try{
            CheckDataValidity(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
            if(null != dataFilePath.toFile()){
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dataFilePath.toFile()));
                bufferedWriter.write(originalData.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
            }
            initialSaveText=originalData.toString();
            Dialog dialog = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            dialog.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVE_TITLE.name()),
                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVE_LAST_LOCATION_MESSAGE.name()));
            ((AppUI) applicationTemplate.getUIComponent()).disableSaveButton(true);
        }catch (IOException io){
            error.show(manager.getPropertyValue(AppPropertyTypes.SAVE_ERROR_TITLE.name()),
                    manager.getPropertyValue(AppPropertyTypes.SAVE_IO_ERROR_MESSAGE.name()));

        }catch (Exception e){
            error.show(manager.getPropertyValue(AppPropertyTypes.SAVE_ERROR_TITLE.name()),
                    e.getMessage());
        }
    }

    public boolean loadData(String data){
        PropertyManager manager = applicationTemplate.manager;
        try{
            CheckDataValidity(data);
            ((AppUI)applicationTemplate.getUIComponent()).showAlgorithmTypeSelection(originalData);
            return true;
        }catch (Exception e){
            Dialog errorDialog = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            errorDialog.show(manager.getPropertyValue(AppPropertyTypes.INVALID_INPUT_TITLE.name()),
                    e.getMessage());
            return false;//failed to load Data
        }
    }
    public boolean hasNewText(String textAreaContent){
        Data temp = new Data();
        try {
            temp.setData(textAreaContent);
            return !initialSaveText.equals(temp.toString());
        }catch (Exception e){
            return false;
        }
    }
    @Override
    public void clear() {
        originalData.clear();
        modifiedData.clear();
    }
    //check if data is valid for TSD format saving
    private void CheckDataValidity(String data) throws Exception{
        originalData = new Data();
        originalData.setData(data);
    }
}
