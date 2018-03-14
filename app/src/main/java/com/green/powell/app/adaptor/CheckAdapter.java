package com.green.powell.app.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.green.powell.app.R;

import java.util.ArrayList;
import java.util.HashMap;


public class CheckAdapter extends BaseAdapter  implements View.OnClickListener{

	private static final String TAG = "CheckAdapter";
	private LayoutInflater inflater;
	private ArrayList<HashMap<String,Object>> boardList;
	private ViewHolder viewHolder;
	private Context con;
	private String name;

	public interface ListBtnClickListener {
		void onCheckApprovalBtn(int position, int type) ;

	}

	private ListBtnClickListener listBtnClickListener ;

	public CheckAdapter(Context con , ArrayList<HashMap<String,Object>> array, String name){
		inflater = LayoutInflater.from(con);
		boardList = array;
		this.con = con;
		this.name = name;
	}

	public CheckAdapter(Context con , ArrayList<HashMap<String,Object>> array, String name, ListBtnClickListener clickListener){
		inflater = LayoutInflater.from(con);
		boardList = array;
		this.con = con;
		this.name = name;
		this.listBtnClickListener = clickListener ;
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

			if(name.equals("Check")){
				v = inflater.inflate(R.layout.check_list_item, parent,false);
				viewHolder.board_data1 = (TextView)v.findViewById(R.id.textView1);
				viewHolder.board_data2 = (TextView)v.findViewById(R.id.textView2);
				viewHolder.board_data3 = (TextView)v.findViewById(R.id.textView3);
				viewHolder.board_data4 = (TextView)v.findViewById(R.id.textView4);
				viewHolder.board_data5 = (TextView)v.findViewById(R.id.textView5);
				viewHolder.board_data6 = (TextView)v.findViewById(R.id.textView6);

			}else if(name.equals("UnCheck")){
				v = inflater.inflate(R.layout.umcheck_list_item, parent,false);
				viewHolder.board_data1 = (TextView)v.findViewById(R.id.textView1);
				viewHolder.board_data2 = (TextView)v.findViewById(R.id.textView2);
				viewHolder.board_data3 = (TextView)v.findViewById(R.id.textView3);
				viewHolder.board_data4 = (TextView)v.findViewById(R.id.textView4);

			}else{
				v = inflater.inflate(R.layout.check_list_item01, parent,false);
				viewHolder.board_data1 = (TextView)v.findViewById(R.id.textView1);
				viewHolder.board_data2 = (TextView)v.findViewById(R.id.textView2);
				viewHolder.board_data3 = (TextView)v.findViewById(R.id.textView3);
				viewHolder.board_data4 = (TextView)v.findViewById(R.id.textView4);
				viewHolder.board_data5 = (TextView)v.findViewById(R.id.textView5);
				viewHolder.button1 = (Button) v.findViewById(R.id.button1);
				viewHolder.button2 = (Button) v.findViewById(R.id.button2);

				viewHolder.button1.setTag(position);
				viewHolder.button1.setOnClickListener(this);
				viewHolder.button1.setFocusable(false);

				viewHolder.button2.setTag(position);
				viewHolder.button2.setOnClickListener(this);
				viewHolder.button2.setFocusable(false);

			}

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}
		if(name.equals("Check")){
			viewHolder.board_data1.setText(boardList.get(position).get("data1").toString());
			viewHolder.board_data2.setText(boardList.get(position).get("data2").toString());
			viewHolder.board_data3.setText(boardList.get(position).get("data3").toString());
			viewHolder.board_data4.setText(boardList.get(position).get("data4").toString());
			viewHolder.board_data5.setText(boardList.get(position).get("data5").toString());
			viewHolder.board_data6.setText(boardList.get(position).get("data6").toString());

			String checkState= boardList.get(position).get("data6").toString();
			if(checkState.equals("1")){
				viewHolder.board_data6.setText("양호");
				viewHolder.board_data6.setBackgroundResource(R.drawable.box_green);
			}else if(checkState.equals("2")){
				viewHolder.board_data6.setText("검토");
				viewHolder.board_data6.setBackgroundResource(R.drawable.box_yellow);
			}else if(checkState.equals("3")){
				viewHolder.board_data6.setText("즉시");
				viewHolder.board_data6.setBackgroundResource(R.drawable.box_blue);
			}else {
				viewHolder.board_data6.setText("긴급");
				viewHolder.board_data6.setBackgroundResource(R.drawable.box_red);
			}

		}else if(name.equals("UnCheck")){
			String type_kor= boardList.get(position).get("data5").toString();
			viewHolder.board_data1.setText(boardList.get(position).get("data1").toString());
			viewHolder.board_data2.setText(boardList.get(position).get("data2").toString());
			viewHolder.board_data3.setText(boardList.get(position).get("data3")+type_kor);
			viewHolder.board_data4.setText(boardList.get(position).get("data4")+type_kor);

		}else{
			viewHolder.board_data1.setText(boardList.get(position).get("data1").toString());
			viewHolder.board_data2.setText(boardList.get(position).get("data2").toString());
			viewHolder.board_data3.setText(boardList.get(position).get("data3").toString());
			viewHolder.board_data4.setText(boardList.get(position).get("data4").toString());
			viewHolder.board_data5.setText(boardList.get(position).get("data5").toString());

			viewHolder.button1.setTag(position);
			viewHolder.button1.setOnClickListener(this);
			viewHolder.button1.setFocusable(false);

			viewHolder.button2.setTag(position);
			viewHolder.button2.setOnClickListener(this);
			viewHolder.button2.setFocusable(false);

			String checkState= boardList.get(position).get("data3").toString();
			if(checkState.equals("1")){
				viewHolder.board_data3.setText("양호");
				viewHolder.board_data3.setBackgroundResource(R.drawable.box_green);
			}else if(checkState.equals("2")){
				viewHolder.board_data3.setText("검토");
				viewHolder.board_data3.setBackgroundResource(R.drawable.box_yellow);
			}else if(checkState.equals("3")){
				viewHolder.board_data3.setText("즉시");
				viewHolder.board_data3.setBackgroundResource(R.drawable.box_blue);
			}else{
				viewHolder.board_data3.setText("긴급");
				viewHolder.board_data3.setBackgroundResource(R.drawable.box_red);
		}

			String firstState= boardList.get(position).get("data6").toString();
			if(firstState.equals("2")){
				viewHolder.button1.setText("승인");
				viewHolder.button1.setBackgroundResource(R.drawable.box_green);
			}else if(firstState.equals("3")){
				viewHolder.button1.setText("반려");
				viewHolder.button1.setBackgroundResource(R.drawable.box_red);
			}else{
				viewHolder.button1.setText("1차");
				viewHolder.button1.setBackgroundResource(R.drawable.box_gray);
			}
			String secondState= boardList.get(position).get("data7").toString();
			if(secondState.equals("2")){
				viewHolder.button2.setText("승인");
				viewHolder.button2.setBackgroundResource(R.drawable.box_green);
			}else if(secondState.equals("3")){
				viewHolder.button2.setText("반려");
				viewHolder.button2.setBackgroundResource(R.drawable.box_red);
			}else{
				viewHolder.button2.setText("2차");
				viewHolder.button2.setBackgroundResource(R.drawable.box_gray);
			}
		}

		return v;
	}

	@Override
	public void onClick(View v) {
		if (this.listBtnClickListener != null) {
			int type=0;
			if(v.getId() == R.id.button1){
				type=1;
			}else if(v.getId() == R.id.button2){
				type=2;
			}
			int position= (int) v.getTag();
			this.listBtnClickListener.onCheckApprovalBtn(position, type) ;

		}
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
		TextView board_data3;
		TextView board_data4;
		TextView board_data5;
		TextView board_data6;
		Button button1;
		Button button2;
	}
	

}







