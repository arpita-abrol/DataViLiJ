package actions;

import Algorithm.Configuration;
import dataProcessors.AppData;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public class AppActions implements ActionComponent{

    private ApplicationTemplate applicationTemplate;
    private Path dataPath;
    private boolean isLoading;

    public AppActions(ApplicationTemplate applicationTemplate){
        this.applicationTemplate= applicationTemplate;
    }

    @Override
    public void handleNewRequest() {
        AppUI ui = (AppUI) applicationTemplate.getUIComponent();
        PropertyManager manager = applicationTemplate.manager;
        Dialog dialog = applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
        if(!ui.getLeftTopPane().isVisible()) {
            ui.getLeftTopPane().setVisible(true);
            ui.disableNewButton(true);
        }
        else if( isLoading||
                !((AppData)applicationTemplate.getDataComponent()).hasNewText(ui.getTextArea().getText())){
            ui.getTextArea().clear();
            ui.clearDataInofrmation();
            ui.getTextArea().setDisable(false);
            ((AppUI)applicationTemplate.getUIComponent()).getSelectionPane().setVisible(false);
            isLoading=false;
            dataPath=null;
        }
        else {
            dialog.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                    manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));
            if (((ConfirmationDialog) dialog).getSelectedOption() == ConfirmationDialog.Option.YES) {
                handleSaveRequest();
                ui.getTextArea().clear();
                ui.clearDataInofrmation();
                dataPath=null;
            } else if (((ConfirmationDialog) dialog).getSelectedOption() == ConfirmationDialog.Option.NO) {
                //clear
                ui.clearDataInofrmation();
                ui.getTextArea().clear();
                dataPath=null;
            }
        }
    }

    @Override
    public void handleSaveRequest() {
        try {
            if(dataPath==null) {
                promptToSave();
            }
            applicationTemplate.getDataComponent().saveData(dataPath);
        } catch (NullPointerException e) {
            //do nothing if user cancel saving
        }
    }

    @Override
    public void handleLoadRequest() {
        FileChooser fileChooser =new FileChooser();
        try{
            dataPath=fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow()).toPath();
            applicationTemplate.getDataComponent().loadData(dataPath);
            isLoading=true;
        }catch (NullPointerException e){
            //do nothing if user cancel loading
        }
    }

    @Override
    public void handleExitRequest() {
        PropertyManager manager=applicationTemplate.manager;
        if( isLoading||
                !((AppData)applicationTemplate.getDataComponent())
                        .hasNewText(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText())){
            applicationTemplate.getUIComponent().getPrimaryWindow().close();
        }else{
            Dialog dialog = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            dialog.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                    manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));
            if((((ConfirmationDialog) dialog).getSelectedOption() == ConfirmationDialog.Option.YES)){
                handleSaveRequest();
                applicationTemplate.getUIComponent().getPrimaryWindow().close();
            }else if((((ConfirmationDialog) dialog).getSelectedOption() == ConfirmationDialog.Option.NO))
                applicationTemplate.getUIComponent().getPrimaryWindow().close();
        }
    }

    public void handleScreenShootRequest(){
        PropertyManager manager = applicationTemplate.manager;
        Dialog errorDialog= applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        WritableImage image = ((AppUI)applicationTemplate.getUIComponent()).getChart().snapshot(new SnapshotParameters(),null);
        FileChooser fileChooser = new FileChooser();
        String directory_Path=manager.getPropertyValue(AppPropertyTypes.Separator.name()) +
                manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
        URL url=getClass().getResource(directory_Path);
        File temp = new File(url.getFile());
        if(!temp.isDirectory()){
            errorDialog.show(manager.getPropertyValue(AppPropertyTypes.Subdir_Not_Found_Title.name()),
                    manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND_MESSAGE.name()));
        }else
            fileChooser.setInitialDirectory(temp);
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.Image_File_Ext_Desc.name())
                ,manager.getPropertyValue(AppPropertyTypes.Image_File_Ext_With_StarKey.name()));
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if(file!= null){
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), manager.getPropertyValue(AppPropertyTypes.Image_File_Ext.name()), file);
            }catch (IOException ioError){
                errorDialog.show(manager.getPropertyValue(AppPropertyTypes.ScreenShot_Error_Title.name()),
                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ScreenShot_Error_Message.name()));
            }
        }
    }
    @Override
    public void handlePrintRequest() {

    }
    private void promptToSave() throws NullPointerException{
        PropertyManager manager = applicationTemplate.manager;
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name())
                ,manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name()));
        fileChooser.getExtensionFilters().add(filter);
        //set initial directory
        String directory_Path=applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Separator.name()) +
                applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
        URL url=getClass().getResource(directory_Path);
        File temp = new File(url.getFile());
        fileChooser.setInitialDirectory(temp);

        dataPath = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow()).toPath();
    }
}
