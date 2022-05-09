package com.erif.readmoretextview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class TextViewReadMore extends AppCompatTextView {

    private static final String DEFAULT_EXPAND_TEXT = "Read More";
    private static final String DEFAULT_COLLAPSE_TEXT = "Close";

    private SpannableStringBuilder builder;
    private boolean expand = false;
    private int originalMaxLines = 0;
    private int originalHeight = 0;
    private int maxHeight = 0;
    private String originalText = null;
    private String snippedText = null;
    private boolean animating = false;

    private String expandText = null;
    private int expandTextColor;
    private int expandTextStyle = 0;
    private boolean expandTextUnderline = false;

    private String collapseText;
    private int collapseTextColor;
    private int collapseTextStyle = 0;
    private boolean collapseTextUnderline = false;

    private int animationDuration = 200;

    private TextViewReadMoreCallback callback;

    public void actionListener(TextViewReadMoreCallback callback) {
        this.callback = callback;
    }

    public TextViewReadMore(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public TextViewReadMore(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public TextViewReadMore(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public void init(Context context, AttributeSet attrs, int defStyleAttr) {
        Resources.Theme theme = context.getTheme();
        builder = new SpannableStringBuilder();
        if (theme != null) {
            TypedArray typedArray = theme.obtainStyledAttributes(
                    attrs, R.styleable.TextViewReadMore, defStyleAttr, 0
            );
            try {
                String getExpandText = typedArray.getString(R.styleable.TextViewReadMore_expandText);
                expandText = TextUtils.isEmpty(getExpandText) ? DEFAULT_EXPAND_TEXT : getExpandText;
                expandTextColor = typedArray.getColor(R.styleable.TextViewReadMore_expandTextColor, Color.BLUE);
                expandTextStyle = typedArray.getInt(R.styleable.TextViewReadMore_expandTextStyle, 0);
                expandTextUnderline = typedArray.getBoolean(R.styleable.TextViewReadMore_expandTextUnderline, false);

                String getCollapseText = typedArray.getString(R.styleable.TextViewReadMore_collapseText);
                collapseText = TextUtils.isEmpty(getCollapseText) ? DEFAULT_COLLAPSE_TEXT : getCollapseText;
                collapseTextColor = typedArray.getColor(R.styleable.TextViewReadMore_collapseTextColor, Color.BLUE);
                collapseTextStyle = typedArray.getInt(R.styleable.TextViewReadMore_collapseTextStyle, 0);
                collapseTextUnderline = typedArray.getBoolean(R.styleable.TextViewReadMore_collapseTextUnderline, false);

                originalMaxLines = getMaxLines();

                int getAnimationDuration = typedArray.getInt(R.styleable.TextViewReadMore_android_animationDuration, animationDuration);
                if (getAnimationDuration > 1000) {
                    animationDuration = 1000;
                } else animationDuration = Math.max(getAnimationDuration, 100);
            } finally {
                typedArray.recycle();
            }
        } else {
            debug("Theme is null");
        }
        setOnClickListener(v -> {
            if ((snippedText != null && getText().toString().equals(snippedText+expandText)) ||
                    (originalText != null && getText().toString().equals(originalText+" "+collapseText))
            ) {
                expand = !expand;
                readMore();
            }
        });
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        debug("On Draw");
        //debug("Expand: "+expand+" - Anim: "+animating);
        if (!animating) {
            if (getVisibility() == View.VISIBLE && getLineCount() > 0 && getLayout() != null) {
                if (getLineCount() > originalMaxLines) {
                    if (expand) {
                        if (originalText != null && !getText().toString().equals(originalText+" "+collapseText)) {
                            //setWrapContent();
                            String dots = "... ";
                            int lastCharDown = getLayout().getLineVisibleEnd(originalMaxLines - 1);
                            int end = lastCharDown - dots.length() - expandText.length();
                            String mainText = getText().toString().subSequence(0, end).toString();
                            originalText = getText().toString();
                            snippedText = mainText + dots;
                            measuring();
                            createExpandButton();
                            expand = false;
                        }
                    } else {
                        int lastCharDown = getLayout().getLineVisibleEnd(originalMaxLines - 1);
                        String dots = "... ";
                        int end = lastCharDown - dots.length() - expandText.length();
                        String mainText = getText().toString().subSequence(0, end).toString();
                        originalText = getText().toString();
                        snippedText = mainText + dots;
                        measuring();
                        createExpandButton();
                    }
                } else {
                    if (originalText != null && !getText().toString().equals(originalText+" "+collapseText)) {
                        setWrapContent();
                    }
                    int getEllipsize = getLayout().getEllipsisCount(getLineCount() - 1);
                    if (getEllipsize > 0) {
                        measuring();
                        createSnipped(getEllipsize);
                    }
                }
            }
        }
    }

    private void measuring() {
        if (originalMaxLines == 0)
            originalMaxLines = Math.max(getMaxLines(), 1);
        if (originalHeight == 0)
            originalHeight = getMeasuredHeight();
        setMaxLines(Integer.MAX_VALUE);
        measure(measureValue(getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                measureValue(0, View.MeasureSpec.UNSPECIFIED));
        maxHeight = getMeasuredHeight();
        setMaxLines(originalMaxLines);
    }

    private void createSnipped(int ellipsis) {
        String dots = "... ";
        int end = getText().toString().length() - (ellipsis + dots.length() + expandText.length());
        String mainText = getText().toString().subSequence(0, end).toString();
        originalText = getText().toString();
        snippedText = mainText + dots;
        createExpandButton();
    }

    private void createExpandButton() {
        builder.clear();
        String snipped = snippedText+expandText;
        builder.append(snipped);
        expandTextStyle();
        setText(builder);
    }

    private void createCollapseButton() {
        builder.clear();
        String fullText = originalText+" ";
        builder.append(fullText);
        int start = builder.toString().length();
        builder.append(collapseText);
        collapseTextStyle(builder, start);
        setText(builder);
    }

    private void readMore() {
        int fromHeight = expand ? originalHeight : maxHeight;
        int targetHeight = expand ? maxHeight : originalHeight;
        if (expand) {
            setText(originalText);
            setMaxLines(Integer.MAX_VALUE);
        }
        ValueAnimator animation = ValueAnimator.ofInt(fromHeight, targetHeight);
        animation.setDuration(animationDuration);
        ViewGroup.LayoutParams param = getLayoutParams();
        animation.addUpdateListener(valueAnimator -> {
            param.height = (int) valueAnimator.getAnimatedValue();
            setLayoutParams(param);
        });
        animation.addListener(animatorListener());
        animation.start();
    }

    private AnimatorListenerAdapter animatorListener() {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                animating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animating = false;
                if (expand) {
                    createCollapseButton();
                    if (callback != null) callback.onExpand();
                } else {
                    setWrapContent();
                    setMaxLines(originalMaxLines);
                    builder.clear();
                    builder.append(snippedText);
                    setText(builder);
                    createExpandButton();
                    if (callback != null) callback.onCollapse();
                }
            }
        };
    }

    private void expandTextStyle() {
        int start = snippedText.length();
        int end = expandText.length();
        builder.setSpan(new ForegroundColorSpan(expandTextColor), start, start + end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        if (expandTextStyle == 1) {
            boldText(start, end);
        } else if (expandTextStyle == 2) {
            italicText(start, end);
        } else if (expandTextStyle == 3) {
            boldText(start, end);
            italicText(start, end);
        }
        if (expandTextUnderline) {
            underlineText(start, end);
        }
    }

    private void collapseTextStyle(SpannableStringBuilder builder, int start) {
        int end = collapseText.length();
        builder.setSpan(new ForegroundColorSpan(collapseTextColor), start, start + end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        if (collapseTextStyle == 1) {
            boldText(start, end);
        } else if (collapseTextStyle == 2) {
            italicText(start, end);
        } else if (collapseTextStyle == 3) {
            boldText(start, end);
            italicText(start, end);
        }
        if (collapseTextUnderline) {
            underlineText(start, end);
        }
    }

    private void italicText(int start, int end) {
        builder.setSpan(new StyleSpan(Typeface.ITALIC),
                start, start + end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
    }

    private void boldText(int start, int end) {
        builder.setSpan(new StyleSpan(Typeface.BOLD),
                start, start + end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
    }

    private void underlineText(int start, int end) {
        builder.setSpan(new UnderlineSpan(),
                start,start + end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
    }

    private int measureValue(int size, int mode) {
        return View.MeasureSpec.makeMeasureSpec(size, mode);
    }

    private void debug(String message) {
        Log.d("TextViewReadMore", message == null ? "null" : message);
    }

    private void setWrapContent() {
        ViewGroup.LayoutParams param = getLayoutParams();
        param.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        setLayoutParams(param);
        debug("wrap");
    }

}
