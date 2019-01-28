package com.albertsu.helloupload;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.wswin.util.upload.UploadInfo;
import cn.wswin.util.upload.UploadUtil;

public class MainActivity extends AppCompatActivity {

    RecyclerView goingRv;
    UploadAdapter mGoingAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_transfer);

        UploadUtil.getInstance().init(this,"hhh.db");

        LinearLayoutManager mManager1 = new LinearLayoutManager(MainActivity.this);
        mManager1.setOrientation(LinearLayout.VERTICAL);
        goingRv = findViewById(R.id.rv_going);
        goingRv.setLayoutManager(mManager1);
        mGoingAdapter = new UploadAdapter(MainActivity.this);
        goingRv.setAdapter(mGoingAdapter);
        mGoingAdapter.addNewData(UploadUtil.getInstance().getAllUploadTasks());

        findViewById(R.id.tv_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"开始",Toast.LENGTH_LONG).show();
                List<UploadInfo> infos = UploadUtil.getInstance().getAllUploadTasks();
                for (UploadInfo info:infos)
                    UploadUtil.getInstance().start(info);
            }
        });

        findViewById(R.id.tv_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"暂停",Toast.LENGTH_LONG).show();
                List<UploadInfo> infos = UploadUtil.getInstance().getAllUploadTasks();
                for (UploadInfo info:infos)
                    UploadUtil.getInstance().pause(info);
            }
        });

        findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"取消",Toast.LENGTH_LONG).show();
                List<UploadInfo> infos = UploadUtil.getInstance().getAllUploadTasks();
                for (UploadInfo info:infos)
                    UploadUtil.getInstance().stop(info);
            }
        });

        UploadUtil.getInstance().setListener(new UploadUtil.Listener() {
            @Override
            public void onChange(UploadInfo info) {
                 mGoingAdapter.addNewData(UploadUtil.getInstance().getAllUploadTasks());
            }
        });

//        UploadUtil.getInstance().setHandler(new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                mGoingAdapter.addNewData(UploadUtil.getInstance().getAllUploadTasks());
//                Log.d("setHandlersetHandler","00000");
//            }
//        });

        UploadUtil.getInstance().setOnGoingListener(new UploadUtil.OnGoingListener() {
            @Override
            public void onResult(int number) {
                Log.d("setOnGoingListener",""+number);
            }
        });

        String[] permissions = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        TedPermission.with(this).setPermissionListener(new PermissionListener() {
            @Override
            public void onPermissionGranted() {

                        final File[] files = new File("/storage/emulated/0/DCIM/Camera/").listFiles();
                        for (int i=0;i<files.length;i++) {
                            final File file = files[i];
                            if (file.getName().endsWith("jpg")) {
                                UploadUtil.getInstance().commitUploadTask(file.getPath(), "192.168.16.182", "81");
                            }
                        }
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {

            }
        }).setPermissions(permissions).check();
    }

}
