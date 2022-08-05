# TextView Read More
Make your long textview setup easily and quickly

## Installation
#### repositories
```kotlin
maven { url 'https://jitpack.io' }
```

#### dependencies
```kotlin
implementation 'com.github.eriffanani:TextViewReadMore:1.5.2'
```

## How To Use
* Basic
```xml
<com.erif.readmoretextview.TextViewReadMore
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:readMoreMaxLines="3"
    android:text="YOUR TEXT HERE"/>
```
![basic](https://user-images.githubusercontent.com/26743731/167334745-3915b937-a0b4-4524-a0b4-47b165143ec7.png)

#### Styling
* Expand
```xml
<com.erif.readmoretextview.TextViewReadMore
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:readMoreMaxLines="3"
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
    app:collapseTextUnderline="true"
    app:collapsed="false"/>
```
![styling](https://user-images.githubusercontent.com/26743731/167335646-86eb9860-b40e-4281-be49-644993cd49e1.png)

## Result
<img src="https://user-images.githubusercontent.com/26743731/167337556-b46de2b5-9115-4d4e-ba48-7d48adbd018d.gif" width="400"/> <img src="https://user-images.githubusercontent.com/26743731/167338135-9d819401-aa26-4a20-ab83-9e9cc6b3886f.gif" width="400"/>

* Animation Duration
```xml
<com.erif.readmoretextview.TextViewReadMore
    android:animationDuration="1000"/>
```

### Action
```xml
<com.erif.readmoretextview.TextViewReadMore
    app:actionClickColor="@color/colorRed"/>
```
```java
txtReadMore.onClickExpand(v -> txtReadMore.toggle());
txtReadMore.onClickCollapse(v -> txtReadMore.toggle());
```

### Callback
* Java
```Java
txtReadMore.toggleListener(collapsed -> {
    // TODO ACTION
});
```
* Kotlin
```kotlin
txtReadMore.toggleListener {
    // TODO ACTION
}
```
#### With recyclerview
* Use collapse function to onBindViewHolder
```java
holder.text.collapsed(item.isCollapsed());
```
* Use Toggle
```java
holder.text.onClickExpand(v -> holder.text.toggle());
holder.text.onClickCollapse(v -> holder.text.toggle());
holder.text.toggleListener(collapsed -> {
    item.setCollapsed(collapsed);
    notifyItemChanged(position);
});
```
* Use Collapse Properties
```java
// onClickExpand or onClickCollapse 
holder.text.onClickExpand(v -> {
    boolean status = !item.isCollapsed();
    holder.text.collapsed(status);
    item.setCollapsed(status);
    update(position);
});
```

#### Information
This library is still being developed further, please provide feedback if you find a bug. Thank you
### Licence
```license
Copyright 2022 Mukhammad Erif Fanani

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
