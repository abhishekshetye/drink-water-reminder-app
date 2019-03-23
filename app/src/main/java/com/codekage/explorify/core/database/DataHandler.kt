package com.codekage.explorify.core.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.codekage.explorify.core.entities.WaterConsumed
import java.util.*

/**
 * Created by abhisheksimac on 23/03/19.
 */

class DataHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1){


    companion object {
    const val DATABASE_NAME = "WaterConsumption.db"
    const val TABLE_NAME = "waterConsumption"
    const val ID = "id"
    const val DATE_TIME = "dateTime"
    const val WATER_CONSUMPTION = "waterConsumption"
    }



    override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {
        sqLiteDatabase?.execSQL(
                """create table $TABLE_NAME($ID integer primary key autoincrement, $DATE_TIME text, $WATER_CONSUMPTION integer)"""
        )
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase?, p1: Int, p2: Int) {
        sqLiteDatabase?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(sqLiteDatabase)
    }


    fun saveWaterConsumption(date: String, water: Int) : Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(DATE_TIME, date)
        contentValues.put(WATER_CONSUMPTION, water)
        val success = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        Log.d("DATABASE", "Saved water Consumption in DB")
        return success
    }


    fun getAllWaterConsumptionData() : List<WaterConsumed> {
        val waterConsumptionList = ArrayList<WaterConsumed>()
        val selectQuery = "select * from $TABLE_NAME"
        val db = this.readableDatabase
        var cursor : Cursor?= null
        try{
            cursor = db.rawQuery(selectQuery, null);
        } catch (e: SQLiteException){
            db.execSQL(selectQuery)
            return waterConsumptionList
        }

        var id: Int
        var dateTime: String
        var waterConsumed: Int
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                dateTime = cursor.getString(cursor.getColumnIndex(DATE_TIME))
                waterConsumed = cursor.getInt(cursor.getColumnIndex(WATER_CONSUMPTION))
                val emp= WaterConsumed(id = id, date = dateTime, water = waterConsumed)
                waterConsumptionList.add(emp)
            } while (cursor.moveToNext())
        }
        Log.d("DATABASE", "Fetched water consumption list from DB")
        return waterConsumptionList
    }


    fun getWaterConsumptionByDate(startDate: String, endDate: String) : List<WaterConsumed> {
        val waterConsumptionList = ArrayList<WaterConsumed>()
        val selectQuery = "select * from $TABLE_NAME where $DATE_TIME between '$startDate' and '$endDate'"
        val db = this.readableDatabase
        var cursor: Cursor?
        try{
            Log.d("DATABASE", "Fire SQL: $selectQuery")
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException){
            db.execSQL(selectQuery)
            return waterConsumptionList
        }

        var id: Int
        var dateTime: String
        var waterConsumed: Int
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                dateTime = cursor.getString(cursor.getColumnIndex(DATE_TIME))
                waterConsumed = cursor.getInt(cursor.getColumnIndex(WATER_CONSUMPTION))
                val emp= WaterConsumed(id = id, date = dateTime, water = waterConsumed)
                waterConsumptionList.add(emp)
            } while (cursor.moveToNext())
        }
        Log.d("DATABASE", "Fetched ${waterConsumptionList.size} water consumption list from DB")
        printDataAsLogs(waterConsumptionList)
        return waterConsumptionList
    }

    fun printDataAsLogs(waterConsumptionList: List<WaterConsumed>) {
        for(wc in waterConsumptionList)
            Log.d("DATABASE", "Found ${wc.id} ${wc.water} ${wc.date}")
    }

}