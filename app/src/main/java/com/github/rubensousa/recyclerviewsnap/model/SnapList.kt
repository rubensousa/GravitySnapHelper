package com.github.rubensousa.recyclerviewsnap.model

import com.github.rubensousa.recyclerviewsnap.R

data class SnapList(
    val gravity: Int,
    val title: String,
    val apps: List<App>,
    val layoutId: Int = R.layout.adapter_snap,
    val snapToPadding: Boolean = false,
    val showScrollButton: Boolean = false
)
