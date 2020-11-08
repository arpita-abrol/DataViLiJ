package actions;

import dataprocessors.AppData;
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
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    private Path dataFilePath;
    /** content inside the textbox when it first save. */
    private String initialSaveText;
    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }
    @Override
    public void handleNewRequest() {
        PropertyManager manager = applicationTemplate.manager;
        Dialog dialog= applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
        dialog.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));
        Dialog errorDialog = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        if(((ConfirmationDialog)dialog).getSelectedOption()==ConfirmationDialog.Option.YES) {
            try {
                ((AppData)applicationTemplate.getDataComponent()).checkDataFormatInTextField();
                if (promptToSave()) {
                    applicationTemplate.getDataComponent().clear();
                    applicationTemplate.getUIComponent().clear();
                    ((AppUI) applicationTemplate.getUIComponent()).clearTextArea();
                }
            } catch (IOException io) {
                errorDialog.show(manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),
                        manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name()) + dataFilePath);
            }catch (Exception e){
                errorDialog.show(manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),e.getMessage());
            }
        }else if(((ConfirmationDialog) dialog).getSelectedOption()==ConfirmationDialog.Option.NO){
            applicationTemplate.getDataComponent().clear();
            applicationTemplate.getUIComponent().clear();
            ((AppUI) applicationTemplate.getUIComponent()).clearTextArea();
        }
    }
    @Override
    public void handleSaveRequest() {
        PropertyManager manager = applicationTemplate.manager;
        Dialog errorDialog= applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        try{
            ((AppData)applicationTemplate.getDataComponent()).checkDataFormatInTextField();
            if(dataFilePath!=null) {
                Dialog dialog = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                dialog.show(manager.getPropertyValue(PropertyTypes.SAVE_WORK_TITLE.name()),
                        manager.getPropertyValue(AppPropertyTypes.SAVE_WORK_NOTIFICATION.name()));
                applicationTemplate.getDataComponent().saveData(dataFilePath);
            }else{
                promptToSave();
            }
            initialSaveText=((AppUI)applicationTemplate.getUIComponent()).getTextFieldContent();
            ((AppUI)applicationTemplate.getUIComponent()).disableSaveButton();
        }catch (IOException e){
            errorDialog.show(manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),
                    manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name())+dataFilePath.toFile().getName());
        }catch (Exception e){
            errorDialog.show(manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),
                    e.getMessage()+manager.getPropertyValue(AppPropertyTypes.Save_Error_Message.name()));
        }
    }
    @Override
    public void handleLoadRequest() {
        FileChooser fileChooser = new FileChooser();
        try {
            if(((AppUI)applicationTemplate.getUIComponent()).SaveButtonIsEnable()){
                Dialog savingRequest = applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
                savingRequest.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Loading_With_Unsave_Work_Message.name()));
                if (((ConfirmationDialog)savingRequest).getSelectedOption()== ConfirmationDialog.Option.YES){
                    handleSaveRequest();
                }
                if(((ConfirmationDialog)savingRequest).getSelectedOption()== ConfirmationDialog.Option.YES
                        || ((ConfirmationDialog)savingRequest).getSelectedOption()== ConfirmationDialog.Option.NO ){
                    dataFilePath = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow()).toPath();
                    applicationTemplate.getDataComponent().loadData(dataFilePath);
                    initialSaveText=((AppUI)applicationTemplate.getUIComponent()).getTextFieldContent();
                    ((AppUI)applicationTemplate.getUIComponent()).disableSaveButton();
                }
            }else{
                dataFilePath = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow()).toPath();
                applicationTemplate.getDataComponent().loadData(dataFilePath);
                initialSaveText=((AppUI)applicationTemplate.getUIComponent()).getTextFieldContent();
                ((AppUI)applicationTemplate.getUIComponent()).disableSaveButton();
            }
        }catch (NullPointerException e){
           //do nothing
        }
    }
    @Override
    public void handleExitRequest() {
        //ask user save confirmation window if they have new text since last save
        if(((AppUI)applicationTemplate.getUIComponent()).getHasNewText()
                && ((AppUI)applicationTemplate.getUIComponent()).SaveButtonIsEnable()) {
            Dialog dialog = applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
            dialog.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.UnSave_Work.name()),
                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.EXIT_WHILE_RUNNING_WARNING.name()));
            PropertyManager manager=applicationTemplate.manager;
            Dialog errorDialog = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            //user decide to save work before quitting. If they cancel during the prompt window, it will do close window
            if (((ConfirmationDialog) dialog).getSelectedOption() == ConfirmationDialog.Option.YES) {
                try {
                    ((AppData)applicationTemplate.getDataComponent()).checkDataFormatInTextField();
                    promptToSave();
                    applicationTemplate.getUIComponent().getPrimaryWindow().close();
                } catch (IOException io) {
                    errorDialog.show(applicationTemplate.manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),
                            applicationTemplate.manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name()) + dataFilePath);
                }catch (Exception e){
                    errorDialog.show(manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),
                            e.getMessage()+manager.getPropertyValue(AppPropertyTypes.Save_Error_Message.name()));
                }
                //user decide to quit without saving unsave work
            }else if(((ConfirmationDialog) dialog).getSelectedOption() == ConfirmationDialog.Option.NO)
                applicationTemplate.getUIComponent().getPrimaryWindow().close();
            //if there is nothing in the textfield, it will just close without asking
        }else
            applicationTemplate.getUIComponent().getPrimaryWindow().close();
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }
    public void handleScreenshotRequest() throws IOException {
        PropertyManager manager = applicationTemplate.manager;
        WritableImage image = ((AppUI)applicationTemplate.getUIComponent()).getChart().snapshot(new SnapshotParameters(),null);
        FileChooser fileChooser = new FileChooser();
        String directory_Path=manager.getPropertyValue(AppPropertyTypes.Separator.name()) +
                manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
        URL url=getClass().getResource(directory_Path);
        File temp = new File(url.getFile());
        if(!temp.isDirectory()){
            Dialog errorDialog= applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            errorDialog.show(manager.getPropertyValue(AppPropertyTypes.Subdir_Not_Found_Title.name()),
                    manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));
        }else
            fileChooser.setInitialDirectory(temp);
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.Image_File_Ext_Desc.name())
                ,manager.getPropertyValue(AppPropertyTypes.Image_File_Ext_With_StarKey.name()));
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if(file!= null){
            ImageIO.write(SwingFXUtils.fromFXImage(image,null),manager.getPropertyValue(AppPropertyTypes.Image_File_Ext.name()),file);
        }
    }
    //helper method to store the text when it first save
    public String getInitialSaveText(){ return initialSaveText;}

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException{
        // TODO for homework 1
        // TODO remove the placeholder line below after you have implemented this method
        PropertyManager manager = applicationTemplate.manager;
        FileChooser fileChooser = new FileChooser();
        //set extension for file saving
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name())
                                                          ,manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name()));
        fileChooser.getExtensionFilters().add(filter);
        //set initial directory
        String directory_Path=applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Separator.name()) +
                applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
        URL url=getClass().getResource(directory_Path);
        File temp = new File(url.getFile());
        if(!temp.isDirectory()){
            Dialog errorDialog= applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            errorDialog.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Subdir_Not_Found_Title.name()),
                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));
        }else
            fileChooser.setInitialDirectory(temp);
        //saving data
        if(dataFilePath==null) {
            dataFilePath = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow()).toPath();
        }else{
            Dialog dialog = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            dialog.show(manager.getPropertyValue(PropertyTypes.SAVE_WORK_TITLE.name()),
                    manager.getPropertyValue(AppPropertyTypes.SAVE_WORK_NOTIFICATION.name()));
        }
        if (dataFilePath.toFile() != null) {
            FileWriter fileWriter = new FileWriter(dataFilePath.toFile());
            fileWriter.write(((AppUI)applicationTemplate.getUIComponent()).getTextFieldContent());
            fileWriter.close();
            }

        //if user click cancel on save it will also return false
        return (dataFilePath!=null);
    }
}
