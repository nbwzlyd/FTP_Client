package ftpcom.lyd.ftpandroidclient.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;



import com.socket.ftpdome.adapter.FileListAdapter;

import java.io.IOException;

import ftpcom.lyd.ftpandroidclient.R;


public class FileListActivity extends AppCompatActivity {


    private TextView mUpFolderView;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        int  drawable = Color.parseColor("#1590D7");
        ColorDrawable actionbarD = new ColorDrawable(drawable);
        getSupportActionBar().setTitle("ftp传输工具");
        getSupportActionBar().setBackgroundDrawable(actionbarD);
        try {
            initView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initView() throws IOException {
        mUpFolderView = (TextView) findViewById(R.id.up_folder);
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new FileListAdapter());
    }
}
