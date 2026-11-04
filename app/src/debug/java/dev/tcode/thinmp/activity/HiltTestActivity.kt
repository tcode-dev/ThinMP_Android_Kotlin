package dev.tcode.thinmp.activity

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * hiltViewModel()を使う部品を、Composeのテストで単独で描くためのActivity。
 * hiltViewModel()はActivityがHiltのコンポーネントを持つことを求めるので、
 * createComposeRule()が使う素のComponentActivityでは作れない。
 * テストのAPKのActivityはアプリのHiltに参加できないので、debugのソースセットに置く。
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
