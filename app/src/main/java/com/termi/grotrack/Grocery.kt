package com.termi.grotrack

import java.io.Serializable

class Grocery(
    val name: String,
    val count: Long,
    val location: String = Consts.LOCATION_UNKNOWN
): Serializable