package com.agilevent.jobapplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.agilevent.jobapplication.CommitModel.Commit;
import com.google.gson.Gson;

public class MainActivity extends Activity {
    
	ListView list;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	        .detectAll()
	        .penaltyLog()
	        .penaltyDeath()
	        .build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	        .detectAll()
	        .penaltyLog()
	        .penaltyDeath()
	        .build());
    	
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        list = (ListView)findViewById(android.R.id.list); 
        
        // To up to github and get the most recent rails commits and show them to the screen. 
        DefaultHttpClient client = new DefaultHttpClient(); 
		HttpGet get = new HttpGet("http://github.com/api/v2/json/commits/list/rails/rails/master");
		HttpResponse response = null; 
		try {
			response = client.execute(get);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CommitModel model = new CommitModel();
		if(response.getStatusLine().getStatusCode() == 200) {
			InputStream stream = null;
			try {
				stream = response.getEntity().getContent();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String json = new Scanner(stream).useDelimiter("\\A").next();
			model = new Gson().fromJson(json, CommitModel.class); 
		}
        
		list.setAdapter(new CommitAdapter(model.commits)); 
		
     
    }
    
    public class CommitAdapter extends ArrayAdapter<Commit> {
    	
    	List<Commit> commits; 
    	
    	public CommitAdapter(List<Commit> commits) {
    		super(MainActivity.this, android.R.layout.simple_expandable_list_item_2, commits);
    		this.commits = commits; 
    	}
    	
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		
    		if(convertView == null) 
    			convertView = LayoutInflater.from(MainActivity.this).inflate(android.R.layout.simple_expandable_list_item_2, null);
    		
    		Commit commit = commits.get(position);
    		
    		TextView top = (TextView) convertView.findViewById(android.R.id.text1);
    		TextView bottom = (TextView) convertView.findViewById(android.R.id.text2);
    		
    		top.setText(commit.author.name); 
    		
    		
    		return convertView; 
    	}
    };
    
}