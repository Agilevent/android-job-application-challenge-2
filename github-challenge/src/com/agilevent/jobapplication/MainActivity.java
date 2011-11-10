package com.agilevent.jobapplication;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.agilevent.jobapplication.CommitModel.Commit;
import com.google.gson.Gson;

public class MainActivity extends FragmentActivity {

	private ListView list;
	private ProgressDialog mProgress = null;
	private CommitAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectAll().penaltyLog().penaltyDeath().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
				.penaltyLog().penaltyDeath().build());

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		list = (ListView) findViewById(android.R.id.list);
		mAdapter = new CommitAdapter(new ArrayList<Commit>());
		list.setAdapter(mAdapter);
		
		/* Look if there is already an loader fragment
		 * If not, create a new one to start the sync process */
		LoaderFragment loader = (LoaderFragment) getSupportFragmentManager().findFragmentByTag("loader");
		if (loader == null) {
			loader = LoaderFragment.newInstance("http://github.com/api/v2/json/commits/list/rails/rails/master");
			getSupportFragmentManager().beginTransaction().add(loader, "loader").commit();
		}

		if (!loader.isLoaded())
			mProgress = ProgressDialog.show(this, "Job Application", "Loading web page...");
		else {
			if (mProgress != null)
				mProgress.dismiss();
			mAdapter.addAllCompat(loader.getCommits());
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mProgress != null)
			mProgress.dismiss();
		mProgress = null;
	}

	public void onLoadFinished(CommitModel result) {
		if (mProgress != null)
			mProgress.dismiss();
		mAdapter.addAllCompat(result.commits);
		mAdapter.notifyDataSetChanged();
	}

	/* This is the fragment that actually do the load work
	 * I've done this way because I can retain an entire
	 * Fragment while rotating the device.
	 */
	public static class LoaderFragment extends Fragment {

		private boolean loaded = false;
		private MainActivity activity = null;
		private CommitModel commits;

		public static LoaderFragment newInstance(String url) {
			LoaderFragment f = new LoaderFragment();

			Bundle args = new Bundle();
			args.putString("url", url);
			f.setArguments(args);
			return f;
		}

		public boolean isLoaded() {
			return loaded;
		}

		public List<Commit> getCommits() {
			return commits.commits;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			// Keep this fragment on orientation changes...
			setRetainInstance(true);

			String url = getArguments().getString("url");
			asyncTask.execute(url);
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			this.activity = (MainActivity) activity;
		}

		@Override
		public void onDetach() {
			super.onDetach();
			this.activity = null;
		}

		private AsyncTask<String, Void, CommitModel> asyncTask = new AsyncTask<String, Void, CommitModel>() {

			protected CommitModel doInBackground(String... params) {
				// To up to github and get the most recent rails commits and
				// show
				// them to the screen.
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(
						"http://github.com/api/v2/json/commits/list/rails/rails/master");
				HttpResponse response = null;
				try {
					response = client.execute(get);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				CommitModel model = new CommitModel();
				if (response.getStatusLine().getStatusCode() == 200) {
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
					String json = new Scanner(stream).useDelimiter("\\A")
							.next();
					model = new Gson().fromJson(json, CommitModel.class);
				}
				return model;
			}

			@Override
			protected void onPostExecute(CommitModel result) {
				super.onPostExecute(result);
				commits = result;
				loaded = true;
				if (activity != null)
					activity.onLoadFinished(commits);
			}
		};
	}

	public class CommitAdapter extends ArrayAdapter<Commit> {

		List<Commit> commits;

		public CommitAdapter(List<Commit> commits) {
			super(MainActivity.this,
					android.R.layout.simple_list_item_2, commits);
			this.commits = commits;
		}

		public void addAllCompat(List<Commit> list) {
            Class<?> clazz = this.getClass();
            Method method = null;
            try {
                    method = clazz.getMethod("addAll", Collection.class);
            } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            } catch (NoSuchMethodException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }

            if (method != null) {
                    try {
                            method.invoke(this, list);
                            return;
                    } catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                    } catch (IllegalAccessException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                    } catch (InvocationTargetException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                    }
            }

            for (Commit commit : list)
                            commits.add(commit);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null)
				convertView = LayoutInflater.from(MainActivity.this).inflate(
						android.R.layout.simple_list_item_2, null);

			Commit commit = commits.get(position);

			TextView top = (TextView) convertView
					.findViewById(android.R.id.text1);
			TextView bottom = (TextView) convertView
					.findViewById(android.R.id.text2);

			top.setText(commit.author.name);
			bottom.setText(commit.message);

			return convertView;
		}
	};
}
