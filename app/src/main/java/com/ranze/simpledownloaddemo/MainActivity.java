package com.ranze.simpledownloaddemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ranze.simpledownloaddemo.core.DownloadListener;
import com.ranze.simpledownloaddemo.core.Downloader;
import com.ranze.simpledownloaddemo.core.Task;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private TextView mFilePath;
    private TextView mUrl;
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    private Button mDownloadBtn;
    private Button mPauseBtn;
    private Button mCancelBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFilePath = findViewById(R.id.text_view_filename);
        mUrl = findViewById(R.id.text_view_url);
        mProgressBar = findViewById(R.id.progress_bar);
        mProgressText = findViewById(R.id.progress_text);
        mDownloadBtn = findViewById(R.id.download);
        mPauseBtn = findViewById(R.id.pause);
        mCancelBtn = findViewById(R.id.cancel);

        mProgressBar.setMax(100);

        String url = "http://gdown.baidu.com/data/wisegame/bfdbb009e43be130/shoujibaidu_42480896.apk";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "SimpleDownloadDemo";
        String fileName = "demo.apk";

        String path = "保存路径： " + filePath + File.separator + fileName;
        mFilePath.setText(path);
        mUrl.setText("url: " + url);

        SimpleDownloadClient client = new SimpleDownloadClient.Builder()
                .context(getApplicationContext())
                .debuggable(true)
                .build();

        Task task = new Task.Builder()
                .url(url)
                .savedPath(filePath)
                .fileName(fileName)
                .build();

        Downloader downloader = client.newDownloader(task);

        mDownloadBtn.setOnClickListener((view) -> downloader.start(new DownloadListener() {
            @Override
            public void onStart() {
                Toast.makeText(MainActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPause() {
                Toast.makeText(MainActivity.this, "已暂停", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                Toast.makeText(MainActivity.this, "已完成", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable t) {
                Toast.makeText(MainActivity.this, "下载出错", Toast.LENGTH_SHORT).show();
                Log.d("test", t.toString());
            }

            @Override
            public void onProgress(int progress) {
                mProgressBar.setProgress(progress);
                mProgressText.setText(progress + " %");
            }
        }));

        mPauseBtn.setOnClickListener((view) -> downloader.pause());

        mCancelBtn.setOnClickListener((view) -> {
            downloader.cancel();
            mProgressBar.setProgress(0);
            mProgressText.setText("0 %");
        });

        verifyStoragePermissions(this);

    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
