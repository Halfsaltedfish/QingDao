package cn.hsfish.qingdao;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tamsiree.rxkit.RxPermissionsTool;
import com.tamsiree.rxkit.RxRegTool;
import com.tamsiree.rxkit.RxSPTool;
import com.tamsiree.rxkit.RxTool;
import com.tamsiree.rxkit.view.RxToast;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static TextView textView;
    private SMSBroadcastReceiver mSMSBroadcastReceiver;
    private Button button;
    private EditText editText;
    private RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    private void init(){
        textView  = (TextView)findViewById(R.id.text);
        mSMSBroadcastReceiver=new SMSBroadcastReceiver();
        button = (Button)findViewById(R.id.button);
        editText = (EditText)findViewById(R.id.phone);
        editText.setText(RxSPTool.getContent(MyApplication.getInstance(),"phone"));
        queue = Volley.newRequestQueue(this);
        RxPermissionsTool.
                with((Activity) MainActivity.this)
                .addPermission(Manifest.permission.READ_SMS)
                .addPermission(Manifest.permission.RECEIVE_SMS)
                .addPermission(Manifest.permission.BROADCAST_SMS)
                .initPermission();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = editText.getText().toString();
                if(!RxRegTool.isMobileSimple(phone)){
                    RxToast.error("请输入正确的手机号");
                    return;
                }
                RxSPTool.putString(MyApplication.getInstance(),"phone",phone);
                getCode(phone);
            }
        });

        mSMSBroadcastReceiver.setOnReceivedMessageListener(new SMSBroadcastReceiver.MessageListener() {
            public void OnReceived(String message) {
                receiveFlow(editText.getText().toString(),getDynamicPassword(message));
            }
        });
    }
    /**
     * 从字符串中截取连续6位数字组合 ([0-9]{" + 4 + "})截取四位数字 进行前后断言不能出现数字 用于从短信中获取动态密码
     *
     * @param str 短信内容
     * @return 截取得到的4位动态密码
     */
    public String getDynamicPassword(String str) {
        // 4是验证码的位数
        Pattern continuousNumberPattern = Pattern.compile("(?<![0-9])([0-9]{"
                + 4 + "})(?![0-9])");
        Matcher m = continuousNumberPattern.matcher(str);
        String dynamicPassword = "";
        while (m.find()) {
            dynamicPassword = m.group();
            System.out.print("验证码为：" + dynamicPassword);
            textView.append("验证码为：" + dynamicPassword);
        }
        return dynamicPassword;
    }

    /**
     * 获取验证码
     * @param phone 手机号
     */
    public void getCode(final String phone){
        String url ="https://www.iheit.com/ll/get";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("1")){
                            //ok
                            RxToast.success("获取验证码成功");
                            CountDownTimer timer = new CountDownTimer(60000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    button.setEnabled(false);
                                    button.setText("已发送(" + millisUntilFinished / 1000 + ")");
                                }

                                @Override
                                public void onFinish() {
                                    button.setEnabled(true);
                                    button.setText("再次领取");
                                }
                            }.start();
                        }else if (response.equals("-1")){
                            RxToast.error("获取验证码失败");
                        }else if (response.equals("0")){
                            RxToast.error("该号码不是联通号码");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                RxToast.error("网络请求失败");
                textView.append("网络请求失败!");
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("phone", phone);
                return map;
            }
        };
        queue.add(stringRequest);
    }

    /**
     * 领取流量
     * @param phone 手机号
     * @param code 验证码
     */
    private void receiveFlow(final String phone, final String code){
        String url ="https://www.iheit.com/ll/verify";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("true")){
                            //ok
                            RxToast.success("领取成功");
                            textView.append("领取成功");
                        }else{
                            RxToast.error("领取失败");
                            textView.append("领取失败");
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                RxToast.error("网络请求失败");
                textView.append("网络请求失败!");
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("phone", phone);
                map.put("captcha", code);
                return map;
            }
        };
        queue.add(stringRequest);
    }
}
