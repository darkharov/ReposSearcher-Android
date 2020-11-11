package app.darkharov.repossearcher.commons

import android.content.Context
import app.darkharov.repossearcher.R
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class LoadingState<T> {


    class Started<T> : LoadingState<T>()


    class Completed<T>(val data: T) : LoadingState<T>()


    class Fail<T>(val e: Throwable) : LoadingState<T>() {

        fun errorMessage(context: Context) =
            when (e) {

                is ConnectException,
                is SocketTimeoutException,
                is UnknownHostException ->
                    context.getString(R.string.no_internet_connection)

                else ->
                    context.normalizeErrorMessage(e.message)
            }

        private fun Context.normalizeErrorMessage(message: String?) =
            when {
                message.isNullOrBlank() -> getString(R.string.unknown_error)
                else -> message
            }
    }
}
