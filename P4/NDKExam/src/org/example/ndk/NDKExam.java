package org.example.ndk;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

/* 메인 Activity */
public class NDKExam extends Activity {
	public native int switchdriveropen();
	public native int switchdriverclose(int fd);
	public native int driverwrite(int value);
	public native int fndwrite(int value);
	public native int switchread(int fd);
	
	View playLayout;
	View trackLayout;
	TextView tv;
	TextView feverTextView;
	Button airplaneButton;
	TextView airplaneScoreText;
	Button beautyButton;
	TextView beautyScoreText;
	Button appleButton;
	TextView appleScoreText;
	Button freePlayButton;
	Button backButton;
	Button track1play;
	Button track2play;
	int switch_value;
	int currentIndex = 0;
	int myBestScore[] = {0, 0, 0, 0};
	int score = 0;
	int currentSong = 0;
	int musicSpeed = 600;
	String str = "";
	String interval = "";
	String[] doremiColor = {"#ffffff", "#ff0000", "#ffa500", "#ffff00", "#008000", "#0000ff", "#4b0082", "#ee82ee", "#f25278", "#d16002"};
	int[] feverColor = {0x00ffffff, 0x1084CFEC, 0x109589C7, 0x1028547E};
	MediaPlayer[] Doremi;
	boolean feverTime = false;
	int consecutive = 0;
	int feverConsecutive = 0;
	int feverLevel = 0;
	String track1 = "";
	String track2 = "";
	
	/* Track A or Track B에 저장된 음악 플레이
	 * 그 전에 실행되는 음악은 정지한다.
	 */
	private void TrackPlay(int track){
		for(int i=0; i<10; i++)
			if(Doremi[i].isPlaying()){
				Doremi[i].pause();
				if(track == 1 && track1.charAt(currentIndex) != i + '0'){
					Doremi[i].seekTo(300);
				}
				else if(track == 2 && track2.charAt(currentIndex) != i + '0'){
					Doremi[i].seekTo(300);
				}
			}
	}
	
	/* Track A or B에 저장된 음악 플레이가 끝났는 지 체크 */
	private void TrackEndCheck(){
		if(currentIndex >= str.length()){
			str="";
			TrackPlayActivity.mContext.EndSong();
		}
	}
	
	/* 현재 누른 버튼에 따른 색깔 변화 */
	private void TextColorChange(int msg){
		SpannableStringBuilder ssb = new SpannableStringBuilder(str);
		if(currentIndex != 0) //0부터 currentIndex-1까지(지나간 음) 투명색으로 처리
			ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#00777777")), 0, currentIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		for(int i = currentIndex+1; i< str.length(); i++){ //currentIndex + 1 부터 끝까지 투명한 무지개색으로 설정
			ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#55" + doremiColor[interval.charAt(i)-'0'].substring(1))), i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
		//현재 글자 불투명한 컬러 설정 및 굵게 설정
		ssb.setSpan(new ForegroundColorSpan(Color.parseColor(doremiColor[msg])), currentIndex, currentIndex+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.setSpan(new StyleSpan(Typeface.BOLD), currentIndex, currentIndex+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.setText(ssb);
	}
	
	/* 현재 누른 버튼에 따른 글자 변화 */
	private void CurrentDoremiChange(int msg){
		StringBuilder builder = new StringBuilder(str);
		switch(msg){
		case 0: builder.setCharAt(currentIndex, '쉼'); break;
		case 1: builder.setCharAt(currentIndex, '도'); break;
		case 2: builder.setCharAt(currentIndex, '레'); break;
		case 3: builder.setCharAt(currentIndex, '미'); break;
		case 4: builder.setCharAt(currentIndex, '파'); break;
		case 5: builder.setCharAt(currentIndex, '솔'); break;
		case 6: builder.setCharAt(currentIndex, '라'); break;
		case 7: builder.setCharAt(currentIndex, '시'); break;
		case 8: builder.setCharAt(currentIndex, '도'); break;
		case 9: builder.setCharAt(currentIndex, '레');
		}
		str = builder.toString();
	}
	
	/* 현재 누른 버튼에 따른 점수 계산 , fever Time 효과 설정 */
	private void InputCheck(int msg){
		if(interval.substring(currentIndex, currentIndex+1).equals(Integer.toString(msg))){ //올바른 버튼 입력
			if(!feverTime){ //보너스 타임이 아닐 경우
				if(interval.charAt(currentIndex) != '0') consecutive += 1;
				if(msg != 0) score += 1;
			}
			else{ //보너스 타임일 경우
				consecutive = 8;
				if(msg != 0) score += feverLevel * 2;
				if(interval.charAt(currentIndex) != '0') feverConsecutive += 1;
			}
			
			//피버타임이 아닐 경우 5연속 맞았을 때 피버 1단계 실행
			if(!feverTime && consecutive >= 5){
				tv.setBackgroundColor(feverColor[1]);
				feverLevel = 1;
				feverTime = true;
				feverTextView.setText("FEVER TIME X2");
			}
			fndwrite(score);
		}
		else{ //틀릴경우
			consecutive = 0;
			tv.setBackgroundColor(0x00fafafa);
			feverTime = false;
			feverTextView.setText("");
		}
		
		driverwrite(consecutive);
	}
	
	Handler mHandler=new Handler(){
		public void handleMessage(Message msg){
			if(str.length() == 0){
			
			}
			else if(currentSong == 1001){ //track 1 playing
				
				TrackPlay(1);
				
				if(track1.charAt(currentIndex) != '0'){
					Doremi[track1.charAt(currentIndex) - '0'].start();
				}
				currentIndex += 1;
				TrackEndCheck();
			}
			else if(currentSong == 1002){ //track 2 playing
				
				TrackPlay(2);
				
				if(track2.charAt(currentIndex) != '0'){
					Doremi[track2.charAt(currentIndex) - '0'].start();
				}
				currentIndex += 1;
				TrackEndCheck();
			}
			else if(currentIndex >= str.length()){ //노래 끝!
				backButton.setVisibility(View.VISIBLE);
			}
			else if(currentSong == 100 || currentSong == 101 || currentSong == 102){ //free play mode
				
				//현재 누른 버튼에 따른 소리 출력
				for(int i=0; i<10; i++){
					if(Doremi[i].isPlaying()){
						Doremi[i].pause();
						if(i != msg.arg1) Doremi[i].seekTo(300);
					}
				}
				
				if(msg.arg1 != 0){
					Doremi[msg.arg1].start();
				}
				
				CurrentDoremiChange(msg.arg1);
				TextColorChange(msg.arg1);
				
				currentIndex += 1;
				
				//트랙 A 또는 B에 저장
				if(currentSong == 101) track1 += msg.arg1;
				else if(currentSong == 102) track2 += msg.arg1;
			}
			else{ //게임 모드
				//해당 노래 소리 출력
				for(int i=0; i<10; i++)
					if(Doremi[i].isPlaying()){
						Doremi[i].pause();
						if(interval.charAt(currentIndex) -'0' != i) Doremi[i].seekTo(300);
					}
				
				if(interval.charAt(currentIndex) != '0'){
					Doremi[interval.charAt(currentIndex) - '0'].start();
				}
				
				try{Thread.sleep(70);}
				catch(InterruptedException e){}
				
				CurrentDoremiChange(msg.arg1);
				TextColorChange(msg.arg1);
				InputCheck(msg.arg1);
				currentIndex += 1;
				
				//피버 타임에서 연속 3회이상 맞을 경우 피버 1단계 -> 2단계 -> 3단계 업그레이드
				if(feverConsecutive == 3){
					feverConsecutive = 0;
					feverLevel += 1;
					if(feverLevel >= 4) feverLevel = 3; //최대 3단계
					tv.setBackgroundColor(feverColor[feverLevel]);
					feverTextView.setText("FEVER TIME X" + feverLevel*2);
				}
			}
		}
	};
	class BackThread extends Thread{
		Handler sHandler;
		
		BackThread(Handler handler){
			sHandler=handler;
		}
		public void run(){
			while(true){
				Message msg=Message.obtain();
				msg.what=0;
				
				int switch_fd = switchdriveropen();
				switch_value = switchread(switch_fd);
				
				msg.arg1=switch_value;
				switchdriverclose(switch_fd);
				sHandler.sendMessage(msg);
				try{Thread.sleep(musicSpeed - 70);}
				catch(InterruptedException e){
				}
			}
		}
	}
	
	BackThread mThread;
	Typeface myTypeFace;
	
	//게임 시작 전 clear
	private void beforeStartGame(){
		currentIndex = 0;
		
		tv.setText(str);
        feverTextView.setText("");
        
        playLayout.setVisibility(View.INVISIBLE);
        trackLayout.setVisibility(View.INVISIBLE);
        backButton.setVisibility(View.INVISIBLE);
	}
	
	//자유 플레이 모드 전 초기화
	private void freePlayInit(){
		currentSong = 100;
		str = " 지금우리는언제슬프고어려운일을만날거지만알고있어요모든달갑지않은고난이날사로잡겠지호우지금우리는슬프고어려운일이당신을어떻게만들지만알고있어요힘들고힘들겠지흠이제우리는언제우리가슬픔과어려움을이겨낼지를기억해요모든달갑지않은고난은힘을잃겠지호우이제우리는슬프고어려운일이더이상아무것도아니란사실을기억해요";
		interval = "";
		for(int i=0; i<str.length(); i++) interval += "0";
		beforeStartGame();
		backButton.setVisibility(View.VISIBLE);
	}
	
	//비행기 노래 플레이 전 초기화
	private void airplaneInit(){
		currentSong = 1;
		str = "준비시작미미미레도도레레미쉼미쉼미쉼레쉼레쉼레쉼미쉼솔쉼솔쉼미미미레도도레레미쉼미쉼미쉼레레쉼레미미레레도도";
        interval = "000033321122303030202020305050333211223030302202332211";
        beforeStartGame();
	}
	
	//미녀는 석류를 좋아해 노래 플레이 전 초기화
	private void beautyInit(){
		currentSong = 2;
		str = "준비시작라라도도라라솔솔라라솔솔파파파쉼파파파레도레파솔라라솔솔파파파쉼파파파레도레파솔라라솔솔라라라쉼라라도도도도쉼쉼쉼쉼쉼쉼쉼쉼라라도도라라솔솔라라솔솔파파";
        interval = "00006688665566554440444212456655444044421245665566606688880000000066886655665544";
        beforeStartGame();
	}
	
	//사과같은 내얼굴 노래 플레이 전 초기화
	private void appleInit(){
		currentSong = 3;
		str = "준비시작도레미쉼미쉼파미레레레쉼레미파쉼파쉼솔파미미미쉼미파솔쉼솔쉼도라솔쉼솔쉼도레미쉼미쉼레레도도도쉼";
		interval = "0000123030432220234040543330345050865050123030221110";
		beforeStartGame();
	}
	
	/* textView 초기화, animation 설정, ndk library 불러오기, 음악 파일 불러오기, 글꼴 설정 */
	private void init(){
        tv.setText("");
        feverTextView.setText("");
        airplaneScoreText.setText("My best score : 0");
        beautyScoreText.setText("My best score : 0");
        appleScoreText.setText("My best score : 0");
        
		/*fever text animation : 반짝거리게 설정*/
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(100);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        feverTextView.startAnimation(anim);
        
        System.loadLibrary("ndk-exam");
        driverwrite(0); fndwrite(0);
        
		/* 계이름 음악 파일 불러오기 */
        Doremi = new MediaPlayer[10];
        Doremi[0] = MediaPlayer.create(this, R.raw.re); Doremi[1] = MediaPlayer.create(this, R.raw.dol); Doremi[2] = MediaPlayer.create(this, R.raw.re);
        Doremi[3] = MediaPlayer.create(this, R.raw.mi); Doremi[4] = MediaPlayer.create(this, R.raw.fa); Doremi[5] = MediaPlayer.create(this, R.raw.sol);
        Doremi[6] = MediaPlayer.create(this, R.raw.la); Doremi[7] = MediaPlayer.create(this, R.raw.si); Doremi[8] = MediaPlayer.create(this, R.raw.high_dol);
        Doremi[9] = MediaPlayer.create(this, R.raw.high_re);
	
		/* 글꼴 설정 */
        myTypeFace = Typeface.createFromAsset(getAssets(), "fonts/jalnan.ttf");
        TextView title = (TextView)findViewById(R.id.textView1);
        title.setTypeface(myTypeFace); airplaneButton.setTypeface(myTypeFace); appleButton.setTypeface(myTypeFace);
        beautyButton.setTypeface(myTypeFace); backButton.setTypeface(myTypeFace); freePlayButton.setTypeface(myTypeFace);
        track1play.setTypeface(myTypeFace); track2play.setTypeface(myTypeFace);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        playLayout = findViewById(R.id.playLayout);
        trackLayout = findViewById(R.id.trackLayout);
        
        tv = (TextView)findViewById(R.id.str_text); feverTextView = (TextView)findViewById(R.id.fever_text);
        airplaneScoreText = (TextView)findViewById(R.id.airplaneScore); beautyScoreText = (TextView)findViewById(R.id.beautyScore);
        appleScoreText = (TextView)findViewById(R.id.appleScore);
        
        backButton = (Button)findViewById(R.id.backButton); airplaneButton = (Button)findViewById(R.id.airplaneButton);
        beautyButton = (Button)findViewById(R.id.beautyButton); appleButton = (Button)findViewById(R.id.appleButton);        
        freePlayButton = (Button)findViewById(R.id.freeButton); track1play = (Button)findViewById(R.id.track1play); track2play = (Button)findViewById(R.id.track2play);
        
        init();
        
        mThread=new BackThread(mHandler);
		mThread.setDaemon(true);
		mThread.start();
        
        airplaneButton.setOnClickListener(new View.OnClickListener() { //비행기 노래 플레이 버튼
        	
			@Override
			public void onClick(View v) {
				airplaneInit();
			}
		});
        
        beautyButton.setOnClickListener(new View.OnClickListener() { //미녀는 석류를 좋아해 플레이 버튼
			
			@Override
			public void onClick(View v) {
				beautyInit();
			}
		});
        
        appleButton.setOnClickListener(new View.OnClickListener() { //사과 같은 내얼굴 플레이 버튼
			
			@Override
			public void onClick(View v) {
				appleInit();
			}
		});

        freePlayButton.setOnClickListener(new View.OnClickListener() { //자유 플레이 모드
			
			@Override
			public void onClick(View v) {
				// 새로운 창 띄우기
				Intent intent = new Intent(getApplicationContext(), FreePlayActivity.class);
				startActivityForResult(intent, 1234);
			}
		});
        
        track1play.setOnClickListener(new View.OnClickListener() {//Track A 플레이
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				currentSong = 1001;
				str = track1;
				currentIndex = 0;
				Intent intent = new Intent(getApplicationContext(), TrackPlayActivity.class);
				intent.putExtra("str", str.length());
				startActivityForResult(intent, 5678);
			}
		});
        
        track2play.setOnClickListener(new View.OnClickListener() {//Track B 플레이
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				currentSong = 1002;
				str = track2;
				currentIndex = 0;
				Intent intent = new Intent(getApplicationContext(), TrackPlayActivity.class);
				intent.putExtra("str", str.length());
				startActivityForResult(intent, 5678);
			}
		});
        
        backButton.setOnClickListener(new View.OnClickListener() {//Complete 버튼 클릭
			
			@Override
			public void onClick(View v) {
				feverTime = false;
				consecutive = 0;
				feverConsecutive = 0;
				driverwrite(0);
				for(int i=0; i<10; i++){
					if(Doremi[i].isPlaying())
						Doremi[i].pause();
				}
				fndwrite(0);
				str = "";
				tv.setBackgroundColor(0x00fafafa);
				backButton.setVisibility(View.INVISIBLE);
				playLayout.setVisibility(View.VISIBLE);
				trackLayout.setVisibility(View.VISIBLE);
				tv.setText("");
				feverTextView.setText("");
				if(currentSong < 4){
					if(score > myBestScore[currentSong]){
						myBestScore[currentSong] = score;
						if(currentSong == 1) airplaneScoreText.setText("My best score : "+myBestScore[1]);
						else if(currentSong == 2) beautyScoreText.setText("My best score : "+myBestScore[2]);
						else if(currentSong == 3) appleScoreText.setText("My best score : "+myBestScore[3]);
					}
					score = 0;
				}
			}
		});
       
        //노래 속도 조절
        SeekBar speedBar = (SeekBar)findViewById(R.id.seekBar);
        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {	
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				musicSpeed = 400 + progress;
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 5678 && resultCode == RESULT_OK){ //track A or B 플레이
			int res = data.getIntExtra("back", 0);
			if(res == 1){//Back 버튼 누를 경우 현재 나오는 노래 중지
				str = "";
				for(int i=0; i<10; i++){
					if(Doremi[i].isPlaying()) Doremi[i].pause();
				}
			}
		}
		else if(requestCode == 1234 && resultCode == RESULT_OK){//자유플레이모드 알림창
			int trackNum = data.getIntExtra("track", 0);

			freePlayInit();
			if(trackNum == 1){//track A 저장
				track1 = "";
				currentSong = 101;
			}
			else if(trackNum == 2){//track B 저장
				track2 = "";
				currentSong = 102;
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onDestroy(){
		for(int i=0; i<10; i++)
			if(Doremi[i].isPlaying())
				Doremi[i].pause();
		str="";
		super.onDestroy();
	}

}