package com.star.video.starrec.video;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.star.video.starrec.utils.CommandHelper;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 上传线程
 *
 * @author
 * @date 2019/12/23
 */
public class UploadTask implements Runnable {
    private static final String TAG = "UploadTask";
    private static String FILE_MODE = "rwd";
    private OkHttpClient mClient;
    private UploadTaskListener mListener;

    private Builder mBuilder;
    private String id; // task id
    private String url; // file url
    private String fileName; // File name when saving
    private int uploadStatus;
    private int chunck, chuncks; //流块
    private int position;
    private String hphm;//号牌号码
    private String hpzl;//号牌种类
    private String clsbdh;
    private String jylsh;
    private String zpzl;

    private int errorCode;
    static String BOUNDARY = "----------" + System.currentTimeMillis();
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("multipart/form-data;boundary=" + BOUNDARY);

    public UploadTask(Builder builder) {
        mBuilder = builder;
        mClient = new OkHttpClient();
        this.id = mBuilder.id;
        this.url = mBuilder.url;
        this.fileName = mBuilder.fileName;
        this.uploadStatus = mBuilder.uploadStatus;
        this.chunck = mBuilder.chunck;
        this.hphm = mBuilder.hphm;
        this.hpzl = mBuilder.hpzl;
        this.clsbdh = mBuilder.clsbdh;
        this.jylsh = mBuilder.jylsh;
        this.zpzl = mBuilder.zpzl;
        this.setmListener(mBuilder.listener);
        // 以kb为计算单位
    }

    @Override
    public void run() {
        try {
            int blockLength = 1024 * 1024;
            //long lastblockLength = 1024 * 1024;
            //  File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileName);
            File file = new File(fileName);
            //String md5 = getFileMD5(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileName);
            String md5 = getFileMD5(file);
            if (file.length() % blockLength == 0) { // 算出总块数
                chuncks = (int) file.length() / blockLength;
            } else {
                chuncks = (int) file.length() / blockLength + 1;
            }
            //lastblockLength = file.length() / blockLength;

            Log.i(TAG, "chuncks =" + chuncks + "fileName =" + fileName + "uploadStatus =" + uploadStatus);
            Log.i(TAG, "chunck =" + chunck);
            Log.i(TAG, "md5 =" + md5);
            //Log.i(TAG,"lastblockLength =" +lastblockLength);
            String eid = null;
            try {
                eid = CommandHelper.getMMCId();
                Log.i(TAG, "eid =" + eid);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            while (chunck <= chuncks
                    && uploadStatus != UploadStatus.UPLOAD_STATUS_PAUSE
                    && uploadStatus != UploadStatus.UPLOAD_STATUS_ERROR) {
                uploadStatus = UploadStatus.UPLOAD_STATUS_UPLOADING;
                Map<String, String> params = new HashMap<String, String>();
                params.put("filename", fileName);
                params.put("md5", md5);
                params.put("chunks", chuncks + "");
                params.put("chunk", chunck + "");
                params.put("size", blockLength + "");
                params.put("eid", eid);
                params.put("hphm", hphm);
                params.put("hpzl", hpzl);
                Log.i(TAG, "chunck =" + chunck + "chuncks =" + chuncks);
                final byte[] mBlock = FileUtils.getBlock((chunck - 1) * blockLength, file, blockLength);
                Log.i(TAG, "mBlock == " + mBlock.length);
                // 生成RequestBody
                MultipartBody.Builder builder = new MultipartBody.Builder();
                addParams(builder, params);
                String fileType = "file/*";
                RequestBody requestBody = RequestBody.create(
                        MediaType.parse(fileType), mBlock);
                builder.addFormDataPart("sdnvideo", fileName, requestBody);
                Log.i(TAG, "url =" + url);

                //获得Request实例
                Request request = new Request.Builder()
                        .url(url)
                        .post(builder.build())
                        .build();
                Log.i(TAG, "RequestBody execute~");

                Response response = null;
                response = mClient.newCall(request).execute();
                Log.i(TAG, "isSuccessful =" + response.isSuccessful());
                if (response.isSuccessful()) {
                    String ret = response.body().string();
                    Log.d(TAG, "uploadVideo  UploadTask ret:" + ret);
                    chunck++;
                    if (chunck > chuncks) { //上传完成
                        uploadStatus = UploadStatus.UPLOAD_STATUS_COMPLETED;//
                    }
                    onCallBack();
                } else {
                    uploadStatus = UploadStatus.UPLOAD_STATUS_ERROR;
                    onCallBack();
                }
            }
        } catch (IOException e) {
            Log.i(TAG, "run IOException");
            uploadStatus = UploadStatus.UPLOAD_STATUS_ERROR;
            onCallBack();
            Log.i(TAG, "e error: =" + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 更加文件路径生成唯一的MD5值
     *
     * @param file
     * @return
     */
    public String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    /**
     * 分发回调事件到UI层
     */
    private void onCallBack() {
        mHandler.sendEmptyMessage(uploadStatus);
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            int code = msg.what;
            switch (code) {
                // 上传失败
                case UploadStatus.UPLOAD_STATUS_ERROR:
                    mListener.onError(UploadTask.this, errorCode, position);
                    break;
                // 正在上传
                case UploadStatus.UPLOAD_STATUS_UPLOADING:
                    mListener.onUploading(UploadTask.this, getDownLoadPercent(), position);
                    break;
                // 暂停上传
                case UploadStatus.UPLOAD_STATUS_PAUSE:
                    mListener.onPause(UploadTask.this);
                    break;
                case UploadStatus.UPLOAD_STATUS_COMPLETED:
                    File file = new File(fileName);
                    mListener.onUploadSuccess(UploadTask.this, file);
                    break;
            }
        }

        ;
    };

    private String getDownLoadPercent() {
        String percentage = "0"; // 接收百分比得值
        if (chunck >= chuncks) {
            return "100";
        }

        double baiy = chunck * 1.0;
        double baiz = chuncks * 1.0;
        // 防止分母为0出现NoN
        if (baiz > 0) {
            double fen = (baiy / baiz) * 100;
            //NumberFormat nf = NumberFormat.getPercentInstance();
            //nf.setMinimumFractionDigits(2); //保留到小数点后几位
            // 百分比格式，后面不足2位的用0补齐
            //baifenbi = nf.format(fen);
            //注释掉的也是一种方法
            DecimalFormat df1 = new DecimalFormat("0");//0.00
            percentage = df1.format(fen);
        }
        return percentage;
    }

    private String getFileNameFromUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        return System.currentTimeMillis() + "";
    }

    private void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setClient(OkHttpClient mClient) {
        this.mClient = mClient;
    }

    public Builder getBuilder() {
        return mBuilder;
    }

    public void setBuilder(Builder builder) {
        this.mBuilder = builder;
    }

    public String getId() {
        if (!TextUtils.isEmpty(id)) {
        } else {
            id = url;
        }
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getHphm() {
        return hphm;
    }

    public void setHphm(String hphm) {
        this.hphm = hphm;
    }

    public String getHpzl() {
        return hpzl;
    }

    public void setHpzl(String hpzl) {
        this.hpzl = hpzl;
    }

    public String getClsbdh() {
        return clsbdh;
    }

    public String getJylsh() {
        return jylsh;
    }

    public String getZpzl() {
        return zpzl;
    }

    public void setZpzl(String zpzl) {
        this.zpzl = zpzl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setUploadStatus(int uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public int getUploadStatus() {
        return uploadStatus;
    }

    public void setmListener(UploadTaskListener mListener) {
        this.mListener = mListener;
    }

    public static class Builder {
        private String id; // task id
        private String url; // file url
        private String fileName; // File name when saving
        private int uploadStatus = UploadStatus.UPLOAD_STATUS_INIT;
        private int chunck; // 第几块
        private UploadTaskListener listener;
        private String hphm;//号牌号码
        private String hpzl;//号牌种类
        private String clsbdh;
        private String jylsh;
        private String zpzl;//车辆识别代号 检验流水号

        /**
         * 作为上传task开始、删除、停止的key值，如果为空则默认是url
         *
         * @param id
         * @return
         */
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * 上传url（not null）
         *
         * @param url
         * @return
         */
        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        /**
         * 设置上传状态
         *
         * @param uploadStatus
         * @return
         */
        public Builder setUploadStatus(int uploadStatus) {
            this.uploadStatus = uploadStatus;
            return this;
        }

        /**
         * 第几块
         *
         * @param chunck
         * @return
         */
        public Builder setChunck(int chunck) {
            this.chunck = chunck;
            return this;
        }


        /**
         * 设置文件名
         *
         * @param fileName
         * @return
         */
        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        /**
         * 号牌号码
         *
         * @param hphm
         * @return
         */
        public Builder setHphm(String hphm) {
            this.hphm = hphm;
            return this;
        }

        public Builder setHpzl(String hpzl) {
            this.hpzl = hpzl;
            return this;
        }

        /**
         * 车辆识别代号
         *
         * @param _clsbdh
         * @return
         */
        public Builder setClsbdh(String _clsbdh) {
            this.clsbdh = _clsbdh;
            return this;
        }

        /**
         * 检验流水号
         *
         * @param _jylsh
         * @return
         */
        public Builder setJylsh(String _jylsh) {
            this.jylsh = _jylsh;
            return this;
        }

        /**
         * 照片种类
         * @param _zpzl
         * @return
         */
        public Builder setZpzl(String _zpzl){
            this.zpzl = _zpzl;
            return this;
        }

        /**
         * 设置上传回调
         *
         * @param listener
         * @return
         */
        public Builder setListener(UploadTaskListener listener) {
            this.listener = listener;
            return this;
        }

        public UploadTask build() {
            return new UploadTask(this);
        }
    }

    private void addParams(MultipartBody.Builder builder,
                           Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                builder.addPart(
                        Headers.of("Content-Disposition", "form-data; name=\""
                                + key + "\""),
                        RequestBody.create(null, params.get(key)));
            }
        }
    }
}
