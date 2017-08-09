package alerm.vpclub.com.testkotlin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Observable;

import alerm.vpclub.com.download.CallBack;
import alerm.vpclub.com.download.ExecutorDownLoadReactor;
import alerm.vpclub.com.download.StateInfo;

public class MainActivity extends AppCompatActivity implements CallBack {
    String url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502257434748&di=c6002154418af2e0cf86fc90d459cf21&imgtype=0&src=http%3A%2F%2Fpic76.nipic.com%2Ffile%2F20150826%2F12702443_165135194000_2.jpg";
      String TAG ="MainActivity";
    ProgressBar progressBar ;

    ImageView imageView;
    int pos ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        url = "https://download.plugins.jetbrains.com/6954/36603/kotlin-plugin-1.1.3-release-Studio2.3-2.zip?updateId=36603&pluginId=6954&uuid=d9744f72-c84e-4531-af47-7c4f06c52816&code=AI&build=162.2228.14";
        progressBar = (ProgressBar)findViewById(R.id.progressBar2);
        imageView  = (ImageView) findViewById(R.id.imageView);

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                pos = (int) (Math.random()*imges.length);
                pos++;
                pos = pos % imges.length;
                Log.d(TAG,"pos:" + pos);
                url = imges[pos];
                imageView.setImageBitmap(null);
                ExecutorDownLoadReactor.execute(url ,MainActivity.this);
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExecutorDownLoadReactor.stop(url);
            }
        });

        findViewById(R.id.restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setImageBitmap(null);
                ExecutorDownLoadReactor.execute(url,MainActivity.this,true);
            }
        });

        for (int i=0;i<1000;i++){
            ExecutorDownLoadReactor.execute(url,MainActivity.this,true);
        }

    }


    String[] imges = {
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502257434747&di=9e7a9423144e622bb25a98c92fe3686e&imgtype=0&src=http%3A%2F%2Fimg.taopic.com%2Fuploads%2Fallimg%2F120302%2F6444-12030215322171.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502257434747&di=d4e17bdb4d5cdbabec6f9fa33d1e321b&imgtype=0&src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2F5%2F5358e09ce4fd4.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502257434747&di=81823911b21456b2c0ff197a1c0ea397&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2Fd0c8a786c9177f3e1e09f02a7acf3bc79f3d56d3.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502257434747&di=25fddbd68098d383637ba4bfc61d6bdc&imgtype=0&src=http%3A%2F%2Fimg.tuku.cn%2Ffile_big%2F201504%2F8ac78034a89a4db09e7b2ff38c5020e5.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502257434747&di=279415ed8fcfae85f2d2dec52ef48a42&imgtype=0&src=http%3A%2F%2Fpic23.nipic.com%2F20120908%2F10639194_105138442151_2.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502257434746&di=9676961d0789e7540c0bcf6456d34177&imgtype=0&src=http%3A%2F%2Fimg.taopic.com%2Fuploads%2Fallimg%2F110919%2F2195-11091915294019.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502257434743&di=ef8110f4d3a56cb6e13b49e52dde981a&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2Ff9198618367adab4aa480a6781d4b31c8701e47e.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502257434743&di=5bc2a7b4be6ee0dfccbc72c6171437ae&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2F241f95cad1c8a7860ea6962d6d09c93d70cf5001.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502257434743&di=f47d7e7e523e5d7f5b994b5b21baab73&imgtype=0&src=http%3A%2F%2Fpic43.nipic.com%2F20140704%2F2531170_201939199000_2.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502257434743&di=b5cd8adafd35a22268f7b7afc24ab022&imgtype=0&src=http%3A%2F%2Fwww.pp3.cn%2Fuploads%2F201606%2F20160630011.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502257434742&di=9c19390da77846a601acf547aef9d25a&imgtype=0&src=http%3A%2F%2Fpic.58pic.com%2F58pic%2F13%2F75%2F00%2F66R58PICKvP_1024.jpg",

    };





    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof StateInfo) {
            final StateInfo info = (StateInfo) observable ;

            Log.d(TAG,"size =" + info.size + ",进度 =" + info.getProgress() +" 速度：" + info.getSpeed() +"Kb/s" + " 完成："+info.isComplete() + " error="+ info.isError() +" errorinfo=" +info.getErrorInfo());
            Log.d(TAG,"url ="+info.url);
            int pro = (int)(info.getProgress()*100);
            progressBar.setProgress(pro);

            if (info.isComplete()) {
                Log.d(TAG,"文件类型：" + info.type);
                Log.d(TAG,"文件后缀：" + info.extendName);
                Log.d(TAG,"文件地址：" + info.getFilePath());
                Log.d(TAG,"所花时间：" + (info.getEndTime() - info.getStartTime()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"下载成功"+info.errorInfo,Toast.LENGTH_LONG).show();
                        Bitmap bitmap = BitmapFactory.decodeFile(info.filePath);
                        imageView.setImageBitmap(bitmap);
                    }
                });


            }
            if (info.isError() || info.stop) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"下载失败",Toast.LENGTH_LONG).show();
                    }
                });


                Log.d(TAG, Thread.currentThread().getName()+"  size =" + info.size + ",进度 =" + info.getProgress() +" 速度：" + info.getSpeed() +"Kb/s" + " 完成："+info.isComplete() + " error="+ info.isError() +" errorinfo=" +info.getErrorInfo());
            }

        }

    }
}
