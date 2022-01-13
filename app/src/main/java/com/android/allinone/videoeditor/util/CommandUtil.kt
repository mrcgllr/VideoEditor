package com.android.allinone.videoeditor.util

object CommandUtil {
    @JvmStatic
    fun main(args: Array<String>): String {
        StringBuffer().apply {
            for (i in args.indices) {
                if (i == args.size - 1) {
                    append("\"")
                    append(args[i])
                    append("\"")
                } else {
                    append("\"")
                    append(args[i])
                    append("\" ")
                }
            }
            println(toString())
            return toString()
        }
    }
}
