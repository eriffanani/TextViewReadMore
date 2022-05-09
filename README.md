# TextView Read More
Make your long textview setup easily and quickly

## Installation
#### repositories
```kotlin
maven { url 'https://jitpack.io' }
```

#### dependencies
```kotlin
implementation 'com.github.eriffanani:TextViewReadMore:0.3.0'
```

## How To Use
* Basic
```xml
<com.erif.readmoretextview.TextViewReadMore
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:maxLines="3"
    android:ellipsize="end"
    android:text="YOUR TEXT HERE"/>
```
![basic](https://user-images.githubusercontent.com/26743731/167334745-3915b937-a0b4-4524-a0b4-47b165143ec7.png)

#### Styling
* Expand
```xml
<com.erif.readmoretextview.TextViewReadMore
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:maxLines="3"
    android:ellipsize="end"
    app:expandText="Open Text"
    app:expandTextColor="@color/teal_200"
    app:expandTextStyle="bold|italic"
    app:expandTextUnderline="true"
    android:text="YOUR TEXT HERE"/>
```
* Collapse
```xml
<com.erif.readmoretextview.TextViewReadMore
    app:collapseText="Close"
    app:collapseTextColor="@color/teal_200"
    app:collapseTextStyle="bold|italic"
    app:collapseTextUnderline="true"/>
```
![styling](https://user-images.githubusercontent.com/26743731/167335646-86eb9860-b40e-4281-be49-644993cd49e1.png)

## Result
<img src="https://user-images.githubusercontent.com/26743731/167337556-b46de2b5-9115-4d4e-ba48-7d48adbd018d.gif" width="400"/> <img src="https://user-images.githubusercontent.com/26743731/167338135-9d819401-aa26-4a20-ab83-9e9cc6b3886f.gif" width="400"/>

* Animation Duration
```xml
<com.erif.readmoretextview.TextViewReadMore
    android:animationDuration="1000"/>
```

### Callback
* Java
```Java
TextViewReadMore txtReadMore = findViewById(R.id.txtReadMore);
txtReadMore.actionListener(new TextViewReadMoreCallback() {
    @Override
    public void onExpand() {
      // TODO ACTION
    }
    @Override
    public void onCollapse() {
      // TODO ACTION
    }
});
```
* Kotlin
```kotlin
txtReadMore.actionListener(object : TextViewReadMoreCallback {
    override fun onExpand() {
      // TODO ACTION
    }
    override fun onCollapse() {
      // TODO ACTION
    }
})
```
#### Information
This library is still being developed further, please provide feedback if you find a bug. Thank you
