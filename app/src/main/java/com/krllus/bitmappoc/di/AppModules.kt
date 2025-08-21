package com.krllus.bitmappoc.di

import com.krllus.bfc.BitmapGenerator
import com.krllus.bfc.ComposableBitmapRenderer
import com.krllus.bfc.ComposableBitmapRendererImpl
import org.koin.dsl.module

val appModule = module {
    single<BitmapGenerator> { BitmapGenerator(context = get()) }
    single<ComposableBitmapRenderer> { ComposableBitmapRendererImpl(application = get()) }
}