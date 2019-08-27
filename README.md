# RecyclerViewSnap

RecyclerView snapping example with SnapHelper

<img src="screens/snap_googleplay.gif" width=300></img>   <img src="screens/snap_final.gif" width=300></img>

## Start/Top/End/Bottom Snapping

If you need snapping support to start, top, end or bottom, use GravitySnapHelper.

Add this to your build.gradle:

```groovy
// AndroidX
implementation 'com.github.rubensousa:gravitysnaphelper:2.1.0'

// Old support libraries
implementation 'com.github.rubensousa:gravitysnaphelper-compat:2.0'
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

## Center snapping

```java
new LinearSnapHelper().attachToRecyclerView(recyclerView);
```

## Page snapping

```java
new PagerSnapHelper().attachToRecyclerView(recyclerView);
```

## Nested RecyclerViews

Take a look at these blog posts if you're using nested RecyclerViews

1. [Improving scrolling behavior of nested RecyclerViews](https://rubensousa.com/2019/08/16/nested_recyclerview_part1/)

2. [Saving scroll state of nested RecyclerViews](https://rubensousa.com/2019/08/27/saving_scroll_state_of_nested_recyclerviews/)


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
