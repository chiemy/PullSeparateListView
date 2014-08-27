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

public class MainActivity extends Activity {
	private String [] arr = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","g","h","i","j","k","l","m","n","o","p","q"};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final PullSeparateListView lv = (PullSeparateListView) findViewById(R.id.pullExpandListView1);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item, R.id.text, arr);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(MainActivity.this, arr[position], Toast.LENGTH_SHORT).show();
			}
		});
		View header = LayoutInflater.from(this).inflate(R.layout.header_view, null);
		//lv.addHeaderView(header);
		
		CheckBox cb = (CheckBox) findViewById(R.id.checkBox);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				lv.setSeparateAll(isChecked);
			}
		});
	}
	
}
