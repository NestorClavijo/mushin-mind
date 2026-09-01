package com.mushind.mind.data.local.db

import androidx.room.TypeConverter
import com.mushind.mind.data.local.entity.UnlockSessionStatus
import com.mushind.mind.data.local.entity.UnlockSessionType
import com.mushind.mind.domain.model.DailyPlanStatus
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.ChallengeAttemptStatus
import com.mushind.mind.domain.model.PendingRuleChangeStatus
import com.mushind.mind.domain.model.PointTransactionType
import com.mushind.mind.domain.model.TaskOrigin
import com.mushind.mind.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDate

class RoomConverters {
    @TypeConverter fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()
    @TypeConverter fun longToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)
    @TypeConverter fun dateToString(value: LocalDate?): String? = value?.toString()
    @TypeConverter fun stringToDate(value: String?): LocalDate? = value?.let(LocalDate::parse)
    @TypeConverter fun planStatusToString(value: DailyPlanStatus): String = value.name
    @TypeConverter fun stringToPlanStatus(value: String): DailyPlanStatus = DailyPlanStatus.valueOf(value)
    @TypeConverter fun taskStatusToString(value: TaskStatus): String = value.name
    @TypeConverter fun stringToTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)
    @TypeConverter fun taskOriginToString(value: TaskOrigin): String = value.name
    @TypeConverter fun stringToTaskOrigin(value: String): TaskOrigin = TaskOrigin.valueOf(value)
    @TypeConverter fun transactionTypeToString(value: PointTransactionType): String = value.name
    @TypeConverter fun stringToTransactionType(value: String): PointTransactionType = PointTransactionType.valueOf(value)
    @TypeConverter fun sessionTypeToString(value: UnlockSessionType): String = value.name
    @TypeConverter fun stringToSessionType(value: String): UnlockSessionType = UnlockSessionType.valueOf(value)
    @TypeConverter fun sessionStatusToString(value: UnlockSessionStatus): String = value.name
    @TypeConverter fun stringToSessionStatus(value: String): UnlockSessionStatus = UnlockSessionStatus.valueOf(value)
    @TypeConverter fun appRuleTypeToString(value: AppRuleType): String = value.name
    @TypeConverter fun stringToAppRuleType(value: String): AppRuleType = AppRuleType.valueOf(value)
    @TypeConverter fun challengeStatusToString(value: ChallengeAttemptStatus): String = value.name
    @TypeConverter fun stringToChallengeStatus(value: String): ChallengeAttemptStatus = ChallengeAttemptStatus.valueOf(value)
    @TypeConverter fun pendingStatusToString(value: PendingRuleChangeStatus): String = value.name
    @TypeConverter fun stringToPendingStatus(value: String): PendingRuleChangeStatus = PendingRuleChangeStatus.valueOf(value)
}
