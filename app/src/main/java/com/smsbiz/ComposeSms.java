package com.smsbiz;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class ComposeSms extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    EditText phoneNo,messageTxt;
    ImageView sendSms;
    private Handler handler;
    private TextView title;
    ProgressBar progressBar;
    ImageButton createBtn,menu_or_home,searchBtn;
    Activity activity;
    private RecyclerView listMessages;
    private ArrayList<SmsDetail> messageList;
    private  String senderId="";
    private boolean fromNotification;
    private boolean sent,clickedSend=false,createmsg=false,fromSearchText=false;

    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter smsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_sms);
        getActionBarToolbar();
        handler = new Handler();
        phoneNo = (EditText) findViewById(R.id.phoneNo);
        messageTxt = (EditText) findViewById(R.id.messageTxt);
        sendSms = (ImageView) findViewById(R.id.sendSms);
        menu_or_home = (ImageButton) findViewById(R.id.menu_or_home);
        createBtn =(ImageButton) findViewById(R.id.createBtn);
        searchBtn = (ImageButton) findViewById(R.id.searchBtn);
        title = (TextView) findViewById(R.id.title);
        title.setText(getResources().getString(R.string.newmsg));
        Intent intent = getIntent();
        messageList = (ArrayList<SmsDetail>)intent.getSerializableExtra("smsdata");
        senderId = intent.getStringExtra("senderid");
        fromNotification = intent.getBooleanExtra("fromNotification", false);
        createmsg = intent.getBooleanExtra("createmsg",false);
        fromSearchText = intent.getBooleanExtra("fromSearchText", false);
        listMessages = (RecyclerView) findViewById(R.id.listMessages);
        smsAdapter = new SmsAdapter();
        layoutManager = new LinearLayoutManager(activity);
        listMessages.setLayoutManager(layoutManager);

        if(fromNotification || fromSearchText)
        {
            getSmsFromInbox(senderId);
        }
        if(messageList!=null)
        {
            phoneNo.setVisibility(View.GONE);
            listMessages.setVisibility(View.VISIBLE);
            smsAdapter = new SmsAdapter();
            title.setText(senderId);
            title.setTextColor(getResources().getColor(R.color.white));
            Collections.sort(messageList);
            listMessages.setAdapter(smsAdapter);
            scrollListToBottom();
        }
        if(!(fromSearchText || fromNotification|| messageList!=null))
        {
            listMessages.setVisibility(View.GONE);
        }
        createBtn.setVisibility(View.GONE);
        searchBtn.setVisibility(View.GONE);
        messageTxt.setOnClickListener(this);
        sendSms.setOnClickListener(this);
        phoneNo.setOnClickListener(this);
        progressBar =(ProgressBar) findViewById(R.id.progressBar);
        menu_or_home.setOnClickListener(this);
        menu_or_home.setImageResource(R.drawable.ic_keyboard_backspace_black_24dp);
        menu_or_home.setVisibility(View.VISIBLE);
        activity = this;
        int permissionCheck2 = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.SEND_SMS);

        int permissionCheck3 = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.RECEIVE_SMS);
        int permissionCheck4 = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_CONTACTS);
        if(permissionCheck3 == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
        }
        if(permissionCheck2 ==PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }
        if(permissionCheck4 ==PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        }
    }
    private void  getSmsFromInbox(String fromMsg)
    {
        SmsDetail objSms = new SmsDetail();
        Uri message = Uri.parse("content://sms");
        if(activity==null)
            activity = this;
        ContentResolver cr = activity.getContentResolver();
        Cursor c = cr.query(message, null, null, null, null);
        if(messageList ==null)
            messageList =new ArrayList<SmsDetail>();
        activity.startManagingCursor(c);
        int totalSMS = c.getCount();
        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

                objSms = new SmsDetail();
                objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));

                objSms.setAddress(c.getString(c
                        .getColumnIndexOrThrow("address")));
                objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                objSms.setReadState(c.getString(c.getColumnIndex("read")));
                objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.setFolderName("inbox");
                } else {
                    objSms.setFolderName("sent");
                }
                String senderId = c.getString(c
                        .getColumnIndexOrThrow("address")).toString();
                if(senderId.equalsIgnoreCase(fromMsg))
                    messageList.add(objSms);
                c.moveToNext();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case  R.id.sendSms:
                if(senderId!=null && !senderId.equalsIgnoreCase("")) {
                    SmsDetail sd = new SmsDetail();
                    sd.setAddress(senderId);
                    sd.setFolderName("sent");
                    sd.setMsg(messageTxt.getText().toString());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Calendar cal = Calendar.getInstance();
                    sd.setTime(dateFormat.format(cal.getTime()));
                    sd.setReadState(null);
                    sd.setId(null);
                    clickedSend = true;
                    messageList.add(sd);
                    smsAdapter.notifyDataSetChanged();
                    sent = false;
                    listMessages.scrollToPosition(messageList.size() - 1);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            sendSMS(senderId, messageTxt.getText().toString());
                        }
                    });
                }
                else
                {
                    if(phoneNo.getText().toString().equalsIgnoreCase(""))
                        Toast.makeText(activity.getApplicationContext(),"Please enter phone number",Toast.LENGTH_SHORT).show();
                    else {

                        if(verifyPhoneNo(phoneNo.getText().toString())) {
                            progressBar.setVisibility(View.VISIBLE);

                            sendSMS(phoneNo.getText().toString(), messageTxt.getText().toString());
                        }
                        else
                            Toast.makeText(activity.getApplicationContext(),"Please enter correct phone number.",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.messageTxt:
//                activity.getWindow().setSoftInputMode(
//                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                break;
            case R.id.phoneNo:
                break;
            case R.id.menu_or_home:
                if(fromNotification)
                {
                    Intent i = new Intent(activity, Inbox.class);
                    startActivity(i);
                }
                finish();
                break;
        }
    }

    private void scrollListToBottom() {
        listMessages.post(new Runnable() {
            @Override
            public void run() {
                listMessages.scrollToPosition(smsAdapter.getItemCount() - 1);

            }
        });
    }

    public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.ViewHolder>  {
        public  class ViewHolder extends RecyclerView.ViewHolder {
            public TextView senderId,senderCount,timeDate, sending,received;

            public ViewHolder(View v) {
                super(v);
                senderId = (TextView) v.findViewById(R.id.senderId);
                senderCount= (TextView)v.findViewById(R.id.senderCount);
                timeDate = (TextView) v.findViewById(R.id.timeDate);
                sending = (TextView)v.findViewById(R.id.sending);
                received = (TextView) v.findViewById(R.id.recieved);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_sms, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            holder.timeDate.setVisibility(View.VISIBLE);
            holder.senderId.setText(messageList.get(position).getMsg());
            holder.timeDate.setBackgroundColor(getResources().getColor(R.color.white));
            holder.senderCount.setVisibility(View.GONE);
            if(messageList.get(position).getFolderName().equalsIgnoreCase("inbox"))
                holder.received.setVisibility(View.VISIBLE);
            else
                holder.sending.setVisibility(View.VISIBLE);
            String timeStamp= messageList.get(position).getTime();
            if(clickedSend )
            {
                if(!sent && position==messageList.size()-1 ) {
                    holder.sending.setText("Sending");
                    holder.received.setVisibility(View.GONE);
                }
                if(sent && position == messageList.size()-1 ){
                    holder.sending.setText("Sent");
                    holder.received.setVisibility(View.GONE);
                }
                if(position == messageList.size()-1)
                {
                    String month = timeStamp.substring(3,5);
                    String dd = timeStamp.substring(0,2);
                    holder.timeDate.setText(dd +" "+getMonth(month));
                }
                else
                {
                    String date = getDate(Long.parseLong(timeStamp));
                    String month = date.substring(3,5);
                    String dd = date.substring(0,2);
                    holder.timeDate.setText(dd+" "+getMonth(month));
                }

            }
            else
            {
                String date = getDate(Long.parseLong(timeStamp));
                String month = date.substring(3,5);
                String dd = date.substring(0,2);
                holder.timeDate.setText(dd+" "+getMonth(month));
            }


        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }


    private String getMonth(String month)
    {
        String smonth="";
        switch (month){
            case "01":
                smonth="Jan";
                break;
            case "02":
                smonth="Feb";
                break;
            case "03":
                smonth="Mar";
                break;
            case "04":
                smonth="Apr";
                break;
            case "05":
                smonth="May";
                break;
            case "06":
                smonth="Jun";
                break;
            case "07":
                smonth="Jul";
                break;
            case "08":
                smonth="Aug";
                break;
            case "09":
                smonth="Sep";
                break;
            case "10":
                smonth="Oct";
                break;
            case "11":
                smonth="Nov";
                break;
            case "12":
                smonth="Dec";
                break;
            default:
                break;
        }
        return smonth;
    }


    public boolean verifyPhoneNo(String number)
    {
        return android.util.Patterns.PHONE.matcher(number).matches();
    }
    private void sendSMS(String phoneNumber, String message)
    {
        try{
            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";

            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(SENT), 0);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(DELIVERED), 0);

            registerReceiver(new BroadcastReceiver(){
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode())
                    {
                        case Activity.RESULT_OK:
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getBaseContext(), "SMS sent",
                                    Toast.LENGTH_SHORT).show();
                            sent =true;
                            if(!createmsg)
                                smsAdapter.notifyDataSetChanged();

                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(getBaseContext(), "Generic failure",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(getBaseContext(), "No service",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(getBaseContext(), "Null PDU",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(getBaseContext(), "Radio off",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter(SENT));

            registerReceiver(new BroadcastReceiver(){
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode())
                    {
                        case Activity.RESULT_OK:
                            Toast.makeText(getBaseContext(), "SMS delivered",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(getBaseContext(), "SMS not delivered",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter(DELIVERED));
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public Toolbar getActionBarToolbar() {
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.main_toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
        return toolbar;
    }

}
