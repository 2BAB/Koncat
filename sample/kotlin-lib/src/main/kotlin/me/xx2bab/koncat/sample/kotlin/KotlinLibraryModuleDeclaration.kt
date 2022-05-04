package me.xx2bab.koncat.sample.kotlin

import org.koin.dsl.module

val kotlinLibraryModule = module {
    factory { KotlinLibraryAPI2() }
}