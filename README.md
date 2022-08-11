# TextView Read More
Make your long textview setup easily and quickly

## Installation
#### repositories
```kotlin
maven { url 'https://jitpack.io' }
```

#### dependencies
```kotlin
implementation 'com.github.eriffanani:TextViewReadMore:2.2.0'
```

## How To Use
* Basic
```xml
<com.erif.readmoretextview.TextViewReadMore
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:readMoreMaxLines="3" (Default 1)
    android:text="YOUR TEXT HERE"/>
```
![basic](https://user-images.githubusercontent.com/26743731/167334745-3915b937-a0b4-4524-a0b4-47b165143ec7.png)

#### Styling
* Expand
```xml
<com.erif.readmoretextview.TextViewReadMore
    app:readMoreMaxLines="3" (Default 1)
    app:expandText="Open Text" (Default "Read More")
    app:expandTextColor="@color/teal_200" (Default Color.BLUE)
    app:expandTextStyle="bold|italic" (Default normal)
    app:expandTextUnderline="true" (Default false)
    android:text="YOUR TEXT HERE"/>
```
![styling](https://user-images.githubusercontent.com/26743731/167335646-86eb9860-b40e-4281-be49-644993cd49e1.png)

* Collapse
```xml
<com.erif.readmoretextview.TextViewReadMore
    app:collapseText="Close Text" (Default "Close")
    app:collapseTextColor="@color/teal_200" (Default Color.BLUE)
    app:collapseTextStyle="bold|italic" (Default normal)
    app:collapseTextUnderline="true" (Default false)
    app:collapsed="false" (Default true)/>
```

## Result
<img src="https://user-images.githubusercontent.com/26743731/167337556-b46de2b5-9115-4d4e-ba48-7d48adbd018d.gif" width="400"/> <img src="https://user-images.githubusercontent.com/26743731/167338135-9d819401-aa26-4a20-ab83-9e9cc6b3886f.gif" width="400"/>

### Animation Duration
```xml
<com.erif.readmoretextview.TextViewReadMore
    android:animationDuration="500" (Default 200)/>
```

### Ellipsis Type
```xml
<com.erif.readmoretextview.TextViewReadMore
    app:ellipsisType="none" (Default dots)/>
```
### Action
```xml
<com.erif.readmoretextview.TextViewReadMore
    app:actionClickColor="@color/colorRed" (Default @color/text_view_read_more_button_hover_color)/>
```
```java
TextViewReadMore txtReadMore = findViewById(R.id.txtReadMore);
txtReadMore.onClickExpand(v -> txtReadMore.toggle());
txtReadMore.onClickCollapse(v -> txtReadMore.toggle());
```

### Listener
* Java
```Java
txtReadMore.toggleListener(collapsed -> { // TODO ACTION });
```
* Kotlin
```kotlin
txtReadMore.toggleListener { collapsed -> // TODO ACTION }
```
### With recyclerview
* Use collapse function to onBindViewHolder
```java
/** 
    item.isCollapsed() is variable from your item to store and save state of 
    textview (collapsed/expanded) when item displayed again from RecyclerView scroll 
*/
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
    notifyItemChanged(position);
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
