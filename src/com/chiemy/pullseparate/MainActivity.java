package com.chiemy.pullseparate;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnCheckedChangeListener{
	private String [] arr = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","g","h","i","j","k","l","m","n","o","p","q"};
	private PullSeparateListView lv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lv = (PullSeparateListView) findViewById(R.id.pullExpandListView1);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item, R.id.text, arr);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(MainActivity.this, arr[position], Toast.LENGTH_SHORT).show();
			}
		});
		//View header = LayoutInflater.from(this).inflate(R.layout.header_view, null);
		//lv.addHeaderView(header);
		
		CheckBox cb1 = (CheckBox) findViewById(R.id.is_separateAll_cb);
		CheckBox cb2 = (CheckBox) findViewById(R.id.is_down_anim_cb);
		cb1.setChecked(lv.isSeparateAll());
		cb2.setChecked(lv.isShowDownAnim());
		cb1.setOnCheckedChangeListener(this);
		cb2.setOnCheckedChangeListener(this);
	}
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId()){
		case R.id.is_separateAll_cb:
			lv.setSeparateAll(isChecked);
			break;
		case R.id.is_down_anim_cb:
			lv.setShowDownAnim(isChecked);
			break;
		}
	}
	
}
