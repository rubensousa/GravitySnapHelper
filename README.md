# RecyclerViewSnap

RecyclerView snapping example with SnapHelper

<img src="screens/snap_googleplay.gif" width=300></img>   <img src="screens/snap_final.gif" width=300></img>

## How to

If you need snapping support to start, top, end or bottom, use GravitySnapHelper.

Add this to your build.gradle:

```groovy
compile 'com.github.rubensousa:gravitysnaphelper:1.0'
```

Otherwise, center snapping is done with LinearSnapHelper (part of the recyclerview-v7 package).

### Snapping center:

```java
SnapHelper snapHelper = new LinearSnapHelper();
snapHelper.attachToRecyclerView(recyclerView);
```

### Snapping start with GravitySnapHelper:

```java
startRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
                
SnapHelper snapHelperStart = new GravitySnapHelper(Gravity.START);
snapHelperStart.attachToRecyclerView(startRecyclerView);
```

### Snapping top with GravitySnapHelper:

```java
topRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                
SnapHelper snapHelperTop = new GravitySnapHelper(Gravity.TOP);
snapHelperTop.attachToRecyclerView(topRecyclerView);
```

## License

    Copyright 2016 The Android Open Source Project
    Copyright 2016 Rúben Sousa
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
