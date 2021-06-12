package com.prasadsawant.inferasl

import android.app.Application
import android.util.Log
import com.prasadsawant.inferasl.tflite.Classifier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

const val IMAGE_SIZE = 150
const val MODEL = "model.tflite"
const val LABELS = "label.txt"

class MyApplication : Application() {

    val appModule = module {
        single<Classifier> { Classifier(assets, MODEL, LABELS, IMAGE_SIZE) }
    }

    private val TAG = MyApplication::class.java.simpleName

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "Starting koin application")
        startKoin{
            androidLogger()
            androidContext(this@MyApplication)
            koin.loadModules(listOf(appModule))
        }
    }
}