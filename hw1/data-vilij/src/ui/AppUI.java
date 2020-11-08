package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.IOException;


/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    private CheckBox                     readOnly;        //checkbox to make textarea read only
    private boolean                      NonmachineAction;

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    public void addToExistingText(String newText){textArea.appendText(newText);}
    public String getTextFieldContent(){ return textArea.getText(); }
    public void clearTextArea(){textArea.clear();}

    public void disableSaveButton(){saveButton.setDisable(true);}
    public boolean SaveButtonIsEnable(){return !saveButton.isDisable();}
    public void diableScrnshotButton(Boolean disable){
        if(disable)
            scrnshotButton.setDisable(true);
        else
            scrnshotButton.setDisable(false);
    }

    public boolean getHasNewText(){return hasNewText;}

    public LineChart<Number, Number> getChart() { return chart; }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        //added new button for screenshot
        PropertyManager manager = applicationTemplate.manager;

        super.setToolBar(applicationTemplate);
        String scrnShotPath =manager.getPropertyValue(AppPropertyTypes.Separator.name())+String.join(manager.getPropertyValue(AppPropertyTypes.Separator.name()),
                                                    manager.getPropertyValue(PropertyTypes.GUI_RESOURCE_PATH.name()),
                                                    manager.getPropertyValue(PropertyTypes.ICONS_RESOURCE_PATH.name()),
                                                    manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnShotPath,manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),true);
        toolBar = new ToolBar(newButton,saveButton,loadButton,printButton,exitButton,scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        //clears all data
        while(!chart.getData().isEmpty())
            chart.getData().remove((int)(Math.random()*(chart.getData().size()-1)));
    }

    private void layout() {
        //initialize the pane
        workspace= new GridPane();
        HBox CheckBox_DisPlayButton_Pane = new HBox();
        PropertyManager manager= applicationTemplate.manager;
        //initialize the UI components needed
        NumberAxis x_axis = new NumberAxis();
        NumberAxis y_axis = new NumberAxis();
        chart= new LineChart<>(x_axis,y_axis);
        chart.setVerticalGridLinesVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalZeroLineVisible(false);
        chart.setHorizontalZeroLineVisible(false);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.chart_Title.name()));
        displayButton= new Button();
        displayButton.setText(manager.getPropertyValue(AppPropertyTypes.Display_Label.name()));
        textArea = new TextArea();
        textArea.setPromptText(manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name()));
        readOnly = new CheckBox();
        readOnly.setText(manager.getPropertyValue(AppPropertyTypes.READ_ONLY_LABEL.name()));
        Label label = new Label();
        label.setText(manager.getPropertyValue(AppPropertyTypes.Text_Field_Title.name()));
        label.setFont(new Font(manager.getPropertyValueAsInt(AppPropertyTypes.Data_Label_Title_Font.name())));//label font size
        //formatting the GridPane
        CheckBox_DisPlayButton_Pane.getChildren().addAll(displayButton,readOnly);
        CheckBox_DisPlayButton_Pane.setAlignment(Pos.CENTER);
        CheckBox_DisPlayButton_Pane.setSpacing(270);
        workspace.getChildren().addAll(chart,textArea,label,CheckBox_DisPlayButton_Pane);
        GridPane.setConstraints(label,0,0,1,1,HPos.CENTER,VPos.CENTER);
        GridPane.setConstraints(CheckBox_DisPlayButton_Pane,0,2,1,1,HPos.CENTER,VPos.CENTER);
        GridPane.setConstraints(textArea,0,1);
        GridPane.setConstraints(chart,1,1);
        appPane.getChildren().add(workspace);
        primaryScene.getStylesheets().add(getClass().getResource(manager.getPropertyValue(AppPropertyTypes.CSS_Path.name())).toExternalForm());
    }
    public void setTextFild(String data){
        NonmachineAction=false;
        textArea.clear();
        textArea.setText(data);
        NonmachineAction=true;
    }

    private void setWorkspaceActions(){
        // TODO for homework 1
        textArea.textProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) ->{

                    //if text is not empty, new button and save button enable
                if(!textArea.getText().isEmpty()) {
                    ((AppData)applicationTemplate.getDataComponent()).setTextAreaAtTenLines();
                    newButton.setDisable(false);
                    if(((AppActions) applicationTemplate.getActionComponent()).getInitialSaveText()!=null &&
                            (((AppActions) applicationTemplate.getActionComponent()).getInitialSaveText()).equals(newValue)) {
                        saveButton.setDisable(true);
                        hasNewText = false;
                    }
                    else {

                        saveButton.setDisable(false);
                        hasNewText=true;
                    }
                }
                    //if text is empty, new button and save button disable
                else {
                    if(NonmachineAction) {
                        ((AppData)applicationTemplate.getDataComponent()).setTextAreaAtTenLines();
                    }
                    hasNewText=false;
                    newButton.setDisable(true);
                    saveButton.setDisable(true);
                }
            }
        );
        //((AppData)applicationTemplate.getDataComponent()).setTextAreaAtTenLines();
        displayButton.setOnAction(e -> {
                if(!textArea.getText().isEmpty())
                    ((AppData) applicationTemplate.getDataComponent()).loadData(textArea.getText());
        });
        readOnly.setOnAction(e ->{
            if(readOnly.isSelected())
                textArea.setDisable(true);
            else
                textArea.setDisable(false);
        } );
        scrnshotButton.setOnAction(e -> {
            try{
            ((AppActions)applicationTemplate.getActionComponent()).handleScreenshotRequest();
            }catch (IOException io){
                Dialog error =applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                error.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ScreenShot_Error_Title.name()),
                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ScreenShot_Error_Message.name()));

            }
        });
    }


}
