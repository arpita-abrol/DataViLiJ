package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /*warning title*/
    UnSave_Work,
    /* warning messages*/
    EXIT_WHILE_RUNNING_WARNING,
    /* error title*/
    Subdir_Not_Found_Title,
    Display_Error_Title,
    ScreenShot_Error_Title,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    Save_Error_Message,
    Load_Error_Message,
    ScreenShot_Error_Message,


    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,
    Loaded_Data_Info,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,
    SAVE_WORK_NOTIFICATION,
    Loading_With_Unsave_Work_Message,
    Loaded_Data_Info_Message_Version1,
    Loaded_Data_Info_Message_Version2,
    /* file path symbol*/
    Separator,
    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    Image_File_Ext_With_StarKey,
    Image_File_Ext,
    Image_File_Ext_Desc,
    TEXT_AREA,
    chart_Title,
    Display_Label,
    Text_Field_Title,
    Data_Label_Title_Font,
    READ_ONLY_LABEL,
    CSS_Path,
    Average_Label,
    Average_Line_CSS_ID,
}
