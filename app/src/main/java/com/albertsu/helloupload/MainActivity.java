package com.albertsu.helloupload;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

        UploadUtil.getInstance().init(this);

        LinearLayoutManager mManager1 = new LinearLayoutManager(MainActivity.this);
        mManager1.setOrientation(LinearLayout.VERTICAL);
        goingRv = findViewById(R.id.rv_going);
        goingRv.setLayoutManager(mManager1);
        mGoingAdapter = new UploadAdapter(MainActivity.this);
        goingRv.setAdapter(mGoingAdapter);

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
                 List<UploadInfo> mData  = UploadUtil.getInstance().getAllUploadTasks();
                 mGoingAdapter.addNewData(mData);
            }
        });

        String[] permissions = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        TedPermission.with(this).setPermissionListener(new PermissionListener() {
            @Override
            public void onPermissionGranted() {

//                for (UploadInfo info:UploadUtil.getInstance().getAllUploadTasks()) {
//                    UploadUtil.getInstance().commitUploadTask("0", info.getDir()+"/"+info.getName(), "120.26.126.129", "16881");
//                }

                File[] files = new File("/storage/emulated/0/DCIM/Camera/").listFiles();
                for (int i=0;i<files.length;i++)
                {
                    File file = files[i];
                    if (file.getName().endsWith("mp4"))
                        UploadUtil.getInstance().commitUploadTask("0", file.getPath(), "120.26.126.129", "16881");
                }
//                UploadUtil.getInstance().commitUploadTask("0", files[1].getPath(), "120.26.126.129", "16881");

//                String filePath = "/storage/emulated/0/DCIM/Camera/IMG_20190109_173534.jpg";
//                UploadUtil.getInstance().commitUploadTask("0", filePath, "120.26.126.129", "16881");

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {

            }
        }).setPermissions(permissions).check();
    }

}
