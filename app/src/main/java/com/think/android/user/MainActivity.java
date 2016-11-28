package com.think.android.user;

        import java.io.BufferedReader;
        import java.io.File;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.io.PrintWriter;
        import java.net.ServerSocket;
        import java.net.Socket;
        import java.util.HashMap;
        import java.util.Timer;
        import java.util.TimerTask;

        import android.media.AudioFormat;
        import android.media.AudioManager;
        import android.media.AudioRecord;
        import android.media.MediaRecorder;
        import android.media.SoundPool;
        import android.os.Bundle;
        import android.app.Activity;
        import android.content.Context;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.util.AttributeSet;
        import android.view.Gravity;
        import android.view.Menu;
        import android.view.View;
        import android.view.WindowManager;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.LinearLayout;
        import android.widget.TextView;

public class MainActivity extends Activity {

    int  x,y;
    LinearLayout line1,line2,line3,line4,line5,line6;
    TextView text1;
    public static BorderTextView[] tv1 = new BorderTextView[9];
    public static BorderTextView[] tv2 = new BorderTextView[9];
    BorderTextView[] tv3 = new BorderTextView[6];
    BorderTextView[] tv4 = new BorderTextView[4];
    BorderTextView btv1;
    Button b_connect,b_record,b_import;

    Thread thread1,thread2,thread3,thread4,thread5,thread6,thread7,thread8;
    PrintWriter pw1,pw2,pw3,pw4,pw5,pw6,pw7,pw8;
    BufferedReader br1,br2,br3,br4,br5,br6,br7,br8;
    ServerSocket server1,server2,server3,server4,server5,server6,server7,server8;
    Socket socket1,socket2,socket3,socket4,socket5,socket6,socket7,socket8;
    Timer timer = new Timer();

    //int FeedbackStart = 0;//开始录音的反馈标志位
    private static int FolderNum = 1;//用于存放录音文件的与 文件夹名称的相关标志位

    //设置音频来源为麦克风
    private static int audioSource = MediaRecorder.AudioSource.MIC;
    //设置采样频率
    private static int sampleRateInHz = 44100;
    //设置音频的录制声道:CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    //音频数据格式：PCM编码的样本位数，或者8位，或者16位。要保证设备支持
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //设置表征缓存大小的变量
    private int bufferSizeInBytes = 0;

    //录音实现类AudioRecord
    private AudioRecord audioRecord;
    //用SoundPool播放声音
    private SoundPool sp;
    private HashMap<Integer,Integer> spMap;

    static int ipAddress;
    /*发送广播端的socket*/


    double[] resultLoc;//定位的结果
    double[][] resultloop ;//存循环几次的结果
    private static int loopnum = 1;

    long time1,time2;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        x=(int)93*getWindowManager().getDefaultDisplay().getWidth()/100;//获取屏幕宽度
        y=(int)93*getWindowManager().getDefaultDisplay().getHeight()/100;//获取屏幕高度

        final MyApplication app = (MyApplication)getApplication();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//防止休眠

        line1 = (LinearLayout)findViewById(R.id.line1);
        line2 = (LinearLayout)findViewById(R.id.line2);
        line3 = (LinearLayout)findViewById(R.id.line3);
        line4 = (LinearLayout)findViewById(R.id.line4);
        line5 = (LinearLayout)findViewById(R.id.line5);
        line6 = (LinearLayout)findViewById(R.id.line6);

        text1 = new TextView(this);
        text1.setWidth(x);
        text1.setHeight(y/40);
        text1.setText("");
        text1.setGravity(Gravity.CENTER);
        text1.setVisibility(View.INVISIBLE);
        line6.addView(text1);


        tv1[0] = new BorderTextView(this);
        tv1[0].setText("Beacon:");
        tv1[0].setWidth(x/6);
        tv1[0].setHeight(y/8);
        tv1[0].setTextSize(15);
        tv1[0].setGravity(Gravity.CENTER);

        for(int i=1;i<9;i++)
        {
            tv1[i] = new BorderTextView(this);
            tv1[i].setText(Integer.toString(i));
            tv1[i].setWidth(5*x/48);
            tv1[i].setHeight(y/8);
            tv1[i].setTextSize(16);
            tv1[i].setGravity(Gravity.CENTER);
        }

        for(int i=0;i<9;i++)
        {
            line1.addView(tv1[i]);
        }

        tv2[0] = new BorderTextView(this);
        tv2[0].setText("State:");
        tv2[0].setWidth(x/6);
        tv2[0].setHeight(y/8);
        tv2[0].setTextSize(16);
        tv2[0].setGravity(Gravity.CENTER);

        for(int i=1;i<9;i++)
        {
            tv2[i] = new BorderTextView(this);
            tv2[i].setText(Integer.toString(i));
            tv2[i].setWidth(5*x/48);
            tv2[i].setHeight(y/8);
            tv2[i].setTextSize(16);
            tv2[i].setGravity(Gravity.CENTER);
            tv2[i].setBackgroundColor(Color.LTGRAY);
        }

        for(int i=0;i<9;i++)
        {
            line2.addView(tv2[i]);
        }


        btv1 = new BorderTextView(this);
        btv1.setWidth(x);
        btv1.setHeight(y/12);
        btv1.setText("WORKING");
        btv1.setTextSize(18);
        btv1.setGravity(Gravity.CENTER);
        btv1.setBackgroundColor(Color.LTGRAY);

        line5.addView(btv1);

        resultLoc = new double[3];
        resultloop = new double[loopnum][3];

        while(new File("mnt/sdcard/Data"+String.valueOf(FolderNum)).exists()==true){
            FolderNum = FolderNum+1;
        }//建立根文件夹用于存放录音文件

        //获得缓冲区大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,channelConfig,audioFormat);
        System.out.println(bufferSizeInBytes);
        //实例化AudioRecord
        audioRecord = new AudioRecord(audioSource,sampleRateInHz,channelConfig,audioFormat,bufferSizeInBytes);
        //防止文件夹重复

        sp = new SoundPool(7,AudioManager.STREAM_MUSIC,0);//同时播放的最大音频数为7
        spMap = new HashMap<Integer,Integer>();
        //spMap.put(1, sp.load(this, R.raw.data_refer, 1));

        b_import = (Button)findViewById(R.id.button0);
        b_connect = (Button)findViewById(R.id.button00);
        b_record = (Button)findViewById(R.id.button1);

        b_import.setEnabled(false);
        b_record.setEnabled(false);

        b_import.setOnClickListener(new OnClickListener(){
            public void onClick(View v)
            {

                Thread waitcon = new Thread(new Runnable(){
                    public void run()
                    {
                        while(true)
                        {
                            if((app.connect1==true)&&(app.connect2==true)&&(app.connect3==true)&&(app.connect4==true)&&(app.connect5==true)
                                    &&(app.connect6==true)&&(app.connect7==true)&&(app.connect8==true))
//			                if(connect5==true)
                            {
                                b_record.post(new Runnable(){
                                    public void run()
                                    {
                                        b_record.setEnabled(true);
                                    }
                                });
                                b_import.post(new Runnable(){
                                    public void run()
                                    {
                                        b_import.setEnabled(false);
                                    }
                                });
                                break;
                            }
                        }
                    }
                });
                waitcon.start();


            }
        });

        b_connect.setOnClickListener(new OnClickListener(){
            public void onClick(View v)
            {
                b_import.setEnabled(true);
                b_connect.setEnabled(false);
                BeaconBase base1=new BeaconBase(socket1,server1,1,app);
                BeaconBase base2=new BeaconBase(socket2,server2,2,app);
                BeaconBase base3=new BeaconBase(socket3,server3,3,app);
                BeaconBase base4=new BeaconBase(socket4,server4,4,app);
                BeaconBase base5=new BeaconBase(socket5,server5,5,app);
                BeaconBase base6=new BeaconBase(socket6,server6,6,app);
                BeaconBase base7=new BeaconBase(socket7,server7,7,app);
                BeaconBase base8=new BeaconBase(socket8,server8,8,app);

                thread1 = new Thread( base1);
                thread2 = new Thread(base2);
                thread3 = new Thread(base3);
                thread4 = new Thread(base4);
                thread5 = new Thread(base5);
                thread6 = new Thread(base6);
                thread7 = new Thread(base7);
                thread8 = new Thread(base8);

//                thread1 = new Thread(new base1());
//                thread2 = new Thread(new base2());
//                thread3 = new Thread(new base3());
//                thread4 = new Thread(new base4());
//                thread5 = new Thread(new base5());
//                thread6 = new Thread(new base6());
//                thread7 = new Thread(new base7());
//                thread8 = new Thread(new base8());
                thread1.start();
                thread2.start();
                thread3.start();
                thread4.start();
                thread5.start();
                thread6.start();
                thread7.start();
                thread8.start();
            }
        });

        b_record.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if(String.valueOf(b_record.getText()).equals("Start"))
                {
//					b_record.setEnabled(false);
                    b_record.setText("Stop");
                    //状态位置为灰色
                    for(int i=1;i<9;i++)
                    {
                        tv2[i].setBackgroundColor(Color.LTGRAY);
                    }
                    for(int i=1;i<6;i++)
                    {
                        tv3[i].setText("");
                    }
                    for(int i=1;i<4;i++)
                    {
                        tv4[i].setText("");
                    }

                    timer.schedule(new RecordStart(),0);//开始录音
                    long time1 = System.currentTimeMillis();
                    while(true)
                    {
                        if(app.FeedbackStart==8)
//						if(app.FeedbackStart==1)
                        {
                            timer.schedule(new Clock(), 400);//开始时钟同步
                            app.FeedbackStart=0;
                            break;
                        }
                        long time2 = System.currentTimeMillis();
                        if((time2-time1)>(long)3)
                        {
                            timer.schedule(new Clock(), 400);//开始时钟同步
                            app.FeedbackStart=0;
                            break;
                        }
                    }
                }
                else{
//					b_record.setEnabled(false);
                    b_record.setText("Start");
                    timer.schedule(new RecordEnd(), 100);//结束录音
                    app.end1 = false;
                    app.end2 = false;
                    app.end3 = false;
                    app.end4 = false;
                    app.end5 = false;
                    app.end6 = false;
                    app.end7 = false;
                    app.end8 = false;
                }
            }
        });

    }
//
//    class base1 implements Runnable{
//        public void run(){
//            try{
//                server1 = new ServerSocket(2001);
//                socket1 = server1.accept();
//                connect1 = true;
//
//                tv1[1].post(new Runnable(){
//                    public void run()
//                    {
//                        tv1[1].setBackgroundColor(Color.GREEN);
//                    }
//                });
//
//                pw1 = new PrintWriter(socket1.getOutputStream(),true);
//                while(true)
//                {
//                    br1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
//                    String line1 = br1.readLine();
//                    while(line1!=null)
//                    {
//                        if(line1.equals("520"))
//                        {
//                            FeedbackStart = FeedbackStart +1;
//                        }
//                        if(line1.equals("420"))
//                        {
//                            end1 = true;
//                            tv2[1].post(new Runnable(){
//                                public void run(){
//                                    tv2[1].setBackgroundColor(Color.GREEN);
//                                }
//                            });
//                        }
//
//                        line1 = br1.readLine();
//                    }
//                }
//            }catch(IOException e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    class base2 implements Runnable{
//        public void run(){
//            try{
//                server2 = new ServerSocket(2002);
//                socket2 = server2.accept();
//                connect2 = true;
//
//                tv1[2].post(new Runnable(){
//                    public void run()
//                    {
//                        tv1[2].setBackgroundColor(Color.GREEN);
//                    }
//                });
//
//                pw2 = new PrintWriter(socket2.getOutputStream(),true);
//                while(true)
//                {
//                    br2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
//                    String line2 = br2.readLine();
//                    while(line2!=null)
//                    {
//                        if(line2.equals("520"))
//                        {
//                            FeedbackStart = FeedbackStart +1;
//                        }
//                        if(line2.equals("420"))
//                        {
//                            end2 = true;
//                            tv2[2].post(new Runnable(){
//                                public void run(){
//                                    tv2[2].setBackgroundColor(Color.GREEN);
//                                }
//                            });
//                        }
//
//                        line2 = br2.readLine();
//                    }
//                }
//            }catch(IOException e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    class base3 implements Runnable{
//        public void run(){
//            try{
//                server3 = new ServerSocket(2003);
//                socket3 = server3.accept();
//                connect3 = true;
//
//                tv1[3].post(new Runnable(){
//                    public void run()
//                    {
//                        tv1[3].setBackgroundColor(Color.GREEN);
//                    }
//                });
//
//                pw3 = new PrintWriter(socket3.getOutputStream(),true);
//                while(true)
//                {
//                    br3 = new BufferedReader(new InputStreamReader(socket3.getInputStream()));
//                    String line3 = br3.readLine();
//                    while(line3!=null)
//                    {
//                        if(line3.equals("520"))
//                        {
//                            FeedbackStart = FeedbackStart +1;
//                        }
//                        if(line3.equals("420"))
//                        {
//                            end3 = true;
//                            tv2[3].post(new Runnable(){
//                                public void run(){
//                                    tv2[3].setBackgroundColor(Color.GREEN);
//                                }
//                            });
//                        }
//
//                        line3 = br3.readLine();
//                    }
//                }
//            }catch(IOException e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    class base4 implements Runnable{
//        public void run(){
//            try{
//                server4 = new ServerSocket(2004);
//                socket4 = server4.accept();
//                connect4 = true;
//
//                tv1[4].post(new Runnable(){
//                    public void run()
//                    {
//                        tv1[4].setBackgroundColor(Color.GREEN);
//                    }
//                });
//
//                pw4 = new PrintWriter(socket4.getOutputStream(),true);
//                while(true)
//                {
//                    br4 = new BufferedReader(new InputStreamReader(socket4.getInputStream()));
//                    String line4 = br4.readLine();
//                    while(line4!=null)
//                    {
//                        if(line4.equals("520"))
//                        {
//                            FeedbackStart = FeedbackStart +1;
//                        }
//                        if(line4.equals("420"))
//                        {
//                            end4 = true;
//                            tv2[4].post(new Runnable(){
//                                public void run(){
//                                    tv2[4].setBackgroundColor(Color.GREEN);
//                                }
//                            });
//                        }
//                        line4 = br4.readLine();
//                    }
//                }
//            }catch(IOException e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    class base5 implements Runnable{
//        public void run(){
//            try{
//                server5 = new ServerSocket(2005);
//                socket5 = server5.accept();
//                connect5 = true;
//
//                tv1[5].post(new Runnable(){
//                    public void run()
//                    {
//                        tv1[5].setBackgroundColor(Color.GREEN);
//                    }
//                });
//
//                pw5 = new PrintWriter(socket5.getOutputStream(),true);
//                while(true)
//                {
//                    br5 = new BufferedReader(new InputStreamReader(socket5.getInputStream()));
//                    String line5 = br5.readLine();
//                    while(line5!=null)
//                    {
//                        if(line5.equals("520"))
//                        {
//                            FeedbackStart = FeedbackStart +1;
//                        }
//                        if(line5.equals("420"))
//                        {
//                            end5 = true;
//                            tv2[5].post(new Runnable(){
//                                public void run(){
//                                    tv2[5].setBackgroundColor(Color.GREEN);
//                                }
//                            });
//                        }
//                        line5 = br5.readLine();
//                    }
//                }
//            }catch(IOException e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    class base6 implements Runnable{
//        public void run(){
//            try{
//                server6 = new ServerSocket(2006);
//                socket6 = server6.accept();
//                connect6 = true;
//
//                tv1[6].post(new Runnable(){
//                    public void run()
//                    {
//                        tv1[6].setBackgroundColor(Color.GREEN);
//                    }
//                });
//
//                pw6 = new PrintWriter(socket6.getOutputStream(),true);
//                while(true)
//                {
//                    br6 = new BufferedReader(new InputStreamReader(socket6.getInputStream()));
//                    String line6 = br6.readLine();
//                    while(line6!=null)
//                    {
//                        if(line6.equals("520"))
//                        {
//                            FeedbackStart = FeedbackStart +1;
//                        }
//                        if(line6.equals("420"))
//                        {
//                            end6 = true;
//                            tv2[6].post(new Runnable(){
//                                public void run(){
//                                    tv2[6].setBackgroundColor(Color.GREEN);
//                                }
//                            });
//                        }
//                        line6 = br6.readLine();
//                    }
//                }
//            }catch(IOException e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    class base7 implements Runnable{
//        public void run(){
//            try{
//                server7 = new ServerSocket(2007);
//                socket7 = server7.accept();
//                connect7 = true;
//
//                tv1[7].post(new Runnable(){
//                    public void run()
//                    {
//                        tv1[7].setBackgroundColor(Color.GREEN);
//                    }
//                });
//
//                pw7 = new PrintWriter(socket7.getOutputStream(),true);
//                while(true)
//                {
//                    br7 = new BufferedReader(new InputStreamReader(socket7.getInputStream()));
//                    String line7 = br7.readLine();
//                    while(line7!=null)
//                    {
//                        if(line7.equals("520"))
//                        {
//                            FeedbackStart = FeedbackStart +1;
//                        }
//                        if(line7.equals("420"))
//                        {
//                            end7 = true;
//                            tv2[7].post(new Runnable(){
//                                public void run(){
//                                    tv2[7].setBackgroundColor(Color.GREEN);
//                                }
//                            });
//                        }
//                        line7 = br7.readLine();
//                    }
//                }
//            }catch(IOException e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    class base8 implements Runnable{
//        public void run(){
//            try{
//                server8 = new ServerSocket(2008);
//                socket8 = server8.accept();
//                connect8 = true;
//
//                tv1[8].post(new Runnable(){
//                    public void run()
//                    {
//                        tv1[8].setBackgroundColor(Color.GREEN);
//                    }
//                });
//
//                pw8 = new PrintWriter(socket8.getOutputStream(),true);
//                while(true)
//                {
//                    br8 = new BufferedReader(new InputStreamReader(socket8.getInputStream()));
//                    String line8 = br8.readLine();
//                    while(line8!=null){
//                        if(line8.equals("000"))
//                        {
////							timer.schedule(new TargetGo(), 400);
//                        }
//                        if(line8.equals("520"))
//                        {
//                            FeedbackStart = FeedbackStart +1;
//                        }
//                        if(line8.equals("420"))
//                        {
//                            end8 = true;
//                            tv2[8].post(new Runnable(){
//                                public void run(){
//                                    tv2[8].setBackgroundColor(Color.GREEN);
//                                }
//                            });
//                        }
//
//                        line8 = br8.readLine();
//                    }
//                }
//            }catch(IOException e){
//                e.printStackTrace();
//            }
//        }
//    }

    class RecordStart extends TimerTask{
        public void run(){
            pw1.println("00");//00作为开始录音的标志位
            pw2.println("00");
            pw3.println("00");
            pw4.println("00");
            pw5.println("00");
            pw6.println("00");
            pw7.println("00");
            pw8.println("00");

        }
    }

    class RecordEnd extends TimerTask{
        public void run(){
            pw1.println("01");//01作为结束录音的标志位
            pw2.println("01");
            pw3.println("01");
            pw4.println("01");
            pw5.println("01");
            pw6.println("01");
            pw7.println("01");
            pw8.println("01");

        }
    }

    class Clock extends TimerTask{
        public void run(){
            pw8.println("02");//02作为5号节点发声的标志位，用于时钟同步
        }
    }

    public class BorderTextView extends TextView{

        public BorderTextView(Context context) {
            super(context);
        }
        public BorderTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        private int sroke_width = 1;
        @Override
        protected void onDraw(Canvas canvas) {
            Paint paint = new Paint();
            //  将边框设为黑色
            paint.setColor(android.graphics.Color.BLACK);
            //  画TextView的4个边
            canvas.drawLine(0, 0, this.getWidth() - sroke_width, 0, paint);
            canvas.drawLine(0, 0, 0, this.getHeight() - sroke_width, paint);
            canvas.drawLine(this.getWidth() - sroke_width, 0, this.getWidth() - sroke_width, this.getHeight() - sroke_width, paint);
            canvas.drawLine(0, this.getHeight() - sroke_width, this.getWidth() - sroke_width, this.getHeight() - sroke_width, paint);
            super.onDraw(canvas);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
