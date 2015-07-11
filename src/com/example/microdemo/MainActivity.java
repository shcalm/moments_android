package com.example.microdemo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;

import com.example.microdemo.adapter.MyListAdapter;
import com.example.microdemo.custonListView.CustomListView;
import com.example.microdemo.custonListView.CustomListView.OnLoadMoreListener;
import com.example.microdemo.custonListView.CustomListView.OnRefreshListener;
import com.example.microdemo.domain.FirendMicroList;
import com.example.microdemo.domain.FirendMicroListDatas;
import com.example.microdemo.domain.FirendsMicro;
import com.example.microdemo.domain.OwnerMicro;
import com.example.microdemo.util.FastjsonUtil;
import com.example.testpic.PublishedActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private static final String TAG = "MicroActivity";
	private String uid = "";
	private String companykey = "";
	int now = 0;
	int count = 1;
	private String strIcon = "";
	List<FirendMicroListDatas> listdatas = new ArrayList<FirendMicroListDatas>();
	
	private View header;
	public CustomListView listview;
	public ImageButton selectpic;
	public MyListAdapter mAdapter;
	public OwnerMicro ownerdata;
	private ImageView MicroIcon;
	String res = "";
	String ownerres = "";
	static String encoding = null;
	static {
		encoding = Base64.encodeToString(new String(
		        "123:e10adc3949ba59abbe56e057f20f883e").getBytes(),
		        Base64.NO_WRAP);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try {

			InputStream owner = getResources().openRawResource(R.raw.ownerjson);
			int ownerlength = owner.available();
			byte[] ownerbuffer = new byte[ownerlength];
			owner.read(ownerbuffer);
			ownerres = EncodingUtils.getString(ownerbuffer, "UTF-8");
			owner.close();

			InputStream in = getResources().openRawResource(R.raw.json);

			int length = in.available();

			byte[] buffer = new byte[length];

			in.read(buffer);

			res = EncodingUtils.getString(buffer, "UTF-8");

			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		init();
	}

	private void init() {

		selectpic = (ImageButton) findViewById(R.id.ib_right);
		selectpic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {

				Intent intent = new Intent(MainActivity.this,
				        PublishedActivity.class);
				startActivity(intent);
			}
		});

		header = LayoutInflater.from(this).inflate(R.layout.micro_list_header,
		        null);
		MicroIcon = (ImageView) header.findViewById(R.id.MicroIcon);
		listview = (CustomListView) findViewById(R.id.list);
		listview.setVerticalScrollBarEnabled(false);
		listview.setDivider(this.getResources().getDrawable(R.drawable.h_line));
		listview.addHeaderView(header);

		mAdapter = new MyListAdapter(this, listdatas);
		listview.setAdapter(mAdapter);
		getOwnerList();
		getMicroList(1, true);

		listview.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
				String s = "下拉刷新";
				getData(s);
			}

		});

		listview.setOnLoadListener(new OnLoadMoreListener() {

			public void onLoadMore() {
				String s = "上拉加载更多";
				getData(s);

			}
		});
	}

	private void getOwnerList() {
		if (TextUtils.isEmpty(ownerres)) {
			return;
		}

		ownerdata = FastjsonUtil.json2object(ownerres, OwnerMicro.class);
	}

	private void getMicroList(final int i, boolean has) {
		if (TextUtils.isEmpty(res)) {
			return;
		}
		new AsyncTask<String, Void, String>() {
			@Override
			protected void onPostExecute(String result) {
				int firstpage = 1;
				FirendsMicro fm = FastjsonUtil.json2object(result,
				        FirendsMicro.class);
				FirendMicroList fList = fm.getFriendPager();

				{

					if (i == firstpage) {
						listdatas.clear();
						count = 1;
					}

					if (null == fList.getDatas()
					        || fList.getDatas().size() == 0) {
						if (i == firstpage) {
							listview.onRefreshComplete();
						} else {
							listview.onLoadMoreComplete(false);
						}
					} else {
						if (i == firstpage) {
							listview.onRefreshComplete();
						} else {
							listview.onLoadMoreComplete();
						}
						listdatas.addAll(fList.getDatas());
						count++;
					}
					int k = listdatas.size();
					now = k > 0 ? k - 1 : 0;
					mAdapter.notifyDataSetChanged();
				}

			}

			@Override
			protected String doInBackground(String... params) {
				HttpClient client = new DefaultHttpClient();

				HttpGet httpget = new HttpGet(params[0]);
				// HttpPost httppost = new
				// HttpPost("http://moments.daoapp.io/api/v1.0/posts/");
				httpget.setHeader("user-agent",
				        "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0");
				httpget.setHeader("Authorization", "Basic " + encoding);

				String result = null;
				try {
					HttpResponse response = client.execute(httpget);
					result = EntityUtils.toString(response.getEntity());
					Log.d("====", "result = " + result);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return result;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
		        "http://moments.daoapp.io/api/v1.0/posts/?page="+i);

	}

	private void getData(String s) {

		if ("下拉刷新".equals(s)) {

			getMicroList(1, true);

			listview.onRefreshComplete();
		} else {
			getMicroList(count, true);

			listview.onLoadMoreComplete(); // 加载更多完成
		}
	}

	public void LoadList() {
		getMicroList(1, true);
	}
}
