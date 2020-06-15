package com.crazylegend.kotlinextensions.coroutines

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.crazylegend.kotlinextensions.databaseResult.*
import com.crazylegend.kotlinextensions.retrofit.retrofitResult.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import okhttp3.ResponseBody
import retrofit2.Response


/**
 * Created by hristijan on 5/27/19 to long live and prosper !
 */

suspend inline fun <T, R> T.onMain(crossinline block: (T) -> R): R {
    return withContext(mainDispatcher) { this@onMain.let(block) }
}

suspend inline fun <T> onMain(crossinline block: CoroutineScope.() -> T): T {
    return withContext(mainDispatcher) { block.invoke(this@withContext) }
}

suspend inline fun <T, R> T.onDefault(crossinline block: (T) -> R): R {
    return withContext(defaultDispatcher) { this@onDefault.let(block) }
}

suspend inline fun <T, R> T.nonCancellable(crossinline block: (T) -> R): R {
    return withContext(NonCancellable) { this@nonCancellable.let(block) }
}

suspend inline fun <T> onDefault(crossinline block: CoroutineScope.() -> T): T {
    return withContext(defaultDispatcher) { block.invoke(this@withContext) }
}

suspend inline fun <T, R> T.onIO(crossinline block: (T) -> R): R {
    return withContext(ioDispatcher) { this@onIO.let(block) }
}

suspend inline fun <T> onIO(crossinline block: CoroutineScope.() -> T): T {
    return withContext(ioDispatcher) { block.invoke(this@withContext) }
}

val mainDispatcher = Dispatchers.Main
val defaultDispatcher = Dispatchers.Default
val unconfinedDispatcher = Dispatchers.Unconfined
val ioDispatcher = Dispatchers.IO


fun <T> ioCoroutineGlobal(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, block: suspend () -> T): Job {
    return GlobalScope.launch(ioDispatcher, coroutineStart) {
        block()
    }
}

fun <T> mainCoroutineGlobal(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, block: suspend () -> T): Job {
    return GlobalScope.launch(mainDispatcher, coroutineStart) {
        block()
    }
}

fun <T> defaultCoroutineGlobal(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, block: suspend () -> T): Job {
    return GlobalScope.launch(defaultDispatcher, coroutineStart) {
        block()
    }
}

fun <T> unconfinedCoroutineGlobal(coroutineStart: CoroutineStart = CoroutineStart.DEFAULT, block: suspend () -> T): Job {
    return GlobalScope.launch(unconfinedDispatcher, coroutineStart) {
        block()
    }
}

suspend fun <T> withMainContext(block: suspend () -> T): T {
    return withContext(mainDispatcher) {
        block()
    }
}

suspend fun <T> withIOContext(block: suspend () -> T): T {
    return withContext(ioDispatcher) {
        block()
    }
}


suspend fun <T> withDefaultContext(block: suspend () -> T): T {
    return withContext(defaultDispatcher) {
        block()
    }
}


suspend fun <T> withUnconfinedContext(block: suspend () -> T): T {
    return withContext(Dispatchers.Unconfined) {
        block()
    }
}

suspend fun <T> withNonCancellableContext(block: suspend () -> T): T {
    return withContext(NonCancellable) {
        block()
    }
}


/**

USAGE:

viewModelScope.launch {
makeApiCall(client?.getSomething(), retrofitResult)
}

 * @receiver CoroutineScope
 * @param response Response<T>?
 * @param retrofitResult MutableLiveData<RetrofitResult<T>>
 * @param includeEmptyData Boolean
 * @return Job
 */
fun <T> CoroutineScope.makeApiCall(
        response: Response<T>?,
        retrofitResult: MutableLiveData<RetrofitResult<T>>,
        includeEmptyData: Boolean = false
): Job {
    retrofitResult.loadingPost()
    return launch(ioDispatcher) {
        try {
            retrofitResult.subscribePost(response)
        } catch (t: Throwable) {
            retrofitResult.callErrorPost(t)
        }
    }

}

fun <T> CoroutineScope.makeApiCallList(
        response: Response<T>?,
        retrofitResult: MutableLiveData<RetrofitResult<T>>,
        includeEmptyData: Boolean = true
): Job {
    retrofitResult.loadingPost()
    return launch(ioDispatcher) {
        try {
            retrofitResult.subscribeListPost(response, includeEmptyData)
        } catch (t: Throwable) {
            retrofitResult.callErrorPost(t)
        }
    }

}


/**

USAGE:

viewModelScope.launch {
makeDBCall(db?.getSomething(), dbResult)
}

 * @receiver CoroutineScope
 * @param queryModel Response<T>?
 * @param dbResult MutableLiveData<DBResult<T>>
 * @param includeEmptyData Boolean
 * @return Job
 */
fun <T> CoroutineScope.makeDBCall(
        queryModel: T?,
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = false
): Job {
    dbResult.queryingPost()
    return launch(ioDispatcher) {
        try {
            dbResult.subscribePost(queryModel, includeEmptyData)
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}


/**

USAGE:

viewModelScope.launch {
makeDBCall(db?.getSomething(), dbResult)
}

 * @receiver CoroutineScope
 * @param queryModel Response<T>?
 * @param dbResult MutableLiveData<DBResult<T>>
 * @param includeEmptyData Boolean
 * @return Job
 */
fun <T> CoroutineScope.makeDBCallList(
        queryModel: T?,
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = true
): Job {
    dbResult.queryingPost()
    return launch(ioDispatcher) {
        try {
            dbResult.subscribeListPost(queryModel, includeEmptyData)
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }

}

/**
 * Android View model coroutine extensions
 * Must include the view model androidX for coroutines to provide view model scope
 */


/**
 * USAGE:
makeApiCall(sentResultData) {
retrofitClient?.apiCall()
}
 * @receiver AndroidViewModel
 * @param retrofitResult MutableLiveData<RetrofitResult<T>>
 * @param includeEmptyData Boolean
 * @param apiCall SuspendFunction0<Response<T>?>
 * @return Job
 */
fun <T> ViewModel.makeApiCall(
        retrofitResult: MutableLiveData<RetrofitResult<T>>,
        includeEmptyData: Boolean = false,
        apiCall: suspend () -> Response<T>?): Job {
    retrofitResult.loadingPost()
    return viewModelIOCoroutine {
        try {
            retrofitResult.subscribePost(apiCall())
        } catch (t: Throwable) {
            retrofitResult.callErrorPost(t)
        }
    }
}


/**
 * USAGE:
makeApiCall(sentResultData) {
retrofitClient?.makeApiCallList()
}
 * @receiver AndroidViewModel
 * @param retrofitResult MutableLiveData<RetrofitResult<T>>
 * @param includeEmptyData Boolean
 * @param apiCall SuspendFunction0<Response<T>?>
 * @return Job
 */
fun <T> ViewModel.makeApiCallList(
        retrofitResult: MutableLiveData<RetrofitResult<T>>,
        includeEmptyData: Boolean = true,
        apiCall: suspend () -> Response<T>?): Job {
    retrofitResult.loadingPost()
    return viewModelIOCoroutine {
        try {
            retrofitResult.subscribeListPost(apiCall(), includeEmptyData)
        } catch (t: Throwable) {
            retrofitResult.callErrorPost(t)
        }
    }
}

/**
 * USAGE:
makeApiCall(dbResult) {
db?.getDBSomething()
}
 * @receiver AndroidViewModel
 * @param dbResult MutableLiveData<DBResult<T>>
 * @param includeEmptyData Boolean
 * @param dbCall SuspendFunction0<T?>
 * @return Job
 */
fun <T> ViewModel.makeDBCall(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = false,
        dbCall: suspend () -> T?): Job {
    dbResult.queryingPost()
    return viewModelIOCoroutine {
        try {
            dbResult.subscribePost(dbCall(), includeEmptyData)
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}

/**
 * Must include empty data
 * @receiver AndroidViewModel
 * @param dbResult MutableLiveData<DBResult<T>>
 * @param includeEmptyData Boolean
 * @param dbCall SuspendFunction0<T?>
 * @return Job
 */
fun <T> ViewModel.makeDBCallList(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = true,
        dbCall: suspend () -> T?): Job {
    dbResult.queryingPost()
    return viewModelIOCoroutine {
        try {
            dbResult.subscribeListPost(dbCall(), includeEmptyData)
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}

/**
 * USAGE:
makeApiCall(dbResult) {
db?.getDBSomething()
}
 * @receiver AndroidViewModel
 * @param dbCall SuspendFunction0<T?>
 * @return Job
 */

fun ViewModel.makeDBCall(
        onCallExecuted: () -> Unit = {},
        dbCall: suspend () -> Unit): Job {
    return viewModelIOCoroutine {
        try {
            dbCall()
        } catch (t: Throwable) {
            t.printStackTrace()
        } finally {
            withMainContext {
                onCallExecuted()
            }
        }
    }
}

fun ViewModel.makeDBCall(
        onCallExecuted: () -> Unit = {},
        onErrorAction: (throwable: Throwable) -> Unit = { _ -> },
        dbCall: suspend () -> Unit): Job {
    return viewModelIOCoroutine {
        try {
            dbCall()
        } catch (t: Throwable) {
            onErrorAction(t)
        } finally {
            withMainContext {
                onCallExecuted()
            }
        }
    }
}


/**
 *
 * @receiver AndroidViewModel
 * @param action SuspendFunction0<Unit>
 * @return Job
 */
fun ViewModel.viewModelIOCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return viewModelScope.launch(ioDispatcher) {
        action(this)
    }
}


/**
 *
 * @receiver AndroidViewModel
 * @param action SuspendFunction0<Unit>
 * @return Job
 */
fun ViewModel.viewModelMainCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return viewModelScope.launch(mainDispatcher) {
        action(this)
    }
}


/**
 *
 * @receiver AndroidViewModel
 * @param action SuspendFunction0<Unit>
 * @return Job
 */
fun ViewModel.viewModelDefaultCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return viewModelScope.launch(defaultDispatcher) {
        action(this)
    }
}

/**
 *
 * @receiver AndroidViewModel
 * @param action SuspendFunction0<Unit>
 * @return Job
 */
fun ViewModel.viewModelUnconfinedCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return viewModelScope.launch(unconfinedDispatcher) {
        action(this)
    }
}

/**
 *
 * @receiver ViewModel
 * @param action SuspendFunction0<Unit>
 * @return Job
 */
fun ViewModel.viewModelNonCancellableCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return viewModelScope.launch(NonCancellable) {
        action(this)
    }
}


fun Fragment.ioCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return lifecycleScope.launch(ioDispatcher) {
        action(this)
    }
}

fun Fragment.mainCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return lifecycleScope.launch(mainDispatcher) {
        action(this)
    }
}

fun Fragment.unconfinedCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return lifecycleScope.launch(unconfinedDispatcher) {
        action(this)
    }
}

fun Fragment.defaultCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return lifecycleScope.launch(defaultDispatcher) {
        action(this)
    }
}

fun Fragment.nonCancellableCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return lifecycleScope.launch(NonCancellable) {
        action(this)
    }
}


fun AppCompatActivity.ioCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return lifecycleScope.launch(ioDispatcher) {
        action(this)
    }
}

fun AppCompatActivity.mainCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return lifecycleScope.launch(mainDispatcher) {
        action(this)
    }
}

fun AppCompatActivity.unconfinedCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return lifecycleScope.launch(unconfinedDispatcher) {
        action(this)
    }
}

fun AppCompatActivity.defaultCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return lifecycleScope.launch(defaultDispatcher) {
        action(this)
    }
}

fun AppCompatActivity.nonCancellableCoroutine(action: suspend (scope: CoroutineScope) -> Unit = {}): Job {
    return lifecycleScope.launch(NonCancellable) {
        action(this)
    }
}


/**
 * Appcompat activity coroutine extensions
 * Must include the view model androidX for coroutines to provide view model scope
 */


/**
 * USAGE:
makeApiCall(sentResultData) {
retrofitClient?.apiCall()
}
 * @receiver AndroidViewModel
 * @param retrofitResult MutableLiveData<RetrofitResult<T>>
 * @param includeEmptyData Boolean
 * @param apiCall SuspendFunction0<Response<T>?>
 * @return Job
 */
fun <T> AppCompatActivity.makeApiCall(
        retrofitResult: MutableLiveData<RetrofitResult<T>>,
        includeEmptyData: Boolean = false,
        apiCall: suspend () -> Response<T>?): Job {
    retrofitResult.loadingPost()
    return ioCoroutine {
        try {
            retrofitResult.subscribePost(apiCall())
        } catch (t: Throwable) {
            retrofitResult.callErrorPost(t)
        }
    }
}


/**
 * USAGE:
makeApiCall(sentResultData) {
retrofitClient?.makeApiCallList()
}
 * @receiver AndroidViewModel
 * @param retrofitResult MutableLiveData<RetrofitResult<T>>
 * @param includeEmptyData Boolean
 * @param apiCall SuspendFunction0<Response<T>?>
 * @return Job
 */
fun <T> AppCompatActivity.makeApiCallList(
        retrofitResult: MutableLiveData<RetrofitResult<T>>,
        includeEmptyData: Boolean = true,
        apiCall: suspend () -> Response<T>?): Job {
    retrofitResult.loadingPost()
    return ioCoroutine {
        try {
            retrofitResult.subscribeListPost(apiCall(), includeEmptyData)
        } catch (t: Throwable) {
            retrofitResult.callErrorPost(t)
        }
    }
}

/**
 * USAGE:
makeApiCall(dbResult) {
db?.getDBSomething()
}
 * @receiver AndroidViewModel
 * @param dbResult MutableLiveData<DBResult<T>>
 * @param includeEmptyData Boolean
 * @param dbCall SuspendFunction0<T?>
 * @return Job
 */
fun <T> AppCompatActivity.makeDBCall(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = false,
        dbCall: suspend () -> T?): Job {
    dbResult.queryingPost()
    return ioCoroutine {
        try {
            dbResult.subscribePost(dbCall(), includeEmptyData)
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}

/**
 * Must include empty data
 * @receiver AndroidViewModel
 * @param dbResult MutableLiveData<DBResult<T>>
 * @param includeEmptyData Boolean
 * @param dbCall SuspendFunction0<T?>
 * @return Job
 */
fun <T> AppCompatActivity.makeDBCallList(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = true,
        dbCall: suspend () -> T?): Job {
    dbResult.queryingPost()
    return ioCoroutine {
        try {
            dbResult.subscribeListPost(dbCall(), includeEmptyData)
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}

/**
 * USAGE:
makeApiCall(dbResult) {
db?.getDBSomething()
}
 * @receiver AndroidViewModel
 * @param dbCall SuspendFunction0<T?>
 * @return Job
 */

fun AppCompatActivity.makeDBCall(
        onCallExecuted: () -> Unit = {},
        dbCall: suspend () -> Unit): Job {
    return ioCoroutine {
        try {
            dbCall()
        } catch (t: Throwable) {
            t.printStackTrace()
        } finally {
            withMainContext {
                onCallExecuted()
            }
        }
    }
}

fun AppCompatActivity.makeDBCall(
        onCallExecuted: () -> Unit = {},
        onErrorAction: (throwable: Throwable) -> Unit = { _ -> },
        dbCall: suspend () -> Unit): Job {
    return ioCoroutine {
        try {
            dbCall()
        } catch (t: Throwable) {
            onErrorAction(t)
        } finally {
            withMainContext {
                onCallExecuted()
            }
        }
    }
}


/**
 * Appcompat activity coroutine extensions
 * Must include the view model androidX for coroutines to provide view model scope
 */


/**
 * USAGE:
makeApiCall(sentResultData) {
retrofitClient?.apiCall()
}
 * @receiver AndroidViewModel
 * @param retrofitResult MutableLiveData<RetrofitResult<T>>
 * @param includeEmptyData Boolean
 * @param apiCall SuspendFunction0<Response<T>?>
 * @return Job
 */
fun <T> Fragment.makeApiCall(
        retrofitResult: MutableLiveData<RetrofitResult<T>>,
        includeEmptyData: Boolean = false,
        apiCall: suspend () -> Response<T>?): Job {
    retrofitResult.loadingPost()
    return ioCoroutine {
        try {
            retrofitResult.subscribePost(apiCall())
        } catch (t: Throwable) {
            retrofitResult.callErrorPost(t)
        }
    }
}


/**
 * USAGE:
makeApiCall(sentResultData) {
retrofitClient?.makeApiCallList()
}
 * @receiver AndroidViewModel
 * @param retrofitResult MutableLiveData<RetrofitResult<T>>
 * @param includeEmptyData Boolean
 * @param apiCall SuspendFunction0<Response<T>?>
 * @return Job
 */
fun <T> Fragment.makeApiCallList(
        retrofitResult: MutableLiveData<RetrofitResult<T>>,
        includeEmptyData: Boolean = true,
        apiCall: suspend () -> Response<T>?): Job {
    retrofitResult.loadingPost()
    return ioCoroutine {
        try {
            retrofitResult.subscribeListPost(apiCall(), includeEmptyData)
        } catch (t: Throwable) {
            retrofitResult.callErrorPost(t)
        }
    }
}

/**
 * USAGE:
makeApiCall(dbResult) {
db?.getDBSomething()
}
 * @receiver AndroidViewModel
 * @param dbResult MutableLiveData<DBResult<T>>
 * @param includeEmptyData Boolean
 * @param dbCall SuspendFunction0<T?>
 * @return Job
 */
fun <T> Fragment.makeDBCall(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = false,
        dbCall: suspend () -> T?): Job {
    dbResult.queryingPost()
    return ioCoroutine {
        try {
            dbResult.subscribePost(dbCall(), includeEmptyData)
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}

/**
 * Must include empty data
 * @receiver AndroidViewModel
 * @param dbResult MutableLiveData<DBResult<T>>
 * @param includeEmptyData Boolean
 * @param dbCall SuspendFunction0<T?>
 * @return Job
 */
fun <T> Fragment.makeDBCallList(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = true,
        dbCall: suspend () -> T?): Job {
    dbResult.queryingPost()
    return ioCoroutine {
        try {
            dbResult.subscribeListPost(dbCall(), includeEmptyData)
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}

/**
 * USAGE:
makeApiCall(dbResult) {
db?.getDBSomething()
}
 * @receiver AndroidViewModel
 * @param dbCall SuspendFunction0<T?>
 * @return Job
 */

fun Fragment.makeDBCall(
        onCallExecuted: () -> Unit = {},
        dbCall: suspend () -> Unit): Job {
    return ioCoroutine {
        try {
            dbCall()
        } catch (t: Throwable) {
            t.printStackTrace()
        } finally {
            withMainContext {
                onCallExecuted()
            }
        }
    }
}

fun Fragment.makeDBCall(
        onCallExecuted: () -> Unit = {},
        onErrorAction: (throwable: Throwable) -> Unit = { _ -> },
        dbCall: suspend () -> Unit): Job {
    return ioCoroutine {
        try {
            dbCall()
        } catch (t: Throwable) {
            onErrorAction(t)
        } finally {
            withMainContext {
                onCallExecuted()
            }
        }
    }
}


//Api call without wrappers
fun <T> ViewModel.makeApiCall(apiCall: suspend () -> Response<T>?,
                              onError: (throwable: Throwable) -> Unit = { _ -> },
                              onUnsuccessfulCall: (errorBody: ResponseBody?, responseCode: Int) -> Unit = { _, _ -> },
                              onResponse: (response: T?) -> Unit
): Job {

    return viewModelIOCoroutine {
        try {
            val response = apiCall()
            response?.apply {
                if (isSuccessful) {
                    onResponse(body())
                } else {
                    onUnsuccessfulCall(errorBody(), code())
                }
            }

        } catch (t: Throwable) {
            onError(t)
        }
    }
}


fun <T> CoroutineScope.makeApiCall(apiCall: suspend () -> Response<T>?,
                                   onError: (throwable: Throwable) -> Unit = { _ -> },
                                   onUnsuccessfulCall: (errorBody: ResponseBody?, responseCode: Int) -> Unit = { _, _ -> },
                                   onResponse: (response: T?) -> Unit
): Job {

    return launch(ioDispatcher) {
        try {
            val response = apiCall()
            response?.apply {
                if (isSuccessful) {
                    onResponse(body())
                } else {
                    onUnsuccessfulCall(errorBody(), code())
                }
            }
        } catch (t: Throwable) {
            onError(t)
        }
    }
}


/**
 * USAGE:
makeApiCall(sentResultData) {
retrofitClient?.apiCall()
}
 * @receiver AndroidViewModel
 * @param retrofitResult MutableLiveData<RetrofitResult<T>>
 * @param includeEmptyData Boolean
 * @param apiCall SuspendFunction0<Response<T>?>
 * @return Job
 */
fun <T> CoroutineScope.makeApiCall(
        retrofitResult: MutableLiveData<RetrofitResult<T>>,
        includeEmptyData: Boolean = false,
        apiCall: suspend () -> Response<T>?): Job {
    retrofitResult.loadingPost()
    return launch(ioDispatcher) {
        try {
            retrofitResult.subscribePost(apiCall())
        } catch (t: Throwable) {
            retrofitResult.callErrorPost(t)

        }
    }

}


/**
 * USAGE:
makeApiCall(sentResultData) {
retrofitClient?.makeApiCallList()
}
 * @receiver AndroidViewModel
 * @param retrofitResult MutableLiveData<RetrofitResult<T>>
 * @param includeEmptyData Boolean
 * @param apiCall SuspendFunction0<Response<T>?>
 * @return Job
 */
fun <T> CoroutineScope.makeApiCallList(
        retrofitResult: MutableLiveData<RetrofitResult<T>>,
        includeEmptyData: Boolean = true,
        apiCall: suspend () -> Response<T>?): Job {
    retrofitResult.loadingPost()

    return launch(ioDispatcher) {
        try {
            retrofitResult.subscribeListPost(apiCall(), includeEmptyData)
        } catch (t: Throwable) {
            retrofitResult.callErrorPost(t)

        }
    }

}

/**
 * USAGE:
makeApiCall(dbResult) {
db?.getDBSomething()
}
 * @receiver AndroidViewModel
 * @param dbResult MutableLiveData<DBResult<T>>
 * @param includeEmptyData Boolean
 * @param dbCall SuspendFunction0<T?>
 * @return Job
 */
fun <T> CoroutineScope.makeDBCall(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = false,
        dbCall: suspend () -> T?): Job {
    dbResult.queryingPost()

    return launch(ioDispatcher) {
        try {
            dbResult.subscribePost(dbCall(), includeEmptyData)
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)

        }
    }
}

/**
 * Must include empty data
 * @receiver AndroidViewModel
 * @param dbResult MutableLiveData<DBResult<T>>
 * @param includeEmptyData Boolean
 * @param dbCall SuspendFunction0<T?>
 * @return Job
 */
fun <T> CoroutineScope.makeDBCallList(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = true,
        dbCall: suspend () -> T?): Job {
    dbResult.queryingPost()

    return launch(ioDispatcher) {
        try {
            dbResult.subscribeListPost(dbCall(), includeEmptyData)
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)

        }
    }
}

/**
 * USAGE:
makeApiCall(dbResult) {
db?.getDBSomething()
}
 * @receiver AndroidViewModel
 * @param dbCall SuspendFunction0<T?>
 * @return Job
 */

fun CoroutineScope.makeDBCall(
        onCallExecuted: () -> Unit = {},
        dbCall: suspend () -> Unit): Job {

    return launch(ioDispatcher) {
        try {
            dbCall()
        } catch (t: Throwable) {
            t.printStackTrace()
        } finally {
            launch(mainDispatcher) {
                onCallExecuted()
            }
        }
    }
}

fun CoroutineScope.makeDBCall(
        onCallExecuted: () -> Unit = {},
        onErrorAction: (throwable: Throwable) -> Unit = { _ -> },
        dbCall: suspend () -> Unit): Job {
    return launch(ioDispatcher) {
        try {
            dbCall()
        } catch (t: Throwable) {
            t.printStackTrace()
            launch(mainDispatcher) {
                onErrorAction(t)
            }
        } finally {
            launch(mainDispatcher) {
                onCallExecuted()
            }
        }
    }
}


//flow

inline fun <T> CoroutineScope.makeDBCallListFlow(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = true,
        crossinline onFlow: Flow<T>?.() -> Unit = {},
        crossinline dbCall: suspend () -> Flow<T>?): Job {
    dbResult.queryingPost()

    return launch(ioDispatcher) {
        try {

            val result = dbCall()
            result.onFlow()
            result?.collect {
                dbResult.subscribeListPost(it, includeEmptyData)
            }
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)

        }
    }
}

inline fun <T> CoroutineScope.makeDBCallFlow(
        queryModel: Flow<T>?,
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = false,
        crossinline onFlow: Flow<T>?.() -> Unit = {}

): Job {
    dbResult.queryingPost()
    return launch(ioDispatcher) {
        try {
            queryModel.onFlow()
            queryModel?.collect {
                dbResult.subscribePost(it, includeEmptyData)
            }
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}

inline fun <T> CoroutineScope.makeDBCallListFlow(
        queryModel: Flow<T>?,
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = true,
        crossinline onFlow: Flow<T>?.() -> Unit = {}
): Job {
    dbResult.queryingPost()
    return launch(ioDispatcher) {
        try {
            queryModel.onFlow()
            queryModel?.collect {
                dbResult.subscribeListPost(it, includeEmptyData)
            }
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }

}


inline fun <T> ViewModel.makeDBCallFlow(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = false,
        crossinline onFlow: Flow<T>?.() -> Unit = {},
        crossinline dbCall: suspend () -> Flow<T>?): Job {
    dbResult.queryingPost()
    return viewModelIOCoroutine {
        try {
            val flow = dbCall()
            flow.onFlow()
            flow?.collect {
                dbResult.subscribePost(it, includeEmptyData)
            }
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}


inline fun <T> ViewModel.makeDBCallListFlow(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = true,
        crossinline onFlow: Flow<T>?.() -> Unit = {},
        crossinline dbCall: suspend () -> Flow<T>?): Job {
    dbResult.queryingPost()
    return viewModelIOCoroutine {
        try {
            val flow = dbCall()
            flow.onFlow()
            flow?.collect {
                dbResult.subscribeListPost(it, includeEmptyData)
            }
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}


inline fun <T> AppCompatActivity.makeDBCallFlow(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = false,
        crossinline onFlow: Flow<T>?.() -> Unit = {},
        crossinline dbCall: suspend () -> Flow<T>?): Job {
    dbResult.queryingPost()
    return ioCoroutine {
        try {
            val flow = dbCall()
            flow.onFlow()
            flow?.collect {
                dbResult.subscribePost(it, includeEmptyData)
            }
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}


inline fun <T> AppCompatActivity.makeDBCallListFlow(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = true,
        crossinline onFlow: Flow<T>?.() -> Unit = {},
        crossinline dbCall: suspend () -> Flow<T>?): Job {
    dbResult.queryingPost()
    return ioCoroutine {
        try {
            val flow = dbCall()
            flow.onFlow()
            flow?.collect {
                dbResult.subscribeListPost(it, includeEmptyData)
            }
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}


inline fun <T> Fragment.makeDBCallFlow(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = false,
        crossinline onFlow: Flow<T>?.() -> Unit = {},
        crossinline dbCall: suspend () -> Flow<T>?): Job {
    dbResult.queryingPost()
    return ioCoroutine {
        try {
            val flow = dbCall()
            flow.onFlow()
            flow?.collect {
                dbResult.subscribeListPost(it, includeEmptyData)
            }
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}


inline fun <T> Fragment.makeDBCallListFlow(
        dbResult: MutableLiveData<DBResult<T>>,
        includeEmptyData: Boolean = true,
        crossinline onFlow: Flow<T>?.() -> Unit = {},
        crossinline dbCall: suspend () -> Flow<T>?): Job {
    dbResult.queryingPost()
    return ioCoroutine {
        try {
            val flow = dbCall()
            flow.onFlow()
            flow?.collect {
                dbResult.subscribeListPost(it, includeEmptyData)
            }
        } catch (t: Throwable) {
            dbResult.callErrorPost(t)
        }
    }
}

// no wrappers getting the result straight up
fun <T> ViewModel.makeDBCall(
        onCallExecuted: () -> Unit = {},
        onErrorAction: (throwable: Throwable) -> Unit = { _ -> },
        dbCall: suspend () -> T,
        onCalled: (model: T) -> Unit): Job {
    return viewModelIOCoroutine {
        try {
            val call = dbCall()
            viewModelMainCoroutine {
                onCalled(call)
            }
        } catch (t: Throwable) {
            onErrorAction(t)
        } finally {
            withMainContext {
                onCallExecuted()
            }
        }
    }
}

fun <T> CoroutineScope.makeDBCall(
        onCallExecuted: () -> Unit = {},
        onErrorAction: (throwable: Throwable) -> Unit = { _ -> },
        dbCall: suspend () -> T,
        onCalled: (model: T) -> Unit): Job {
    return launch(ioDispatcher) {
        try {
            val call = dbCall()
            launch(mainDispatcher) {
                onCalled(call)
            }
        } catch (t: Throwable) {
            onErrorAction(t)
        } finally {
            launch(mainDispatcher) {
                onCallExecuted()
            }
        }
    }
}

fun <T> Fragment.makeDBCall(
        onCallExecuted: () -> Unit = {},
        onErrorAction: (throwable: Throwable) -> Unit = { _ -> },
        dbCall: suspend () -> T,
        onCalled: (model: T) -> Unit): Job {
    return ioCoroutine {
        try {
            val call = dbCall()
            mainCoroutine {
                onCalled(call)
            }
        } catch (t: Throwable) {
            onErrorAction(t)
        } finally {
            withMainContext {
                onCallExecuted()
            }
        }
    }
}

fun <T> AppCompatActivity.makeDBCall(
        onCallExecuted: () -> Unit = {},
        onErrorAction: (throwable: Throwable) -> Unit = { _ -> },
        dbCall: suspend () -> T,
        onCalled: (model: T) -> Unit): Job {
    return ioCoroutine {
        try {
            val call = dbCall()
            mainCoroutine {
                onCalled(call)
            }
        } catch (t: Throwable) {
            onErrorAction(t)
        } finally {
            withMainContext {
                onCallExecuted()
            }
        }
    }
}

// no wrappers getting the result straight up for flow

fun <T> ViewModel.makeDBCallFlow(
        onCallExecuted: () -> Unit = {},
        onErrorAction: (throwable: Throwable) -> Unit = { _ -> },
        dbCall: suspend () -> Flow<T>,
        onCalled: (model: T) -> Unit): Job {
    return viewModelIOCoroutine {
        try {
            val call = dbCall()
            call.collect { model ->
                withMainContext {
                    onCalled(model)
                }
            }

        } catch (t: Throwable) {
            onErrorAction(t)
        } finally {
            withMainContext {
                onCallExecuted()
            }
        }
    }
}

fun <T> CoroutineScope.makeDBCallFlow(
        onCallExecuted: () -> Unit = {},
        onErrorAction: (throwable: Throwable) -> Unit = { _ -> },
        dbCall: suspend () -> Flow<T>,
        onCalled: (model: T) -> Unit): Job {
    return launch(ioDispatcher) {
        try {
            val call = dbCall()
            call.collect {
                withMainContext {
                    onCalled(it)
                }
            }

        } catch (t: Throwable) {
            onErrorAction(t)
        } finally {
            launch(mainDispatcher) {
                onCallExecuted()
            }
        }
    }
}

fun <T> Fragment.makeDBCallFlow(
        onCallExecuted: () -> Unit = {},
        onErrorAction: (throwable: Throwable) -> Unit = { _ -> },
        dbCall: suspend () -> Flow<T>,
        onCalled: (model: T) -> Unit): Job {
    return ioCoroutine {
        try {
            val call = dbCall()
            call.collect { model ->
                withMainContext {
                    onCalled(model)
                }
            }

        } catch (t: Throwable) {
            onErrorAction(t)
        } finally {
            withMainContext {
                onCallExecuted()
            }
        }
    }
}

fun <T> AppCompatActivity.makeDBCallFlow(
        onCallExecuted: () -> Unit = {},
        onErrorAction: (throwable: Throwable) -> Unit = { _ -> },
        dbCall: suspend () -> Flow<T>,
        onCalled: (model: T) -> Unit): Job {
    return ioCoroutine {
        try {
            val call = dbCall()
            call.collect { model ->
                withMainContext {
                    onCalled(model)
                }
            }
        } catch (t: Throwable) {
            onErrorAction(t)
        } finally {
            withMainContext {
                onCallExecuted()
            }
        }
    }
}