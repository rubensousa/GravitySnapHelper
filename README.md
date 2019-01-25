# RecyclerViewSnap

RecyclerView snapping example with SnapHelper

<img src="screens/snap_googleplay.gif" width=300></img>   <img src="screens/snap_final.gif" width=300></img>

## How to

If you need snapping support to start, top, end or bottom, use GravitySnapHelper.

Add this to your build.gradle:

```groovy
// AndroidX
implementation 'com.github.rubensousa:gravitysnaphelper:2.0'

// Old support libraries
implementation 'com.github.rubensousa:gravitysnaphelper-compat:2.0'
```

Otherwise, center snapping is done with LinearSnapHelper.

### Snapping center:

```java
new LinearSnapHelper().attachToRecyclerView(recyclerView);
```

### Snapping start with GravitySnapHelper:

```java
startRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
                
new GravitySnapHelper(Gravity.START).attachToRecyclerView(startRecyclerView);
```

### Snapping top with GravitySnapHelper:

```java
topRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                
new GravitySnapHelper(Gravity.TOP).attachToRecyclerView(topRecyclerView);
```

### If you're using nested RecyclerViews, take a look at:

https://github.com/rubensousa/RecyclerViewSnap/blob/master/gravitysnaphelper/src/main/java/com/github/rubensousa/gravitysnaphelper/OrientationAwareRecyclerView.java

## License

    Copyright 2018 The Android Open Source Project
    Copyright 2018 Rúben Sousa
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
