package com.ximenez.joel.cazarpatos

data class Player(
    var username: String,
    var huntedDucks: Int
) {
    constructor() : this("", 0)
}
