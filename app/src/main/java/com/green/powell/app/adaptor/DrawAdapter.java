package com.green.powell.app.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.green.powell.app.R;

import java.util.ArrayList;
import java.util.HashMap;


public class DrawAdapter extends BaseAdapter{

	private LayoutInflater inflater;
	private ArrayList<HashMap<String,Object>> boardList;
	private ViewHolder viewHolder;
	private Context con;
	private String title;


	public DrawAdapter(Context con , ArrayList<HashMap<String,Object>> array, String title){
		inflater = LayoutInflater.from(con);
		boardList = array;
		this.title = title;
	}

	@Override
	public int getCount() {
		return boardList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int position, final View convertview, ViewGroup parent) {

		View v = convertview;

		if(v == null){
			viewHolder = new ViewHolder();

			if(title.equals("Draw")){
				v = inflater.inflate(R.layout.map_list_item, parent,false);
			}else{
				v = inflater.inflate(R.layout.msds_list_item, parent,false);
			}
			viewHolder.board_data1 = (TextView)v.findViewById(R.id.textView1);
			viewHolder.board_data2 = (TextView)v.findViewById(R.id.textView2);

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}
		viewHolder.board_data1.setText(boardList.get(position).get("data1").toString());
		viewHolder.board_data2.setText(boardList.get(position).get("data2").toString());

		return v;
	}

	
	public void setArrayList(ArrayList<HashMap<String,Object>> arrays){
		this.boardList = arrays;
	}
	
	public ArrayList<HashMap<String,Object>> getArrayList(){
		return boardList;
	}
	
	
	/*
	 * ViewHolder
	 */
	class ViewHolder{
		TextView board_data1;
		TextView board_data2;
	}
	

}







