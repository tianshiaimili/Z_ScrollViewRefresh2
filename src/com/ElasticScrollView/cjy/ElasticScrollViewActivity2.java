package com.ElasticScrollView.cjy;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.ElasticScrollView.view.ElasticScrollView2;
import com.ElasticScrollView.view.ElasticScrollView2.OnRefreshListener2;
import com.ElasticScrollView.view.LogUtils2;

public class ElasticScrollViewActivity2 extends Activity {
	//����view
	
	ElasticScrollView2 elasticScrollView;
	int count;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);
        elasticScrollView = (ElasticScrollView2)findViewById(R.id.scrollview2);
        for(int i=1;i<=50;i++){
			TextView tempTextView = new TextView(this);
			tempTextView.setText("Text:" + i);
			elasticScrollView.addChild(tempTextView,1);
		}
        
        final Handler handler = new Handler() {
        	public void handleMessage(Message message) {
        		String str = (String)message.obj;
        		OnReceiveData(str);
        	}
        };
        elasticScrollView.setonRefreshListener(new OnRefreshListener2() {
			
			@Override
			public void onRefresh() {
				LogUtils2.i("onRefresh**************");
				Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Message message = handler.obtainMessage(0, "new Text"+(count++));
						handler.sendMessage(message);
					}
				});
				thread.start();
			}
		});
    }

	protected void OnReceiveData(String str) {
		TextView textView =  new TextView(this);
		textView.setText(str);
		elasticScrollView.addChild(textView, 1);
		elasticScrollView.onRefreshComplete();
	}
}