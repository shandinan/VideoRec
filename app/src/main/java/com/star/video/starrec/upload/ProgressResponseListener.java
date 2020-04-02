package com.star.video.starrec.upload;

/**
 * 包装的响体，处理进度
 */
public interface ProgressResponseListener {
    void   onResponseProgress(long bytesRead, long contentLength,boolean done);
}