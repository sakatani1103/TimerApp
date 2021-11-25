package com.example.timerapp.others

// データ取り扱いが成功したらSuccessでdataを返す、
// 失敗したらErrorでメッセージを返す
data class Resource<out T>(var status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }
    }
}

enum class Status{
    SUCCESS,
    ERROR
}