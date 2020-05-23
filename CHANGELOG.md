# Changelog

## 2.2.1

- Fixed `GravitySnapHelper` not dispatching snap events for edge items sometimes on `SnapListener`

## 2.2.0

### New features
- Added Gravity.CENTER support ([#21](https://github.com/rubensousa/GravitySnapHelper/issue/21))
- Added setMaxFlingDistance and setMaxFlingSizeFraction to change the max fling distance allowed ([#29](https://github.com/rubensousa/GravitySnapHelper/issue/29))
- Added setSnapToPadding to allow snapping to the padding set in the RecyclerView. Defaults to false ([#58](https://github.com/rubensousa/GravitySnapHelper/issue/58))
- Added GravitySnapRecyclerView that uses GravitySnapHelper by default
- Added setGravity to change the gravity of GravitySnapHelper
- Added setScrollMsPerInch to change the scroll speed
- Added setSnapListener to allow changing the SnapListener that was set or clearing it
- GravityPagerSnapHelper is now deprecated. Use GravitySnapHelper together with setMaxFlingSizeFraction() to achieve the same behavior
- Added updateSnap to update the snap position if for some reason snapping was stopped

### Improvements

- Improved behavior when smoothScrollToPosition is called. Now the smooth scroller is used
- Added getters to all relevant properties
- getCurrentSnappedPosition now searches for the correct snap view instead of returning the last position that we snapped to
- Added missing NonNull and Nullable annotations

## 2.1.0

- OrientationAwareRecyclerView now doesn't depend on LinearLayoutManager directly
- Fixed SnapListener not being called for the first position sometimes (#57)
- Added getCurrentSnappedPosition to GravitySnapHelper and GravityPagerSnapHelper

## 2.0

### New features:
- Migrated to AndroidX.
- Added smoothScrollToPosition and scrollToPosition methods that'll snap to a certain position.
- Added OrientationAwareRecyclerView that only handles scroll events according to its orientation.

### Bug fixes:
- Fixed snapping not working correctly for RecyclerViews with padding (#49)
- Fixed snapping not considering GridLayoutManager.SpanSizeLookup (#52)

## 1.5

- Fixed reverse layout causing scrolling issues (#40)

## 1.4

- Added Nullable and NonNull annotations
- Updated support library

## 1.3

- Fixed IllegalStateException being thrown when SnapHelper is attached twice to a RecyclerView (#23)
- Bumped min sdk to 14

## 1.2

- Added support for GridLayoutManager

## 1.1

- Added GravityPagerSnapHelper
- Updated support library

## 1.0

- GravitySnapHelper extends from LinearSnapHelper again
- Added SnapListener to listen for snap events


## 0.3

- Extend from SnapHelper until https://code.google.com/p/android/issues/detail?id=223649 gets fixed.

## 0.2

- Added RTL support
- Fixed GravitySnapHelper scrolling too far sometimes (#2)
- Added support to snap last items via enableLastItemSnap(boolean enable)


## 0.1

- Initial release
