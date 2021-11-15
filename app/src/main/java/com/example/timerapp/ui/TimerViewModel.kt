package com.example.timerapp.ui

import androidx.lifecycle.*
import com.example.timerapp.database.*
import com.example.timerapp.others.Constants
import com.example.timerapp.others.Event
import com.example.timerapp.others.Resource
import com.example.timerapp.repository.TimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val repository: TimerRepository
) : ViewModel() {

    // TimerListFragmentでのタイマー表示、追加に必要
    val timerItems = repository.observeAllTimer()

    // 選択したタイマー関する情報
    private val _currentTimer = MutableLiveData<Timer>()
    val currentTimer: LiveData<Timer> = _currentTimer

    private val currentTimerWithPresetTimerList = MutableLiveData<TimerWithPresetTimer>()
    private val _presetTimerList = MutableLiveData<List<PresetTimer>>()
    val presetTimerList: LiveData<List<PresetTimer>> = _presetTimerList

    private val _currentPresetTimer = MutableLiveData<PresetTimer>()
    val currentPresetTimer: LiveData<PresetTimer> = _currentPresetTimer

    private val _currentNumberOfPresetTimers = MutableLiveData<Int>()
    val currentNumberOfPresetTimers: LiveData<Int> = _currentNumberOfPresetTimers

    private val _timerNamesList = MutableLiveData<List<String>>()
    private val timerNamesList: LiveData<List<String>> = _timerNamesList

    private val _numberOfTimers = MutableLiveData<Int>()
    val numberOfTimers: LiveData<Int> = _numberOfTimers

    // Timer関連の処理のERR等を格納
    private val _timerItemStatus = MutableLiveData<Resource<Timer>>()
    val timerItemStatus: LiveData<Resource<Timer>> = _timerItemStatus

    // PresetTimer関連の処理のERR等を格納
    private val _presetTimerItemStatus = MutableLiveData<Resource<List<PresetTimer>>>()
    val presetTimerItemStatus: LiveData<Resource<List<PresetTimer>>> = _presetTimerItemStatus

    // 名前の入力関連のERR等の処理を格納
    private val _timerNameStatus = MutableLiveData<Event<Resource<String>>>()
    val timerNameStatus: LiveData<Event<Resource<String>>> = _timerNameStatus

    // Navigation
    // TimerListFragmentからPresetTimerListFragmentへの遷移
    private val _navigateToPresetTimer = MutableLiveData<String?>()
    val navigateToPresetTimer: LiveData<String?> = _navigateToPresetTimer

    // TimerListFragmentからTimerFragmentへの遷移
    private val _navigateToTimer = MutableLiveData<String?>()
    val navigateToTimer: LiveData<String?> = _navigateToTimer

    // PresetTimerFragmentからSettingTimerFragmentへの遷移
    private val _navigateToSettingTimer = MutableLiveData<MutableMap<String, String>?>()
    val navigateToSettingTimer: LiveData<MutableMap<String, String>?> = _navigateToSettingTimer

    // DB
    fun insertTimerIntoDb(timer: Timer) {
        viewModelScope.launch {
            repository.insertTimer(timer)
        }
    }

    private fun deleteTimerFromDb(timer: Timer) {
        viewModelScope.launch {
            repository.deleteTimer(timer)
        }
    }

    // PresetTimerがない場合にSnackbarを表示
    // PresetTimerが1以下になった場合にTimerをSimpleLayoutに変更する処理も記述する
    private fun deletePresetTimer(presetTimer: PresetTimer) {
        viewModelScope.launch {
            repository.deletePresetTimer(presetTimer)
        }
    }

    // Timer名が変更時
    // PresetTimerのTimer名も変更する
    private fun updatePresetTimerFromName(presetTimer: PresetTimer, name: String) {
        // nameだけ変更
        val newPresetTimer = PresetTimer(
            name, presetTimer.presetName, presetTimer.presetTime,
            presetTimer.notificationTime
        )
        viewModelScope.launch {
            repository.updatePresetTimer(newPresetTimer)
        }
    }

    fun insertPresetTimerList(presetTimerList: List<PresetTimer>) {
        viewModelScope.launch {
            presetTimerList.forEach {
                repository.insertPresetTimer(it)
            }
        }
    }

    // 追加の際はデフォルトで、プリセットタイマー1のようにプリセットタイマー数+1の名前で保存
    // 最初は追加せずにやった方がいいかも？？
    fun addPresetTimer() {
        val presetNum = currentNumberOfPresetTimers.value.toString()
        val newPresetTimerName = "presetTimer$presetNum"
        val presetTimer = PresetTimer(
            "currentTimer.name", newPresetTimerName,
            0, 0
        )
        viewModelScope.launch {
            repository.insertPresetTimer(presetTimer)
        }
    }

    // Timer名(主キー)以外の変更
    private fun updateTimerIntoDb(timer: Timer) {
        viewModelScope.launch {
            repository.updateTimer(timer)
        }
    }

    // related to get Data
    fun getCurrentTimer(name: String) {
        viewModelScope.launch {
            val timer = repository.getCurrentTimer(name)
            _currentTimer.postValue(timer)
        }
    }

    fun getCurrentPresetTimer(timerName: String, presetName: String) {
        viewModelScope.launch {
            _currentPresetTimer.postValue(repository.getCurrentPresetTimer(timerName, presetName))
        }
    }

    private fun getTimerNamesList() {
        viewModelScope.launch {
            _timerNamesList.postValue(repository.getTimerNames())
        }
    }

    fun getNumberOfTimers() {
        viewModelScope.launch {
            _numberOfTimers.postValue(repository.getNumberOfTimers())
        }
    }

    fun getCurrentTimerWithPresetTimerList(name: String) {
        viewModelScope.launch {
            currentTimerWithPresetTimerList.value = repository.getPresetTimerWithTimer(name)
        }
        val errorList = mutableListOf<PresetTimer>()
        _presetTimerList.value = currentTimerWithPresetTimerList.value?.presetTimer ?: errorList
    }

    // timerNameからPresetTimer数を取得
    fun getNumberOfPresetTimers(name: String) {
        viewModelScope.launch {
            _currentNumberOfPresetTimers.postValue(repository.getNumberOfPresetTimers(name))
        }
    }

    // FakeRepositoryを使用したUnit Testを行う
    // related to Insert Timer
    // 以下はエラー表示(このfunctionはnameのみの入力)
    // 空の入力
    // 15文字以内
    // 既に同じタイマーの名前が登録されている
    // タイマー数が15個登録されている
    fun insertTimer(name: String) {
        if (name.isEmpty()) {
            _timerNameStatus.postValue(
                Event(
                    Resource.error(
                        "タイマー名が入力されていません。", name
                    )
                )
            )
            return
        }

        if (name.length > Constants.MAX_NAME_LENGTH) {
            _timerNameStatus.postValue(
                Event(
                    Resource.error(
                        "タイマー名は${Constants.MAX_NAME_LENGTH}文字までです。", name
                    )
                )
            )
            return
        }

        getTimerNamesList()
        val currentTimerNamesList = timerNamesList.value ?: mutableListOf()
        if (currentTimerNamesList.contains(name)) {
            _timerNameStatus.postValue(
                Event(
                    Resource.error(
                        "入力したタイマー名は使用されています。", name
                    )
                )
            )
            return
        }

        getNumberOfTimers()
        val currentNumberOfTimers = numberOfTimers.value ?: 0
        if (currentNumberOfTimers >= Constants.TIMER_NUM) {
            _timerNameStatus.postValue(
                Event(
                    Resource.error(
                        "登録できるタイマーは${Constants.TIMER_NUM}までです。", name
                    )
                )
            )
            return
        }

        val timer = Timer(name)
        insertTimerIntoDb(timer)
        _timerNameStatus.postValue(Event(Resource.success(name)))
    }

    // Timer名変更の処理
    // タイマー名の変更 下記の場合にERR
    // 空の入力
    // 15文字以内
    // 既に同じタイマーの名前が登録されている(変更なしの場合はスルー)
    fun updateTimerName(name: String) {
        if (name.isEmpty()) {
            _timerItemStatus.postValue(
                Resource.error(
                    "タイマー名が入力されていません。", null
                )
            )
            return
        }

        if (name.length > Constants.MAX_NAME_LENGTH) {
            _timerItemStatus.postValue(
                Resource.error(
                    "タイマー名は${Constants.MAX_NAME_LENGTH}までです。", null
                )
            )
            return
        }

        getTimerNamesList()
        val currentTimerNameList = timerNamesList.value ?: mutableListOf()
        if (currentTimer.value != null) {
            if (currentTimerNameList.contains(name) && currentTimer.value?.name != name) {
                _timerItemStatus.postValue(
                    Resource.error(
                        "入力したタイマー名は使用されています。", null
                    )
                )
                return
            }
        }
        updateTimerNameIntoDb(name)
    }

    // タイマー名を変更する場合は、主キーを変更することになるので、
    // タイマーを追加、プリセットタイマー名を変更して変更前のタイマーを削除する
    private fun updateTimerNameIntoDb(name: String) {
        // 新しいタイマーを追加
        val timer = currentTimer.value!! //現在指定してるタイマー
        val newTimer = Timer(
            name, timer.total, timer.listType, timer.notificationType,
            timer.isDisplay, timer.detail
        )
        insertTimerIntoDb(newTimer)
        // プリセットタイマーを変更(登録されていれば)
        val presetTimerList = presetTimerList.value
        if (presetTimerList != null && presetTimerList.count() > 0) {
            updatePresetTimerList(presetTimerList, name)
        }
        //　現在のタイマーを削除して、新しいタイマーをcurrentTimerに登録
        deleteTimerFromDb(timer)
        getCurrentTimer(name)

        _timerItemStatus.postValue(Resource.success(timer))
    }

    private fun updatePresetTimerList(presetTimerList: List<PresetTimer>, name: String) {
        presetTimerList.forEach { updatePresetTimerFromName(it, name) }
    }

    // FakeRepositoryを使用したUnitTestを記述予定
    // タイマー削除処理
    // 関連するPresetTimerも削除することに注意
    // タイマーが存在しないときにはERR(delete buttonにもERR出す)
    fun deleteTimer(name: String) {
        getCurrentTimer(name)
        getCurrentTimerWithPresetTimerList(name)

        val timer = currentTimer.value
        if (timer == null) {
            _timerItemStatus.postValue(
                Resource.error(
                    "タイマーが登録されていません。", null
                )
            )
        } else {
            deleteTimerFromDb(timer)
        }

        val presetTimerList = presetTimerList.value
        if (presetTimerList != null && presetTimerList.count() > 0) {
            presetTimerList.forEach {
                deletePresetTimer(it)
            }
        }

        _presetTimerItemStatus.postValue(Resource.success(presetTimerList))

    }

    // related setting
    fun switchTimerDisplay(notificationType: NotificationType) {
        val timer = currentTimer.value!!
        val newTimer = Timer(
            timer.name, timer.total, timer.listType, notificationType,
            timer.isDisplay, timer.detail
        )
        updateTimerIntoDb(newTimer)
    }

    fun settingSound(isDisplay: Boolean) {
        val timer = currentTimer.value!!
        val newTimer = Timer(
            timer.name, timer.total, timer.listType, timer.notificationType, isDisplay, timer.detail
        )
        updateTimerIntoDb(newTimer)
    }

    // related to Navigation
    // TimerListFragmentからPresetTimerListFragmentへの遷移
    fun navigateToPresetTimer(name: String) {
        _navigateToPresetTimer.value = name
    }

    // 遷移終了後にnullにする
    fun doneNavigateToPresetTimer() {
        _navigateToPresetTimer.value = null
    }

    fun navigateToSettingTimer(timerName: String, presetName: String) {
        val nameMap = mutableMapOf("timerName" to timerName, "presetName" to presetName)
        _navigateToSettingTimer.value = nameMap
    }

    fun doneNavigateToSettingTimer() {
        _navigateToSettingTimer.value = null
    }

    fun navigateToTimer(name: String) {
        _navigateToTimer.value = name
    }

    fun doneNavigateToTimer() {
        _navigateToTimer.value = null
    }

}