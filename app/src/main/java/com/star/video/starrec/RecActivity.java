package com.star.video.starrec;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.star.video.starrec.ftpUpload.FTP;
import com.star.video.starrec.utils.DateHelper;
import com.star.video.starrec.utils.HttpUtils;
import com.star.video.starrec.video.UploadManager;
import com.star.video.starrec.video.UploadTask;
import com.star.video.starrec.video.UploadTaskListener;

import java.io.File;
import java.io.IOException;

import static com.star.video.starrec.utils.MD5Helper.getFileMD5;

@TargetApi(19)
public class RecActivity extends SuperActivity implements
        SurfaceHolder.Callback, MediaRecorder.OnInfoListener, UploadTaskListener {
    private static final String TAG = "RecActivity";
    private SurfaceView mSurfaceview;
    private ImageButton mBtnStartStop;
    private ImageButton mBtnPlay;
    private ImageButton mBtnUpdateVideo;
    private boolean mStartedFlg = false;// 是否正在录像
    private boolean mIsPlay = false;// 是否正在播放录像
    private MediaRecorder mRecorder;
    private SurfaceHolder mSurfaceHolder;
    private ImageView mImageView;
    private Camera camera;
    private MediaPlayer mediaPlayer;
    private String path;
    private TextView recTimeView;
    private int time_second = 0; //录像时长 秒
    String hphm = ""; //号牌号码
    String hpzl = ""; //号牌种类
    String clsbdh="";//车辆识别代号
    String jylsh="";//检验流水号
    String vectype = ""; //检测类型
    String strServer_ip = ""; //上传服务IP
    String strServer_port = "";//上传服务端口
    String fieldPath = ""; //录像路径
    String strClsbdh = "";//车辆识别代号
    private ProgressDialog prodialog; //进度条弹出框
    private Boolean isUploadSucc;//是否上传完成
    private String fileName; // File name when saving
    private static String URL_Test_ID = "url_test";
    private UploadManager uploadManager; //上传进程管理
    private android.os.Handler handler = new android.os.Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            time_second++;
            recTimeView.setText(time_second + " 秒");
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sdnrecvideo);
        mSurfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        mImageView = (ImageView) findViewById(R.id.imageview);
        mBtnStartStop = (ImageButton) findViewById(R.id.btnStartStop);
        mBtnStartStop.setBackgroundResource(R.mipmap.start); //播放
        mBtnPlay = (ImageButton) findViewById(R.id.btnPlayVideo);
        mBtnPlay.setBackgroundResource(R.mipmap.play_b);
        mBtnPlay.setEnabled(false);//设置不可用
        mBtnUpdateVideo = (ImageButton) findViewById(R.id.btnUpdateVideo);// 上传视频
        mBtnUpdateVideo.setBackgroundResource(R.mipmap.up_b);
        mBtnUpdateVideo.setEnabled(false); //设置不可用
        recTimeView = (TextView) findViewById(R.id.rectime);
        // 开始结束按钮
        mBtnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsPlay) {
                    if (mediaPlayer != null) {
                        mIsPlay = false;
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
                if (!mStartedFlg) {
                    handler.postDelayed(runnable, 1000);
                    mImageView.setVisibility(View.GONE);
                    if (mRecorder == null) {
                        mRecorder = new MediaRecorder();
                        mRecorder.setOnInfoListener(RecActivity.this); // 设置摄像头事件监听
                    }
                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    if (camera != null) {
                        camera.setDisplayOrientation(90);
                        camera.unlock();
                        mRecorder.setCamera(camera);
                    }
                    try {
                        // 这两项需要放在setOutputFormat之前
                     //   mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                    //    mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                        //设置视频源
                       mRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
                        //设置音频源
                         mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);

                        // Set output file format
                         // mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                          // mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC); //he_aac 编码
                         //  mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); //h264编码

                        //设置文件输出格式
                        //   mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                        //  mRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
                        // //480p效果
                        //mRecorder.setProfile(CamcorderProfile);
                        // 这两项需要放在setOutputFormat之后
                        //  mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        //  mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        //  mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
                        //  mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); // H263的貌似有点不清晰
                        // mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                      CamcorderProfile cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                        mRecorder.setProfile(cProfile);
                       // mRecorder.setVideoSize(640, 480);
                      //  mRecorder.setVideoSize(1280,720);
                        mRecorder.setVideoFrameRate(30);
                        // mRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);
                      //  mRecorder.setVideoEncodingBitRate(900*1024);//较为清晰，且文件大小为3.26M(30秒)
                     //    mRecorder.setVideoSize(720,1280); // //较为清晰，且文件大小为3.26M(30秒)
                         mRecorder.setOrientationHint(90);
                        // 设置记录会话的最大持续时间（毫秒）
                        // mRecorder.setMaxDuration(15 * 1000);
                        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
                        // mRecorder.setOnInfoListener(this);
                        path = getSDPath();
                        if (path != null) {
                            if (hphm != null && !"".equals(hphm)) {
                                fieldPath = path + "/recordtest/" + hphm.substring(1, hphm.length() - 1) + "/";
                            } else {
                                fieldPath = path + "/recordtest/ESDNSD/";
                            }
                            File dir = new File(fieldPath + DateHelper.getDate());
                            //	File dir = new File("/sdcard/newcar" + "/recordvideo/"+ getDate());
                            if (!dir.exists()) {
                                boolean blres = dir.mkdirs();
                            }
                            path = dir + "/" + strClsbdh + ".mp4";
                            fileName = path; //设置全局文件路径
                            mRecorder.setOutputFile(path);
                            mRecorder.prepare();
                            mRecorder.start();
                            mStartedFlg = true;
                           // mBtnStartStop.setText("停止");
                            mBtnStartStop.setBackgroundResource(R.mipmap.stop);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else { //正在录像
                    // stop
                    if (mStartedFlg) {
                        try {
                            handler.removeCallbacks(runnable);
                            try {
                                //下面三个参数必须加，不加的话会奔溃，在mediarecorder.stop();
                                //报错为：RuntimeException:stop failed
                                mRecorder.setOnErrorListener(null);
                                mRecorder.setOnInfoListener(null);
                                mRecorder.setPreviewDisplay(null);
                                mRecorder.stop();
                            } catch (IllegalStateException e) {
                                // TODO: handle exception
                                Log.i("Exception", Log.getStackTraceString(e));
                            }catch (RuntimeException e) {
                                // TODO: handle exception
                                Log.i("Exception", Log.getStackTraceString(e));
                            }catch (Exception e) {
                                // TODO: handle exception
                                Log.i("Exception", Log.getStackTraceString(e));
                            }
                            mRecorder.reset();
                            mRecorder.release();
                            mRecorder = null;
                            mBtnStartStop.setBackgroundResource(R.mipmap.start);
                            if (camera != null) {
                                camera.release();
                                camera = null;
                            }
                            mBtnPlay.setEnabled(true); //回播按钮可用
                            mBtnPlay.setBackgroundResource(R.mipmap.play);
                            mBtnUpdateVideo.setEnabled(true);//上传按钮可用
                            mBtnUpdateVideo.setBackgroundResource(R.mipmap.up);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mStartedFlg = false;
                }
            }
        });

        // 播放按钮
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsPlay) { //未播放视频 开始播放视频
                    mIsPlay = true;
                 //   mBtnPlay.setText("停止");
                    mBtnPlay.setBackgroundResource(R.mipmap.play_stop);
                    mImageView.setVisibility(View.GONE);
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                    }
                    mediaPlayer.reset();
                    Uri uri = Uri.parse(path);
                    mediaPlayer = MediaPlayer.create(RecActivity.this, uri);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDisplay(mSurfaceHolder);
                    try {
                        mediaPlayer.prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.start(); //开始播放视频
                } else { //视频正在播放  停止播放
                    try {
                        if (mediaPlayer != null) {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                            }
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                        mIsPlay = false;
                    //    mBtnPlay.setText("回播");
                        mBtnPlay.setBackgroundResource(R.mipmap.play);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        // 上传视频文件
        mBtnUpdateVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowProgress();//显示进度条
                String uploadUrl = String.format("http://%s:%s/FileUpload/fileUploadMult", strServer_ip, strServer_port);
                //  new Thread(RecActivity.this).start();
                UploadTask task = new UploadTask.Builder()
                        .setId(URL_Test_ID)
                        .setUrl(uploadUrl).setChunck(1)
                        .setFileName(fileName)
                        .setHphm(hphm)
                        .setHpzl(hpzl)
                        .setClsbdh(clsbdh)
                        .setJylsh(jylsh)
                        .setZpzl(vectype) //上传照片种类
                        .setListener(RecActivity.this)
                        .build();
                uploadManager.addUploadTask(task);
            }
        });
        uploadManager = UploadManager.getInstance();//初始化 上传管理器
        SurfaceHolder holder = mSurfaceview.getHolder();
        holder.addCallback(this);
        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Intent localIntent = getIntent();
        hphm = localIntent.getStringExtra("hphm");
        hpzl = localIntent.getStringExtra("hpzl");
        strServer_ip = localIntent.getStringExtra("ip"); //获取传送过来的IP
        strServer_port = localIntent.getStringExtra("port"); //获取传送过来的端口
        clsbdh = localIntent.getStringExtra("clsbdh");//车辆识别代号
        jylsh = localIntent.getStringExtra("jylsh");//检验流水号
        vectype = localIntent.getStringExtra("zpzl"); //照片种类，这里当作拍照类型用
//        hphm="苏E11111";
//        hpzl="02";
//        strServer_ip="192.168.1.228";
//        strServer_port="8888";
//        clsbdh="LS4AAB3D86F001738";
//        jylsh="05803320031000053";
//        vectype="0113";
        //  strClsbdh = localIntent.getStringExtra("clsbdh");
        strClsbdh = clsbdh;
        // queue_id = localIntent.getStringExtra("queueid");
        isUploadSucc = false;
    }

    /**
     * 获取SD path
     *
     * @return
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory().getAbsoluteFile();// 获取跟目录
            return sdDir.getPath();
        }

        return null;
    }

    /**
     * 摄像头录像回调
     *
     * @param mr
     * @param what
     * @param extra
     */
    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            // Log.v("VIDEOCAPTURE", "Maximum Duration Reached");
            mRecorder.stop();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceview = null;
        mSurfaceHolder = null;
        handler.removeCallbacks(runnable);
        if (mRecorder != null) {
            mRecorder.release(); // Now the object cannot be reused
            mRecorder = null;
            Log.d(TAG, "surfaceDestroyed release mRecorder");
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void insertVideoInfo() {
        new Thread() {
            @Override
            public void run() {
                try {
                    HttpUtils.okHttpPost("", "json");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void FTPUpload(String filePath) {
        // 上传
        File file = new File(filePath);
        try {
            //单文件上传
            new FTP().uploadSingleFile(file, "/phoneVideo", new FTP.UploadProgressListener() {
                @Override
                public void onUploadProgress(String currentStep, long uploadSize, File file) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, currentStep);
                    if (currentStep.equals("ftp文件上传成功")) {
                        Log.d(TAG, "-----shanchuan--successful");
                    } else if (currentStep.equals("FTP上传中")) {
                        long fize = file.length();
                        float num = (float) uploadSize / (float) fize;
                        int result = (int) (num * 100);
                        Log.d(TAG, "-----upload---" + result + "%");
                    }
                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private void ShowProgress() {
        //新建ProgressDialog对象
        prodialog = new ProgressDialog(RecActivity.this);
        //设置显示风格
        prodialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //设置标题
        prodialog.setTitle("视频上传");
        //设置对话框文字信息
        prodialog.setMessage("视频上传中，请勿断网并耐心等待！");
        //设置图标
        prodialog.setIcon(R.mipmap.ic_launcher);
        /**
         * 设置关于ProgressBar属性
         */
        //设置最大进度
        prodialog.setMax(100);
        //设定初始化已经增长到的进度
        prodialog.incrementProgressBy(0);
        //进度条是明显显示进度的
        prodialog.setIndeterminate(false);
        /**
         * * 设定一个确定按钮
         * */
      /*  prodialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!isUploadSucc){
                    Toast.makeText(RecActivity.this, "上传中请等待……", Toast.LENGTH_LONG).show();
                }else {

                }
            }
        });*/
        //是否可以通过返回按钮退出对话框
        prodialog.setCancelable(false);

        //显示ProgressDialog
        prodialog.show();
    }

    @Override
    public void onUploading(UploadTask uploadTask, String percent, int position) {
        if (uploadTask.getId().equals(URL_Test_ID)) {
         //   mProgressBar.setProgress(Integer.parseInt(percent));
          //  mTvStatus.setText("正在下载..." + percent + "%");
            prodialog.setProgress(Integer.parseInt(percent));
        } else {
        //    mProgressBar.setProgress(Integer.parseInt(percent));
          //  mTvStatus.setText("正在上传..." + percent + "%");
            prodialog.setProgress(Integer.parseInt(percent));
        }
    }

    @Override
    public void onUploadSuccess(UploadTask uploadTask, File file) {
        if (uploadTask.getId().equals(URL_Test_ID)) {
            //mTvStatus.setText("上传完成 path：" + file.getAbsolutePath());
            prodialog.setMessage("视频成功，点击确定关闭！");
            Toast.makeText(RecActivity.this, "上传成功", Toast.LENGTH_LONG).show();
            prodialog.dismiss();
            try
            {
                file.delete();
                file.getAbsoluteFile().delete();//删除文件夹
                file=null;
                Thread.sleep(1000);
                Intent localIntent = new Intent();
                localIntent.putExtra("result", true); //取消
                setResult(1, localIntent);
                finish();
            }catch (Exception ex){

            }
        } else {
           // mTvStatus.setText("上传完成 path：" + file.getAbsolutePath());
            prodialog.setMessage("视频成功，点击确定关闭！");
            Toast.makeText(RecActivity.this, "上传成功", Toast.LENGTH_LONG).show();
            prodialog.dismiss();
            try
            {
                file.delete();
                file.getAbsoluteFile().delete();//删除文件夹
                file=null;
                Thread.sleep(1000);
                Intent localIntent = new Intent();
                localIntent.putExtra("result", true); //取消
                setResult(1, localIntent);
                finish();
            }catch (Exception ex){

            }
        }
    }

    @Override
    public void onError(UploadTask uploadTask, int errorCode, int position) {
        if (uploadTask.getId().equals(URL_Test_ID)) {
           // mTvStatus.setText("上传失败=" + errorCode);
            prodialog.setMessage("视频上传失败，点击确定关闭！");
            Toast.makeText(RecActivity.this, "失败", Toast.LENGTH_LONG).show();
            prodialog.dismiss();
        } else {
           // mTvStatus.setText("上传失败errorCode=" + errorCode);
            prodialog.setMessage("视频上传失败，点击确定关闭！");
            Toast.makeText(RecActivity.this, "失败", Toast.LENGTH_LONG).show();
            prodialog.dismiss();
        }
    }

    @Override
    public void onPause(UploadTask uploadTask) {
        if (uploadTask.getId().equals(URL_Test_ID)) {
          //  mTvStatus.setText("上传暂停！");
        } else {
          //  mTvStatus.setText("上传暂停！");
        }
    }
}
