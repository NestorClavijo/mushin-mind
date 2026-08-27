package com.mushind.mind.platform.debug

import androidx.room.withTransaction
import com.mushind.mind.BuildConfig
import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.data.local.db.MindDatabase
import com.mushind.mind.data.local.entity.PointTransactionEntity
import com.mushind.mind.data.local.entity.UserProgressEntity
import com.mushind.mind.domain.model.PointTransactionType
import javax.inject.Inject

class DebugPointSeeder @Inject constructor(
    private val database: MindDatabase,
    private val idProvider: IdProvider,
    private val clock: ClockProvider,
) {
    suspend fun add(points: Int = 40): Int {
        check(BuildConfig.DEBUG) { "Test points are only available in debug builds" }
        require(points > 0)
        return database.withTransaction {
            val dao = database.unlockSessionDao()
            dao.initializeProgress(UserProgressEntity(balance = 0, xp = 0))
            check(dao.addPoints(points) == 1)
            dao.insertTransaction(
                PointTransactionEntity(
                    id = idProvider.newId(),
                    type = PointTransactionType.CORRECTION,
                    amount = points,
                    referenceId = "debug-seed",
                    description = "Puntos de prueba para validar el shield",
                    createdAt = clock.now(),
                ),
            )
            dao.getBalance() ?: 0
        }
    }
}

