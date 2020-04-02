package com.star.video.starrec.upload;

import android.content.Intent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class test {

    private OkHttpClient client;//ok http



    //设置超时，不设置可能会报异常
    private void initClient() {
        client = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).writeTimeout(20, TimeUnit.SECONDS).build();
    }
    /**
     * 文件上传
     *
     * @param url
     * @param filePath
     */
    private void upload(String url, String filePath) throws InterruptedException {
        File file = new File(filePath);

        //这个是ui线程回调，可直接操作UI
        final UIProgressRequestListener uiProgressRequestListener = new UIProgressRequestListener() {
            @Override
            public void onUIRequestProgress(long bytesWrite, long contentLength, boolean done) {
                //   Log.e("TAG", "bytesWrite:" + bytesWrite);
                //   Log.e("TAG", "contentLength" + contentLength);
                //   Log.e("TAG", (100 * bytesWrite) / contentLength + " % done ");
                //   Log.e("TAG", "done:" + done);
                //   Log.e("TAG", "================================");
                //ui层回调

                if (done) {
                    //  prodialog.cancel();
                    //     int iProgress =Integer.parseInt(getDownLoadPercent());
                 //   chunck++;

                    int iProgress = (int) ((100 * bytesWrite) / contentLength);//换算成100分制的上传进度
                 //   prodialog.setProgress(iProgress);
                } else {
                    //  int iProgress = (int) ((100 * bytesWrite) / contentLength);//换算成100分制的上传进度
                    //  prodialog.setProgress(iProgress);
                }
            }
        };

        try {
            RequestBody requestBody = RequestBody.create(MediaType.parse("file/*"), file); //创建requestBody对象
            MultipartBody multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("sdnvideo", file.getName(), requestBody)//后端接收关键字
                    .addFormDataPart("md5", "")
                  //  .addFormDataPart("hphm", hphm) //号牌号码
                 //   .addFormDataPart("hpzl", hpzl)  //号牌种类
                    // .addFormDataPart("size", blockLength + "")
                 //   .addFormDataPart("chunks", chuncks + "")
                //    .addFormDataPart("chunk", chunck + "")
                    //.addFormDataPart("file")
                    .build();//如果还需传递其他字段调用此方法传递
            //进行包装，使其支持进度回调
            final Request request = new Request.Builder().url(url).post(ProgressHelper.addProgressRequestListener(multipartBody, uiProgressRequestListener)).build();
            //开始请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Log.e("TAG", "error ", e);
                    // prodialog.cancel();
                    //  prodialog=null;

                 //   uploadStatus = UploadStatus.UPLOAD_STATUS_ERROR;
                    //   Intent localIntent = new Intent();
                    //   localIntent.putExtra("result", false); //取消
                    //  setResult(1, localIntent);
                    //  finish();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //   Log.e("TAG", response.body().string());
                //    uploadStatus = UploadStatus.UPLOAD_STATUS_SUCC;//成功
                    // if(chunck>=chuncks){
                    //     prodialog.cancel();
                    //    prodialog=null;
                    Intent localIntent = new Intent();
                    localIntent.putExtra("result", true); //取消
                //    setResult(1, localIntent);
               //     finish();
                    //  }

                }
            });

            // mBlock=null;
            //     System.gc();//强制垃圾回收
            //Thread.sleep(1000); //while停止1秒
        } catch (Exception ex) {
            throw(ex);
        }

    }


}
