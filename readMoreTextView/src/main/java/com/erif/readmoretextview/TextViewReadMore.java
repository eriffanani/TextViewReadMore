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
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
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
    private boolean stale = true;
    private boolean collapsable = true;

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
                collapseText = getCollapseText == null ? DEFAULT_COLLAPSE_TEXT : getCollapseText;
                collapseTextColor = typedArray.getColor(R.styleable.TextViewReadMore_collapseTextColor, Color.BLUE);
                collapseTextStyle = typedArray.getInt(R.styleable.TextViewReadMore_collapseTextStyle, 0);
                collapseTextUnderline = typedArray.getBoolean(R.styleable.TextViewReadMore_collapseTextUnderline, false);
                originalText = getText().toString();
                collapsable = typedArray.getBoolean(R.styleable.TextViewReadMore_collapsable, true);

                originalMaxLines = getMaxLines();

                int getAnimationDuration = typedArray.getInt(R.styleable.TextViewReadMore_android_animationDuration, animationDuration);
                if (getAnimationDuration > 1000) {
                    animationDuration = 1000;
                } else animationDuration = Math.max(getAnimationDuration, 100);
            } finally {
                typedArray.recycle();
            }
        }
        setOnClickListener(v -> {
            if ((snippedText != null && getText().toString().equals(snippedText+expandText)) ||
                    (originalText != null && getText().toString().equals(originalText+" "+collapseText))
            ) {
                if (collapsable) {
                    expand = !expand;
                    animateTextHeight();
                } else {
                    if (!expand) {
                        expand = true;
                        animateTextHeight();
                    }
                }
            }
        });
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        stale = true;
    }

    private boolean isEllipsized() {
        Layout layout = getLayout();
        return layout.getEllipsisCount(getLineCount() - 1) > 0;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        stale = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (expand) {
            if (stale) {
                createCollapseButton();
            }
        } else {
            if (!animating) {
                if (getVisibility() == View.VISIBLE) {
                    if (getLayout() != null) {
                        if (isEllipsized()) {
                            if (stale) {
                                int lastIndex = getLayout().getEllipsisCount(getLineCount() - 1);
                                if (lastIndex < (expandText.length() + 4)) {
                                    lastIndex = getLayout().getLineVisibleEnd(getLineCount() - 1);
                                } else {
                                    String dots = "... ";
                                    lastIndex = getText().length() - (lastIndex + dots.length() + expandText.length());
                                }
                                if (lastIndex > 0) {
                                    measuring();
                                    createSnipped(lastIndex);
                                }
                            }
                        }
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

    private void createSnipped(int lastIndex) {
        String dots = "... ";
        if (lastIndex > 0) {
            String mainText = getText().toString().subSequence(0, lastIndex).toString();
            String lastChar = mainText.substring(mainText.length() - 1);
            String newline = System.getProperty("line.separator");
            if (newline != null) {
                boolean hasNewline = lastChar.contains(newline);
                if(hasNewline)
                    mainText = getText().toString().subSequence(0, lastIndex -1).toString();
            }
            originalText = getText().toString();
            snippedText = mainText + dots;
            createExpandButton();
            stale = false;
        }
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
        stale = false;
    }

    private void animateTextHeight() {
        int fromHeight = expand ? originalHeight : maxHeight;
        int targetHeight = expand ? maxHeight : originalHeight;
        if (expand) {
            setText(originalText);
            setMaxLines(Integer.MAX_VALUE);
        }
        ValueAnimator animation = ValueAnimator.ofInt(fromHeight, targetHeight);
        animation.setDuration(animationDuration);
        animation.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams param = getLayoutParams();
            param.height = (int) valueAnimator.getAnimatedValue();
            setLayoutParams(param);
            requestLayout();
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

    private void setWrapContent() {
        ViewGroup.LayoutParams param = getLayoutParams();
        param.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        setLayoutParams(param);
    }
}
