package com.example.mytest

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.mytest.data.db.AppDatabase
import com.example.mytest.data.prefs.PreferencesKeys
import com.example.mytest.data.prefs.PreferencesRepositoryImpl
import com.example.mytest.data.repository.DefaultAlarmRepository
import com.example.mytest.domain.challenge.ArithmeticTaskGenerator
import com.example.mytest.domain.challenge.ChallengeProvider
import com.example.mytest.domain.repository.AlarmRepository
import com.example.mytest.domain.repository.PreferencesRepository
import com.example.mytest.domain.ringing.RingingController
import com.example.mytest.domain.scheduler.AlarmScheduler
import com.example.mytest.domain.usecase.CreateAlarmUseCase
import com.example.mytest.domain.usecase.SnoozeUseCase
import com.example.mytest.domain.usecase.SubmitAnswerUseCase
import com.example.mytest.domain.usecase.TriggerAlarmUseCase
import com.example.mytest.system.alarm.AlarmManagerScheduler
import com.example.mytest.system.alarm.AndroidRingingController
import com.example.mytest.ui.alarm.AlarmViewModel

/**
 * Manual service-locator used as a stand-in for Hilt until the DI graph
 * lands. Every singleton is created lazily on first access; nothing is
 * touched until the first [AlarmViewModel] is requested.
 *
 * Also referenced from [com.example.mytest.system.boot.AlarmRescheduleWorker]
 * (which can't take constructor arguments without a custom WorkerFactory).
 */
object AppGraph {

    private lateinit var application: Application

    fun init(app: Application) {
        application = app
    }

    private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = PreferencesKeys.DATASTORE_NAME,
    )

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        ).build()
    }

    val alarmScheduler: AlarmScheduler by lazy {
        AlarmManagerScheduler(application)
    }

    val alarmRepository: AlarmRepository by lazy {
        DefaultAlarmRepository(database.alarmDao(), alarmScheduler)
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepositoryImpl(application.userPreferencesDataStore)
    }

    val challengeProvider: ChallengeProvider by lazy {
        ArithmeticTaskGenerator()
    }

    val ringingController: RingingController by lazy {
        AndroidRingingController(application)
    }

    private val createAlarmUseCase by lazy {
        CreateAlarmUseCase(alarmRepository, preferencesRepository)
    }
    private val triggerAlarmUseCase by lazy {
        TriggerAlarmUseCase(alarmRepository, challengeProvider)
    }
    private val submitAnswerUseCase by lazy { SubmitAnswerUseCase(alarmRepository) }
    private val snoozeUseCase by lazy { SnoozeUseCase(alarmRepository, preferencesRepository) }

    /** Factory used in `viewModel(factory = AppGraph.alarmViewModelFactory)`. */
    val alarmViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
                "Unknown ViewModel: $modelClass"
            }
            return AlarmViewModel(
                alarmRepository = alarmRepository,
                preferencesRepository = preferencesRepository,
                challengeProvider = challengeProvider,
                createAlarmUseCase = createAlarmUseCase,
                triggerAlarmUseCase = triggerAlarmUseCase,
                submitAnswerUseCase = submitAnswerUseCase,
                snoozeUseCase = snoozeUseCase,
                ringingController = ringingController,
            ) as T
        }
    }
}
