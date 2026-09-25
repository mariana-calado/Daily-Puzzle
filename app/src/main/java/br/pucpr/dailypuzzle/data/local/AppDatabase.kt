package br.pucpr.dailypuzzle.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import br.pucpr.dailypuzzle.data.local.dao.ProgressDao
import br.pucpr.dailypuzzle.data.local.dao.StatsDao
import br.pucpr.dailypuzzle.data.local.entity.GameProgressEntity
import br.pucpr.dailypuzzle.data.local.entity.GameStatsEntity

@Database(
    entities = [GameProgressEntity::class, GameStatsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun progressDao(): ProgressDao

    abstract fun statsDao(): StatsDao
}
