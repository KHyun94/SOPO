package com.delivery.sopo.exceptions

import com.delivery.sopo.models.ValidateResult
import java.io.PrintStream
import java.io.PrintWriter

class ValidateException(val e: ValidateResult<String>) : Exception()
{
    fun getErrorObject(): ValidateResult<String>
    {
        return e
    }

    override fun setStackTrace(stackTrace: Array<StackTraceElement>)
    {
        super.setStackTrace(stackTrace)
    }

    override fun printStackTrace()
    {
        super.printStackTrace()
    }

    override fun printStackTrace(s: PrintStream)
    {
        super.printStackTrace(s)
    }

    override fun printStackTrace(s: PrintWriter)
    {
        super.printStackTrace(s)
    }

    override fun getStackTrace(): Array<StackTraceElement>
    {
        return super.getStackTrace()
    }

    override fun initCause(cause: Throwable?): Throwable
    {
        return super.initCause(cause)
    }

    override fun fillInStackTrace(): Throwable
    {
        return super.fillInStackTrace()
    }

    override fun getLocalizedMessage(): String?
    {
        return super.getLocalizedMessage()
    }

    override val cause: Throwable?
        get() = super.cause
    override val message: String?
        get() = super.message
}
