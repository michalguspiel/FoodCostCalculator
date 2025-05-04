package com.erdees.foodcostcalc.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface MyDispatchers {
    val ioDispatcher: CoroutineDispatcher
    val mainDispatcher: CoroutineDispatcher
    val defaultDispatcher: CoroutineDispatcher
}

class MyDispatchersImpl : MyDispatchers {
    override val ioDispatcher: CoroutineDispatcher
        get() = Dispatchers.IO
    override val mainDispatcher: CoroutineDispatcher
        get() = Dispatchers.Main
    override val defaultDispatcher: CoroutineDispatcher
        get() = Dispatchers.Default
}