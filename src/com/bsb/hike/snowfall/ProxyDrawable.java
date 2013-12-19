package com.bsb.hike.snowfall;

import java.util.Random;

import com.bsb.hike.utils.Utils;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class ProxyDrawable extends Drawable {
    
    private Random random = new Random();
    private Drawable mProxy;
    public int size;
    
    public ProxyDrawable(Drawable target) {
    	size = random.nextInt(21);
    	if(size>9 && size<18)
    		size = 9;
    	if(size > 18 && random.nextInt(2)==0)
    		size = 9;
    	size = (int)(size*Utils.densityMultiplier)+1;
    	mProxy = target;
    }
    
    public Drawable getProxy() {
    	return mProxy;
    }
    
    public void setProxy(Drawable proxy) {
        if (proxy != this) {
            mProxy = proxy;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mProxy != null) {
            mProxy.draw(canvas);
        }
    }
    
    @Override
    public int getIntrinsicWidth() {
        return mProxy != null ? mProxy.getIntrinsicWidth() : -1;
    }
    
    @Override
    public int getIntrinsicHeight() {
        return mProxy != null ? mProxy.getIntrinsicHeight() : -1;
    }
    
    @Override
    public int getOpacity() {
        return mProxy != null ? mProxy.getOpacity() : PixelFormat.TRANSPARENT;
    }
    
    @Override
    public void setFilterBitmap(boolean filter) {
        if (mProxy != null) {
            mProxy.setFilterBitmap(filter);
        }
    }
    
    @Override
    public void setDither(boolean dither) {
        if (mProxy != null) {
            mProxy.setDither(dither);
        }
    }
    
    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (mProxy != null) {
            mProxy.setColorFilter(colorFilter);
        }
    }
    
    @Override
    public void setAlpha(int alpha) {
        if (mProxy != null) {
            mProxy.setAlpha(alpha);
        }
    }
}
    