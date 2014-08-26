package com.chiemy.pulltoexpand;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {
	private String [] arr = {"a","b","c","d","e","f","g","h","i","j","k","l","m"};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ListView lv = (ListView) findViewById(R.id.pullExpandListView1);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item, R.id.text, arr);
		lv.setAdapter(adapter);
	}
	
}
