package com.example.administrator.customswitchdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 颜厥共 on 2016/4/1.
 * email:644613693@qq.com
 */
public class CustomSwitch extends View {

    private Paint textAndPointPaint = new Paint();
    /**
     * 小点的半径，dp为单位
     */
    private int pointRadii=3;
    private int xinfengPointColor=0xff552233;
    private int circlePointColor=0xff446633;
    private int circleRadii=2;
    private int xinfengSize=18;
    private int strsSize=16;
    /**
     * 未被选中时点跟字体的颜色
     */
    private int textAndPointColorN=0xff899888;
    /**
     * 被选中时点跟字体的颜色
     */
    private int textAndPointColorS=0xffff0000;
    /**
     * 中间的指针
     */
    private Bitmap pointerBitmap;
    private Point point;
    /**
     * 画小点圆形弧度的容器
     */
    private RectF rectF;
    /**
     * 测量字体大小的容器
     */
    private Rect rect;
    /**
     * 存放字体的点击范围
     */
    private List<Rect> rects;
    /**
     * 保存已经记录字的点击范围
     */
    private boolean isSaveRects;
    /**
     * 平滑过渡的线程
     */
    private SmoothSlide smoothSlide;

    private boolean onOffSwitch;
    /**
     * 当前档位
     */
    private int shift;
    /**
     * 当前角度
     */
    private float degree=180;

    /**
     * 屏幕上显示的点的个数和名称
     */
    String[] strs={
            "0","1","2","3","离家"
    };

    String xinfeng="新风";

    /**
     * 回调
     */
    CustomSwitchCallBack callBack;

    public CustomSwitch(Context context) {
        this(context,null);
    }

    public CustomSwitch(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public interface CustomSwitchCallBack{
        /**
         * @param position 被选择的项
         */
        void selected(int position);

        /**
         * @param offset 当前指针的角度
         */
        void offset(float offset);

        /**
         * @param state 开关状态回调
         */
        void state(boolean state);
    }

    /**
     * 设置回调
     * @param callBack
     */
    public void setCallBack(CustomSwitchCallBack callBack){
        this.callBack=callBack;
    }

    /**
     * 设置开关
     * @param onOffSwitch
     */
    public void setOnOffSwitch(boolean onOffSwitch){
        this.onOffSwitch=onOffSwitch;
        if (callBack!=null){
            callBack.state(onOffSwitch);
        }
        if (!getOnOffSwitch()){
            Log.d("CustomSwitch","当前开关为关，强制档位归0");
            this.shift=0;
        }
        smooth(shiftToDegree(shift));
    }

    public boolean getOnOffSwitch(){
        return onOffSwitch;
    }

    /**
     * 设置档位
     * @param shift
     */
    public void setShift(int shift){
        if (!onOffSwitch){
            Log.d("CustomSwitch","当前开关为关闭状态");
            return;
        }
        this.shift=shift;
        smooth(shiftToDegree(shift)) ;
    }

    /**
     * 获取当前档位
     * @return
     */
    private int getShift(){
        return shift;
    }

    /**
     * 档位换角度
     * @param shift 档位
     * @return 根据 strs的长度 返回每个str所在的角度
     */
    private float shiftToDegree(int shift){
        return shift*180/(strs.length-1)+180;
    }

    /**
     * 角度换档位
     * @param degree 角度
     * @return 根据角度得出最近的档位
     */
    private synchronized int degreeToShift(float degree){
        for (int i=0;i<6;i++){
            if (degree>i*45+157.5&&degree<=(i+1)*45+157.5){
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取当前的角度
     * @return
     */
    private float getCurrentDegree(){
        return degree;
    }

    /**
     * @param degree 设置当前的角度
     */
    private void setCurrentDegree(float degree){
        this.degree=degree;
    }


    /**
     * 画笔初始化
     */
    private void init(){
        textAndPointPaint.setStrokeCap(Paint.Cap.BUTT);
        textAndPointPaint.setAntiAlias(true);
        textAndPointPaint.setDither(true);
        rects=new ArrayList<>();
        rect=new Rect();
        onOffSwitch=true;//开关
    }


    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        width=widthSize;
        height=heightSize;
        if (heightMode==MeasureSpec.AT_MOST||heightMode==MeasureSpec.UNSPECIFIED){
            height=widthSize;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //变更图片的大小
        int bitmapWidth=w*4/9;
        int bitmapHeight=h*4/9;
        Bitmap bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.bitmap);
        pointerBitmap=zoomImg(bitmap,bitmapWidth,bitmapHeight);


        //设置外层小圆点的弧度大小，这里我取 3/4 大小
        int middleL = w/10;
        int middleT = w/4;
        int middleR = w - w/4;
        int middleB = w- w/4;
        rectF = new RectF(middleL, middleT, middleR, middleB);
        isSaveRects=false;
        point=new Point((int)rectF.centerX(),(int)rectF.centerY());

    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        rotateBitmap(canvas,getCurrentDegree());
        //画图片 或者 直接画字

    }

    private synchronized void rotateBitmap(Canvas canvas,float currentDegree){

        canvas.save();
        canvas.rotate(currentDegree,point.x,point.y);
        //获取图片的旋转中心
        textAndPointPaint.setColor(circlePointColor);
        canvas.drawCircle(point.x,point.y,pointerBitmap.getWidth()/2+DensityUtils.dp2px(getContext(),circleRadii),textAndPointPaint);
        canvas.drawBitmap(pointerBitmap,point.x-pointerBitmap.getWidth()/2,point.y-pointerBitmap.getHeight()/2,null);
        canvas.restore();
        drawPointAndText(canvas,currentDegree);
        if (callBack!=null){
            callBack.offset(currentDegree);
        }
        textAndPointPaint.setTextSize(DensityUtils.sp2px(getContext(),xinfengSize));//字体大小
        textAndPointPaint.setColor(xinfengPointColor);
        textAndPointPaint.getTextBounds(xinfeng,0,xinfeng.length(),rect);
        canvas.drawText("新风",point.x-rect.width()/2,point.y+rect.height()/2,textAndPointPaint);
    }

    /**
     * 画点和字
     * @param canvas
     * @param degree
     */
    private synchronized void drawPointAndText(Canvas canvas, float degree){
        float r=rectF.width()/2; //小圆点所在位置与圆心距离
        int circleRadii=DensityUtils.dp2px(getContext(),pointRadii);//小圆点的半径
        float x;//小圆点圆心x
        float y;//小圆点圆心Y
        float textX=0;//字体的位置x
        float textY=0;//字体的位置y
        int position= degreeToShift(degree);//获得大概的位置
        textAndPointPaint.setTextSize(DensityUtils.sp2px(getContext(),strsSize));//字体大小
        for (int i=0;i<strs.length;i++){
            if (i==position){
                textAndPointPaint.setColor(textAndPointColorS);
            }else {
                textAndPointPaint.setColor(textAndPointColorN);
            }
            x= (float) (point.x+r*Math.cos(shiftToDegree(i)*Math.PI/180));//三角函数 得出x坐标
            y= (float) (point.y+r*Math.sin(shiftToDegree(i)*Math.PI/180));//三角函数 得出y坐标
            canvas.drawCircle(x,y,circleRadii, textAndPointPaint);//画圆
            String string = strs[i];
            textAndPointPaint.getTextBounds(string,0,string.length(),rect);
            switch (i){//根据设计图计算字体摆放的位置
                case 0:
                    textX= x-rect.width()-circleRadii*2;
                    textY=y+rect.height()/2;
                    break;
                case 1:
                    textX=x-rect.width()-circleRadii*2;
                    textY=y+rect.height()/2;
                    break;
                case 2:
                    textX=x-rect.width()/2;
                    textY=y-circleRadii*2;
                    break;
                case 3:
                    textX=x+circleRadii;
                    textY=y+rect.height()/2;
                    break;
                case 4:
                    textX=x+circleRadii;
                    textY=y+rect.height()/2;
                    break;
            }
            canvas.drawText(string,textX,textY, textAndPointPaint);
            if (!isSaveRects){//记录字体点击的区域  如果想增加点击的区域 这里增加
                int left= (int) textX-circleRadii*2;
                int top= (int) (textY-rect.height())-circleRadii;
                int right= (int) (textX+rect.width()+circleRadii*3);
                int bottom= (int) textY+circleRadii;
                Rect rect=new Rect(left,top,right,bottom);
                rects.add(i,rect);
                if (i==strs.length-1){
                    isSaveRects=true;
                }
            }
        }

    }


    int oldX;//记录按下时候的x
    int oldY;//记录按下时候的y
    float oldDegree;//记录手指点击的相对角度
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!onOffSwitch){
            return false;
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (smoothSlide!=null){
                    smoothSlide.threadStop();//停止
                }
                oldX= (int) event.getX();
                oldY= (int) event.getY();
                Rect rect;
                for (int i=0;i<rects.size();i++){
                    rect=rects.get(i);
                    if (rect.contains(oldX,oldY)){
                        setShift(i);//当前档位
                        if (callBack!=null){
                            callBack.selected(getShift());
                        }
                        smooth(shiftToDegree(i));
                        return false;
                    }
                }
                float zhengqie = (float) Math.atan2(point.y-oldY,point.x-oldX);//三角函数 计算两点位置
                if(zhengqie < 0){
                    zhengqie = (float) (zhengqie + 2*Math.PI);//如果得到正切是负数，则把它转换
                }
                oldDegree= (float) (180*zhengqie/Math.PI);//转换成角度
                break;
            case MotionEvent.ACTION_MOVE:
                float x=event.getX();
                float y=event.getY();
                float zhengqie2 = (float) Math.atan2(point.y-y,point.x-x);
                if(zhengqie2 < 0){
                    zhengqie2 = (float) (zhengqie2 + 2*Math.PI);
                }
                float newDegree= (float) (180*zhengqie2/Math.PI);
                float changeDegree=newDegree-oldDegree;//角度变更的大小
                oldDegree=newDegree;//替换旧的相对位置
                changeDegree=getCurrentDegree()+changeDegree;//当前需要变更的大小
                if (changeDegree<0){//如果角度是负数，则加上360
                    changeDegree+=360;
                }
                changeDegree=changeDegree%360;//取模
                if (changeDegree>0&&changeDegree<180){//如果超出最大最小值，则不处理
                    return true;
                }
                setCurrentDegree(changeDegree);
                postInvalidate();
                return true;
            case MotionEvent.ACTION_UP:
                int position= degreeToShift(getCurrentDegree());//获取当前档位
                setShift(position);
                if (callBack!=null){
                        callBack.selected(getShift());
                }
                smooth(shiftToDegree(getShift()));
                break;
        }
        return true;
    }


    /**
     * 平滑过渡
     * @param degree 最终想要到达的角度
     */
    private void smooth(float degree){
        if (smoothSlide!=null){
            smoothSlide.threadStop();
        }
        smoothSlide=new SmoothSlide();
        smoothSlide.setDegress(degree);
        smoothSlide.start();
    }

    /**
     * 平滑处理的线程
     */
    private class SmoothSlide extends Thread{
        private float degree;
        boolean threadStop=false;

        public void setDegress(float degree){
            this.degree=degree;
        }

        /**
         * 停止线程标记
         */
        public void threadStop(){
            this.threadStop=true;
        }


        @Override
        public void run() {
            super.run();
            float current=getCurrentDegree();//当前角度
            float speed=Math.abs(degree-current)/60;//设置当前速度
            if (speed<1){//如果速度小于1 则强制设为1
                speed=1;
            }
            while (current!=degree&&!threadStop){
                if (current<degree){
                    current+=speed;
                    if (current > degree) {
                        current = degree;
                    }
                }else if (current>degree){
                    current-=speed;
                    if (current < degree) {
                        current = degree;
                    }
                }else {
                    current=degree;
                }
                setCurrentDegree(current);
                postInvalidate();
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 图片大小变换
     * @param bm
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap zoomImg(Bitmap bm, int newWidth , int newHeight){
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (smoothSlide!=null){
            smoothSlide.threadStop();
        }
        super.onDetachedFromWindow();
    }
}
