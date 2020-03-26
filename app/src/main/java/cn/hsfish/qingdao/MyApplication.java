package cn.hsfish.qingdao;

import android.app.Application;

import com.tamsiree.rxkit.RxTool;

public class MyApplication extends Application {
    private static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        RxTool.init(this.getApplicationContext());
    }

    public static MyApplication getInstance(){
        return myApplication;
    }
}
