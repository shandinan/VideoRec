package com.star.video.starrec.video;

import java.io.File;

/**
 *  上传进程监听
 * @author
 *
 */
public interface UploadTaskListener {

    /**
     * 上传中
     * @param uploadTask
     * @param percent
     * @param position
     */
    void onUploading(UploadTask uploadTask, String percent, int position);

    /**
     * 上传成功
     * @param uploadTask
     * @param file
     */
    void onUploadSuccess(UploadTask uploadTask, File file);

    /**
     * 上传失败
     * @param uploadTask
     * @param errorCode
     * @param position
     */
    void onError(UploadTask uploadTask, int errorCode, int position);

    /**
     * 上传暂停
     * @param uploadTask
     */
    void onPause(UploadTask uploadTask);
}
