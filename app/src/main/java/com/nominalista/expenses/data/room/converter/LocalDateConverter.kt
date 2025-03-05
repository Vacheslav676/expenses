package com.nominalista.expenses.data.room.converter

import androidx.room.TypeConverter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset

class LocalDateConverter {

    /**
     * First convert time since epoch to the ZonedDateTime. Epoch time is always in UTC.
Сначала преобразуйте время, начиная с эпохи, в ZonedDateTime. Время эпохи всегда указывается в UTC.
     * Then convert ZonedDateTime to LocalDate.
Затем преобразуйте ZonedDateTime в LocalDate.
     *
     * Example:
     * 946684800 -> 2000-01-01T00:00:00+00:00 -> 2000-01-01.
     */
    @TypeConverter
    fun toLocalDate(long: Long): LocalDate =
        Instant.ofEpochMilli(long).atZone(ZoneOffset.UTC).toLocalDate()

    /**
     * First convert LocalDate to the ZonedDateTime. LocalDate doesn't hold zone information, so we
Сначала преобразуйте LocalDate в ZonedDateTime. LocalDate не содержит информации о зоне, поэтому мы
     * can assume that the converted date is in UTC. Then convert ZonedDateTime to time since epoch.
можно предположить, что преобразованная дата находится в UTC. Затем преобразуйте ZonedDateTime в время, прошедшее с эпохи.
     *
     * Example:
     * 2000-01-01 -> 2000-01-01T00:00:00+00:00 -> 946684800.
     */
    @TypeConverter
    fun fromLocalDate(localDate: LocalDate): Long =
        localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}