package com.example.mysecondapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.triggertrap.seekarc.SeekArc;

public class CustomSeekArc extends SeekArc {

    public CustomSeekArc(Context context) {
        super(context);
    }
    public CustomSeekArc(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSeekArc(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setProgress(int progress, boolean animated, long duration, int currentProgress) {
        if (animated) {
            SeekArcAnimation animation = new SeekArcAnimation(this, currentProgress, progress);
            animation.setDuration(duration);
            startAnimation(animation);
        }
    }



    public class SeekArcAnimation extends Animation {
        private SeekArc seekArc;
        private float from;
        private float to;

        public SeekArcAnimation(SeekArc seekArc, float from, float to) {
            super();

            this.seekArc = seekArc;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);

            float value = from + (to - from) * interpolatedTime;
            seekArc.setProgress((int)value);
        }
    }
}
