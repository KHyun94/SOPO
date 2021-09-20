package com.delivery.sopo.exceptions

import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.models.ValidateResult
import java.io.PrintStream
import java.io.PrintWriter

class ValidateException() : Exception()
{
    override var message: String = ""
    private lateinit var data: Pair<InfoEnum, Boolean>

    constructor(message: String): this()
    {
        this.message = message
    }

    constructor(message: String, data: Pair<InfoEnum, Boolean>): this()
    {
        this.message = message
        this.data = data
    }

    fun getData():Pair<InfoEnum, Boolean>{
        return data
    }
}
