package com.example.timerapp.others

// StatusをObserveしてエラー表示したい場合など、
// 一度だけイベントを発生させるジェネリクスクラスを作成
open class Event<out T>(private val content: T) {
    var hasHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if(hasHandled){
            null
        } else {
            hasHandled = true
            content
        }
    }
}