package cn.hsfish.qingdao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SMSBroadcastReceiver extends BroadcastReceiver {
    private static MessageListener mMessageListener;
    public SMSBroadcastReceiver() {
        super();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Object [] pdus= (Object[]) intent.getExtras().get("pdus");
        for(Object pdu:pdus){
            SmsMessage smsMessage=SmsMessage.createFromPdu((byte [])pdu);
            String sender=smsMessage.getDisplayOriginatingAddress();
            String content=smsMessage.getMessageBody();
            long date=smsMessage.getTimestampMillis();
            Date timeDate=new Date(date);
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time=simpleDateFormat.format(timeDate);

            System.out.println("短信来自:"+sender);
            System.out.println("短信内容:"+content);
            System.out.println("短信时间:"+time);
            //短信应来自10010或106550010010
            if(sender.equals("106550010010") || sender.equals("10010") && content.contains("青啤活动")){
                mMessageListener.OnReceived(content);
            }
        }
    }
    // 回调接口
    public interface MessageListener {
        public void OnReceived(String message);
    }
    public void setOnReceivedMessageListener(MessageListener messageListener) {
        this.mMessageListener=messageListener;
    }
}
