package com.star.video.starrec.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.net.URLEncoder;
import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {
    /*
     * Function  :   发送Post请求到服务器
     * Param     :   params请求体内容，encode编码格式
     */
    public static String submitPostData(String strUrlPath,Map<String, String> params, String encode) {

        byte[] data = getRequestData(params, encode).toString().getBytes();//获得请求体
        try {

            //String urlPath = "http://192.168.1.9:80/JJKSms/RecSms.php";
            URL url = new URL(strUrlPath);

            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(3000);     //设置连接超时时间
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
            //设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            if(response == HttpURLConnection.HTTP_OK) {
                InputStream inptStream = httpURLConnection.getInputStream();
                return dealResponseResult(inptStream);                     //处理服务器的响应结果
            }
        } catch (IOException e) {
            //e.printStackTrace();
            return "err: " + e.getMessage().toString();
        }
        return "-1";
    }

    /*
     * Function  :   封装请求体信息
     * Param     :   params请求体内容，encode编码格式
     */
    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
        try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    /*
     * Function  :   处理服务器的响应结果（将输入流转化成字符串）
     * Param     :   inputStream服务器的响应输入流
     */
    public static String dealResponseResult(InputStream inputStream) {
        String resultData = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }

    public  static  String SdnPost(String path, Map<String, String> map) throws Exception {
        // 第一步 创建HttpClient
        HttpClient httpClient = new DefaultHttpClient();

        // 第三步 创建请求对象 Post
        HttpPost postRequest = new HttpPost(path);

        // 第六步 参数封装操作
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        for (Map.Entry<String, String> mapItem : map.entrySet()) {
            // 遍历Map集合里面的 key value  >>>  name=zhangsan    pwd=123456
            String key = mapItem.getKey();
            String value = mapItem.getValue();

            // 创建参数对象
            NameValuePair nameValuePair = new BasicNameValuePair(key, value);

            // 把参数对象放入List<NameValuePair>集合
            nameValuePairs.add(nameValuePair);
        }

        // 第五步 创建实体对象 传入参数>>>List<? extends NameValuePair>
        HttpEntity entity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");

        // 第四步 把请求的参数 放入实体
        postRequest.setEntity(entity);

        // 第二步 执行请求，获取响应对象
        HttpResponse response = httpClient.execute(postRequest);

        // 第七步 判断请求是否成功
        if (response.getStatusLine().getStatusCode() == 200) {

            // 第八步 获取响应的流
            InputStream inputStream = response.getEntity().getContent();

            byte[] bytes = new byte[1024];
            int len = 0;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            while (-1 != (len = inputStream.read())) {
                // 把存取到bytes的数据，写入到>>ByteArrayOutputStream
                bos.write(bytes, 0, len);
            }

            // 第九步 判断是否请求成功， 注意：⚠️ success 是自定义服务器返回的  success代表登录成功
            String strResult = bos.toString();
            // 第十步 关闭流
            inputStream.close();
            bos.close();
            return  strResult;

        } else {
            return  "{\"code\":500,\"msg\":\"网络错误\"}";
        }
    }

    /**
     * 通过okhtpp 请求 POST类型 API接口
     * @param url 接口地址
     * @param json JSON数据
     * @return
     * @throws IOException
     */
    public  static  String okHttpPost(String url, String json) throws IOException{
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON,json);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                return response.body().string();
            }
    }


}