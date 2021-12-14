## 試験勉強用タイマー

### 概要

試験勉強用にカスタマイズできるタイマーです。<br>
各設問にどのくらい時間をかけるか計画をたて、試験勉強をすることができます。<br>
設定した残り時間に事前通知をすることも可能です。<br>
各設問に要する体感時間を身に着けて、戦略的に試験に臨むためのアプリです。<br>

### 画面遷移図

![タイマーアプリ画面遷移図横](https://user-images.githubusercontent.com/60771916/139251588-ace1d2d6-86c9-4b07-b268-787d36fb5652.png)

<ol>
    <li>Home/TimerListFragment</li>
    <li>PresetTimerListFragment</li>
    <li>SetTimerFragment</li>
</ol>
    A. TimerFragment

### 機能

<ul>
    <li>タイマー機能</li>
        <ul>
        <li>開始機能</li>
        <li>終了機能</li>
        <li>停止機能</li>
        </ul>
    <li>タイマー設定機能</li>
        <ul>
        <li>タイマー通知の音もしくはバイブ設定機能</li>
	    <li>タイマー稼働中の画面表示設定機能</li>
        <li>タイマー削除機能(ボタン,スワイプ)</li>
        </ul>
    <li>プリセットタイマー作成機能</li>
        <ul>
        <li>時間選択機能</li>
        <li>事前通知設定機能</li>
        <li>プリセットタイマー順番変更機能</li>
        <li>プリセットタイマー削除機能(ボタン,スワイプ)</li>
        </ul>
</ul>

- タイマー作成機能
![insertTimer](https://user-images.githubusercontent.com/60771916/145973592-d13d33b2-bed3-4309-aa1d-7424b4f8c40a.gif)

- タイマー削除機能
![deleteTimer](https://user-images.githubusercontent.com/60771916/145973821-20bc056e-06c4-4709-ba00-87f20f0ff830.gif)

- タイマー機能
![TimerStart](https://user-images.githubusercontent.com/60771916/145973928-1dc26f23-dcad-4e85-a8bc-09f26f9f26ea.gif)

### ライブラリ

- Room
- LiveData
- ViewModel
- Navigation
- JUnit4
- espresso
- mockito
- truth

## 感想

今後はアプリを起動していない時にも、タイマーの終了をお知らせできる機能を追加したいと考えています。<br>
また、今回はimageView等のテストができなかったので、今後勉強していきたいと思います。<br>
初めてのアプリ作りということもあり、途中からissueを活用してアプリ作成する等、勉強と並行していたため、段取りが悪くなってしまいました。<br>
今後は計画的にアプリ作成をしたいと思います。<br>

### 参考
- 『Advanced Android in Kotlin 05.1:Testing Basics』
https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-basics?hl=ja#0
- 『Advanced Android in Kotlin 05.2:Introduction to Test Doubles and Dependency Injection』
https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-test-doubles?hl=ja#0
-『Advanced Android in Kotlin 05.3: Testing Coroutines and Jetpack integrations』
 https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-survey?hl=ja#1
- Testの時のLiveDataの扱い方について
https://medium.com/androiddevelopers/unit-testing-livedata-and-other-common-observability-problems-bb477262eb04
- AnimationTestRuleについて
『Android端末のアニメーションを無効にするJUnitテストルール』
https://android.suzu-sd.com/2020/04/android_no_animation_wo_mukou_ni_suru_testrule/
-『NumberPicker: Espresso Testing』
https://blog.stylingandroid.com/numberpicker-espresso-testing/