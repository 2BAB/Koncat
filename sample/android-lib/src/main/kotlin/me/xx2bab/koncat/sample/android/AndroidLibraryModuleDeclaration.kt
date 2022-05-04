package me.xx2bab.koncat.sample.android

import org.koin.dsl.module

val androidLibraryModule = module {
    factory { AndroidLibraryAPI2() }
}