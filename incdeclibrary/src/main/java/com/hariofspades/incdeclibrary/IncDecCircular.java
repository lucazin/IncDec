package com.hariofspades.incdeclibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.support.design.widget.FloatingActionButton;
import android.widget.TextView;
import android.widget.Toast;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.operators.observable.ObservableJust;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by Hari on 29/11/16.
 */

public class IncDecCircular extends RelativeLayout{

    /** Core Items*/
    private Context mContext;
    private AttributeSet attrs;
    private int styleAttr;
    private View view;

    /** Static Items*/
    public static String TYPE_INTEGER="INTEGER";
    public static String TYPE_FLOAT="FLOAT";
    public static String TYPE_ARRAY="ARRAY";
    private static final String TAG = "INKDEC";
    public static String INCREMENT="INCREMENT";
    public static String DECREMENT="DECREMENT";
    private static final int VERTICAL=LinearLayout.VERTICAL;
    private static final int HORIZONTAL=LinearLayout.HORIZONTAL;

    /** Attributes */
    private float initialValue=0;
    private float finalValue=Float.MAX_VALUE;
    private Float textSize;
    private int textColor;
    private Drawable leftSrc;
    private Drawable rightSrc;
    private int leftButtonTint;
    private int rightButtonTint;
    private int leftDrawableTint;
    private int rightDrawableTint;
    private float interval;
    private int int_val=1;
    private int orientation=LinearLayout.HORIZONTAL;

    /** Listeners */
    private OnValueChangeListener mValueListener;
    private OnClickListener mListener;

    /** Components */
    FloatingActionButton leftButton;
    FloatingActionButton rightButton;
    TextView counter;
    LinearLayout layout;

    /** Helpers */
    private float currentValue;
    private int currentValue_int;
    private int initialValue_int;
    private int finalValue_int;
    private ArrayList<String> array;
    private String type=TYPE_INTEGER;
    private String precesion="%.2f";
    private String leftType;
    private String rightType;
    private int index=0;
    private int startIndex;
    private boolean isLeftButtonLongPressed = false;
    private boolean isRightButtonLongPressed = false;
    private boolean leftLongPress=false;
    private boolean rightLongPress=false;
    private long seconds=500;
    private int oldIntValue;
    private int oldFloatValue;
    private int oldIndex;
    private int startIndexVal=0, stopIndexVal=0;

    /** subscribers **/
    DisposableSubscriber<Long> _Incrementsubscriber;
    DisposableSubscriber<Long> _Decrementsubscriber;

    public IncDecCircular(Context context) {
        super(context);
        this.mContext=context;
        initView();
    }

    public IncDecCircular(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext=context;
        this.attrs=attrs;
        initView();
    }

    public IncDecCircular(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        this.attrs=attrs;
        this.styleAttr=defStyleAttr;
        initView();
    }

    /** Intializing the view */
    private void initView(){
        this.view=this;
        inflate(mContext,R.layout.incdec_circular,this);
        final Resources resources = getResources();
        final int defaultColor = resources.getColor(R.color.colorPrimary);
        final int defaultTextColor = resources.getColor(R.color.colorTextPrimary);
        final int white=resources.getColor(R.color.white);
        /** Getting the attributes from attrs.xml */
        TypedArray arr = mContext.obtainStyledAttributes(attrs,R.styleable.IncDecCircular,
                styleAttr,0);
        leftButtonTint=arr.getColor(R.styleable.IncDecCircular_leftButtonColorTint,
                defaultColor);
        rightButtonTint=arr.getColor(R.styleable.IncDecCircular_rightButtonColorTinit,
                defaultColor);
        leftSrc=arr.getDrawable(R.styleable.IncDecCircular_leftDrawable);
        rightSrc=arr.getDrawable(R.styleable.IncDecCircular_rightDrawable);
        leftDrawableTint=arr.getColor(R.styleable.IncDecCircular_leftDrawableTint,white);
        rightDrawableTint=arr.getColor(R.styleable.IncDecCircular_rightDrawableTint,white);
        textSize=arr.getFloat(R.styleable.IncDecCircular_textSize,13);
        textColor=arr.getColor(R.styleable.IncDecCircular_textColor,defaultTextColor);
        /** Component declaration */
        leftButton=(FloatingActionButton) findViewById(R.id.decrement_button);
        rightButton=(FloatingActionButton) findViewById(R.id.increment_button);
        counter=(TextView) findViewById(R.id.number_counter);
        layout=(LinearLayout) findViewById(R.id.layout);
        /** Setup the views with attributes */
        setupLeftButton(leftButton,leftSrc,leftButtonTint,leftDrawableTint);
        setupRightButton(rightButton,rightSrc,rightButtonTint,rightDrawableTint);
        initLongClickListener(leftButton,rightButton,counter);

        arr.recycle();


    }


    /** Setting up the right button */
    private void setupRightButton(FloatingActionButton rightButton, Drawable rightSrc,
                                  int rightButtonTint, int rightDrawableTint) {
        if(rightSrc!=null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rightSrc.setTintList(new ColorStateList(new int[][]{new int[]{0}},
                        new int[]{rightDrawableTint}));
            }
            else
            {
                final Drawable wrappedDrawable = DrawableCompat.wrap(rightSrc);
                DrawableCompat.setTintList(wrappedDrawable, ColorStateList.valueOf(rightDrawableTint));
            }
            rightButton.setImageDrawable(rightSrc);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rightButton.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}},
                    new int[]{rightButtonTint}));
        }
        else
        {
            ViewCompat.setBackgroundTintList(rightButton, ColorStateList.valueOf(rightButtonTint));
        }
    }

    /** Setting up the left button */
    private void setupLeftButton(FloatingActionButton leftButton, Drawable leftSrc,
                                 int leftButtonTint, int leftDrawableTint) {
        if(leftSrc!=null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                leftSrc.setTintList(new ColorStateList(new int[][]{new int[]{0}},
                        new int[]{leftDrawableTint}));
            }
            else
            {
                final Drawable wrappedDrawable = DrawableCompat.wrap(leftSrc);
                DrawableCompat.setTintList(wrappedDrawable, ColorStateList.valueOf(leftDrawableTint));
            }
            leftButton.setImageDrawable(leftSrc);

        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            leftButton.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}},
                    new int[]{leftButtonTint}));
        }
        else
        {
            ViewCompat.setBackgroundTintList(leftButton, ColorStateList.valueOf(leftButtonTint));
        }

    }

    /** Setting up width/height */
    private void setupConfiguration(int orientation, String type) {
        if(orientation==LinearLayout.VERTICAL)
            setLayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT);
        else
            setLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,0);

        fixOrientation(layout,orientation);

        if(type.equals(TYPE_ARRAY)){

        }else if(type.equals(TYPE_FLOAT)){

        }else{

        }
    }

    /** Settign up orientation*/
    private void fixOrientation(LinearLayout layout, int orientation) {
        layout.setOrientation(orientation);
    }

    /** Layout params */
    public void setLayoutParams(int first, int second){
        leftButton.getLayoutParams().height=first;
        leftButton.getLayoutParams().width=second;
        rightButton.getLayoutParams().height=first;
        rightButton.getLayoutParams().width=second;
        counter.getLayoutParams().height=first;
        counter.getLayoutParams().width=second;
    }


    /** Setting up the counter */
    private void setTextCounter(TextView counter, Float textSize, int textColor, float initialValue
    ,int flag,float currentValue) {
        if(flag==0)
            counter.setText(String.valueOf(currentValue));
        else if(flag==1)
            counter.setText(String.valueOf(currentValue));
        else
            counter.setText(String.valueOf((int)currentValue));
        counter.setTextSize(textSize);
        counter.setTextColor(textColor);
    }

    /** get Configuration information */
    public void setConfiguration(int orientation,String type,String leftType,String rightType){
        this.orientation=orientation;
        this.type=type;
        setupConfiguration(this.orientation,this.type);
        this.leftType=leftType;
        this.rightType=rightType;
    }

    /** Setup the first and last value*/
    public void setupValues(float initialValue,float finalValue, float interval,float startValue){
        this.initialValue=initialValue;
        this.finalValue=finalValue;
        this.interval=interval;
        if(type.equals(TYPE_ARRAY)){

        }if(type.equals(TYPE_FLOAT)){
            currentValue=startValue;
            setTextCounter(counter,textSize,textColor,initialValue,1,currentValue);
        }else if(type.equals(TYPE_INTEGER)){
            finalValue_int=(int)finalValue;
            initialValue_int=(int)initialValue;
            currentValue_int=(int)startValue;
            setTextCounter(counter,textSize,textColor,initialValue_int,2,currentValue_int);
        }
    }

    /** Setup the value*/
    public void setValue(float startValue){

       if(type.equals(TYPE_FLOAT)){
            setTextCounter(counter,textSize,textColor,initialValue,1,startValue);
        }else if(type.equals(TYPE_INTEGER)) {
           setTextCounter(counter,textSize,textColor,initialValue_int,2,startValue);
        }

    }


    /** Setup the array value*/
    public void setArrayValue(int index){
        try {
            this.startIndex = index;
            counter.setText(array.get(startIndex));
        }catch (Exception e){
           e.printStackTrace();
        }
    }

    /** Click listeners */
    private void initClickListener(ImageButton leftButton, ImageButton rightButton, final TextView counter) {
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!leftLongPress) {
                    if (leftType.equals(INCREMENT))
                        IncrementAction();
                    else if (leftType.equals(DECREMENT))
                        DecrementAction();
                    else
                        Log.e(TAG, "invalid Type");
                }
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!rightLongPress) {
                    if (rightType.equals(INCREMENT))
                        IncrementAction();
                    else if (rightType.equals(DECREMENT))
                        DecrementAction();
                    else
                        Log.e(TAG, "invalid Type");
                }
            }
        });
    }


    /** Setup the long press*/
    private void initLongClickListener(final ImageButton leftButton, ImageButton rightButton,
                                       final TextView counter){
        leftButton.setOnTouchListener(speakLeftTouchListener);
        leftButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(leftLongPress) {
                    isLeftButtonLongPressed = true;
                    if (leftType.equals(INCREMENT))
                        startIncrementObserver();
                    else if (leftType.equals(DECREMENT))
                        startDecrementObserver();
                    else
                        Log.e(TAG, "invalid Type");
                    return true;
                }else {
                    return false;
                }
            }
        });

        rightButton.setOnTouchListener(speakRightTouchListener);
        rightButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(rightLongPress) {
                    isRightButtonLongPressed = true;
                    if (rightType.equals(INCREMENT))
                        startIncrementObserver();
                    else if (rightType.equals(DECREMENT))
                        startDecrementObserver();
                    else
                        Log.e(TAG, "invalid Type");
                    return true;
                }else{
                    return false;
                }
            }
        });

    }

    /** Touch listeners*/
    private View.OnTouchListener speakLeftTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View pView, MotionEvent pEvent) {
            pView.onTouchEvent(pEvent);
            if (pEvent.getAction() == MotionEvent.ACTION_UP) {
               if(leftLongPress){
                    if (isLeftButtonLongPressed) {
                        isLeftButtonLongPressed = false;
                    }else{
                        if(leftType.equals(INCREMENT))
                            IncrementAction();
                        else if(leftType.equals(DECREMENT))
                            DecrementAction();
                        else
                            Log.e(TAG,"invalid Type");
                    }
                }

            }
            return true;
        }
    };

    private View.OnTouchListener speakRightTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View pView, MotionEvent pEvent) {
            pView.onTouchEvent(pEvent);
            if (pEvent.getAction() == MotionEvent.ACTION_UP) {
                if(rightLongPress){
                    if (isRightButtonLongPressed) {
                        isRightButtonLongPressed = false;
                    }else{
                        if(rightType.equals(INCREMENT))
                            IncrementAction();
                        else if(rightType.equals(DECREMENT))
                            DecrementAction();
                        else
                            Log.e(TAG,"invalid Type");
                    }
                }

            }
            return true;
        }
    };

    /** Thread for decrement*/
    private void startDecrementTimerThread() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            private long startTime = System.currentTimeMillis();
            public void run() {
                while (isLeftButtonLongPressed||isRightButtonLongPressed) {
                    try {
                        Thread.sleep(seconds);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable(){
                        public void run() {
                            DecrementAction();
                        }
                    });
                }
            }
        };
        new Thread(runnable).start();
    }

    /** Thread for increment */
    private void startIncrementTimerThread() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            private long startTime = System.currentTimeMillis();
            public void run() {
                while (isLeftButtonLongPressed||isRightButtonLongPressed) {
                    try {
                        Thread.sleep(seconds);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable(){
                        public void run() {
                            IncrementAction();
                        }
                    });
                }
            }
        };
        new Thread(runnable).start();
    }

    /** Perform the increment action */
    public void IncrementAction(){
        if(type.equals(TYPE_ARRAY)){
            try {
                oldIndex = index;
                if (checkIncVaidation(index, startIndexVal, stopIndexVal)) {
                    index = index + int_val;
                    counter.setText(array.get(index));
                    callArryListener(this, oldIndex, index);
                } else {
                    index = stopIndexVal;
                    counter.setText(array.get(stopIndexVal));
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }else if(type.equals(TYPE_FLOAT)){
            float num= Float.parseFloat(counter.getText().toString());
            setFloatNumber(num+interval, true,num);
        }else if(type.equals(TYPE_INTEGER)){
            int num= Integer.parseInt(counter.getText().toString());
            setIntNumber(num+(int)interval,true,num);
        }else
            Log.e(TAG,"error");
    }




    private boolean checkIncVaidation(int startValue, int minimum, int maximum){
        if((startValue>=minimum && startValue<=maximum)&&(startValue!=maximum)) {
            return true;
        }
        else {
            return false;
        }
    }


    private boolean checkDecVaidation(int startValue, int minimum, int maximum){
        if((startValue>=minimum && startValue<=maximum)&&(startValue!=minimum)) {
            return true;
        }
        else {
            return false;
        }
    }


    /** Decrement Action */
    private void DecrementAction() {
        if(type.equals(TYPE_ARRAY)){
            try {
                oldIndex = index;
                if (checkDecVaidation(index, startIndexVal, stopIndexVal)) {
                    index = index - int_val;
                    counter.setText(array.get(index));
                    callArryListener(this, oldIndex, index);
                } else {
                    index = startIndexVal;
                    counter.setText(array.get(startIndexVal));
                }
            }catch (Exception e){}
        }else if(type.equals(TYPE_FLOAT)){
            float num= Float.parseFloat(counter.getText().toString());
            setFloatNumber(num-interval, true,num);
        }else if(type.equals(TYPE_INTEGER)){
            int num= Integer.parseInt(counter.getText().toString());
            setIntNumber(num-(int)interval,true,num);
        }else
            Log.e(TAG,"error");
    }

    /** Set float value */
    public void setFloatNumber(float number, boolean notifyListener,float num){
        setFloatNumber(number);
        if(notifyListener)
        {
            callFloatListener(this,num,number);
        }
    }

    /** Set int value */
    public void setIntNumber(int number,boolean notifyListener,int oldnum){
        setIntNumber(number);
        if(notifyListener)
        {
            callIntListener(this,oldnum,number);
        }
    }

    /** Configure longress */
    public void enableLongPress(boolean leftLongpress,boolean rightLongpress,long seconds){
        this.leftLongPress=leftLongpress;
        this.rightLongPress=rightLongpress;
        this.seconds=seconds;
        if(!leftLongPress||!rightLongPress) {
            initClickListener(leftButton, rightButton, counter);
        }
    }

    /** Setup precesion**/
    public void setprecision(String precision){
        this.precesion=precision;
    }

    /** Set float number */
    @SuppressLint("DefaultLocale")
    public void setFloatNumber(float number){
        //finalValue = currentValue;
        this.currentValue = number;
        if(this.currentValue > finalValue)
        {
            this.currentValue = finalValue;
        }
        if(this.currentValue < initialValue)
        {
            this.currentValue = initialValue;
        }
        counter.setText(String.format(precesion,currentValue));
    }

    /** Setup the int number */
    public void setIntNumber(int number){
        this.currentValue_int=number;
        if(this.currentValue_int>finalValue_int)
            this.currentValue_int=finalValue_int;
        if(this.currentValue_int<initialValue_int)
            this.currentValue_int=initialValue_int;
        counter.setText(String.valueOf(currentValue_int));
    }

    /** Int listener */
    public void callIntListener(View view, int oldvalue, int newValue){
        if(mListener!=null)
            mListener.onClick(view);
        if(mValueListener!=null){
            if(finalValue!=currentValue)
                mValueListener.onValueChange(this,oldvalue,newValue);
        }
    }

    /** Float listener */
    public void callFloatListener(View view, float oldvalue, float newValue){
        if(mListener!=null)
            mListener.onClick(view);
        if(mValueListener!=null){
            if(finalValue!=currentValue)
                mValueListener.onValueChange(this,oldvalue,newValue);
        }
    }


    /** Array listener */
    public void callArryListener(View view, int oldIndex,int newIndex){
        if(mListener!=null)
            mListener.onClick(view);
        if(mValueListener!=null){
            if(finalValue!=currentValue)
                mValueListener.onValueChange(this,oldIndex,newIndex);
        }
    }

    /** Rx Java **/

    private void startIncrementObserver(){

        _Incrementsubscriber=new DisposableSubscriber<Long>() {

            @Override
            public void onNext(Long aLong) {
                if(isLeftButtonLongPressed||isRightButtonLongPressed) {
                    IncrementAction();
                }
                else
                    _Incrementsubscriber.dispose();

            }

            @Override
            public void onError(Throwable t) {
                Log.i(TAG,t.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };

        Flowable.interval(0,seconds,TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_Incrementsubscriber);

    }

    private void startDecrementObserver(){

        _Decrementsubscriber=new DisposableSubscriber<Long>() {

            @Override
            public void onNext(Long aLong) {
                if(isLeftButtonLongPressed||isRightButtonLongPressed) {
                    DecrementAction();
                }
                else
                    _Decrementsubscriber.dispose();

            }

            @Override
            public void onError(Throwable t) {
                Log.i(TAG,t.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };

        Flowable.interval(0,seconds,TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_Decrementsubscriber);


    }


    public void IncrementOperation(){
        if(type.equals(TYPE_ARRAY)){
            try {
                oldIndex = index;
                if (checkIncVaidation(index, startIndexVal, stopIndexVal)) {
                    index = index + int_val;
                    counter.setText(array.get(index));
                    callArryListener(this, oldIndex, index);
                } else {
                    index = stopIndexVal;
                    counter.setText(array.get(stopIndexVal));
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }else if(type.equals(TYPE_FLOAT)){
            float num= Float.parseFloat(counter.getText().toString());
            setFloatNumber(num+interval, true,num);
        }else if(type.equals(TYPE_INTEGER)){
            int num= Integer.parseInt(counter.getText().toString());
            setIntNumber(num+(int)interval,true,num);
        }else
            Log.e(TAG,"error");
    }

    /** **/


    /** Setting arraylist */
    public void setArrayList(ArrayList<String> arrayList){
        this.array=arrayList;
    }

    public void setArrayInitialization(int interval,int startIndex){
        this.int_val=interval;
        this.startIndex=startIndex;
        this.index=startIndex;
        counter.setText(array.get(startIndex));
    }

    public void setArrayIndexes(int startIndexVal, int stopIndexVal, int interval){
        this.startIndexVal=startIndexVal;
        this.stopIndexVal=stopIndexVal;
        this.int_val=interval;
        this.index=startIndexVal;
        counter.setText(array.get(startIndexVal));
    }

    /** Receive the value */
    public String getValue(){
        return counter.getText().toString();
    }

    /** Get the current index */
    public int getCurrentIndex(){
        return index;
    }

    /** Interfaces */
    public interface OnClickListener {
        void onClick(View view);
    }

    public interface OnValueChangeListener {
        void onValueChange(IncDecCircular view,float oldValue,float newValue);
    }

    /** Interface object receivers */
    public void setOnClickListener(OnClickListener listener){
        this.mListener=listener;
    }

    public void setOnValueChangeListener(OnValueChangeListener listener){
        this.mValueListener=listener;
    }


}
