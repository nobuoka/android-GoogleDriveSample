android-GoogleDriveSample
===================================

Android アプリから Google Drive API を使用するサンプルプロジェクト。

## ビルド方法

### プロジェクトのクローン

クローンするのに予め準備しておく必要があるものは次のとおり。
* [Git](http://git-scm.com/)

```
# プロジェクトを git clone する
git clone --recursive git@github.com:nobuoka/android-GoogleDriveSample.git

# 移動
cd android-GoogleDriveSample
```

### Gradle を使用してビルドする

[Android の Gradle Plugin](http://tools.android.com/tech-docs/new-build-system) を使用してビルドする。
ビルドのために予め準備しておく必要があるものは次のとおり。
* [Java SE Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (1.8)
* Android SDK Manager で次のものをインストールしておくこと (過不足があるかもしれない):
  * [SDK with Build Tools](http://developer.android.com/sdk/index.html): Tools の Android SDK Build-tools 25.0.2
  * Extras の Google Play services
  * Extras の Google Repository

本リポジトリには Gradle ラッパーが含まれているので、Gradle をインストールしておく必要はない。

```
# ビルド
ANDROIND_HOME=/path/to/android_sdk ./gradlew --daemon build
```

ビルド時に Android SDK Tools を使用するので、環境変数 `ANDROID_HOME` でそのパスを指定する必要がある。
もしくは local.properties ファイルで指定してもよい。
`gradlew` コマンドの `--daemon` オプションは必須ではないが、付けておくとデーモンが起動するので 2 回目以降の実行がはやくなって良い。

### Android Studio を使用してビルドする

Android Studio にインポートすることもできる。
インポート時に使用する Gradle を選択するが、「Gradle wrapper」 を選べばよい。
