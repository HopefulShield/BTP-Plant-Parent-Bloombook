package com.bloombook.common.modifier

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp


/* This file will hold preset modifiers for anyone to use
 * in the app. Not required but would help with readability and
 * consistency. If you want to add a new modifier, feel free ;)
 */



fun Modifier.textField():Modifier {
    return this
        .fillMaxWidth()
        .height(70.dp)
        .padding(16.dp, 8.dp)
        .shadow(elevation = 4.dp, shape = RoundedCornerShape(24))
}
fun Modifier.bigTextField():Modifier {
    return this
        .fillMaxWidth()
        .height(100.dp)
        .padding(16.dp, 8.dp)
        .shadow(elevation = 4.dp, shape = RoundedCornerShape(8))
}
fun Modifier.searchField():Modifier {
    return this
        .fillMaxWidth(.85f)
        .padding(16.dp, 8.dp)
        .height(50.dp)
        .shadow(elevation = 4.dp, shape = RoundedCornerShape(24))
}

fun Modifier.biggerTextField():Modifier {
    return this
        .fillMaxWidth()
        .height(200.dp)
        .padding(16.dp, 8.dp)
        .shadow(elevation = 4.dp, shape = RoundedCornerShape(8))
}