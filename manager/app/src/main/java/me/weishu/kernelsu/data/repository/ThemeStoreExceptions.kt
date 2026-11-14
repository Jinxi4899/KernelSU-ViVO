package me.weishu.kernelsu.data.repository

import java.io.IOException

/** The mirror rejected the device credential even after it was re-acquired. */
class ThemeStoreUnauthorizedException : IOException("Theme store token rejected")
