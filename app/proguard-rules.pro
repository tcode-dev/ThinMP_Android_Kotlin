# プロジェクト固有のProGuardルールを書くファイル。
# どの設定ファイルを適用するかはbuild.gradleのproguardFilesで指定する。
# 詳細: http://developer.android.com/guide/developing/tools/proguard.html

# WebViewでJSを使う場合は、以下を有効にしてJavaScriptインターフェースのクラス名を指定する。
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# スタックトレースに行番号を残す場合は有効にする。
#-keepattributes SourceFile,LineNumberTable

# 行番号を残しつつ元のソースファイル名を隠す場合は有効にする。
#-renamesourcefileattribute SourceFile