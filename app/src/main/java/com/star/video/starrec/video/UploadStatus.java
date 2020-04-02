package com.star.video.starrec.video;

/**
 * description the download status
 * <p/>
 * Created by weiwei on 19/12/23.
 */
public class UploadStatus {
    /**
     * init download
     */
    public static final int UPLOAD_STATUS_INIT = 1;
    /**
     * downloading
     */
    public static final int UPLOAD_STATUS_UPLOADING = 2;
    /**
     * download cancel
     */
    public static final int UPLOAD_STATUS_CANCEL = 3;
    /**
     * download error
     */
    public static final int UPLOAD_STATUS_ERROR = 4;
    /**
     * download finish
     */
    public static final int UPLOAD_STATUS_COMPLETED = 5;
    /**
     * download pause
     */
    public static final int UPLOAD_STATUS_PAUSE = 6;
    /**
     * download error file not found
     */
    public static final int UPLOAD_ERROR_FILE_NOT_FOUND = 7;
    /**
     * download error io exception
     */
    public static final int UPLOAD_ERROR_IO_ERROR = 8;

}
