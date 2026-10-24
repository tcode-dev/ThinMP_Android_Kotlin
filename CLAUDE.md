# CLAUDE.md

このリポジトリで作業するClaude Code(claude.ai/code)向けのガイド。

## プロジェクトの概要

ThinMPはKotlinで書いた軽量なAndroid用の音楽プレーヤー。
端末内の音声を再生し、お気に入り、プレイリスト、ショートカット、バックグラウンド再生に対応する。

## ビルドと実行

```bash
./gradlew assembleDebug        # デバッグ用APKをビルドする
./gradlew test                 # ユニットテストを実行する
./gradlew connectedAndroidTest # インストルメンテーションテストを実行する(Roomのリポジトリのテストはここ)
```

- Compile/Target SDKは36、Min SDKは33(Android 13以上)
- Javaのターゲットは JDK 17
- HiltとRoomはKAPTで処理する

### インストルメンテーションテストの実行

`JAVA_HOME`は`.claude/settings.json`の`env`で設定しているので、`./gradlew`に前置きは要らない。
Android SDKは`PATH`に入っていないので、エミュレーターとadbはフルパスで呼ぶ。

```bash
~/Library/Android/sdk/emulator/emulator -avd Medium_Phone_API_36.1 -no-boot-anim
```

- `tools/push-test-audio.sh`で端末に音声を入れる。
  入れないと、MediaStoreを使うテストは自分をスキップする。
  `tools/push-compilation-audio.sh`は、アルバムを持たないアーティストがいる状態を手で作るためのもの
- 起動したばかりのエミュレーターで`Starting 0 tests`→`Process crashed`になることがある。
  これはエミュレーターの不調ではなく、APKが入っていないだけ(`adb logcat`に`Unable to find instrumentation info`が出る)。
  `app-debug.apk`と`app-debug-androidTest.apk`の両方を`adb install -r -t`で入れてから再実行する
- 終わったらエミュレーターを止める(`adb emu kill`)。
  残っていると、同じAVDを次に起動できない
- 音声の出力先が変わることに依存するものは、エミュレーターでは試せない。
  `ACTION_HEADSET_PLUG`と`ACTION_AUDIO_BECOMING_NOISY`は保護されたブロードキャストで、
  ヘッドセットのハードウェアがないので`HEADSET_PLUG`はstickyの一覧にも入らない。
  偽装しようとせず、実機での確認を頼む

## アーキテクチャ

MVVMを使った層構造。

```
View (Jetpack Compose) → ViewModel (StateFlow) → Service → Repository → Model
View → ViewModel → Register → Repository                  (お気に入りやプレイリストなどのドメイン操作)
View → ViewModel → PlaybackController → MusicService      (再生。MediaSession経由)
```

- **View**:画面は`view/screen/`、部品は`view/cell/`、`view/row/`、`view/layout/`、`view/button/`など
- **ViewModel**:画面ごとに1つ。状態は`StateFlow`で持つ
- **Service**:ビジネスロジックの層(`MainService`、`ArtistDetailService`など)
- **Register**:お気に入りやプレイリストを操作するドメインのインターフェース。ViewModelに実装させて使う
- **Repository**:リポジトリはすべて`repository/`にある(MediaStoreのクエリとRoomのアクセス)。DAOは`repository/dao/`
- **Model**:データモデルは`model/media/`、型付きのIDは`model/media/valueObject/`(`SongId`、`AlbumId`、`ArtistId`など)、Roomのエンティティは`model/room/`
- **再生**:`PlaybackController`はアプリで唯一のMediaController。
  `MusicService`は`MediaSessionService`で、ExoPlayerとMediaSessionを1つずつ持つ。
  画面も通知もロック画面もヘッドセットも、すべてMediaSession経由で操作する

## 主な技術

- **UI**:Jetpack Compose + Material 3。画像はCoil、権限はAccompanist
- **再生**:Media3 1.4.0(ExoPlayer、MediaSession)。通知、ロック画面、フォアグラウンド化は`MediaSessionService`に任せる
- **DI**:Hilt
- **データベース**:Room(お気に入り、プレイリスト、ショートカット)。
  `AppDatabase`はシングルトンで、`MainApplication.appContext`から作る。
  スキーマは`app/schemas/`に書き出す。
  アプリは未リリースなので、スキーマの変更はバージョン1のまま直接変更し、書き出したスキーマを作り直す。`Migration`は書かない。
  リリース後はこれが安全でなくなる。`fallbackToDestructiveMigration()`がないので、スキーマを変えるたびにバージョンを上げ、`AppDatabase`に`Migration`を追加する必要がある
- **設定**:DataStore Preferences(リピート、シャッフル、メニューの表示)
- **非同期**:Kotlin Coroutines。RoomとMediaStoreのI/Oはすべてメインスレッドの外で行う

### ライブラリのAPIを調べる

サードパーティのAPIは、まずこのリポジトリの既存の呼び出し箇所を読む。それが実際に使っているバージョンになる。
既存の呼び出しにないAPIを使うときは、このプロジェクトが指定しているバージョンで存在を確かめる。
`~/.gradle/caches/modules-2/files-2.1/`に`-sources.jar`があればそれを読み、なければContext7(`.mcp.json`)で調べる。
最新版の知識で書かないこと。指定しているバージョン(Media3 1.4.0など)は最新より古い。

## 主なディレクトリ

```
app/src/main/java/dev/tcode/thinmp/
├── activity/          # MainActivity(Activityはこれ1つ)
├── application/       # MainApplication(Hiltのエントリーポイント。appContextを持つ)
├── config/            # ConfigStore(DataStoreの設定)
├── constant/          # ナビゲーションのルート、スタイル、通知などの定数
├── model/             # データモデル、値オブジェクト、Roomのエンティティ
├── player/            # PlaybackController(画面が使うMediaController)、MusicService(MediaSessionService)
├── register/          # ドメインのインターフェース(お気に入り、プレイリスト、ショートカット)
├── repository/        # MediaStoreとRoomへのアクセス。RoomのDAOはdao/
├── service/           # ビジネスロジック
├── view/              # ComposeのUIすべて(画面、セル、レイアウト、ナビゲーションなど)
├── viewModel/         # 画面ごとのViewModel
└── ui/theme/          # Composeのテーマ
```

## 規約

- Activityは1つで、画面遷移はComposeのナビゲーションで行う
- XMLのレイアウトは使わず、すべてJetpack Composeで書く
- エンティティのIDは型付きの値オブジェクトで扱い、生の文字列やLongは使わない
- 画面ごとに専用のViewModelを持つ
- ユーザーが作るデータはRoom、端末の音声はMediaStoreに置く
- サロゲートの`id`は、それを読むものがある場合にだけ持たせる。
  お気に入りのテーブルはMediaStoreのidそのものを主キーにする(`favorite_songs(song_id)`、`favorite_artists(artist_id)`)。
  `playlist_songs`は(`playlist_id`, `song_id`)の組を主キーにする。
  これで重複行(`toggle`が防ごうとしていたものや、同じ曲を同じプレイリストに2回登録したもの)が禁止され、
  クエリが絞り込む列のインデックスも同時に付く。
  `shortcuts`は、`ShortcutService`が`id`を`ShortcutModel`に渡すので`id`を残している
- DAOのクエリが絞り込む主キー以外の列には`@Index`を付け、それ以上は付けない。
  複合インデックスは、すべてのクエリがその組で絞り込む場合だけにする(`shortcuts(item_id, type)`)。
  主キーのインデックスの先頭がその列なら、インデックスは付けない(`playlist_songs(playlist_id, song_id)`は`playlist_id`だけを指定するクエリにも使える)
- プレイリストの曲順は行を挿入した順。
  そのため`findByPlaylistId`は、SQLiteが選ぶインデックスに任せず`ORDER BY rowid`を指定する。
  順序を保つ必要がある書き込み(`updatePlaylist`)は、そのプレイリストの行を削除し、新しい順で挿入し直す
- 列名は`snake_case`にする。Kotlinのプロパティがcamelの場合は`@ColumnInfo(name = ...)`で指定する。
  指定しないとRoomはプロパティ名をそのまま列名にする。
  SQLiteの識別子は大文字と小文字を区別しないので、camelCaseにしても何も区別できない
- Roomのリポジトリは既定で`MainApplication.appContext`からデータベースを使うが、DAOか`AppDatabase`をコンストラクターで受け取る。
  テストではインメモリのデータベースを渡せる
- Registerのインターフェースは、メソッドごとにその場でリポジトリを作る
- ProGuard/R8の難読化・最適化は有効にしていない
- 一覧はすべて行のidをkeyにする(`items(key = …)`)ので、行のcompositionがほかの行に使い回されることはない。
  画面の`Modifier.pointerInput(Unit)`や`DropdownMenuView`の`remember { }`にkeyがないのはそのためだ。
  行のidはその行のcompositionの中で変わらないので、keyにしても`Unit`と同じになる。
  **`pointerInput(Unit)`を`pointerInput(id)`に変えないこと。**
  `853a6e9`まではitemのkeyがないことへの回避策としてそうしていたが、keyを付けたことで不要になり、外した。
  これが正しく動くのは、タップのクロージャーが行の識別子(`songId`、`album.id`、`shortcut.url`)だけを読み、
  再読み込みで変わりうる表示用のフィールドを読まないからだ。
  変化を追う必要がある値は、`pointerInput`のkeyではなく`rememberUpdatedState`で読む
- `catch`は、特定の例外に特定の回復方法があるところにだけ書く。今はそういう場所はない。
  削除されたファイルは、例外ではなく`MusicService`の`onPlayerError()`に`ERROR_CODE_IO_FILE_NOT_FOUND`として届く。
  ViewModelの読み込みにもあえて`catch`を書いていない。
  権限はどの画面よりも先に`PermissionView`で確かめ、`ContentResolver.query()`はプロバイダーの失敗を`null`で返し、`?.use`がそれを空の一覧にする。
  残るのは不正なselectionやソート順、Roomの`SQLiteException`で、これはプログラムの誤りか回復できないものだ。
  まとめて`catch`すると、クラッシュではなく空の画面として現れてしまう

### コメント

- コメントは日本語で、である調で書く。識別子の前後にスペースは入れない
- コードを読み直すだけのコメントは書かない。なぜそうしているかを短く書く
- 過去の実装や経緯は書かない。それはコミットメッセージとPRに書く
- KDocは1文だけでも、`/**`と`*/`をそれぞれ単独の行に置く
- 宣言の説明はKDoc、関数の中の補足は`//`で書く
- 複数行のコメントは「。」ごとに改行し、1行1文にする。
  1文が長いときだけ、括弧の外の「、」で折り返す

### スレッド

- **`repository/dao/`の関数はすべて`suspend`にする。**
  これでRoomの`assertNotMainThread()`に届かなくなり、`AppDatabase`は`.allowMainThreadQueries()`なしで済む。
  `suspend`でないDAO関数を足すとメインスレッドでのI/Oが戻り、`MainThreadAccessTest`が失敗する
- **`suspend`のRoomの呼び出しを`withContext(Dispatchers.IO)`で囲まないこと。**
  Roomは自分のクエリ用のExecutorに切り替える。
  `withTransaction { }`の中でディスパッチャーを切り替えると、トランザクションのスレッドから外れて壊れる
- **`ConfigStore`のアクセサーもすべて`suspend`で、同じ理由で`withContext(Dispatchers.IO)`で囲まない。**
  DataStoreは自分のディスパッチャーでファイルにアクセスする
- `withContext(Dispatchers.IO)`を使うのは`MediaStoreRepository.get()`/`getList()`(ブロックする`ContentResolver.query()`)の1か所だけ。
  通知のアルバムアートは、Media3のビットマップローダーが`MediaMetadata.artworkUri`から読み込む
- トランザクション:1つのDAOで書けるものはDAOのメソッドに`@Transaction`を付ける。
  複数のDAOにまたがるものや、Kotlinの処理を挟むものは、リポジトリで`db.withTransaction { }`を使う
- **行を読んでから書くことを、別々の呼び出しで行わないこと。**
  `suspend`のDAO呼び出しはどれも中断点なので、`if (exists(id)) delete(id) else insert(id)`だと、
  同時に呼んだ2つがどちらも「ない」と判断して2回挿入する。
  このような読んでから書く処理は、1つの`@Transaction`のDAOメソッドにする(`toggle`、`insertAtEnd`、`replaceAll`)。
  これは実際に起きたことで、`FavoriteSongDao.toggle`があるのはそのためだ
- ViewModelの`load()`は`viewModelScope`で動かし、先に前のジョブをキャンセルする(`loadJob?.cancel()`)。
  `onResume`と`PlaybackController.songRemoved`の両方から呼ばれうるからだ。
  この3行は、あえてすべてのViewModelに繰り返し書いている。
  15個のうち`SongListViewModel`を継承するのは5つだけで、残りの10個は`AndroidViewModel`を直接継承する(Hiltで注入するのは全体で4つ)。
  共通化すると、`cancel()`1つを隠すためにすべての継承関係に新しい基底クラスが要り、
  しかも`viewModelScope.launch`を直接呼ぶことは防げない。`loadJob`を基底クラスに移さないこと
- 編集画面は`OneShotEvent`/`OnEvent`で保存し、書き込みが終わってから画面を離れる。
  すぐに閉じるもの(ドロップダウンメニュー、プレイリストのポップアップ)から始める書き込みは、
  `rememberCoroutineScope()`ではなく`viewModelScope`で行う
- ドロップダウンメニューの項目は、`produceState(initialValue, id)`で状態を読み、effectと状態のkeyを揃える。
  書き込みにはidだけを渡し、表示していた状態は渡さない。メニューを開いている間に古くなっているかもしれないからだ
- `MusicService`は、`ConfigStore`へのアクセスのために`serviceScope`を持つ(`onDestroy`でキャンセルする)。
  `ViewModel`ではなく`Service`なので、`viewModelScope`がない
- RoomのidをMediaStoreと突き合わせるサービス(`FavoriteSongsService`、`ShortcutService`、`PlaylistDetailService`など)は、
  何にも解決しなかったidを削除し、**すでに変換したリストを返す**。
  削除の後に関数を呼び直さないこと。
  idが重複していると、idの件数とMediaStoreの行数がずっと一致しなくなり、読み直すと収束せずに回り続ける

## 作業の取り決め

- **PRは日本語で書く。** タイトル、コミットメッセージ、本文のすべてが対象。
  本文は`## 概要`、`## 理由`、話題ごとの節、`## 動作確認`の順に書く。
  #4から#12はこの取り決めより前のものなので、そのままにする
- **タイトルには、その変更で何が成り立つようになるかを、コードの外から見える言葉で書く。**
  `編集画面で、存在しない id があっても並び替えを保存できるようにする`のように書き、
  変更した関数の名前や`〜を修正`は使わない(どのコミットも変更なので、何も伝わらない)。
  実際に確かめていない症状をタイトルに書かない。
  本文で再現していないと認めているクラッシュをタイトルに書くと、本文と矛盾する。
  何が壊れていたかは`## 概要`に書く
- マージは**squash**で、このリポジトリの`squash_merge_commit_title`は`COMMIT_OR_PR_TITLE`。
  コミットが1つのPRは、PRのタイトルではなく*コミットの*見出しでmainに入るので、見出しをタイトルのつもりで書く。
  マージ後もブランチは残すので、`--delete-branch`は付けない
- **force-pushは、どのブランチでも、どんな形でもしない。**
  `--amend`はpushしていないコミットにだけ使う。
  リモートに上がったコミットの修正は後続のコミットで行い、合わなくなった文言はPRの本文で直す
- `## 動作確認`の基準は、ユニットテスト、件数を付けたインストルメンテーションテスト、**ネガティブコントロール**
  (修正を戻して新しいテストが失敗することを確かめ、元に戻す)。
  確かめていないことは、暗に残さずはっきり書く
- **PRにはその変更のことだけを書く。**
  途中で気づいたほかのバグや、レビューする人に端末での確認を頼むことは、PRの本文ではなく作業後の報告に書く。
  変更より大きなPRになってしまうからだ。
  「Android 13では確かめていない」は`## 動作確認`の一部なので書く。
  「手元の端末で確認してください」は書かない
- コマンドが成功したと報告するときは、結果をコマンドの行に書かない。
  `./gradlew test — pass`はそのままコピーされ、Gradleが`—`をタスク名として扱って失敗する
